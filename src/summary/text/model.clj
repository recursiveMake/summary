(ns summary.text.model
  (:require [summary.text.parse :refer [split-word split-sentence split-paragraph]]
            [summary.text.score :refer [average-length score-sentence word-weight word-frequency word-significance]]))

(defrecord Document [paragraphs])
(defrecord Paragraph [sentences])
(defrecord Sentence [text score])

(defn make-sentences
  [text weights significance]
  (let [sentences (split-sentence text)
        words (map split-word sentences)
        sentence-length (int (average-length words))
        scores (map #(score-sentence % weights significance sentence-length) words)]
    (map ->Sentence sentences scores)))

(defn make-paragraphs
  [text frequencies]
  (let [paragraphs (split-paragraph text)
        weights (map word-weight paragraphs)
        significance (map #(word-significance % frequencies) paragraphs)
        sentences (map make-sentences paragraphs weights significance)]
    (map ->Paragraph sentences)))

(defn make-document
  [text]
  (let [frequencies (word-frequency text)
        paragraphs (make-paragraphs text frequencies)]
    (Document. paragraphs)))
