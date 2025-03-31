#!/bin/bash

command -v tmux >/dev/null 2>&1 || { echo "I require tmux but it's not installed. Aborting." >&2; exit 1; }

sn=sailing

SERVERS_DIR=/home/sailing/servers

TMUX_ACTIVE=`tmux has-session -t $sn 2>/dev/null`
if [ $? -eq 0 ]; then
        echo "Session exists...not configuring a new one"
else
        echo "Session does not exist...creating a new one with name $sn"
        cd /home/sailing/code
        tmux new-session -s "$sn" -n "BUILD" -d

        counter=0

        cd $SERVERS_DIR
        tmux new-window -t "$sn:$[counter+1]" -n "GOAccess" "bash -c 'goaccess -f /var/log/httpd/access_log'; bash"

        cd $SERVERS_DIR
        tmux new-window -t "$sn:$[counter+2]" -n "ATop" "bash -c 'apachetop -f /var/log/httpd/access_log'; bash"

        cd $SERVERS_DIR
        tmux new-window -t "$sn:$[counter+3]" -n "Htop" "bash -c 'htop'; bash"

        cd $SERVERS_DIR/server/logs
        tmux new-window -t "$sn:$[counter+4]" -n "Logs" "bash -c 'ls -lah'; bash"

        cd $SERVERS_DIR
        tmux new-window -t "$sn:$[counter+5]" -n "LeaderboardCalc" "bash -c 'tail -f server/logs/sailing0.log.0 | grep took'; bash"

        tmux select-window -t "$sn:0"
fi

if [[ "$1" != "unattended" ]]; then
        tmux -2 attach-session -t "$sn"
fi