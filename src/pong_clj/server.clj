(ns pong-clj.server
  (:use [clojure.java.io :only [reader writer]])
  (:require [pong-clj.network :as net])
  (:import (java.net Socket)
           (java.net DatagramSocket DatagramPacket InetAddress)))

(defn create-server [port]
  (let [serverSocket (DatagramSocket. port)
        receiveData (byte-array 1024)
        sendData (byte-array 1024)]
    (loop []
      (let [receivePacket (DatagramPacket. receiveData (alength receiveData))
            _ (.receive serverSocket receivePacket)
            raw-packet (.getData receivePacket)]
        (when (net/protocol-packet? raw-packet)
          (let [packet (net/unpack-packet raw-packet)
                sentence (String. (byte-array (:payload packet)))
                _ (println "RECEIVED: " sentence)
                addr (.getAddress receivePacket)
                port (.getPort receivePacket)
                capitalizedSentence (.toUpperCase sentence)
                sendData (.getBytes capitalizedSentence)
                sendPacket (DatagramPacket. sendData (alength sendData) addr port)
                _ (.send serverSocket sendPacket)])))
      (recur))))

(defn main
  ([port]
    (create-server port)
    (println "Listening on port" port))
  ([] (main net/UDP_PORT)))
