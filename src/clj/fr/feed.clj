(ns clj.fr.feed
  (:require [clojure.data.xml :as xml]
            [hiccup.page :as page]))

(defn- entry [post]
  [:entry 
   [:title (:title post)]
   [:updated (:date post)]
   [:author [:name "Boris Kourtoukov"]]
   [:link {:href (:link post)}]
   [:id (str "urn:first-rest:feed:post:" (clojure.string/lower-case 
                                           (clojure.string/replace (:title post) #" " "-")))]
   [:content {:type "html"} (:content post)]])

(defn atom-xml [posts kind path]
  (let [items (first (partition 10 10 {} posts))]
    (xml/emit-str
      (xml/sexp-as-element
        [:feed 
         [:id "urn:first-rest:feed"]
         [:updated (-> posts first :date)]
         [:title {:type "text"} (str "(First (Rest)) ; " kind)]
         [:link {:rel "self" :href (str "http://first.rest/" path)}]
         (map entry posts)]))))

(comment
  (ns clj.fr.feed
    (:require [clj-rss.core :as rss]))

  (defn rss-feed [posts kind]
    (let [items (first (partition 10 10 {} posts))
          rss-list (map #(select-keys % [:title :link :description]) items)]
      (rss/channel-xml  {:title "first.rest" :link "http://first.rest/" :description ""}
                       rss-list))))

