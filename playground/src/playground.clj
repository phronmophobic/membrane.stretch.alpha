(ns playground
  (:require [membrane.component
             :refer [defui]
             :as component]
            [membrane.ui :as ui]
            [membrane.alpha.stretch :as stretch]
            [membrane.skia :as backend]))

(defn hpct->label [pct]
  (case pct
    0 "left"
    1 "right"
    0.5 "center"
    "arbitrary"))
(defn halign->label [child-pct parent-pct]
  (str
   (hpct->label child-pct)
   " to "
   (hpct->label parent-pct)
   " (" child-pct " " parent-pct ")"))

(defn vpct->label [pct]
  (case pct
    0 "top"
    1 "bottom"
    0.5 "center"
    "arbitrary"))
(defn valign->label [child-pct parent-pct]
  (str
   (vpct->label child-pct)
   " to "
   (vpct->label parent-pct)
   " (" child-pct " " parent-pct ")"))


(def color2 [1.0 0.88 0.93 0.98])
(defui left-side [{:keys []}]
  (let [[cw ch :as size] (:membrane.stretch/container-size context)
        ]
    (ui/translate (* cw 0.2)
                  (* ch 0.2)
                  (stretch/with-container-size [(* cw 0.6)
                                        (* ch 0.6)] 
                    (let [[cw ch :as size] (:membrane.stretch/container-size context)
                          rect (ui/with-style ::ui/style-fill
                                 (ui/with-color color2
                                   (ui/rectangle cw ch)))
                          rw cw
                          rows [   
                                {:alignment [1 0]} 
                                {:alignment [0.5 0]} 
                                {:alignment [0.6 0.2]} 
                                {:alignment [0 0]} 
                                {:alignment [1 0.5]} 
                                {:alignment [0.5 0.5]} 
                                {:alignment [0 0.5]} 
                                {:alignment [1 1]} 
                                {:alignment [0.5 1]} 
                                {:alignment [0 1]}
                                ;; stretch
                                {:stretch true}
                                
                                ;; [:stretch 1 (label {:text "Stretch"})]


                                ]
                          rh (/ ch (count rows))]
                      [rect
                       (stretch/vlayout
                        (map
                         (fn [row]
                           (ui/fixed-bounds [rw rh]
                                            (if (:stretch row)
                                              [(ui/label "Stretch")
                                               (ui/filled-rectangle [0 0 1 0.2]
                                                                    rw
                                                                    rh)]
                                              (let [[child-pct parent-pct :as alignment] (:alignment row)
                                                    child (ui/label (halign->label child-pct parent-pct))
                                                    rect (ui/filled-rectangle [0 0 1 0.2]
                                                                              (ui/width child)
                                                                              rh
                                                                              ;;(ui/height child)
                                                                              )]
                                                (stretch/halign [(ui/center child
                                                                            (ui/bounds rect))
                                                                 rect]
                                                                child-pct
                                                                rw
                                                                parent-pct))))))
                        rows)])))))


(defui right-side [{:keys []}]
  (let [[cw ch :as size] (:membrane.stretch/container-size context)
        ]
    (ui/translate (* cw 0.1)
                  (* ch (/ 1 3))
                  (stretch/with-container-size [(* cw 0.8)
                                        (* ch (/ 1 3))] 
                    (let [[cw ch :as size] (:membrane.stretch/container-size context)
                          rect (ui/with-style ::ui/style-fill
                                 (ui/with-color color2
                                   (ui/rectangle cw ch)))
                          rh ch
                          cols [
                                {:alignment [1 0]} 
                                {:alignment [0.5 0]} 
                                {:alignment [0.6 0.2]} 
                                {:alignment [0 0]} 
                                {:alignment [1 0.5]} 
                                {:alignment [0.5 0.5]} 
                                {:alignment [0 0.5]} 
                                {:alignment [1 1]} 
                                {:alignment [0.5 1]} 
                                {:alignment [0 1]}
                                ;; stretch
                                {:stretch true}
                                
                                ;; [:stretch 1 (label {:text "Stretch"})]


                                ]
                          rw (/ cw (count cols))
                          ]
                      [rect
                       (stretch/hlayout
                        (map
                         (fn [row]
                           (ui/fixed-bounds [rw rh]
                                            (if (:stretch row)
                                              [(ui/label "Stretch")
                                               (ui/filled-rectangle [0 0 1 0.2]
                                                                    rw
                                                                    rh)]
                                              (let [[child-pct parent-pct :as alignment] (:alignment row)
                                                    child (ui/label (valign->label child-pct parent-pct))
                                                    rect (ui/filled-rectangle [0 0 1 0.2]
                                                                              rw
                                                                              rh
                                                                              ;;(ui/height child)
                                                                              )]
                                                
                                                (stretch/valign [(ui/center child
                                                                            (ui/bounds rect))
                                                                 rect]
                                                                child-pct
                                                                rh
                                                                parent-pct))))))
                        cols)])))))



(defui both [{:keys []}]
  (let [cols [(right-side {})
              (left-side {})]
        [cw ch :as size] (:membrane.stretch/container-size context)
        col-width (/ cw (count cols))
        col-height ch]
    (apply
     ui/horizontal-layout
     (eduction
      (map #(assoc-in % [:context :membrane.stretch/container-size]
                      [col-width col-height]))
      cols)))
  )

(comment
  (let [app (component/make-app #'left-side {})]
    (backend/run app {:include-container-info true}))

  (let [app (component/make-app #'right-side {})]
    (backend/run app {:include-container-info true}))

  (let [app (component/make-app #'both {})]
    (backend/run app {:include-container-info true}))
  ,)


(defui stretch-rect [{}]
  (let [[w h :as size] (:membrane.stretch/container-size context)
        lbl (ui/label (str (format "%.2f" (double w)) "x" (format "%.2f" (double h))))]
    [(ui/filled-rectangle [0 0 1 0.2]
                          w h
                          )
     (ui/center lbl
                size)]))

(defui left-component [{}]
  (stretch-rect {})
  )

(defui right-component [{}]
  (stretch-rect {})
  )

(defui my-parent-component [{:keys []}]
  (let [[w h :as size] (:membrane.stretch/container-size context)
        ;; split the width of the container in half
        child-size [(/ w 2) h]
        context (assoc context :membrane.stretch/container-size child-size)]
    (ui/horizontal-layout
     ;; left
     (left-component {})

     ;; right
     (right-component {}))))

(comment
  (let [app (component/make-app #'my-parent-component {})]
    (backend/run app {:include-container-info true}))
  )


(defui my-parent-component2 [{:keys []}]
  (let [[w h :as size] (:membrane.stretch/container-size context)
        ;; split the width of the container in half
        child-size [(/ w 2) h]]
    (apply
     ui/horizontal-layout
     (eduction (map #(assoc-in % [:context :membrane.stretch/container-size]
                               child-size))
               [(left-component {})
                (right-component {})]))))

(comment
  (let [app (component/make-app #'my-parent-component2 {})]
    (backend/run app {:include-container-info true}))
  )


(def my-view
  (let [child-size [200 200]
        context {}]
    (ui/horizontal-layout
     (left-component {:context (assoc context
                                      :membrane.stretch/container-size child-size)})
     (right-component {:context
                       (assoc context
                              :membrane.stretch/container-size child-size)}))))



(comment
  (backend/run (constantly
                my-view))
  ,
  )
