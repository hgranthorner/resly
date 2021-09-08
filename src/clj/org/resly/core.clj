(ns org.resly.core
  (:require [clj-http.client :as client]
            [clojure.string :as st]
            [clojure.data.json :as json]
            [clojure.walk :as walk]
            [clojure.edn :as edn]))

(defn map-keys [f m]
  (->> (map (fn [[k v]] [(f k) v]) m)
       (into {})))

(defn snake_case->kebab-case [k]
  (let [words (st/split (name k) #"_")]
    (keyword (st/join "-" words))))

(comment
  (snake_case->kebab-case :volunteer_work)
  (comment))

(defn kebab-case->camelCase [k]
  (let [words (clojure.string/split (name k) #"-")]
    (->> (map clojure.string/capitalize (rest words))
         (apply str (first words))
         keyword)))

(defn transform-keys [t form]
  (walk/postwalk (fn [x] (if (map? x) (map-keys t x) x)) form))

(comment
  (edn/read-string (slurp "resources/test_data.edn"))
  (def api-key (st/replace (slurp "resources/config.txt") #"\n" ""))
  (let [url "https://nubela.co/proxycurl/api/v2/linkedin?url=https%3A%2F%2Fwww.linkedin.com%2Fin%2Fhgranthorner"
        data (client/get url
                    {:headers {"Authorization" (str "Bearer " api-key)}})]
    (transform-keys snake_case->kebab-case (json/read-json (:body data))))
  (comment))
