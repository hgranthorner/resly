(ns org.resly.core
  (:require [org.resly.utils :as u]
            [clj-http.client :as client]
            [clojure.string :as st]
            [clojure.data.json :as json]
            [clojure.edn :as edn])
  (:import [java.io StringReader]
           [org.apache.lucene.analysis CharArraySet StopFilter]
           [org.apache.lucene.analysis.core LowerCaseFilter]
           [org.apache.lucene.analysis.en EnglishMinimalStemFilter PorterStemFilter]
           [org.apache.lucene.analysis.standard StandardTokenizer]
           [org.apache.lucene.analysis.tokenattributes CharTermAttribute]))

(defn tokenize-input [input stemmer]
  (let [char-array-set (CharArraySet. (st/split-lines (slurp "resources/en_stopwords.txt")) true)
        tokenizer (-> (doto (new StandardTokenizer)
                        (.setReader (new StringReader input))
                        (.reset))
                      (LowerCaseFilter.)
                      (StopFilter. char-array-set)
                      stemmer)
        attr (.addAttribute tokenizer CharTermAttribute)]
    (loop [acc []
           token (.incrementToken tokenizer)]
      (if token
        (recur (conj acc (str attr)) (.incrementToken tokenizer))
        (do (.close tokenizer)
            acc)))))

(defn token-weights [tokens]
  (let [num (count tokens)]
    (->> tokens
        frequencies
        (map (fn [[k v]] [k (float (/ v num))]))
        (into {}))))

(comment
  (def oi-posting (slurp "resources/openinvest_research_and_strategy_esg.txt"))
  (sort (frequencies (tokenize-input oi-posting #(EnglishMinimalStemFilter. %))))
  (sort (frequencies (tokenize-input oi-posting #(PorterStemFilter. %))))
  (sort-by second > (token-weights (tokenize-input oi-posting #(PorterStemFilter. %))))
  (edn/read-string (slurp "resources/test_data.edn"))
  (def api-key (st/replace (slurp "resources/config.txt") #"\n" ""))
  (let [url "https://nubela.co/proxycurl/api/v2/linkedin?url=https%3A%2F%2Fwww.linkedin.com%2Fin%2Fhgranthorner"
        data (client/get url
                         {:headers {"Authorization" (str "Bearer " api-key)}})]
    (u/transform-keys u/snake_case->kebab-case (json/read-json (:body data))))
  (comment))
