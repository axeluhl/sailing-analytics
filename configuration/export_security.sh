#!/bin/bash
if [ "$1" = "" ]; then
  echo "Usage: $0 {variant} {dbname} {port}"
  echo "Example: $0 archive winddb 10201"
else
  variant=$1
  dbname=$2
  port=$3
  COLLECTIONS="USERS USER_GROUPS OWNERSHIPS ACCESS_CONTROL_LISTS PREFERENCES ROLES SESSIONS"
  for c in $COLLECTIONS; do
    mongoexport --port $port -d $dbname -c $c >${c}_${variant}.json
  done
fi
