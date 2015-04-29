(ns clojure-rest.handler
  (:use compojure.core)
  (:use ring.util.response)
  (:require [compojure.handler :as handler]
            [ring.middleware.json :as middleware]
            [compojure.route :as route]
            [clojure.walk :refer [stringify-keys]]
            [clojure-rest.http :as http]
            [clojure-rest.db :as db]
            [clojure-rest.users :as users]
            [clojure-rest.events :as events]
            [clojure-rest.comments :as comments]))


;; Response -> Response
;; Prints the content of the Response to stdout
(defn wrap-log-requests [handler]
  (fn [req]
    (->> (stringify-keys req)
         (interpose "\n")
         (println))
    (handler req)))

;; () -> Response
(defroutes app-routes
  (GET "/" [] (resource-response "index.html" {:root "public"}))
 	(route/resources "/")
  (context "/api" [] 

           (OPTIONS "/" []
                    (http/options [:options] {:version "0.1.0-SNAPSHOT"}))

           (ANY "/" []
                (http/method-not-allowed [:options]))

           (context "/events" [] (defroutes event-routes
                                   (GET "/" [] (http/not-implemented))
                                   (POST "/" [] (http/not-implemented))
                                   (OPTIONS "/" [] (http/options [:options :get :post]))
                                   (ANY "/" [] (http/method-not-allowed [:options :get :post]))
                                   (context ":id" [id] (defroutes event-routes
                                                         (GET "/" [] (http/not-implemented))
                                                         (PUT "/" [] (http/not-implemented))
                                                         (DELETE "/" [] (http/not-implemented))
                                                         (OPTIONS "/" [] (http/options [:options :get :put :delete]))
                                                         (ANY "/" [] (http/method-not-allowed [:options :get :put :delete]))))))
           
           (context "/users" [] (defroutes event-routes
                                   (GET "/" [] (http/not-implemented))
                                   (POST "/" [] (http/not-implemented))
                                   (OPTIONS "/" [] (http/options [:options :get :post]))
                                   (ANY "/" [] (http/method-not-allowed [:options :get :post]))
                                   (context ":id" [id] (defroutes event-routes
                                                         (GET "/" [] (http/not-implemented))
                                                         (PUT "/" [] (http/not-implemented))
                                                         (DELETE "/" [] (http/not-implemented))
                                                         (OPTIONS "/" [] (http/options [:options :get :put :delete]))
                                                         (ANY "/" [] (http/method-not-allowed [:options :get :put :delete]))))))
           
           (context "/comments" [] (defroutes event-routes
                                   (GET "/" [] (http/not-implemented))
                                   (POST "/" [] (http/not-implemented))
                                   (OPTIONS "/" [] (http/options [:options :get :post]))
                                   (ANY "/" [] (http/method-not-allowed [:options :get :post]))
                                   (context ":id" [id] (defroutes event-routes
                                                         (GET "/" [] (http/not-implemented))
                                                         (PUT "/" [] (http/not-implemented))
                                                         (DELETE "/" [] (http/not-implemented))
                                                         (OPTIONS "/" [] (http/options [:options :get :put :delete]))
                                                         (ANY "/" [] (http/method-not-allowed [:options :get :put :delete]))))))
           
           (context "/coordinates" [] (defroutes event-routes
                                   (GET "/" [] (http/not-implemented))
                                   (POST "/" [] (http/not-implemented))
                                   (OPTIONS "/" [] (http/options [:options :get :post]))
                                   (ANY "/" [] (http/method-not-allowed [:options :get :post]))
                                   (context ":id" [id] (defroutes event-routes
                                                         (GET "/" [] (http/not-implemented))
                                                         (PUT "/" [] (http/not-implemented))
                                                         (DELETE "/" [] (http/not-implemented))
                                                         (OPTIONS "/" [] (http/options [:options :get :put :delete]))
                                                         (ANY "/" [] (http/method-not-allowed [:options :get :put :delete]))))))


           (route/not-found {:status 404}))
  (route/not-found {:status 404}))


;; Request -> Response
(def app
  (-> (handler/api app-routes)
      (wrap-log-requests)
      (middleware/wrap-json-body)
      (middleware/wrap-json-response)))
