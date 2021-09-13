(ns org.resly.db
  (:require
   [clojure.java.io :as io]
   [org.resly.rake :as rake]
   [org.resly.utils :as utils]
   [xtdb.api :as xt]))

(declare node)

(defn start-xtdb! []
  (letfn [(kv-store [dir]
            {:kv-store {:xtdb/module 'xtdb.rocksdb/->kv-store
                        :db-dir (io/file dir)
                        :sync? true}})]
    (def node (xt/start-node
               {:xtdb/tx-log (kv-store "data/dev/tx-log")
                :xtdb/document-store (kv-store "data/dev/doc-store")
                :xtdb/index-store (kv-store "data/dev/index-store")}))
    node))


(defn stop-xtdb! [node]
  (.close node))

(defn put
  [x]
  (cond
    (map? x) (let [id (or (:xt/id x) (utils/uuid))
                   m (assoc x :xt/id id)]
               {:transaction (xt/submit-tx node [[::xt/put m]]) :data m})

    (sequential? x) (let [ts (for [doc x]
                               (let [id (or (:xt/id x) (utils/uuid))
                                     m (assoc doc :xt/id id)]
                                 [::xt/put m]))]
                      {:transaction (xt/submit-tx node (vec ts))
                       :data (map second ts)})
    :else (throw (Exception. (str "org.resly.db/put only accepts maps and sequences, but was passed " x)))))

(defn q [query & args]
  (apply xt/q (xt/db node) query args))

(comment
  (start-xtdb!)
  (stop-xtdb! node)

  (xt/submit-tx node
                [[::xt/put
                  {:xt/id "hi2u2"
                   :user/name "zig"
                   :user/time (java.time.Instant/now)}]])

  (put {:user/name "go"})
  (put [{:user/name "rust"} {:user/name "C#"}])

  (xt/q (xt/db node)
        '{:find [(pull ?user [*])]
          :where [[?user :user/name]]})

  (q '{:find [(pull ?user [*])]
       :where [[?user :user/name "zig"]]})

  )

(comment
  (start-xtdb!)
  (let [data (rake/apply-rake
              "OpenInvest ESG Analyst"
              (slurp "resources/openinvest_research_and_strategy_esg.txt"))
        id (utils/uuid)
        data' (-> data (assoc :xt/id id) (dissoc :position/results))
        results (map #(hash-map
                       :xt/id (utils/uuid)
                       :rake.result/score (:score %)
                       :rake.result/term (:term %)
                       :position/id id) (:position/results data))]
    (put data')
    (put results))
  (q '{:find [(pull ?e [*])]
          :where [[?e :position/name]]})

  (xt/submit-tx node
                (mapv #(vector ::xt/evict %)
                      (map first
                           (vec (q '{:find [?e]
                                        :where [[?e :result/score]]})))))
  (xt/submit-tx node
                (mapv #(vector ::xt/evict %)
                      (map first
                           (vec (q '{:find [?e]
                                        :where [[?e :position/name]]})))))

  (q '{:find [(pull ?e [* {:position/id [:position/name]}])]
          :where [[?e :rake.result/score]
                  [?e :position/id ?p]
                  [?p :position/name ?name]]})

  (q '{:find [(pull ?p [:position/name {:position/_id [:rake.result/score]}])]
          :where [[?p :position/name]]})

  (def posting (slurp "resources/openinvest_research_and_strategy_esg.txt"))
  (rake/apply-rake "OpenInvest ESG Analyst" posting)
  )
