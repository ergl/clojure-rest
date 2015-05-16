(ns clojure-rest.util.error)

;; Error handling with a tuple [Value Error]

;; (f [{}?, Error?]) -> Either<(f val)|[nil Error]>
;; Either execute (f val) or bind the error
;; If the error is present in the signature
(defn bind-error [f [val err]]
  (if (nil? err)
    (f val)
    [nil err]))


;; Turns:
;; (>>= (f val) g)
;; Into:
;; (->> (f val) (bind-error g))
(defmacro >>= [val & fns]
  (let [fns (for [f fns] `(bind-error ~f))]
    `(->> [~val nil]
          ~@fns)))


;; Turns:
;; (=>>= (f val) g)
;; Into:
;; (->> (bind-error (f val)) (bind-error g))
(defmacro =>>= [inif & fns]
  (let [fns (for [f fns] `(bind-error ~f))]
    `(->> ~inif
          ~@fns)))


;; f k [{}?, Err?] -> Either<(f {})|[nil Error]|[s nil]>
;; Execute (f s) if k is present in s, return [s nil] otherwise
;; If there is an error coming in, just propagate the error
;; TODO: Figure out a macro
(defn apply-if-present [f k [s err]]
  (if (nil? err)
    (if (s k) (f s) [s nil])
    [nil err]))


;; {} -> [{}, nil]
;; Adaptor from map to error tuple
(defn bind-to [params]
  [params nil])

;; [{}?, Error?] -> Either<{}|Error>
;; Extracts the value from the optional map
;; If there is no value present, extract the error
(defn wrap-error [[val err]]
  (if (nil? err) val err))
