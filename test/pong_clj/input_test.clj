(ns pong-clj.input-test
  (:use clojure.test
        pong-clj.input))

(defn select [f s]
  (keep #(if (f %) % nil) s))

(defn down-events [queue]
  (select #(= "DOWN" (:name %)) queue))

(deftest a-test
  (let [event-queue [{:key 205, :name "RIGHT", :state :down, :time 1361001074800, :record-time 1361001074808}
                     {:key 205, :name "RIGHT", :state :up, :time 1361001074808, :record-time 1361001074824}
                     {:key 205, :name "RIGHT", :state :down, :time 1361001074901, :record-time 1361001074908}
                     {:key 205, :name "RIGHT", :state :up, :time 1361001075057, :record-time 1361001075073}
                     {:key 208, :name "DOWN", :state :down, :time 1361001075085, :record-time 1361001075092}
                     {:key 205, :name "RIGHT", :state :down, :time 1361001075131, :record-time 1361001075157}
                     {:key 205, :name "RIGHT", :state :up, :time 1361001075357, :record-time 1361001075374}
                     {:key 208, :name "DOWN", :state :up, :time 1361001075523, :record-time 1361001075540}
                     {:key 200, :name "UP", :state :down, :time 1361001075793, :record-time 1361001076542}
                     ;;{:key 200, :name "UP", :state :up, :time 1361001076125, :record-time 1361001076542}
                     {:key 219, :name "LWIN", :state :down, :time 1361001076809, :record-time 1361001076828}]]
    (testing "(key-down?) should be true"
      (let [state (key-down? event-queue)]
        (is (false? state))))
    (testing "(key-up?) should be false"
      (let [state (key-up? event-queue)]
        (is (true? state))))))

(deftest event-queue
  (testing "(keyboard-events-for)"
    (let [event {:time 1234}
          queue-data [event]
          queue (atom queue-data)
          events (keyboard-events-for :client queue)]
      (is (= queue-data events))
      (is (= (assoc event :client-read-at 123) (first @queue))))))
