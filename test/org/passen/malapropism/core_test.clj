(ns org.passen.malapropism.core-test
  (:require
   [clojure.java.io :as io]
   [clojure.test :refer [deftest is testing]]
   [matcher-combinators.matchers :as matchers]
   [matcher-combinators.test]
   [org.passen.malapropism.core :as malapropism]))

(def ^:private schema
  [:map
   [:foo-bar :int]
   [:baz :keyword]])

(deftest with-values-from-map
  (testing "keys not present in schema are omitted"
    (let [values {:foo-bar 12
                  :baz     :eggs
                  :bat     ["spam"]}]
      (is (= {:foo-bar 12
              :baz     :eggs}
             (-> (malapropism/with-schema schema)
                 (malapropism/with-values-from-map values)
                 (malapropism/verify!)))))))

(deftest with-values-from-file
  (testing "can read edn from the classpath"
    (is (= {:foo-bar 23
            :baz     :yep}
           (-> (malapropism/with-schema schema)
               (malapropism/with-values-from-file (io/resource "values.edn"))
               (malapropism/verify!))))))

(deftest with-values-from-env
  (testing "can read from env and turn ENV_VAR looking keys into :env-var"
    (with-redefs [malapropism/environment-variables
                  (constantly {"FOO_BAR" "12"
                               "BAZ"     "bat"})]
      (is (= {:foo-bar 12
              :baz     :bat}
             (-> (malapropism/with-schema schema)
                 (malapropism/with-values-from-env)
                 (malapropism/verify!)))))))

(deftest validate
  (testing "acceptable values are returned"
    (let [values {:foo-bar 12
                  :baz     :eggs}]
      (is (= values (-> (malapropism/with-schema schema)
                        (malapropism/with-values-from-map values)
                        (malapropism/verify!))))))
  (testing "acceptable values may start as strings"
    (let [values {:foo-bar "12"
                  :baz     "spam"}]
      (is (= {:foo-bar 12
              :baz     :spam}
             (-> (malapropism/with-schema schema)
                 (malapropism/with-values-from-map values)
                 (malapropism/verify!))))))
  (testing "acceptable values may come from any source and be overridden"
    (let [values {:foo-bar "12"
                  :baz     "spam"}]
      (with-redefs [malapropism/environment-variables
                    (constantly {"FOO_BAR" "16"})]
        (is (= {:foo-bar 16
                :baz     :yep}
               (-> (malapropism/with-schema schema)
                   (malapropism/with-values-from-map values)
                   (malapropism/with-values-from-file (io/resource "values.edn"))
                   (malapropism/with-values-from-env)
                   (malapropism/verify!)))))))
  (testing "an exception is thrown with unacceptable values"
    (let [values {:foo-bar 23.4
                  :baz     [\e \g \g \s]}]
      (is (thrown-match?
           (matchers/match-with
            [map? matchers/equals]
            {:schema schema
             :humanized
             {:foo-bar vector?
              :baz     vector?}})
           (-> (malapropism/with-schema schema)
               (malapropism/with-values-from-map values)
               (malapropism/verify!))))))
  (testing "verbose verify includes more data"
    (let [values {:foo-bar 23.4
                  :baz     [\e \g \g \s]}]
      (is (thrown-match?
           {:values values
            :errors seq?
            :schema schema
            :humanized
            {:foo-bar vector?
             :baz     vector?}}
           (-> (malapropism/with-schema schema)
               (malapropism/with-values-from-map values)
               (malapropism/verify! :verbose? true)))))))
