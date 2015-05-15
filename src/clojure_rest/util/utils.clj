(ns clojure-rest.util.utils)

;; fn {} -> {}
;; Applies fn to each value in {}
(defn fmap [f m]
  (into {} (for [[k v] m] [k (f v)])))
