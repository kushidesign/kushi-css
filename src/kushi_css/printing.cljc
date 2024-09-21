(ns kushi-css.printing
  (:require [kushi-css.specs :as specs]
            [clojure.string :as string]
            [clojure.spec.alpha :as s]
            [bling.core :refer [callout bling point-of-interest]]
            [fireworks.core :refer [? !? ?> !?> pprint]]))

(defn cssrule-selector
  "Prints warning"
  [sel form]
  (callout {:type        :warning
            :padding-top 1}
           (point-of-interest
            (merge {:file   ""
                    :header (bling "Bad css selector:"
                                   "\n"
                                   [:bold sel]
                                   "\n\n"
                                   "The first argument to "
                                   [:bold.blue "cssrule"]
                                   " must be:"
                                   "\n"
                                   "- a string"
                                   "\n"
                                   "- valid css selector"
                                   )
                    :body   "No css ruleset will be created."}
                   (meta form)
                   {:form form}))))


(defn cssrule-args
  "Prints warning"
  [{:keys [fname          
           invalid-args        
           form                
           ret]}]
  (callout
   {:type        :warning
    :margin-top  0
    :margin-bottom  1
    :padding-top 1}
   (point-of-interest
    (merge {:file
            ""
            :header
            (apply
             bling
             (concat ["Bad args to " [:italic fname] ":"
                      "\n"]
                     (interpose "\n"
                                (map (fn [arg] [:bold arg])
                                     invalid-args))
                     ))
            :body   (let [spec-data (s/form ::specs/valid-sx-arg)]
                      (apply
                       bling
                       (concat
                        [(if (contains? #{"kushi.core/css-rule"}
                                        fname)
                           "All args beyond the first are validated with:"
                           "All args are validated with:"
                           )
                         "\n"
                         [:bold.italic (str ::specs/valid-sx-arg)]
                         "\n\n"
                         [:italic (-> (? :data
                                         {:theme "Neutral Light"}
                                         (nth spec-data 0 nil))
                                      :formatted
                                      :string)]
                         "\n"
                         (-> (? :data
                                {:theme             "Neutral Light"
                                 :display-metadata? false}
                                (with-meta (apply hash-map (rest spec-data))
                                  {:fw/hide-brackets? true}))
                             :formatted
                             :string)
                         
                         "\n\n"
                         "The bad arguments will be discarded."
                         "\n\n"
                         "The follwing css ruleset will be created"
                         "\n"
                         "with the valid args:"
                         "\n\n"]
                        (interpose "\n"
                                   (map (fn [x] [:blue x])
                                        (string/split ret #"\n"))))))}
           (meta form)
           {:form form}))))
