(ns clojure-rest.util.http
  (:require [ring.util.response :refer :all]
            [clojure.string :refer [upper-case]]
            [clojure.string :refer [join]]
            [clojure-rest.util.error :refer :all]))


;; Natural -> Response[:body nil :status Natural]
(defn empty-response-with-code [code]
  (-> (response nil)
      (status code)))

;; () -> Response[:body nil :options OPTIONS]
;; [] -> Response[:body nil :options []]
;; [], String -> Response[:body String :options OPTIONS]
(defn options
  ([] (options #{:options} nil))
  ([allowed] (options allowed nil))
  ([allowed body]
   (-> (response body)
       (header "Allow" (join ", " (map (comp upper-case name) allowed))))))


;; () -> Response[:body nil :options OPTIONS :status 405]
;; [] -> Response[:body nil :options [] :status 405]
;; [], String -> Response[:body String :options [] :status 405]
(defn method-not-allowed [allowed]
  (-> (options allowed)
      (status err-not-allowed)))

;; () -> Response[:body nil :status 204]
(defn deleted []
  (empty-response-with-code status-deleted))


;; () -> Response[:body nil :status 400]
(defn bad-request []
  (empty-response-with-code err-bad-request))


;; () -> Response[:body nil :status 401]
(defn unauthorized []
  (empty-response-with-code err-unauthorized))


;; () -> Response[:body nil :status 403]
(defn forbidden []
  (empty-response-with-code err-forbidden))


;; () -> Response[:body nil :status 501]
(defn not-implemented []
  (empty-response-with-code err-not-implemented))


;; [{}?, Error?] -> Either<Response[:body nil :status err]|Response[:body val]>
(defn wrap-response [[val err]]
  (if (nil? err)
    (response val)
    (empty-response-with-code err)))
