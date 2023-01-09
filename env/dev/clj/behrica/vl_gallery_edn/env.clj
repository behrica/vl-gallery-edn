(ns behrica.vl-gallery-edn.env
  (:require
    [clojure.tools.logging :as log]
    [behrica.vl-gallery-edn.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init       (fn []
                 (log/info "\n-=[behrica.vl-gallery-edn starting using the development or test profile]=-"))
   :start      (fn []
                 (log/info "\n-=[behrica.vl-gallery-edn started successfully using the development or test profile]=-"))
   :stop       (fn []
                 (log/info "\n-=[behrica.vl-gallery-edn has shut down successfully]=-"))
   :middleware wrap-dev
   :opts       {:profile       :dev
                :persist-data? true}})
