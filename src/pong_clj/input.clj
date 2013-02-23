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

(defn now [] (.getTime (java.util.Date.)))

(defn latest-keyboard-events
  ([] (latest-keyboard-events (now)))
  ([current-time] (map #(parse-lwjgl-event % current-time) (lwjgl-keyboard-events))))

(defn save-keyboard-events [delta]
  (let [events (latest-keyboard-events)]
    (swap! client-event-queue into events)
    (swap! server-event-queue into events)
    events))

(defn event-processed? [event]
  (and (:client-read-at event) (:server-read-at event)))

(defn keep-unprocessed-events [queue]
  (keep (fn [e] (if (event-processed? e) nil e)) queue))

(defn keyboard-events-for
  ([caller]
    (keyboard-events-for caller (now)))
  ([caller current-time]
    (keyboard-events-for caller current-time (latest-keyboard-events current-time)))
  ([caller current-time queue]
    (let [ret-data @queue]
      (swap! queue (fn [q] (map #(assoc % :client-read-at current-time) q)))
      ret-data)))

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
