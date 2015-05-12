(ns clojure-rest.validate-test
  (:require [clojure.test :refer :all]
            [clojure-rest.util.validate :as v]))

(deftest test-validate
  
  (testing "finding a non-existing user"
    (is (= (v/field-exists-in-table? "users" "username" "foo") false)))
  
  (testing "finding a non-existing email"
    (is (= (v/field-exists-in-table? "users" "email" "null@null.null") false))))
