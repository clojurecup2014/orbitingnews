(ns orbitingnews.core
  (:use compojure.core)
  (:use org.httpkit.server)
  (:require [org.httpkit.client :as http]
            [compojure.route :as route]))

(defroutes app-routes
  (GET "/" [] "<html><body>Houston we have a problem<br><img src='https://earthkam.ucsd.edu/images/iss-future.jpg'></body></html>"))

(defn -main
  "I don't do a whole lot."
  [& args]
  (prn "Server up and running on http://127.0.0.1:8080")
  (run-server app-routes {:port 8080}))
