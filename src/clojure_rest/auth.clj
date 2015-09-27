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
            [schema.core :as s]
            [clojure.core.match :refer [match]]
            [defun :refer [defun-]]))


;; Either<String|nil>
;; Gets the secret key from the environment variable secret-key, returns nil if not found
;; (will throw exception at runtime)
(def ^:private SECRET-KEY
  (when-let [key (env :secret-key)] key))


(def ^:private auth-schema
  {:username s/Str :password s/Str})


(defn- valid-auth-schema? [schema]
  (valid-schema? auth-schema schema))


(defn- generate-session
  "
  () -> Str
  Generates a random token with username$timestamp$hmac(sha256, username$timestamp)
  "
  [username date]
  (str username "$" date "$" (sha256-hmac (str username "$" date) SECRET-KEY)))


;; {} -> [{}?, Error?]
;; Checks for the :token field in the given map
(defn- check-token [params]
  (if (params :token)
    [params nil]
    [nil err-unauthorized]))


(defn- token-exists?
  "
  Str -> Bool
  Check if token exists in database
  "
  [token]
  (v/field-exists-in-table? "sessions" "token" token))


(defn- make-token!
  "
  UUID -> Str
  Creates a token and inserts it into the session table, then returns that token
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
  [{:keys [username password] :as input}]
  (if (users/pass-matches? username password)
    [:ok input] [:err err-unauthorized]))


(defun- bind-validate
  ([[:ok content]] (validate-auth content))
  ([[:err error]] [:err error]))


(defn- token-gen [{:keys [username password]}]
  (if-let [{user-id :usersid} (users/get-user-id username)]
    [:ok {:token (make-token! username user-id)}]
    [:err err-not-found]))


(defun- bind-token-gen
  ([[:ok content]] (token-gen content))
  ([[:err error]] [:err error]))


;; String -> Either<{}|nil>
;; Returns the username associated with the given token if that token is less than 6 hours old
;; Returns nil otherwise
(defn- validate-token [token]
  (sql/with-connection (db/db-connection)
                       (sql/with-query-results results
                                               ["select username
                                                from sessions
                                                inner join users on (sessions.usersid = users.usersid)
                                                where token = ?
                                                and createdAt > timestampadd(minute, -21600, current_timestamp)" token]
                                               (when-not (empty? results)
                                                 (first results)))))


;; {} -> [{}?, Error?]
;; Takes a map with a token key and returns one without it
;; Adding the issuer key with the appropiate username
;; Returns an error if (params :token) is not valid
(defn- bind-token-validation [params]
  (let [user-id (:username (validate-token (params :token)))]
    (if (nil? user-id)
      [nil err-unauthorized]
      [(-> params (dissoc :token) (assoc :author user-id)) nil])))


(defn- process-auth
  "
  Given a correct user/pass combination, return an auth token;
  return error otherwise
  "
  [input]
  (match [(-> [:ok input] bind-validate bind-token-gen)]
         [[:ok content]] (response content)
         [[:err error]] (h/empty-response-with-code error)))


(defn auth-handler
  [content]
  (match [(clojure.walk/keywordize-keys content)]
         [(input :guard valid-auth-schema?)] (process-auth input)
         :else (h/bad-request)))


;; {:token? ...} -> [{:issuer ...}?, Error?]
;; Checks if there is a :token key in the supplied map
;; If it does, and it is valid, it dissocs said key and assocs the issuer username
;; If anything goes wrong, returns an error
(defn auth-adapter [content]
  (->> content
       clojure.walk/keywordize-keys
       check-token
       (bind-error bind-token-validation)))


;; String, {} -> Response[:status Either<204|403|404>]
;; Deletes the given token if it exists and the params supply the same token as a parameter
(defn delete-token [token params]
  (if (token-exists? token)
    (->> params
         clojure.walk/keywordize-keys
         check-token
         (bind-error (fn [v] (if (= token (v :token)) [v nil] [nil err-forbidden])))
         (bind-error (fn [v] (do (revoke-token! (v :token)) [(v :token) nil])))
         (bind-error (fn [_] [nil status-deleted]))
         h/wrap-response)
    {:status err-not-found}))
