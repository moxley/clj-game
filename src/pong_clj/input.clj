(ns pong-clj.input
  (:require [pong-clj.entities :as e])
  (:import [org.lwjgl.input Keyboard]))

(def PADDLE-SPEED 0.35)

(defn key-up? []  (Keyboard/isKeyDown Keyboard/KEY_UP))
(defn key-down? [] (Keyboard/isKeyDown Keyboard/KEY_DOWN))

(defn paddle-at-border? []
  (or (and (<= (:y @e/paddle) 0)
           (key-up?))
      (and (>= (+ (:y @e/paddle) (:height @e/paddle))
               (:height e/border))
           (key-down?))))

(defn handle-input
  ([paddle]
    (let [dy (cond (key-up?)   (* PADDLE-SPEED -1)
                   (key-down?) PADDLE-SPEED
                   :else 0)]
      (if (paddle-at-border?)
        (e/update-entity paddle {:dy 0})
        (e/update-entity paddle {:dy dy}))))
  ([] (handle-input e/paddle)))
