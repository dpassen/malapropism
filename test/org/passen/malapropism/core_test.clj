(ns org.passen.malapropism.core-test
  (:require
   [clojure.java.io :as io]
   [clojure.test :refer [deftest is testing]]
   [matcher-combinators.matchers :as matchers]
   [matcher-combinators.test]
   [org.passen.malapropism.core :as malapropism]
   [org.passen.malapropism.environment-variables :as environment-variables]
   [org.passen.malapropism.system-properties :as system-properties]))

(def ^:private schema
  [:map
   [:env-key :keyword]
   [:scm-rev :string]
   [:port :int]
   [:prefix
    {:default "/api"}
    :string]])

(deftest with-values-from-file
  (testing "can read edn from the classpath"
    (is (= {:env-key :qa
            :scm-rev "ffffff"
            :port    8443
            :prefix  "/app"}
           (-> (malapropism/with-schema schema)
               (malapropism/with-values-from-file (io/resource "values.edn"))
               (malapropism/verify!))))))

(deftest with-values-from-env
  (testing "can read from env and turn ENV_VAR looking keys into :env-var"
    (with-redefs [environment-variables/environment-variables
                  (constantly {"ENV_KEY" "local"
                               "SCM_REV" "123456"
                               "PORT"    "5000"
                               "PREFIX"  "/v2"})]
      (is (= {:env-key :local
              :scm-rev "123456"
              :port    5000
              :prefix  "/v2"}
             (-> (malapropism/with-schema schema)
                 (malapropism/with-values-from-env)
                 (malapropism/verify!)))))))

(deftest with-values-from-system
  (testing "can read from system and turn system.PropertyName looking keys into system-property-name"
    (with-redefs [system-properties/system-properties
                  (constantly {"env.key" "remote"
                               "scmRev"  "aeiouy"
                               "port"    "3600"
                               "prefix"  "/main"})]
      (is (= {:env-key :remote
              :scm-rev "aeiouy"
              :port    3600
              :prefix  "/main"}
             (-> (malapropism/with-schema schema)
                 (malapropism/with-values-from-system)
                 (malapropism/verify!)))))))

(deftest validate
  (testing "acceptable values are returned"
    (let [values {:env-key :test
                  :scm-rev "n/a"
                  :port    9000
                  :prefix  "/api/v3"}]
      (is (= values (-> (malapropism/with-schema schema)
                        (malapropism/with-values-from-map values)
                        (malapropism/verify!))))))
  (testing "acceptable values may start as strings"
    (let [values {:env-key "stage"
                  :scm-rev "abc123"
                  :port    "9443"
                  :prefix  "/webapp"}]
      (is (= {:env-key :stage
              :scm-rev "abc123"
              :port    9443
              :prefix  "/webapp"}
             (-> (malapropism/with-schema schema)
                 (malapropism/with-values-from-map values)
                 (malapropism/verify!))))))
  (testing "acceptable values may come from any source and be overridden"
    (let [values {:prefix "/myapp"}]
      (with-redefs [environment-variables/environment-variables
                    (constantly {"ENV_KEY" "qa2"})
                    system-properties/system-properties
                    (constantly {"scm.Rev" "aaaaaa"})]
        (is (= {:env-key :qa2
                :scm-rev "aaaaaa"
                :port    8443
                :prefix  "/myapp"}
               (-> (malapropism/with-schema schema)
                   (malapropism/with-values-from-file (io/resource "values.edn"))
                   (malapropism/with-values-from-map values)
                   (malapropism/with-values-from-env)
                   (malapropism/with-values-from-system)
                   (malapropism/verify!)))))))
  (testing "keys not present in schema are omitted"
    (let [values {:env-key :dev
                  :scm-rev "923345"
                  :port    8080
                  :prefix  "/web"
                  :foo     "bar"}]
      (is (= {:env-key :dev
              :scm-rev "923345"
              :port    8080
              :prefix  "/web"}
             (-> (malapropism/with-schema schema)
                 (malapropism/with-values-from-map values)
                 (malapropism/verify!))))))
  (testing "keys with default values may be omitted"
    (let [values {:env-key :prod
                  :scm-rev "zyx987"
                  :port    3000}]
      (is (= {:env-key :prod
              :scm-rev "zyx987"
              :port    3000
              :prefix  "/api"}
             (-> (malapropism/with-schema schema)
                 (malapropism/with-values-from-map values)
                 (malapropism/verify!))))))
  (testing "an exception is thrown with unacceptable values"
    (let [values {:env-key true
                  :scm-rev "zyx987"
                  :port    3500}]
      (is (thrown-match?
           (matchers/match-with
            [map? matchers/equals]
            {:schema schema
             :humanized
             {:env-key vector?}})
           (-> (malapropism/with-schema schema)
               (malapropism/with-values-from-map values)
               (malapropism/verify!))))))
  (testing "verbose verify includes more data"
    (let [values {:env-key false
                  :scm-rev "zyx987"
                  :port    4242}]
      (is (thrown-match?
           {:values values
            :errors seq?
            :schema schema
            :humanized
            {:env-key vector?}}
           (-> (malapropism/with-schema schema)
               (malapropism/with-values-from-map values)
               (malapropism/verify! :verbose? true)))))))
