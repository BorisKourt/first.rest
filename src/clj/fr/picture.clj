(ns clj.fr.picture
  (:require [optimus.link :as link]
            [hiccup.core :refer  [html]]))

(defn buid-srcset 
  "Constructs the list of image files based on
  resolution and file type."
  [request image sizes exten]
  (map #(link/file-path 
          request 
          (str image "-" % ".webp")) 
       sizes))

(defn picture
  "Creates a picture element with hiccup based on
  the responsive image practices outline here:
  http://dev.opera.com/articles/responsive-images/" 
  [request 
   image
   alt 
   &  {:keys  [breakpoint sizes] 
       :or    {breakpoint 640 
               sizes  [200,400,800,1200,1600,2000]}}]
  [:picture
   [:source  
    {:srcset  (str "\"" (build-srcset request image sizes ".webp") "\"")
     :type "image/webp"}]
   [:img 
    {:src (link/file-path
            request
            (str image "-fallback.jpg"))
     :alt alt
     :sizes (str "\"(min-width:" breakpoint ") 60vw, 100vw\"")
     :srcset (str "\"" (build-srcset request image sizes ".jpg") "\"")}]])
