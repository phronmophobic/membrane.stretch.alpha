(ns membrane.alpha.stretch
  (:require [membrane.ui :as ui]))

(defn space-around
  "Given a container-length and lengths of child elements,
  return a sequence of offsets so that extra space is
  distributed evenly around the child elements."
  [container-length lengths]
  (let [extra-space (- container-length
                       (reduce + lengths))
        _ (assert (not (neg? extra-space))
                  "No extra space for space around.")
        pad (/ extra-space
               (count lengths)
               2)]

    (loop [offsets []
           lengths (seq lengths)
           offset 0]
      (if-not lengths
        offsets
        (let [length (first lengths)
              current-offset (+ offset pad)]
          (recur (conj offsets current-offset)
                 (next lengths)
                 (+ current-offset length pad)))))))

(defn- test-space-around []
  (vec
   (let [container-size 100
         lengths [5 10 15 20]]
     [(ui/with-style ::ui/style-stroke
        (ui/with-color [0 0 0]
          (ui/rectangle container-size 10)))
      (vec
       (for [[x length]
             (map vector
                  (space-around container-size lengths)
                  lengths)]
         (ui/translate x 0
                       (ui/filled-rectangle [0 0 1]
                                            length 10))))])))

(comment
  (backend/run #'test-space-around)
  ,)


(defn space-between
  "Given a container-length and lengths of child elements,
  return a sequence of offsets so that extra space is
  distributed evenly between the child elements."  
  [container-length lengths]
  (case (count lengths)
    0 []
    1 [0]

    ;; else
    (let [extra-space (- container-length
                         (reduce + lengths))
          _ (assert (not (neg? extra-space))
                    "No extra space for space between.")
          pad (/ extra-space (dec (count lengths)))]

      (loop [offsets [0]
             offset (first lengths)
             lengths (next lengths)]
        (if-not lengths
          offsets
          (let [length (first lengths)
                current-offset (+ offset pad)]
            (recur (conj offsets current-offset)
                   (+ current-offset length)
                   (next lengths))))))))


(defn test-space-between []
  (vec
   (let [container-size 100
         lengths [19 19 19 19 19]]
     [(ui/with-style ::ui/style-stroke
        (ui/with-color [0 0 0]
          (ui/rectangle container-size 10)))
      (vec
       (for [[x length]
             (map vector
                  (space-between container-size lengths)
                  lengths)]
         (ui/translate x 0
                       (ui/filled-rectangle [0 0 1]
                                            length 10))))])))

(comment
  (backend/run #'test-space-between)
  ,)


(defn space-evenly
  "Given a container-length and lengths of child elements,
  return a sequence of offsets so that each child elements
  will be evenly distributed in the container."
  [container-length lengths]
  (let [extra-space (- container-length
                       (reduce + lengths))
        _ (assert (not (neg? extra-space))
                  "No extra space for space evenly.")
        pad (/ extra-space (inc (count lengths)))]
    
    (loop [offsets []
           offset 0
           lengths (seq lengths)]
      (if-not lengths
        offsets
        (let [length (first lengths)
              current-offset (+ offset pad)]
          (recur (conj offsets current-offset)
                 (+ current-offset length)
                 (next lengths)))))))

(defn- test-space-evenly []
  (vec
   (let [container-size 100
         lengths [10 20 30]]
     [(ui/with-style ::ui/style-stroke
        (ui/with-color [0 0 0]
          (ui/rectangle container-size 10)))
      (vec
       (for [[x length]
             (map vector
                  (space-evenly container-size lengths)
                  lengths)]
         (ui/translate x 0
                       (ui/filled-rectangle [0 0 1]
                                            length 10))))])))


(defn align-distance
  [child-length child-pct parent-length  parent-pct ]
  (- (* parent-length parent-pct)
     (* child-length child-pct)))

(defn weighted-partition [length weights]
  (let [total-weight (reduce + weights)
        base-length (/ length total-weight)]
    (eduction
     (map (fn [weight]
            (* base-length weight)))
     weights)))


(defn halign
  "Horizontally align child within a parent width."
  [child child-pct parent-width parent-pct]
  (let [x (align-distance (ui/width child)
                          child-pct
                          parent-width
                          parent-pct)]
   (ui/translate x 0
                 child)))


(defn valign
  "Vertically align child within a parent height."
  [child child-pct parent-height parent-pct]
  (let [y (align-distance (ui/height child)
                          child-pct
                          parent-height
                          parent-pct)]
   (ui/translate 0 y
                 child)))



(defmacro with-container-size
  "Assoc in `size` into the component context. Children within the lexical
  scope will inherit use the contextual container size."
  [size & body]
  `(let [~'context (assoc ~'context :membrane.stretch/container-size ~size)]
       ~@body))

(defn vlayout
  "Vertically stack `elems`. Optional `xform` can be supplied."
  ([elems]
   (vlayout identity elems))
  ([xform elems]
   (-> (transduce xform
                  (completing
                   (fn [[offset xs] elem]
                     [(+ offset (ui/height elem))
                      (conj! xs
                             (ui/translate 0 offset
                                           elem))]))
                  [0
                   (transient [])]
                  elems)
       second
       persistent!)))

(defn hlayout
  "Horizontally stack `elems`. Optional `xform` can be supplied."
  ([elems]
   (hlayout identity elems))
  ([xform elems]
   (-> (transduce xform
                  (completing
                   (fn [[offset xs] elem]
                     [(+ offset (ui/width elem))
                      (conj! xs
                             (ui/translate offset 0
                                           elem))]))
                  [0
                   (transient [])]
                  elems)
       second
       persistent!)))



