(ns pong-clj.core
  (:use [pong-clj.entities]
        [pong-clj.input]
        [pong-clj.display])
  (:require [clj-time.core :as t])
  (:import [org.lwjgl.opengl Display DisplayMode GL11]
           [org.lwjgl Sys]))

(defn quit []
  (Display/destroy)
  (System/exit 0))

(def game-state (atom {:score 0 :mode :playing}))

(defn -inc-score [state] (conj state {:score (inc (state :score))}))
(defn inc-score [] (swap! game-state -inc-score))

(defn set-pause [state] (into state {:mode :point-pause :paused-at (t/now)}))
(defn start-point-pause []
  (swap! game-state set-pause))

(defn lose-point []
  ;; Pause game, display message
  (inc-score)
  (start-point-pause))

(defn do-playing-logic [delta]
  (update-position ball delta)
  (update-position paddle delta)
  (if (collides? @ball @paddle) ;; Collides with paddle
    (flip-delta ball :dx)

    (let [[exit-x exit-y] (ball-exits-border-at @ball border)]
      (cond
       ;; Goes past paddle (at left wall); point lost
       (< exit-x 0)
       (lose-point)

       ;; Hits right wall
       (> exit-x 0)
       (flip-delta ball :dx)

       ;; Hits horizontal walls
       (not (zero? exit-y))
       (flip-delta ball :dy)))))

(defn unpause []
  (swap! game-state into {:paused-at nil :mode :playing}))

(defn unpause-point []
  (reset-ball ball)
  (unpause))

(defn do-point-pause-logic [delta]
  (if (t/after? (t/now) (t/plus (@game-state :paused-at) (t/secs 3)))
    (unpause-point)))

(defn do-logic [delta]
  (cond
    (= (@game-state :mode) :point-pause)
    (do-point-pause-logic delta)

    :else ; playing
    (do-playing-logic delta)))

(defn render-entities []
  (draw-entity ball)
  (draw-entity paddle))

(defn render-point-pause [])

(defn render []
  (GL11/glClear GL11/GL_COLOR_BUFFER_BIT)
  (if (= (@game-state :mode) :point-pause)
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
        (do-logic delta)
        (handle-input paddle)
        (update-display)
        (recur loop-time))))))

(defn -main[]
  (setup-display)
  (setup-opengl)
  (run-loop))
