(ns org.resly.utils
  (:require
    [clojure.walk :as walk]
    [clojure.string :as st]))


(defn map-keys [f m]
  (->> (map (fn [[k v]] [(f k) v]) m)
       (into {})))

(defn snake_case->kebab-case [k]
  (let [words (st/split (name k) #"_")]
    (keyword (st/join "-" words))))

(comment
  (snake_case->kebab-case :volunteer_work)
  (comment))

(defn transform-keys [t form]
  (walk/postwalk (fn [x] (if (map? x) (map-keys t x) x)) form))
