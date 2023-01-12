(ns org.passen.malapropism.core
  "Malapropism is a malli-backed configuration library."
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
   (clojure.lang ExceptionInfo)
   (java.io PushbackReader)))

(defn with-schema
  "Initializes malapropism with a malli schema."
  [config-schema]
  [config-schema])

(defn with-values-from-map
  "Reads configuration values from a map. This is foundational;
  all other with-values-from-* functions call this one."
  [[config-schema config-values] m]
  (log/infof "Populating, %d values" (count m))
  [config-schema (merge config-values m)])

(defn with-values-from-file
  "Reads configuration values from an edn file."
  [config file]
  (log/info "Populating from file")
  (with-values-from-map
    config
    (-> (io/reader file)
        (PushbackReader.)
        edn/read)))

(defn with-values-from-env
  "Reads configuration values from the process's environment variables."
  [config]
  (log/info "Populating from env")
  (with-values-from-map
    config
    (update-keys
     (environment-variables/environment-variables)
     environment-variables/parse-key)))

(defn with-values-from-system
  "Reads configuration values from the JVM's system properties."
  [config]
  (log/info "Populating from system")
  (with-values-from-map
    config
    (update-keys
     (system-properties/system-properties)
     system-properties/parse-key)))

(defn verify!
  "Verifies the configuration matches the schema.
  Returns the coerced configuration data if valid,
  throws an exception if not. verbose? flag controls
  whether the ex-data contains the original values."
  [[config-schema config-values] & {:keys [verbose?]}]
  (let [transformer (mt/transformer
                     mt/strip-extra-keys-transformer
                     mt/default-value-transformer
                     mt/string-transformer)]
    (try
      (m/coerce config-schema config-values transformer)
      (catch ExceptionInfo e
        (let [{{:keys [explain]} :data} (ex-data e)]
          (throw
           (ex-info
            "Config values do not match schema!"
            (cond->
                {:humanized (me/humanize explain)
                 :schema    config-schema}

              verbose?
              (assoc :errors (:errors explain)
                     :values config-values)))))))))
