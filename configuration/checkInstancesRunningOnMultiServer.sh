#!/bin/sh
BASEDIR=/home/sailing/servers

serverlist=`mktemp`
runningservers=`mktemp`

cd $BASEDIR
for i in */env.sh; do
  cat $i | grep SERVER_NAME | tail -1
done | sed -e 's/^SERVER_NAME=//' | sort >$serverlist
ps axlw | grep java | grep -v grep | awk '{ print $14; }' | sed -e 's/^-D//' | sort >$runningservers
diff $serverlist $runningservers
