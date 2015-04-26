(ns clojure-rest.handler
  (:use compojure.core)
  (:use ring.util.response)
  (:require [compojure.handler :as handler]
            [ring.middleware.json :as middleware]
            [compojure.route :as route]
            [clojure-rest.http :as http]
            [clojure-rest.db :as db]
            [clojure-rest.users :as users]
            [clojure-rest.events :as events]
            [clojure-rest.comments :as comments]))

;; () -> ring.util.response<Success, Error>
(defroutes app-routes
  (GET "/" [] (resource-response "index.html" {:root "public"}))
 	(route/resources "/")
  (context "/api" [] 

           (OPTIONS "/" []
                    (http/options [:options] {:version "0.1.0-SNAPSHOT"}))

           (ANY "/" []
                (http/method-not-allowed [:options]))

           (context "/events" [] (defroutes event-routes
                                   (GET "/" [] (events/get-all-events))
                                   (POST "/" {body :body} (events/create-new-event body))
                                   (OPTIONS "/" [] (http/options [:options :get :put :post :delete]))
                                   (context "/:id" [id] (defroutes event-routes
                                                          (GET "/" [] (events/get-event id))
                                                          (PUT "/" {body :body} (events/update-event id body))
                                                          (DELETE "/" [] (events/delete-event id))))
                                   (ANY "/" [] (http/method-not-allowed [:options :get :put :post :delete]))))
           
           (context "/users" [] (defroutes user-routes
                                  (GET "/" [] (users/get-all-users))
                                  (POST "/" {body :body} (users/create-new-user body))
                                  (OPTIONS "/" [] (http/options [:options :get :put :post :delete]))
                                  (context "/:id" [id] (defroutes user-routes
                                                         (GET "/" [] (users/get-user id))
                                                         (PUT "/" {body :body} (users/update-user id body))
                                                         (DELETE "/" [] (users/delete-user id))))
                                  (ANY "/" [] (http/method-not-allowed [:options :get :put :post :delete]))))
           
           (context "/comments" [] (defroutes comment-routes
                                     (GET "/" [] (comments/get-all-comments))
                                     (POST "/" {body :body} (comments/create-new-comment body))
                                     (OPTIONS "/" [] (http/options [:options :get :put :post :delete]))
                                     (context "/:id" [id] (defroutes comment-routes
                                                            (GET "/" [] (comments/get-comment id))
                                                            (PUT "/" {body :body} (comments/update-comment id body))
                                                            (DELETE "/" [] (comments/delete-comment id))))
                                     (ANY "/" [] (http/method-not-allowed [:options :get :put :post :delete]))))
           
           (route/not-found {:status 404}))
  (route/not-found {:status 404}))


;; HTTPRequest -> JSONResponse
(def app
  (-> (handler/api app-routes)
      (middleware/wrap-json-body)
      (middleware/wrap-json-response)))
