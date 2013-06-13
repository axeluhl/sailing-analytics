#!/bin/bash

command -v tmux >/dev/null 2>&1 || { echo "I require tmux but it's not installed. Aborting." >&2; exit 1; }

sn=sailing

SERVERS_DIR=/home/trac/servers

TMUX_ACTIVE=`tmux has-session -t $sn 2>/dev/null`
if [ $? -eq 0 ]; then
	echo "Session exists...not configuring a new one"
else
	echo "Session does not exist...creating a new one with name $sn"
	cd /home/trac/git
	tmux new-session -s "$sn" -n "BUILD" -d

	counter=1
	for dir in dev test prod1 prod2; do
	  cd $SERVERS_DIR/$dir
	  tmux new-window -t "$sn:$counter" -n `basename $dir` "bash -c './start'; bash"
	  counter=$[counter + 1]
	done

	cd /home/trac/servers/prod1
	tmux new-window -t "$sn:$counter" -n "UDP" "bash -c './udpmirror -v 2012 localhost 2010 localhost 2011 localhost 2013 localhost 2014'; bash"

	cd /opt/mongodb/bin
	tmux new-window -t "$sn:$[counter+1]" -n "GOAccess" "bash -c 'goaccess -f /var/log/httpd/access_log'; bash"

	cd /opt/
	tmux new-window -t "$sn:$[counter+2]" -n "ATop" "bash -c 'apachetop -f /var/log/httpd/access_log'; bash"

	cd /home/trac/servers
	tmux new-window -t "$sn:$[counter+3]" -n "Logs" "bash -c 'ls -lah'; bash"

	cd /home/trac/servers/prod1
	tmux new-window -t "$sn:$[counter+4]" -n "STListener" "bash -c './swisstiminglistener 3500 3501'; bash"

	tmux select-window -t "$sn:0"
fi

if [[ "$1" != "unattended" ]]; then
	tmux -2 attach-session -t "$sn"
fi
