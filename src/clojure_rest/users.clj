(ns clojure-rest.users
  (:use ring.util.response)
  (:require [clojure.java.jdbc :as sql]
            [buddy.hashers :as hashers]
            [clojure.walk :refer [keywordize-keys]]
            [clojure-rest.db :as db]
            [clojure-rest.sanitize :as s]
            [clojure-rest.validate :as v]))


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

;; String -> String
;; Hashes the given password with bcrypt + sha512, 12 iterations
(defn- hash-pass [pass]
  (hashers/encrypt pass))


;; String -> Boolean
;; Checks if the user exists in the database
(defn- user-exists? [username]
  (v/field-exists-in-table? "users" "username" username))


;; String -> Boolean
;; Check if the email exists in the database
(defn- email-exists? [email]
  (v/field-exists-in-table? "users" "email" email))


;; String, String -> Boolean
;; Check if the supplied password matches with the hashed password of the given username
(defn pass-matches? [username password]
  (if (user-exists? username)
    (sql/with-connection (db/db-connection)
                         (->> (sql/with-query-results results
                                                      ["select password from users where username = ?" username]
                                                      (into {} results))
                              (:password)
                              (hashers/check password)))))

;; {} -> {}
;; Fills in the default values for a new user
;; From signup form we only get email, username and password
(defn- complete-default-user [content]
  (assoc content :profileImage nil :deleted false :moderator false))


;; {} -> Response[:body String]
;; {} -> Response[:body null :status 404]
;; Creates a new user with the provided content, then returns said user
;; See get-user
; TODO: When updating to put in non-placeholder values, change this approach
; with an assoc-based one
(defn create-new-user [content]
  (let [id (db/uuid)]
    (sql/with-connection (db/db-connection)
                         (sql/insert-values :users []
                                            [id
                                             (content "email")
                                             (content "name")
                                             (content "username")
                                             (hash-pass (content "password"))
                                             ; TODO - Placeholder values
                                             nil
                                             false
                                             false]))
    (get-user (content "username"))))


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
