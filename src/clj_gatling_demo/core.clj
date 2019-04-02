(ns clj-gatling-demo.core
  (require [clj-gatling.core :as gatling]
           [org.httpkit.client :as client]
           )
  (:gen-class))

(defn ->analytics-request
  "Returns an httpkit request map"
  [{:keys [page-id page-url screen-resolution event-timestamp visitor-id]}]
  {:url          "https://events-integration.ub-analytics.com/i"
   :query-params {:url         page-url
                  :res         screen-resolution
                  :stm         [event-timestamp "480"]
                  :tz          480
                  :aid         "amp"
                  :tv          "amp-0.2"
                  :cd          24
                  :cs          "UTF-8"
                  :duid        visitor-id
                  :lang        "en"
                  :e           "pv"
                  :eventSource "amp"
                  :eventTime   event-timestamp
                  :pageId      page-id
                  :pageVariant "a"
                  :visitorId   visitor-id
                  :eventType   "visit"}
   :headers      {"Referer"    page-url
                  "User-Agent" "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Mobile Safari/537.36"}
   })

(->analytics-request {:page-id "some-id"
                      :page-url "https://5.tynan.ca/sano-amp/amp.html"
                      :screen-resolution "723x1198"
                      :event-timestamp 1547573119051
                      :visitor-id "amp-_y1LFKeQ8C2kk30Y3TU2KA"})

;;   "https://events-integration.ub-analytics.com/i?url=https%3A%2F%2F5.tynan.ca%2Fsano-amp%2Famp.html&page=&res=723x1198&stm=1547573119049&tz=480&aid=amp&p=web&tv=amp-0.2&cd=24&cs=UTF-8&duid=amp-PCD4y1lVGyaPw0x-3-j9Aw&lang=en-us&refr=&stm=480&vp=723x1198&e=pv&eventSource=amp&eventTime=1547573119051&pageId=482bd0f2-e786-4cd8-ba39-83272eb2b9ba&pageVariant=a&visitorId=amp-_y1LFKeQ8C2kk30Y3TU2KA&eventType=visit'"
@(client/request (->analytics-request {:page-id           "482bd0f2-e786-4cd8-ba39-83272eb2b9ba"
                                       :page-url          "https://5.tynan.ca/sano-amp/amp.html"
                                       :screen-resolution "723x1198"
                                       :event-timestamp   1547573119051
                                       :visitor-id        "amp-_y1LFKeQ8C2kk30Y3TU2KA"}))

(defn- snowplow-collector-request [_]
  (let [{:keys [status]} @(client/request (->analytics-request {:page-id           "482bd0f2-e786-4cd8-ba39-83272eb2b9ba"
                                                                :page-url          "https://5.tynan.ca/sano-amp/amp.html"
                                                                :screen-resolution "723x1198"
                                                                :event-timestamp   1547573119051
                                                                :visitor-id        "amp-_y1LFKeQ8C2kk30Y3TU2KA"}))]
    (= status 200)))

(defn- ramp-up-distribution [percentage-at _]
  (cond
    (< percentage-at 0.1) 0.1
    (< percentage-at 0.2) 0.2
    :else 1.0))

(defn -main
  "Run Gatling load test"
  [& args]
  (println "Starting run...")
  (gatling/run
    {:name      "Simulation"
     :scenarios [{:name    "app-test scenario"
                  :steps [
                          {:name "Root" :request snowplow-collector-request}
                          ]
                  }
                 ]
     }
    {:concurrency 10
     :requests 1000
     :error-file "./error.log"
     :concurrency-distribution ramp-up-distribution}))
