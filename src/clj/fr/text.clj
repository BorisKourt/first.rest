(ns clj.fr.text)

(defn robots [_]
  "User-agent: *
Allow: /
#
# Hello my dearest robot, I hope you enjoy your read.
# - If you have any questions, you know what to do.
#
# Yours truly,
# Boris
#")

(defn humans [_]
  "/* TEAM */
Current Maintainer: Boris Kourtoukov
Twitter: @boriskourt

/* THANKS */
Community: The amazing people within the Clojure community

/* SITE */
Libs: Stasis, Optimus, Enlive, Clygments, Clj-jgit, Clj-template, ring, hiccup")
