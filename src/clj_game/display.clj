(ns clj-game.display)

(import org.lwjgl.opengl.Display)
(import org.lwjgl.opengl.DisplayMode)
(import org.lwjgl.opengl.GL11)
;;(import org.lwjgl.Sys)


(def WIDTH 640)
(def HEIGHT 480)
(def X-SPEED 0.15)

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

(defn update-display []
  (Display/update)
  (Display/sync 60))
