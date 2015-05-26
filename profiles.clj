{:dev {:dependencies [[ring/ring-mock "0.2.0"]]
       :env {:secret-key "your-secret-key"
             :h2-user "your-db-user"
             :h2-password "your-db-pass"
             :h2-type "relative-path-to-your-database"
             :h2-script ""}}
 :test {:env {:secret-key "your-secret-key"
              :h2-user ""
              :h2-password ""
              :h2-type "mem:documents"
              :h2-script "INIT=RUNSCRIPT FROM './schema.sql'"}}}
