(ns clojure-rest.auth
  (:require [ring.util.response :refer :all]
            [clojure-rest.validation.validate :as v]
            [clojure-rest.data.users :as users]
            [clojure-rest.sanitize.user-sanitize :as us]
            [pandect.core :refer [sha256-hmac]]
            [environ.core :refer [env]]
            [clojure-rest.db :as db]
            [clojure.java.jdbc :as sql]
            [clojure-rest.util.http :as h]
            [clojure-rest.util.error :refer :all]
            [clojure-rest.util.utils :refer :all]
            [clojure.walk :refer [keywordize-keys]]
            [schema.core :as s]
            [clojure.core.match :refer [match]]
            [defun :refer [defun-]]))


(def ^:private SECRET-KEY
  "Gets the secret key from the environment, returns nil if not found
  (will throw exception at runtime, so don't forget to fill it in"
  (when-let [key (env :secret-key)] key))


(def ^:private auth-schema
  {:username s/Str :password s/Str})


(defn- valid-auth-schema? [schema]
  (valid-schema? auth-schema schema))


(defn- generate-session
  "
  Str -> Str -> Str
  Generates a random token with username$timestamp$hmac(sha256, username$timestamp)
  "
  [username date]
  (str username "$" date "$" (sha256-hmac (str username "$" date) SECRET-KEY)))


(defn- token-exists?
  "
  Str -> Bool
  Check if token exists in database
  "
  [token]
  (v/field-exists-in-table? "sessions" "token" token))


(defn- make-token!
  "
  Str -> UUID -> Str
  Given an username and its id, generate an auth token, and insert into
  the session table. Then, return that token
  "
  [username user-id]
  (try-backoff []
               (let [now (time-now)
                     token (generate-session username now)]
                 (sql/with-connection (db/db-connection)
                                      (sql/insert-values :sessions [] [token
                                                                       user-id
                                                                       (format-time now)]))
                 token)))


(defn- revoke-token!
  "
  Str -> ()
  Delete the token from the database
  "
  [token]
  (sql/with-connection (db/db-connection)
                       (sql/delete-rows :sessions ["token = ?" token])))


(defn- validate-auth
  "
  {:username Str :password Str} -> Either [:ok {}] | [:err Natural]
  Check if the given user / pass combination is correct
  "
  [{:keys [username password] :as auth-map}]
  (if (users/pass-matches? username password)
    [:ok auth-map] [:err err-unauthorized]))


(defun- bind-validate
  ([[:ok input-map]] (validate-auth input-map))
  ([[:err error]] [:err error]))


(defn- token-gen
  "
  {:username Str :password Str} -> Either [:ok {:token Str}] | [:err Natural]
  Given a valid username/password, generate an auth token and return it
  in a map
  "
  [{:keys [username password]}]
  (if-let [{user-id :usersid} (users/get-user-id username)]
    [:ok {:token (make-token! username user-id)}]
    [:err err-not-found]))


(defun- bind-token-gen
  ([[:ok content]] (token-gen content))
  ([[:err error]] [:err error]))


(defn- token->username
  "
  Str -> Str?
  Given a valid auth token, return the username associated with it,
  if the token is less than 6 hours old.
  Returns nil otherwise
  "
  [token]
  (let [query "select username from sessions s, users u
              where s.usersid = u.usersid
              and token = ?
              and s.createdAt > timestampadd(minute, -21600, current_timestamp)"]
    (sql/with-connection (db/db-connection)
                         (sql/with-query-results results
                                                 [query token]
                                                 (when-not (empty? results)
                                                   (:username (first results)))))))


(defn- bind-token-validation
  "
  {:token Str ...} -> [{...}? Error?]
  Takes a map with a token key and returns one without it
  Adding the issuer key with the appropiate username
  Returns an error if the token is not valid
  "
  [token-map]
  (if-let [username (token->username (:token token-map))]
    [(-> token-map (dissoc :token) (assoc :author username)) nil]
    [nil err-unauthorized]))


(defn- process-auth
  "
  {:username Str :password Str} -> Response {:status Either 200 | 403 | 404}
  Given a correct user/pass combination, return an auth token
  Return error otherwise
  "
  [auth-map]
  (match [(-> [:ok auth-map] bind-validate bind-token-gen)]
         [[:ok content]] (response content)
         [[:err error]] (h/empty-response-with-code error)))


(defn auth-handler
  "
  Any -> Response {:status Either 200 | 400 | 403 | 404}
  If given a user / password combination, validate if the password matches,
  generate an auth token, and return it.
  "
  [request-body]
  (match [(keywordize-keys request-body)]
         [(req-map :guard valid-auth-schema?)] (process-auth req-map)
         :else (h/bad-request)))


(defn auth-adapter
  "
  {...} -> [{:issuer ...}? Error?]
  Given a map, check if it contains a :token key.
  If it does, and it is valid, dissoc it and assoc the issuer username
  Return 403 otherwise
  "
  [input-map]
  (let [auth-map (keywordize-keys input-map)]
    (if (contains? auth-map :token)
      (bind-token-validation auth-map)
      [nil err-unauthorized])))


(defn- process-delete-token
  "
  Given two tokens, if they are equal, delete one of them from the database;
  return 403 forbidden otherwise
  "
  [input-token auth-token]
  (if-not (= input-token auth-token)
    (h/forbidden)
    (do
      (revoke-token! auth-token)
      (h/deleted))))


(defn delete-token
  "
  Str -> {:token Str} -> Response {:status Either 204 | 403 | 404 }
  Deletes the given token if it exists and the params supply the same token
  as a parameter
  "
  [token request-body]
  (match [token (keywordize-keys request-body)]
         [(token :guard token-exists?) {:token auth-token}] (process-delete-token token auth-token)
         [_ _] {:status err-not-found}))
