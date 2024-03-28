(ns io.shadowtraffic.predict.kafka
  (:require [clojure.string :as s]
            [cheshire.core :as json]
            [io.shadowtraffic.predict.base :as b]
            [io.shadowtraffic.predict.ollama :as ai])
  (:import [java.util Properties]
           [java.time Duration]
           [org.apache.kafka.clients.consumer KafkaConsumer]))

(defn prefix [prefix xs]
  (into [prefix] xs))

(defn to-json [message]
  {"topic" (.topic message)
   "key" (.key message)
   "value" (.value message)})

(defn init-profile? [profile messages]
  (if (and (empty? profile) (> (.count messages) 0))
    (let [head (to-json (first messages))
          key-paths (map (partial prefix "key") (b/keys-in (get head "key")))
          val-paths (map (partial prefix "value") (b/keys-in (get head "value")))
          all-paths (into key-paths val-paths)]
      (zipmap all-paths (repeat {})))
    profile))

(defn update-profile [profile messages opts]
  (let [profile (init-profile? profile messages)]
    (reduce
     (fn [profile message]
       (let [as-json (to-json message)]
         (reduce
          (fn [profile path]
            (let [x (get-in as-json path)
                  subprofile (get-in profile [path (type x)])
                  result (if (nil? subprofile)
                           (b/init-subprofile x)
                           (b/integrate-message subprofile x opts))]
              (assoc-in profile [path (type x)] result)))
          profile
          (keys profile))))
     profile
     messages)))

(defn update-history [history messages opts]
  (-> history
      (update :n + (.count messages))
      (update :profile update-profile messages opts)))

(defn profile-topic [{:strs [predict producerConfigs consumerConfigs] :as connection-config} {:strs [topic] :as target}]
  (let [key-deserializer (get target "key.deserializer" (get consumerConfigs "key.deserializer"))
        value-deserializer (get target "value.deserializer" (get consumerConfigs "value.deserializer"))
        deserializers {"key.deserializer" key-deserializer
                       "value.deserializer" value-deserializer}

        consumer-configs (merge producerConfigs deserializers)
        group-id (format "shadowtraffic-%s" (random-uuid))
        consumer-configs (assoc consumer-configs "group.id" group-id "auto.offset.reset" "earliest")
        consumer-props (doto (Properties.) (.putAll consumer-configs))
        consumer (KafkaConsumer. consumer-props)]
    (.subscribe consumer [topic])

    (try
      (let [history (atom {:n 0 :profile {}})
            {:strs [sampleSize reservoirSize]} predict
            sample-size (or sampleSize 500)
            opts {:reservoir-size (or reservoirSize 10)}]
        (loop []
          (let [{:keys [n]} @history]
            (when (< n sample-size)
              (let [messages (.poll consumer (Duration/ofMillis 10))]
                (swap! history update-history messages opts)
                (recur)))))
        (:profile @history))
      (finally
        (.close consumer)))))

(defn prediction-input [connection-config]
  (mapv
   (fn [{:strs [topic] :as target}]
     {:topic topic
      :profile (profile-topic connection-config target)})
   (get-in connection-config ["predict" "topics"])))

(defn make-generators [topics llm-opts]
  (map
   (fn [{:keys [topic profile]}]
     (reduce-kv
      (fn [generator path specs]
        (ai/integrate-specs generator path (vals specs) llm-opts))
      {"topic" topic}
      profile))
   topics))


(comment
  (let [llm-opts {:model "mistral:7b-instruct"
                  :url "http://127.0.0.1:11434"
                  :seed 42}
        topics (prediction-input
                {"kind" "kafka"
                 "producerConfigs" {"bootstrap.servers" "localhost:9092"
                                    "key.serializer" "io.shadowtraffic.kafka.serdes.JsonSerializer"
                                    "value.serializer" "io.shadowtraffic.kafka.serdes.JsonSerializer"}
                 "consumerConfigs" {"key.deserializer" "io.shadowtraffic.kafka.serdes.JsonDeserializer"
                                    "value.deserializer" "io.shadowtraffic.kafka.serdes.JsonDeserializer"}
                 "predict" {"topics" [{"topic" "customers"}
                                      {"topic" "orders"}]}})
        generators (make-generators topics llm-opts)
        generator-json (json/generate-string {"generators" generators} {:pretty true})]
    (println generator-json))
  )
