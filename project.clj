(defproject fr "0.5.4"
  :description "first.rest"
  :url "http://first.rest"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0-alpha5"]
                 [stasis "2.2.2"]
                 [ring "1.3.2"]
                 [hiccup "1.0.5"]
                 [me.raynes/cegdown "0.1.1"]
                 [enlive "1.1.5"]
                 [clygments "0.1.1"]
                 [clj-time "0.9.0"]
                 [org.clojure/data.xml "0.0.8"]
                 [optimus "0.17.0"]
                 [optimus-img-transform "0.2.0"]
                 [pathetic "0.5.1"]
                 [clj-template "1.0.1"]
                 [crouton "0.1.2"]
                 [hickory "0.5.4"]
                 [org.clojure/core.async "0.1.319.0-6b1aca-alpha"]
                 [clj-jgit "0.8.3"]]
  :plugins [[lein-ancient "0.6.2"]
            [lein-marginalia "0.8.0"]
            [lein-gorilla "0.3.5-SNAPSHOT"]]
  :aliases {"build-site" ["run"  "-m" "clj.fr.web/export"]}
  :profiles {:server   {:plugins [[lein-ring "0.9.1"]]
                        :ring {:handler clj.fr.web/server}}
             :test {:dependencies [[midje "1.7.0-SNAPSHOT"]]
                    :plugins [[lein-midje "3.1.4-SNAPSHOT"]]}})