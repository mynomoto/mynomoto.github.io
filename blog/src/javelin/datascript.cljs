(ns javelin.datascript
  (:require
    [datascript :as d]
    [tailrecursion.javelin :as j]))

(defn create-cell-conn [& [schema]]
  (j/cell (d/empty-db schema)
    :meta {:listeners (atom {})}))
