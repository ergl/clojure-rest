(ns clojure-rest.auth
  (:require [ring.util.response :refer :all]
            [clojure-rest.data.users :as users]
            [clojure-rest.util.user-sanitize :as us]
            [clojure-rest.util.http :as h]))


;; String, String -> [{}?, Error?]
;; Checks if the given user / pass combination is correct, returns an error tuple
(defn- validate-user-pass [username password]
  (if (users/pass-matches? username password) 200 401))


;; [{}?, Error?] -> Response [:body nil :status Either<200|401|Error>]
(defn- bind-validate [[val err]]
  (if (nil? err)
    [nil (validate-user-pass (val :username) (val :password))]
    [nil err]))


;; {} -> Response [:body nil :status Natural]
;; Destructures the given content into username and password for validation ingestion
(defn auth-handler [content]
  (->> content
       clojure.walk/keywordize-keys
       us/sanitize-auth
       bind-validate
       h/wrap-response))
