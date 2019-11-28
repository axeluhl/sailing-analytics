#!/bin/bash
variant=$1
dbname=$2
export PATH="${PATH}:/cygdrive/c/Program Files/MongoDB/Server/3.6/bin"
COLLECTIONS="USERS USER_GROUPS OWNERSHIPS ACCESS_CONTROL_LISTS PREFERENCES"
for c in $COLLECTIONS; do
  mongoexport -d $dbname -c $c >${c}_${variant}.json
done
