(ns clj.fr.tags
  (:refer-clojure :rename  {name clj-name map clj-map meta clj-meta time clj-time})
  (:require clj-template.core))

(clj-template.core/assoc-to-fn ["feed"
                                "updated"
                                "id"
                                "author"
                                "name"
                                "rights"
                                "entry"
                                "content"
                                "subtitle"
                                "uri"
                                "icon"
                                "summary"
                                ])
(clj-template.core/assoc-to-fn-unbalanced ["category"])

;; Atom feed specific XML tags.
(def feed>      feed)
(def updated>   updated)
(def id>        id)
(def author>    author)
(def name>      name)
(def rights>    rights)
(def entry>     entry)
(def content>   content)
(def subtitle>  subtitle)
(def uri>       uri)
(def category->  category-)
(def icon>      icon)
(def summary>   summary)

;; Sitemap XML tags.

