(set-env!
  :dependencies  '[[adzerk/boot-cljs-repl     "0.1.9"]
                   [adzerk/boot-cljs          "0.0-2814-3"]
                   [adzerk/boot-reload        "0.2.6"]
                   [cljsjs/mui                "0.0.5-0"]
                   [hoplon/highlight          "8.4.0-0"]
                   [cljsjs/boot-cljsjs        "0.4.7"]
                   [mathias/boot-sassc        "0.1.1"]
                   [tailrecursion/boot-hoplon "0.1.0-SNAPSHOT"]
                   [tailrecursion/hoplon      "6.0.0-SNAPSHOT"]
                   [tailrecursion/javelin     "3.8.0"]]
  :source-paths   #{"src"}
  :resource-paths #{"assets" "bower_components"})

(require
  '[adzerk.boot-cljs          :refer [cljs]]
  '[adzerk.boot-reload        :refer [reload]]
  '[adzerk.boot-cljs-repl     :refer [cljs-repl start-repl]]
  '[cljsjs.boot-cljsjs        :refer [from-cljsjs]]
  '[mathias.boot-sassc        :refer [sass]]
  '[tailrecursion.boot-hoplon :refer [haml hoplon prerender html2cljs]])

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
    (reload)
    (cljs)))

(deftask prod
  "Build blog for production deployment."
  []
  (comp
    (hoplon)
    (cljs :optimizations :advanced)
    (prerender)))
