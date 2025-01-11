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

(def ^:private transformer
  (mt/transformer
   mt/strip-extra-keys-transformer
   mt/default-value-transformer
   mt/string-transformer))

(defn with-schema
  "Initializes malapropism with a malli schema."
  [schema]
  (let [coerce (m/coercer schema transformer)]
    {::coerce coerce
     ::schema schema}))

(defn with-values-from-map
  "Reads configuration values from a map. This is foundational;
  all other with-values-from-* functions call this one."
  [config m]
  (log/infof "Populating, %d values" (count m))
  (update config ::values merge m))

(defn with-values-from-file
  "Reads configuration values from an edn file."
  [config file]
  (log/info "Populating from file")
  (with-values-from-map
    config
    (-> (io/reader file)
        PushbackReader/new
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
  [{::keys [coerce schema values]} & {:keys [verbose?]}]
  (try
    (coerce values)
    (catch ExceptionInfo e
      (let [{{:keys [explain]} :data} (ex-data e)]
        (throw
         (ex-info
          "Config values do not match schema!"
          (cond->
           {:humanized (me/humanize explain)
            :schema    schema}

            verbose?
            (assoc :errors (:errors explain)
                   :values values))))))))
