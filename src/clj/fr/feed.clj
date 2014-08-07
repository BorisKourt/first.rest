(ns clj.fr.feed
  (:require [clojure.data.xml :as xml]
            [clj-template.html5 :as <!]
            [clojure.string :as str]
            [hiccup.page :as hpage :refer [html5]]
            [clj-time.core :as t]
            [clj.fr.tags :as <-]))

(defn feed-entry [{:keys [title link description date connections]}]
  (<-/entry>
    (<!/title title)
    (<!/link {:href link :rel "alternate" :type "text/html"})
    (<-/updated> (str date))
    (<-/author>
      (<-/name> "Boris Kourtoukov")
      (<-/uri>  "http://boris.kourtoukov.com/"))
    (<-/id> link)
    (apply str (map #(<-/category-> {:term %}) connections))
    (<-/content> {:src link :type "text/html"})
    (<-/summary> {:type "html"}
                 (str "<![CDATA[ " description " ]]>"))))

(defn feed [posts kind path]
  (str
    (hpage/xml-declaration "utf-8")
    (<-/feed> {:xmlns "http://www.w3.org/2005/Atom"}
              (<!/title     {:type "text" :xml:lang "en"}
                        (str "(first (rest)) ; " kind))
              (<-/subtitle> {:type "html"}
                            "Talking about Clojure, ClojureScript, and programming in general")
              (<!/link      {:type "application/atom+xml"
                             :href (str "http://first.rest" path)
                             :rel "self"})
              (<-/updated>  (-> posts first :date str))
              (<-/icon>    "http://first.rest/images/appicon-72x72-precomposed.png")
              (<-/id>       "http://first.rest/")
              (<-/author>
                (<-/name>   "Boris Kourtoukov")
                (<-/uri>    "http://boris.kourtoukov.com/"))
              (<-/rights>   (str "copyright (c) " (t/year (t/today)) " first.rest and Boris Kourtoukov"))
              (apply str    (map feed-entry posts)))))
