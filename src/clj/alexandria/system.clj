(ns alexandria.system
  (:require [com.stuartsierra.component :as component]
            modular.ring
            [modular.http-kit :refer [new-webserver]]
            [datomic.api :as d]
            [clojure.edn :as edn]
            [ring.middleware.defaults :refer [api-defaults wrap-defaults]]
            [ring.middleware.edn :refer [wrap-edn-params]]
            [ring.middleware.cors :refer [wrap-cors]]))

(def db-uri "datomic:ddb://us-east-1/msg-production-db/scribe")

(defn return-ok
  [result]
  {:status  200
   :headers {"Content-Type" "application/edn"}
   :body    result})

(defn return-error
  [message]
  {:status  500
   :headers {"Content-Type" "application/edn"}
   :body    message})

(defn handler
  [{{:keys [q]} :edn-params}]
  (try
    (let [result
          (pr-str
            (d/q (edn/read-string q)
                 (d/db (d/connect db-uri))))]
      (return-ok (pr-str result)))
    (catch Exception e
      (println (.printStackTrace e))
      (return-error (.getMessage e)))))

(defn system
  []
  (component/system-map
    :handler
    (-> handler
        wrap-edn-params
        (wrap-defaults api-defaults)
        (wrap-cors :access-control-allow-origin [#".*"]
                   :access-control-allow-methods [:get :put :post :delete]))

    :web-server
    (component/using
      (new-webserver :port 3000)
      [:handler])))

(comment
  (pr-str
    '{:find  [(count ?e)]
      :where [[?e :request/uuid]]})
  )
