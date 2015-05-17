(ns clojure-rest.api-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [clojure-rest.handler :refer :all]))

(deftest test-api
  ;; A GET to an available api method should return a 200 OK response
  (testing "valid url"
    (let [response (app (mock/request :options "/api"))]
      (is (= (:status response) 200))
      (is (= (get-in response [:headers "Content-Type"]) "application/json; charset=utf-8"))))
  
  ;; A request to a non-existing api route should return a 404 response
  (testing "invalid url"
    (let [response (app (mock/request :get "/api/illegal"))]
      (is (= (:status response) 404)))))
