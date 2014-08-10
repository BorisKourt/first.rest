<!--
{
:title "Basic img srcset support for Stasis with Optimus"
:connections [clojure,stasis,optimus,srcset]
}
-->

As I work on creating this site I want to document some bits of progress that others will hopefully find useful. 

One of the topics I want to focus on while using a static site generator is end user page speed. Although still in early browser support stages the `srcset` attribute in html `<img>` tags is a great way to help lower the bandwidth that, especially mobile, users will have to incur. (A shim helps fill in support gaps)

At the time of writing the resources section of this site is still in early development so I will list some of the relevant content and people that helped make getting started with stasis possible for a relative novice like myself.

## Result:

![Swiss](/resources/public/post-assets/switzerland.jpg)

If you inspect the above image you should see something similar to:

```html
<img 
srcset="/post-assets/adccb3cb3896/200-switzerland.jpg 200w, 
	/post-assets/8558d11b0040/400-switzerland.jpg 400w, 
	/post-assets/ef0a980b2de5/600-switzerland.jpg 600w, 
	/post-assets/ad548f984c46/860-switzerland.jpg 860w, 
	/post-assets/0a6fa84978b4/1020-switzerland.jpg 1020w" 
sizes="(min-width: 721px) 60vw, 80vw)" 
alt="Swiss">
```
This is generated from from an unmodified markdown image declaration. 

As you can see both image sizes and paths for aggressive caching (from Optimus) are working.

## The code:

To start we need to adjust the way assets are optimized via Optimus: 

```clj
(defn optimize
  "Have not been able to map over `transform-images` here."
  [assets options]
  (-> assets
      (transform-images {:regexp #"/post-assets/.*\.jpg"
                         :quality 0.7
                         :width 1020
                         :prefix "1020-"
                         :progressive true})
      (transform-images {:regexp #"/post-assets/.*\.jpg"
                         :quality 0.7
                         :width 860
                         :prefix "860-"
                         :progressive true})
      (transform-images {:regexp #"/post-assets/.*\.jpg"
                         :quality 0.7
                         :width 600
                         :prefix "600-"
                         :progressive true})
      (transform-images {:regexp #"/post-assets/.*\.jpg"
                         :quality 0.7
                         :width 400
                         :prefix "400-"
                         :progressive true})
      (transform-images {:regexp #"/post-assets/.*\.jpg"
                         :quality 0.7
                         :width 200
                         :prefix "200-"
                         :progressive true})
      (transform-images {:regexp #"/post-assets/.*\.jpg"
                         :quality 0.6
                         :width 904
                         :progressive true})
      (optimizations/all options)))
```

`transform-images` is added to the asset optimiziation "pipeline" and each
statement grabs the original image and reduces it by the parameters. Note that
you can specify the quality parameter to carefully control size. (One trick
not implemented here is to use a very low, around 0.3, quality value on images
for high density displays as the physical size of device would hide the artifacts)

In `export` this is plugged in like so:

```clj
(defn export  []
  (let  [assets  (optimize  (get-assets)  {})] ;; Note our `optimize` function
    (stasis/empty-directory! export-dir)
    (optimus.export/save-assets assets export-dir)
    (stasis/export-pages  (get-pages) export-dir  {:optimus-assets assets})))
```

Everything else is done in `clj.fr.picture` namespace:

```clj
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
             {:srcset (build-srcset request (path/just-the-filename src) sizes)
              :sizes  (str "(min-width: " breakpoint "px) 60vw, 80vw)")
              :alt (if alt alt "")})
      node)))

(defn convert-to-srcset
   ([page] page)
   ([page request]
      (enlive/sniptest page
                   [:img] #(prepare-image % request))))
```



Here is another demo:

![Cardinal, from the social body lab](/resources/public/post-assets/cardinal.jpg)
