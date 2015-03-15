(ns alexandria.system
  (:require [com.stuartsierra.component :as component]
            modular.ring
            [modular.http-kit :refer [new-webserver]]
            [datomic.api :as d]))

(defn system
  []
  (component/system-map
    :handler
    (fn [_]
      {:status 200
       :body   (pr-str
                 (d/q '{:find  [(count ?e) .]
                        :where [[?e :request/uuid]]}
                      (d/db (d/connect "datomic:ddb://us-east-1/msg-production-db/scribe"))))})

    :web-server
    (component/using
      (new-webserver :port 3000)
      [:handler])))

(comment

  (def sys (atom (system)))

  (swap! sys component/start)

  (swap! sys component/stop)
  )