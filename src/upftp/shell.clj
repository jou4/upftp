(ns upftp.shell
  (:require [clojure.contrib.string :as string])
  (:require [upftp.ftp :as ftp]
            [upftp.task :as task]))

(defn user-input []
  (do
    (print "upftp> ")
    (flush)
    (read-line)))

(defn user-input-safely []
  (do
    (print "upftp> ")
    (flush)
    (apply str (.readPassword (System/console)))))

(defn execute [client input]
  (let [parts (string/split #"\s+" input)
        command (fn [c] (= c (first parts)))
        tasks (cond (command "cd") (task/task-cd (nth parts 1))
                    (command "ls") (if (< 1 (count parts))
                                     (task/task-ls (nth parts 1))
                                     (task/task-ls))
                    (command "rm") (task/task-rm (nth parts 1))
                    (command "rmdir") (task/task-rmdir (nth parts 1))
                    (command "mkdir") (task/task-mkdir (nth parts 1))
                    (command "mv") (task/task-rename (nth parts 1) (nth parts 2))
                    (command "upload") (task/task-upload (nth parts 1) (nth parts 2))
                    (command "download") (task/task-download (nth parts 1) (nth parts 2)))]
    (try
     (doseq [t tasks]
;       (println (str "[" (:command (meta t)) "]" (:doc (meta t))))
       (t client))
     (catch Exception e
       (.printStackTrace e)))))

(defn run []
  (let [client (ftp/client)]
    (try
     (println "Welcome to upftp shell.")
     
     (println "Input server name or server address.")
     (def server (user-input))

     (when-not (ftp/connect client server)
       (throw (Exception. "unconnected.")))
     
     (println "Input login user name.")
     (def user (user-input))
     (println "Input login password.")
     (def pass (user-input))

     (when-not (ftp/login client user pass)
       (throw (Exception. "login failed.")))
     (ftp/local-passive-mode client)
     (println (str "Connected to \"" server "\"."))

     (loop []
       (let [input (user-input)]
         (if (= "quit" input)
           (do
             (ftp/disconnect client)
             (println "Goodbye."))
           (do
             (execute client input)
             (recur)))))
     
     (catch Exception e
       (.printStackTrace e)))))
