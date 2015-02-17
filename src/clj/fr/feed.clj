(ns clj.fr.feed)
;  (:require [clojure.data.xml :as xml]
;            [clj-template.html5 :as dn] ;; default nodes
;            [clojure.string :as str]
;            [hiccup.page :as hpage :refer [html5]]
;            [clj-time.core :as t]
;            [clj.fr.tags :as an]))      ;; added nodes
;
;(defn feed-entry [{:keys [title link description date connections]}]
;  (an/entryn
;    (dn/title title)
;    (dn/link {:href link :rel "alternate" :type "text/html"})
;    (an/updatedn (str date))
;    (an/authorn
;      (an/namen "Boris Kourtoukov")
;      (an/urin  "http://boris.kourtoukov.com/"))
;    (an/idn link)
;    (apply str (map #(an/categoryn- {:term %}) connections))
;    (an/contentn {:src link :type "text/html"})
;    (an/summaryn {:type "html"}
;                 (str "<![CDATA[ "
;                      description
;                      (dn/a {:href link
;                             :title "the rest, possibily"} "Full Text &raquo;")
;                      (dn/br-)
;                      "-"
;                      " ]]>"))))
;
;(defn feed [posts kind path]
;  (str
;    (hpage/xml-declaration "utf-8")
;    (an/feedn {:xmlns "http://www.w3.org/2005/Atom"}
;              (dn/title     {:type "text" :xml:lang "en"}
;                        (str "(first (rest)) ; " kind))
;              (an/subtitlen {:type "html"}
;                            "Talking about Clojure, ClojureScript, and programming in general")
;              (dn/link      {:type "application/atom+xml"
;                             :href (str "http://first.rest" path)
;                             :rel "self"})
;              (dn/link      {:type "text/html"
;                             :href "http://first.rest/"
;                             :rel "alternate"})
;              (an/updatedn  (-> posts first :date str))
;              (an/iconn    "http://first.rest/images/appicon-72x72-precomposed.png")
;              (an/idn       "http://first.rest/")
;              (an/authorn
;                (an/namen   "Boris Kourtoukov")
;                (an/urin    "http://boris.kourtoukov.com/"))
;              (an/rightsn   (str "copyright (c) " (t/year (t/today)) " first.rest and Boris Kourtoukov"))
;              (apply str    (map feed-entry posts)))))