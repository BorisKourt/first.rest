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

(defn- make-link  [link]
  [:a  {:href  (str "/links/" link ".html")} link])

(defn- connect  [links]
  (reduce #(conj %1 ", "  (make-link %2))  (make-link  (first links))  (rest links)))

(defn wrapper [request title page]
  (html5
    [:head
     [:meta  {:charset "utf-8"}]
     [:meta  {:name "viewport"
              :content "width=device-width, initial-scale=1.0"}]
     [:title (str "first rest : " title)]
     [:link  {:rel "stylesheet" :href  (link/file-path request "/styles/main.css")}]]
    [:body
     [:div.logo "first rest"]
     page]
    [:footer.endcap "nothing to see here"]))

(defn index-page [request posts]
  (let  [{:keys  [title tags date path content]}  (->> posts  (sort-by :date) reverse first)]
    (wrapper request title
             [:section.left "hi"]
             [:aside.right "hello"])))

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

(defn single-item [request {:keys  [title tags date path content]}]
  (wrapper request title
           [:article.page 
            [:header
             [:h2.title title]
             (when tags 
               [:span.tags (connect tags)])
             (when date
               [:span.date [:time  {:datetime date}  (monthf date) " "  (dayf date) ", "  (yearf date)]])]
            [:section.content content]]))

(defn- archive-post  [{:keys  [title date tags path]}]
  [:article
   [:h1  [:a  {:href path} title]]
   [:time  {:datetime date}
    [:span.month  (monthf date)] " "
    [:span.day  (dayf date)]
    [:span.year  (yearf date)]]
   (when  (not-empty tags)
     [:span.categories "Tags: "  (connect tags)])])

(defn- archive-group  [[year posts]]
  (let  [sorted-posts  (reverse  (sort-by :path posts))]
    (cons
      [:h2 year]
      (map archive-post sorted-posts))))

(defn archive-like  [request posts title]
  (let  [post-groups  (->> posts  (group-by #(t/year  (:date %)))  (sort-by first) reverse)]
    [:div
       [:article.entry  {:role "article"}
        [:header
         [:h1.entry-title title]]
        [:div.body.entry-content
         [:div#blog-archives
          (map archive-group post-groups)]]]]))

(defn archive [request posts title]
 (wrapper request title 
  (archive-like request posts title)))

(defn home [request blog mlog] 
  (wrapper request "core"
  [:div 
   [:section.main (archive-like request blog "Main")]
   [:aside.right (archive-like request mlog "Shortform")]]))

(defn tag  [request posts tag]
  (archive request posts tag))

(defn tag-post  [{:keys  [path title]}]
  [:article
   [:h1  [:a  {:href path} title]]])

(defn tag-entry  [tag posts]
  (let  [sorted-posts  (reverse  (sort-by :date posts))]
    (cons
      [:h2 tag]
      (map tag-post sorted-posts))))

(defn tags  [request posts]
  (let  [unique-tags  (->> posts  (map :tags) flatten distinct sort)]
    (wrapper request "connections"
          [:div#content
           [:article.hentry  {:role "article"}
            [:header
             [:h1.entry-title "Tags"]]
            [:div.body.entry-content
             [:div#blog-archives
              (map #(tag-entry % posts) unique-tags)]]]])))


(defn layout-tag-page  [tags posts]
    [(str "/links/" tags ".html")  (fn  [req]  (tag req posts tags))])

(defn get-unique-tags  [posts]
    (->> posts  (map :tags) flatten distinct sort))

(defn tag-posts  [tag posts]
    (filter #((-> % :tags set) tag) posts))

(defn group-tags-posts  [posts]
    (let  [unique-tags  (get-unique-tags posts)]
          (reduce #(assoc %1 %2  (tag-posts %2 posts))  {} unique-tags)))

(defn create-tag-pages  [posts]
    (let  [tags-posts  (group-tags-posts posts)
                   tags-layouts  (map #(apply layout-tag-page %) tags-posts)]
          (into  {} tags-layouts)))

(defn layout-post  [post]
  [(:path post)  (fn  [req]  (single-item req post))])

(defn layout-posts  [posts]
  (let  [post-layouts  (map layout-post posts)]
    (into  {} post-layouts)))

(defn partial-pages  [pages]
  (zipmap (keys pages)
          (map #(fn  [req]  (layout-page req %))  (vals pages))))

(defn gen-posts-from-type [kind]
  (let [location (str "resources/" kind)]
    (map #(create-post kind %) (stasis/slurp-directory location #"\.md$"))))

(defn create-dynamic-pages  [blog-posts mlog-posts posts]
  {"/index.html"       (fn  [req]  (home req blog-posts mlog-posts))
   "/blog/index.html"  (fn  [req]  (archive req blog-posts "Blog Entries"))
   "/mlog/index.html"  (fn  [req]  (archive req mlog-posts "Micro Blog"))
   "/links/index.html" (fn  [req]  (tags req posts))
  })

(defn get-raw-pages  []
  (let [blog-posts (gen-posts-from-type "blog")
        mlog-posts (gen-posts-from-type "mlog")
        posts (concat blog-posts mlog-posts)]
    (stasis/merge-page-sources
      {:partials (partial-pages  (stasis/slurp-directory "resources/partials" #".*\.html$"))
       :dynamic  (create-dynamic-pages blog-posts mlog-posts posts)
       :blog (layout-posts blog-posts)
       :mlog (layout-posts mlog-posts)
       :conn (create-tag-pages posts)
       })))

(defn prepare-page  [page req]
  (->  (if  (string? page) page  (page req)) 
        highlight-code-blocks))

(defn prepare-pages  [pages]
  (zipmap (keys pages)  
          (map #(partial prepare-page %) (vals pages))))

(defn get-pages  []
  (prepare-pages  (get-raw-pages)))

(def app (optimus/wrap 
           (stasis/serve-pages get-pages)  
           get-assets 
           optimizations/all 
           serve-live-assets))

(def export-dir "dist")

(defn export  []
  (let  [assets  (optimizations/all  (get-assets)  {})]
    (stasis/empty-directory! export-dir)
    (optimus.export/save-assets assets export-dir)
    (stasis/export-pages  (get-pages) export-dir  {:optimus-assets assets})))
