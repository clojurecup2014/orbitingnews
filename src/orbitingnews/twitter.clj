(ns orbitingnews.twitter
  (:require [orbitingnews.config :as config]
            [clojure.core.async :as async :refer [<! >! chan go put! alts! take!]])
  (:import [twitter4j TwitterFactory TwitterStreamFactory StatusListener UserStreamListener Query QueryResult FilterQuery]
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

(defn- oauth-stream-authorized-twitter
  "Create a twitter client with OAuth authentication"
  []
  (let [access-token (AccessToken. *access-token* *access-secret*)
        twitter (.getInstance (TwitterStreamFactory.))]
    (doto twitter
      (.setOAuthConsumer *consumer-key* *consumer-secret*)
      (.setOAuthAccessToken access-token))
    twitter))

(defn fetch-link [status]
  (.getExpandedURL status))

(defn handle-status [status channel]
  (go (>! channel status)))
  ; (comment
  ;  (let [urls (map fetch-link (vec (.getURLEntities status)))]
  ;    (when-not (empty? urls) (prn urls)))))

(defn status-listener [channel]
  "Implementation of twitter4j's StatusListener interface"
  (proxy [StatusListener] []
    (onStatus [^twitter4j.Status status] (handle-status status channel))
    (onException [^java.lang.Exception e] (.printStackTrace e))
    (onDeletionNotice [^twitter4j.StatusDeletionNotice statusDeletionNotice] ())
    (onScrubGeo [userId upToStatusId] ())
    (onTrackLimitationNotice [numberOfLimitedStatuses] ())))

(defn user-listener []
  "Implementation of twitter4j's StatusListener interface"
  (proxy [UserStreamListener] []
    (onStatus [^twitter4j.Status status] (handle-status status))
    (onException [^java.lang.Exception e] (.printStackTrace e))
    (onDeletionNotice [^twitter4j.StatusDeletionNotice statusDeletionNotice] ())
    (onScrubGeo [userId upToStatusId] ())
    (onTrackLimitationNotice [numberOfLimitedStatuses] ())
    (onBlock [source blockedUser] ())
    (onDirectMessage [dm] ())
    (onFavorite [source target favorited-status] ())
    (onFollow [source followed] ())
    (onFriendList [friends] ())
    (onUnblock [source unblocked] ())
    (onUnfavorite [source target status] ())
    (onUserListCreation [owner user-list] ())
    (onUserListDeletion [listOwner ll] ())
    (onUserListMemberAddition [addedMember listOwner ll] ())
    (onUserListMemberDeletion [deletedMember listOwner ll] ())
    (onUserListSubscription [subscriber listOwner ll] ())
    (onUserListUnsubscription [subscriber listOwner ll] ())
    (onUserListUpdate [listOwner UserList ll] ())
    (onUserProfileUpdate [User updatedUser] ())))

(defn do-sample-stream []
  (let [stream (oauth-stream-authorized-twitter)]
    (.addListener stream (status-listener))
    (.sample stream)))

(defn do-filter-stream []
  ; We want tweets with the word tweet in them
  (let [filter-query (FilterQuery. 0 (long-array []) (into-array String ["#MissBrasil"]))
        stream (oauth-stream-authorized-twitter)
        c (chan)]
    (.addListener stream (status-listener c))
    (.filter stream filter-query)
    c))

(defn firehose []
  ; We want tweets with the word tweet in them
  (let [stream (oauth-stream-authorized-twitter)
        c (chan (async/sliding-buffer 50))]
    (.addListener stream (status-listener c))
    (.sample stream)
    c))

(defn user-stream []
  (let [stream (oauth-stream-authorized-twitter)]
    (.addListener stream (user-listener))
    (.user stream)))

(defn search
  "Returns tweets from search"
  [query-string]
  (let [query (Query. query-string)]
    (.setCount query 20)
    (-> (oauth-authorized-twitter)
        (.search query)
        (.getTweets))))
