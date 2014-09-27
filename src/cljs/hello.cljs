(ns example.hello
  (:require [cognitect.transit :as t]
            [cljs.core.async :refer [<! >! chan]])
  (:require-macros [cljs.core.async.macros :refer [go]]))


(defn log [text]
  (.log js/console text))

(def r (t/reader :json))

(def tweets-channel (chan))

(def socket (js/WebSocket. "ws://localhost:8080/ws"))

(set! socket.onmessage (fn [e] (go (>! tweets-channel (:msg (t/read r e.data))))))
(set! socket.onerror (fn [e] (go (>! tweets-channel (:msg (t/read r e.data))))))

(.setTimeout js/window (fn []
                         (.close socket)) 3000)

(go (while true
      (log (<! tweets-channel))))
