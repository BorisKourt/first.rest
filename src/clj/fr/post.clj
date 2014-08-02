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

(defn extract-edn [page]
  (->> page (re-seq #"(?is)^<!--(.*?)-->") first second edn/read-string))

(defn create-post  [kind [raw-path raw-content]]
  (let  [meta-section  (extract-edn raw-content)
         path  (str "/" kind (prepare-path raw-path))
         commit (file-commit-hash (str "resources/" kind raw-path ))]
    {:title (or (:title meta-section) "Random Thought")
     :connections  (or (:connections meta-section) [])
     :date  (extract-date raw-path)
     :path path
     :commit commit
     :content  (md/to-html raw-content pegdown-options)}))
