(ns pong-clj.display
  (:require [pong-clj.entities :as e])
  (:import [org.lwjgl.opengl Display DisplayMode GL11]
           [org.lwjgl LWJGLException]
           [java.awt Font]
           [org.newdawn.slick Color TrueTypeFont]))

(def font-res (atom {}))

(defn primary-font [] (:font @font-res))

(defn load-fonts []
  (let [awt-font (Font. "Arial" Font/BOLD, 24)
        font (TrueTypeFont. awt-font true)]
    (swap! font-res into {:awt-font awt-font :font font})))

(defn setup-display []
  (let [mode (new DisplayMode (@e/game :width) (@e/game :height))]
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
  (GL11/glMatrixMode GL11/GL_MODELVIEW)
  (load-fonts))

(defn setup []
  (setup-display)
  (setup-opengl))

(defn render-entities []
  (if (= (@e/game :mode) :playing)
    (e/draw-entity e/ball))
  (e/draw-entity e/paddle))

(defn render-point-pause []
  (GL11/glEnable GL11/GL_BLEND)
  (GL11/glBlendFunc GL11/GL_SRC_ALPHA GL11/GL_ONE_MINUS_SRC_ALPHA)
  (.bind Color/white)
  (.drawString (primary-font) (- (/ (@e/game :width) 2.0) 70) 50 "1 point lost" Color/white))

(defn render []
  (GL11/glClear GL11/GL_COLOR_BUFFER_BIT)
  (if (= (@e/game :mode) :point-pause)
    (render-point-pause))

  (GL11/glDisable GL11/GL_BLEND)
  (render-entities))

(defn update-display []
  (Display/update)
  (Display/sync 60))
