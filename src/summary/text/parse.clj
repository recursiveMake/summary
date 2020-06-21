(ns summary.text.parse
  (:require [summary.utils :refer [construct]]
            [clojure.string :as str]
            [clojure.java.io :as io])
  (:import [java.text BreakIterator]
           [org.apache.lucene.analysis.en EnglishAnalyzer]
           [org.apache.lucene.analysis.tokenattributes CharTermAttribute]
           [opennlp.tools.postag POSModel]
           [opennlp.tools.postag POSTaggerME]
           [opennlp.tools.tokenize TokenizerModel]
           [opennlp.tools.tokenize TokenizerME]))

(defn split-word
  "Tokeninze and de-stem text"
  [text]
  (let [analyzer (EnglishAnalyzer.)
        reader (io/reader (char-array text))
        token-stream (.tokenStream analyzer "text" reader)
        term-attribute (.addAttribute token-stream CharTermAttribute)]
    (.reset token-stream)
    (try
      (loop [tokens []]
        (if (.incrementToken token-stream)
          (recur (conj tokens (.toString term-attribute)))
          tokens))
      (finally
        (.end token-stream)
        (.close token-stream)))))

(defn split-sentence
  "Split text into sentences"
  [text]
  (let [boundary (BreakIterator/getSentenceInstance)]
    (.setText boundary text)
    (loop [sentences []
           start (.first boundary)
           end (.next boundary)]
      (if (= end BreakIterator/DONE)
        sentences
        (recur (conj sentences (.substring text start end)) end (.next boundary))))))

(defn split-paragraph
  "Split text into paragraphs"
  [text]
  (->> text
       (str/split-lines)
       (remove str/blank?)))

;; POS

(def ^{:private true} tags
  {:noun ["NN" "NNS" "NNP" "NNPS" "PRP" "PRP$" "WP" "WP$"]
   :verb ["VB" "VBD" "VBG" "VBN" "VBP" "VBZ"]
   :adjective ["JJ" "JJR" "JJS"]
   :adverb ["RB" "RBR" "RBS" "WRB"]})

(defn- invert-tag
  "Remap POSTagger symbols to parts of speech"
  [all-tags]
  (letfn [(invert [tag]
            (let [[old-key, old-val] tag
                  new-keys (map keyword old-val)]
              (zipmap new-keys (repeat old-key))))]
    (->> all-tags
         (map invert)
         (apply conj))))

(defn- load-into-model
  "Load resource into specified model"
  [Model resource]
  (->> resource
       (io/resource)
       (io/file)
       ;; (io/input-stream)
       (construct Model)))

(defn- tokenize
  "Tokenize using OpenNLP's max entropy tokenizer"
  [text]
  (let [model (load-into-model TokenizerModel "en-token.bin")
        tokenizer (TokenizerME. model)]
    (seq (.tokenize tokenizer text))))

(defn tag-text
  "Label word's part of speech"
  [text]
  (let [model (load-into-model POSModel "en-pos-maxent.bin")
        tagger (POSTaggerME. model)
        tokens (tokenize text)]
    (->> tokens
         (into-array String)
         (.tag tagger)
         (map keyword)
         (map #(assoc {} %1 %2) tokens))))
