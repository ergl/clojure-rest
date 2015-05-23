(ns clojure-rest.util.sanitize
  (:require [clojure.string :refer [trim]]
            [clojure-rest.util.error :refer :all]))

;; Error handling with a tuple [Value Error]
;; In our case, the value is a keywordized map coming from the client
;; Although some validation is done on the client, we double-check here
;; for data inconsistencies, fill default missing values and otherwise
;; clean the data for model and database handling.

;; {} :key -> {}
;; Trims space on {{} :key}
(defn trim-in [params k]
  (assoc params k (trim (params k))))


;; {} :key -> [{}?, Error?]
(defn clean-field [params k]
  (if (and (params k) (not (empty? (params k))))
    [(trim-in params k) nil]
    [nil err-bad-request]))


;; f {} :key -> [{}?, Error?]
;; Returns {:key (f (params :k)}
;; Provided f has to be pure and return no errors
(defn clean-field-with [f params k]
  (if (and (params k) (not (empty? (params k))))
    [(assoc params k (f (params k))) nil]
    [nil err-bad-request]))


;; String -> String
;; Replaces all <, > and & with escaped characters
(defn escape-html [text]
  (-> text
      (clojure.string/replace #"&" "&amp;")
      (clojure.string/replace #"<" "&lt;")
      (clojure.string/replace #">" "&gt;")))
