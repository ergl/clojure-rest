(ns clojure-rest.util.event-validate
  (:require [clojure-rest.util.validate :as v]))

;; String -> Boolean
(defn event-exists? [id]
  (v/field-exists-in-table? "events" "eventsid" id))


;; String -> Either<{}|nil>
(defn get-event-table [id]
  (v/get-table-values "events" "eventsid" id))
