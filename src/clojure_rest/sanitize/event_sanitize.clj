(ns clojure-rest.sanitize.event-sanitize
  (:require [clojure-rest.sanitize.sanitize :as s]
            [clojure-rest.sanitize.coordinate-sanitize :as cs]
            [clojure-rest.util.error :refer :all]))


;; {} -> [{}?, Error?]
;; Checks if (params :username) is non-empty
(defn- clean-author [params]
  (s/clean-field params :author))

;; {} -> [{}?, Error?]
;; Checks that the :title key is present and with an associated value
(defn- clean-title [params]
  (s/clean-field-with s/escape-html params :title))


;; {} -> [{}?, Error?]
;; Checks that the :content key is present and with an associated value
(defn- clean-content [params]
  (s/clean-field-with s/escape-html params :content))


;; :key {} -> [{}?, Error?]
;; Regexes the key against YYYY-MM-DD
(defn- regex-date [k params]
  (if (re-find #"[0-9]{4}-[0-9]{2}-[0-9]{2}" (params k))
    [params nil]
    [nil err-bad-request]))


;; {} -> [{}?, Error?]
;; Confirms that the supplied map has :initialdate and that it conforms to YYYY-MM-DD
;; If :finaldate is present, check it too
(defn- clean-dates [params]
  (->> (s/clean-field params :initialdate)
       (bind-error #(regex-date :initialdate %))
       (apply-if-present #(s/clean-field % :finaldate) :finaldate)
       (apply-if-present #(regex-date :finaldate %) :finaldate)))


;; Any -> [{}?, Error?]
(defn sanitize-create [params]
  (>>= params
       s/input->map
       clean-author
       clean-title
       cs/clean-coordinates
       clean-content
       clean-dates))
