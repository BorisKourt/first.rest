(ns clj.fr.git
	(:require [clj-jgit.porcelain :refer (load-repo git-branch-list git-log git-blame)]
                  [clj-jgit.querying :refer (rev-list commit-info)]))

;; --- 
;; File versions
;; ---

(defn latest-commit []
	(let [repo (load-repo ".git")]
	    (let [commits (git-log repo "master~~" "master")
	          commit  (first commits)]
	        (println (commit-info repo commit))
                (println (first  (git-blame repo "README.md"))))))
