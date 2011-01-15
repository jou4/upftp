(ns upftp.ftp
  (:import [org.apache.commons.net.ftp FTP FTPClient FTPReply]
           [java.io FileInputStream FileOutputStream])
  (:require [clojure.contrib.string :as string]))

(defn client []
  (FTPClient.))

(defn connect [client server-name]
  (.connect client server-name)
  (FTPReply/isPositiveCompletion (.getReplyCode client)))

(defn disconnect [client]
  (when (.isConnected client)
    (.disconnect client)))

(defn login [client user pass]
  (.login client user pass))

(defn list-names
  ([client] (.listNames client))
  ([client path] (.listNames client path)))

(defn list-files [client path]
  (.listFiles client path))

(defn remove-file [client path]
  (.deleteFile client path))

(defn rename-file [client from to]
  (.rename client from to))

(defn change-directory [client path]
  (.changeWorkingDirectory client path))

(defn make-directory [client path]
  (when-not (string/blank? path)
    (let [cd (.printWorkingDirectory client)]
      (when (= \/ (string/get path 0))
        (change-directory client "/"))
      (let [dirs (string/split #"\/" path)]
        (doseq [d dirs]
          (when-not (string/blank? d)
            (.makeDirectory client d)
            (change-directory client d))))
      (change-directory client cd))))

(defn remove-directory [client path]
  (.removeDirectory client path))

(defn upload [client remote local]
  (make-directory client (string/join \/ (butlast (string/split #"\/" remote))))
  (with-open [st (FileInputStream. local)]
    (.storeFile client remote st)))

(defn download [client remote local]
  (with-open [st (FileOutputStream. local)]
    (.retrieveFile client remote st)))

(defn local-passive-mode [client]
  (.enterLocalPassiveMode client))