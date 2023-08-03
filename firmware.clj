#!/usr/bin/env bb

(require '[babashka.process :refer [shell]])
(require '[clojure.string :as str])

(defn wait-until [check n]
  (doseq [_n (range n)
          :while (not (check))]
    (print ".")
    (flush)
    (Thread/sleep 1000))
  (println))

(def lh-mountpoint (format "/run/media/%s/GLV80LHBOOT" (System/getenv "USER")))
(def rh-mountpoint (format "/run/media/%s/GLV80RHBOOT" (System/getenv "USER")))

(defn mounted? [path]
  (-> (shell {:continue :true :out :string} "lsblk")
      :out
      (str/includes? path)))

(defn both-mounted? []
  (and (mounted? lh-mountpoint)
       (mounted? rh-mountpoint)))

(defn build []
  (println "🔵 Starting build.")
  (shell "nix-build config")
  (shell "cp result/glove80.uf2 .")
  (println "✅ Build successful."))

(defn flash [side path]
  (println (format "\n🔵 Flashing the %s half." side))
  (when (not (mounted? path))
    (print (format "🔵 Waiting until the %s half is mounted." side))
    (wait-until (fn [] (mounted? path)) 30))
  (if (mounted? path)
    (do
      (println (format "Found the %s half." side))
      (println (format "Copying to %s half..." side))
      (shell (format "cp glove80.uf2 %s" path))
      (println (format "✅ Flashing of %s half successful." side))
      true)
    (do (println "❌ Couldn't find mountpoint.")
        false)))

(build)
(and (flash "left" lh-mountpoint)
     (flash "right" rh-mountpoint)
     (println "\n✅ Flashing successful."))


