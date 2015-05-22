(ns clojure-rest.util.utils)

;; fn {} -> {}
;; Applies fn to each value in {}
(defn fmap [f m]
  (into {} (for [[k v] m] [k (f v)])))

;; fn {} -> {}
;; Applies fn to each value in {}, but takes the :key as a parameter too
(defn fkmap [f m]
  (into {} (for [[k v] m] [k (f k v)])))

;; Turns:
;; (-!>> val fn1 fn2 ...)
;; Into:
;; (do (->> val fn fn2 ...) val)
;; Pipes the given params into the given functions, then returns the original params
;; Useful when using dead-end functions, like writing to a file or updating a database
(defmacro -!>> [params & fns]
  `(do
     (->> ~params ~@fns)
     ~params))


;; () -> String
;; Returns the current time in yyyy-MM-dd-HH:mm:ss
(defn time-now []
  (.format (java.text.SimpleDateFormat. "yyyy-MM-dd~HH:mm:ss") (java.util.Date.)))


;; String -> String
;; Replaces "~" in the supplied date with " "
;; "1970-01-01~00:00:00" -> "1970-01-01 00:00:00"
(defn format-time [date]
  (let [date (clojure.string/split date #"~")]
    (str (first date) " " (second date))))


;; String -> String
;; Replaces " " in the supplied date with "~"
;; "1970-01-01 00:00:00" -> "1970-01-01~00:00:00"
(defn join-time [date]
  (clojure.string/replace date #" " "~"))


;; String -> String
;; Removes extra spaces inside a string
;; "    foo      bar    " -> "foo bar"
(defn trim-inside [word]
  (->> word
       (#(clojure.string/split % #" "))
       (filter #(not (clojure.string/blank? %)))
       (clojure.string/join " ")))


;; String -> Integer
(defn parse-int [s]
  (Integer/parseInt (re-find #"\A-?\d+" s)))


;; String -> Float
(defn parse-float [s]
  (Float/parseFloat s))
