(defproject clj-game "0.1.0-SNAPSHOT"
  :description "A pong game... in clojure!"
  :url "https://github.com/moxley/clj-game"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.lwjgl/lwjgl "2.7.1"]
                 [org.lwjgl/lwjgl-util "2.7.1"]
                 [org.lwjgl/lwjgl-native-platform "2.7.1"]]
  :main clj-game.core)
