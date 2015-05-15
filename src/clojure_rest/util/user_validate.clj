(ns clojure-rest.util.user-validate
  (:require [clojure-rest.util.validate :as v]
            [clojure-rest.util.error :refer [=>>=
                                             bind-error
                                             bind-to]]))


;; String -> Boolean
(defn user-exists? [username]
  (v/field-exists-in-table? "users" "username" username))


;; String -> Boolean
(defn email-exists? [email]
  (v/field-exists-in-table? "users" "email" email))


;; String -> Either<{}|nil>
(defn get-user-table [username]
  (v/get-table-values "users" "username" username))


;; {} -> [{}?, Error?]
;; Check for {{} :username}
(defn- check-username [params]
  (v/check-field params :username user-exists?))


;; {} -> [{}?, Error?]
;; Check for {{} :email}
(defn- check-email [params]
  (v/check-field params :email email-exists?))


;; {} -> {}
;; Fills in the default values for a new user
;; From signup form we only get email, username and password
(defn- complete-default-user [content]
  (assoc content :profileImage nil :deleted false :moderator false))


;; [{}?, Error?] -> [{}?, Error?]
(defn validate-signup [params]
  (=>>= params
        #(bind-to (complete-default-user %))
        check-email
        check-username))
