(ns clojure-rest.util.sanitize
  (:require [clojure.string :refer [trim]]))

;; Error handling with a tuple [Value Error]
;; In our case, the value is a keywordized map coming from the client
;; Although some validation is done on the client, we double-check here
;; for data inconsistencies, fill default missing values and otherwise
;; clean the data for model and database handling.

;; {} :key -> {}
;; Trims space on {{} :key}
(defn- trim-in [params k]
  (assoc params k (trim (params k))))


;; {} -> [{}?, Error?]
;; Checks if (params :email) is a valid email address
(defn clean-email [params]
  (if (and (params :email) (re-find #".*@.*\..*" (params :email)))
    [(trim-in params :email) nil]
    [nil 400]))


;; {} -> [{}?, Error?]
;; Checks if (params :username) is non-empty
(defn clean-username [params]
  (if (empty? (params :username))
    [nil 400]
    [(trim-in params :username) nil]))


;; {} -> [{}?, Error?]
;; Checks if (params :password) is non-empty
(defn clean-password [params]
  (if (empty? (params :password))
    [nil 400]
    [(trim-in params :password) nil]))
