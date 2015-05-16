(ns clojure-rest.validate-test
  (:require [clojure.test :refer :all]
            [clojure-rest.util.validate :as v]
            [clojure-rest.util.error :refer [wrap-error]]))

(deftest test-validate
  
  (testing "finding a non-existing user"
    (is (= (v/field-exists-in-table? "users" "username" "foo") false)))
  
  (testing "finding a non-existing email"
    (is (= (v/field-exists-in-table? "users" "email" "null@null.null") false)))

  ;; Checking for a parameter in a map with a predicate returns the same map if true, nil if false
  (testing "check-field works correctly"
    (let [params {:username "foo"}]
      (is (= (wrap-error (v/check-field params :username nil?)) params)))))
