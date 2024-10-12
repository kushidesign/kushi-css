(ns kushi-css.core-test
  (:require [clojure.test :refer :all]
            [fireworks.core :refer [? !? ?> !?>]]
            [lasertag.core :refer [tag-map]]
            [kushi-css.core :refer [at-rule
                                    ;; at-rule*
                                    at-rule*
                                    ?at-rule
                                    css-block-data
                                    css-block
                                    css-rule
                                    ?css
                                    ?defcss
                                    css
                                    css-vars
                                    css-vars-map
                                    sx
                                    lightning-opts
                                    lightning]]
            [kushi-css.specs :as specs]
            [clojure.spec.alpha :as s]
            [clojure.walk :as walk]))


;; (? (tag-map "hi"))
;; (println (s/conform ::specs/sx-args 
;;                     '(:c--red
;;                       222
;;                       :bgc--blue
;;                       :.gold )
;;                     ))

;; (? specs/css-selector-re #_(s/explain ::specs/css-selector "p"))
;; (? (s/explain ::specs/css-selector "p"))

#_(def my-runtime-class-binding "foo")

#_(?   (css-block {:c  :blue
                 :>p {:c   :red
                      :bgc :blue}}))



#_(s/explain ::specs/style-vec [:>p {:c   :red
                                   :bgc :blue}])


#_(s/explain ::specs/pseudo-element-and-string [:before:content "\"⌫\""])




;; (? (css-block {"[data-foo-bar-sidenav][aria-expanded=\"true\"] &"
;;                {:>.sidenav-menu-icon:d  :none
;;                 :>.sidenav-close-icon:d :inline-flex
;;                 :>ul:h                  "calc((100vh - (var(--navbar-height) * 2)) * 1)"
;;                 :h                      :fit-content
;;                 :o                      1} }))


;; (? (css-rule "@font-face" :c--blue))

;; (at-rule "supports not (color: oklch(50% .37 200))"
;;            (css-rule ".element" {:color :#0288D7}))

;; (css-rule 9 {:color :#0288D1})

#_(? (at-rule "@font-face"
            {:font-family "Trickster"
             :src         "local(Trickster), url(\"trickster-COLRv1.otf\") format(\"opentype\") tech(color-COLRv1)"}))


;; (!? (css-block 
;;     {:font-family "Trickster"
;;      :src         "local(Trickster), url(\"trickster-COLRv1.otf\") format(\"opentype\") tech(color-COLRv1),"}))


#_(? (css-block
    ;; :hover:c--blue
    ;; :>a:hover:c--red
    ;; :_a:hover:c--gold ; The "_" gets converted to " "
    ;; :.bar:hover:c--pink
    ;; :before:fw--bold
    ;; :after:mie--5px
    ;; {"~a:hover:c" :blue} ; Vector is used as "~" is not valid in a keyword
    ;; {"nth-child(2):c" :red} ; Vector is used as "(" and ")" are not valid in keywords
    [:before:content "\"⌫\""]

    #_{"nav[data-foo-bar-sidenav][aria-expanded=\"true\"] &"
       {:>.sidenav-menu-icon:d  :none
        :>.sidenav-close-icon:d :inline-flex
        :>ul:h                  "calc((100vh - (var(--navbar-height) * 2)) * 1)"
        :o                      1}}

    #_{:>p {:hover {:c   :blue
                    :td  :underline
                    :bgc :yellow
                    :_a  {:c   :purple
                          :td  :none
                          :bgc :pink}
                    }}}

    ;; :hover:c--red
    ;; :active:c--red
    ;; :lg:dark:hover:c--orange
    ;; :lg:dark:hover:>p:hover:c--black
    ;;  :lg:dark:c--black
    ;;  :hover:c--red
    ;;  :dark:c--white
    ;;  :dark:hover:c--hotpink
    ;;  :lg:dark:hover:c--yellow
    ;;  :lg:dark:hover:>div.foo:c--silver
    ;; [:hover {:bgc :blue
    ;;          :>p  {:c   :teal
    ;;                :bgc :gray}}]
    ;; [:hover:c :yellow]
    ;; {:>p :5px}
    ;; {:>p :10px}
    ;; [:hover {:c :red
    ;;          :bgc :blue}]
    

    ;; ["@media (min-width: 30em) and (max-width: 50em)" 
    ;;  {:c :red}]
    

    ;; ["@media screen and print" 
    ;;  {:c :red}]
    
    ;; {:>a {:c   :green
    ;;       :bgc :orange}
    ;;  :>b {:c   :silver
    ;;       :bgc :gold}}
    ;; {:hover:>a {:c   :blue
    ;;             :bgc :orange}
    ;;  :hover:>b {:c   :black
    ;;             :bgc :gold}}
;;  [:>p {:c   :red
;;        :bgc :blue}]
;;  [:>d {:c   :yellow
;;        :bgc :teal}]
    ))

;; (?defcss ".bang" :c--blue)

;; (? (at-rule* "@supports not (color: oklch(50% .37 200))"
;;             (css-rule ".element" {:color :#0288D7})
;;             (css-rule ".element2" {:color :#0288D0})))

;; (? (at-rule*2 "@supports not (color: oklch(50% .37 200))"
;;               3 #_[".element" {:color :#0288D7}]
;;               [".element2" {:color :#0288D0}]))

;; (? (at-rule*2 "@keyframes slider"
;;               [:froms {:transform "translateX(0%)"
;;                       :opacity   0}]
;;               [:to {:transform "translateX(100%)"
;;                     :opacity   1}]))

(?at-rule "@keyframes slider"
          [:from {:transform "translateX(0%)"
                  :opacity   0}]
          [:to {:transform "translateX(100%)"
                :opacity   1}])

#_(at-keyframes "slider"
              [:from {:transform "translateX(0%)"
                      :opacity   0}]
              [:to {:transform "translateX(100%)"
                    :opacity   1}])

;; (?css :hover:c--blue
;;     :>a:hover:c--red
;;     :_a:hover:c--gold ; The "_" gets converted to " "
;;     :.bar:hover:c--pink
;;     :before:fw--bold
;;     :after:mie--5px
;;     ["~a:hover:c" :blue] ; Vector is used as "~" is not valid in a keyword
;;     ["nth-child(2):c" :red] ; Vector is used as "(" and ")" are not valid in keywords
;;     [:before:content "\"⌫\""])

;;  (? (s/explain ::specs/keyframe-percentage "50%"))
;;  (? (s/valid? ::specs/keyframe ["50%" {:color :blue}]))

#_(css-rule ".gold"
          {:>p {:c   :red
               :bgc :blue}
           :>div {:c :orange :b :1px:solid:black}}
          :c--blue)

;; (def my-var1 "blue")
;; (def my-var2 "yellow")

;; (? (css-vars my-var1 my-var2))
;; (? (css-vars-map my-var1 my-var2))

;; (? (sx :c--blue :.some-other-class))

;; (? (let [my-var1 "blue"       
;;          my-var2 "yellow"]
;;        (css-vars my-var1 my-var2)))

;; #_(? (let [my-var1 "blue"       
;;          my-var2 "yellow"]
;;   {
;;    :style (css-vars my-var1 my-var2)
;;   ;;  :class (css :c--$my-var1 :bgc--$my-var2)
;;    }))

;; (? :pp
;;    (css-rule
;;     "p"
;;     :c--red
;;     :bgc--blue
;;     #_(when false :.bold)))

;; (? #_:pp (css :.foo :p--10px :c--red))


;; (? (css :.foo--bang :c--red))
;; (? (re-find specs/classname-with-dot-re ".pc--gold"))



;; (def result 
;; ".foo {
;;   color: red;
;;   & .bar {
;;     color: green;
;;   }
;; }")

;; (? (into [] {:a "a" :b "b" :c "c"}))

;; (? (:out (shell {:in result :out :string} "npx" "lightningcss" "--minify" "--targets" ">= 0.25%")))

#_(css-block
;;  12
 :c--red
 :bgc--blue
 :p--10
 {:>p {:bgc :blue
       :b   :1px:solid:black
       :p   :10px}})

;; Fix tests
#_(do 
  ;; Figure out how to test these from a test namespace
  #_(deftest css-macro 
    (testing "tokenized keyword"
      (is (= (css :p--10px) "")))
    (testing "tokenized keywords"
      (is (= (css :p--10px :c--red) "")))
    (testing "tokenized keyword with classname"
      (is (= (css :.foo :p--10px :c--red) "foo"))))

  (deftest css-block-macro
    (testing "tokenized -> " 

      (testing "keywords -> " 
        (testing "2"
          (is (= (css-block :c--red :bgc--blue)
                 "{\n  color: red;\n  background-color: blue;\n}")))
        (testing "with classname"
          (is (= (css-block :.foo :c--red :bgc--blue)
                 "{\n  color: red;\n  background-color: blue;\n}")))
        (testing "with pseudoclasses"
          (is (= (css-block :active:c--magenta
                            :visited:c--orange
                            :hover:c--red)
"{
  &:visited {
    color: orange;
  }
  &:hover {
    color: red;
  }
  &:active {
    color: magenta;
  }
}")))
        (testing "with pseudoclasses and nesting"
          (is (= (css-block :active:c--magenta
                            :visited:c--orange
                            :hover:c--red
                            :focus:c--pink
                            :focus:bgc--blue)
"{
  &:visited {
    color: orange;
  }
  &:focus {
    color: pink;
    background-color: blue;
  }
  &:hover {
    color: red;
  }
  &:active {
    color: magenta;
  }
}"))))

      (testing "keyword -> "
        (testing "1"
          (is (= (css-block :c--red)
                 "{\n  color: red;\n}")))

        (testing "with css var"
          (is (= (css-block :c--$red-100)
                 "{\n  color: var(--red-100);\n}")))

        (testing "with with multiple properties syntax"
          (is (= (css-block :b--1px:solid:red)
                 "{\n  border: 1px solid red;\n}")))

        (testing "with with multiple properties syntax and css-var"
          (is (= (css-block :b--1px:solid:$red-100)
                 "{\n  border: 1px solid var(--red-100);\n}")))

        (testing "with alternation syntax"
          (is (= (css-block :ff--sans-serif|fantasy)
                 "{\n  font-family: sans-serif, fantasy;\n}")))

        (testing "with alternation syntax and multiple properties syntax"
          (is (= (css-block :text-shadow--5px:5px:10px:red|-5px:-5px:10px:blue)
                 "{\n  text-shadow: 5px 5px 10px red, -5px -5px 10px blue;\n}")))) 
      
      (testing "strings -> " 
        (testing "2"
          (is (= (css-block "c--red" "bgc--blue")
                 "{\n  color: red;\n  background-color: blue;\n}")))

        (testing "with classname"
          (is (= (css-block :.foo "c--red" "bgc--blue")
                 "{\n  color: red;\n  background-color: blue;\n}"))))

      (testing "string -> "
        (testing "1"
          (is (= (css-block "c--red")
                 "{\n  color: red;\n}")))

        (testing "with css var"
          (is (= (css-block "c--$red-100")
                 "{\n  color: var(--red-100);\n}")))

        (testing "with with multiple properties syntax"
          (is (= (css-block "b--1px:solid:red")
                 "{\n  border: 1px solid red;\n}")))

        (testing "with with multiple properties syntax and css-var"
          (is (= (css-block "b--1px:solid:$red-100")
                 "{\n  border: 1px solid var(--red-100);\n}")))

        (testing "with alternation syntax"
          (is (= (css-block "ff--sans-serif|fantasy")
                 "{\n  font-family: sans-serif, fantasy;\n}")))

        (testing "with alternation syntax and multiple properties syntax"
          (is (= (css-block "text-shadow--5px:5px:10px:red|-5px:-5px:10px:blue")
                 "{\n  text-shadow: 5px 5px 10px red, -5px -5px 10px blue;\n}")))))
    

    (testing "map ->"
      (testing "1 entry"
        (is (= (css-block {:c :red})
               "{\n  color: red;\n}")))

      (testing "2 entries"
        (is (= (css-block {:c   :red
                           :mie :1rem})
               "{\n  color: red;\n  margin-inline-end: 1rem;\n}")))

      
      (testing "1 entry, with css calc"
        (is (= (css-block {:w "calc((100vh - (var(--navbar-height) * (2 + (6 / 2)))) * 1)"})
"{
  width: calc((100vh - (var(--navbar-height) * (2 + (6 / 2)))) * 1);
}")))

      (testing "with psdeudoclass ->"
        (testing "1 entry"
          (is (= (css-block {:last-child:c :red})
                 "{\n  &:last-child {\n    color: red;\n  }\n}")))
        (testing "2 entries"
          (is (= (css-block {:last-child:c  :red
                             :first-child:c :blue})
                 "{\n  &:last-child {\n    color: red;\n  }\n  &:first-child {\n    color: blue;\n  }\n}")))
        (testing "1 entry, nested"
          (is (= (css-block {:last-child {:c   :red
                                          :bgc :blue}})
                 "{\n  &:last-child {\n    color: red;\n    background-color: blue;\n  }\n}")))
        (testing "1 entries, grouped"
          (is (= (css-block {:>p:c :red}
                            {:>p:bgc :blue})
"{
  &>p {
    color: red;
    background-color: blue;
  }
}"))))

      (testing "with psdeudoelement ->"
        (testing "1 entry"
          (is (= (css-block {:before:content "\"⌫\"" })
"{
  &::before {
    content: \"⌫\";
  }
}")))
        (testing "2 entries"
          (is (= (css-block {:before:content "\"⌫\""
                             :after:content "\"⌫\""})
"{
  &::before {
    content: \"⌫\";
  }
  &::after {
    content: \"⌫\";
  }
}")))
        (testing "1 entry, nested"
          (is (= (css-block {:last-child {:c   :red
                                          :bgc :blue}})
                 "{\n  &:last-child {\n    color: red;\n    background-color: blue;\n  }\n}")))
        (testing "1 entries, grouped"
          (is (= (css-block {:>p:c :red}
                            {:>p:bgc :blue})
"{
  &>p {
    color: red;
    background-color: blue;
  }
}"))))
      (testing "with compound data attribute selectors ->"
        (testing "1 entry, with nesting"
          (is (= (css-block {"[data-foo-bar-sidenav][aria-expanded=\"true\"]" 
                             {:>.sidenav-menu-icon:d  :none
                              :>.sidenav-close-icon:d :inline-flex
                              :>ul:h                  "calc((100vh - (var(--navbar-height) * 2)) * 1)"
                              :h                      :fit-content
                              :o                      1} })
"{
  &[data-foo-bar-sidenav][aria-expanded=\"true\"] {
    &>.sidenav-menu-icon {
      display: none;
    }
    &>.sidenav-close-icon {
      display: inline-flex;
    }
    &>ul {
      height: calc((100vh - (var(--navbar-height) * 2)) * 1);
    }
    height: fit-content;
    opacity: 1;
  }
}")))
        (testing "1 entry, with nesting, ancestor selector"
          (is (= (css-block {"[data-foo-bar-sidenav][aria-expanded=\"true\"] &" 
                             {:>.sidenav-menu-icon:d  :none
                              :>.sidenav-close-icon:d :inline-flex
                              :>ul:h                  "calc((100vh - (var(--navbar-height) * 2)) * 1)"
                              :h                      :fit-content
                              :o                      1} })
"{
  [data-foo-bar-sidenav][aria-expanded=\"true\"] & {
    &>.sidenav-menu-icon {
      display: none;
    }
    &>.sidenav-close-icon {
      display: inline-flex;
    }
    &>ul {
      height: calc((100vh - (var(--navbar-height) * 2)) * 1);
    }
    height: fit-content;
    opacity: 1;
  }
}")))))

    (testing "vector ->"
      (testing "1 entry"
        (is (= (css-block [:c :red])
               "{\n  color: red;\n}")))

      (testing "2 entries"
        (is (= (css-block [:c   :red] [:mie :1rem])
               "{\n  color: red;\n  margin-inline-end: 1rem;\n}")))

      
      (testing "1 entry, with css calc"
        (is (= (css-block [:w "calc((100vh - (var(--navbar-height) * (2 + (6 / 2)))) * 1)"])
"{
  width: calc((100vh - (var(--navbar-height) * (2 + (6 / 2)))) * 1);
}")))

      (testing "with psdeudoclass ->"
        (testing "1 entry"
          (is (= (css-block [:last-child:c :red])
                 "{\n  &:last-child {\n    color: red;\n  }\n}")))
        (testing "2 entry"
          (is (= (css-block [:last-child:c  :red]
                            [:first-child:c :blue])
"{
  &:last-child {
    color: red;
  }
  &:first-child {
    color: blue;
  }
}")))
        (testing "1 entry, nested"
          (is (= (css-block [:last-child {:c   :red
                                          :bgc :blue}])
                 "{\n  &:last-child {\n    color: red;\n    background-color: blue;\n  }\n}")))
        
        (testing "1 entry, double nesting"
          (is (= (css-block [:hover {:bgc :blue
                                        :>p  {:c   :teal
                                              :bgc :gray}}])
"{
  &:hover {
    background-color: blue;
    &>p {
      color: teal;
      background-color: gray;
    }
  }
}"
                 
                 )))
        
        (testing "2 entries, double nesting and grouping"
          (is (= (css-block [:hover {:bgc :blue
                                     :>p  {:c   :teal
                                           :bgc :gray}}]
                            [:hover:c :yellow])
"{
  &:hover {
    background-color: blue;
    &>p {
      color: teal;
      background-color: gray;
    }
    color: yellow;
  }
}")))

        ))
    

)

  (deftest css-rule-macro
    (testing "tokenized keyword"
      (is (= (css-rule "p" :c--red)
             "p {\n  color: red;\n}")))
    (testing "tokenized keywords"
      (is (= (css-rule "p" :c--red :bgc--blue)
             "p {\n  color: red;\n  background-color: blue;\n}")))
    (testing "tokenized keyword with classname" 
      (is (= (css-rule "p" :.foo :c--red :bgc--blue)
             "p {\n  color: red;\n  background-color: blue;\n}")))
    ))
