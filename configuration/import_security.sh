#!/bin/bash
if [ "$1" = "" ]; then
  echo "Usage: $0 {variant} {dbname} {port}"
  echo "Example: $0 archive winddb 10201"
else
  variant=$1
  dbname=$2
  port=$3
  export PATH="${PATH}:/cygdrive/c/Program Files/MongoDB/Server/3.6/bin"
  COLLECTIONS="USERS USER_GROUPS OWNERSHIPS ACCESS_CONTROL_LISTS PREFERENCES ROLES SESSIONS"
  for c in $COLLECTIONS; do
    mongoimport --drop --port $3 -d $dbname -c $c ${c}_${variant}.json
  done
fi
