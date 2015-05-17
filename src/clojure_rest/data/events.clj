(ns clojure-rest.data.events
  (:use ring.util.response)
  (:require [clojure.java.jdbc :as sql]
            [clojure-rest.data.db :as db]
            [clojure-rest.util.http :as h]
            [clojure-rest.util.event-validate :as ev]
            [clojure-rest.util.error :refer [bind-error]]))


;; String -> Either<{}|nil>
;; Returns a either nil or the brief contents of the supplied event id
(defn- event-brief-extract! [id]
  (sql/with-connection (db/db-connection)
                       (sql/with-query-results results
                                               ["select events.eventsId, title, count(usersId), latitude, longitude
                                                from events
                                                inner join events_attendees on (events.eventsId = events_attendees.eventsId)
                                                inner join coordinates on (events.coordinatesId = coordinates.coordinatesId)
                                                where events.eventsId = ?" id]
                                               (cond (empty? results) nil
                                                     :else (first results)))))


;; [String?, Error?] -> [{}?, Error?]
;; Binds the result of event-brief-extract! to an error tuple
(defn- bind-event-brief-extract [params]
  (bind-error #(let [res (event-brief-extract! %)]
                 (if (nil? res)
                   [nil 404]
                   [res nil])) params))


;; String -> Response[:body? :status Either<200|404>]
;; Returns a response with either the contents of the given event or a 404 not found (if error)
(defn get-event-case [id]
  (->> id
       clojure.string/trim
       (#(if (ev/event-exists? %) [% nil] [nil 404]))
       bind-event-brief-extract
       h/wrap-response))

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
