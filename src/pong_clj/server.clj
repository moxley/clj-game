(ns pong-clj.server
  (:use [clojure.java.io :only [reader writer]])
  (:require [pong-clj.network :as net]
             [pong-clj.input :as input]
             [pong-clj.logic :as logic]
             [pong-clj.entities :as e])
  (:import (java.util Date)
           (java.net DatagramSocket)))

(def server-state (atom {}))
(def clients (atom {}))

(defn client-from-packet [packet]
  {:addr (:addr packet) :port (:port packet) :last-packet-at (Date.)})

(defn get-or-initialize-client [packet]
  (or
    (@clients (:addr packet))
    (let [client (atom (client-from-packet packet))
          addr (:addr packet)]
      (println "New client at" (.toString addr))
      (swap! clients assoc addr client)
      client)))

(defn update-client [client props]
  (swap! client into props))

(def CLIENT_TIMEOUT 5000)

(defn timeout-clients []
  (let [t (.getTime (Date.))]
    (doseq [[addr client] @clients]
      (when (> (- t (:last-packet-at @client)) CLIENT_TIMEOUT)
        (println "Client at" (.toString addr) " timed out")
        (swap! clients dissoc addr)))))

;; Sync
;; Takes an argument for the frames-per-second
;; Keep track of last invocation
;; If there was no last invocation, sleep for default amount
;; Else, set sleep amount to current time minus last invocation time,

(def sync-state (atom {:prev-times []}))

;;(defn calc-sleep [target-iter-time prev-times]
;;  (let [cumulative-time (apply + prev-times)
;;        overage (- cumulative-time (* (count prev-times) target-iter-time))]
;;    (max (- target-iter-time overage) 0)))

;;(defn sync-fps [fps]
;;  (let [t (.getTime (Date.))
;;        target-iter-time (/ 1000.0 fps)
;;        prev-times (:prev-times @sync-state)
;;        sleep (calc-sleep target-iter-time prev-times)]
;;    ;; TODO This calculation is wrong. Absolute times are stored (as they should be). Need to calculate deltas between them
;;    (swap! sync-state conj [:prev-times (lazy-cat (rest prev-times) [t])])
;;    (when (> sleep 0)
;;      (Thread/sleep sleep))))

;; Game loop
;; Record the start time
;; Run the loop
;;   At end of loop:
;;     Figure out how much time to sleep
;;     Sleep that much
(defn timeout-loop []
  (while true
    (timeout-clients)
    (Thread/sleep 500)))

(defn create-remote [addr port socket]
  {:addr addr :port port :socket socket})

(defn state-for-client []
  {:paddle {:x (:x @e/paddle) :y (:y @e/paddle)}})

(defn periodic-update [socket]
  "Periodically update the client"
  (while true
    (doseq [[addr client] @clients]
      (let [remote (create-remote (:addr @client) (:port @client) socket)
            send-string (str (state-for-client))]
        (net/send-data remote (.getBytes send-string))))
    (Thread/sleep 500)))

(defn handle-request [sentence]
  (let [[_ key-up-str] (re-find #":key-up (\w+)" sentence)
        key-up (= "true" key-up-str)
        [_ key-down-str] (re-find #":key-down (\w+)" sentence)
        key-down (= "true" key-down-str)]
    (swap! input/inputs into {:key-up key-up :key-down key-down})
    (logic/update-paddle)
    (state-for-client)))

(defn create-server [port]
  (let [serverSocket (DatagramSocket. port)]
    (println "Listening on port" port)
    (doto (Thread. #(timeout-loop)) (.start))
    (doto (Thread. #(periodic-update serverSocket)) (.start))
    (while true
      (let [packet (net/receive-packet serverSocket)
            client (get-or-initialize-client packet)
            _ (update-client client {:last-packet-at (.getTime (Date.))})
            sentence (String. (byte-array (:payload packet)))
            _ (println (.toString (:addr packet)) ":" sentence)

            ;; Handle request
            res (handle-request sentence)
            res-str (str res)
            _ (println "res-str" res-str)

            ;; Response
            remote (create-remote (:addr packet) (:port packet) serverSocket)

            ;capitalizedSentence (.toUpperCase sentence)
            ;_ (net/send-data remote (.getBytes capitalizedSentence))

            _ (net/send-data remote (.getBytes res-str))]))))

(defn handle-packet [packet]
  (let [sentence (String. (byte-array (:payload packet)))
        capitalizedSentence (.toUpperCase sentence)]))

(defn main
  ([port]
    (create-server port))
  ([] (main net/UDP_PORT)))
