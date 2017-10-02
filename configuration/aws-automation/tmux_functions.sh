#!/usr/bin/env bash

function checkDependencies() {
if ! type tmux >/dev/null 2>/dev/null; then
  echo "The package \"tmux\" is required to run this script"
  safeExit
fi
}

function checkEnvironment() {
if ! { [ "$TERM" = "screen" ] && [ -n "$TMUX" ]; } then
  echo "Please run this script inside a tmux session. To do so enter \"tmux\" into the console and start the script from there."
  safeExit
fi 
}

function configureUI() {
# enable scrolling, clicking on panes etc.
tmux set -g mouse on

# enable named border at the top of panes
tmux set -g pane-border-status top
tmux set -g pane-border-format " [#{pane_index}] - #T "  

# construct pane layout
tmux split-window -h -p 50 
tmux select-pane -t 0
tmux split-window -v -p 50 
tmux select-pane -t 2 
tmux split-window -v -p 50 
tmux select-pane -t 0
}

function tmux_open_connections() {
	tmux send-keys -t 1 "ssh -o StrictHostKeyChecking=no -i $key_file $ssh_user@$public_dns_name" C-m
	tmux send-keys -t 2 "ssh -o StrictHostKeyChecking=no -i $key_file $ssh_user@$public_dns_name" C-m
	tmux send-keys -t 3 "ssh -o StrictHostKeyChecking=no -i $key_file $ssh_user@$public_dns_name" C-m
}

function tmux_tail_logfiles(){
	tmux send-keys -t 1 "clear;echo \"Waiting for file /home/sailing/servers/server/logs/sailing0.log.0 to appear...\";tail -F -v /home/sailing/servers/server/logs/sailing0.log.0" C-m
	tmux send-keys -t 2 "clear;tail -f -v /var/log/sailing.out" C-m
	tmux send-keys -t 3 "clear;tail -f -v /var/log/sailing.err" C-m
}