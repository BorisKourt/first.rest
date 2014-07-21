(ns fr.post
  (:require  [fr.highlight :as highlight]
             [clojure.string :as string]
             [me.raynes.cegdown :as md]
             [clj-time.format :as tf]))

(def formatter  (tf/formatter "yyyy-MM-dd"))

(def pegdown-options ;; https://github.com/sirthias/pegdown
  [:autolinks :fenced-code-blocks :smartypants :strikethrough])


(defn- remove-meta  [page]
  (string/replace page #"(?is)^---.*?---" ""))

(defn- extract-meta-block  [page]
  (->> page  (re-seq #"(?is)^---(.*?)---") first second))

(defn- extract-title  [meta]
  (->> meta  (re-seq #"title\s*:\s*(.*)") first second))

(defn- extract-tags  [meta]
  (if-let  [tag-str  (->> meta  (re-seq #"tags\s*:\s*\[(.*?)\]") first second)]
    (map #(-> % string/trim string/lower-case)  (string/split tag-str #","))
    []))

(defn- extract-date  [path]
  (when-let  [date-str  (->> path  (re-seq #"(\d\d\d\d-\d\d-\d\d)") first second)]
    (tf/parse formatter date-str)))

(defn- prepare-path  [path]
  (-> path
      (string/replace #"\.md$" ".html")
      (string/replace #"(\d\d\d\d)-(\d\d)-(\d\d)-" "$1/$2/$3/")))

(defn create-post  [kind [raw-path raw-content]]
  (let  [meta-section  (extract-meta-block raw-content)
         content  (remove-meta raw-content)
         path  (str "/" kind (prepare-path raw-path))]
    {:title  (or  (extract-title meta-section) "Random Thought")
     :tags  (extract-tags meta-section)
     :date  (extract-date raw-path)
     :path path
     :content  (md/to-html content pegdown-options)}))
