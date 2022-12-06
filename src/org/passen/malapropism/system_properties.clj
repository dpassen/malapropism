(ns org.passen.malapropism.system-properties
  "Helper functions to deal with JVM system properties.'"
  (:require
   [camel-snake-kebab.core :as csk]
   [clojure.string :as str]))

(defn system-properties
  []
  (System/getProperties))

(defn parse-key
  [k]
  (let [segments (str/split k #"\.")]
    (->> segments
         (map csk/->kebab-case)
         (str/join \-)
         keyword)))
