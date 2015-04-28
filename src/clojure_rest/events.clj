(ns clojure-rest.events
  (:use ring.util.response)
  (:require [clojure.java.jdbc :as sql]
            [clojure-rest.db :as db]))


;; () -> Response[:body String]
;; Returns a response with the contents of all the events in the database
(defn get-all-events []
  (response
    (sql/with-connection (db/db-connection)
                         (sql/with-query-results results
                                                 ["select * from events"]
                                                 (into [] results)))))


;; UUID -> Response[:body String]
;; UUID -> Response[:body null :status 404]
;; Returns a response with the contents of the specified event
(defn get-event [id]
  (sql/with-connection (db/db-connection)
                       (sql/with-query-results results
                                               ["select * from events where eventsId = ?" id]
                                               (cond (empty? results) {:status 404}
                                                 :else (response (first results))))))


;; {} -> Response[:body String]
;; {} -> Response[:body null :status 404]
;; Creates a new event with the provided content, then returns said event
;; See get-event
(defn create-new-event [content]
  (let [id (db/uuid)]
    (sql/with-connection (db/db-connection)
                         (let [event (assoc content "eventsId" id)]
                           (sql/insert-record :events event)))
    (get-event id)))


;; UUID, {} -> Response[:body String]
;; UUID, {} -> Response[:body null :status 404]
;; Updates the specified event with the provided content, then returns said event
;; See get-event
(defn update-event [id content]
  (sql/with-connection (db/db-connection)
                       (let [event (assoc content "eventsId" id)]
                         (sql/update-values :events ["eventsId=?" id] event)))
  (get-event id))


;; UUID -> Response[:status 204]
;; Deletes the specified event, then returns a 204 http code
(defn delete-event [id]
  (sql/with-connection (db/db-connection)
                       (sql/delete-rows :events ["eventsId=?" id]))
  {:status 204})
