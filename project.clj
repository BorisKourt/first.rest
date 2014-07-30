(defproject fr "0.5.3"
  :description "first.rest"
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
                 [clj-time "0.8.0"]
                 [org.clojure/data.xml "0.0.7"]
                 [optimus "0.15.0"]
                 [optimus-img-transform "0.2.0"]
                 [clj-jgit "0.7.6"]]
  :ring {:handler clj.fr.web/app}
  :aliases {"build-site" ["run" "-m" "clj.fr.web/export"]}
  :profiles {:dev {:plugins [[lein-ring "0.8.11"]
                             [lein-marginalia "0.7.1"]
                             [lein-ancient "0.6.0-SNAPSHOT"]]}
             :test {:dependencies [[midje "1.6.3"]]
                    :plugins [[lein-midje "3.1.3"]]}})
