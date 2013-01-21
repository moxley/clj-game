(ns clj-game.display
  (:import [org.lwjgl.opengl Display DisplayMode GL11]
           [org.lwjgl LWJGLException]))

(def WIDTH 640)
(def HEIGHT 480)
(def X-SPEED 0.15)

(defn setup-display []
  (let [mode (new DisplayMode WIDTH HEIGHT)]
    (try
      (do
        (Display/setDisplayMode mode)
        (Display/setTitle "Pong")
        (Display/create))
      (catch LWJGLException e
        (do
          (.printStackTrace e)
          (Display/destroy)
          (System/exit 1))))))

(defn setup-opengl []
  (GL11/glMatrixMode GL11/GL_PROJECTION)
  (GL11/glLoadIdentity)
  (GL11/glOrtho 0 640 480 0 1 -1)
  (GL11/glMatrixMode GL11/GL_MODELVIEW))

(defn update-display []
  (Display/update)
  (Display/sync 60))
