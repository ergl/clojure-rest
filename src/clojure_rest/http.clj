(ns clojure-rest.http
  (:require [ring.util.response :refer :all]
            [clojure.string :refer [upper-case]]
            [clojure.string :refer [join]]))

(defn options
  ([] (options #{:options} nil))
  ([allowed] (options allowed nil))
  ([allowed body]
   (-> (response body)
       (header "Allow" (join ", " (map (comp upper-case name) allowed))))))

(defn method-not-allowed [allowed]
  (-> (options allowed)
      (status 405)))

(defn not-implemented []
  (-> (response nil)
      (status 501)))
