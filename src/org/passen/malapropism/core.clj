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

(defn- schema-keys
  [config-schema]
  (m/walk
   config-schema
   (fn [schema _ children _options]
     (let [children (if (m/entries schema)
                      (filter last children)
                      children)]
       (map first children)))))

(defn with-schema
  [config-schema]
  [config-schema nil])

(defn with-values-from-map
  [[config-schema config-values] m]
  (let [values (select-keys m (schema-keys config-schema))]
    (log/infof "Populating, %d values" (count values))
    [config-schema (merge config-values values)]))

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
    (-> (System/getenv)
        (into {})
        (update-keys csk/->kebab-case-keyword))))

(defn verify!
  ([config]
   (verify! config nil))
  ([[config-schema config-values] verbose?]
   (let [transform          (m/decoder config-schema (mt/string-transformer))
         transformed-values (transform config-values)]
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
                    :values config-values)))))))))
