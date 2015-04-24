(ns clojure-rest.api-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [clojure-rest.handler :refer :all]))

(deftest test-api
  (testing "content found"
    (let [response (app (mock/request :get "/api/events"))]
      (is (= (:status response) 200))))
  
  (testing "not-found asset"
    (let [response (app (mock/request :get "/api/events/00000000-0000-0000-0000-000000000000"))]
      (is (= (:status response) 404)))))
