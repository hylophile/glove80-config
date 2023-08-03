#!/usr/bin/env bb

(require '[babashka.process :refer [shell]])
(require '[clojure.string :as str])

(defn wait [n]
  (dotimes [n n]
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

(defn flash []
  (println "\n🔵 Starting flash.")
  (when (not (both-mounted?))
    (println "🔵 Mount both halves now.")
    (wait 3)
    (doseq [n (range 1 11)
            :while (not (both-mounted?))]
      (println (format "Checking whether both halves are mounted. Attempt: %s/10" n))
      (Thread/sleep 2000)))
  (if (both-mounted?)
    (do
      (println "Found both halves.")
      (println "Copying to left half...")
      (shell (format "cp glove80.uf2 %s" lh-mountpoint))
      (println "Copying to right half...")
      (shell (format "cp glove80.uf2 %s" rh-mountpoint))
      (println "✅ Flashing successful."))
    (println "❌ Couldn't find mountpoints.")))

(build)
(flash)

