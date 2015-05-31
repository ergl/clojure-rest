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
                                                              :initialdate "9999-12-31"}))
                              (mock/content-type "application/json")))]
        (is (= (response :status) 200))
        (is (= ((parse-string (:body response)) "title") "This is an event title"))
        (is (= ((parse-string (:body response)) "attending") 1))))
    
    ;; Retrieving a particular event should return the id, title
    ;; author username, location, content and comment count
    (testing "retrieving a complete event"
      (let [event (app (mock/request :get "/api/events"))
            event-id ((first (parse-string (event :body))) "eventsid")
            response (app (mock/request :get (str "/api/events/" event-id)))]
        (is (= (response :status) 200))
        (is (= ((parse-string (response :body)) "eventsid") event-id))
        (is (= ((parse-string (response :body)) "author") "a"))
        (is (= ((parse-string (response :body)) "content") "This is an event description"))
        (is (= ((parse-string (response :body)) "commentcount") 0))))
    
    (testing "searching an event"
      (let [response (app (mock/request :get "/api/events/search/description"))]
        (is (= (:status response) 200))
        (is (= ((parse-string (response :body)) "title") "This is an event title"))))
    
    (app (mock/request :delete "/api/users/a"))))
