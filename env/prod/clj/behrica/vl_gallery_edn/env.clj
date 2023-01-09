(ns behrica.vl-gallery-edn.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init       (fn []
                 (log/info "\n-=[behrica.vl-gallery-edn starting]=-"))
   :start      (fn []
                 (log/info "\n-=[behrica.vl-gallery-edn started successfully]=-"))
   :stop       (fn []
                 (log/info "\n-=[behrica.vl-gallery-edn has shut down successfully]=-"))
   :middleware (fn [handler _] handler)
   :opts       {:profile :prod}})
