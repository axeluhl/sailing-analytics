#!/bin/sh

# Executes the following script on the DB server:
#!/bin/sh
#useremail=$1
#for i in `echo "show dbs" | mongo --port 10202 | tail -n +3 | grep -v "^bye$" | awk '{print $1;}'`; do
#  match=`echo "db.USERS.find({EMAIL: '$useremail'}); db.COMPETITORS.find({email: '$useremail'})" | /opt/mongodb-linux-x86_64-2.6.7/bin/mongo --quiet --port 10202 $i`
#  if [ "$match" != "" ]; then
#    echo ${i}: $match
#  fi
#done
#for i in `echo "rs.slaveOk()
#show dbs" | mongo --port 10203 | tail -n +5 | grep -v "^bye$" | awk '{print $1;}'`; do
#  match=`echo "db.USERS.find({EMAIL: '$useremail'}); db.COMPETITORS.find({email: '$useremail'})" | /opt/mongodb-linux-x86_64-2.6.7/bin/mongo --quiet --port 10202 $i`
#  if [ "$match" != "" ]; then
#    echo ${i}: $match
#  fi
#done

ssh -A root@sapsailing.com ssh -A dbserver.internal.sapsailing.com finduserbyemail $1
