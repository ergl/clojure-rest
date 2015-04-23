(ns clojure-rest.comments
  (:use ring.util.response)
  (:require [clojure.java.jdbc :as sql]
            [clojure-rest.db :as db]))


;; () -> ring.util.response<SQLQuery>
(defn get-all-comments []
  (response
    (sql/with-connection (db/db-connection)
                         (sql/with-query-results results
                                                 ["select * from comments"]
                                                 (into [] results)))))

;; UUID -> ring.util.response<SQLQuery, 404Error>
(defn get-comment [id]
  (sql/with-connection (db/db-connection)
                       (sql/with-query-results results
                                               ["select * from comments where commentsId = ?" id]
                                               (cond (empty? results) {:status 404}
                                                     :else (response (first results))))))


;; UUID -> ring.util.response<SQLQuery, 404Error>
(defn create-new-comment [content]
  (let [id (db/uuid)]
    (sql/with-connection (db/db-connection)
                         (let [comment (assoc content "commentsId" id)]
                           (sql/insert-record :comments comment)))
    (get-comment id)))


;; UUID -> ring.util.response<SQLQuery, 404Error>
(defn update-comment [id content]
  (sql/with-connection (db/db-connection)
                       (let [comment (assoc content "commentsId" id)]
                         (sql/update-values :comments ["commentsId=?" id] comment)))
  (get-comment id))


;; UUID -> ring.util.response<SQLQuery, 204Error>
(defn delete-comment [id]
  (sql/with-connection (db/db-connection)
                       (sql/delete-rows :comments ["commentsId=?" id]))
  {:status 204})