(ns upftp.file
  (:require [clojure.contrib.string :as string])
  (:import [java.io File]
           [java.util.regex Pattern]))


(def *ignore* (atom [".svn" ".git" ".gitignore"]))

(defn include? [v coll]
  (not (empty? (drop-while #(not= v %) coll))))

(defn ignore-set [coll]
  (reset! *ignore* coll))

(defn ignore? [f]
  (include? (.getName f) @*ignore*))


(defn replace-aster [s]
  (string/replace-str "*" ".+" s))

(defn escape-re [s]
  (let [escs ["." "+" "?" "-" "[" "]" "^" "$"]]
    (reduce
     (fn [s e] (string/replace-str e (str \\ e) s))
     s
     escs)))

(defn match [pattern s]
  (let [result (re-find
                (Pattern/compile (str "^" (replace-aster (escape-re pattern)) "$") Pattern/CASE_INSENSITIVE)
                s)]
   (not (nil? result))))

(defn match-file [pattern f]
  (let [fname (.getName f)]
    (match pattern fname)))

(defn match-files-in-dir [pattern dir]
  (let [fs (.listFiles dir)]
    (mapcat
     (fn [f]
       (when-not (ignore? f)
         (if (.isDirectory f)
           (match-files-in-dir pattern f)
           (when (match-file pattern f) [f]))))
     fs)))

(defn enum-files [path]
  (let [f (File. path)]
    (if (.isDirectory f)
      (map #(.getAbsolutePath %) (match-files-in-dir ".*" f))
      (if (.exists f)
        [(.getAbsolutePath f)]
        (when (re-find #"\*" (.getName f))
          (map #(.getAbsolutePath %) (match-files-in-dir (.getName f) (.getParentFile f))))))))

(defn file? [path]
  (let [f (File. path)]
    (and (.isFile f) (not (re-find #"\*" (.getName f))))))

(defn file-name [path]
  (.getName (File. path)))

(defn dir [path]
  (let [f (File. path)]
    (if (.isDirectory f)
      (.getAbsolutePath f)
      (.getAbsolutePath (.getParentFile (File. path))))))

