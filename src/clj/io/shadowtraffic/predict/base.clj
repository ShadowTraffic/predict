(ns io.shadowtraffic.predict.base
  (:require [cheshire.core :as json]))

(defn keys-in [m]
  (if (map? m)
    (vec 
     (mapcat (fn [[k v]]
               (let [sub (keys-in v)
                     nested (map #(into [k] %) (filter (comp not empty?) sub))]
                 (if (seq nested)
                   nested
                   [[k]])))
             m))
    []))

(defn init-number [x]
  {:count 1
   :mean x
   :m2 0})

(defn process-number [subprofile x opts]
  (let [n (inc (:count subprofile))
        delta (- x (:mean subprofile))
        mean (+ (:mean subprofile) (/ delta n))
        m2 (+ (:m2 subprofile) (* delta (- x mean)))]
    (assoc subprofile :count n :mean mean :m2 m2)))

(defn init-string [x]
  {:elements [x]
   :n 1})

(defn process-string [subprofile x {:keys [reservoir-size] :as opts}]
  (if x
    (let [new-el (if (< (count (:elements subprofile)) reservoir-size)
                   (update subprofile :elements conj x)
                   (let [k (rand-int (inc (+ (:n subprofile) reservoir-size)))]
                     (if (< k reservoir-size)
                       (assoc-in subprofile [:elements k] x)
                       subprofile)))]
      (update new-el :n inc))
    subprofile))

(defn init-subprofile [x]
  (cond (boolean? x)
        {:kind :boolean}

        (string? x)
        (into {:kind :string} (init-string x))

        (integer? x)
        (into {:kind :integer} (init-number x))

        (double? x)
        (into {:kind :double} (init-number x))

        :else
        {:kind :unknown}))

(defn integrate-message [subprofile x opts]
  (case (:kind subprofile)
    :boolean subprofile
    :string (process-string subprofile x opts)
    :integer (process-number subprofile x opts)
    :double (process-number subprofile x opts)
    :unknown subprofile
    subprofile))

(defn deserialize-json [x]
  (json/parse-string (String. x "UTF-8")))
