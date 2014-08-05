(ns clj.fr.post
  (:require  [clojure.string :as string]
            [me.raynes.cegdown :as md]
            [clj-time.format :as tf]
            [clojure.edn :as edn]
            [clj.fr.git :refer [file-commit-hash]]))

(def formatter  (tf/formatter "yyyy-MM-dd"))

(def pegdown-options ;; https://github.com/sirthias/pegdown
  [:autolinks :fenced-code-blocks :smartypants :tables :strikethrough])

(defn- extract-date  [path]
  (when-let  [date-str  (->> path  (re-seq #"(\d\d\d\d-\d\d-\d\d)") first second)]
    (tf/parse formatter date-str)))

(defn- prepare-path  [path]
  (-> path
      (string/replace #"\.md$" ".html")))

(defn sans-meta [page]
  (string/replace page #"(^<!--[\s|\S]*?-->[\s*(#|$)]*?)" ""))

(defn mdown [page]
  (md/to-html page pegdown-options))

(defn more-link [path content]
  (str content "&hellip; [Full text &raquo;](http://first.rest" path ")"))

(defn extract-excerpt [page path]
  (->> page 
       sans-meta 
       string/split-lines 
       (take 6)
       (string/join "\n") 
       mdown))

(defn extract-edn [page]
  (->> page (re-seq #"(?is)^<!--(.*?)-->") first second edn/read-string))

(defn create-post  [kind [raw-path raw-content]]
  (let  [meta-section  (extract-edn raw-content)
         path  (str "/" kind (prepare-path raw-path))
         commit (file-commit-hash (str "resources/" kind raw-path))]
    {:title (or (:title meta-section) "Random Thought")
     :connections  (or (:connections meta-section) [])
     :date  (extract-date raw-path)
     :path path
     :link (str "http://first.rest/" path)
     :commit (second (string/split (str commit) #" "))
     :content  (mdown (sans-meta raw-content))
     :description (extract-excerpt raw-content path)}))
