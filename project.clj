(defproject
  alexandria "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}

  :source-paths ["src/clj" "src/cljs"]

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [cljsjs/react "0.12.2-5"]
                 [reagent "0.5.0"]
                 [reagent-forms "0.4.4"]
                 [reagent-utils "0.1.3"]
                 [secretary "1.2.1"]
                 [org.clojure/clojurescript "0.0-3058" :scope "provided"]
                 [ring "1.3.2"]
                 [ring/ring-defaults "0.1.3"]
                 [prone "0.8.0"]
                 [compojure "1.3.2"]
                 [selmer "0.8.0"]
                 [environ "1.0.0"]
                 [re-frame "0.2.0"]
                 [juxt.modular/http-kit "0.5.3" :exclusions [http-kit]]
                 [juxt.modular/ring "0.5.2"]
                 [com.stuartsierra/component "0.2.3"]
                 [prismatic/schema "0.4.0"]
                 [com.datomic/datomic-pro "0.9.5130"
                  :exclusions [joda-time
                               org.apache.httpcomponents/httpclient
                               org.clojure/tools.cli
                               org.apache.httpcomponents/httpcore]]
                 [http-kit "2.1.16"]
                 ]

  :plugins [
            [lein-cljsbuild "1.0.4"]
            [lein-environ "1.0.0"]
            [lein-ring "0.9.1"]
            [lein-asset-minifier "0.2.2"]]

  :min-lein-version "2.5.0"

  :uberjar-name "alexandria.jar"

  :main alexandria.server

  :clean-targets ^{:protect false} ["resources/public/js"]

  :minify-assets
  {:assets
   {"resources/public/css/site.min.css" "resources/public/css/site.css"}}

  :cljsbuild {:builds {:app {:source-paths ["src/cljs"]
                             :compiler     {:output-to     "resources/public/js/app.js"
                                            :output-dir    "resources/public/js/out"
                                            ;;:externs       ["react/externs/react.js"]
                                            :asset-path    "js/out"
                                            :optimizations :none
                                            :pretty-print  true}}}}

  :profiles {:dev        {:repl-options {:init-ns          alexandria.dev
                                         :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

                          :dependencies [[ring-mock "0.1.5"]
                                         [ring/ring-devel "1.3.2"]
                                         [leiningen "2.5.1"]
                                         [figwheel "0.2.5-SNAPSHOT"]
                                         [weasel "0.6.0-SNAPSHOT"]
                                         [com.cemerick/piggieback "0.1.6-SNAPSHOT"]
                                         [pjstadig/humane-test-output "0.6.0"]
                                         [org.clojure/tools.namespace "0.2.10"]]

                          :source-paths ["env/dev/clj"]
                          :plugins      [[lein-figwheel "0.2.3-SNAPSHOT"]]

                          :injections   [(require 'pjstadig.humane-test-output)
                                         (pjstadig.humane-test-output/activate!)]

                          :figwheel     {:http-server-root "public"
                                         :server-port      3449
                                         :css-dirs         ["resources/public/css"]
                                         :ring-handler     alexandria.handler/app}

                          :env          {:dev? true}

                          :cljsbuild    {:builds {:app {:source-paths ["env/dev/cljs"]
                                                        :compiler     {:main       "alexandria.dev"
                                                                       :source-map true}}
                                                  }
                                         }}

             :uberjar    {:hooks       [leiningen.cljsbuild minify-assets.plugin/hooks]
                          :env         {:production true}
                          :aot         :all
                          :omit-source true
                          :cljsbuild   {:jar    true
                                        :builds {:app
                                                 {:source-paths ["env/prod/cljs"]
                                                  :compiler
                                                                {:optimizations :advanced
                                                                 :pretty-print  false}}}}}

             :production {:ring      {:open-browser? false
                                      :stacktraces?  false
                                      :auto-reload?  false}
                          :cljsbuild {:builds {:app {:compiler {:main "alexandria.prod"}}}}
                          }}

  :repositories
  {"my.datomic.com"
   {:url      "https://my.datomic.com/repo"
    :username ~(System/getenv "DATOMIC_USERNAME")
    :password ~(System/getenv "DATOMIC_PASSWORD")}})
