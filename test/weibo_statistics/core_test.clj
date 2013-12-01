(ns weibo-statistics.core-test
  (:require [clojure.test :refer :all]
            [weibo-statistics.weibo :as w]
            [weibo-statistics.analyser :as a]))

(deftest finish?-test
  (testing "check finish?"
    (is (= false (w/finish? 3 5 {:a 6 :b 7 :c 1})))
    (is (= true (w/finish? 3 5 {:a 6 :b 7 :c 1 :d 5})))))


(deftest update-user-status-test
  (testing "check update-user-status"
    (is (= {"a" 1 "b" 1} (w/update-user-status {} '("a" "b"))))
    (is (= {"a" 2 "b" 1} (w/update-user-status {} '("a" "b" "a"))))
    (is (= {"a" 4 "b" 1 "c" 1} (w/update-user-status {"a" 2 "c" 1} '("a" "b" "a"))))))


(deftest get-hour-string-test
  (testing "check get-hour-string"
    (is (= "2013-11-30 19" (a/get-hour-string "Sat Nov 30 19:40:06 +0800 2013@test")))))


(deftest filter-test
  (testing "check filter"
    (is (= "aabbd" (a/filter-emotion "aabb[哈哈]d")))
    (is (= "一张图教你玩转微信~~" (a/filter-emotion "一张图教你玩转微信~~[din推撞]")))
    (is (= "aabbd" (a/filter-emotion "aabb[ad]d")))
    (is (= ["d" "c"] (a/filter-after-seg ["d"  "c"])))
    (is (= ["d" "c"] (a/filter-after-seg ["d" "的" "c" "的"])))
    (is (= "aabb d" (a/filter-link "aabbhttp://t.cn/zr0e3hk d")))))

