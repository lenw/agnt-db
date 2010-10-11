(ns agnt-db.agnt-db-test
  (:use agnt-db.agnt-db)
  (:use clojure.test))

(deftest open
  (is (= (agnt-open :udb "./test/users.adb") nil)))

(deftest upd-read
  (is (= (agnt-upd :udb [:joe]   {:uid :joe :fname "Joe" :lname "Soap"}) nil))
  (is (= (agnt-get :udb [:joe]) {:uid :joe, :fname "Joe", :lname "Soap"})))

(run-tests)
