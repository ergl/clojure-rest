(ns clojure-rest.sanitize.comment-sanitize
  (:require [clojure-rest.sanitize.sanitize :as s]
            [clojure-rest.util.error :refer [>>=
                                             bind-error]]))


;; {} -> [{}?, Error?]
;; Checks if (params :username) is non-empty
(defn- clean-author [params]
  (s/clean-field-with s/escape-html params :author))


;; {} -> [{}?, Error?]
;; Checks that the :content key is present and with an associated value
(defn- clean-content [params]
  (s/clean-field-with s/escape-html params :content))


;; {} -> [{}?, Error?]
;; Checks that the :eventsid key is present and with an associated value
(defn- clean-event-id [params]
  (s/clean-field params :eventsid))


;; {} -> [{}?, Error?]
(defn sanitize-comment [params]
  (>>= params
       #(s/check-schema % [:author :content :parent :eventsid])
       clean-author
       clean-event-id
       clean-content))
