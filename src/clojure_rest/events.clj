(ns clojure-rest.events
  (:use ring.util.response)
  (:require [clojure.java.jdbc :as sql]
            [clojure-rest.db :as db]))


;; () -> ring.util.response<SQLQuery>
(defn get-all-events []
  (response
    (sql/with-connection (db/db-connection)
                         (sql/with-query-results results
                                                 ["select * from events"]
                                                 (into [] results)))))


;; UUID -> ring.util.response<SQLQuery, 404Error>
(defn get-event [id]
  (sql/with-connection (db/db-connection)
                       (sql/with-query-results results
                                               ["select * from events where eventsId = ?" id]
                                               (cond (empty? results) {:status 404}
                                                 :else (response (first results))))))

;; UUID -> ring.util.response<SQLQuery, 404Error>
(defn create-new-event [content]
  (let [id (db/uuid)]
    (sql/with-connection (db/db-connection)
                         (let [event (assoc content "eventsId" id)]
                           (sql/insert-record :events event)))
    (get-event id)))


;; UUID -> ring.util.response<SQLQuery, 404Error>
(defn update-event [id content]
  (sql/with-connection (db/db-connection)
                       (let [event (assoc content "eventsId" id)]
                         (sql/update-values :events ["eventsId=?" id] event)))
  (get-event id))


;; UUID -> ring.util.response<SQLQuery, 204Error>
(defn delete-event [id]
  (sql/with-connection (db/db-connection)
                       (sql/delete-rows :events ["eventsId=?" id]))
  {:status 204})
