(ns fr.clj.post
  (:require  [clojure.string :as string]
             [me.raynes.cegdown :as md]
             [clj-time.format :as tf]))

(def formatter  (tf/formatter "yyyy-MM-dd"))

(def pegdown-options ;; https://github.com/sirthias/pegdown
  [:autolinks :fenced-code-blocks :smartypants :tables :strikethrough])

(defn- remove-meta  [page]
  (string/replace page #"(?is)^---.*?---" ""))

(defn- extract-meta-block  [page]
  (->> page  (re-seq #"(?is)^---(.*?)---") first second))

(defn- extract-title  [meta]
  (->> meta  (re-seq #"title\s*:\s*(.*)") first second))

(defn- extract-connections  [meta]
  (if-let  [connection-str  (->> meta  (re-seq #"connections\s*:\s*\[(.*?)\]") first second)]
    (map #(-> % string/trim string/lower-case)  (string/split connection-str #","))
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
     :connections  (extract-connections meta-section)
     :date  (extract-date raw-path)
     :path path
     :content  (md/to-html content pegdown-options)}))
