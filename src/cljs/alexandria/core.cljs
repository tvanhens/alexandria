(ns alexandria.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent]
            [re-frame.core :refer [register-handler
                                   subscribe
                                   dispatch
                                   dispatch-sync
                                   register-sub]]
            [ajax.core :as ajax]
            [taoensso.sente :as sente :refer (cb-success?)]))

(def default-chsk-url-fn
  (fn [path {:as window-location :keys [protocol host pathname]} websocket?]
    (str (if-not websocket? protocol (if (= protocol "https:") "wss:" "ws:"))
         "//" "localhost:3000" (or path pathname))))

(let [{:keys [chsk ch-recv send-fn state]}
      (sente/make-channel-socket!
        "/chsk"
        {:type        :ajax
         :chsk-url-fn default-chsk-url-fn})]
  (def chsk chsk)
  (def ch-chsk ch-recv)
  (def chsk-send! send-fn)
  (def chsk-state state))

(def default-value {:name "Tyler"})

(register-handler
  :initialize-db
  (fn [_ _]
    (chsk-send! [:hello/world {:som :data}])
    default-value))

(register-sub
  :name
  (fn [db _]
    (reaction (get @db :name))))

(defn alexandria []
  (let [name (subscribe [:name])]
    (fn []
      [:h1 "Hello " @name])))

(defn init! []
  (dispatch [:initialize-db])
  (reagent/render [alexandria] (js/document.getElementById "app")))

