(ns clojure-rest.sanitize-test
  (:require [clojure.test :refer :all]
            [clojure-rest.util.error :refer [wrap-error]]
            [clojure-rest.util.user-sanitize :as us]))

(deftest test-sanitize
  
  (testing "cleaning a valid user should return itself"
    (let [user {:email "foo@foo.com" :username "boob" :password "secret"}]
      (is (= (wrap-error (us/sanitize-signup user)) user))))
  
  (testing "strips whitespace from map fields"
    (let [user {:email "      foo@foo.com  " :username "  ayyy" :password "    secret    "}]
      (is (= (wrap-error (us/sanitize-signup user)) (assoc user
                                                      :email (clojure.string/trim (user :email))
                                                      :username (clojure.string/trim (user :username))
                                                      :password (clojure.string/trim (user :password)))))))
  
  (testing "rejects an invalid email"
    (let [user {:email "invalidemail" :username "foo" :password "bar"}]
      (is (= (wrap-error (us/sanitize-signup user)) 400)))))
