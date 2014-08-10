(ns clj.fr.picture
  (:require [optimus.link :as link]
            [clojure.string :as str]
            [optimus.paths :as path]
            [pathetic.core :as pathetic]
            [net.cgrand.enlive-html :as enlive]))

(def breakpoint 721)

(def sizes [200 400 600 860 1020])

(defn build-srcset
  "Constructs the list of image files based on
  resolution and file type."
  [request image sizes]
  (str/join ", " (map #(str (link/file-path
                              request
                             (str "/post-assets/" % "-" image))
                            " " % "w")
                          sizes)))

(defn prepare-image [node request]
  (let [src (->> node :attrs :src)
        alt (->> node :attrs :alt)
        path (path/just-the-path src)]
    (if (pathetic/absolute-path? src)
      (assoc node
             :attrs
             {:src (link/file-path request src)
              :srcset (build-srcset request (path/just-the-filename src) sizes)
              :sizes  (str "(min-width: " breakpoint "px) 60vw, 80vw)")
              :alt (if alt alt "")
              :class "optim"})
      node)))

(defn convert-to-srcset
   ([page] page)
   ([page request]
      (enlive/sniptest page
                   [:img] #(prepare-image % request))))
