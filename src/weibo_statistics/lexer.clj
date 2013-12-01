(ns weibo-statistics.lexer
  (:import (org.lionsoul.jcseg.core JcsegTaskConfig
             DictionaryFactory
             ISegment
             SegmentFactory
             ILexicon)
           (java.io StringReader)))


(defn- init-seg
  []
  (let [config (JcsegTaskConfig. "resources/jcseg.properties")
        dic (DictionaryFactory/createDefaultDictionary config)]
    (SegmentFactory/createJcseg
      JcsegTaskConfig/COMPLEX_MODE
      (object-array [config dic]))))


(def seg (init-seg))


(defn seg-string
  [string]
  (.reset seg (StringReader. string))
  (loop [word-list '()
         word (.next seg)]
    (if (= word nil)
      word-list
      (recur
        (if (= (.getType word) (ILexicon/CE_MIXED_WORD))
          word-list
          (conj word-list (.getValue word)))
        (.next seg)))))
