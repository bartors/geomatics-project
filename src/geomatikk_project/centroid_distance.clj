(ns geomatikk-project.centroid-distance
  (:require [geomatikk-project.core :as core]
            [clojure.math.numeric-tower :as math]))

(defn calculate-euclidean-distance [coordinate-1, coordinate-2]
  (Math/sqrt (+ (Math/pow (- (first coordinate-1) (first coordinate-2)) 2)
                (Math/pow (- (second coordinate-1) (second coordinate-2)) 2))))

(defn calculate-centroid-distance [polygon-1 polygon-2]
  (let [mbb-1 (get-in polygon-1 [:properties :minimum-bounding-box])
        r-1 (/ (calculate-euclidean-distance (first mbb-1) (second mbb-1)) 2)
        mbb-2 (get-in polygon-2 [:properties :minimum-bounding-box])
        r-2 (/ (calculate-euclidean-distance (first mbb-2) (second mbb-2)) 2)]
    (/ (+ r-1 r-2)
       (+ r-1 r-2 (calculate-euclidean-distance
                    (get-in polygon-1 [:properties :my-centroid])
                    (get-in polygon-2 [:properties :my-centroid]))))))

(defn describe-similarity [polygon-1 polygon-2]
  {:id-1 (get-in polygon-1 [:properties :id])
   :id-2 (get-in polygon-2 [:properties :id])
   :similarity (calculate-centroid-distance polygon-1 polygon-2)})

(defn run-one-to-many [polygons-2 polygon-1]
  (let [describe* (partial describe-similarity polygon-1)]
    (map describe* polygons-2)))

(defn run-centroid-distance-calculations [polygons-1 polygons-2]
  (let [polygons (-> (core/run-polygons polygons-1))
        mod-polygons (-> (core/run-polygons polygons-2))
        calculate-centroid-distances (partial run-one-to-many mod-polygons)]
    (map calculate-centroid-distances polygons)))