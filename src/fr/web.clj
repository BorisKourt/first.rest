(ns fr.web
  (:require [optimus.assets :as assets]
            [optimus.export]
            [optimus.link :as link]
            [optimus.optimizations :as optimizations]
            [optimus.prime :as optimus]
            [optimus.strategies :refer  [serve-live-assets]]
            [fr.highlight :refer  [highlight-code-blocks]]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clj-time.format :as tf]
            [clj-time.core :as t]
            [fr.post :refer [create-post]]
            [hiccup.page :refer  [html5]]
            [me.raynes.cegdown :as md]
            [stasis.core :as stasis]))

(defn get-assets  []
  (assets/load-assets "public"  [#".*"]))

(defn monthf  [date]
    (tf/unparse  (tf/formatter "MMM") date))

(defn dayf  [date]
    (tf/unparse  (tf/formatter "dd") date))

(defn yearf  [date]
    (tf/unparse  (tf/formatter "yyyy") date))

(defn layout-page  [request page]
  (html5
    [:head
     [:meta  {:charset "utf-8"}]
     [:meta  {:name "viewport"
              :content "width=device-width, initial-scale=1.0"}]
     [:title "first rest"]
     [:link  {:rel "stylesheet" :href  (link/file-path request "/styles/main.css")}]]
    [:body
     [:div.logo "first rest"]
     page]))

(defn- make-link  [link]
    [:a  {:href  (str "/links/" link)} link])

(defn- connect  [links]
  (reduce #(conj %1 ", "  (make-link %2))  (make-link  (first links))  (rest links)))

(defn core-page [request {:keys  [title tags date path content]}]
  (html5
    [:head
     [:meta  {:charset "utf-8"}]
     [:meta  {:name "viewport"
              :content "width=device-width, initial-scale=1.0"}]
     [:title (str "first rest : " title)]
     [:link  {:rel "stylesheet" :href  (link/file-path request "/styles/main.css")}]]
    [:body
     [:div.logo "first rest"]
     [:article.page 
      [:header
       [:h2.title title]
       (when tags 
         [:span.tags (connect tags)])
       (when date
         [:span.date [:time  {:datetime date}  (monthf date) " "  (dayf date) ", "  (yearf date)]])]
      [:section.content content]
      [:footer.endcap "nothing to see here"]]]))

(defn layout-post  [post]
    [(:path post)  (fn  [req]  (core-page req post))])

(defn layout-posts  [posts]
    (let  [post-layouts  (map layout-post posts)]
          (into  {} post-layouts)))

(defn home-page [pages]
  (zipmap  (keys pages)
           (map #(fn  [req]  (layout-page req %))  (vals pages))))

(defn partial-pages  [pages]
  (zipmap (keys pages)
          (map #(fn  [req]  (layout-page req %))  (vals pages))))

(defn gen-posts-from-type [kind]
  (let [location (str "resources/" kind)]
    (map #(create-post kind %) (stasis/slurp-directory location #"\.md$"))))

(defn get-raw-pages  []
  (let [blog-posts (gen-posts-from-type "blog")
        mlog-posts (gen-posts-from-type "mlog")]
    (stasis/merge-page-sources
      {:public  (home-page (stasis/slurp-directory "resources/public" #".*\.html$"))
       :partials  (partial-pages  (stasis/slurp-directory "resoues/partials" #".*\.html$"))
       :blog (layout-posts blog-posts)
       :mlog (layout-posts mlog-posts)})))

(defn prepare-page  [page req]
    (->  (if  (string? page) page  (page req)) highlight-code-blocks))

(defn prepare-pages  [pages]
    (zipmap  (keys pages)  (map #(partial prepare-page %)  (vals pages))))

(defn get-pages  []
    (prepare-pages  (get-raw-pages)))

(def app (optimus/wrap (stasis/serve-pages get-pages)  get-assets optimizations/all serve-live-assets) )

(def export-dir "dist")

(defn export  []
  (let  [assets  (optimizations/all  (get-assets)  {})]
    (stasis/empty-directory! export-dir)
    (optimus.export/save-assets assets export-dir)
    (stasis/export-pages  (get-pages) export-dir  {:optimus-assets assets})))
