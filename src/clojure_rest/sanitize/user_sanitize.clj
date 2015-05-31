(ns clojure-rest.sanitize.user-sanitize
  (:require [clojure-rest.sanitize.sanitize :as s]
            [clojure-rest.util.error :refer [>>=
                                             >?=
                                             bind-error
                                             apply-if-present
                                             err-bad-request]]))


;; {} -> [{}?, Error?]
;; Checks if (params :email) is a valid email address
(defn- clean-email [params]
  (if (and (params :email) (re-find #".*@.*\..*" (params :email)))
    [(s/trim-in params :email) nil]
    [nil err-bad-request]))


;; {} -> [{}?, Error?]
;; Checks if (params :username) is non-empty
(defn- clean-username [params]
  (s/clean-field params :username))


;; {} -> [{}?, Error?]
;; Checks if (params :password) is non-empty
(defn- clean-password [params]
  (s/clean-field params :password))


;; {} -> [{}?, Error?]
(defn sanitize-signup [params]
  (>>= params
       clean-email
       clean-username
       clean-password))


;; {} -> [{}?, Error?]
(defn sanitize-update [params]
  (>?= params
       (clean-email :email)
       (clean-username :username)
       (clean-password :password)))


;; {} -> [{}?, Error?]
;; Checks if the given map contains the username and password fields
(defn sanitize-auth [content]
  (->> content
       (#(if (% :username) [% nil] [nil err-bad-request]))
       ((fn [c] (bind-error #(if (% :password) [% nil] [nil err-bad-request]) c)))))


;; {} -> [{}?, Error?]
(defn sanitize-add [content]
  (>>= content
       clean-username))
