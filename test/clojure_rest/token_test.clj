(ns clojure-rest.token-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [clojure-rest.handler :refer :all]
            [cheshire.core :refer [parse-string]]
            [cheshire.core :refer [generate-string]]))

(deftest authorize-users-with-token
  ;; Creates a dummy user for testing purposes
  (let [user (app (-> (mock/request :post "/api/users"
                                    (generate-string {:email "foo@com.com"
                                                      :name "pepe"
                                                      :username "xyzz"
                                                      :password "sosecret"}))
                      (mock/content-type "application/json")))
        
        token (app (-> (mock/request :post "/api/auth"
                                     (generate-string {:username "xyzz"
                                                       :password "sosecret"}))
                       (mock/content-type "application/json")))
        
        token-value ((parse-string (token :body)) "token")
        
        alt-token (app (-> (mock/request :post "/api/auth"
                                         (generate-string {:username "xyzz"
                                                           :password "sosecret"}))
                           (mock/content-type "application/json")))
        
        alt-token-value ((parse-string (alt-token :body)) "token")]
    
    (testing "trying to delete a non-existing token should return 404"
      (let [response (app (mock/request :delete "/api/auth/bogus"))]
        (is (= (response :status) 404))))
    
    (testing "trying to revoke a different token from our own should return 403"
      (let [response (app (-> (mock/request :delete (str "/api/auth/" token-value)
                                            (generate-string {:token "bogustoken"}))
                              (mock/content-type "application/json")))]
        (is (= (response :status) 403))))
    
    (testing "deleting a valid token should return 204"
      (let [response (app (-> (mock/request :delete (str "/api/auth/" token-value)
                                            (generate-string {:token token-value}))
                              (mock/content-type "application/json")))]
        (is (= (response :status) 204))))
    
    (testing "deleting an user deletes all his auth tokens"
      (let [delete-response (app (mock/request :delete "/api/users/xyzz"))
            response (app (-> (mock/request :delete (str "/api/auth/" alt-token-value)
                                            (generate-string {:token alt-token-value}))
                              (mock/content-type "application/json")))]
        (is (= (delete-response :status) 204))
        (is (= (response :status) 404))))))
