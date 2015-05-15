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
            [clojure-rest.util.error :refer [bind-error]]))


;; String -> String
;; Hashes the given password with bcrypt + sha512, 12 iterations
(defn- hash-pass [pass]
  (bhash/encrypt pass))


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
                   [nil 404]
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
                   [nil 500]
                   [res nil])) params))


;; String, {} -> Either<{}|nil>
;; Updates the specified user with the provided content, then returns said user
(defn- user-update! [username content]
  (let [user (merge (uv/get-user-table username) content)]
    (sql/with-connection (db/db-connection)
                         (sql/update-values :users ["username = ?" username] user))
    (user-brief-extract! (user :username))))


;; String, {} -> [{}?, Error?]
;; Binds the user-update! call to an error tuple
(defn- bind-user-update [username params]
  (let [res (user-update! username params)]
    (if (nil? res)
      [nil 500]
      [res nil])))


;; () -> Response[:body String]
;; Returns a response with the contents of all the users in the database
(defn get-all-users []
  (response
    (sql/with-connection (db/db-connection)
                         (sql/with-query-results results
                                                 ["select username, name from users"]
                                                 (into [] results)))))


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
       (#(if (uv/user-exists? %) [% nil] [nil 404]))
       bind-user-brief-extract
       h/wrap-response))


;; String, {} -> Response[:body val? :status Either<200|404|500>]
;; Returns a response with either the contents of the updated user, a 404 not found, or a 500 server error
(defn update-user [username content]
  (if (uv/user-exists? username)
    (do
      (->> content
           keywordize-keys
           (fmap #(clojure.string/trim %))
           (bind-user-update username)
           h/wrap-response))
    {:status 404}))


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


;; String -> Response[:status 204]
;; String -> Response[:status 404]
;; "Deletes" the specified user, then returns a 204 http code
; TODO: Returns 404 if no username is found
(defn delete-user [username]
  (sql/with-connection (db/db-connection)
                       (sql/update-values :users ["username = ?" username] {:deleted true}))
  {:status 204})
