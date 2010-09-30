
agnt-db - A simple Clojure agent-based store for hash-map structures
======================================================================

A work in progress.

The original goals of this project were to provide a simple mechanism for sharing clojure hash-map
structures between threads with coordinated updates and also file-based persistence of the maps.

The particular application which inspired the approach was a web app where users could
register online bookings for certain `resources`. A user could see which resources were
available and which were already booked, then choose a free resource and attempt to 
book it. If the resource was still available when the server received the request, great - the
booking was made and confirmed in the response. If however in the mean time, someone else had
booked that resource, the server had to say 'sorry, taken already - try again'.

In practice, the requests were http requests, invoking a clojure handler which would perform
the above processing and return the response. Each handler invocation would be on the container's
particular request thread and would also perform side-affects such as persisting the state of 
the bookings model to disk.

This project is aimed at evolving a "clojure-spirited" way of dealing with this problem.

The first pass is a clojure agent based approach, but we'll create a general api and may
provide other (than agent) implementations in future. 

Here's a simple example of the basic usage of agnt-db for the scenario above;


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


Further Notes (see source for details)
--------------------------------------

By having a single agent handle all the requests and using send-off/await, we are (hopefully) certain of
each update being handled serially and appropriate blocking ensuring the disk persistence side effects are
correctly coordinated.

The use of agnt-upd and agnt-iswas *should* allow the esired semantics of the requirement to be met.

Issues
------

Discuss issues on agents, error handling, etc.

Still To Do
-----------

- Proper test framework to verify the model under stress load.
- Additional api functions (query, join, etc)
- More sophisticated disk-persistence model (use of caching, indexing, etc
 
