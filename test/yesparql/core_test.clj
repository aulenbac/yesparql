(ns yesparql.core-test
  (:import [java.net URI URL URLEncoder])
  (:require [clojure.string :refer [upper-case]]
            [expectations :refer :all]

            [yesparql.tdb :as tdb]
            [yesparql.sparql :as sparql]
            [yesparql.core :refer :all]))

;; Test in-memory SPARQL

(defn triple-count
  [results]
  (count (get-in (sparql/result->clj results) [:results :bindings])))

(def tdb (tdb/create-bare))


(defquery select-all
  "yesparql/samples/select.sparql"
  {:connection tdb})

(defquery update-books
  "yesparql/samples/update.sparql"
  {:connection tdb})

(defquery ask-book
  "yesparql/samples/ask.sparql"
  {:connection tdb})

(defquery construct-books
  "yesparql/samples/construct.sparql"
  {:connection tdb})


;; With 4 books
(update-books)
(expect 4 (triple-count (select-all)))

(expect true (ask-book))

(expect true (not (nil? (sparql/model->json-ld (construct-books)))))

(expect true (not (nil? (sparql/model->rdf+xml (sparql/result->model (select-all))))))

(defquery select-book
  "yesparql/samples/select-bindings.sparql"
  {:connection tdb})

(expect {:type "literal", :value "A default book"}
        (:title (first (get-in
                        (sparql/result->clj (select-book {:bindings {"book" (URI. "http://example/book0")}}))
                        [:results :bindings]))))

;; Test remote SPARQL endpoints

(defquery dbpedia-select
  "yesparql/samples/remote-query.sparql"
  {:connection "http://dbpedia.org/sparql"})

(expect 10
        (triple-count
         (dbpedia-select {:bindings {"subject" (URI. "http://dbpedia.org/resource/Category:1952_deaths")}})))
