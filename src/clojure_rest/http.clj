(ns clojure-rest.http
  (:require [ring.util.response :refer :all]
            [clojure.string :refer [upper-case]]
            [clojure.string :refer [join]]))


;; Natural -> Response[:body nil :status Natural]
(defn- empty-response-with-code [code]
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
      (status 405)))


;; () -> Response[:body nil :status 501]
(defn not-implemented []
	(empty-response-with-code 501))


;; () -> Response[:body nil :status 401]
(defn unauthorized []
	(empty-response-with-code 401))


;; () -> Response[:body nil :status 403]
(defn forbidden []
	(empty-response-with-code 403))
