(ns org.resly.db
  (:require [clojure.java.io :as io]
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
    (map? x) (let [id (java.util.UUID/randomUUID)
                   m (assoc x :xt/id id)]
               {:transaction (xt/submit-tx node [[::xt/put m]]) :data m})

    (sequential? x) (let [ts (for [doc x]
                               (let [id (java.util.UUID/randomUUID)
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
