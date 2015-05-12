(ns clojure-rest.data.coordinates
  (:use ring.util.response)
  (:require [clojure.java.jdbc :as sql]
            [clojure-rest.data.db :as db]))


;; () -> Response[:body String]
;; Returns a response with the contents of all the coordinates in the database
(defn get-all-coordinates []
  (response
    (sql/with-connection (db/db-connection)
                         (sql/with-query-results results
                                                 ["select * from coordinates"]
                                                 (into [] results)))))


;; UUID -> Response[:body String]
;; UUID -> Response[:body null :status 404]
;; Returns a response with the contents of the specified coordinate id
(defn get-coordinate [id]
  (sql/with-connection (db/db-connection)
                       (sql/with-query-results results
                                               ["select * from coordinates where coordinatesId = ?" id]
                                               (cond (empty? results) {:status 404}
                                                     :else (response (first results))))))


;; {} -> Response[:body String]
;; {} -> Response[:body null :status 404]
;; Creates a new coordinate pair with the provided content, then returns said coordinate pair
;; See get-coordinate
(defn create-new-coordinate [content]
  (let [id (db/uuid)]
    (sql/with-connection (db/db-connection)
                         (let [coord (assoc content "coordinatesId" id)]
                           (sql/insert-record :coordinates coord)))
    (get-coordinate id)))


;; UUID, {} -> Response[:body String]
;; UUID, {} -> Response[:body null :status 404]
;; Updates the specified coordinate pair with the provided content, then returns said coordinate pair
;; See get-coordinate
(defn update-coordinates [id content]
  (sql/with-connection (db/db-connection)
                       (let [coord (assoc content "coordinatesId" id)]
                         (sql/update-values :coordinates ["coordinatesId=?" id] coord)))
  (get-coordinate id))


;; UUID -> Response[:status 204]
;; Deletes the specified coordinate pair, then returns a 204 http code
(defn delete-coordinate [id]
  (sql/with-connection (db/db-connection)
                       (sql/delete-rows :coordinates ["coordinatesId=?" id]))
  {:status 204})
