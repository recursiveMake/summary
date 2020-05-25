(ns summary.text.score
  (:require [taoensso.timbre :as log]
            [summary.text.parse :refer [split-word]]))

(defn word-frequency
  "Get word frequency in text (scaled)"
  [text]
  (let [freqs (frequencies (split-word text))
        ;total (reduce + (map second freqs))
        total 1]
    (zipmap (keys freqs) (map #(/ % total) (vals freqs)))))

(defn word-significance
  "Calculate a score for importance of each word (document based)"
  [text doc-freq]
  (let [text-freq (word-frequency text)
        score (fn [fg-freq bg-freq]
                ;(max (* (- fg-freq bg-freq) (/ fg-freq bg-freq)) 0)
                (max (* (- fg-freq bg-freq) (/ fg-freq bg-freq)) 1))]
    (->> (keys text-freq)
         (map #(score (get text-freq %) (get doc-freq %)))
         (zipmap (keys text-freq)))))

(defn word-weight
  "Calculate the weight of words in text (paragraph based)"
  [text]
  (let [freqs (frequencies (split-word text))
        max-word (apply max (map second freqs))]
    (zipmap (keys freqs) (map #(/ % max-word) (vals freqs)))))

(defn average-length
  [tokens]
  (let [lengths (map count tokens)
        items (count tokens)
        avg-len (/ (reduce + lengths) items)]
    (log/debug "Average length:" (float avg-len))
    avg-len))

(defn score-word
  "Calculate a score for a word (combine weight and significance)"
  [word weights significance]
  (let [weight (get weights word)
        sig (get significance word)
        word-score (float (* weight sig))]
    (log/debug word word-score "(" (float weight) "*" (float sig) ")")
    word-score))

(defn score-sentence
  "Calculate a score for a sentence (sum word scores)"
  [words weights significance limit]
  (let [scores (map #(score-word % weights significance) words)
        norm-scores (take limit (reverse (sort scores)))
        sentence-score (reduce + norm-scores)]
    (log/debug (zipmap words scores))
    (log/debug "Sentence score:" sentence-score)
    sentence-score))
