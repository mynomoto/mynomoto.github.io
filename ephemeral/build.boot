(set-env!
  :source-paths    #{"src"}
  :asset-paths  #{"asset"}
  :dependencies '[[org.clojure/clojure "1.9.0-alpha15" :scope "test"]

                  ;; Clojurescript
                  [adzerk/boot-cljs "1.7.228-2" :scope "test"]
                  [org.clojure/clojurescript "1.9.521" :scope "test"]

                  ;; Clojurescript repl
                  [adzerk/boot-cljs-repl "0.3.3" :scope "test"]
                  [com.cemerick/piggieback "0.2.1" :scope "test"]
                  [weasel "0.7.0" :scope "test"]
                  [org.clojure/tools.nrepl "0.2.13" :scope "test"]

                  ;; Auto reload
                  [adzerk/boot-reload "0.5.1" :scope "test"]

                  ;; Serve static contents
                  [tailrecursion/boot-static "0.1.0" :scope "test"]

                  ;; Create static site and upload to S3
                  [confetti "0.1.5" :scope "test"]

                  ;; Gzip all things
                  [org.martinklepsch/boot-gzip "0.1.3" :scope "test"]

                  ;; Better devtools for Clojurescript
                  [binaryage/devtools "0.9.3" :scope "test"]
                  [binaryage/dirac "1.2.4" :scope "test"]
                  [powerlaces/boot-cljs-devtools "0.2.0" :scope "test"]

                  ;; Clojurescript test
                  [crisptrutski/boot-cljs-test "0.3.0" :scope "test"]
                  [juxt/iota "0.2.3" :scope "test"]

                  ;; Rum
                  [rum "0.10.8"]

                  ;; http
                  [funcool/httpurr "0.6.2"]
                  [funcool/promesa "1.8.0"]

                  ;; util functions
                  [benefactor "0.0.1-SNAPSHOT"]
                  [medley "0.8.4"]
                  [com.andrewmcveigh/cljs-time "0.5.0-alpha2"]

                  ;; UI
                  [clj-tachyons "0.1.0"]])

(require
  '[adzerk.boot-cljs :refer [cljs]]
  '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
  '[adzerk.boot-reload :refer [reload]]
  '[boot.git :refer [clean? last-commit]]
  '[clojure.java.io :as io]
  '[clojure.string :as str]
  '[confetti.boot-confetti :refer [sync-bucket create-site]]
  '[crisptrutski.boot-cljs-test :refer [test-cljs]]
  '[org.martinklepsch.boot-gzip :refer [gzip]]
  '[powerlaces.boot-cljs-devtools :refer [cljs-devtools dirac]]
  '[tailrecursion.boot-static :refer [serve]])

(defn- last-commit*
  []
  (try (subs (last-commit) 0 8) (catch Throwable _)))

(defn- clean?*
  []
  (try (clean?) (catch Throwable _)))

(deftask build []
  (comp
    (speak)
    (cljs)))

(deftask run []
  (comp
    (watch)
    (cljs-devtools)
    (dirac)
    (cljs-repl)
    (reload)
    (build)
    (serve :port 8000)))

(deftask production []
  (task-options!
    cljs {:optimizations :advanced
          :compiler-options {:parallel-build true
                             :language-in  :ecmascript5
                             :closure-defines {'ephemeral.config/clean? (clean?*)
                                               'ephemeral.config/last-commit (last-commit*)
                                               'goog.DEBUG false}}})
  identity)

(deftask development []
  (task-options!
    cljs {:optimizations :none
          :compiler-options {:parallel-build true
                             :closure-defines {'ephemeral.config/clean? (clean?*)
                                               'ephemeral.config/last-commit (last-commit*)}}}
    reload {:on-jsload 'ephemeral.app/on-js-reload})
  identity)

(deftask dev
  "Simple alias to run application in development mode"
  []
  (comp
    (development)
    (run)))

(deftask prod
  "Simple task to generate production artifacts"
  []
  (comp
    (production)
    (build)
    (sift :include #{#"\.out"} :invert true)
    (target)))

(deftask testing []
  (merge-env! :source-paths #{"test"})
  identity)

(ns-unmap 'boot.user 'test)

(deftask test []
  (comp
    (testing)
    (test-cljs
      :closure-defines {'ephemeral.config/test? true}
      :js-env :phantom
      :exit?  true)))

(deftask auto-test []
  (comp
    (testing)
    (watch)
    (speak)
    (test-cljs
      :closure-defines {'ephemeral.config/test? true}
      :js-env :phantom)))

(def file-maps-file "file-maps.edn")

(defn ->s3-key
  [fileset-path]
  (let [p (str/replace fileset-path #"\.gz$" "")]
    p))

(defn fileset->file-maps [fileset]
  (for [[_ tmpf] (:tree fileset)
        :let [gzip? (.endsWith (:path tmpf) ".gz")]]
    {:s3-key (->s3-key (:path tmpf))
     :file (.getCanonicalPath (tmp-file tmpf))
     :metadata (when gzip? {:content-encoding "gzip"})}))

(deftask save-file-maps []
  (with-pre-wrap fs
    (let [tmp (tmp-dir!)]
      (spit (io/file tmp file-maps-file)
            (pr-str (fileset->file-maps fs)))
      (-> fs (add-resource tmp) commit!))))

(deftask deploy
  []
  (comp
    (cljs
      :optimizations :advanced)
    (sift
      :include [#"main\.out/.*" #".*\.cljs"]
      :invert true)
    (gzip
      :regex [#".*\.css$" #".*\.js$" #".*\.html$"])
    (sift
      :include [#".*\.css$" #".*\.js$" #".*\.html$"]
      :invert true)
    (save-file-maps)
    (sync-bucket
      :file-maps-path file-maps-file
      :bucket "s3-bucket-name"
      :prune true
      :access-key (System/getenv "AWS_ACCESS_KEY_ID")
      :secret-key (System/getenv "AWS_SECRET_ACCESS_KEY"))))
