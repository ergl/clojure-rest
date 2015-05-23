(ns clojure-rest.routes
  (:use compojure.core)
  (:use ring.util.response)
  (:require [compojure.route :as route]
            [clojure-rest.util.http :as http]
            [clojure-rest.data.events :as events]
            [clojure-rest.data.comments :as comments]))


(defn- event-comment-routes [event-id]
  (routes
    (GET "/" [] (events/get-event-comments event-id))
    (POST "/" {body :body} (comments/create-new-comment-case event-id body))
    (OPTIONS "/" [] (http/options [:options :get :post]))
    (ANY "/" [] (http/method-not-allowed [:options :get :post]))))


(defn- event-id-routes [event-id]
  (routes
    (GET "/" [] (events/get-event event-id))
    (PUT "/" [] (http/not-implemented))
    (DELETE "/" [] (http/not-implemented))
    (OPTIONS "/" [] (http/options [:options :get :put :delete]))
    (ANY "/" [] (http/method-not-allowed [:options :get :put :delete]))
    (context "/comments" [] (event-comment-routes event-id))))


(defn- event-search-routes [query]
  (routes
    (GET "/" [] (events/search-events query))
    (OPTIONS "/" [] (http/options [:options :get]))
    (ANY "/" [] (http/method-not-allowed [:options :get]))))


(defroutes event-routes
  (GET "/" [] (events/get-all-events))
  (POST "/" {body :body} (events/create-new-event body))
  (OPTIONS "/" [] (http/options [:options :get :post]))
  (ANY "/" [] (http/method-not-allowed [:options :get :post]))
  (context "/:id" [id] (event-id-routes id))
  (context "/search/:query" [query] (event-search-routes query)))
