(ns pong-clj.core
  (:use [pong-clj.entities]
        [pong-clj.input]
        [pong-clj.display])
  (:import [org.lwjgl.opengl Display DisplayMode GL11]
           [org.lwjgl Sys]))

(defn quit []
  (Display/destroy)
  (System/exit 0))

(def game-state (atom {:score 0}))
(defn -inc-score [state] (conj state {:score (inc (state :score))}))
(defn inc-score [] (swap! game-state -inc-score))

(defn lose-point []
  ;; Pause game, display message
  (inc-score)
  (reset-ball ball)
  (println "Lost one point"))

(defn do-logic [delta]
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

(defn render []
  (GL11/glClear GL11/GL_COLOR_BUFFER_BIT)
  (draw-entity ball)
  (draw-entity paddle))

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
