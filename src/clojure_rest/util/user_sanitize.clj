(ns clojure-rest.util.user-sanitize
  (:require [clojure-rest.util.sanitize :as s]
            [clojure-rest.util.error :refer [>>=
                                             >?=
                                             bind-error
                                             apply-if-present]]))


;; {} -> [{}?, Error?]
;; Checks if (params :email) is a valid email address
(defn- clean-email [params]
  (if (and (params :email) (re-find #".*@.*\..*" (params :email)))
    [(s/trim-in params :email) nil]
    [nil 400]))


;; {} -> [{}?, Error?]
;; Checks if (params :username) is non-empty
(defn- clean-username [params]
  (if (empty? (params :username))
    [nil 400]
    [(s/trim-in params :username) nil]))


;; {} -> [{}?, Error?]
;; Checks if (params :password) is non-empty
(defn- clean-password [params]
  (if (empty? (params :password))
    [nil 400]
    [(s/trim-in params :password) nil]))


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
       (clean-username :username)))