(ns orbitingnews.twitter
  (:require [orbitingnews.config :as config])
  (:import [twitter4j TwitterFactory Query QueryResult]
           [twitter4j.auth AccessToken])
  (:gen-class))

(def ^:dynamic *access-token* (config/env "TWITTER_ACCESS_TOKEN"))
(def ^:dynamic *access-secret* (config/env "TWITTER_ACCESS_SECRET"))
(def ^:dynamic *consumer-key* (config/env "TWITTER_CONSUMER_KEY"))
(def ^:dynamic *consumer-secret* (config/env "TWITTER_CONSUMER_SECRET"))

(defn- oauth-authorized-twitter
  "Create a twitter client with OAuth authentication"
  []
  (let [access-token (AccessToken. *access-token* *access-secret*)
        twitter (.getInstance (TwitterFactory.))]
    (doto twitter
      (.setOAuthConsumer *consumer-key* *consumer-secret*)
      (.setOAuthAccessToken access-token))
    twitter))

(defn search
  "Returns tweets from search"
  [query-string]
  (let [query (Query. query-string)]
    (.setCount query 20)
    (-> (oauth-authorized-twitter)
        (.search query)
        (.getTweets))))
