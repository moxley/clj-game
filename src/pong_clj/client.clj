(ns pong-clj.client
  (:require [pong-clj.logic :as logic]
            [pong-clj.display :as display]
            [pong-clj.input :as input]
            [pong-clj.entities :as entities])
  (:import [java.net Socket]
           [java.io PrintWriter InputStreamReader BufferedReader]
           [org.lwjgl Sys]
           [java.net ConnectException]))

(defn write [conn msg]
  (doto (:out @conn)
    (.println (str msg "\r"))
    (.flush))
  (println msg))

(defn conn-handler [conn]
  (while (nil? (:exit @conn))
    (let [msg (.readLine (:in @conn))]
      (println msg)
      (cond
        (re-find #"^ERROR :Closing Link:" msg)
        (dosync (alter conn merge {:exit true}))

        (re-find #"^PING" msg)
        (write conn (str "PONG "  (re-find #":.*" msg)))

        (re-find #"^What is your name" msg)
        (do
          (write conn "Clojure"))))))

(defn connect [server]
  (let [socket (Socket. (:name server) (:port server))
        in (BufferedReader. (InputStreamReader. (.getInputStream socket)))
        out (PrintWriter. (.getOutputStream socket))
        conn (ref {:in in :out out})]
    (doto (Thread. #(conn-handler conn)) (.start))
    conn))

(defn connect-to-server []
  (println "Connecting to server...")
  (try
    (connect {:name "localhost" :port 3333})
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
