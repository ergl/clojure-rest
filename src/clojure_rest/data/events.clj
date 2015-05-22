(ns clojure-rest.data.events
  (:use ring.util.response)
  (:require [clojure.java.jdbc :as sql]
            [clojure-rest.data.db :as db]
            [clojure.walk :refer [keywordize-keys]]
            [clojure-rest.util.http :as h]
            [clojure-rest.data.coordinates :as gps]
            [clojure-rest.util.event-sanitize :as es]
            [clojure-rest.util.event-validate :as ev]
            [clojure-rest.util.error :refer [bind-error
                                             err-not-found
                                             status-deleted
                                             err-server-error]]))


(def ^:private brief-query
  (str "select events.eventsId, title, count(usersId) as attending, latitude, longitude "
       "from events "
       "inner join events_attendees on (events.eventsId = events_attendees.eventsId) "
       "inner join coordinates on (events.coordinatesId = coordinates.coordinatesId)"))


;; String -> Either<{}|nil>
;; Returns a either nil or the brief contents of the supplied event id
(defn- event-brief-extract! [id]
  (sql/with-connection (db/db-connection)
                       (sql/with-query-results results
                                               [(str brief-query " where events.eventsId = ?") id]
                                               (when-not (empty? results)
                                                 (first results)))))


;; [String?, Error?] -> [{}?, Error?]
;; Binds the result of event-brief-extract! to an error tuple
(defn- bind-event-brief-extract [params]
  (bind-error #(let [res (event-brief-extract! %)]
                 (if (nil? res)
                   [nil err-not-found]
                   [res nil])) params))



;; {} -> Either<{}|nil>
;; Creates a new event with the provided content, then returns said event
(defn- event-insert! [content]
  (let [id (db/uuid)
        c-id (gps/get-coordinate-id (hash-map :latitude (content :latitude)
                                             :longitude (content :longitude)))
        event-user (hash-map :usersid (content :author) :eventsid id)]
    (sql/with-connection (db/db-connection)
                         (let [event (-> content
                                         (assoc :eventsid id) (assoc :coordinatesid c-id)
                                         (dissoc :author) (dissoc :latitude) (dissoc :longitude))]
                           (sql/insert-record :events event)
                           (sql/insert-record :events_author event-user)
                           (sql/insert-record :events_attendees event-user)))
    (event-brief-extract! id)))


;; [{}?, Error?] -> [{}?, Error?]
(defn- bind-event-insert [params]
  (bind-error #(let [res (event-insert! %)]
                 (if (nil? res)
                   [nil err-server-error]
                   [res nil])) params))


;; () -> Response[:body [{}]?]
;; Returns a map vector of at most 15 events in the database, nil if database is empty
(defn get-all-events []
  (response
    (sql/with-connection (db/db-connection)
                         (sql/with-query-results results
                                                 [(str brief-query " group by events.eventsid limit 15")]
                                                 (when-not (empty? results)
                                                   (vec results))))))


;; {} -> Response[:body val? :status Either<200|400|500>]
;; Returns a response with either the contents of the created event, a 400 bad request, or a 500 server error
(defn create-new-event [content]
  (->> content
       keywordize-keys
       es/sanitize-create
       ev/validate-create
       bind-event-insert
       h/wrap-response))


;; String -> Response[:body? :status Either<200|404>]
;; Returns a response with either the contents of the given event or a 404 not found (if error)
(defn get-event [id]
  (->> id
       clojure.string/trim
       (#(if (ev/event-exists? %) [% nil] [nil err-not-found]))
       bind-event-brief-extract
       h/wrap-response))


;; UUID -> Response[:body String]
;; UUID -> Response[:body null :status 404]
;; Returns a response with the contents of the specified event
(defn get-event-depr [id]
  (sql/with-connection (db/db-connection)
                       (sql/with-query-results results
                                               ["select * from events where eventsId = ?" id]
                                               (cond (empty? results) {:status err-not-found}
                                                     :else (response (first results))))))


;; UUID, {} -> Response[:body String]
;; UUID, {} -> Response[:body null :status 404]
;; Updates the specified event with the provided content, then returns said event
;; See get-event
(defn update-event [id content]
  (sql/with-connection (db/db-connection)
                       (let [event (assoc content "eventsId" id)]
                         (sql/update-values :events ["eventsId=?" id] event)))
  (get-event-depr id))


;; UUID -> Response[:status 204]
;; Deletes the specified event, then returns a 204 http code
(defn delete-event [id]
  (sql/with-connection (db/db-connection)
                       (sql/delete-rows :events ["eventsId=?" id]))
  {:status status-deleted})
