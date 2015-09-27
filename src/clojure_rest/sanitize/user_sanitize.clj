(ns clojure-rest.sanitize.user-sanitize
  (:require [clojure-rest.sanitize.sanitize :as s]
            [clojure-rest.util.error :refer [>>=
                                             =>?=
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


;; {"email" "username" "password"} -> [{:email :username :password}?, Error?]
(defn sanitize-signup [params]
  (>>= params
       s/input->map
       clean-email
       clean-username
       clean-password))


;; {} -> [{}?, Error?]
(defn sanitize-update [params]
  (=>?= (s/input->map params)
        (clean-email :email)
        (clean-username :username)
        (clean-password :password)))


;; {} -> [{}?, Error?]
(defn sanitize-add [content]
  (>>= content
       #(s/check-schema % [:username])
       clean-username))
