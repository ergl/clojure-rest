(defproject clojure-rest "0.1.0-SNAPSHOT"
  :description "REST service for saleokase"
  :url "http://github.com/ergl/clojure-rest"
  :license {:name "General Public License - v 3"
            :url "https://www.gnu.org/licenses/gpl-3.0-standalone.html"
            :distribution :repo}
  :dependencies [[org.clojure/clojure "1.7.0"]

                 ; erlang-style pattern matching
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [defun "0.2.0"]

                 ; annotations
                 [prismatic/schema "1.0.1"]

                 ; routes, responses and web server
                 [compojure "1.4.0"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-jetty-adapter "1.4.0"]

                 ; json de/encoder
                 [cheshire "5.5.0"]

                 ; thread pooling
                 [com.mchange/c3p0 "0.9.5"]

                 ; database and sql
                 [com.h2database/h2 "1.4.189"]
                 [org.clojure/java.jdbc "0.2.3"] ; TODO: Upgrade to new syntax

                 ; HMAC, hashing and encription
                 [buddy "0.7.2"]
                 [pandect "0.5.4"]
                 [buddy/buddy-hashers "0.7.0"]

                 ; environment variables
                 [environ "1.0.1"]]
  :plugins [[lein-ring "0.9.3"]
            [lein-environ "1.0.0"]]
  :ring {:handler clojure-rest.handler/app
         :port 5000
         :auto-reload? true
         :auto-refresh? false}
  :profiles
  {:dev {:dependencies [[ring/ring-mock "0.3.0"]]}}
  :main clojure-rest.app)

;; documentation may be found here
;; https://github.com/technomancy/leiningen/blob/stable/sample.project.clj
