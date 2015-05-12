(ns clojure-rest.sanitize
  (:require [clojure.string :refer [trim]]
            [clojure.walk :refer [keywordize-keys]]))

;; Error handling with a tuple [Value Error]
;; In our case, the value is a keywordized map coming from the client
;; Although some validation is done on the client, we double-check here
;; for data inconsistencies, fill default missing values and otherwise
;; clean the data for model and database handling.

;; (f [{}?, Error?]) -> Either<(f val)|[nil Error]>
;; Either execute (f val) or bind the error
;; If the error is present in the signature
(defn bind-error [f [val err]]
  (if (nil? err)
    (f val)
    [nil err]))


;; Turns:
;; (>>= (f val) g)
;; Into:
;; (->> (f val) (bind-error g))
(defmacro >>= [val & fns]
  (let [fns (for [f fns] `(bind-error ~f))]
    `(->> [~val nil]
          ~@fns)))


;; {} :key -> {}
;; Trims space on {{} :key}
(defn- trim-in [params k]
  (assoc params k (trim (params k))))


;; {} -> [{}?, Error?]
;; Checks if (params :username) is non-empty
(defn clean-username [params]
  (if (empty? (params :username))
    [nil 400]
    [(trim-in params :username) nil]))


;; {} -> [{}?, Error?]
;; Checks if (params :email) is a valid email address
(defn clean-email [params]
  (if (and (params :email) (re-find #".*@.*\..*" (params :email)))
    [(trim-in params :email) nil]
    [nil 400]))


;; {} -> [{}?, Error?]
;; Checks if (params :password) is non-empty
(defn clean-password [params]
  (if (empty? (params :password))
    [nil 400]
    [(trim-in params :password) nil]))


;; [{}?, Error?] -> Either<{}|Error>
;; Extracts the value from the optional map
;; If there is no value present, extract the error
(defn- wrap-error [[val err]]
  (if (nil? err) val err))


;; {} -> [{}, nil]
;; Adaptor from map to error tuple
(defn bind-to [params]
  [params nil])
