(ns orbitingnews.config
  (:require [clojure.string :as s]))

; copied from bkeepers/dotenv Ruby version
(def ^:const line-pattern
  #"\A(?:export\s+)?([\w\.]+)(?:\s*=\s*|:\s+?)('(?:\'|[^'])*'|\"(?:\"|[^\"])*\"|[^#\n]+)?(?:\s*\#.*)?\z")

(defn- extract-value
  "Remove value from wrapped quotes"
  [value]
  (-> value s/trim (s/replace #"\A(['\"])(.*)\1\z" "$2")))

(defn- parse-env
  "Parse variable line into key-value pair"
  [l]
  (let [[_ k v] (re-matches line-pattern l)]
    [k (extract-value v)]))

(defn load-env
  "Parses the dotenv file and returns a map"
  ([]
   (load-env ".env"))
  ([file]
   (with-open [reader (clojure.java.io/reader file)]
     (doall (->> (line-seq reader)
                 (remove s/blank?)
                 (map parse-env)
                 (into {}))))))

(def ^{:doc "A map of environment variables."}
  env
  (load-env))
