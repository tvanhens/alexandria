(ns alexandria.system
  (:require [com.stuartsierra.component :as component]
            [clojure.core.async :refer [close! go-loop <! chan]]
            [modular.ring :refer [new-web-request-handler-head]]
            [modular.http-kit :refer [new-webserver]]
            [modular.bidi :refer [new-router]]
            [ring.middleware.defaults :refer [api-defaults wrap-defaults]]
            [bidi.bidi :refer [RouteProvider]]
            [ring.middleware.cors :refer [wrap-cors]]
            [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit
             :refer [sente-web-server-adapter]]))

;; -- Mutitmethods -------------------------------------------------------------

(defmulti dispatch
          "Dispatches a sente message based off of the variant token of :?data."
          (fn [{:keys [id]}] id))

;; -- Dispatch Handlers --------------------------------------------------------

(defmethod dispatch :hello/world
  [& _]
  (println "World"))

(defmethod dispatch :default
  [& _]
  (println "None found"))

;; -- Sente --------------------------------------------------------------------

(defrecord SenteServer []
  component/Lifecycle
  (start [this]
    (let [{:keys [ch-recv] :as sente-config}
          (sente/make-channel-socket! sente-web-server-adapter {})]
      (go-loop []
        (when-let [v (<! ch-recv)]
          (dispatch v)
          (recur)))
      (merge this sente-config)))

  (stop [{:keys [ch-recv] :as this}]
    (when ch-recv (close! ch-recv))
    (dissoc this :ch-recv :send-fn :ajax-post-fn :ajax-get-or-ws-handshake-fn
            :connected-uids))

  RouteProvider
  (routes [{:keys [ajax-post-fn ajax-get-or-ws-handshake-fn]}]
    ["" {"/chsk" {:get  ajax-get-or-ws-handshake-fn
                  :post ajax-post-fn}}]))

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

