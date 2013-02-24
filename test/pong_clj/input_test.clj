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
  (let [event-time 100
        read-time  200]

    (testing "(transform-event)"
      (let [caller :client
            event {:server-read-at read-time}]
        (testing "it returns nil if both callers have read it"
          (let [t-event (transform-event event caller event-time)]
            (is (nil? t-event))))
        (testing "it returns an event if only one caller has read it"
          (let [event {:time event-time}
                expected (assoc event :client-read-at read-time)
                t-event (transform-event event caller read-time)]
            (is (= expected t-event))))))

    (testing "(keep-unprocessed-events)"
      (testing "it filters out completely read events"
        (let [queue [{:client-read-at event-time :server-read-at event-time}]]
          (is (= [] (keep-unprocessed-events queue)))))
      (testing "it keeps unread events"
        (let [queue [{:time event-time}]]
          (is (= queue (keep-unprocessed-events queue)))))
      (testing "it keeps partially-read events"
        (let [queue [{:client-read-at event-time}]]
          (is (= queue (keep-unprocessed-events queue)))))
        (let [queue [{:server-read-at event-time}]]
          (is (= queue (keep-unprocessed-events queue)))))

    (testing "(keyboard-events-for)"
      (testing "only client reads"
        (let [queue-data [{:time event-time}]
              queue       (atom queue-data)
              events      (keyboard-events-for :client read-time queue)]
          (testing "returned events"
            (is (= queue-data events)))
          (testing "queue data"
            (is (= queue-data events))))))

      (testing "removes events that have been read by both callers"
        (let [queue-data [{:time event-time :server-read-at 101}]
              queue      (atom queue-data)
              events     (keyboard-events-for :client read-time queue)]
          (testing "queue data"
            (is (= [] @queue)))))
    nil))
