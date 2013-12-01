(ns weibo-statistics.analyser
  (:require [clj-time.core :as tc]
            [clj-time.coerce :as tco]
            [clj-time.format :as tf]
            [clojure.string :as str]
            [weibo-statistics.lexer :as lex]
            [weibo-statistics.config :as config])
  (:use [clojure.tools.logging :only (debug, info, warn)])
  (:import (java.util Locale TimeZone)
           (org.joda.time DateTimeZone)
           (java.io IOException)))


(def DATA_PATH (config/get-config :data-dir "./data"))
(def date-formatter (tf/formatter "EEE MMM dd HH:mm:ss Z yyyy"))
(def hour-formatter (tf/formatter "yyyy-MM-dd HH"))
(def locale (Locale. "en" "US"))
(def zone (DateTimeZone/forID "+08"))


(defn- read-lines
  [file]
  (str/split-lines (slurp file)))


(defn filter-emotion
  [string]
  (str/replace string #"\[[\w\u4e00-\u9fa5]*\]" ""))


(defn filter-link
  [string]
  (str/replace string #"http://[\S]*" ""))


(defn filter-after-seg
  "this can filter some words after segment"
  [word-list]
  (let [filter-list ["的" "”" "“" "…"]]
    (filter
      (fn [elm] (not (some #(= elm %) filter-list)))
      word-list)))


(defn gen-chart
  "according to template.htm replace @categories@ and @data@, write into a new file"
  [name data]
  (let [categories (reduce #(str %1 "'" %2 "', ") "" (map #(first %) data))
        rslt (reduce #(str %1 %2 ", ") "" (map #(last %) data))]
    (try
      (spit
        (str name ".htm")
        (-> (slurp "template.htm")
          (str/replace #"@categories@" categories)
          (str/replace #"@data@" rslt)))
      (catch IOException e (warn (.getMessage e) "gen chart fail")))))


(defn- handle-weibo-word
  [statistics data]
  (let [d (last (str/split data #"@" 2))
        str (-> d filter-emotion filter-link)]
    (reduce
      #(assoc %1 %2 (inc (%1 %2 0)))
      statistics
      (filter-after-seg (lex/seg-string str)))))


(defn- handle-by-word
  [data-seq statistics]
  (reduce handle-weibo-word statistics data-seq))


(defn- show-by-word
  [statistics]
  (let [s (sort #(> (last %1) (last %2)) statistics)
        n (config/get-config :top-n 100)
        popular (take n s)]
    (println "The top " n " keywords' occurrences")
    (println "**********************************")
    (doseq [data popular]
      (println (str (first data) ":\t" (second data))))
    (println "**********************************")
    (info "generate chart word.htm")
    (gen-chart "word" popular)))


(defn get-hour-string
  [data]
  (let [date-str (first (str/split data #"@"))
        date (tf/parse (tf/with-locale date-formatter locale) date-str)]
    (tf/unparse (tf/with-zone hour-formatter zone) date)))


(defn- handle-by-hour
  [data-seq statistics]
  (reduce
    #(assoc %1 %2 (inc (%1 %2 0)))
    statistics
    (map get-hour-string data-seq)))


(defn- show-by-hour
  [statistics]
  (println "The number of weibo in each hour")
  (println "**********************************")
  (let [sort-data (sort
                    #(tc/before?
                      (tf/parse hour-formatter (first %1))
                      (tf/parse hour-formatter (first %2)))
                    statistics)]
  (doseq [data sort-data]
    (println (str (first data) ":\t" (second data))))
  (println "**********************************")
  (info "generate chart hour.htm")
  (gen-chart "hour" sort-data)))


(defn- analyze
  [weibo-num analyze-fn show-fn]
  (let [users (read-lines (str DATA_PATH "/user_list"))]
    (loop [statistics {}
           index 0]
      (if (= index (count users))
        (show-fn statistics)
        (recur
          (analyze-fn
            (take weibo-num (read-lines (str DATA_PATH "/" (nth users index))))
            statistics)
          (inc index))))))


(defn analyze-by-hour
  [weibo-num]
  (analyze weibo-num handle-by-hour show-by-hour))


(defn analyze-by-word
  [weibo-num]
  (analyze weibo-num handle-by-word show-by-word))

