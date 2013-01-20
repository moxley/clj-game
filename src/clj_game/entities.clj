(ns clj-game.entities
  (:use [clj-game.display]))

(import org.lwjgl.opengl.GL11)

;;-----------------------------------------------------------------------------
;;  Main game entities
(def border {:x 0 :y 0 :width WIDTH :height HEIGHT})

(def BALL-DEFAULT-X (- (/ WIDTH 2) (/ 10 2)))
(def BALL-DEFAULT-Y (- (/ HEIGHT 2) (/ 10 2)))
(def BALL-DEFAULT-DX (* -1 X-SPEED))

(def ball (atom {:x BALL-DEFAULT-X
                 :y BALL-DEFAULT-Y
                 :width 10
                 :height 10
                 :dx BALL-DEFAULT-DX
                 :dy 0.0}))

(def paddle (atom {:x 10
                :y (- (/ HEIGHT 2) (/ 80 2))
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
                       :dx BALL-DEFAULT-DX}))

(defn collides? [e1 e2]
  (and
   (<= (e1 :x) (+ (e2 :x) (e2 :width)))
   (>= (e1 :x) (e2 :x))
   (>= (e1 :y) (e2 :y))
   (<= (e1 :y) (+ (e2 :y) (e2 :height)))))

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
