(ns clojure-rest.user-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [clojure-rest.handler :refer :all]
            [cheshire.core :refer [parse-string]]
            [cheshire.core :refer [generate-string]]))

(deftest test-users
  ;; A GET to an available user should return its username and name (if it exists)  
  (testing "retrieving an existing user"
    (let [response (app (mock/request :get "/api/users/johndoe"))]
      (is (= (:status response) 200))
      (is (= ((parse-string (:body response)) "username") "johndoe"))))
  
  ;; A GET to a non-existing user should return a 404 error message
  (testing "retrieving a non-existing user"
    (let [response (app (mock/request :get "/api/users/bogusname"))]
      (is (= (:status response) 404))))
  
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
  
  ;; A PUT to /api/users/username should update that user with whatever we send
  (testing "Updating an user"
    (let [response (app (-> (mock/request :put "/api/users/johndoe"
                                          (generate-string {:name "NotJohn"}))
                            (mock/content-type "application/json")))]
      (is (= (:status response) 200))
      (is (= ((parse-string (:body response)) "name") "NotJohn"))))
  
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