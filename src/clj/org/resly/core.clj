(ns org.resly.core
  (:require
   [org.resly.rake :as rake]))

(comment
  (def posting (slurp "resources/openinvest_research_and_strategy_esg.txt"))
  (rake/apply-rake posting)
  )
