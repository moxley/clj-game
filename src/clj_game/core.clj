(ns clj-game.core
  (:use [clj-game.entities]
        [clj-game.input]
        [clj-game.display]))

(import org.lwjgl.opengl.Display)
(import org.lwjgl.opengl.DisplayMode)
(import org.lwjgl.opengl.GL11)
(import org.lwjgl.Sys)

(defn quit []
  (Display/destroy)
  (System/exit 0))

(defn lose-point []
  ;; Pause game, display message
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
