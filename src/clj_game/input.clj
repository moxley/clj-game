(ns clj-game.input
  (:use [clj-game.entities :only [update-entity]])
  (:import [org.lwjgl.input Keyboard]))

(defn handle-input [paddle]
  (let [dy (cond
           (Keyboard/isKeyDown Keyboard/KEY_UP) -0.2
           (Keyboard/isKeyDown Keyboard/KEY_DOWN) 0.2
           :else 0)]
    (update-entity paddle {:dy dy})))
