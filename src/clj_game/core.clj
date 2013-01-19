(ns clj-game.core)
(import org.lwjgl.opengl.Display)
(import org.lwjgl.opengl.DisplayMode)
(import org.lwjgl.opengl.GL11)
(import org.lwjgl.Sys)

(def WIDTH 640)
(def HEIGHT 480)
(def X_SPEED 0.15)

(def ball (atom {:x (- (/ WIDTH 2) (/ 10 2))
                 :y (- (/ HEIGHT 2) (/ 10 2))
                 :width 10
                 :height 10
                 :dx (* -1 X_SPEED)
                 :dy 0.0}))

(def bat (atom {:x 10
                :y (- (/ HEIGHT 2) (/ 80 2))
                :width 10
                :height 80
                :dx 0.0
                :dy 0.0}))


(defn setup-display []
  (let [mode (new DisplayMode WIDTH HEIGHT)]
    (Display/setDisplayMode mode)
    (Display/setTitle "Pong")
    (Display/create)))

(defn setup-opengl []
  (GL11/glMatrixMode GL11/GL_PROJECTION)
  (GL11/glLoadIdentity)
  (GL11/glOrtho 0 640 480 0 1 -1)
  (GL11/glMatrixMode GL11/GL_MODELVIEW))

(defn quit []
  (Display/destroy)
  (System/exit 0))

(defn update-display []
  (Display/update)
  (Display/sync 60))

(defn draw-entity [entity]
  (let [e @entity]
    (GL11/glRectd
     (e :x)
     (e :y)
     (+ (e :x) (e :width))
     (+ (e :y) (e :height)))))

(defn update-entity [entity delta]
  (let [e @entity
        x (+ (e :x) (* delta (e :dx)))
        y (+ (e :y) (* delta (e :dy)))]
    (swap! entity conj [:x x] [:y y])))

(defn flip-delta [entity]
  (swap! entity conj [:dx (* -1 (@entity :dx))]))

(defn collides? [e1 e2]
  (and
   (<= (e1 :x) (+ (e2 :x) (e2 :width)))
   (>= (e1 :x) (e2 :x))
   (>= (e1 :y) (e2 :y))
   (<= (e1 :y) (+ (e2 :y) (e2 :height)))))

(defn do-logic [delta]
  (update-entity ball delta)
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
        (update-display)
        (recur loop-time))))))

(defn -main[]
  (setup-display)
  (setup-opengl)
  (run-loop))
