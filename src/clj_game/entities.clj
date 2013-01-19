(ns clj-game.entities
  (:use [clj-game.display]))

(import org.lwjgl.opengl.GL11)

;;-----------------------------------------------------------------------------
;;  Main game entities

(def ball (atom {:x (- (/ WIDTH 2) (/ 10 2))
                 :y (- (/ HEIGHT 2) (/ 10 2))
                 :width 10
                 :height 10
                 :dx (* -1 X-SPEED)
                 :dy 0.0}))

(def bat (atom {:x 10
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

(defn collides? [e1 e2]
  (and
   (<= (e1 :x) (+ (e2 :x) (e2 :width)))
   (>= (e1 :x) (e2 :x))
   (>= (e1 :y) (e2 :y))
   (<= (e1 :y) (+ (e2 :y) (e2 :height)))))

(defn flip-delta [entity]
  (swap! entity conj [:dx (* -1 (@entity :dx))]))
