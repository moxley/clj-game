(ns pong-clj.core
  (:require [pong-clj.client :as client]
            [pong-clj.server :as srvr]
            [pong-clj.entities :as entities])
  (:use commandline.core))

(def runtime-state (atom {}))

(defn process-command-line []
  (with-commandline
    [arguments ["-n" "--network" "-s" "--server"]]
    [[n network "connect to multi-player server"]
     [s server "start multi-player server"]]
    (println "network:" network)
    (println "server:" server)
    (when (true? network)
      (swap! entities/game into {:network? true}))
    (when (true? server)
      (swap! runtime-state into {:server? true}))))

(defn -main [& rest]
  (process-command-line)
  (client/main))
