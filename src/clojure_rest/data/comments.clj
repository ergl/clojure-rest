(ns clojure-rest.data.comments
  (:use ring.util.response)
  (:require [clojure.java.jdbc :as sql]
            [clojure-rest.data.db :as db]
            [clojure-rest.util.comment-sanitize :as cs]
            [clojure-rest.util.comment-validate :as cv]
            [clojure.walk :refer [keywordize-keys]]
            [clojure-rest.util.http :as h]
            [clojure-rest.util.error :refer [bind-error
                                             err-not-found
                                             status-deleted]]))



(def ^:private count-query
  (str "select count(commentsid) as commentcount "
       "from events_comments "
       "where eventsid = ?"))


(def ^:private complete-comment-query
  (str "select events_comments.commentsid"
       ", users.username as author"
       ", comments.content"
       ", comments.positivevotes"
       ", comments.negativevotes"
       ", comments.parentid as parent"
       " from events_comments"
       " inner join comments on (events_comments.commentsid = comments.commentsid)"
       " inner join users_comments on (users_comments.commentsid = comments.commentsid)"
       " inner join users on (users.usersid = users_comments.author)"
       " where events_comments.eventsid = ?"))


(def ^:private brief-comment-query
  (str "select comments.commentsid as commentid"
       ", users.username as author"
       ", comments.content"
       ", comments.positivevotes"
       ", comments.negativevotes"
       ", comments.parentid as parent"
       " from comments"
       " inner join users_comments on (users_comments.commentsid = comments.commentsid)"
       " inner join users on (users.usersid = users_comments.author)"
       " where comments.commentsid = ?"))


;; UUID -> Natural
(defn get-comment-count [event-id]
  (sql/with-connection (db/db-connection)
                       (sql/with-query-results results
                                               [count-query event-id]
                                               (if-not (empty? results)
                                                 (into {} results)
                                                 0))))


;; UUID -> [{}?]
;; Returns a vector of all the comments of the given event id
;; Returns an empty vector if no comments are found
(defn get-all-event-comments [event-id]
  (sql/with-connection (db/db-connection)
                       (sql/with-query-results results
                                               [complete-comment-query event-id]
                                               (into [] results))))


;; UUID -> Either<{}|nil>
;; Get the id, author username, content, votes and parent id of the given comment id
(defn- comment-extract! [comment-id]
  (sql/with-connection (db/db-connection)
                       (sql/with-query-results results
                                               [brief-comment-query comment-id]
                                               (when-not (empty? results)
                                                 (first results)))))


;; [UUID?, Error?] -> [{}?, Error?]
(defn- bind-comment-extract [params]
  (bind-error #(let [res (comment-extract! %)]
                 (if (nil? res)
                   [nil err-not-found]
                   [res nil])) params))


;; {} -> Either<{}|nil>
(defn- comment-insert! [content]
  (let [id (db/uuid)]
    (sql/with-connection (db/db-connection)
                         (let [user-comment (hash-map :author (content :author) :commentsid id)
                               event-comment (hash-map :eventsid (content :eventsid) :commentsid id)
                               comment-entry (-> (cv/complete-parent-id content id)
                                                 (dissoc :author) (dissoc :eventsid)
                                                 (assoc :commentsid id))]
                           (sql/insert-record :comments comment-entry)
                           (sql/insert-record :events_comments event-comment)
                           (sql/insert-record :users_comments user-comment)))
    (comment-extract! id)))


;; [{}?, Error?] -> [{}?, Error?]
(defn- bind-comment-insert [params]
  (bind-error #(let [res (comment-insert! %)]
                 (if (nil? res)
                   [nil err-not-found]
                   [res nil])) params))


;; {:author username :content String :parent UUID?}
(defn create-new-comment-case [event-id content]
  (->> (assoc content :eventsid event-id)
       keywordize-keys
       cs/sanitize-comment
       cv/validate-comment
       bind-comment-insert
       h/wrap-response))


;; () -> Response[:body String]
;; Returns a response with the contents of all the comments in the database
(defn get-all-comments []
  (response
    (sql/with-connection (db/db-connection)
                         (sql/with-query-results results
                                                 ["select * from comments"]
                                                 (into [] results)))))


;; UUID -> Response[:body String]
;; UUID -> Response[:body null :status 404]
;; Returns a response with the contents of the specified comment
(defn get-comment [id]
  (sql/with-connection (db/db-connection)
                       (sql/with-query-results results
                                               ["select * from comments where commentsId = ?" id]
                                               (cond (empty? results) {:status err-not-found}
                                                     :else (response (first results))))))


;; {} -> Response[:body String]
;; {} -> Response[:body null :status 404]
;; Creates a new comment with the provided content, then returns said comment
;; See get-comment
(defn create-new-comment [content]
  (let [id (db/uuid)]
    (sql/with-connection (db/db-connection)
                         (let [comment (assoc content "commentsId" id)]
                           (sql/insert-record :comments comment)))
    (get-comment id)))


;; UUID, {} -> Response[:body String]
;; UUID, {} -> Response[:body null :status 404]
;; Updates the specified comment with the provided content, then returns said comment
;; See get-comment
(defn update-comment [id content]
  (sql/with-connection (db/db-connection)
                       (let [comment (assoc content "commentsId" id)]
                         (sql/update-values :comments ["commentsId=?" id] comment)))
  (get-comment id))


;; UUID -> Response[:status 204]
;; Deletes the specified comment, then returns a 204 http code
(defn delete-comment [id]
  (sql/with-connection (db/db-connection)
                       (sql/delete-rows :comments ["commentsId=?" id]))
  {:status status-deleted})
