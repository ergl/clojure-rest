(ns clojure-rest.data.db
  (:import com.mchange.v2.c3p0.ComboPooledDataSource))


;; () -> java.util.UUID
;; Generates a random UUID
(defn uuid []
  (str (java.util.UUID/randomUUID)))

; Change this if you want to go for another database engine
; Currently using h2
(def db-config
  {:classname "org.h2.Driver"
   :subprotocol "h2"
   :subname "mem:documents"
   :init-script "INIT=RUNSCRIPT FROM './schema.sql'"
   :user ""
   :password ""})

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
