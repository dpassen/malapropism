(ns org.passen.malapropism.environment-variables
  "Helper functions to deal with process environment variables."
  (:require
   [camel-snake-kebab.core :as csk]))

(defn environment-variables
  []
  (System/getenv))

(defn parse-key
  [k]
  (csk/->kebab-case-keyword k))
