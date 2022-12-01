# membrane.alpha.stretch

Stretch layout design for membrane. This will probably be merged into membrane once the kinks have been worked out.

## Usage

With membrane, layout is just data transformation. All the clojure tools for generic data manipulation apply. The goal of this library is to supply layout specific helpers.

Almost all layout boils down to measuring elements (see `membrane.ui/bounds`), partitioning space, and moving things around (see `membrane.ui/translate`).

As an example, let's take a lookout the implementation of `vlayout`:

```clojure
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
```

`vlayout` will vertically stack elements. There's no magic. Each element is added to a vector and translated by the current offset. The offset is the accumulation of the heights of all the previous elements.


### Containers

Another common layout task is subdividing space. To subdivide space, you need to know the amount of space you're working with. To facilitate components that stretch to fill their container, membrane now supports a convention for specifying the current container's size. The container size is a type of contextual state. The membrane convention is to pass the container size in the `:membrane.stretch/container-size` key of the component context.

Previously, container info and container size wasn't provided by the various toolkits. Starting with membrane `0.10.4-beta`, container info will now be provided by all the toolkits if the `:include-container-info` option is truthy.

Example:

```clojure

(require '[membrane.java2d :as backend]
         '[membrane.ui :as ui])

(backend/run
  (fn [container-info]
    (ui/label (pr-str (:container-size container-info))))
  {:include-container-info true})
```

When using `membrane.component/make-app`, the container size will automatically be included in the component context if available.

#### Getting the current container size

If the toolkit option,`:include-container-info`, is truthy, then the container size will be available. The current container size can be found within a `defui` component with `(:membrane.stretch/container-size context)` (`context` is available within the body of all `defui` components).

#### Specifying the container size for a child component

One of the jobs of `defui` is to implicitly pass `context` to child components. To set the container size for a child component, there are a few methods.

1) Lexical scoping

```clojure
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
```

2) Associng the container size in an element

Components are values, so you can assoc the container size directly.

```clojure
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
```

3) Explicitly passing a container size

This is the most common method when being used outside `defui`.
 
```clojure
(def my-view
  (let [child-size [200 200]
        context {}]
    (ui/horizontal-layout
     (left-component {:context (assoc context
                                      :membrane.stretch/container-size child-size)})
     (right-component {:context
                       (assoc context
                              :membrane.stretch/container-size child-size)}))))
```

## Playground

See [playground](/playground).

## License

Copyright © 2022 Adrian

Distributed under the Eclipse Public License version 1.0.
