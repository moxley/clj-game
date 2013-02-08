(ns pong-clj.logic
  (:require [pong-clj.entities :as e]
            [pong-clj.input :as input]
            [clj-time.core :as t]))

(def PADDLE-SPEED 0.35)

(defn -inc-score [state] (conj state {:score (inc (state :score))}))
(defn inc-score [] (swap! e/game -inc-score))

(defn set-pause [state] (into state {:mode :point-pause :paused-at (t/now)}))
(defn start-point-pause []
  (swap! e/game set-pause))

(defn lose-point []
  ;; Pause game, display message
  (inc-score)
  (start-point-pause))

(defn paddle-at-border? []
  (or (and (<= (:y @e/paddle) 0)
           (:key-up @input/inputs))
      (and (>= (+ (:y @e/paddle) (:height @e/paddle))
               (:height e/border))
           (:key-down @input/inputs))))

(defn update-paddle []
  (let [dy (cond (:key-up @input/inputs) (* PADDLE-SPEED -1)
                 (:key-down @input/inputs) PADDLE-SPEED
                 :else 0)]
    (if (paddle-at-border?)
      (e/update-entity e/paddle {:dy 0})
      (e/update-entity e/paddle {:dy dy}))))

(defn update-ball [delta]
  (e/update-position e/ball delta)
  (e/update-position e/paddle delta)
  (if (e/collides? @e/ball @e/paddle) ;; Collides with paddle
    (do 
      (e/flip-delta e/ball :dx)
      (swap! e/ball conj  [:dx (+ (:dx @e/ball) 0.04)])) ;; muahaha, accelerate!

    (let [[exit-x exit-y] (e/ball-exits-border-at @e/ball e/border)]
      (cond
       ;; Goes past paddle (at left wall); point lost
       (< exit-x 0)
       (lose-point)

       ;; Hits right wall
       (> exit-x 0)
       (e/flip-delta e/ball :dx)

       ;; Hits horizontal walls
       (not (zero? exit-y))
       (e/flip-delta e/ball :dy)))))

(defn do-playing-logic [delta]
  (update-paddle)
  (update-ball delta))

(defn unpause []
  (swap! e/game into {:paused-at nil :mode :playing}))

(defn unpause-point []
  (e/reset-ball e/ball)
  (unpause))

(defn do-point-pause-logic [delta]
  (if (t/after? (t/now) (t/plus (@e/game :paused-at) (t/secs 3)))
    (unpause-point)))

(defn update [delta]
  (cond
    (= (@e/game :mode) :point-pause)
    (do-point-pause-logic delta)

    :else ; playing
    (do-playing-logic delta)))
