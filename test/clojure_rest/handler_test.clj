(ns clojure-rest.handler-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [clojure-rest.handler :refer :all]))

(deftest test-app
  (testing "main route"
    (let [response (app (mock/request :get "/"))]
      (is (= (:status response) 200))))

  (testing "not-found route"
    (let [response (app (mock/request :get "/invalid"))]
      (is (= (:status response) 404))))

  (testing "content found"
    (let [response (app (mock/request :get "/api/events"))]
      (is (= (:status response) 200))))

  (testing "not-found asset"
    (let [response (app (mock/request :get "/api/events/00000000-0000-0000-0000-000000000000"))]
      (is (= (:status response) 404)))))
