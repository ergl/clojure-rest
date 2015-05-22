(ns clojure-rest.event-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [clojure-rest.handler :refer :all]
            [cheshire.core :refer [parse-string
                                   generate-string]]))


(deftest test-events
  (let [user (app (-> (mock/request :post "/api/users"
                                    (generate-string {:email "a@a.a"
                                                      :name "a"
                                                      :username "a"
                                                      :password "a"}))
                      (mock/content-type "application/json")))]
    
    ;; A get request to /api/events should always return 200 no matter what
    (testing "requesting events"
      (let [response (app (mock/request :get "/api/events"))]
        (is (= (response :status) 200))))
    
    ;; Creating an event should return its id, title, position and number of attendees
    (testing "creating an event"
      (let [response (app (-> (mock/request :post "/api/events"
                                            (generate-string {:author "a"
                                                              :title "This is an event title"
                                                              :content "This is an event description"
                                                              :coordinates "40, -3"
                                                              :initialdate "2015-05-30"}))
                              (mock/content-type "application/json")))]
        (is (= (response :status) 200))
        (is (= ((parse-string (:body response)) "title") "This is an event title"))
        (is (= ((parse-string (:body response)) "attending") 1))))
    
    (app (mock/request :delete "/api/users/a"))))
