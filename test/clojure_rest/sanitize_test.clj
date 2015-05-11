(ns clojure-rest.sanitize-test
  (:require [clojure.test :refer :all]
            [clojure-rest.sanitize :as s]
            [clojure-rest.validate :as v]))

(deftest test-sanitize
  
  (testing "clean up a signup"
    (let [user {:email "foo@foo.com" :username "username" :password "password"}]
      (is (= (s/signup-flow user) user))))
  
  (testing "clean up a login"
    (let [user {:username "ayy" :password "foo"}]
      (is (= (s/login-flow user) user))))
  
  (testing "foo"
    (is (= (v/field-exists-in-table? "users" "username" "foo") false))))
