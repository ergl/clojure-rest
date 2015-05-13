(ns clojure-rest.util.validate
  (:require [clojure.java.jdbc :as sql]
            [clojure-rest.data.db :as db]
            [clojure-rest.util.error :refer [bind-to]]))

;; String String String -> Boolean
;; Checks if the value exists in the given row of the given table
(defn field-exists-in-table? [table row value]
  (not (empty?
         (sql/with-connection (db/db-connection)
                              (sql/with-query-results results
                                                      [(str "select 1 from " table " where " row " = ?") value]
                                                      (into {} results))))))


;; {} key fn -> Boolean
;; Check (fn {{} :key})
(defn check-field [params field pred]
  (if (pred (params field))
    [nil 400]
    (bind-to params)))
