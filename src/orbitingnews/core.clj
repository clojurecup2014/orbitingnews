(ns orbitingnews.core
  (:use compojure.core)
  (:use org.httpkit.server)
  (:require [org.httpkit.client :as http]
            [compojure.route :as route]
            [orbitingnews.config :as config])
  (:gen-class))

(defroutes app-routes
  (GET "/" [] "<html><body>Houston we have a problem<br><img src='https://earthkam.ucsd.edu/images/iss-future.jpg'></body></html>"))

(def ^:dynamic *server-port*
    (Integer/parseInt (config/env "SERVER_PORT")))

(defn -main
  "I don't do a whole lot."
  [& args]
  (println (str "Server up and running on http://127.0.0.1:" *server-port*))
  (run-server app-routes {:port *server-port*}))
