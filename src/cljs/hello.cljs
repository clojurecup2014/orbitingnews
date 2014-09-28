(ns example.hello
  (:require [cognitect.transit :as t]
            [cljs.core.async :refer [<! >! chan]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [hiccups.core :as hiccups]))

(def tweets-channel (chan))

(def app-state (atom {:tweets [] :tweets-count 50}))

(defn log [text]
  (.log js/console text))

(def r (t/reader :json))

(def socket (js/WebSocket. (str "ws://" js/location.hostname ":" js/location.port "/ws")))

(set! socket.onmessage (fn [e] (go (>! tweets-channel (t/read r e.data)))))
(set! socket.onerror (fn [e] (go (>! tweets-channel (t/read r e.data)))))

(defn tweet [{:keys [msg]} owner]
  (reify
    om/IRender
    (render [this]
      (dom/div #js {:className "tweet"}
               (dom/a #js {:href msg} msg)))))

(defn tweets [{:keys [tweets-count] :as app} owner]
  (reify
    om/IWillMount
    (will-mount [this]
      (go (while true
            (let [tweet (<! tweets-channel)]
              (om/transact! app :tweets #(take tweets-count (conj % tweet)))))))
    om/IRender
    (render [this]
      (apply dom/div #js {:className "tweets"}
             (om/build-all tweet (:tweets app))))))

(om/root tweets app-state
         {:target (. js/document (getElementById "app"))})
