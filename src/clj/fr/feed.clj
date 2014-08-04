(ns clj.fr.feed
  (:require [clj-rss.core :as rss]))

(defn rss-feed [posts kind]
  (let [items (first (partion 10 10 {} posts))
        rss-list (map #(select-keys % [:title :path :description] items))])
  (rss/channel  {:title "first.rest" :link "http://first.rest/" :description ""}
                              rss-list))

