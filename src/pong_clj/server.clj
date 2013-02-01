(ns pong-clj.server
  (:use [clojure.java.io :only [reader writer]]
        [server.socket :only [create-server]])
  (:import (java.net Socket)
           (java.io PrintWriter InputStreamReader BufferedReader)))

(defn- write [conn & rest]
  (let [msg (apply str (or rest ""))]
    (print msg)
    (.print (:out conn) msg)
    (.flush (:out conn))
    msg))

(defn- writeln [conn & rest]
  (let [msg (apply str (or rest ""))]
    (write conn (str msg "\n"))))

(defn- prompt [conn]
  (write conn "> ")
  (let [line (.readLine (:in conn))]
    (println line)
    line))

(defn- greeting-game [conn]
  (writeln conn "What is your name?")
  (let [player-name (prompt conn)]
    (writeln conn "Hello, " player-name)
    (.flush (:error conn))))

(defn- handle-client [in out]
  (let [error (writer System/err)
        conn {:in (BufferedReader. (InputStreamReader. in))
              :out (PrintWriter. out)
              :error error}]
    (writeln conn)
    (greeting-game conn)
    (loop [input (prompt conn)]
      (when input
        (writeln conn "You said: " input)
        (.flush error)
        (recur (prompt))))
    (println "Client disconnected")))

(defn main
  ([port]
    (println "Starting server...")
    (defonce server (create-server (Integer. port) handle-client))
    (println "Listening on port" port))
  ([] (main 3333)))
