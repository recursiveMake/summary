(defproject summary "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://github.com/recursiveMake/summary"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.cli "1.0.194"]
                 [com.taoensso/timbre "4.10.0"]
                 [org.apache.opennlp/opennlp-tools "1.9.2"]
                 [org.apache.lucene/lucene-analyzers-common "8.5.1"]]
  :main summary.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
