(ns pong-clj.entities
  (:require [pong-clj.display :as display])
  (:import [org.lwjgl.opengl GL11]))

;;-----------------------------------------------------------------------------
;;  Main game entities
(def game (atom {:score 0 :mode :playing}))

(def border {:x 0 :y 0 :width display/WIDTH :height display/HEIGHT})

(def BALL-DEFAULT-X (- (/ display/WIDTH 2) (/ 10 2)))
(def BALL-DEFAULT-Y (- (/ display/HEIGHT 2) (/ 10 2)))
(def BALL-DEFAULT-DX (* -1 display/X-SPEED))

(def ball (atom {:x BALL-DEFAULT-X
                 :y BALL-DEFAULT-Y
                 :width 10
                 :height 10
                 :dx BALL-DEFAULT-DX
                 :dy 0.1}))

(def paddle (atom {:x 10
                :y (- (/ display/HEIGHT 2) (/ 80 2))
                :width 10
                :height 80
                :dx 0.0
                :dy 0.0}))

;;-----------------------------------------------------------------------------
;; Display logic for entities

(defn draw-entity [entity]
  (let [e @entity]
    (GL11/glRectd
     (e :x)
     (e :y)
     (+ (e :x) (e :width))
     (+ (e :y) (e :height)))))

(defn update-position [entity delta]
  (let [e @entity
        x (+ (e :x) (* delta (e :dx)))
        y (+ (e :y) (* delta (e :dy)))]
    (swap! entity conj [:x x] [:y y])))

(defn update-entity [entity values]
  (swap! entity into values))

(defn reset-ball [ball]
  (update-entity ball {:x BALL-DEFAULT-X
                       :y BALL-DEFAULT-Y
                       :dx BALL-DEFAULT-DX
                       :dy 0.2}))

(defn point-collides? [point entity]
  (let [px (point :x)
        py (point :y)
        e entity
        entity-x1 (entity :x)
        entity-x2 (+ entity-x1 (entity :width))
        entity-y1 (entity :y)
        entity-y2 (+ entity-y1 (entity :height))]
    (and
     (>= px entity-x1)
     (<= px entity-x2)
     (>= py entity-y1)
     (<= py entity-y2))))

(defn collides? [e1 e2]
  ;; Check all four corners of e1
  (let [e1-x1 (e1 :x)
        e1-x2 (+ e1-x1 (e1 :width))
        e1-y1 (e1 :y)
        e1-y2 (+ e1-y1 (e1 :height))]
    (or
     (point-collides? {:x e1-x1 :y e1-y1} e2)
     (point-collides? {:x e1-x2 :y e1-y1} e2)
     (point-collides? {:x e1-x1 :y e1-y2} e2)
     (point-collides? {:x e1-x2 :y e1-y2} e2))))

;; Returns a 2-dimentional vector representing
;; which walls the ball is exiting
;; [-1 0] means the ball is exiting the left wall
;; [1 0] means the ball is exiting the right wall
;; [0 -1] top wall
;; [0 1] bottom wall
(defn ball-exits-border-at [ball border]
  (let [b ball
        b-x1 (b :x)
        b-x2 (+ b-x1 (b :width))
        b-y1 (b :y)
        b-y2 (+ b-y1 (b :height))

        r border
        r-x1 (r :x)
        r-x2 (+ r-x1 (r :width))
        r-y1 (r :y)
        r-y2 (+ r-y1 (r :height))

        wall-vx (cond
                 (<= b-x1 r-x1) -1
                 (>= b-x2 r-x2) 1
                 :else 0)
        wall-vy (cond
                 (<= b-y1 r-y1) -1
                 (>= b-y2 r-y2) 1
                 :else 0)]
    [wall-vx wall-vy]))

(defn flip-delta [entity key]
  (swap! entity conj [key (* -1 (@entity key))]))
