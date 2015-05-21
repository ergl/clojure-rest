(ns clojure-rest.util.error)

(def status-deleted 204)
(def err-bad-request 400)
(def err-unauthorized 401)
(def err-forbidden 403)
(def err-not-found 404)
(def err-not-allowed 405)
(def err-server-error 500)
(def err-not-implemented 501)

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
(defn apply-if-present [f k [s err]]
  (if (nil? err)
    (if (s k) (f s) [s nil])
    [nil err]))


;; Turns;
;; (>?= val (f :k))
;; Into:
;; (->> [val nil] (apply-if-present f :k))
(defmacro >?= [val & fns]
  (let [fns (for [f fns] `(apply-if-present ~(first f) ~(second f)))]
    `(->> [~val nil]
          ~@fns)))


;; Turns;
;; (>?= (f val) (f' :k))
;; Into:
;; (->> (f val) (apply-if-present f' :k))
(defmacro =>?= [inif & fns]
  (let [fns (for [f fns] `(apply-if-present ~(first f) ~(second f)))]
    `(->> ~inif
          ~@fns)))

;; {} -> [{}, nil]
;; Adaptor from map to error tuple
(defn bind-to [params]
  [params nil])

;; [{}?, Error?] -> Either<{}|Error>
;; Extracts the value from the optional map
;; If there is no value present, extract the error
(defn wrap-error [[val err]]
  (if (nil? err) val err))
