(ns pong-clj.display
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
  (GL11/glEnable GL11/GL_TEXTURE_2D)
  (GL11/glShadeModel GL11/GL_SMOOTH)
  (GL11/glDisable GL11/GL_DEPTH_TEST)
  (GL11/glDisable GL11/GL_LIGHTING)

  (GL11/glClearColor (float 0), (float 0), (float 0), (float 0))
  (GL11/glClearDepth 1)

  (GL11/glMatrixMode GL11/GL_PROJECTION)
  (GL11/glLoadIdentity)
  (GL11/glOrtho 0 640 480 0 1 -1)
  (GL11/glMatrixMode GL11/GL_MODELVIEW))

(defn update-display []
  (Display/update)
  (Display/sync 60))
