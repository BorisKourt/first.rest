(ns fr.clj.web
  (:require [optimus.assets :as assets]
            [optimus.export]
            [optimus.link :as link]
            [optimus.optimizations :as optimizations]
            [optimus.prime :as optimus]
            [optimus.strategies :refer  [serve-live-assets]]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clj-time.format :as tf]
            [clj-time.core :as t]
            [hiccup.core :refer [html]]
            [hiccup.page :refer  [html5]]
            [me.raynes.cegdown :as md]
            [stasis.core :as stasis]
            [fr.clj.highlight :refer  [highlight-code-blocks]]
            [fr.clj.post :refer [create-post]]))

;; ---
;; Helpers
;; ---
 
(defn seq-contains?  [coll target]  (some #(= target %) coll))

(defn monthf  [date]
  (tf/unparse  (tf/formatter "MMM") date))

(defn dayf  [date]
  (tf/unparse  (tf/formatter "dd") date))

(defn yearf  [date]
  (tf/unparse  (tf/formatter "yyyy") date))

;; ---
;; Core Template, wraps all other pages.
;; ---

(defn wrapper [request title page]
  (html5
    [:head
     [:meta  {:charset "utf-8"}]
     [:meta  {:name "viewport"
              :content "width=device-width, initial-scale=1.0"}]
     [:title (str "(first (rest)) " (when title (str "; " title)))]
     [:link  {:rel "stylesheet" :href  (link/file-path request "/styles/main.css") :type "text/css"}]
     [:link  {:rel "icon" :href  (link/file-path request "/favicon.ico") :type "image/x-icon"}]
     [:link
      {:type "text/css",
       :rel "stylesheet",
       :href
       "http://fonts.googleapis.com/css?family=Roboto:400,400italic,700,700italic"}]

     (map (fn [a & rest]
            [:link
             {:href (link/file-path request (str "/appicon-" a "x" a "-precomposed.png"))
              :sizes (str  a "x" a )
              :rel "apple-touch-icon-precomposed"}])
          [152 144 114 96 72])

     [:link {:href (link/file-path request (str "/appicon-precomposed.png")) 
             :rel "apple-touch-icon-precomposed"}]
    "<!--[if gte IE 9]>\n  <style type=\"text/css\">\n    .core, .endcap, .wraps {\n       filter: none;\n    }\n  </style>\n<![endif]-->" 
    [:script {:src "/js/modernizr.js" :type "text/javascript"}]]
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
         [:a {:href "/about.html"} "About"]
         [:a {:href "/longform.html"} "Longform"]
         [:a {:href "/shortform.html"} "Shortform"]
         [:a {:href "/connections.html"} "Connections"]]]
       [:nav.core__navigation
        [:a {:href "/about.html"} "About"]
        [:a {:href "/longform.html"} "Longform"]
        [:a {:href "/shortform.html"} "Shortform"]
        [:a {:href "/connections.html"} "Connections"]]]     
      [:section.wraps
       page]
      [:footer.endcap "&copy; First Rest &amp; Boris Kourtoukov " (t/year (t/today))]]))

;; ---
;; Connection helpers
;; ---

(defn make-connection  [connection]
  [:a  {:href  (str "/connections/" connection ".html")} connection])

(defn connect  [connections]
  (reduce #(conj %1 ", "  (make-connection %2))  
           (make-connection  (first connections))  
           (rest connections)))

;; ---
;; Single post template
;; ---

(defn single-item [request {:keys  [title connections date path content]}]
  (wrapper request title
           [:article.page 
            [:header
             [:h2.page__title title]
             (when connections 
               [:span.tags (connect connections)])
             " "
             (when date
               [:span.date [:time  {:datetime date}  (monthf date) " "  (dayf date) ", "  (yearf date)]])]
            [:section.page__content content]]))

;; ---
;; Archives templates & functionality
;; ---

(defn archive-post  [{:keys  [title date connections path]}]
  [:article.archive__post
   [:h1.archive__title--single  [:a  {:href path} title]]
   [:time  {:datetime date}
    [:span.month  (monthf date)] " "
    [:span.day  (dayf date)] ", "
    [:span.year  (yearf date)]] " "
   (when  (not-empty connections)
     [:span.links "connections: "  (connect connections)])])

(defn archive-group  [[year posts]]
  (let  [sorted-posts  (reverse  (sort-by :path posts))]
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
 (wrapper request title 
  (archive-like request posts title)))

(defn home 
  "Home is a type of archive"
  [request longform shortform] 
  (wrapper request "core"
    (html [:header.home--intro "Welcome! These pages will speak to functional
                               programming, Clojure, ClojureScript, game and web development,
                               as well as anything that can play a role in tying these together. "
           [:a {:href "/about.html" :alt "about"} "More information &raquo;"]]
          [:section.main (archive-like request longform "Longform")]
          [:aside.right  (archive-like request shortform "Shortform")])))

;; ---
;; Connection templates & functionality
;; ---

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
    (wrapper request "connections"
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

;; ---
;; Post and partial pages
;; ---

(defn layout-post  [post]
  [(:path post)  (fn  [req]  (single-item req post))])

(defn layout-posts  [posts]
  (let  [post-layouts  (map layout-post posts)]
    (into  {} post-layouts)))

(defn partial-pages  [pages]
  (zipmap (keys pages)
          (map #(fn  [req]  (wrapper req false %))  (vals pages))))

(defn create-dynamic-pages  [long-posts short-posts posts]
  {"/index.html"       (fn  [req]  (home req long-posts short-posts))
   "/longform.html"  (fn  [req]  (archive req long-posts "Longform"))
   "/shortform.html"  (fn  [req]  (archive req short-posts "Shortform"))
   "/connections.html" (fn  [req]  (connections req posts))
  })

(defn gen-posts-from-type [kind]
  (let [location (str "resources/" kind)]
    (map #(create-post kind %) (stasis/slurp-directory location #"\.md$"))))

(defn get-raw-pages  []
  (let [long-posts (gen-posts-from-type "longform")
        short-posts (gen-posts-from-type "shortform")
        posts      (concat long-posts short-posts)]
    (stasis/merge-page-sources
      {:partials (partial-pages  (stasis/slurp-directory "resources/partials" #".*\.html$"))
       :dynamic  (create-dynamic-pages long-posts short-posts posts)
       :longform     (layout-posts long-posts)
       :shortform     (layout-posts short-posts)
       :connections     (create-connection-pages posts)})))

(defn prepare-page  [page req]
  (->  (if  (string? page) page  (page req)) 
        highlight-code-blocks))

(defn prepare-pages  [pages]
  (zipmap (keys pages)  
          (map #(partial prepare-page %) (vals pages))))

(defn get-pages  []
  (prepare-pages  (get-raw-pages)))

;; ---
;; Assets
;; ---

(defn get-assets  []
  (assets/load-assets "public"  [#".*"]))

;; ---
;; Ring App
;; ---

(def app (optimus/wrap 
           (stasis/serve-pages get-pages)  
           get-assets 
           optimizations/all 
           serve-live-assets))

;; ---
;; Static Export Setup
;; ---

(def export-dir "html")

(defn export  []
  (let  [assets  (optimizations/all  (get-assets)  {})]
    (stasis/empty-directory! export-dir)
    (optimus.export/save-assets assets export-dir)
    (stasis/export-pages  (get-pages) export-dir  {:optimus-assets assets})))
