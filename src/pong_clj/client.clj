(ns pong-clj.client
  (:require [pong-clj.logic :as logic]
            [pong-clj.display :as display]
            [pong-clj.input :as input]
            [pong-clj.entities :as entities]
            [pong-clj.network :as net])
  (:import (org.lwjgl Sys)
           (java.net ConnectException DatagramSocket InetAddress SocketException)
           (java.util Date)))

(defn send-input [remote inputs new-inputs]
  (if (not= inputs new-inputs)
    (let [t (.getTime (Date.))
          sentence (str "time: " t ", :key-up " (:key-up new-inputs) ", :key-down " (:key-down new-inputs))
          ;;_ (println "Sending:" sentence)
          _ (net/send-data remote (.getBytes sentence))]
          ;;packet (net/receive-packet clientSocket)
          ;;modifiedSentence (String. (:payload packet))
          ;;_ (println "FROM SERVER:" modifiedSentence)
      1)
    0))

(defn pause [] (Thread/sleep (long 100)))

(defn send-state [remote]
  "Send user input state to server"
  (let [new-inputs {:key-up (input/key-up?) :key-down (input/key-down?)}]
    (send-input remote @input/inputs new-inputs)
  (pause)))

(defn loop-receive-state [remote]
  (while (:running? @entities/game)
    ;; Block and receive
    (try
      (let [packet (net/receive-packet (:socket remote))
            sentence (String. (byte-array (:payload packet)))
            data (read-string sentence)]
        (when (:paddle data)
          (swap! entities/paddle into (:paddle data))))
      (catch SocketException e nil))))

(defn client-loop [remote]
  "Run the client side network loop"
  ;; Receive state
  (doto (Thread. #(loop-receive-state remote)) (.start))
  (while (:running? @entities/game) (send-state remote))
  (println "Disconnecting")
  (.close (:socket remote)))

(defn connect-to-server []
  (println "Connecting to server...")
  (try
    (let [clientSocket (DatagramSocket.)
          server {:host "localhost" :port net/UDP_PORT}
          addr (InetAddress/getByName (:host server))
          _ (println "IPAddress:" addr)
          remote (into server {:addr addr :socket clientSocket})]
      (doto (Thread. #(client-loop remote)) (.start)))
    (catch ConnectException e (println "Failed to connect to server"))))

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
        (input/handle-input delta)
        (display/update-display)
        (recur loop-time))))))

(defn main [& rest]
  (display/setup)
  (when (@entities/game :network?) (connect-to-server))
  (swap! entities/game into {:running? true})
  (run-loop))
