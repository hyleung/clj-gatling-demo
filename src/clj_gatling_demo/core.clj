(ns clj-gatling-demo.core
  (require [clj-gatling.core :as gatling]
           [org.httpkit.client :as client]
           )
  (:gen-class))

(defn- localhost-request [_]
  (let [{:keys [status]} @(client/get "http://localhost:8081/hello")]
    (= status 200)))

(defn- ramp-up-distribution [percentage-at _]
  (cond
    (< percentage-at 0.1) 0.1
    (< percentage-at 0.2) 0.2
    :else 1.0))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Starting run...")
  (gatling/run
    {:name      "Simulation"
     :scenarios [{:name    "Localhost test scenario"
                  :steps [
                          {:name "Root" :request localhost-request}
                          ]
                  }
                 ]
     }
    {:concurrency 10
     :concurrency-distribution ramp-up-distribution}))
