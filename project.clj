(defproject orbitingnews "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0-alpha2"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [http-kit "2.1.16"]
                 [cheshire "5.3.1"]
                 [compojure "1.1.8"]
                 [ring/ring-defaults "0.1.1"]]
  :main orbitingnews.core
  :aot [orbitingnews.core])