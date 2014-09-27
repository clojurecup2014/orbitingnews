(ns orbitingnews.twitter
  (:require [orbitingnews.config :as config])
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

(defn status-listener []
  "Implementation of twitter4j's StatusListener interface"
  (proxy [StatusListener] []
    (onStatus [^twitter4j.Status status] (println (.getText status)))
    (onException [^java.lang.Exception e] (.printStackTrace e))
    (onDeletionNotice [^twitter4j.StatusDeletionNotice statusDeletionNotice] ())
    (onScrubGeo [userId upToStatusId] ())
    (onTrackLimitationNotice [numberOfLimitedStatuses] ())))

(defn user-listener []
  "Implementation of twitter4j's StatusListener interface"
  (proxy [UserStreamListener] []
    (onStatus [^twitter4j.Status status] (println (.getText status)))
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
  (let [filter-query (FilterQuery. 0 (long-array []) (into-array String ["#ChristianChavezNoRaulGil"]))
        stream (oauth-stream-authorized-twitter)]
    (.addListener stream (status-listener))
    (.filter stream filter-query)))

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
