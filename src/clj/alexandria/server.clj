(ns alexandria.server
  (:require [alexandria.handler :refer [app]])
  (:gen-class))

 (defn -main [& args]
   (let [port (Integer/parseInt (or (System/getenv "PORT") "3000"))]
     ))
