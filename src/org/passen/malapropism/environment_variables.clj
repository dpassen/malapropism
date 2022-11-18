(ns org.passen.malapropism.environment-variables
  (:require
   [camel-snake-kebab.core :as csk]))

(defn environment-variables
  []
  (System/getenv))

(defn parse-key
  [k]
  (csk/->kebab-case-keyword k))
