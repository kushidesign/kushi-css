(defproject design.kushi/kushi-css "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 ]
  :repl-options {:init-ns kushi-css.core}
  :plugins [[lein-auto "0.1.3"]]
  :profiles {:dev {:source-paths [
                                  ;; "../fireworks/src"
                                  ;; "../bling/src/"
                              
                                  ;;  for local testing
                                  "../humane-test-output-master/src"
                                  ]
                   :dependencies [[babashka/process "0.5.22"]
                                  [io.github.paintparty/fireworks "0.10.2"]
                                  [io.github.paintparty/bling "0.4.2"]
                                  ;; for testing & profiling
                                  [com.taoensso/tufte "2.6.3"]                
                                  ]}})
