(defproject weibo-statistics "0.1.0-SNAPSHOT"
  :description "a weibo statistics tool"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [clj-http "0.7.7"]
                 [clj-time "0.6.0"]
                 [log4j/log4j "1.2.16"]
                 [org.clojure/tools.logging "0.2.6"]
                 [org.clojure/data.json "0.2.3"]]
  :resource-paths ["lib/*" "resources"]
  :main weibo-statistics.core)
