(defproject geomatikk-project "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/math.numeric-tower "0.0.4"]
                 [org.locationtech.jts/jts-core "1.16.1"]
                 [com.vividsolutions/jts "1.13"]
                 [factual/geo "3.0.1"]
                 [meridian/clj-jts "0.0.2"]]
  :repl-options {:init-ns geomatikk-project.core
                })
