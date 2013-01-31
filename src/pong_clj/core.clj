(ns pong-clj.core
  (:require [pong-clj.logic :as logic]
            [pong-clj.display :as d]
            [pong-clj.input :as input])
  (:import [org.lwjgl.opengl Display DisplayMode GL11]
           [org.lwjgl Sys]
           [org.newdawn.slick Color]))

(defn quit []
  (Display/destroy)
  (System/exit 0))

(defn get-current-time []
  (/ (* (Sys/getTime) 1000) (Sys/getTimerResolution)))

(defn run-loop []
  (loop [time (get-current-time)]
    (let [loop-time (get-current-time)
          delta (- loop-time time)]
    (if (Display/isCloseRequested)
      (quit)
      (do 
        ; do game stuff
        (d/render)
        (logic/update delta)
        (input/handle-input)
        (d/update-display)
        (recur loop-time))))))

(defn -main[]
  (d/setup)
  (run-loop))
