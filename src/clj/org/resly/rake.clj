(ns org.resly.rake
  (:require [libpython-clj2.python :as py :refer [py. py.. py.-]]))

(def rake-nltk (py/import-module "rake_nltk"))

(defn get-ranked-phrases-with-scores [text & {:keys [min-length max-length] :or {min-length 1 max-length 1000}}]
  (let [rake (py. rake-nltk Rake :min_length min-length :max_length max-length)]
    (py. rake extract_keywords_from_text text)
    (into [] (map vec (py. rake get_ranked_phrases_with_scores)))))

(comment
  (get-ranked-phrases-with-scores
   (slurp "resources/openinvest_research_and_strategy_esg.txt")
   :max-length 3))
