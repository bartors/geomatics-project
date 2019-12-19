(ns geomatikk-project.centroid-distance
  (:import com.vividsolutions.jts.algorithm.match.HausdorffSimilarityMeasure)
  (:require [geomatikk-project.core :as core]
            [clojure.math.numeric-tower :as math]
            [meridian.clj-jts :as jts]
            ))

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
  {:id-1       (get-in polygon-1 [:properties :id])
   :id-2       (get-in polygon-2 [:properties :id])
   :similarity (calculate-centroid-distance polygon-1 polygon-2)})

(defn run-one-to-many [polygons-2 polygon-1]
  (let [describe* (partial describe-similarity polygon-1)]
    (map describe* polygons-2)))

(defn run-centroid-distance-calculations [polygons-1 polygons-2]
  (let [polygons (-> (core/run-polygons polygons-1))
        mod-polygons (-> (core/run-polygons polygons-2))
        calculate-centroid-distances (partial run-one-to-many mod-polygons)]
    (map calculate-centroid-distances polygons)))

(defn hausdorff-sim-measure [hausdorff polygon-1 polygon-2]
  {:id-1       (get-in polygon-1 [:properties :id])
   :id-2       (get-in polygon-2 [:properties :id])
   :similarity (.measure hausdorff (get polygon-1 :polygon) (get polygon-2 :polygon))})

(defn run-hausdorff [hausdorff polygons-2 polygon]
  (let [haus (partial hausdorff-sim-measure hausdorff polygon)]
    (map haus polygons-2)))

(defn create-polygon [polygon]
  (assoc polygon :polygon (jts/polygon (get-in polygon [:geometry :coordinates]))))

(defn run-hausdorff-matching [polygons-1 polygons-2]
  (let [hausdorff-measure (new HausdorffSimilarityMeasure)
        polygons (map create-polygon polygons-1)
        mod-polygons (map create-polygon polygons-2)
        hausdorff (partial run-hausdorff hausdorff-measure mod-polygons)]
    (map hausdorff polygons)))

(defn my-get [col]
  (get col :similarity))

(defn lower-simplyfy [collection]
  (map my-get collection))

(defn simplyfy [collection]
  (map lower-simplyfy collection))

(defn get-max [coll]
  (map (fn [x]
         (apply max x)) coll))

(defn get-mean [coll]
  (/ (reduce + (get-max coll)) 100))

(defn get-sd [coll mean]
  (Math/sqrt (/ (reduce + (map (fn [x] (* (- x mean) (- x mean)))
                               (get-max coll))) 100))
  )