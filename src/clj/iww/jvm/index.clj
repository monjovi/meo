(ns iww.jvm.index
  (:require [hiccup.page :refer [html5 include-css include-js]]
            [compojure.route :as r]
            [iww.jvm.routes.upload-qr :as qr]
            [iww.jvm.files :as f]
            [iww.jvm.routes.images :as ir]
            [iww.jvm.routes.map-tile :as mt]
            [iww.jvm.file-utils :as fu]))

(defn index-page [_]
  (html5
    {:lang "en"}
    [:head
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
     [:title "test"]]
    [:body
     [:div [:h1 "hello world"]]]))

(defn routes-fn [_put-fn]
  [(r/files "/photos" {:root fu/img-path})
   (r/files "/audio" {:root (str fu/data-path "/audio/")})
   (r/files "/videos" {:root (str fu/data-path "/videos/")})
   qr/address-qr-route
   ir/img-resized-route
   mt/map-tile-route])

(def sente-map
  {:index-page-fn index-page
   :routes-fn     routes-fn
   :host          "0.0.0.0"
   :port          8765
   :relay-types   #{:entry/saved :entry/found :state/new
                    :stats/result :stats/result2 :state/stats-tags :cmd/toggle-key
                    :state/stats-tags2 :search/refresh :cfg/show-qr}})