(ns org.passen.malapropism.system-properties
  "Helper functions to deal with JVM system properties."
  (:require
   [camel-snake-kebab.core :as csk]
   [clojure.string :as str]))

(defn system-properties
  []
  (System/getProperties))

(defn parse-key
  [k]
  (-> k
      (str/replace \. \-)
      csk/->kebab-case-keyword))
