(ns example.hello
  (:require [cognitect.transit :as t]))

(defn log [text]
  (.log js/console text))

(def r (t/reader :json))

(def socket (js/WebSocket. "ws://localhost:8080/ws"))

(set! socket.onmessage (fn [e] (log (:msg (t/read r e.data)))))
(set! socket.onerror (fn [e] (log (:msg (t/read r e.data)))))

(.setTimeout js/window (fn []
                         (.close socket)) 3000)
