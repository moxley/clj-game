(ns clj-game.core)
(import org.lwjgl.opengl.Display)
(import org.lwjgl.opengl.DisplayMode)

(defn setup-display []
  (let [mode (new DisplayMode 640 480)]
    (Display/setDisplayMode mode)
    (Display/setTitle "Pong")
    (Display/create)))
