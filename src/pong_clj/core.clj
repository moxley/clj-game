(ns pong-clj.core
  (:use [pong-clj.entities :as e]
        [pong-clj.input]
        [pong-clj.display])
  (:require [pong-clj.logic :as logic])
  (:import [org.lwjgl.opengl Display DisplayMode GL11]
           [org.lwjgl Sys]))

(defn quit []
  (Display/destroy)
  (System/exit 0))

(defn render-entities []
  (draw-entity e/ball)
  (draw-entity e/paddle))

(defn render-point-pause [])

(defn render []
  (GL11/glClear GL11/GL_COLOR_BUFFER_BIT)
  (if (= (@e/game :mode) :point-pause)
    (render-point-pause))

  (render-entities))

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
        (render)
        (logic/update delta)
        (handle-input paddle)
        (update-display)
        (recur loop-time))))))

(defn -main[]
  (setup-display)
  (setup-opengl)
  (run-loop))
