(defproject fr "0.5.1"
  :description "First Rest"
  :url "http://first.rest"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [stasis "2.1.1"]
                 [ring "1.3.0"]
                 [hiccup "1.0.5"]
                 [me.raynes/cegdown "0.1.1"]
                 [enlive "1.1.5"]
                 [clygments "0.1.1"]
                 [clj-time "0.7.0"]
                 [org.clojure/data.xml "0.0.7"]
                 [optimus "0.15.0"]]
  :ring {:handler fr.web/app}
  :aliases {"build-site" ["run" "-m" "fr.web/export"]}
  :profiles {:dev {:plugins [[lein-ring "0.8.11"]]}
             :test {:dependencies [[midje "1.6.3"]]
                    :plugins [[lein-midje "3.1.3"]]}})
