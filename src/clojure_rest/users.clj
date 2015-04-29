(ns clojure-rest.users
  (:use ring.util.response)
  (:require [clojure.java.jdbc :as sql]
            [clojure-rest.db :as db]))


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


;; {} -> Response[:body String]
;; {} -> Response[:body null :status 404]
;; Creates a new user with the provided content, then returns said user
;; See get-user
(defn create-new-user [content]
  (let [id (db/uuid)]
    (sql/with-connection (db/db-connection)
                         (let [user (assoc content "usersId" id)]
                           (sql/insert-record :users user)))
    (get-user id)))


;; UUID, {} -> Response[:body String]
;; UUID, {} -> Response[:body null :status 404]
;; Updates the specified user with the provided content, then returns said user
;; See get-user
(defn update-user [id content]
  (sql/with-connection (db/db-connection)
                       (let [user (assoc content "usersId" id)]
                         (sql/update-values :users ["usersId=?" id] user)))
  (get-user id))


;; UUID -> Response[:status 204]
;; Deletes the specified user, then returns a 204 http code
(defn delete-user [id]
  (sql/with-connection (db/db-connection)
                       (sql/delete-rows :users ["usersId=?" id]))
  {:status 204})
