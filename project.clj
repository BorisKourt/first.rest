(defproject fr "0.5.3"
  :description "first.rest"
  :url "http://first.rest"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0-alpha1"]
                 [stasis "2.2.0" :exclusions [org.clojure/clojure]]
                 [ring "1.3.0"]
                 [hiccup "1.0.5"]
                 [me.raynes/cegdown "0.1.1"]
                 [enlive "1.1.5"]
                 [clygments "0.1.1"]
                 [clj-time "0.8.0"]
                 [org.clojure/data.xml "0.0.7"]
                 [optimus "0.15.0"]
                 [optimus-img-transform "0.2.0"]
                 [pathetic "0.5.1"]
                 [clj-template "1.0.0"]
                 [crouton "0.1.2"]
                 [hickory "0.5.3"]
                 [org.clojure/core.async "0.1.319.0-6b1aca-alpha"]
                 [clj-jgit "0.7.6"]]
  :plugins [[lein-ancient "0.6.0-SNAPSHOT"]]
  :aliases {"build-site" ["with-profile" "build" "run"  "-m" "clj.fr.web/export"]}
  :profiles {:build {:plugins [[lein-marginalia "0.7.1"]]}
             :server   {:plugins [[lein-ring "0.8.11"]]
                        :ring {:handler clj.fr.web/server}}
             :test {:dependencies [[midje "1.6.3"]]
                    :plugins [[lein-midje "3.1.3"]]}})
