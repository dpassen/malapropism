(ns org.passen.malapropism.core
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.tools.logging :as log]
   [malli.core :as m]
   [malli.error :as me]
   [malli.transform :as mt]
   [org.passen.malapropism.environment-variables :as environment-variables]
   [org.passen.malapropism.system-properties :as system-properties])
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

(defn with-values-from-env
  [config]
  (log/info "Populating from env")
  (with-values-from-map
    config
    (update-keys
     (environment-variables/environment-variables)
     environment-variables/parse-key)))

(defn with-values-from-system
  [config]
  (log/info "Populating from system")
  (with-values-from-map
    config
    (update-keys
     (system-properties/system-properties)
     system-properties/parse-key)))

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
