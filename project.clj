(defproject io.shadowtraffic/predict "0.1.0-SNAPSHOT"
  :description "Module for inspecting live systems and predicting ShadowTraffic configuration"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}

  :source-paths ["src/clj"]
  :java-source-paths ["src/java"]
  
  :repositories {"confluent" "https://packages.confluent.io/maven/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.apache.kafka/kafka-clients "7.4.0-ccs"]
                 [cheshire "5.12.0"]
                 [clj-http "3.12.3"]]
  :repl-options {:init-ns io.shadowtraffic.predict.kafka})
