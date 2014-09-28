(ns orbitingnews.core
  (:use compojure.core)
  (:use ring.util.response)
  (:use org.httpkit.server)
  (:import [java.io ByteArrayInputStream ByteArrayOutputStream])
  (:require [org.httpkit.client :as http]
            [org.httpkit.timer :as timer]
            [compojure.route :as route]
            [orbitingnews.config :as config]
            [orbitingnews.twitter :as twitter]
            [clojure.core.async :as async :refer [<! >! chan go put! alts! take! filter<]]
            [cognitect.transit :as transit])
  (:gen-class))

(defonce listeners
  (atom #{}))

(defn url-container [status]
  (.getURLEntities status))

(defn with-links [status]
  (not (empty? (url-container status))))

(defn fetch-link [container]
  (.getExpandedURL container))

(defn links [status]
  (map fetch-link (url-container status)))

(go (let [c (twitter/firehose)]
      (while true
        (let [status (<! (filter< with-links c))
              links (links status)
              send-links (fn [link]
                           (let [out (ByteArrayOutputStream. 4096)
                                 writer (transit/writer out :json)]
                             (transit/write writer {:msg link})
                             (let [msg (.toString out)]
                               (doall (pmap #(send! % msg false) @listeners)))))] ; false => don't close after send
          (doall (map send-links links))))))

(defn handler [req]
  (with-channel req channel              ; get the channel
    ;; communicate with client using method defined above
    (on-close channel (fn [status]
                        (swap! listeners disj channel)))

    (swap! listeners conj channel)

;       (loop [id 0]
;         (when (< id 10)
;           (timer/schedule-task (* id 200) ;; send a message every 200ms
;                                (let [out (ByteArrayOutputStream. 4096)
;                                      writer (transit/writer out :json)]
;                                  (transit/write writer {:msg (str "message from server #" id)})
;                                  (send! channel (.toString out) false))) ; false => don't close after send
;           (recur (inc id))))

    (on-receive channel (fn [data]       ; data received from client
                          ;; An optional param can pass to send!: close-after-send?
                          ;; When unspecified, `close-after-send?` defaults to true for HTTP channels
                          ;; and false for WebSocket.  (send! channel data close-after-send?)
                          (send! channel data))))) ; data is sent directly to the client

(defroutes app-routes
  (GET "/" [] (resource-response "index.html" {:root "public"}))
  (GET "/ws" [] handler)
  (route/resources "/")
  (route/not-found "Page not found"))

(def ^:dynamic *server-port*
    (Integer/parseInt (config/env "SERVER_PORT")))

(defn -main
  "I don't do a whole lot."
  [& args]
  (println (str "Server up and running on http://127.0.0.1:" *server-port*))
  (run-server app-routes {:port *server-port*}))
