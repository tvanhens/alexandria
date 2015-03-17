(ns alexandria.system
  (:require [com.stuartsierra.component :as component]
            [clojure.core.async :refer [close! go-loop <!]]
            [modular.ring :refer [new-web-request-handler-head]]
            [modular.http-kit :refer [new-webserver]]
            [modular.bidi :refer [new-router]]
            [ring.middleware.defaults :refer [api-defaults wrap-defaults]]
            [bidi.bidi :refer [RouteProvider]]
            [ring.middleware.cors :refer [wrap-cors]]
            [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit
             :refer [sente-web-server-adapter]]))

(def db-uri "datomic:ddb://us-east-1/msg-production-db/scribe")

;; -- Sente --------------------------------------------------------------------

(defrecord SenteServer []
  component/Lifecycle
  (start [this]
    (let [{:keys [ch-recv] :as sente-config}
          (sente/make-channel-socket! sente-web-server-adapter {})]
      (go-loop []
        (when-let [{:keys [?data]} (<! ch-recv)]
          (println ?data)
          (recur)))
      (merge this sente-config)))

  (stop [{:keys [ch-recv] :as this}]
    (when ch-recv (close! ch-recv))
    (dissoc this :ch-recv :send-fn :ajax-post-fn :ajax-get-or-ws-handshake-fn
            :connected-uids))

  RouteProvider
  (routes [{:keys [ajax-post-fn ajax-get-or-ws-handshake-fn]}]
    ["" {"/chsk" {:get     ajax-get-or-ws-handshake-fn
                  :post    ajax-post-fn
                  :options (fn [_] {:status 200})}}]))

;; -- System -------------------------------------------------------------------

(defn system
  []
  (component/system-map
    :middleware
    (comp #(wrap-cors % #".*")
          #(wrap-defaults % api-defaults))

    :sente
    (->SenteServer)

    :router
    (component/using
      (new-router)
      [:sente])

    :router-head
    (component/using
      (new-web-request-handler-head)
      {:request-handler :router
       :middleware      :middleware})

    :web-server
    (component/using
      (new-webserver :port 3000)
      [:router-head])))

