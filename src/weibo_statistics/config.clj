(ns weibo-statistics.config
  (:use [clojure.tools.logging :only (warn)])
  (:require [clojure.data.json :as json])
  (:import (java.io IOException)))


(defn get-config
  "get configuration from file named config.json"
  [conf default]
  (try
    ((json/read-json (slurp "./config.json")) conf default)
    (catch IOException e
      (do
        (warn (.getMessage e) "; use default val " default)
        default))))

