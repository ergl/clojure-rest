(ns clojure-rest.data.users
  (:use ring.util.response)
  (:require [clojure.java.jdbc :as sql]
            [buddy.hashers :as bhash]
            [clojure-rest.util.utils :refer :all]
            [clojure.walk :refer [keywordize-keys]]
            [clojure-rest.data.db :as db]
            [clojure-rest.util.http :as h]
            [clojure-rest.util.user-sanitize :as us]
            [clojure-rest.util.user-validate :as uv]
            [clojure-rest.util.error :refer [bind-error
                                             apply-if-present
                                             bind-to
                                             err-not-found
                                             status-deleted
                                             err-server-error]]))


;; String -> String
;; Hashes the given password with bcrypt + sha512, 12 iterations
(defn- hash-pass [pass]
  (bhash/encrypt pass))


;; String -> Either<String|nil>
;; Gets the UUID of the given user
(defn get-user-id [username]
  (sql/with-connection (db/db-connection)
                       (sql/with-query-results results
                                               ["select usersId from users where username = ?" username]
                                               (when-not (empty? results)
                                                 (first results)))))


;; String -> Either<{}|nil>
;; Returns a response with the contents of the specified username
(defn- user-brief-extract! [username]
  (sql/with-connection (db/db-connection)
                       (sql/with-query-results results
                                               ["select username, name from users where username = ?" username]
                                               (cond (empty? results) nil
                                                     :else (first results)))))


;; [{}?, Error?] -> [{}?, Error?]
(defn- bind-user-brief-extract [params]
  (bind-error #(let [res (user-brief-extract! %)]
                 (if (nil? res)
                   [nil err-not-found]
                   [res nil])) params))


;; {} -> Either<{}|nil>
;; Creates a new user with the provided content, then returns said user
(defn- user-insert! [content]
  (let [id (db/uuid)]
    (sql/with-connection (db/db-connection)
                         (let [user (assoc content :usersId id :password (hash-pass (content :password)))]
                           (sql/insert-record :users user)))
    (user-brief-extract! (content :username))))


;; [{}?, Error?] -> [{}?, Error?]
(defn- bind-user-insert [params]
  (bind-error #(let [res (user-insert! %)]
                 (if (nil? res)
                   [nil err-server-error]
                   [res nil])) params))


;; String, {} -> Either<{}|nil>
;; Updates the specified user with the provided content, then returns said user
(defn- user-update! [username content]
  (let [user (merge (uv/get-user-table username) content)]
    (sql/with-connection (db/db-connection)
                         (sql/update-values :users ["username = ?" username] user))
    (user-brief-extract! (user :username))))


;; String, [{}?, Error?] -> [{}?, Error?]
;; Binds the user-update! call to an error tuple
(defn- bind-user-update [username [val err]]
  (if (nil? err)
    (let [res (user-update! username val)]
      (if (nil? res) [nil err-server-error] [res nil]))
    [nil err]))


;; String -> ()
;; Deletes the given user from the table.
;; Returns nothing
(defn- user-delete! [username]
  (let [user-id ((get-user-id username) :usersid)]
    (sql/with-connection (db/db-connection)
                         (sql/update-values :users ["username = ?" username] {:deleted true})
                         (sql/delete-rows :sessions ["usersId = ?" user-id]))))


;; String -> Natural
;; Binds the deletion of the given user to 204 no content (resource deleted)
(defn- bind-user-delete [user]
  (->> (-!>> user user-delete!)
       (#(when (= user %) status-deleted))))


;; String -> [{}]?
;; Returns a list of 5 users that match the supplied username
(defn- match-users [username]
  (sql/with-connection (db/db-connection)
                       (sql/with-query-results results
                                               ["select username, profileImage from users where username like '%' || ? || '%' limit 5" username]
                                               (when-not (empty? results)
                                                 (into {} results)))))


;; String -> [[{}?], Error?]
(defn- bind-match-users [username]
  (let [result (match-users username)]
    (if (nil? result)
      [nil err-not-found]
      [result nil])))


;; () -> Response[:body []?]
;; Returns a response with the contents of all the users in the database
(defn get-all-users []
  (response
    (sql/with-connection (db/db-connection)
                         (sql/with-query-results results
                                                 ["select username, name from users where deleted = false"]
                                                 (when-not (empty? results)
                                                   (into [] results))))))


;; {} -> Response[:body val? :status Either<200|400|500>]
;; Returns a response with either the contents of the created user, a 400 bad request, or a 500 server error
(defn create-new-user [content]
  (->> content
       keywordize-keys
       us/sanitize-signup
       uv/validate-signup
       bind-user-insert
       h/wrap-response))


;; String -> Response[:body val? :status Either<200|404>]
;; Returns a response with either the contents of the given user or a 404 not found (if error)
(defn get-user [username]
  (->> username
       clojure.string/trim
       (#(if (uv/user-exists? %) [% nil] [nil err-not-found]))
       bind-user-brief-extract
       h/wrap-response))


;; String, {} -> Response[:body val? :status Either<200|404|500>]
;; Returns a response with either the contents of the updated user, a 404 not found, or a 500 server error
(defn update-user [username content]
  (if (uv/user-exists? username)
    (do
      (->> content
           keywordize-keys
           us/sanitize-update
           uv/validate-update
           (apply-if-present #(bind-to (assoc % :password (hash-pass (% :password)))) :password)
           (bind-user-update username)
           h/wrap-response))
    {:status err-not-found}))


;; String -> Response[:status Either<204|404>]
;; Returns a response with either 204 no content (user deleted) or 404 (user not found)
(defn delete-user [username]
  (if (uv/user-exists? username)
    (do
      (->> username
           bind-user-delete
           h/empty-response-with-code))
    {:status err-not-found}))


;; String -> Response[:body [{}?] :status Either<200|404>]
;; Returns a response with either the matches of the supplied username, or 404 not found
(defn search-users [username]
  (->> username
       clojure.string/trim
       bind-match-users
       h/wrap-response))


;; String, String -> Boolean
;; Check if the supplied password matches with the hashed password of the given username
(defn pass-matches? [username password]
  (if (uv/user-exists? username)
    (sql/with-connection (db/db-connection)
                         (->> (sql/with-query-results results
                                                      ["select password from users where username = ?" username]
                                                      (into {} results))
                              (:password)
                              (bhash/check password)))))
