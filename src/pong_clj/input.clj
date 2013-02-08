(ns pong-clj.input
  (:import [org.lwjgl.input Keyboard]))

(def inputs (atom {}))

(defn key-up? []  (Keyboard/isKeyDown Keyboard/KEY_UP))
(defn key-down? [] (Keyboard/isKeyDown Keyboard/KEY_DOWN))

(defn handle-input []
  (swap! inputs into {:key-up (key-up?) :key-down (key-down?)}))
