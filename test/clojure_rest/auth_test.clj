(ns clojure-rest.auth-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [clojure-rest.handler :refer :all]
            [cheshire.core :refer [parse-string]]
            [cheshire.core :refer [generate-string]]))

(deftest authorize-users
  ;; Creates a dummy user for testing purposes
  (let [user (app (-> (mock/request :post "/api/users"
                                    (generate-string {:email "bar@bar.com"
                                                      :name "bar"
                                                      :username "newbar"
                                                      :password "sosecret"}))
                      (mock/content-type "application/json")))]
    
    ;; A POST to /api/auth with a correct user/pass combination should return a 200 response
    (testing "authenticating a correct user"
      (let [response (app (-> (mock/request :post "/api/auth"
                                            (generate-string {:username
                                                              ((parse-string (:body user)) "username")
                                                              :password "sosecret"}))
                              (mock/content-type "application/json")))]
        (is (= (:status response) 200))))
    
    ;; A POST to /api/auth with a wrong user/pass combination should return a 401 response
    (testing "denegating an incorrect user"
      (let [response (app (-> (mock/request :post "/api/auth"
                                            (generate-string {:username
                                                              ((parse-string (:body user)) "username")
                                                              :password "notarealpassword"}))
                              (mock/content-type "application/json")))]
        (is (= (:status response) 401))))))