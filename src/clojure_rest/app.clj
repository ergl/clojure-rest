(ns clojure-rest.app
  (:require [ring.adapter.jetty :as jetty]
            [clojure-rest.handler :as handler])
  (:gen-class))


(defn -main [& args]
  (jetty/run-jetty handler/app {:port 5000 :join? false}))
