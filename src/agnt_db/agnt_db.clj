; new agnt-db - experimental
(ns agnt-db.agnt-db
  (:import (java.io File FileWriter FileReader PushbackReader)))

(set! *warn-on-reflection* true)

(declare lcl-save)
(declare lcl-load)

(defn- _opn
  "Agnt function - if the keyed file is not in the mp, open it and agntize with ky."
  [mp ky ^String fpath]
   (if (nil? (mp ky))
     (let [file (java.io.File. fpath)
           rec (lcl-load file)]
       (assoc mp ky {:file file :rec rec}))
     (throw (IllegalArgumentException. (str "Agnt key already open for: " ky ", fpath: " fpath))))) 

(defn- _close
  [^clojure.lang.Associative mp ky]
  (dissoc mp ky))

(defn- _upd-rec
  [mp ky ks-vec new-val was-val]
  (let [rec (get-in mp [ky :rec])
        itm (get-in rec ks-vec)
        ;_ (println :KS-VEC ks-vec :ITM itm :WAS-VAL was-val)
        ]
    (if (and (when-not (nil? was-val) (not= (first was-val) itm)))
      mp
      (let [new-rec (assoc-in rec ks-vec new-val)]
        (lcl-save (get-in mp [ky :file]) new-rec)
        (assoc-in mp [ky :rec] new-rec )))))

(defn- _snd-off
  [agnt f & args]
  (do (await (apply send-off agnt f args))))

(let [agnt (agent {} :error-handler (fn [agnt err] (println :AGNT-ERROR err)))]
  (defn agnt-open
    "Open an agnt with key ky and use fpath as a file for persisting ky's value which is a hash-map."
    [ky fpath]
    (_snd-off agnt _opn ky fpath)
    )
  (defn agnt-upd
    "Update the agnt key's hash-map (for ky) where ks-vec is the tree path into the map (like update-in), new-val
    is the new value for the element at that node in the map. If was-val is specified, then the update
    will only happen if the before-update node's value is equal to was-val. Otherwise the update is not applied.
    Use agnt-iswas to check whether the update was successful."
    [ky ks-vec new-val & was-val]
    (_snd-off agnt _upd-rec ky ks-vec new-val was-val))
  (defn agnt-close
    "Close an existing agnt key (for ky)."
    [ky]
    (_snd-off agnt _close ky))
  (defn agnt-get
    "Get the value from the agnt key's hash-map (for ky) where ks-vec is the tree path into the map (like get-in).
    Use [] as the ks-vec to get the whole map for ky."
    [ky ks-vec] ; use [] to get the whole :rec
    (get-in @agnt (into [ky :rec] ks-vec)))
  (defn agnt-iswas
    "Check that the value for the agnt key's hash-map (for ky) where ks-vec is the tree path into the map (like get-in)
    is the same as was-val. If so, return was-val otherwise nil."
    [ky ks-vec was-val]
    (let [now-is (get-in @agnt (into [ky :rec] ks-vec))]
      (if (= now-is was-val) was-val nil)))
  (defn agnt-get-file
    "Get the File object being used to persist the agnt key's hash-map (for ky)."
    [ky]
    (get-in @agnt [ky :file]))
  (defn get-agnt
    "Get the agent object."
    [] agnt)
  (defn agnt-errors
    "More work needs to be done on errors occuring in the agnt's agent. Error handling changed in clj 1.2. We need to deal with
    file io errors, etc. Presently the agnt's error handler just prints the error message and the agent continues."
    []
    (agent-errors agnt))
  )

(defn lcl-save
 "Save a clojure form to file. Returns the saved form."
  [^File file frm]
  (with-open [w (FileWriter. file)] (binding [*out* w *print-dup* true] (prn frm))) frm )

(defn lcl-load
  "Load a clojure form from file."
  [^File file]
  (if (not (.exists file))
    (lcl-save file {})
    (with-open [r (PushbackReader. (FileReader. file))]
      (let [rec (read r false {})] rec))))

