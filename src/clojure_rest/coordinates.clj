(ns clojure-rest.coordinates
  (:use ring.util.response)
  (:require [clojure.java.jdbc :as sql]
            [clojure-rest.db :as db]))


;; () -> ring.util.response<SQLQuery>
(defn get-all-coordinates []
  (response
    (sql/with-connection (db/db-connection)
                         (sql/with-query-results results
                                                 ["select * from coordinates"]
                                                 (into [] results)))))

;; UUID -> ring.util.response<SQLQuery, 404Error>
(defn get-coordinate [id]
  (sql/with-connection (db/db-connection)
                       (sql/with-query-results results
                                               ["select * from coordinates where coordinatesId = ?" id]
                                               (cond (empty? results) {:status 404}
                                                     :else (response (first results))))))


;; UUID -> ring.util.response<SQLQuery, 404Error>
(defn create-new-coordinate [content]
  (let [id (db/uuid)]
    (sql/with-connection (db/db-connection)
                         (let [coord (assoc content "coordinatesId" id)]
                           (sql/insert-record :coordinates coord)))
    (get-coordinate id)))


;; UUID -> ring.util.response<SQLQuery, 404Error>
(defn update-coordinates [id content]
  (sql/with-connection (db/db-connection)
                       (let [coord (assoc content "coordinatesId" id)]
                         (sql/update-values :coordinates ["coordinatesId=?" id] coord)))
  (get-coordinate id))


;; UUID -> ring.util.response<SQLQuery, 204Error>
(defn delete-coordinate [id]
  (sql/with-connection (db/db-connection)
                       (sql/delete-rows :coordinates ["coordinatesId=?" id]))
  {:status 204})


