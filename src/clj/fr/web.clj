(ns clj.fr.web
  (:require [optimus.assets :as assets]
            [optimus.export]
            [optimus.link :as link]
            [optimus.optimizations :as optimizations]
            [optimus.prime :as optimus]
            [optimus.strategies :refer  [serve-live-assets serve-frozen-assets]]
            [optimus-img-transform.core :refer [transform-images]]
            
            [clojure.java.io :as io]
            [clojure.string :as str]
            
            [clj-time.format :as tf]
            [clj-time.core :as t]
            
            [hiccup.core :refer [html]]
            [hiccup.page :as hpage :refer [html5]]
            
            [me.raynes.cegdown :as md]
            
            [ring.middleware.content-type :refer  [wrap-content-type]]
            
            [stasis.core :as stasis]
            
            [net.cgrand.enlive-html :as enlive :refer [deftemplate defsnippet]]
            
            [clj.fr.highlight :refer  [highlight-code-blocks]]
            [clj.fr.post :refer [create-post]]
            [clj.fr.git :refer :all]
            [clj.fr.picture :as picture :refer [convert-to-srcset]]
            ;[clj.fr.feed :refer [feed]]
            [clj.fr.text :refer [humans robots]]))


;; Helpers

(defn seq-contains?  [coll target]  (some #(= target %) coll))

(defn monthf  [date]
  (tf/unparse  (tf/formatter "MMM") date))

(defn dayf  [date]
  (tf/unparse  (tf/formatter "dd") date))

(defn yearf  [date]
  (tf/unparse  (tf/formatter "yyyy") date))


;; core template, wraps all other pages.

(defn wrapper
  "Too messy!"
  [request title link page]
  (html
    (:html5 hpage/doctype)
    [:html {:xmlns:og "http://opengraphprotocol.org/schema/"}
    [:head
     [:meta  {:charset "utf-8"}]

     ;; Responsive Device Base
     [:meta  {:name "viewport"
              :content "width=device-width, initial-scale=1.0"}]

     ;; Description
     [:meta {:name "title"
             :content (str "(first (rest)) " (when title (str "; " title)))}]
     [:meta {:name "description"
             :content "(first (rest)) ; Is a website dedicated to articles on Clojure, ClojureScript, and programming in general."}]
     [:meta {:name "keywords"
             :content "Clojure, ClojureScript, Code, Programming, Tutorial, Example, Lisp"}]

     ;; Humans and Robots
     [:link {:rel "author"
             :href "/humans.txt"}]
     [:link {:rel "robots"
             :href "/robots.txt"}]

     ;; Twitter + OG Meta
     [:meta {:property "twitter:card"
             :content "summary"}]
     [:meta {:property "twitter:site"
             :content "@boriskourt"}]
     [:meta {:property "twitter:creator"
             :content "@boriskourt"}]
     [:meta {:property "og:title"
             :content title}]
     [:meta {:property "og:description"
             :content "Content from (first (rest))"}]
     [:meta {:property "og:image"
             :content "http://first.rest/images/appicon-152x152-precomposed.png"}]
     [:meta {:property "og:url"
             :content link}]

     ;; End Twitter + OG Meta
     [:title (str "(first (rest)) " (when title (str "; " title)))]
     [:link  {:rel "stylesheet" :href  (link/file-path request "/styles/main.css") :type "text/css"}]

     ;; Atom Feeds Per Category
     [:link
      {:title "Full site Atom feed"
       :href "/feed.atom"
       :type "application/atom+xml"
       :rel "alternate"}]
     [:link
      {:title "Only longform Atom feed"
       :href "/longform.atom"
       :type "application/atom+xml"
       :rel "alternate"}]
     [:link
      {:title "Only shortform Atom feed"
       :href "/shortform.atom"
       :type "application/atom+xml"
       :rel "alternate"}]

     ;; Webfont
     [:link
      {:type "text/css",
       :rel "stylesheet",
       :href
       "http://fonts.googleapis.com/css?family=Roboto:400,400italic,700,700italic"}]

     ;; IE9 Tweak
     "<!--[if gte IE 9]>\n  <style type=\"text/css\">\n    .core, .endcap, .wraps {\n       filter: none;\n    }\n  </style>\n<![endif]-->"

     ;; Modernity
     [:script {:src "/js/modernizr.js" :type "text/javascript"}]
     [:script
      {:type "text/javascript"}
      "\n Modernizr.load({\n test: Modernizr.srcset,\n nope: '/js/srcset.js'\n },{\n test: Modernizr.vhunit,\n nope: '/js/viewport.js'\n });\n"]

     ;; Application Icons
     (map (fn [a & rest]
            [:link
             {:href (link/file-path request (str "/images/appicon-" a "x" a "-precomposed.png"))
              :sizes (str  a "x" a )
              :rel "apple-touch-icon-precomposed"}])
          [152 144 114 96 72])

     [:link {:href (link/file-path request (str "/images/appicon-precomposed.png"))
             :rel "apple-touch-icon-precomposed"}]
     [:link  {:rel "icon" :href  (link/file-path request "/images/favicon.ico") :type "image/x-icon"}]]

    ;; Begin Body
    [:body
     [:header.core
      [:h1.logo
       [:a {:href "/"}
        [:span.pa "("]
        [:span.wa "first"] " "
        [:span.pb "("]
        [:span.wb "rest"]
        [:span.pb ")"]
        [:span.pa ")"]]]
      [:nav.core__navigation--dropdown
       [:a "Menu"]
       [:section.dropdown__content
        [:a {:href "/context.html"} "Context"]
        [:a {:href "/longform.html"} "Longform"]
        [:a {:href "/shortform.html"} "Shortform"]
        [:a {:href "/connections.html"} "Connections"]]]
      [:nav.core__navigation
       [:a {:href "/context.html"} "Context"]
       [:a {:href "/longform.html"} "Longform"]
       [:a {:href "/shortform.html"} "Shortform"]
       [:a {:href "/connections.html"} "Connections"]]]
     [:section.wraps
      page]
     [:footer.endcap
      [:a {:href "/feed.atom" :title "Atom Feed"} "feed"]
      " &mdash; "
      [:a {:href "/humans.txt" :target "_blank" :title "Humans.txt"} "humans"]
      " &mdash; "
      "&copy; first.rest &amp; Boris Kourtoukov "
      (t/year (t/today))
      " &mdash; "
      [:a {:target "_blank" :title "Documentation coming soon"} "doc"]
      " &mdash; "
      [:a {:href "https://github.com/BorisKourt/first.rest" :target "_blank" :title "Repository"} "source"]]]]))


;; Connection helpers

(defn make-connection  [connection]
  [:a  {:href  (str "/connections/" connection ".html")} connection])

(defn connect  [connections]
  (reduce #(conj %1 ", "  (make-connection %2))
          (make-connection  (first connections))
          (rest connections)))


;; Single post template

(defn single-item [request {:keys  [title connections date path link content commit]} & kind]
  (wrapper request title link
           [:article.page
            [:header
             [:h2.page__title title]
             [:p.page__meta
             (when connections
               [:span.tags (connect connections)])
             " "
             (when date
               [:span.date [:time  {:datetime date}  (monthf date) " "  (dayf date) ", "  (yearf date)]])]]
            [:section.page__content content]
            [:footer.page__data
            (when commit
             [:span "Last edited at commit: "
              [:a {:href (str "https://github.com/BorisKourt/first.rest/commit/" commit)
                   :target "_blank"
                   :title "View changes on GitHub"} commit]])]]))


;; Archives: templates & functionality

(defn archive-post  [{:keys  [title date connections path description]}]
  [:article.archive__post
   [:h1.archive__title--single  [:a  {:href path} title]]
   [:p.archive__meta
    [:time  {:datetime date}
     [:span.month  (monthf date)] " "
     [:span.day  (dayf date)] ", "
     [:span.year  (yearf date)]] " "
    (when  (not-empty connections)
      [:span.links "connections: "  (connect connections)])]])

(defn archive-group  [[year posts]]
  (let  [sorted-posts  (reverse  (sort-by :date posts))]
    (cons
      [:h2.archive__title--date year]
      (map archive-post sorted-posts))))

(defn archive-like  [request posts title]
  (let  [post-groups  (->> posts  (group-by #(t/year  (:date %)))  (sort-by first) reverse)]
    [:section.archive
     [:header.archive__header
      [:h1.archive__title title]]
     [:article.archive__content
      (map archive-group post-groups)]]))

(defn archive [request posts title]
  (wrapper request title "http://first.rest/"
           (archive-like request posts title)))

(defn home
  "Home is a type of archive"
  [request longform shortform]
  (wrapper request "Core" "http://first.rest/"
           (html [:header.home--intro "Welcome! These pages will speak to functional
                                      programming, Clojure, ClojureScript, game and web development,
                                      as well as anything that can play a role in tying these together. "
                  [:a {:href "/context.html" :title "The Context"} "More information &raquo;"]]
                 [:section.main (archive-like request longform "Longform")]
                 [:aside.right  (archive-like request shortform "Shortform")])))


;; Connection templates & functionality

(defn connection  [request posts connection]
  (archive request posts connection))

(defn connection-post  [{:keys  [path title]}]
  [:h3.archive__title--single  [:a  {:href path} title]])

(defn connection-entry  [connection posts]
  (let  [sorted (reverse  (sort-by :date posts))
         filtered (filter #(seq-contains? (% :connections) connection) sorted)]
    (cons
      [:h2.archive__title--single (make-connection connection)]
      (html [:nav.archive__posts
             (map connection-post filtered)]))))

(defn connections  [request posts]
  (let  [unique-connections  (->> posts  (map :connections) flatten distinct sort)]
    (wrapper request "Connections" "http://first.rest/"
             [:section.archive
              [:header.archive__header
               [:h1.archive__title "Connections"]]
              [:article.archive__content
               (map #(connection-entry % posts) unique-connections)]])))

(defn layout-connection-page  [connections posts]
  [(str "/connections/" connections ".html")  (fn  [req]  (connection req posts connections))])

(defn get-unique-connections  [posts]
  (->> posts  (map :connections) flatten distinct sort))

(defn connection-posts  [connection posts]
  (filter #((-> % :connections set) connection) posts))

(defn group-connections-posts  [posts]
  (let  [unique-connections  (get-unique-connections posts)]
    (reduce #(assoc %1 %2  (connection-posts %2 posts))  {} unique-connections)))

(defn create-connection-pages  [posts]
  (let  [connections-posts  (group-connections-posts posts)
         connections-layouts  (map #(apply layout-connection-page %) connections-posts)]
    (into  {} connections-layouts)))


;; Post and partial pages


(defn layout-post  [post kind]
  [(:path post)  (fn  [req]  (single-item req post kind))])

(defn layout-posts  [posts kind]
  (let  [post-layouts  (map #(layout-post % kind) posts)]
    (into  {} post-layouts)))

(defn partial-pages  [pages]
  (zipmap (keys pages)
          (map #(fn  [req]  (wrapper req false "http://first.rest/" %))  (vals pages))))

(defn create-dynamic-pages  [long-posts short-posts posts]
  {"/index.html"       (fn  [req]  (home req long-posts short-posts))
   "/longform.html"  (fn  [req]  (archive req long-posts "Longform"))
   "/shortform.html"  (fn  [req]  (archive req short-posts "Shortform"))
   "/connections.html" (fn  [req]  (connections req posts))})

;(defn create-feed-pages [{:keys [long-posts short-posts all-posts] :as post-map}]
;  {"/feed.atom"      (fn [req] (feed all-posts "All content" "/feed.atom"))
;   "/longform.atom"  (fn [req] (feed long-posts "Longform feed" "/longform.atom"))
;   "/shortform.atom" (fn [req] (feed short-posts "Shortform feed" "/shortform.atom"))})

(defn create-hrobots-pages [{:keys [long-posts short-posts all-posts] :as post-map}]
  {"/robots.txt" (fn [req] (robots post-map))
   "/humans.txt" (fn [req] (humans post-map))})

(defn setup-html-pages  [{:keys [long-posts short-posts all-posts]}]
  (stasis/merge-page-sources
    {:partials     (partial-pages  (stasis/slurp-directory "resources/partials" #".*\.html$"))
     :dynamic      (create-dynamic-pages long-posts short-posts all-posts)
     :longform     (layout-posts long-posts :longform)
     :shortform    (layout-posts short-posts :shortform)
     :connections  (create-connection-pages all-posts)}))

(defn setup-data-pages  [{:keys [long-posts short-posts all-posts] :as post-map}]
  (stasis/merge-page-sources
    {:hrobots (create-hrobots-pages post-map)
     ;:feeds   (create-feed-pages post-map)
     ;:sitemap (create-sitemap all-posts)
     }))

(defn prepare-html-page  [page req]
  (->  (if  (string? page) page  (page req))
      highlight-code-blocks
      (convert-to-srcset req)))

(defn prepare-html-pages  [pages]
  (zipmap (keys pages)
          (map #(partial prepare-html-page %) (vals pages))))

(defn gen-posts-from-type [kind]
  (let [location (str "resources/" (name kind))]
    (map #(create-post kind %) (stasis/slurp-directory location #"\.md$"))))

(defn resort-posts [posts]
  (->> posts
       (sort-by :date)
       reverse))

(defn get-content-pages []
  (let [long-posts  (resort-posts (gen-posts-from-type "longform"))
        short-posts (resort-posts (gen-posts-from-type "shortform"))
        posts       (resort-posts (concat long-posts short-posts))]
    {:long-posts long-posts :short-posts short-posts :all-posts posts}))

(defn get-pages  []
  (let [content-pages (get-content-pages)]
    (stasis/merge-page-sources
      {:html (prepare-html-pages (setup-html-pages content-pages))
       :data (setup-data-pages content-pages)})))


;; Assets

(comment defn get-assets [] ;; 4
  (concat ;; 5
   (assets/load-bundle "public" ;; 6
                       "styles.css" ;; 7
                       ["/styles/reset.css" ;; 8
                        "/styles/main.css"]) ;; 9
   (assets/load-bundles "public" ;; 10
                        {"lib.js" ["/scripts/ext/angular.js"
                                   #"/scripts/ext/.+\.js$"] ;; 11
                         "app.js" ["/scripts/controllers.js"
                                   "/scripts/directives.js"]})
   (assets/load-assets "public" ;; 12
                       ["/images/logo.png"
                        "/images/photo.jpg"])
   [{:path "/init.js" ;; 13
     :contents (str "var contextPath = " (:context-path env))
     :bundle "app.js"}]))

(defn get-assets  []
  (assets/load-assets "public"  [#".*"]))

(defn optimize
  "Have not been able to map over transform images here."
  [assets options]
  (-> assets
      (transform-images {:regexp #"/post-assets/.*\.jpg"
                         :quality 0.7
                         :width 1020
                         :prefix "1020-"
                         :progressive true})
      (transform-images {:regexp #"/post-assets/.*\.jpg"
                         :quality 0.7
                         :width 860
                         :prefix "860-"
                         :progressive true})
      (transform-images {:regexp #"/post-assets/.*\.jpg"
                         :quality 0.7
                         :width 600
                         :prefix "600-"
                         :progressive true})
      (transform-images {:regexp #"/post-assets/.*\.jpg"
                         :quality 0.7
                         :width 400
                         :prefix "400-"
                         :progressive true})
      (transform-images {:regexp #"/post-assets/.*\.jpg"
                         :quality 0.7
                         :width 200
                         :prefix "200-"
                         :progressive true})
      (transform-images {:regexp #"/post-assets/.*\.jpg"
                         :quality 0.6
                         :width 904
                         :progressive true})
      (optimizations/all options)))


;; Ring App

(defn server [req]
  (-> (stasis/serve-pages get-pages)
      (optimus/wrap
        get-assets
        optimize
        serve-frozen-assets)
      wrap-content-type))


;; Static Export Setup

(def export-dir "html")

(defn export  []
  (let  [assets  (optimize  (get-assets)  {})]
    (println "Assets Loaded")
    (stasis/empty-directory! export-dir)
    (println "Directory Emptied")
    (optimus.export/save-assets assets export-dir)
    (println "Assets Exported")
    (stasis/export-pages  (get-pages) export-dir  {:optimus-assets assets})
    (println "Pages Exported")))
