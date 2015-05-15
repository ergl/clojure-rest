(ns clojure-rest.data.users
  (:use ring.util.response)
  (:require [clojure.java.jdbc :as sql]
            [buddy.hashers :as bhash]
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
                   [nil 404]
                   [res nil])) params))


;; [{}?, Error?] -> [{}?, Error?]
(defn- bind-user-brief-extract [params]
  (bind-error #(let [res (user-brief-extract! %)]
                 (if (nil? res)
                   [nil 404]
                   [res nil])) params))


;; {} -> [{}?, Error?]
(defn create-user-case [content]
  (->> content
       keywordize-keys
       us/sanitize-signup
       uv/validate-signup
       bind-user-insert
       h/wrap-response))


(defn get-user-case [content]
  (->> content
       clojure.string/trim
       (#(if (uv/user-exists? %) [% nil] [nil 404]))
       bind-user-brief-extract
       h/wrap-response))


;; () -> Response[:body String]
;; Returns a response with the contents of all the users in the database
(defn get-all-users []
  (response
    (sql/with-connection (db/db-connection)
                         (sql/with-query-results results
                                                 ["select username, name from users"]
                                                 (into [] results)))))


;; String -> Response[:body String]
;; String -> Response[:body null :status 404]
;; Returns a response with the contents of the specified username
(defn get-user [username]
  (sql/with-connection (db/db-connection)
                       (sql/with-query-results results
                                               ["select username, name from users where username = ?" username]
                                               (cond (empty? results) {:status 404}
                                                     :else (response (first results))))))


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


;; String -> {}?
;; Gets the stored hashmap of the given username
(defn- get-user-table [username]
  (sql/with-connection (db/db-connection)
                       (sql/with-query-results results
                                               ["select * from users where username = ?" username]
                                               (cond (empty? results) nil
                                                     :else (first results)))))

;; String, {} -> Response[:body String]
;; String, {} -> Response[:body null :status 404]
;; Updates the specified user with the provided content, then returns said user
;; See get-user
; TODO - Review
(defn update-user [username content]
  (let [user (merge (get-user-table username) (keywordize-keys content))]
    (sql/with-connection (db/db-connection)
                         (sql/update-values :users ["username = ?" username] user))
    (get-user (:username user))))


;; String -> Response[:status 204]
;; String -> Response[:status 404]
;; "Deletes" the specified user, then returns a 204 http code
; TODO: Returns 404 if no username is found
(defn delete-user [username]
  (sql/with-connection (db/db-connection)
                       (sql/update-values :users ["username = ?" username] {:deleted true}))
  {:status 204})
