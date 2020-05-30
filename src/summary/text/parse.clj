(ns summary.text.parse
  (:require [clojure.string :as str]
            [clojure.java.io :as io])
  (:import [java.text BreakIterator]
           [org.apache.lucene.analysis.en EnglishAnalyzer]
           [org.apache.lucene.analysis.tokenattributes CharTermAttribute]))

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
