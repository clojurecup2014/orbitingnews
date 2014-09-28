(ns orbitingnews.core
  (:use compojure.core)
  (:use org.httpkit.server)
  (:import [java.io ByteArrayInputStream ByteArrayOutputStream])
  (:require [org.httpkit.client :as http]
            [org.httpkit.timer :as timer]
            [compojure.route :as route]
            [orbitingnews.config :as config]
            [orbitingnews.twitter :as twitter]
            [hiccup.core :as dom]
            [hiccup.page :as page]
            [clojure.core.async :as async :refer [<! >! chan go put! alts! take!]]
            [cognitect.transit :as transit])
  (:gen-class))

(defonce listeners
  (atom #{}))

(go (let [c (twitter/firehose)]
      (while true
        (let [status (<! c)
              out (ByteArrayOutputStream. 4096)
              writer (transit/writer out :json)]
          (transit/write writer {:msg (.getText status)})
          (let [msg (.toString out)]
            ; (prn (str "listeners " (count @listeners)))
            (doall (pmap #(send! % msg false) @listeners))))))) ; false => don't close after send

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


(defn tweets-page
  [tweets]
  [:html
   [:head
    [:title "Houston we have a problem"]
    (page/include-js "/js/main.js")]
   [:body
    [:h1 "Orbiting News"]
    [:h2 "Houston we have a problem"]
    [:img {:src "https://earthkam.ucsd.edu/images/iss-future.jpg" :width "20%"}]
    [:div {:id "tweet-list"} (for [tweet tweets]
                               [:p tweet])]]])

(defn root-handler [req]
  (let [tweets (map #(.getText %) (twitter/search "#clojurecup"))]
    (dom/html (tweets-page tweets))))

(defroutes app-routes
  (GET "/" [] root-handler)
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
