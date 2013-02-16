(ns pong-clj.input
  (:import [org.lwjgl.input Keyboard]))

(def inputs (atom {}))
(def client-event-queue (atom []))
(def server-event-queue (atom []))

(defn parse-lwjgl-event [e now]
  {:key (:event-key e)
   :name (:key-name e)
   :state (if (true? (:event-key-state e)) :up :down)
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

(defn key-up? []  (Keyboard/isKeyDown Keyboard/KEY_UP))
(defn key-down? [] (Keyboard/isKeyDown Keyboard/KEY_DOWN))

(defn handle-input [delta]
  ;;(save-keyboard-events delta)
  ;;(println "client-event-queue:" @client-event-queue)
  ;;(swap! inputs into {:key-up false :key-down false})
  ;;(println "save-keyboard-events:" (save-keyboard-events delta))
  (swap! inputs into {:key-up (key-up?) :key-down (key-down?)}))
