(ns clojure-rest.sanitize)

;; Error handling with a tuple [Value Error]
;; In our case, the value is a keywordized map coming from the client
;; Although some validation is done on the client, we double-check here
;; for data inconsistencies, fill default missing values and otherwise
;; clean the data for model and database handling.

;; (f [{}?, Error?]) -> Either<(f val)|[nil Error]>
;; Either execute (f val) or bind the error
;; If the error is present in the signature
(defn- bind-error [f [val err]]
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


;; {} -> [{}?, Error?]
;; Checks if (params :username) is non-empty
(defn- clean-username [params]
  (if (empty? (params :username))
    [nil "Please enter your username"]
    [params nil]))


;; {} -> [{}?, Error?]
;; Checks if (params :email) is a valid email address
(defn- clean-email [params]
  (if (and (params :email) (re-find #".*@.*\..*" (params :email)))
    [params nil]
    [nil "Please enter an email address"]))


;; {} -> [{}?, Error?]
;; Checks if (params :password) is non-empty
(defn- clean-password [params]
  (if (empty? (params :password))
    [nil "Please enter your password"]
    [params nil]))


;; [{}?, Error?] -> Either<{}|Error>
;; Extracts the value from the optional map
;; If there is no value present, extract the error
(defn- wrap-error [[val err]]
  (if (nil? err) val err))


;; {} -> [{}?, Error?]
;; Chains a map through the different login validations
(defn clean-login [params]
  (>>= params
       clean-username
       clean-password))


;; {} -> [{}?, Error?]
;; Chains a map through the different signup validations
(defn clean-signup [params]
  (>>= params
       clean-email
       clean-username
       clean-password))

;; {} -> Either<{}|Error>
;; Chains a map through the login validation and extract the result
(defn login-flow [params]
  (wrap-error (clean-login params)))

;; {} -> Either<{}|Error>
;; Chains a map through the login validation and extract the result
(defn signup-flow [params]
  (wrap-error (clean-signup params)))
