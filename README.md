
`agnt-db` - A simple Clojure agent-based store for hash-map structures
======================================================================

The original goals of this project were to provide a simple mechanism for sharing clojure hash-map
structures between threads with coordinated updates and also file based persistence of the maps.

The particular application which inspired the approach was a web app where users could
register online for certain `resources`. More to follow...


Example usage
-------------

    ./repl.sh
    Clojure 1.2.0
        
    (use 'agnt-db.agnt-db :reload)
    nil

    (agnt-open :udb "./test/users.adb")
    nil

    (agnt-upd :udb [:joe] {:uid :joe :fname "Joe" :lname "Soap"})
    nil

    (agnt-upd :udb [:fred] {:uid :fred :fname "Fred" :lname "Blogs"})
    nil

    (agnt-get :udb [])
    {:fred {:uid :fred, :fname "Fred", :lname "Blogs"}, :joe {:uid :joe, :fname "Joe", :lname "Soap"}}

    (agnt-get :udb [:fred])
    {:uid :fred, :fname "Fred", :lname "Blogs"}

    (agnt-get :udb [:fred :fname])
    "Fred"

    (agnt-open :rooms "./test/rooms.adb")
    nil
    (agnt-upd :rooms [:room1 "Jul 01"] {:am :free :pm :free})
    nil
    (agnt-upd :rooms [:room1 "Jul 02"] {:am :free :pm :free})
    nil

    ; Book room1 for Joe whether its free or not
    (agnt-upd :rooms [:room1 "Jul 01" :am] :joe)
    nil

    (agnt-get :rooms [])
    {:room1 {"Jul 02" {:am :free, :pm :free}, "Jul 01" {:am :joe, :pm :free}}}

    (agnt-get :rooms [:room1 "Jul 01"])
    {:am :joe, :pm :free}

    ; Now Fred gets room1 status
    (agnt-get :rooms [:room1 "Jul 01"])
    {:am :joe, :pm :free}

    ; Fred books room1 pm provided it is still free
    (agnt-upd :rooms [:room1 "Jul 01" :pm] :fred :free)
    nil

    ; Fred confirms that he got the booking
    (agnt-iswas :rooms [:room1 "Jul 01" :pm] :fred)
    :fred

    ; Joe thinking pm was free books that as well
    (agnt-upd :rooms [:room1 "Jul 01" :pm] :joe :free)
    nil

    ; Joe checks if he got it - sorry, pm was already taken by Fred
    (agnt-iswas :rooms [:room1 "Jul 01" :pm] :joe)
    nil

    (agnt-get :rooms [])
    {:room1 {"Jul 02" {:am :free, :pm :free}, "Jul 01" {:am :joe, :pm :fred}}}


Further Notes
-------------

Issues
------

Still To Do
-----------
 
