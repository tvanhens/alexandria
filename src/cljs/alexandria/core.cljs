(ns alexandria.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent]
            [re-frame.core :refer [register-handler
                                   subscribe
                                   dispatch
                                   dispatch-sync
                                   register-sub]]))

(def default-value {:name "Tyler"})

(register-handler
  :initialize-db
  (fn [_ _]
    default-value))

(register-handler
  :change-name
  (fn [db [_ new-v]]
    (assoc db :name new-v)))

(register-sub
  :name
  (fn [db _]
    (reaction (:name @db))))

(defn alexandria []
  (let [name (subscribe [:name])]
    (fn []
      [:div
       [:input {:type      "text"
                :value     @name
                :on-change #(dispatch [:change-name
                                       (-> % .-target .-value)])}]
       [:div "Hello " @name]])))

(defn init! []
  (dispatch [:initialize-db])
  (reagent/render [alexandria] (js/document.getElementById "app")))

