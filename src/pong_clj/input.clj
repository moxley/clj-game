(ns pong-clj.input
  (:import [org.lwjgl.input Keyboard]))

(def inputs (atom {}))
(def client-event-queue (atom []))
(def server-event-queue (atom []))

(defn parse-lwjgl-event [e now]
  {:key (:event-key e)
   :name (:key-name e)
   :state (if (true? (:event-key-state e)) :down :up)
   :time (/ (e :event-nanoseconds) 1000000)
   :record-time now})

(defn lwjgl-keyboard-events []
  (loop [events []]
    (if (Keyboard/next)
      (let [key (Keyboard/getEventKey)
            e {:event-key key
               :key-name (Keyboard/getKeyName key)
               :event-key-state (Keyboard/getEventKeyState)
               :event-nanoseconds (Keyboard/getEventNanoseconds)}]
        (recur (conj events e)))
      events)))

(defn save-keyboard-events [delta]
  (let [now (.getTime (java.util.Date.))
        events (map #(parse-lwjgl-event % now) (lwjgl-keyboard-events))]
    (swap! client-event-queue into events)
    (swap! server-event-queue into events)))

(defn key-pressed?
  ([key] (key-pressed? key @client-event-queue))
  ([key queue]
    (loop [down? false
           remaining-events queue]
      (let [e (first remaining-events)
            r (rest remaining-events)]
        (if (not e)
          down?
          (if (= (:name e) key)
            (recur (= (:state e) :down) r)
            (recur down? r)))))))

(defn key-up?
  ([] (key-up? @client-event-queue))
  ([queue] (key-pressed? "UP" queue)))

(defn key-down?
  ([] (key-down? @client-event-queue))
  ([queue] (key-pressed? "DOWN" queue)))

(defn handle-input [delta]
  (save-keyboard-events delta)
  (swap! inputs into {:key-up (key-up?) :key-down (key-down?)}))
