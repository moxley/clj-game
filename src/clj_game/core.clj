(ns clj-game.core)
(import org.lwjgl.opengl.Display)
(import org.lwjgl.opengl.DisplayMode)
(import org.lwjgl.opengl.GL11)

(defn setup-display []
  (let [mode (new DisplayMode 640 480)]
    (Display/setDisplayMode mode)
    (Display/setTitle "Pong")
    (Display/create)))

(defn setup-opengl []
  (GL11/glMatrixMode GL11/GL_PROJECTION)
  (GL11/glLoadIdentity)
  (GL11/glOrtho 0 640 480 0 1 -1)
  (GL11/glMatrixMode GL11/GL_MODELVIEW))

(defn -main[]
  (setup-display)
  (setup-opengl))
