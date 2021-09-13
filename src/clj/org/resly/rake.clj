(ns org.resly.rake
  (:require
   [clojure.string :as str])
  (:import
   [io.github.crew102.rapidrake RakeAlgorithm]
   [io.github.crew102.rapidrake.data SmartWords]
   [io.github.crew102.rapidrake.model RakeParams]))


(defn- rake-result->map
  [x]
  (let [words (str/split x #" ")
        score (str/replace (last words) #"[\(|\)]" "")
        term (subvec words 0 (dec (count words)))]
    {:term (str/join " " term) :score (Float/parseFloat score)}))

(defn- rake-results->map
  [results]
  (map rake-result->map
       (-> results
           (str/replace #"[\[|\]]" "")
           (str/split #", "))))

(defn apply-rake
  [name input]
  (let [stop-words (.getSmartWords (SmartWords.))
        stop-pos (into-array String ["VB" "VBD" "VBG" "VBN" "VBP" "VBZ"])
        min-word-char 1
        should-stem true
        phrase-delims "[-,.?():;\"!/]"
        params (RakeParams. stop-words stop-pos min-word-char should-stem phrase-delims)
        pos-tagger-url "resources/en-pos-maxent.bin"
        sent-detect-url "resources/en-sent.bin"
        rakeAlgo (RakeAlgorithm. params pos-tagger-url sent-detect-url)
        result (.rake rakeAlgo input)
        mp (bean result)]
    {:position/name name
     :position/full-keywords (:fullKeywords mp)
     :position/stemmed-keywords (:stemmedKeywords mp)
     :position/scores (:scores mp)
     :position/results (rake-results->map (str (.distinct result)))}))

(comment
  (def oi-posting (slurp "resources/openinvest_research_and_strategy_esg.txt"))
  (def r (apply-rake "OpenInvest ESG Analyst" oi-posting))
  (count (:results r))
  (sort-by :score > (:results (apply-rake "OpenInvest ESG Analyst" oi-posting)))
  )
