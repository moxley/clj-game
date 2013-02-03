(ns pong-clj.network)

(def UDP_PORT 9876)
(def APP_PROTOCOL 0xF4F4F4F4)
(def HEADER_LENGTH 4)

(defn unsigned-to-byte [v]
  (byte
    (if (> v Byte/MAX_VALUE)
      (* -1 (- v Byte/MAX_VALUE))
      v)))

(def PROTOCOL_BYTES
  (vec (map #(unsigned-to-byte (bit-and 0xFF (bit-shift-right APP_PROTOCOL %))) [24 16 8 0])))

(defn pack-packet [data-seq]
  (let [data          (vec data-seq)
        data-length   (count data)
        packet        (byte-array (+ data-length HEADER_LENGTH))]
    (loop [i 0]
      (when (< i (count PROTOCOL_BYTES))
        (aset packet i (nth PROTOCOL_BYTES i))
        (recur (inc i))))
    (loop [i 0]
      (when (< i data-length)
        (aset packet (+ HEADER_LENGTH i) (unsigned-to-byte (nth data i)))
        (recur (inc i))))
    packet))

(defn protocol-packet? [p]
  (and
    (>= (alength p) HEADER_LENGTH)
    (every? identity (map #(= (aget p %) (nth PROTOCOL_BYTES %)) (range 0 (count PROTOCOL_BYTES))))))

(defn unpack-packet [packet]
  (let [proto APP_PROTOCOL
        payload  (vec (map #(aget packet %) (range HEADER_LENGTH (alength packet))))]
    {:payload payload}))
