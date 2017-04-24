(ns ephemeral.ui.atoms
  (:require [rum.core :as rum]
            [clj-tachyons.core :as tachyons]))

(rum/defc container
  []
  [:div
   (for [size (concat [:f-6 :f-5] (map #(keyword (str "f" %)) (range 1 8)))]
     [:h1 {:class (tachyons/generate-class [size])} "A tachyons experiment"])
   [:p "A paragraph"]])
