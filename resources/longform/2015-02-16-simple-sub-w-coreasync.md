<!--
{
:title "Simple Channel Communication via Core.Async"
:connections [clojure,core.async,chan,sub,node,jvm]
}
-->

The intent of this article is to provide you with a reusable base setup that can be adapted for basic communication
between discrete parts of your code.

First you will need to include `core.async` in your project.

```clj
:dependencies [[org.clojure/clojure "1.7.0-alpha5"] ;; CLJ Version
               [org.clojure/core.async "0.1.346.0-17112a-alpha"]] ;; Add this line.
```

Then require `core.async` in your src file:

```clj
; If using CLJ
(ns project.comm
  (:require [clojure.core.async :as a :refer [sub chan pub <! >! go-loop go]]))

; If using CLJS
(ns project.comm
  (:require [cljs.core.async :as a :refer [sub chan pub <! >! put!]])
  (:require-macros [cljs.core.async.macros :refer [go-loop go]]))
```

The first thing is to create a core channel that all communication will be written to:

```clj
(defonce content-stream (chan))
```

Next we are going to define a way to watch `content-stream` for specific topics:

```clj
(defonce watch-stream-for
  (pub content-stream #(:topic %)))
```

This allows us to do the following:

```clj
(defonce first-chan (chan))
(defonce second-chan (chan))

(defn begin-subscriptions []
  (sub watch-stream-for :for-first first-chan)
  (sub watch-stream-for :for-second  rest-chan))
```

Above we define two channels that will contain filtered data from the main channel. Below that is a function that can
be called at the start of the program to subscribe these channels to `content-stream` based on a specific topic. In this
case we are looking for the `:for-first` and `:for-second` messages.

This leaves two things, placing data on `content-stream` and reading the two *sub* channels for new messages.

### Writing to `content-stream`

Lets say we have two AJAX handlers that fire when we receive a completed response, `first-handler` and `second-handler`.
We can tie them into our system in the following way:

```clj
(defn first-handler [response]
    (go (>! content-stream {:topic :for-first
                            :msg response})))

(defn second-handler [response]
  (go (>! content-stream {:topic :for-second
                          :msg response})))
```

All we want to do is pass the response to the relevant parts of our application. Above, when the handler is called with
the provided response we use `>!` to write to `content-stream` providing a map that contains `:topic` &amp; `:msg` keys.

The core benefit is that our AJAX code doesn't need to know who will be dealing with the data it eventually receives, or
when they can get to it. This, in my opinion, provides a clean separation between parts of an application.

### Reading

Anyone can read from the *sub* channels, and they don't have to know where the data on them came from. This can allow
for more than one AJAX handler above to send the same topic to `content-stream` if you want to handle it in the same
way.

Below is the smallest chunk of code to take data from `first-chan` or `second-chan`.

```clj
(go-loop []
         (when-let [v (<! first-chan)]
           (println (:msg v)) ;; :msg contains the above :response
           (recur)))
```

Here we are waiting for `first-chan` to receive a value, and as soon as that happens fire the body. Which in this case
just prints out the response.

At this point this message is in the hands of whatever needs to deal with its contents.

```clj
;; Example in om with om-tools:

(defcomponent app
  "main app"
  [data owner]
  (will-mount [_]
    (go-loop []
             (when-let [v (<! comm/multi-post)] ;; Waits for a new group of posts to come in.
               ;; Merges new posts into the old, and re-renders the view.
               (om/transact! data [:posts] #(into [] (concat % (:msg v))))
               (recur)))
  (render [_]
    (dom/div
       (om/build-all render-posts (:posts data))))
```

## Conclusion

As stated above this can cleanly separate parts of your code. It is especially useful for keeping communication logic
apart from display logic. `core.async` can be used in many other ways as well, here is just a fast way to integrate
this particular part of its functionality into your future or existing application.

Thank you for reading and I hope that this is helpful! And if you find any issues with this implementation please submit
an issue in the source repository linked below.