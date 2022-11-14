(ns org.passen.malapropism.core
  (:require
   [camel-snake-kebab.core :as csk]
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.tools.logging :as log]
   [malli.core :as m]
   [malli.error :as me]
   [malli.transform :as mt])
  (:import
   (java.io PushbackReader)))

(defn with-schema
  [config-schema]
  [config-schema nil])

(defn with-values-from-map
  [[config-schema config-values] m]
  (log/infof "Populating, %d values" (count m))
  [config-schema (merge config-values m)])

(defn with-values-from-file
  [config file]
  (log/info "Populating from file")
  (with-values-from-map
    config
    (-> (io/reader file)
        (PushbackReader.)
        edn/read)))

(defn- environment-variables
  []
  (System/getenv))

(defn with-values-from-env
  [config]
  (log/info "Populating from env")
  (with-values-from-map
    config
    (update-keys (environment-variables) csk/->kebab-case-keyword)))

(defn verify!
  [[config-schema config-values] & {:keys [verbose?]}]
  (let [transformer        (mt/transformer
                            mt/strip-extra-keys-transformer
                            mt/default-value-transformer
                            mt/string-transformer)
        transformed-values (m/decode config-schema config-values transformer)]
    (if (m/validate config-schema transformed-values)
      transformed-values
      (let [explanation (m/explain config-schema transformed-values)]
        (throw
         (ex-info
          "Config values do not match schema!"
          (cond->
              {:humanized (me/humanize explanation)
               :schema    config-schema}

            verbose?
            (assoc :errors (:errors explanation)
                   :values config-values))))))))
