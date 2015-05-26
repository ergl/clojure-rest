(ns clojure-rest.db
  (:import com.mchange.v2.c3p0.ComboPooledDataSource)
  (:require [environ.core :refer [env]]))


;; () -> java.util.UUID
;; Generates a random UUID
(defn uuid []
  (str (java.util.UUID/randomUUID)))


; Change this if you want to go for another database engine
; Currently using h2
(def db-config
  {:classname "org.h2.Driver"
   :subprotocol "h2"
   :subname (env :h2-type)
   :init-script (env :h2-script)
   :user (env :h2-user)
   :password (env :h2-password)})

;; DatabaseConfig -> ComboPooledDataSource
;; Sets up the connection pool for the given database configuration
(defn pool [config]
  (let [cpds (doto (ComboPooledDataSource.)
               (.setDriverClass (:classname config))
               (.setJdbcUrl (str "jdbc:" (:subprotocol config) ":" (:subname config) ";" (:init-script config)))
               (.setUser (:user config))
               (.setPassword (:password config))
               (.setMaxPoolSize 1)
               (.setMinPoolSize 1)
               (.setInitialPoolSize 1))]
    {:datasource cpds}))


;; Delays the execution of the pool until called with deref
(def pooled-db (delay (pool db-config)))

;; () -> ComboPooledDataSource
;; Derefences the connection pool
(defn db-connection [] @pooled-db)
