(ns clojure-rest.api-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [clojure-rest.handler :refer :all]
            [cheshire.core :refer [parse-string]]))

(deftest test-api
  ;; A GET to an available api method should return a 200 OK response
  (testing "valid url"
    (let [response (app (mock/request :get "/api/events"))]
      (is (= (:status response) 200))
      (is (= (get-in response [:headers "Content-Type"]) "application/json; charset=utf-8"))))
  
  ;; A request to a non-existing api route should return a 404 response
  (testing "invalid url"
    (let [response (app (mock/request :get "/api/foo"))]
      (is (= (:status response) 404)))))


(deftest test-users
  ;; A GET to an available user should return its username and name (if it exists)  
  (testing "retrieving an existring user"
    (let [response (app (mock/request :get "/api/users/johndoe"))]
      (is (= (:status response) 200))
      (is (= ((parse-string (:body response)) "username") "johndoe"))))
  
  ;; A GET to a non-existing user should return a 404 error message
  (testing "retrieving a non-existing user"
    (let [response (app (mock/request :get "/api/users/bogusname"))]
      (is (= (:status response) 404)))))
