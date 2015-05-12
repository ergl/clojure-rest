(ns clojure-rest.sanitize-test
  (:require [clojure.test :refer :all]
            [clojure-rest.sanitize :refer [wrap-error
                                           clean-email
                                           clean-username
                                           clean-password]]))

(deftest test-sanitize
  
  (testing "cleaning a valid user should return itself"
    (let [user {:email "foo@foo.com" :username "boob" :password "secret"}]
      (is (= ((wrap-error (clean-email user)) :email) (user :email)))
      (is (= ((wrap-error (clean-username user)) :username) (user :username)))
      (is (= ((wrap-error (clean-password user)) :password) (user :password)))))
  
  (testing "strips whitespace from map fields"
    (let [user {:email "      foo@foo.com  " :username "  ayyy" :password "    secret    "}]
      (is (= ((wrap-error (clean-email user)) :email) (clojure.string/trim (user :email))))
      (is (= ((wrap-error (clean-username user)) :username) (clojure.string/trim (user :username))))
      (is (= ((wrap-error (clean-password user)) :password) (clojure.string/trim (user :password))))))
  
  (testing "rejects an invalid email"
    (let [user {:email "invalidemail" :username "foo" :password "bar"}]
      (is (= (wrap-error (clean-email user)) 400)))))
