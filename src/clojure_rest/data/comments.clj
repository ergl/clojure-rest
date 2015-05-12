(ns clojure-rest.data.comments
  (:use ring.util.response)
  (:require [clojure.java.jdbc :as sql]
            [clojure-rest.data.db :as db]))


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
                                               (cond (empty? results) {:status 404}
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
  {:status 204})
