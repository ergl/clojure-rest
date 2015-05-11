(ns clojure-rest.sanitize-test
  (:require [clojure.test :refer :all]
            [clojure-rest.sanitize :as s]))

(deftest test-sanitize
  
  (testing "cleaning a valid signup map should return the same map"
    (let [user {:email "foo@foo.com" :username "username" :password "password"}]
      (is (= (s/signup-flow user) user))))
  
  (testing "cleaning a valid login map should return the same map"
    (let [user {:username "ayy" :password "foo"}]
      (is (= (s/login-flow user) user))))
  
  (testing "extra spaces are trimmed from the map values"
    (let [user {:email "       fo@fo.com     " :username "ayy    " :password "foo  "}]
      (is (= ((s/signup-flow user) :email) (clojure.string/trim (user :email))))
      (is (= ((s/login-flow user) :username) (clojure.string/trim (user :username))))
      (is (= ((s/login-flow user) :password) (clojure.string/trim (user :password))))))
  
  (testing "string map is keywordized on sanitization"
    (let [user {"email" "foo@foo.com" "username" "john" "password" "mypassword"}]
      (is (= (s/signup-flow user) (clojure.walk/keywordize-keys user)))))
  
  (testing "malformed map should return a 400 Bad Request error code"
    (let [signup-user {:email "" :username "whatever" :password "1"}
          login-user {:username "" :password ""}]
      (is (= (s/login-flow login-user) 400))
      (is (= (s/signup-flow signup-user) 400)))))
