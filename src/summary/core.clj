(ns summary.core
  (:require [summary.cli :refer [validate-args]]
            [summary.text.model :refer [make-document]]
            [taoensso.timbre :as log])
  (:gen-class))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn summarize
  [filename limit]
  (let [text (slurp filename)
        document (make-document text)
        sentences (flatten (map :sentences (:paragraphs document)))
        scores (map :score sentences)
        threshold (last (take limit (reverse (sort scores))))]
    (->> sentences
        (filter #(>= (:score %) threshold))
        (map #(println (:text %)))
        (doall))))

(defn -main
  "Run based on user spec"
  [& args]
  (log/set-level! :error)
  (let [{:keys [action options exit-message ok?]} (validate-args args)]
    (if exit-message
      (exit (if ok? 0 1) exit-message)
      (case action
        (summarize (:file options) (:limit options))))))
