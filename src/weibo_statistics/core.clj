(ns weibo-statistics.core
  (:use [clojure.tools.logging :only (debug, info, warn)])
  (:require [weibo-statistics.weibo :as weibo]
            [clojure.java.io :as io]
            [weibo-statistics.config :as config]
            [weibo-statistics.analyser :as analyser])
  (:import (java.io File IOException))
  (:gen-class))


(defn prepare-data-env
  "clean the directory data will be stored"
  []
  (let [data-dir (config/get-config :data-dir "./data")
        d (File. data-dir)]
    (if (.exists d)
      (if (.isFile d)
        (.delete d)
        (doseq [f (file-seq d)]
          (if (not= data-dir (.getPath f))
            (.delete f))))
      (.mkdir d))))


(defn fetch-weibo
  []
  (let [weibo-num (config/get-config :weibo-num 200)
        user-num (config/get-config :user-num 10)
        user (config/get-config :user "swimablefish")]
    (try
      (prepare-data-env)
      (info "data env has prepared")
      (catch IOException e
        (do
          (warn (.getMessage e))
          (System/exit 1))))
    (weibo/get-weibo user user-num weibo-num)))


(defn analyze
  []
  (let [weibo-num (config/get-config :weibo-num 200)]
    (info "analyze phase 1: analyze by word")
    (analyser/analyze-by-word weibo-num)
    (flush)
    (info "analyze phase 2: analyze by hour")
    (analyser/analyze-by-hour weibo-num)))


(def cmd {"fetch-weibo" fetch-weibo
          "analyze" analyze})


(defn print-usage
  []
  (println "Usage (make sure the config file has been set):")
  (println "  fetch-weibo: fetch 10 accounts' weibo, 200 weibo each account")
  (println "  analyze: the result including ")
  (println "            1) top 100 words and occurrences ")
  (println "            2) the number of weibo every hour")
  (System/exit 1))


(defn -main
  [& args]
  (if (not= (count args) 1)
      (print-usage))
  ((cmd (first args) print-usage)))