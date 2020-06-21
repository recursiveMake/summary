(ns summary.utils
  (:import [java.lang.Class]))

(defn construct [klass & args]
  "Constructs instance dynamically"
  ;; https://stackoverflow.com/questions/9167457/in-clojure-how-to-use-a-java-class-dynamically
  ;; Does not work when args are subclasses
  (.newInstance
    (.getConstructor klass (into-array java.lang.Class (map type args)))
    (object-array args)))
