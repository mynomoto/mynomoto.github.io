(set-env!
  :dependencies '[[adzerk/boot-cljs          "0.0-3308-0"]
                  [adzerk/boot-reload        "0.3.0"]
                  [cljsjs/markdown           "0.6.0-beta1-0"]
                  [cljsjs/mui                "0.1.8-0"]
                  [cljsjs/vega               "1.5.0-0"]
                  [datascript                "0.11.4"]
                  [hoplon/highlight          "8.4.0-0"]
                  [mathias/boot-sassc        "0.1.1"]
                  [org.clojure/clojure       "1.7-0"]
                  [org.clojure/clojurescript "0.0-3308"]
                  [tailrecursion/boot-hoplon "0.1.0"]
                  [tailrecursion/hoplon      "6.0.0-alpha1"]
                  [tailrecursion/javelin     "3.8.0"]]
  :source-paths   #{"src"}
  :resource-paths #{"assets" "bower_components"})

(require
  '[adzerk.boot-cljs          :refer [cljs]]
  '[adzerk.boot-reload        :refer [reload]]
  '[mathias.boot-sassc        :refer [sass]]
  '[tailrecursion.boot-hoplon :refer [haml hoplon html2cljs]])

(deftask dev
  "Build blog for local development."
  []
  (comp
    (watch)
    (speak)
    (sass
      :sass-file "blog.scss"
      :output-file "blog.css")
    (hoplon)
    (reload
      :on-jsload 'blog.db/add-posts)
    (cljs)))

(deftask prod
  "Build blog for production deployment."
  []
  (comp
    (sass
      :sass-file "blog.scss"
      :output-file "blog.css")
    (hoplon)
    (cljs :optimizations :advanced)))
