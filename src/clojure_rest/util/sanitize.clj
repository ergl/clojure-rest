(ns clojure-rest.util.sanitize
  (:require [clojure.string :refer [trim]]))

;; Error handling with a tuple [Value Error]
;; In our case, the value is a keywordized map coming from the client
;; Although some validation is done on the client, we double-check here
;; for data inconsistencies, fill default missing values and otherwise
;; clean the data for model and database handling.

;; {} :key -> {}
;; Trims space on {{} :key}
(defn trim-in [params k]
  (assoc params k (trim (params k))))
