(ns clojure-rest.routes.auth-routes
  (:require [compojure.core :refer :all]
            [clojure-rest.util.http :as http]
            [clojure-rest.auth :as auth]))


(defn- token-routes [token]
  (routes
    (DELETE "/" {body :body} (auth/delete-token token body))
    (OPTIONS "/" [] (http/options [:options :delete]))
    (ANY "/" [] (http/method-not-allowed [:options :delete]))))


(defroutes auth-routes
  (POST "/" {body :body} (auth/auth-handler body))
  (OPTIONS "/" [] (http/options [:options :post]))
  (ANY "/" [] (http/method-not-allowed [:options :post]))
  (context "/:token" [token] (token-routes token)))
