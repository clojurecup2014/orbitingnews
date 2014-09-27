(ns orbitingnews.core
  (:use compojure.core)
  (:use org.httpkit.server)
  (:require [org.httpkit.client :as http]
            [compojure.route :as route]
            [orbitingnews.config :as config]
            [orbitingnews.twitter :as twitter]
            [hiccup.core :as dom])
  (:gen-class))

(defn tweets-page
  [tweets]
  [:html
    [:head
      [:title "Houston we have a problem"]]
    [:body
      [:h1 "Orbiting News"]
      [:h2 "Houston we have a problem"]
      [:img {:src "https://earthkam.ucsd.edu/images/iss-future.jpg"}]
      (for [tweet tweets]
        [:p tweet])]])

(defn root-handler [req]
  (let [tweets (map #(str "<li>" (.getText %) "</li>") (twitter/search "#clojurecup"))]
    (dom/html (tweets-page tweets))))

(defroutes app-routes
  (GET "/" [] root-handler))

(def ^:dynamic *server-port*
    (Integer/parseInt (config/env "SERVER_PORT")))

(defn -main
  "I don't do a whole lot."
  [& args]
  (println (str "Server up and running on http://127.0.0.1:" *server-port*))
  (run-server app-routes {:port *server-port*}))
