(ns clojure-rest.user-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [clojure-rest.handler :refer :all]
            [cheshire.core :refer [parse-string]]
            [cheshire.core :refer [generate-string]]))

(deftest test-users
  
  ;; A POST to /api/users should create a user an return that user
  (testing "Creating a user"
    (let [response (app (-> (mock/request :post "/api/users"
                                          (generate-string {:email "foo@foo.com"
                                                            :name "foo"
                                                            :username "bar"
                                                            :password "12345"}))
                            (mock/content-type "application/json")))]
      (is (= (:status response) 200))
      (is (= (get-in response [:headers "Content-Type"]) "application/json; charset=utf-8"))
      (is (= ((parse-string (:body response)) "username") "bar"))))
  
  ;; Attempting to create an user with an existing email or username should return a 400 response
  (testing "creating an existing user"
    (let [response (app (-> (mock/request :post "/api/users"
                                          (generate-string {:email "foo@foo.com"
                                                            :name "foo"
                                                            :username "bar"
                                                            :password "12345"}))
                            (mock/content-type "application/json")))]
      (is (= (:status response) 400))))
  
  ;; A GET to an available user should return its username and name (if it exists)  
  (testing "retrieving an existing user"
    (let [response (app (mock/request :get "/api/users/bar"))]
      (is (= (:status response) 200))
      (is (= ((parse-string (:body response)) "username") "bar"))))
  
  ;; A GET to a non-existing user should return a 404 error message
  (testing "retrieving a non-existing user"
    (let [response (app (mock/request :get "/api/users/bogusname"))]
      (is (= (:status response) 404))))
  
  ;; A PUT to /api/users/username should update that user with whatever we send
  (testing "Updating an user"
    (let [response (app (-> (mock/request :put "/api/users/bar"
                                          (generate-string {:name "notbar"}))
                            (mock/content-type "application/json")))]
      (is (= (:status response) 200))
      (is (= ((parse-string (:body response)) "name") "notbar"))))
  
  ;; Trying to update a non-existing user should result in a 404 response
  (testing "updating a non-existing user"
    (let [response (app (-> (mock/request :put "/api/users/notfound"
                                          (generate-string {:username "whatever"}))
                            (mock/content-type "application/json")))]
      (is (= (:status response) 404))))
  
  ;; DELETing an existing user should return a 204 status code
  (testing "deleting an exisiting user"
    (let [response (app (mock/request :delete "/api/users/bar"))]
      (is (= (:status response) 204)))))
