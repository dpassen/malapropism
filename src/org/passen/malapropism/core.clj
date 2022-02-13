(ns org.passen.malapropism.core
  (:require
   [camel-snake-kebab.core :as csk]
   [camel-snake-kebab.extras :as cske]
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.tools.logging :as log]
   [malli.core :as m]
   [malli.error :as me])
  (:import
   (java.io PushbackReader)))

(def ^:private config-schema
  (atom nil))

(def ^:private config-values
  (atom nil))

(defn- schema-keys
  []
  (m/walk
   @config-schema
   (fn [schema _ children _options]
     (let [children (if (m/entries schema)
                      (filter last children)
                      children)]
       (map first children)))))

(defn set-schema!
  [schema]
  (reset! config-schema schema)
  (reset! config-values nil))

(defn map->values
  [m]
  (let [values (select-keys m (schema-keys))]
    (log/infof "Populating, %d values" (count values))
    (swap! config-values merge values)))

(defn file->values
  [file]
  (log/info "Populating from file")
  (-> (io/reader file)
      (PushbackReader.)
      edn/read
      map->values))

(defn env->values
  []
  (log/info "Populating from env")
  (->> (System/getenv)
       (into {})
       (cske/transform-keys csk/->kebab-case-keyword)
       map->values))

(defn verify!
  ([]
   (verify! nil))
  ([verbose?]
   (when-not (m/validate @config-schema @config-values)
     (let [explanation (m/explain @config-schema @config-values)]
       (throw
        (ex-info
         "Config values do not match schema!"
         (cond->
          {:humanized (me/humanize explanation)
           :schema    @config-schema}

           verbose?
           (assoc :explanation explanation
                  :values @config-values))))))))

(defn lookup
  ([key]
   (lookup key nil))
  ([key not-found]
   (get @config-values key not-found)))
