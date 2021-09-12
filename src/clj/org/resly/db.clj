(ns org.resly.db
  (:require [clojure.java.io :as io]
            [xtdb.api :as xt]))

(defn start-xtdb! []
  (letfn [(kv-store [dir]
            {:kv-store {:xtdb/module 'xtdb.rocksdb/->kv-store
                        :db-dir (io/file dir)
                        :sync? true}})]
    (xt/start-node
     {:xtdb/tx-log (kv-store "data/dev/tx-log")
      :xtdb/document-store (kv-store "data/dev/doc-store")
      :xtdb/index-store (kv-store "data/dev/index-store")})))


(defn stop-xtdb! [node]
  (.close node))

(comment
  (def xtdb-node (start-xtdb!))

  (stop-xtdb! xtdb-node)

  (xt/submit-tx xtdb-node
                [[::xt/put
                  {:xt/id "hi2u2"
                   :user/name "zig"}]])

  (xt/q (xt/db xtdb-node)
        '{:find [(pull ?user [*])]
          :where [[?user :user/name ?name]]})

  )
