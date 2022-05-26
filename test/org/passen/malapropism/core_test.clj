(ns org.passen.malapropism.core-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [matcher-combinators.test]
   [org.passen.malapropism.core :as malapropism])
  (:import
   (clojure.lang ExceptionInfo)))

(deftest validate
  (let [schema [:map
                [:foo :int]
                [:bar :string]]]
    (testing "acceptable values are returned"
      (let [values {:foo 12
                    :bar "eggs"}]
        (is (= values (-> (malapropism/with-schema schema)
                          (malapropism/with-values-from-map values)
                          (malapropism/verify!))))))
    (testing "an exception is thrown with unacceptable values"
      (let [values {:foo 23.4
                    :bar [\e \g \g \s]}]
        (is (thrown-match? ExceptionInfo {:schema    schema
                                          :humanized {:foo vector?
                                                      :bar vector?}}
                           (-> (malapropism/with-schema schema)
                               (malapropism/with-values-from-map values)
                               (malapropism/verify!))))))
    (testing "verbose verify includes more data"
      (let [values {:foo 23.4
                    :bar [\e \g \g \s]}]
        (is (thrown-match? ExceptionInfo {:schema    schema
                                          :humanized {:foo vector?
                                                      :bar vector?}
                                          :values    values
                                          :errors    seq?}
                           (-> (malapropism/with-schema schema)
                               (malapropism/with-values-from-map values)
                               (malapropism/verify! ::verbose))))))))
