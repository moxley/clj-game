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

(defn do-logic [delta]
  (update-position ball delta)
  (update-position bat delta)
  ;;bat.update(delta)
  (if (collides? @ball @bat)
    (flip-delta ball)))

(defn render []
  (GL11/glClear GL11/GL_COLOR_BUFFER_BIT)
  (draw-entity ball)
  (draw-entity bat))

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
        (handle-input bat)
        (update-display)
        (recur loop-time))))))

(defn -main[]
  (setup-display)
  (setup-opengl)
  (run-loop))
