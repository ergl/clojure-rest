(ns clojure-rest.util.utils)

;; fn {} -> {}
;; Applies fn to each value in {}
(defn fmap [f m]
  (into {} (for [[k v] m] [k (f v)])))

;; Turns:
;; (|>> val fn1 fn2 ...)
;; Into:
;; (do (->> val fn fn2 ...) val)
;; Pipes the given params into the given functions, then returns the original params
;; Useful when using dead-end functions, like writing to a file or updating a database
(defmacro |>> [params & fns]
  `(do
     (->> ~params ~@fns)
     ~params))
