(ns alexandria.dev
  (:require [cemerick.piggieback :as piggieback]
            [weasel.repl.websocket :as weasel]
            [leiningen.core.main :as lein]
            [com.stuartsierra.component :as component]
            [clojure.tools.namespace.repl :refer [refresh]]
            [alexandria.system :as alexandria]))

(def system nil)

(defn init []
  (alter-var-root #'system (constantly (alexandria/system))))

(defn start []
  (alter-var-root #'system component/start))

(defn stop []
  (alter-var-root #'system (fn [s] (when s (component/stop s)))))

(defn go []
  (init)
  (start))

(defn reset []
  (stop)
  (refresh :after 'alexandria.dev/go))

(defn browser-repl []
  (piggieback/cljs-repl :repl-env (weasel/repl-env :ip "0.0.0.0" :port 9001)))

(defn start-figwheel []
  (future
    (print "Starting figwheel.\n")
    (lein/-main ["figwheel"])))

(comment
  (require '[datomic.api :as d])

  (d/q '{:find  [?time]
         :where [[?hit :request/request ?req]
                 [?hit :request/initTime ?time]
                 [?req :request.request/user ?user]
                 [?user :request.request.user/ident "tantonini@sfreedman.com"]]}
       (d/db (d/connect "datomic:ddb://us-east-1/msg-production-db/scribe")))

  )
