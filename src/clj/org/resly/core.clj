(ns org.resly.core
  (:require [org.resly.utils :as u]
            [clj-http.client :as client]
            [clojure.string :as st]
            [clojure.data.json :as json]
            [clojure.edn :as edn]))


(comment
  (edn/read-string (slurp "resources/test_data.edn"))
  (def api-key (st/replace (slurp "resources/config.txt") #"\n" ""))
  (let [url "https://nubela.co/proxycurl/api/v2/linkedin?url=https%3A%2F%2Fwww.linkedin.com%2Fin%2Fhgranthorner"
        data (client/get url
                    {:headers {"Authorization" (str "Bearer " api-key)}})]
    (u/transform-keys u/snake_case->kebab-case (json/read-json (:body data))))
  (comment))
