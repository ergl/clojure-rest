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
  
  ;; Attempting to flip the moderator bit through an user update is forbidden
  (testing "user privilege escalation"
    (let [response (app (-> (mock/request :put "/api/users/bar"
                                          (generate-string {:moderator "true"}))
                            (mock/content-type "application/json")))]
      (is (= (:status response) 403))))
  
  ;; Attempting to change the username of an user to another one that already exists should return a 400 response
  (testing "trying to change an username to another one that already exists"
    (let [user (app (-> (mock/request :post "/api/users"
                                      (generate-string {:email "foo@bar.com"
                                                        :name "foo"
                                                        :username "baz"
                                                        :password "1234"}))
                        (mock/content-type "application/json")))
          response (app (-> (mock/request :put "/api/users/baz"
                                          (generate-string {:username "bar"}))
                            (mock/content-type "application/json")))]
      (is (= (:status response) 400))))
  
  
  ;; Attempting to change the email of an user to another one that already exists should return a 400 response
  (testing "trying to change an user email to another one that already exists"
    (let [response (app (-> (mock/request :put "/api/users/baz"
                                          (generate-string {:email "foo@foo.com"}))
                            (mock/content-type "application/json")))]
      (is (= (:status response) 400))))
  
  ;; Trying to update a non-existing user should result in a 404 response
  (testing "updating a non-existing user"
    (let [response (app (-> (mock/request :put "/api/users/notfound"
                                          (generate-string {:username "whatever"}))
                            (mock/content-type "application/json")))]
      (is (= (:status response) 404))))
  
  ;; Searching for a valid /api/users/search/:query should return the contents
  (testing "searching for an user"
    (let [response (app (mock/request :get "/api/users/search/bar"))]
      (is (= (:status response) 200))
      (is (= ((first (parse-string (:body response))) "username") "bar"))))
  
  ;; Searching for a valid /api/users/search/:query should return 404 if no matches are found
  (testing "searching for something that does not yield any result"
    (let [response (app (mock/request :get "/api/users/search/noresults"))]
      (is (= (:status response) 200))
      (is (empty? (parse-string (:body response))))))
  
  (testing "adding an user as a contact"
    (let [response (app (-> (mock/request :post "/api/users/bar/contacts"
                                          (generate-string {:username "baz"}))
                            (mock/content-type "application/json")))]
      (is (= (:status response) 200))
      (is (= ((parse-string (:body response)) "username") "baz"))))
  
  (testing "adding a non-existing user as a contact"
    (let [response (app (-> (mock/request :post "/api/users/notfound/contacts"
                                          (generate-string {:username "baz"}))
                            (mock/content-type "application/json")))]
      (is (= (:status response) 404))))
  
  (testing "getting the contact list of an user with contacts"
    (let [response (app (mock/request :get "/api/users/bar/contacts"))]
      (is (= (:status response) 200))
      (is (= ((first (parse-string (:body response))) "username") "baz"))))
    
  (testing "getting the contact list of a non-existing user"
    (let [response (app (mock/request :get "/api/users/hue/contacts"))]
      (is (= (:status response) 404))))
  
  ;; Attempting to delete an user through the update procedure should return in a 403 response
  (testing "deleting an user through update"
    (let [response (app (-> (mock/request :put "/api/users/bar"
                                          (generate-string {:deleted "true"}))
                            (mock/content-type "application/json")))]
      (is (= (:status response) 401))))
  
  ;; DELETing an existing user should return a 204 status code
  (testing "deleting an exisiting user"
    (let [response (app (mock/request :delete "/api/users/bar"))
          response1 (app (mock/request :delete "/api/users/baz"))]
      (is (= (:status response) 204))))
  
  ;; DELETEing a non-exisiting user should return a 404
  (testing "deleting a non-existing user"
    (let [response (app (mock/request :delete "/api/users/bar"))]
      (is (= (:status response) 404)))))
