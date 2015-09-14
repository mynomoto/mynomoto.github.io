(ns javelin.datascript
  (:require
    [datascript :as d]
    [datascript.core :as dc]
    [javelin.core :as j]))

(defn create-cell-conn [& [schema]]
  (j/cell (d/empty-db schema)
    :meta {:listeners (atom {})}))

(defn conn? [conn]
  (dc/db? @conn))

(defn -transact! [conn tx-data tx-meta]
  {:pre [(conn? conn)]}
  (let [report (atom nil)]
    (swap! conn (fn [db]
                  (let [r (d/with db tx-data tx-meta)]
                    (reset! report r)
                    (:db-after r))))
    @report))

(defn transact!
  ([conn tx-data] (transact! conn tx-data nil))
  ([conn tx-data tx-meta]
    {:pre [(conn? conn)]}
    (let [report (-transact! conn tx-data tx-meta)]
      (doseq [[_ callback] @(:listeners (meta conn))]
        (callback report))
      report)))
