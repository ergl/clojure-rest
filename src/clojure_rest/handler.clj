(ns clojure-rest.handler
  (:use compojure.core)
  (:use ring.util.response)
  (:require [compojure.handler :as handler]
            [ring.middleware.json :as json]
            [compojure.route :as route]
            [clojure.walk :refer [stringify-keys]]
            [clojure-rest.util.http :as http]
            [clojure-rest.data.db :as db]
            [clojure-rest.auth :as auth]
            [clojure-rest.data.users :as users]
            [clojure-rest.data.events :as events]
            [clojure-rest.data.comments :as comments]
            [clojure-rest.util.error :refer [err-not-found]]))


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
           
           (context "/auth" [] (defroutes auth-routes
                                 (POST "/" {body :body} (auth/auth-handler body))
                                 (OPTIONS "/" [] (http/options [:options :post]))
                                 (ANY "/" [] (http/method-not-allowed [:options :post]))
                                 (context "/:token" [token] (defroutes auth-routes
                                                              (DELETE "/" {body :body} (auth/delete-token token body))
                                                              (OPTIONS "/" [] (http/options [:options :delete]))
                                                              (ANY "/" [] (http/method-not-allowed [:options :delete]))))))
           
           (context "/events" [] (defroutes event-routes
                                   (GET "/" [] (events/get-all-events))
                                   (POST "/" [] (http/not-implemented))
                                   (OPTIONS "/" [] (http/options [:options :get :post]))
                                   (ANY "/" [] (http/method-not-allowed [:options :get :post]))
                                   (context "/:id" [id] (defroutes event-routes
                                                         (GET "/" [] (events/get-event id))
                                                         (PUT "/" [] (http/not-implemented))
                                                         (DELETE "/" [] (http/not-implemented))
                                                         (OPTIONS "/" [] (http/options [:options :get :put :delete]))
                                                         (ANY "/" [] (http/method-not-allowed [:options :get :put :delete]))))))
           
           (context "/users" [] (defroutes event-routes
                                  (GET "/" [] (users/get-all-users))
                                  (POST "/" {body :body} (users/create-new-user body))
                                  (OPTIONS "/" [] (http/options [:options :get :post]))
                                  (ANY "/" [] (http/method-not-allowed [:options :get :post]))
                                  (context "/:username" [username] (defroutes event-routes
                                                                     (GET "/" [] (users/get-user username))
                                                                     (PUT "/" {body :body} (users/update-user username body))
                                                                     (DELETE "/" [] (users/delete-user username))
                                                                     (OPTIONS "/" [] (http/options [:options :get :put :delete]))
                                                                     (ANY "/" [] (http/method-not-allowed [:options :get :put :delete]))))
                                  (context "/search/:query" [query] (defroutes event-routes
                                                                      (GET "/" [] (users/search-users query))
                                                                      (OPTIONS "/" [] (http/options [:options :get]))
                                                                      (ANY "/" [] (http/method-not-allowed [:options :get]))))))
           
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
           
           
           (route/not-found {:status err-not-found}))
  (route/not-found {:status err-not-found}))


;; Request -> Response
(defn prod-handler []
  (-> (handler/api app-routes)
      (json/wrap-json-body)
      (json/wrap-json-response)))


;; Request -> Response
(defn dev-handler[]
  (-> (prod-handler)
      (wrap-log-requests)))


;; Request -> Response
;; Entry point
(def app (prod-handler))
