(ns pong-clj.server
  (:use [clojure.java.io :only [reader writer]])
  (:require [pong-clj.network :as net])
  (:import (java.util Date)
           (java.net DatagramSocket)))

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

(defn calc-sleep [target-iter-time prev-times]
  (let [cumulative-time (apply + prev-times)
        overage (- cumulative-time (* (count prev-times) target-iter-time))]
    (max (- target-iter-time overage) 0)))

(defn sync-fps [fps]
  (let [t (.getTime (Date.))
        target-iter-time (/ 1000.0 fps)
        prev-times (:prev-times @sync-state)
        sleep (calc-sleep target-iter-time prev-times)]
    ;; TODO This calculation is wrong. Absolute times are stored (as they should be). Need to calculate deltas between them
    (swap! sync-state conj [:prev-times (lazy-cat (rest prev-times) [t])])
    (when (> sleep 0)
      (Thread/sleep sleep))))

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

(defn create-server [port]
  (let [serverSocket (DatagramSocket. port)]
    (println "Listening on port" port)
    (doto (Thread. #(timeout-loop)) (.start))
    (while true
      (let [packet (net/receive-packet serverSocket)
            client (get-or-initialize-client packet)
            _ (update-client client {:last-packet-at (.getTime (Date.))})
            sentence (String. (byte-array (:payload packet)))
            _ (println (.toString (:addr packet)) ":" sentence)
            remote {:addr (:addr packet) :port (:port packet) :socket serverSocket}
            capitalizedSentence (.toUpperCase sentence)
            sendData (.getBytes capitalizedSentence)
            _ (net/send-data remote (.getBytes capitalizedSentence))]))))

(defn handle-packet [packet]
  (let [sentence (String. (byte-array (:payload packet)))
        capitalizedSentence (.toUpperCase sentence)]))

(defn main
  ([port]
    (create-server port))
  ([] (main net/UDP_PORT)))
