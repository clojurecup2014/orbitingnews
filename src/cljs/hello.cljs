(ns example.hello
  (:require [cognitect.transit :as t]
            [cljs.core.async :refer [<! >! chan]]
            [domina :as dom]
            [hiccups.runtime :as hiccupsrt])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [hiccups.core :as hiccups]))


(defn log [text]
  (.log js/console text))

(def r (t/reader :json))

(def tweets-channel (chan))

(def socket (js/WebSocket. (str "ws://" (.-hostname (.-location js/document)) ":" (.-port (.-location js/document)) "/ws")))

(set! socket.onmessage (fn [e] (go (>! tweets-channel (:msg (t/read r e.data))))))
(set! socket.onerror (fn [e] (go (>! tweets-channel (:msg (t/read r e.data))))))

(defn prepend-tweet [tweet]
  (dom/prepend! (dom/by-id "tweet-list") (hiccups/html [:p tweet])))

(go (while true
      (prepend-tweet (<! tweets-channel))))
