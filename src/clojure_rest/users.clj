(ns clojure-rest.users
  (:use ring.util.response)
  (:require [clojure.java.jdbc :as sql]
            [clojure-rest.db :as db]))


;; () -> ring.util.response<SQLQuery>
(defn get-all-users []
  (response
    (sql/with-connection (db/db-connection)
                         (sql/with-query-results results
                                                 ["select * from users"]
                                                 (into [] results)))))

;; UUID -> ring.util.response<SQLQuery, 404Error>
(defn get-user [id]
  (sql/with-connection (db/db-connection)
                       (sql/with-query-results results
                                               ["select * from users where usersId = ?" id]
                                               (cond (empty? results) {:status 404}
                                                     :else (response (first results))))))


;; UUID -> ring.util.response<SQLQuery, 404Error>
(defn create-new-user [content]
  (let [id (db/uuid)]
    (sql/with-connection (db/db-connection)
                         (let [user (assoc content "usersId" id)]
                           (sql/insert-record :users user)))
    (get-user id)))


;; UUID -> ring.util.response<SQLQuery, 404Error>
(defn update-user [id content]
  (sql/with-connection (db/db-connection)
                       (let [user (assoc content "usersId" id)]
                         (sql/update-values :users ["usersId=?" id] user)))
  (get-user id))


;; UUID -> ring.util.response<SQLQuery, 204Error>
(defn delete-user [id]
  (sql/with-connection (db/db-connection)
                       (sql/delete-rows :users ["usersId=?" id]))
  {:status 204})
