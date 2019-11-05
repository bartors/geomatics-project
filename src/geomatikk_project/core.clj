(ns geomatikk-project.core
  (:require [clojure.data.json :as json]))

(def square [{:type     "Feature"
              :properties {:my-id-field 0, :my-area-field 100}
              :geometry {:type        "Polygon"
                         :coordinates [[[10 10]
                                        [20 10]
                                        [20 20]
                                        [10 20]
                                        [10 10]]]}}] )

(def square-holes [{:type       "Feature"
                    :properties {:my-id-field 0, :my-area-field 1200}
                    :geometry   {:type        "Polygon"
                                 :coordinates [[[10 10]
                                                [50 10]
                                                [50 50]
                                                [10 50]
                                                [10 10]]
                                               [[20 20]
                                                [40 20]
                                                [40 40]
                                                [20 40]
                                                [20 20]]]}}])

(def polygons (get (json/read-str (slurp "./resources/Polygon-utm.geojson") :key-fn keyword)
                   :features))

(def centroids (get (json/read-str (slurp "./resources/centroids-utm-2.geojson") :key-fn keyword)
                    :features))


(def modified-polygons (get (json/read-str (slurp "./resources/Polygon-mod-utm.geojson") :key-fn keyword)
                            :features))
(defn combine-vectors [vec-1 vec-2]
  (-> (conj [] vec-1)
      (conj vec-2)))



(defn get-point-pairs [coordinates]
  (let [_ (println coordinates)
        points (-> (reverse coordinates)
                   rest
                   reverse)
        next-points (rest coordinates)]
    (->> (map combine-vectors points next-points)
         (assoc {} :coordinates))))

(defn sum-centroid-x [sum [point next-point]]
  (let [point-x (first point)
        point-y (second point)
        next-point-x (first next-point)
        next-point-y (second next-point)]
    (+ sum (* (+ point-x next-point-x)
              (- (* point-x next-point-y)
                 (* next-point-x point-y))))))

(defn sum-centroid-y [sum [point next-point]]
  (let [point-x (first point)
        point-y (second point)
        next-point-x (first next-point)
        next-point-y (second next-point)]
    (+ sum
       (* (+ point-y next-point-y)
          (- (* point-x next-point-y)
             (* next-point-x point-y))))))

(defn calculate-axis-centre [area sum]
  (/ sum (* 6 area)))

(defn area-sum [sum [point next-point]]
  (let [point-x (first point)
        point-y (second point)
        next-point-x (first next-point)
        next-point-y (second next-point)]
    (+ sum
       (- (* point-x
             next-point-y)
          (* next-point-x point-y)))))

(defn calculate-area [sum]
  (/ sum 2))

(defn get-area [points]
  (->> points
       (reduce area-sum 0)
       calculate-area))

(defn calculate-centroid [polygon]
  (let [ point-pairs (:coordinates polygon)
        area (get-area point-pairs)
        centroid [(->> point-pairs
                       (reduce sum-centroid-x 0)
                       (calculate-axis-centre area))
                  (->> point-pairs
                       (reduce sum-centroid-y 0)
                       (calculate-axis-centre area))]]
    (->(assoc polygon :area area)
       (assoc :centroid centroid))))

(defn get-sub-polygons [polygon]
  (->> (get-in polygon [:geometry :coordinates])
       (map get-point-pairs)
       (map calculate-centroid)))

(defn shrink-area [area polygon]
  (- area (:area polygon)))

(defn x-divident [minuend polygon]
  (- minuend (* (:area polygon) (first (:centroid polygon)))))

(defn y-divident [minuend polygon]
  (- minuend (* (:area polygon) (second (:centroid polygon)))))

(defn divisor [value polygon]
  (- value (:area polygon)))

(defn calculate-top-level-attributes [polygon]
  (let [properties (:properties polygon)
        sub-polygons (get-sub-polygons polygon)
        main-polygon (first sub-polygons)
        rest-of-polygons (rest sub-polygons)
        _ (println sub-polygons)
        divisor (reduce divisor
                        (:area main-polygon)
                        rest-of-polygons)
        x-divident (reduce x-divident
                           (* (:area main-polygon)
                                       (first (:centroid main-polygon)))
                           rest-of-polygons)

        y-divident (reduce y-divident
                           (* (:area main-polygon)
                              (last (:centroid main-polygon)))
                           rest-of-polygons)
        ]
    (->  (assoc-in polygon
                   [:properties :area]
                   (reduce shrink-area
                           (:area main-polygon)
                           rest-of-polygons))
         (assoc-in [:properties :centroid]
                   [(/ x-divident divisor) (/ y-divident divisor)]))))



;; https://math.stackexchange.com/questions/623841/finding-centroid-of-polygon-with-holes-polygons

