(set-env!
  :dependencies '[[adzerk/boot-cljs           "1.7.48-4"]
                  [adzerk/boot-reload         "0.3.2"]
                  [cljsjs/markdown            "0.6.0-beta1-0"]
                  [cljsjs/mui                 "0.1.21-0"]
                  [cljsjs/vega                "2.2.4-0"]
                  [datascript                 "0.13.0"]
                  [hoplon                     "6.0.0-alpha10"]
                  [tailrecursion/boot-heredoc "0.1.0"]
                  [hoplon/boot-hoplon         "0.1.9"]
                  [hoplon/highlight           "8.4.0-0"]
                  [mathias/boot-sassc         "0.1.5"]
                  [org.clojure/clojure        "1.7.0"]
                  [org.clojure/clojurescript  "1.7.122"]]
  :source-paths   #{"src"}
  :resource-paths #{"assets" "bower_components"})

(require
  '[adzerk.boot-cljs   :refer [cljs]]
  '[adzerk.boot-reload :refer [reload]]
  '[boot.heredoc       :refer [heredoc]]
  '[mathias.boot-sassc :refer [sass]]
  '[hoplon.boot-hoplon :refer [hoplon html2cljs]])

(deftask dev
  "Build blog for local development."
  []
  (comp
    (watch)
    (speak)
    (sass
      :sass-file "blog.scss"
      :output-file "blog.css")
    (heredoc)
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
    (heredoc)
    (hoplon)
    (cljs :optimizations :advanced)))
