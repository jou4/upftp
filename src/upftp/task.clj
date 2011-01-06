(ns upftp.task
  (:require [upftp.file :as file]
            [upftp.ftp :as ftp]
            [clojure.contrib.string :as string]))

(defn task-cd [path]
  [(with-meta
   (fn [client]
     (ftp/change-directory client path))
   {:command "cd"
    :doc (str "Change working directory to \"" path "\".")})])

(defn task-mkdir [path]
  [(with-meta
   (fn [client]
     (ftp/make-directory client path))
   {:command "mkdir"
    :doc (str "Make directory named \"" path "\".")})])

(defn task-rmdir [path]
  [(with-meta
   (fn [client]
     (ftp/remove-directory client path))
   {:command "rmdir"
    :doc (str "Remove directory named \"" path "\".")})])

(defn taks-rm [path]
  [(with-meta
   (fn [client]
     (ftp/remove-file client path))
   {:command "rm"
    :doc (str "Remove file named \"" path "\".")})])

(defn task-rename [from to]
  [(with-meta
   (fn [client]
     (ftp/rename-file client from to))
   {:command "rename"
    :doc (str "Rename file named \"" from "\" to \"" to "\".")})])

(defn task-upload [remote local]
  (let [func-gen (fn [r l]
                   (with-meta
                    (fn [client]
                      (ftp/upload client r l))
                    {:command "upload"
                     :doc (str "Upload \"" l "\" to \"" r "\".")}))]
    (if (file/file? local)
      [(func-gen remote local)]
      (let [files (file/enum-files local)
            base-dir (file/dir local)
            relative-path (fn [path] (string/replace-re #"\\" "/" (string/replace-str base-dir "" path)))
            concat-path (fn [a b]
                          (str (string/replace-re #"[/\\]$" "" a)
                               (when-not (empty? a) "/")
                               (string/replace-re #"^[/\\]" "" b)))]
        (map #(func-gen (concat-path remote (relative-path %)) %) files)))))

(defn task-download [remote local]
  [(with-meta
   (fn [client]
     (ftp/download client remote local))
   {:command "download"
    :doc (str "Download \"" remote "\" to \"" local "\".")})])
