(ns clojure-rest.routes.coordinate-routes
  (:require [compojure.core :refer :all]
            [clojure-rest.util.http :as http]))


(defn- coordinate-id-routes [id]
  (routes
    (GET "/" [] (http/not-implemented))
    (PUT "/" [] (http/not-implemented))
    (DELETE "/" [] (http/not-implemented))
    (OPTIONS "/" [] (http/options [:options :get :put :delete]))
    (ANY "/" [] (http/method-not-allowed [:options :get :put :delete]))))


(defroutes coordinate-routes
  (GET "/" [] (http/not-implemented))
  (POST "/" [] (http/not-implemented))
  (OPTIONS "/" [] (http/options [:options :get :post]))
  (ANY "/" [] (http/method-not-allowed [:options :get :post]))
  (context ":id" [id] (coordinate-id-routes id)))
