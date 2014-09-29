(ns example.hello
  (:require [cognitect.transit :as t]
            [cljs.core.async :refer [<! >! chan put!]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [hiccups.core :as hiccups]))

(def tweets-channel (chan))

(def app-state (atom {
                      :tweets []
                      :new-tweets []
                      :tweets-count 50
                      :tweets-received 0
                      :new-tweets-received 0
                      }))

(defn log [text]
  (.log js/console text))

(def r (t/reader :json))

(def socket (js/WebSocket. (str "ws://" js/location.hostname ":" js/location.port "/ws")))

(set! socket.onmessage (fn [e] (go (>! tweets-channel (t/read r e.data)))))
(set! socket.onerror (fn [e] (go (>! tweets-channel (t/read r e.data)))))

(defn tweet [{:keys [text urls titles]} owner]
  (reify
    om/IRender
    (render [this]
      (dom/div #js {:className "tweet"}
               (dom/p nil text)
               (apply dom/p #js {:className "link-title"} titles)
               (apply dom/ul nil
                      (map (fn [url]
                             (dom/li nil
                                     (dom/a #js {:href url} url))) urls))))))

(defn tweets [{:keys [connected tweets-count new-tweets-received] :as app} owner]
  (reify
    om/IWillMount
    (will-mount [this]
      (go (while true
            (let [tweet (<! tweets-channel)]
              (om/transact! app :tweets-received inc)
              (om/transact! app :new-tweets-received inc)
              (om/transact! app :new-tweets #(take tweets-count (conj % tweet)))))))
    om/IRender
    (render [this]
      (dom/div #js {:id "tweets"}
               (dom/div nil (str "Received " (:tweets-received app) " links so far"))
               (when (> new-tweets-received 0)
                 (dom/div #js {:onClick (fn [e]
                                          (om/transact! app :new-tweets-received (constantly 0))
                                          (om/transact! app :tweets #(take tweets-count (concat (:new-tweets @app) %)))
                                          (om/transact! app :new-tweets (constantly [])))
                               :className "new-tweets"}
                          (str new-tweets-received " new tweets received")))
               (apply dom/div #js {:className "tweets"}
                      (om/build-all tweet (:tweets app)))))))

(om/root tweets app-state
         {:target (. js/document (getElementById "app"))})
