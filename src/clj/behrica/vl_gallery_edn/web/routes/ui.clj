(ns behrica.vl-gallery-edn.web.routes.ui
  (:require
   [behrica.vl-gallery-edn.web.middleware.exception :as exception]
   [behrica.vl-gallery-edn.web.middleware.formats :as formats]
   [behrica.vl-gallery-edn.web.routes.utils :as utils]
   [behrica.vl-gallery-edn.web.htmx :refer [ui page] :as htmx]
   [integrant.core :as ig]
   [cheshire.core :as json]
   [clojure.java.io :as io]
   [puget.printer :as puget]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [reitit.ring.middleware.parameters :as parameters]))

(def vl-examples (-> (io/as-url  "https://raw.githubusercontent.com/vega/vega-lite/next/site/_data/examples.json")
                     io/reader
                     (json/parse-stream keyword)))

(def vl-infos
  (->> (for  [[k-1 v-1] vl-examples [k-2 v-2] v-1]

         (map (fn [m]
                (assoc m
                       :level-1 k-1
                       :level-2 k-2))
              v-2))
       (flatten)))

(defn collect-info [vl-info]
  (let [
        vl-spec (slurp (io/resource (format  "vl-specs/%s.vl.json" (:name vl-info))))
        edn-spec (puget/pprint-str
                  (json/parse-string vl-spec keyword)
                  {:map-delimiter ""})
        img-file-url (format  "https://github.com/vega/vega-lite/raw/next/examples/compiled/%s.svg" (:name vl-info))]
    (assoc vl-info
           :img-file-url img-file-url
           :vl-spec vl-spec
           :edn-spec edn-spec)))



(defn info->hiccup [collected-info]
  [:div
   [:a {:id (format "%s" (:name collected-info))}]
   [:h3  (:title collected-info)]
   [:p (:description collected-info)]
   [:img {:src  (:img-file-url collected-info)}]
   [:a {:href (format  "https://vega.github.io/editor/#/examples/vega-lite/%s" (:name collected-info))}
    "View this example in the online editor"]
   [:h5 "edn"]
   [:div [:pre (:edn-spec collected-info)]]])

(defn example-link [vl-info link]
  [:a {:href ""
        :hx-push-url (str "/" (:name vl-info))
        :hx-get "/example-clicked"
        :hx-target "#content"
        :hx-swap "outerHTML"
        :hx-vals (json/generate-string  {:name (:name vl-info)})}
   link])

(defn make-td [vl-info]
  [:td {:style {:padding "8px"
                :border-bottom "1px solid #ddd"}}
   (example-link vl-info (:title vl-info))

   [:div {:style {:height "100px"
                  :overflow-y "hidden"}}

    (example-link vl-info [:img {:src (:img-file-url vl-info)
                                 :width "100px"
                                 :height "100px"}])]])

(defn example-overview []
  (apply vector :table {:id "content"
                        :style {:cellspacing 0}}
         (map
          (fn [infos]
            [:tr {:height "100px"}
             (make-td (first infos))
             (make-td (second infos))
             (make-td (get (vec infos) 2))
             (make-td (get (vec infos) 3))
             (make-td (get (vec infos) 4))])
          (->> vl-infos
               (remove #(contains? #{"bar_count_minimap" "geo_trellis"}
                                   (:name %)))
               (map collect-info)
               (partition-all 5)))))



(defn home [request]
  (page
   [:head
    [:meta {:charset "UTF-8"}]
    [:title "Htmx + Kit"]
    [:script {:src "https://unpkg.com/htmx.org@1.8.4/dist/htmx.min.js" :defer true}]]
    ;; [:script {:src "https://unpkg.com/hyperscript.org@0.9.5" :defer true}]

   [:body {:hx-push-url "/index"}
    [:h1 "Vega Lite example gallery in EDN format"]
    (example-overview)]))


(defn example-clicked [request]
  (ui
   (->> vl-infos
        (filter #(= (:name %)
                    (-> request :params :name)))
     first
     collect-info info->hiccup)))



;; Routes
(defn ui-routes [_opts]
  [["/" {:get home}]
   ["/index" {:get home}]
   ["/example-clicked" {:get example-clicked}]])



(defn route-data [opts]
  (merge
   opts
   {:muuntaja   formats/instance
    :middleware
    [;; Default middleware for ui
     ;; query-params & form-params
     parameters/parameters-middleware
     ;; encoding response body
     muuntaja/format-response-middleware
     ;; exception handling
     exception/wrap-exception]}))

(derive :reitit.routes/ui :reitit/routes)

(defmethod ig/init-key :reitit.routes/ui
  [_ {:keys [base-path]
      :or   {base-path ""}
      :as   opts}]
  [base-path (route-data opts) (ui-routes opts)])
