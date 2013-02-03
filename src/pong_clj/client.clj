(ns pong-clj.client
  (:require [pong-clj.logic :as logic]
            [pong-clj.display :as display]
            [pong-clj.input :as input]
            [pong-clj.entities :as entities]
            [pong-clj.network :as net])
  (:import [org.lwjgl Sys]
           [java.net ConnectException DatagramSocket DatagramPacket InetAddress]))

(defn client-loop [conn]
  (let [clientSocket (DatagramSocket.)
        addr (InetAddress/getByName (:host conn))
        _ (println "IPAddress:" addr)
        remote (into conn {:addr addr :socket clientSocket})]
    (loop [i 0]
      (let [sentence   "Hello, this is client"
            _ (println "Sending:" sentence)
            _ (net/send-data remote (.getBytes sentence))
            packet (net/receive-packet clientSocket)
            modifiedSentence (String. (:payload packet))
            _ (println "FROM SERVER:" modifiedSentence)]
        (when (< i 10)
          (recur (inc i)))))
    (println "Disconnecting")
    (.close clientSocket)))

(defn connect [server]
  (doto (Thread. #(client-loop server)) (.start)))

(defn connect-to-server []
  (println "Connecting to server...")
  (try
    (connect {:name "localhost" :port net/UDP_PORT})
    (catch ConnectException e (println "Failed to connect to server"))))

(defn setup-network []
  (when (@entities/game :network?) (connect-to-server)))

(defn quit []
  (display/destroy)
  (System/exit 0))

(defn get-current-time []
  (/ (* (Sys/getTime) 1000) (Sys/getTimerResolution)))

(defn run-loop []
  (loop [time (get-current-time)]
    (let [loop-time (get-current-time)
          delta (- loop-time time)]
    (if (display/close-requested?)
      (quit)
      (do 
        ; do game stuff
        (display/render)
        (logic/update delta)
        (input/handle-input)
        (display/update-display)
        (recur loop-time))))))

(defn main [& rest]
  (display/setup)
  (setup-network)
  (run-loop))
