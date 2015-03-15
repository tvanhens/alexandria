(ns alexandria.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent]
            [re-frame.core :refer [register-handler
                                   subscribe
                                   dispatch
                                   dispatch-sync
                                   register-sub]]
            [ajax.core :as ajax]))

(def default-value {:reactive-q/queries {}})

(register-handler
  :initialize-db
  (fn [_ _]
    default-value))

(register-handler
  :reactive-q/query!
  (fn [db [_ ident q]]
    (ajax/POST "http://localhost:3000"
               {:format        :edn
                :params        {:q (pr-str q)}
                :handler       #(dispatch [:reactive-q.query!/handle-result
                                           ident %])
                :error-handler #(dispatch [:reactive-q.query!/handle-result
                                           ident (str %)])})
    db))

(register-handler
  :reactive-q.query!/handle-result
  (fn [db [_ ident result]]
    (assoc-in db [:reactive-q/queries ident :result] result)))

(register-handler
  :reactive-q/init!
  (fn [db [_ ident]]
    (assoc-in db [:reactive-q/queries ident :result] "Loading...")))

(register-sub
  :reactive-q/result
  (fn [db [_ ident]]
    (reaction (get-in @db [:reactive-q/queries ident :result]))))

(defn reactive-q
  [ident q interval]
  (js/setInterval #(dispatch [:reactive-q/query! ident q]) interval)
  (dispatch [:reactive-q/init! ident])
  (let [result (subscribe [:reactive-q/result ident])]
    (fn []
      [:div
       [:h1 (str ident)]
       [:div (pr-str @result)]])))

(defn alexandria []
  (fn []
    [:div
     [reactive-q :hits
      '{:find [(count ?req) .]
        :where [[?req :request/uuid]]}
      1000]

     [reactive-q :hits-by-user
      '{:find  [?user-ident (count ?hit)]
        :where [[?hit :request/request ?req]
                [?req :request.request/user ?user]
                [?user :request.request.user/ident ?user-ident]]}
      5000]]))

(defn init! []
  (dispatch [:initialize-db])
  (reagent/render [alexandria] (js/document.getElementById "app")))

