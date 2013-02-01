(ns pong-clj.core
  (:require [pong-clj.client :as client]
            [pong-clj.server :as server]
            [pong-clj.entities :as entities])
  (:use commandline.core))

(def runtime-state (atom {}))

(defn process-command-line [args]
  (doseq [a args]
    (when (= a "-n")
      (swap! entities/game into {:network? true}))
    (when (= a "-s")
      (swap! runtime-state into {:server? true}))))

(defn -main [& rest]
  (process-command-line rest)
  (if (:server? @runtime-state)
    (server/main)
    (client/main)))
