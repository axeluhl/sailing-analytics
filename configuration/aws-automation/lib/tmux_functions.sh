#!/usr/bin/env bash

sailing_0='/home/sailing/servers/server/logs/sailing0.log.0'
sailing_err='/var/log/sailing.err'
sailing_out='/var/log/sailing.out'

function check_if_tmux_is_used(){
	check_dependencies
	check_environment
}

function check_dependencies() {
if ! type tmux >/dev/null 2>/dev/null; then
  echo "The package \"tmux\" is required to run the script with this option"
  safeExit
fi
}

function check_environment() {
if ! { [ "$TERM" = "screen" ] && [ -n "$TMUX" ]; } then
  echo "To tail instance log files, please run this script inside a tmux session. To do so enter \"tmux\" into the console and start the script from there."
  safeExit
fi 
}

# $1: public_dns_name $2: ssh_user 
function tail_instance_logfiles(){
	configureUI
	if [ ! -z "$2" ]; then
		open_connections "$1" "$2"
	else
		open_connections "$1" "$ssh_user"
	fi
	tail_logfiles
}

function configureUI() {
# enable scrolling, clicking on panes etc.
tmux set -g mouse on

# enable named border at the top of panes
tmux set -g pane-border-status top
tmux set -g pane-border-format " [#{pane_index}] - #T "  

# construct pane layout

close_all_panes
sleep 1
tmux split-window -h -p 50 
tmux select-pane -t 0
tmux split-window -v -p 50 
tmux select-pane -t 2 
tmux split-window -v -p 50 
tmux select-pane -t 0

}

function open_connections() {
	wait_for_ssh_connection "$key_file" "$2" "$1"
	tmux send-keys -t 1 "ssh -o StrictHostKeyChecking=no -i $key_file $2@$1" C-m
	tmux send-keys -t 2 "ssh -o StrictHostKeyChecking=no -i $key_file $2@$1" C-m
	tmux send-keys -t 3 "ssh -o StrictHostKeyChecking=no -i $key_file $2@$1" C-m
}

function tail_logfiles(){
	tmux send-keys -t 1 "clear;echo \"Waiting for file $sailing_0 to appear...\";touch $sailing_0;cat $sailing_0;tail -F -v $sailing_0" C-m
	tmux send-keys -t 2 "clear;cat $sailing_out;tail -F -v $sailing_out" C-m
	tmux send-keys -t 3 "clear;cat $sailing_err;tail -F -v $sailing_err" C-m
}

function close_all_panes(){
	if [ "$(get_number_of_panes)" -gt 1 ]; then
		tmux kill-pane -a -t 0
	fi
}

function get_number_of_panes(){
	tmux display-message -p '#{window_panes}' 
}