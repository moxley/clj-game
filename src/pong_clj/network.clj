(ns pong-clj.network
  (:import (java.net DatagramSocket DatagramPacket InetAddress)))

(def UDP_PORT 9877)
(def APP_PROTOCOL 0xF4F4F4F4)
(def HEADER_LENGTH 4)

(defn unsigned-to-byte [v]
  (byte
    (if (> v Byte/MAX_VALUE)
      (* -1 (- v Byte/MAX_VALUE))
      v)))

(def PROTOCOL_BYTES
  (vec (map #(unsigned-to-byte (bit-and 0xFF (bit-shift-right APP_PROTOCOL %))) [24 16 8 0])))

(defn identity-args [& args] args)

(defn pack-packet [payload-bytes]
  (let [payload-length  (alength payload-bytes)
        packet-bytes    (byte-array (+ HEADER_LENGTH payload-length))
        indexed-bytes   (map-indexed identity-args (lazy-cat PROTOCOL_BYTES (seq payload-bytes)))]
    (doseq [[i byte] indexed-bytes] (aset packet-bytes i byte))
    {:bytes          packet-bytes
     :length         (alength packet-bytes)
     :payload        payload-bytes
     :payload-length payload-length}))

(defn protocol-packet? [p]
  (and
    (>= (alength p) HEADER_LENGTH)
    (every? identity (map #(= (aget p %) (nth PROTOCOL_BYTES %)) (range 0 (count PROTOCOL_BYTES))))))

(defn unpack-packet [packet-bytes]
  (let [proto          APP_PROTOCOL
        packet-length  (alength packet-bytes)
        payload        (byte-array (map #(aget packet-bytes %) (range HEADER_LENGTH packet-length)))]
    {:payload        payload
     :payload-length (alength payload)
     :bytes          packet-bytes}))

(defn send-data [remote data-bytes]
  (let [packet (pack-packet data-bytes)
        udp-packet (DatagramPacket. (:bytes packet) (:length packet) (:addr remote) (:port remote))]
    (.send (:socket remote) udp-packet)))

(defn receive-packet [socket]
  (let [receiveData   (byte-array 1024)
        receivePacket (DatagramPacket. receiveData (alength receiveData))]
    (loop []
      (.receive socket receivePacket)
      (let [raw-packet (.getData receivePacket)]
        (if (protocol-packet? raw-packet)
          (into (unpack-packet raw-packet) {:addr (.getAddress receivePacket) :port (.getPort receivePacket)})
          (recur))))))
