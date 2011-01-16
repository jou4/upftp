(ns upftp
  (:gen-class)
  (:require [upftp.ftp :as ftp]
            [upftp.gui :as gui]
            [upftp.file :as file]
            [upftp.task :as task]
            [upftp.shell :as shell])
  (:use [clojure.contrib.command-line :only (with-command-line)]))

(def client (ftp/client))
(def *tasks* (ref nil))


(defmacro connect
  ([server-name user pass]
     `(connect ~server-name ~user ~pass true))
  ([server-name user pass passive]
     `(try
       (ftp/disconnect client)
       (when-not (ftp/connect client ~server-name)
         (throw (Exception. "unconnected.")))
       (when-not (ftp/login client ~user ~pass)
         (throw (Exception. "login failed.")))
       (when ~passive
         (ftp/local-passive-mode client))
       (println (str "Connected to \"" ~server-name "\"."))
       (catch Exception e#
         (.printStackTrace e#)))))

(defmacro tasks [& ts]
  `(dosync (ref-set *tasks* (apply concat [~@ts]))))

(defmacro cd [path] `(task/task-cd ~path))
(defmacro mkdir [path] `(task/task-mkdir ~path))
(defmacro rmdir [path] `(task/task-rmdir ~path))
(defmacro rm [path] `(task/task-rm ~path))
(defmacro rename [from to] `(task/task-rename ~from ~to))
(defmacro upload [remote local] `(task/task-upload ~remote ~local))
(defmacro download [remote local] `(task/task-download ~remote ~local))

(defmacro ignore [& names] `(file/ignore-set '~names))


(defn reload [file]
  (load-file file))

(defn execute []
  (let [result (atom true)]
    (try
     (doseq [t @*tasks*]
       (println (str "[" (:command (meta t)) "]" (:doc (meta t))))
       (t client))
     (catch Exception e
       (.printStackTrace e)
       (reset! result false))
     (finally
      (ftp/disconnect client)))
    @result))

(defn -main [& args]
  (with-command-line args "[-i] filename"
                     [[interactive? i? "interactive" false]
                      others]
                     (if interactive?
                       (shell/run)
                       (if (< 0 (count others))
                         (do
                           (reload (first others))
                           (when (execute) (println "Fin.")))
                         (do
                           (gui/select-file
                            #(do
                               (reload %)
                               (when (execute) (gui/show-message "完了しました。")))))))))


(defn -sample []
  (let [context (atom nil)
        open-action #(do
                       (reload %)
                       ((:set-enabled @context) true))
        execute-action #(when (execute) (gui/show-message "完了しました。"))]
    (reset! context
            (gui/main {:open open-action
                       :execute execute-action}))))
