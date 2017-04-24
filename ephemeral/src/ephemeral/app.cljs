(ns ephemeral.app
  (:require [rum.core :as rum]
            [ephemeral.ui.atoms :as a]))

(defn init []
  (rum/mount (a/container) (.getElementById js/document "container")))

(init)

(defn on-js-reload []
  (init))
