(ns pong-clj.core
  (:require [pong-clj.logic :as logic]
            [pong-clj.display :as display]
            [pong-clj.input :as input])
  (:import [org.lwjgl Sys]))

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

(defn -main[]
  (display/setup)
  (run-loop))
