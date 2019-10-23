(ns geomatikk-project.core
  (:require [clojure.data.json :as json]))

(def square {:type     "Feature"
             :geometry {:type        "Polygon"
                        :coordinates [[[10 10]
                                       [20 10]
                                       [20 20]
                                       [10 20]
                                       [10 10]]]}})

(def square-holes {:type "Feature"
                   :geometry {:type "Polygon"
                              :coordinates [[[10 10]
                                             [50 10]
                                             [50 50]
                                             [10 50]
                                             [10 10]]
                                            [[20 20]
                                             [30 20]
                                             [30 30]
                                             [20 30]
                                             [20 20]]]}})

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
         (assoc {} :coordinates ))))


(defn get-sub-polygons [polygon]
  (->> (get-in polygon [:geometry :coordinates])
       (map get-point-pairs)))

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
  (let [
        ;this needs to be rewritten
        point-pairs (get-point-pairs polygon)
        area (get-area point-pairs)]
    [(->> point-pairs
          (reduce sum-centroid-x 0)
          (calculate-axis-centre area))
     (->> point-pairs
          (reduce sum-centroid-y 0)
          (calculate-axis-centre area))]))


;; https://math.stackexchange.com/questions/623841/finding-centroid-of-polygon-with-holes-polygons

