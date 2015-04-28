(ns clojure-rest.handler-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [clojure-rest.handler :refer :all]
            [cheshire.core :refer [parse-string]]))

(deftest test-app
  ;; A request to an available method should return a 200 OK response
  (testing "main route"
    (let [response (app (mock/request :get "/"))]
      (is (= (:status response) 200))))

  ;; A request to a non-existing route should return a 404 response
  (testing "not-found route"
    (let [response (app (mock/request :get "/invalid"))]
      (is (= (:status response) 404))))

  ;; An OPTIONS request to the /api route should return the version of the api
  (testing "options response"
    (let [response (app (mock/request :options "/api"))]
      (is (= ((parse-string (:body response)) "version") "0.1.0-SNAPSHOT"))
      (is (= (:status response) 200))))

  ;; Any other request to the /api route should return a 405 response
  (testing "any 405 response"
    (let [response (app (mock/request :get "/api"))]
      (is (= (:status response) 405))
      (is (nil? (:body response))))))
