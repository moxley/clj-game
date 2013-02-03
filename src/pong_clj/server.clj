(ns pong-clj.server
  (:use [clojure.java.io :only [reader writer]])
  (:require [pong-clj.network :as net])
  (:import (java.net Socket)
           (java.net DatagramSocket DatagramPacket InetAddress)))

(defn create-server [port]
  (let [serverSocket (DatagramSocket. port)]
    (println "Listening on port" port)
    (loop []
      (let [packet (net/receive-packet serverSocket)
            sentence (String. (byte-array (:payload packet)))
            _ (println "RECEIVED: " sentence)
            remote {:addr (:addr packet) :port (:port packet) :socket serverSocket}
            capitalizedSentence (.toUpperCase sentence)
            sendData (.getBytes capitalizedSentence)
            _ (net/send-data remote (.getBytes capitalizedSentence))])
      (recur))))

(defn handle-packet [packet]
  (let [sentence (String. (byte-array (:payload packet)))
        capitalizedSentence (.toUpperCase sentence)]))

(defn main
  ([port]
    (create-server port))
  ([] (main net/UDP_PORT)))
