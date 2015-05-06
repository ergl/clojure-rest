(ns clojure-rest.auth
  (:require [ring.util.response :refer :all]
            [clojure-rest.users :as users]))


;; String, String -> Response[:body nil :status Natural]
;; Checks if the given user / pass combination is correct, returns 200 if correct, 401 otherwise
(defn- validate-user-pass [user pass]
  (-> (response nil)
      (status (if (users/pass-matches? user pass) 200 401))))


;; {} -> Response [:body nil :status Natural]
;; Destructures the given content into username and password for validation ingestion
(defn auth-handler [content]
  (validate-user-pass (content "username") (content "password")))
