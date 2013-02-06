(ns pong-clj.client
  (:require [pong-clj.logic :as logic]
            [pong-clj.display :as display]
            [pong-clj.input :as input]
            [pong-clj.entities :as entities]
            [pong-clj.network :as net])
  (:import (org.lwjgl Sys)
           (java.net ConnectException DatagramSocket InetAddress)
           (java.util Date)))

(defn send-input-diff [inputs new-inputs remote]
  (if (not= inputs new-inputs)
    (let [t (.getTime (Date.))
          sentence (str "time: " t ", :key-up " (:key-up new-inputs) ", :key-down " (:key-down new-inputs))
          _ (println "Sending:" sentence)
          _ (net/send-data remote (.getBytes sentence))]
          ;;packet (net/receive-packet clientSocket)
          ;;modifiedSentence (String. (:payload packet))
          ;;_ (println "FROM SERVER:" modifiedSentence)
      1)
    0))

(defn pause [] (Thread/sleep (long 100)))
(def inputs (atom {}))

(defn iteration [remote]
  (let [new-inputs {:key-up (input/key-up?) :key-down (input/key-down?)}]
    (send-input-diff @inputs new-inputs remote)
    (swap! inputs into new-inputs))
  (pause))

(defn client-loop [conn]
  (let [clientSocket (DatagramSocket.)
        addr (InetAddress/getByName (:host conn))
        _ (println "IPAddress:" addr)
        remote (into conn {:addr addr :socket clientSocket})]
    (while (:running? @entities/game) (iteration remote))
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
  (swap! entities/game into {:running? false})
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
  (swap! entities/game into {:running? true})
  (run-loop))
