(ns clj-game.core)
(import org.lwjgl.opengl.Display)
(import org.lwjgl.opengl.DisplayMode)
(import org.lwjgl.opengl.GL11)

(def WIDTH 640)
(def HEIGHT 480)

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

(defn set-up-entities []
  ;ball = new Ball(WIDTH / 2 - 10 / 2, HEIGHT / 2 - 10 / 2, 10, 10);
  (def ball (atom {:x (- (/ WIDTH 2) (/ 10 2))
                   :y (- (/ HEIGHT 2) (/ 10 2))
                   :width 10
                   :height 10
                   :dx -0.1}))

  ;bat = new Bat(10, HEIGHT / 2 - 80 / 2, 10, 80);
  (def bat (atom {:x 10
                  :y (- (/ HEIGHT 2) (/ 80 2))
                  :width 10
                  :height 80
                  :dx 0})))

(defn render []
  (GL11/glClear GL11/GL_COLOR_BUFFER_BIT)
  (draw-entity ball)
  (draw-entity bat))

(defn get-current-time [] (.getTime (java.util.Date.)))

(defn run-loop []
  (loop [time (get-current-time)]
    (let [loop-time (get-current-time)
          delta (- loop-time time)]
    (if (Display/isCloseRequested)
      (quit)
      (do 
        ; do game stuff
        (println "delta: " delta)
        (render)
        (update-display)
        (recur loop-time))))))

(defn -main[]
  (set-up-entities)
  (setup-display)
  (setup-opengl)
  (run-loop))
