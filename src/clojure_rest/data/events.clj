(ns clojure-rest.data.events
  (:use ring.util.response)
  (:require [clojure.java.jdbc :as sql]
            [clojure-rest.db :as db]
            [clojure.walk :refer [keywordize-keys]]
            [clojure-rest.util.http :as h]
            [clojure-rest.data.coordinates :as gps]
            [clojure-rest.data.comments :as comments]
            [clojure-rest.sanitize.event-sanitize :as es]
            [clojure-rest.validation.event-validate :as ev]
            [clojure-rest.util.error :refer [bind-to
                                             bind-error
                                             err-not-found
                                             status-deleted
                                             err-server-error]]))


(def ^:private brief-query
  "Get the id, title, number of attendees and coordinates of an event"
  (str "select events.eventsId, title, count(usersId) as attending, latitude, longitude "
       "from events "
       "inner join events_attendees on (events.eventsId = events_attendees.eventsId) "
       "inner join coordinates on (events.coordinatesId = coordinates.coordinatesId)"))


(def ^:private complete-query
  "Get the id, title, author, body and coordinates of an event"
  (str "select events.eventsid, title, users.username as author, content, latitude, longitude "
       "from events "
       "inner join coordinates on (events.coordinatesid = coordinates.coordinatesid) "
       "inner join events_author on (events_author.eventsid = events.eventsid) "
       "inner join users on (events_author.usersid = users.usersid) "
       "where events.eventsid = ?"))


(def ^:private search-query
  "Search events with the given title or content and get a brief description"
  (str brief-query
       " where (events.title like '%' || ? || '%') or "
       "(events.content like '%' || ? || '%') "
       "group by events.eventsid order by events.title limit 5"))


(defn- event-brief-extract!
  "
  event-brief-extract! :: String -> Maybe {}

  Given an event id, return a brief description from the database.
  If no event matches, returns nil
  "
  [id]
  (sql/with-connection
    (db/db-connection)
    (sql/with-query-results results
                            [(str brief-query " where events.eventsId = ?") id]
                            (when-not (empty? results)
                              (first results)))))


(defn- bind-event-brief-extract
  "
  bind-event-brief-extract :: Either String | Error -> Either {} | Error

  Given an event id, return a brief description of it.
  If given an error, or if the event doesn't exist, return an error
  "
  [either-id]
  (bind-error (fn [id]
                (let [description (event-brief-extract! id)]
                  (if (nil? description)
                    [nil err-not-found]
                    [description nil]))) either-id))


(defn- event-complete-extract!
  "
  event-complete-extract! :: String -> Maybe {}

  Given an event id, return a complete description of it.
  If no event matches, returns nil
  "
  [id]
  (let [comment-count (get (comments/get-comment-count id) :commentcount 0)]
    (sql/with-connection
      (db/db-connection)
      (sql/with-query-results results
                              [complete-query id]
                              (when-not (empty? results)
                                (assoc (first results) :commentcount comment-count))))))


(defn- bind-event-complete-extract
  "
  bind-event-complete-extract :: Either String | Error -> Either {} | Error

  Given an event id, return a full description of it.
  If given an error, or if the event doesn't exist, return an error
  "
  [either-id]
  (bind-error (fn [id]
                (let [description (event-complete-extract! id)]
                  (if (nil? description)
                    [nil err-not-found]
                    [description nil]))) either-id))


(defn- event-insert!
  "
  event-insert! :: {} -> Maybe {}

  Given an event map, insert it into the database.
  It also inserts its author and coordinates into the appropiate tables.
  Then returns a brief description of the given event.
  "
  [content]
  (let [id (db/uuid)
        event-author (hash-map :usersid (content :author) :eventsid id)
        c-id (gps/get-coordinate-id
               (hash-map :latitude (content :latitude)
                         :longitude (content :longitude)))]
    (sql/with-connection
      (db/db-connection)
      (let [event (-> content
                      (dissoc :author :latitude :longitude)
                      (assoc :eventsid id :coordinatesid c-id))]
        (sql/insert-record :events event)
        (sql/insert-record :events_author event-author)
        (sql/insert-record :events_attendees event-author)))
    (event-brief-extract! id)))


(defn- bind-event-insert
  "
  bind-event-insert :: Either {} | Error -> Either {} | Error

  Given an event map, insert it into the database (see event-insert!)
  Returns the brief description of the given event

  If given an error, or if something goes wrong inserting the event,
  return error.
  "
  [either-id]
  (bind-error (fn [id]
                (let [description (event-insert! id)]
                  (if (nil? description)
                    [nil err-server-error]
                    [description nil]))) either-id))


(defn- event-extract-comments!
  "
  event-extract-comments! :: String -> List Maybe {}

  Given an event id, return a list of all its comments
  "
  [id]
  (comments/get-all-event-comments id))


(defn- bind-event-extract-comments
  "
  bind-event-extract-comments :: Either String | Error -> Either List {} | List

  Given an event id, get all its comments.
  If no comments are found, return an empty list
  "
  [params]
  (bind-error (fn [id]
                [(event-extract-comments! id) nil]) params))


(defn- match-events
  "
  match-events :: String -> Either List {} | List

  Given a description, return a list of the first 5 events that match
  their title or body against it.

  If no matches are found, return an empty list.
  "
  [query]
  (sql/with-connection
    (db/db-connection)
    (sql/with-query-results results
                            [search-query query query]
                            (vec results))))


(defn- bind-match-event
  "
  bind-match-event :: String -> Either Either List {} | List | Error

  Given a description, return a list of events matching against it.
  If no matches are found, return an empty list.

  The result is packed in a either so that we can compose it.
  "
  [query]
  (bind-to (match-events query)))


(defn get-all-events
  "
  get-all-events :: -> HTTPResponse :body Maybe List

  Return a list of event maps of at most 15 events in the database.
  If no events are found, just return nothing.
  "
  []
  (response
    (sql/with-connection
      (db/db-connection)
      (sql/with-query-results results
                              [(str brief-query " group by events.eventsid limit 15")]
                              (when-not (empty? results)
                                (vec results))))))


(defn create-new-event
  "
  create-new-event :: {} -> HTTPResponse :body Maybe {}
                                         :status Either 200 | 400 | 401

  Given an event map, insert it in the database, then return a brief
  description of it.

  If the given map if badly formatted, return 400 Bad request
  If the sender wasn't logged in, send a 401 Unauthorized
  "
  [content]
  (->> content
       es/sanitize-create
       ev/validate-create
       bind-event-insert
       h/wrap-response))


(defn get-event
  "
  get-event :: String -> HTTPResponse :body Maybe {}
                                      :status Either 200 | 404

  Given an event id, return the appropiate event map.
  If no event is found, return 404 Not Found.
  "
  [id]
  (->> id
       clojure.string/trim
       (#(if (ev/event-exists? %) [% nil] [nil err-not-found]))
       bind-event-complete-extract
       h/wrap-response))


(defn search-events
  "
  search-events :: String -> HTTPResponse :body Either List {} | List

  Given a description, return a list of matched events.
  If no matches are found, return an empty list.
  "
  [query]
  (->> query
       clojure.string/trim
       bind-match-event
       h/wrap-response))


(defn get-event-comments
  "
  get-event-comments :: String -> HTTPResponse :body Either List {} | List
                                               :status Either 200 | 404

  Given an event id, return a list of its comments.
  If no comments are found, return an empty list.
  If the event doesn't exist, return 404 Not Found.
  "
  [event-id]
  (->> event-id
       (#(if (ev/event-exists? %) [% nil] [nil err-not-found]))
       bind-event-extract-comments
       h/wrap-response))


(defn ^:deprecated get-event-depr
  "
  get-event-depr :: String -> HTTPResponse :body Maybe string
                                           :status Either 200 | 404

  Given an event id, return its contents
  "
  [id]
  (sql/with-connection
    (db/db-connection)
    (sql/with-query-results results
                            ["select * from events where eventsId = ?" id]
                            (cond (empty? results) {:status err-not-found}
                                  :else (response (first results))))))


(defn ^:deprecated update-event
  "
  update-event :: String -> {} -> HTTPResponse :body Maybe String
                                               :status Either 200 | 404

  Given an event id, and a map, update that event with the supplied map.
  If the event doesn't exists, return 404 Not Found
  "
  [id content]
  (sql/with-connection
    (db/db-connection)
    (let [event (assoc content "eventsId" id)]
      (sql/update-values :events ["eventsId=?" id] event)))
  (get-event-depr id))


(defn ^:depecrated delete-event
  "
  delete-event :: String -> HTTPResponse :status 204

  Given an event id, delete that event from the database,
  and return 204 No Content
  "
  [id]
  (sql/with-connection
    (db/db-connection)
    (sql/delete-rows :events ["eventsId=?" id]))
  {:status status-deleted})
