#!/bin/sh

# Executes the following script on the DB server:
##!/bin/sh
#useremail=$1
#for port in 10201 10202 10203; do
#  for db in `echo "rs.slaveOk()
#  show dbs
#  quit()" | mongo --port $port | tail -n +3 | grep -v "^bye$" | awk '{print $1;}'`; do
#    match=`echo "rs.slaveOk(); db.USERS.find({EMAIL: '$useremail'}); db.COMPETITORS.find({email: '$useremail'})" | mongo --quiet --port $port $i`
#    if [ "$match" != "" ]; then
#      echo ${i}: $match
#    fi
#  done
#done

ssh -A root@sapsailing.com ssh -A dbserver.internal.sapsailing.com finduserbyemail $1
