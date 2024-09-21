(ns kushi-css.core
  (:require 
   [kushi-css.defs :as defs]
   [kushi-css.hydrated :as hydrated]
   [kushi-css.specs :as specs]
   [kushi-css.printing :as printing]
   [kushi-css.util :refer [keyed]]
   [clojure.walk :as walk :refer [prewalk postwalk]]
   [clojure.string :as string]
   [clojure.spec.alpha :as s]
   [fireworks.core :refer [? !? ?> !?>]]
  ;; for testing
  ;;  [taoensso.tufte :as tufte]
   ))

;; -----------------------------------------------------------------------------
;; Utilities
;; -----------------------------------------------------------------------------

(defn- partition-by-pred [pred coll]
  "Given a coll and a pred, returns a vector of two vectors. The first vector
   contains all the values from coll that satisfy the pred. The second vector
   contains all the values from the coll that do not satisfy the pred."
  (let [ret* (reduce (fn [acc v]
                       (let [k (if (pred v) :valid :invalid)]
                         (assoc acc k (conj (k acc) v))))
                     {:valid [] :invalid []}
                     coll)]
    [(:valid ret*) (:invalid ret*)]))

(defn- partition-by-spec
  "Given a coll and a spec, returns a vector of two vectors. The first vector
   contains all the values from coll that satisfy the spec. The second vector
   contains all the values from the coll that do not satisfy the spec."
  [spec coll]
  (let [ret* (reduce (fn [acc v]
                       (let [k (if (s/valid? spec v) :valid :invalid)]
                         (assoc acc k (conj (k acc) v))))
                     {:valid [] :invalid []}
                     coll)]
    [(:valid ret*) (:invalid ret*)]))



;; -----------------------------------------------------------------------------
;; Flattening / vectorizing
;; -----------------------------------------------------------------------------

(defn split-on [re v]
  (string/split (name v) re))


(defn- unpack-pvs [coll]
  (reduce
   (fn [acc x]
     (if (vector? (nth x 0))
       (apply conj acc x)
       (conj acc x)))
   []
   coll))


(defn- map->vec [v]
  (if (map? v) (into [] v) v))


(defn conformed-map* 
  "Expects a vector of vectors, the output of `(s/conform ::specs/sx-args args)`"
  [coll]
  (reduce (fn [m [k v]]
            (assoc m
                   k 
                   (conj (or (some-> m k) [])
                         v)))
          {}
          coll))


(defn top-level-maps->vecs
  [conformed-map]
  (some->> conformed-map
           :style-map
           (map #(into [] %))
           (apply concat)
           (apply conj [])
           (postwalk map->vec)))


(defn top-level-vecs->vecs
  [conformed-map]
  (some->> conformed-map
           :style-vec
           (postwalk map->vec)))


(defn vectorized*
  [coll]
  (let [conformed-map        (conformed-map* coll)
        untokenized          (->> conformed-map 
                                  :tokenized
                                  (map (partial split-on #"--")))

        top-level-maps->vecs (top-level-maps->vecs conformed-map)
        top-level-vecs->vecs (top-level-vecs->vecs conformed-map)
        list-of-vecs         (concat untokenized
                                     top-level-vecs->vecs
                                     top-level-maps->vecs)
        vectorized           (unpack-pvs list-of-vecs)]

    (!? (keyed [coll
                conformed-map 
                untokenized   
                top-level-maps->vecs  
                list-of-vecs            
                vectorized]))              

    {:conformed-map conformed-map
     :vectorized    vectorized}))


;; -----------------------------------------------------------------------------
;; Grouping
;; -----------------------------------------------------------------------------

(defn- vec-of-vecs? [v]
  (and (vector? v)
       (every? vector? v)))

(defn- sel-and-vec-of-vecs?2 [x]
  (boolean (and (vector? x)
                (string? (nth x 0 nil))
                (vec-of-vecs? (nth x 1 nil)))))

(defn- more-than-one? [coll]
  (> (count coll) 1))

(defn- dupe-reduce [grouped]
  (reduce-kv (fn [acc k v]
               (->> v
                    (reduce (fn [acc [_ vc]] (apply conj acc vc)) [])
                    (vector k)
                    (conj acc)))
             []
             grouped))

(defn group-shared*
  "Groups things for nesting.
   Postions css properties in front of other selector bits.
   Pseudo-classes are ordered according to defs/lvfha-pseudos-order."
  ;; TODO - make pseudo-ordering override-able.
  [v maybe-dupes]
  (let [all-sels
        (map first maybe-dupes)
            
        ;; Determine if there are selectors with lvfha pseudoclasses
        some-lvfha?
        (some #(contains? defs/lvfha-pseudos-strs %) all-sels)

        dupe-sels
        (->> all-sels
             frequencies
             (keep (fn [[sel n]] (when (> n 1) sel)))
             (into #{}))

        ;; If there are any duplicate selectors, partition them from others
        [dupe-vecs others]
        (if (seq dupe-sels)
          (partition-by-pred #(contains? dupe-sels (nth % 0 nil)) v)
          [[] v])

        ;; Potentially group and reduce duplicates
        grouped-dupes
        (some->> dupe-vecs (group-by first) dupe-reduce)

        ;; Create new vec-of-vecs with non-dupes and grouped dupes
        ret*
        (!? 'ret* (apply conj others grouped-dupes))
            
        ;; Optionally sort the new vec-of-vecs, based on whether there were any
        ;; lvfha pseudoclasses
        ret
        (if some-lvfha?
          (into []
                (sort-by #(->> % 
                               first
                               (get defs/lvfha-pseudos-order-strs))
                         ret*))
          ret*)]

#_(!? (keyed [all-sels
           some-lvfha?
           dupe-sels
           dupe-vecs
           others
           grouped-dupes
           ret*
 ret]))


        ret))


(defn group-shared
  [v]
  (if-let [dupes* (seq (filter sel-and-vec-of-vecs?2 v))]
    (if (more-than-one? dupes*)
      (group-shared* v dupes*)
      v)
    v))

;; -----------------------------------------------------------------------------
;; API
;; -----------------------------------------------------------------------------
(defn- loc-id
  "Returns classname based on namespace and line + column.
   e.g. \"starter_browser__41_6\""
  [env form]
  (when-let [ns* (some-> env :ns :name (string/replace #"\." "_"))]
    (let [fm (meta form)]
     (str ns* "__L" (:line fm) "_C" (:column fm)))))



;; -----------------------------------------------------------------------------
;; TODO - Use this version of loc-id to investigate weird diff between 
;; -----------------------------------------------------------------------------

;; (? (css :.foo :p--10px :c--red)) => "foo"
;; and
;; (? :pp (css :.foo :p--10px :c--red)) => "foo [\"__35_8\"]"


;; (defn- loc-id
;;   "Returns classname based on namespace and line + column.
;;    e.g. \"starter_browser__41_6\""
;;   [env form]
;;   (let [ns* (some-> env :ns :name (string/replace #"\." "_"))
;;         fm  (meta form)]
;;     (str ns* "__" (:line fm) "_" (:column fm))))

;; -----------------------------------------------------------------------------



(defn- user-classlist
  "Expects a conformed map based on `::specs/sx-args`. This map is the
   `:conformed` entry from return val of `kushi-css.flatten/vectorized*`.

   Returns a map like:
   {:class-kw '(...)
    :classes [...]}"
  ([m]
   (user-classlist m nil))
  ([{:keys [class-kw class-binding] :as m} loc-id]
   (let [class-kw-stringified (map specs/dot-kw->s class-kw)]
     {:class-binding class-binding
      :classes       (into []
                           (concat class-binding
                                   class-kw-stringified
                                   (some-> loc-id vector)))})))

(defn- classlist
  "Returns classlist vector of classnames as strings. Includes user-supplied
   classes, as well as auto-generated, namespace-derived classname from `css`
   macro."
  ([form args]
   (classlist {:ns {:name "some.test"}} form args))
  ([env form args]
   (let [loc-id   
         (some-> env (loc-id form))

         m
         (->> args
              (s/conform ::specs/sx-args)
              vectorized*
              :conformed-map)]
     (user-classlist m loc-id))))

(defn- spaces [n] (string/join (repeat n " ")))

(defn- css-block-str
  "Reduces nested vector representation of css-block into valid, potentially
   nested, serialized css rule block. Does not include outermost curly braces.
  
   Example:
    
   [[\"color\" \"blue\"]
    [\"&>p\" [[\"color\" \"red\"]
              [\"background-color\" \"blue\"]]]
   =>
   \"color: blue;
     &>p {
       color: red;
       background-color: blue;
     }\""
  ([coll]
   (css-block-str coll 2))
  ([coll indent]
   (reduce
    (fn [acc [k v]]
      (let [spc (spaces indent)]
        (str acc 
             (if (vector? v)
               (str spc k " {\n" (css-block-str v (+ indent 2)) spc "}\n")
               (str spc k ": " v ";\n")))))
    ""
    coll)))

(defn- nested-array-map
  "Takes a vector representation of a nested array map and returns a nested
   array map."
  [coll]
  (walk/postwalk
   #(if (and (vector? %)
             (every? (fn [x]
                       (and (vector? x)
                            (= (count x) 2)))
                     %))
      (apply array-map (sequence cat %))
      %)
   coll))


(defn- css-block* [conformed]
  (let [{:keys [vectorized
                conformed-map]}
        (vectorized* conformed)

        grouped                 
        (!? 'grouped-new
            (->> vectorized 
                 (!? 'vectorized)
                 hydrated/hydrated-stacks
                 (!? 'hydrated)
                 (prewalk group-shared)))]

    {:css-block     (str "{\n" (css-block-str grouped) "}")
     :nested-vector grouped
    ;; Leave this :nested-array-map out for now
    ;; :nested-array-map (nested-array-map grouped)
     :classes       (-> conformed-map
                        user-classlist
                        :classes)}))


(defn- conformed-args
  "Used be kushi-css.core/css-rule and kushi-css.core/css-block to validate and
   conform args. Returns a vector of `[conformed-args invalid-args]`"
  [args &form fname]
  (let [conformed-args* (s/conform ::specs/sx-args args)
        invalid-args?   (= conformed-args* :clojure.spec.alpha/invalid)
        [valid-args
         invalid-args]  (when invalid-args?
                          (partition-by-spec ::specs/valid-sx-arg args))
        conformed-args  (if invalid-args?
                          (s/conform ::specs/sx-args valid-args)
                            conformed-args*)
        ret             (some->> conformed-args
                                 css-block*
                                 :css-block)]
;; (pprint conformed-args)
        (!? {
             :conformed-args* conformed-args* 
             :invalid-args?   invalid-args?   
             :valid-args      valid-args
             :invalid-args    invalid-args  
             :conformed-args  conformed-args
             }
            )
    (when (seq invalid-args)
      (printing/cssrule-args
       {:fname        fname
        :invalid-args invalid-args
        :form         &form
        :ret          ret}))
    ret))


(defmacro ^:public css-block-data
  "Returns a map with following keys:
   :nested-vector    ->  vector representation of nested css.
   :nested-css-block ->  pretty-printed css ruleset, no selector.
   :classes          ->  user supplied classes.
   :ns               ->  namespace of the callsite, a symbol.
   :file             ->  filename as string
   :line             ->  line number
   :column           ->  column number
   :end-line         ->  end line number
   :end-column       ->  end column number"
  [& args]
  (merge (css-block* args)
         (some->> &env :ns :name str symbol (hash-map :ns))
         (meta &form)))


;; Does this need to be a macro?
;; Maybe it just gets callsite info from analyzer fn which calls it.
(defmacro ^:public css-block
  "Returns a pretty-printed css rule block (no selector)."
  [& args]
  (!? &form
     (conformed-args args
                     &form
                     "kushi-css.core/css-block")))


;; TODO
;; - conditionally check for build state to do composing?
;; - maybe leave out for now?
(defmacro ^:public css-rule
  "Returns a serialized css ruleset, with selector and potentially nested css
   block."
  [sel & args]
  (if-not (s/valid? ::specs/css-selector sel)
    (printing/cssrule-selector sel &form)
    (str sel
         " "
         (conformed-args args
                         &form
                         "kushi-css.core/css-rule"))))

;; TODO
;; - conditionally check for build state
;; - Make sx version that returns map with :style and :class entries
(defmacro ^:public css
  "Returns classlist string consisting of auto-generated classname and
   user-supplied classnames.
   
   Example of expansion in a component. Let's say the namespace is called
   foo.core, on line 100:

   100 | (defn my-component [text]
   101 |   [:div
   102 |    {:class (css :.absolute :c--red :fs--48px)}
   103 |    text])
   =>
   (defn my-component [text]
    [:div
     {:class \"absolute foo_core__L102_C11\"}
     text])
   
   The call to `css` produces the following class in the build's
   watch/analyze/css generation process:

   .foo_core__L102_C11 {
     color:     red;
     font-size: 48px;
   }
   "
  [& args]
  (let [
        ;; If calling from a test namespace, it might not resolve a
        ;; val for &env so we will call classlist with 2 args instead of 3.
        {:keys [classes class-binding] :as m}
        (apply classlist 
               (if-not &env
                 [&form args]
                 [&env &form args]))]

    ;; If `classes` vector contains any symbols that are runtime bindings
    ;; intended to hold classnames (`class-bindings`) we will need to
    ;; string/join it at runtime, e.g.:
    ;; `[(when my-runtime-var "foo") my-classname "bar"]`
    ;;
    ;; If no conditional class forms, we can string/join it at compile time
    (if (seq class-binding) 
      `(string/join " " ~classes)
      (string/join " " classes))))

(defmacro ^:public sx
  "Returns a map with a :class string. Sugar for `{:class (css ...)}`, to avoid
   boilerplate when you are only applying styling to an element and therefore do
   not need to supply any html attributes other than :class."
  [& args]
  (let [{:keys [classes class-binding] :as m}
        (apply classlist 
               (if-not &env
                 [&form args]
                 [&env &form args]))]

    (if (seq class-binding) 
      `{:class (string/join " " ~classes)}
      {:class (string/join " " classes)})))



(defn- css-vars-map*
  "Constructs a style map for assigning locals to css custom properties."
  [args]
  (reduce (fn [acc sym]
            (assoc acc (str "--_" sym) sym))
          {}
          args))


(defmacro ^:public css-vars
  "Intended to construct a style str assigning locals to css custom properties.

   Let's say we have a namespace called foo.core, line 100:

   100 | (let [my-var1 \"blue\"
   101 |       my-var2 \"yellow\"]
   102 |   {:style (css-vars my-var1 my-var2)
   103 |    :class (css :c--$my-var1 :bgc--$my-var2)})
   =>
   {:style {\"--_my-var1\" my-var1
            \"--_my-var2\" my-var1}
    :class \"foo_core__L103_C11\"}
   
   The call to `css` produces the following class in the build's
   watch/analyze/css generation process:

   .foo_core__L103_C11 {
     color: --_my-var1;
     background-color: --_my-var2;
   }"
  [& args]
  (let [m (css-vars-map* args)]
    `(reduce-kv (fn [acc# k# v#] (str acc# k# ": " v# ";") ) "" ~m)))


(defmacro ^:public css-vars-map
  "Same as `css-vars`, but returns a map instead of a string."
  [& args]
  (css-vars-map* args))
