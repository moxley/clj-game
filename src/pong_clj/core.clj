(ns pong-clj.core
  (:use [pong-clj.entities :as e]
        [pong-clj.input]
        [pong-clj.display :as d])
  (:require [pong-clj.logic :as logic])
  (:import [org.lwjgl.opengl Display DisplayMode GL11]
           [org.lwjgl Sys]
           [java.awt Font]
           [org.newdawn.slick Color TrueTypeFont]))

(defn quit []
  (Display/destroy)
  (System/exit 0))

(defn render-entities []
  (if (= (@e/game :mode) :playing)
    (draw-entity e/ball))
  (draw-entity e/paddle))

(def font-res (atom {}))

(defn load-fonts []
  (let [awt-font (Font. "Arial" Font/BOLD, 24)
        font (TrueTypeFont. awt-font true)]
    (swap! font-res into {:awt-font awt-font :font font})))

(defn render-point-pause []
  (GL11/glEnable GL11/GL_BLEND)
  (GL11/glBlendFunc GL11/GL_SRC_ALPHA GL11/GL_ONE_MINUS_SRC_ALPHA)
  (.bind Color/white)
  (.drawString (@font-res :font) (- (/ d/WIDTH 2.0) 70) 50 "1 point lost" Color/white))

(defn render []
  (GL11/glClear GL11/GL_COLOR_BUFFER_BIT)
  (if (= (@e/game :mode) :point-pause)
    (render-point-pause))

  (GL11/glDisable GL11/GL_BLEND)
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
        (d/update-display)
        (recur loop-time))))))

(defn -main[]
  (setup-display)
  (setup-opengl)
  (load-fonts)
  (run-loop))
