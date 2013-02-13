(ns pong-clj.input
  (:import [org.lwjgl.input Keyboard]))

(def inputs (atom {}))
(def client-event-queue (atom []))
(def server-event-queue (atom []))

(defn save-keyboard-events [delta]
            ;; milliseconds
  (let [now (.getTime (java.util.Date.))]
    (loop [events []]
      (if (Keyboard/next)
        (let [key (Keyboard/getEventKey)
              name (Keyboard/getKeyName key)
              down (Keyboard/getEventKeyState)
              ;; time is in milliseconds
              time (/ (Keyboard/getEventNanoseconds) 1000000)]
          (recur (conj events {:key key :name name :down down :time time :record-time now})))
        (do
          (swap! client-event-queue into events)
          (swap! server-event-queue into events))))))

(defn key-up? []  (Keyboard/isKeyDown Keyboard/KEY_UP))
(defn key-down? [] (Keyboard/isKeyDown Keyboard/KEY_DOWN))

(defn handle-input [delta]
  ;;(save-keyboard-events delta)
  ;;(println "client-event-queue:" @client-event-queue)
  ;;(swap! inputs into {:key-up false :key-down false})
  (swap! inputs into {:key-up (key-up?) :key-down (key-down?)}))
