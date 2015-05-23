(ns clojure-rest.util.coordinate-sanitize
  (:require [clojure-rest.util.utils :refer [trim-inside
                                             parse-float]]
            [clojure-rest.util.error :refer :all]))


;; {} -> {}
;; {:coordinates "A, B"} -> {:latitude A :longitude B}
;; Strips all whitespace and commas
(defn- format-coordinates [params]
  (let [coord-pair (-> (trim-inside (params :coordinates))
                       (clojure.string/split #","))]
    (-> (assoc params :latitude (-> (first coord-pair)
                                    (clojure.string/replace #"," " ")
                                    (clojure.string/trim)
                                    parse-float))
        
        (#(assoc % :longitude (-> (second coord-pair)
                                  clojure.string/trim
                                  parse-float)))
        
        (#(dissoc % :coordinates)))))


;; {} -> [{}?, Error?]
;; Checks that the :coordinates key is present and with an associated value
(defn clean-coordinates [params]
  (if (and (params :coordinates) (not (empty? (params :coordinates))))
    [(format-coordinates params) nil]
    [nil err-bad-request]))
