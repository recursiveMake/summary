(ns summary.cli
  (:require [clojure.string :as str]
            [clojure.tools.cli :refer [parse-opts]]))

(def cli-options
  "Supported program modes"
  [
   ["-f" "--file FILE" "File or text that can be `slurp`ed"]
   ["-s" "--site SITE" "Site or text that can be `reader`ed"]
   ["-l" "--limit LIMIT" "Number of lines of text"
    :default 5
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 50) "Must be between 0 and 50"]]
   ["-h" "--help"]])

(defn usage
  [options-summary]
  (->> ["Summarizer program"
        ""
        "Usage: java -jar summary.jar [OPTIONS]"
        ""
        "Options:"
        options-summary]
       (str/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (str/join \newline errors)))

(defn validate-args
  "Validate command line arguments. Either return a map indicating the program
  should exit (with a error message, and optional ok status), or a map
  indicating the action the program should take and the options provided."
  [args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
      (:help options) ; help => exit OK with usage summary
      {:exit-message (usage summary) :ok? true}
      errors ; errors => exit with description of errors
      {:exit-message (error-msg errors)}
      ;; custom validation on arguments
      (or (:file options) (:site options))
      {:target (first arguments) :options options}
      :else ; failed custom validation => exit with usage summary
      {:exit-message (usage summary)})))
