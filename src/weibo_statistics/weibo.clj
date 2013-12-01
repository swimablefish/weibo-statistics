(ns weibo-statistics.weibo
  (:use [clojure.tools.logging :only (debug, info, warn)])
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.string :as str]
            [weibo-statistics.config :as config]))


(def WEIBO_API_PREFIX "https://api.weibo.com/2")
(def FETCH_LIMIT 100)
(def DATA_PATH (config/get-config :data-dir "./data"))
(def ACCESS_TOKEN (config/get-config :access-token ""))


(defmulti get-return-val
  (fn [resp] (:status resp))
  :default nil)


(defmethod get-return-val 200
  [resp]
  (:body resp))


(defmethod get-return-val nil
  [resp]
  (warn (str "request fail: " (:body resp)))
  "{}")


(defn- http-get
  [method params]
  (try
    (client/get (str WEIBO_API_PREFIX method)
               {:query-params  (assoc params :access_token ACCESS_TOKEN)
                :throw-exceptions false})
    (catch Exception e
      (do
        (warn (.getMessage e))
        {}))))


(defn call-weibo-api
  "use get method to call request"
  [method params]
  (let [resp (http-get method params)]
    (get-return-val resp)))


(defn- weibo-handler
  [weibo]
  (let [created_time (weibo :created_at)
        text (str/replace (weibo :text) #"[\n\r]+" "")
        user (-> weibo :user :screen_name)]
    (spit
      (str DATA_PATH "/" user)
      (str created_time "@" text "\n")
      :append true)
    user))


(defn- get-home-timeline
  [user page]
  (let [weibo (call-weibo-api "/statuses/home_timeline.json"
                {:screen_name user
                 :count FETCH_LIMIT
                 :page page})
        weibo-json ((json/read-json weibo) :statuses)]
    ;;every hour can call api only 150 times; sleep 25 seconds
    (Thread/sleep 25000)
    (map weibo-handler weibo-json)))


(defn finish?
  [user-num weibo-num user-status]
  (let [reach-weibo-num (filter #(>= % weibo-num) (vals user-status))
        total (count reach-weibo-num)]
    (info total "accounts have reach" weibo-num)
    (>= total user-num)))


(defn update-user-status
  [user-status new-user-list]
  (reduce #(assoc %1 %2 (inc (%1 %2 0))) user-status new-user-list))


(defn- record-users
  [user-num weibo-num user-status]
  (doseq [user (take user-num (filter #(>= (second %) weibo-num) user-status))]
    (spit
      (str DATA_PATH "/" "user_list")
      (str (first user) "\n")
      :append true)))


(defn get-weibo
  [user user-num weibo-num]
  (loop [user-status {}
         page 1]
    (if (finish? user-num weibo-num user-status)
      (record-users user-num weibo-num user-status)
      (let [user-list (get-home-timeline user page)
            num (count user-list)]
        (info "fetch" num "weibo")
        (recur
          (update-user-status user-status user-list)
          (if (= num 0) page (inc page)))))))
