(ns pong-clj.server
  (:use [clojure.java.io :only [reader writer]]
        [server.socket :only [create-server]]))

(defn main []
  (println "Server main"))
