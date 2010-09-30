#!/bin/bash

APP=./test:./src
LIBS=/jx/cl/clojure.jar

CP=$APP:$EXTRA:$LIBS

#OPTS='-Xms32M -Xmx128M -server'
OPTS='-server'

#echo $CP
exec rlwrap java $OPTS -cp $CP clojure.main $1 $2 $3 $4 $5 $6 $7 $8 $9

