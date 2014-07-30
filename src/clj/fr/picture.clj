(ns clj.fr.picture
  (:require [optimus.link :as link]
            [clojure.string :as str]
            [hiccup.core :refer  [html]]))

(defn build-srcset 
  "Constructs the list of image files based on
  resolution and file type."
  [request image sizes ext]
  (str/join ", " (map #(str (link/file-path 
                             request 
                             (str "/post-assets/" % "-" image ext))
                            " " % "w") 
                          sizes)))

(defn picture
  "Creates a picture element with hiccup based on
  the responsive image practices outline here:
  http://dev.opera.com/articles/responsive-images/" 
  [request 
   image
   alt 
   &  {:keys  [breakpoint sizes] 
       :or    {breakpoint 721 
               sizes  [200,400,600,860,1020]}}]
  (html 
    [:img 
     {:src  
      (link/file-path
        request
        (str "/post-assets/" image ".jpg"))
      :alt alt
      :sizes  (str "(min-width:" breakpoint ") 60vw, 80vw")
      :srcset (build-srcset request image sizes ".jpg")
      :style "max-width: 100%;"}]))

;; Create 'Extract Local Picture'
