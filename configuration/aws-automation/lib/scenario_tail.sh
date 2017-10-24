#!/usr/bin/env bash

function tail_start(){
	tail_precondition
	tail_user_input
	tail_execute
}

function tail_precondition(){
	check_if_tmux_is_used
}

# -----------------------------------------------------------
# All these variables are needed for this scenario
# If one variable is not assigned or passed by parameter
# the user will be prompted to enter a value
# -----------------------------------------------------------
function tail_user_input(){
	require_key_file
	require_ssh_user
	require_public_dns_name
}

function tail_execute() {
	echo "Open tmux panes and start tailing log files..."
	construct_ui
	open_connections "$key_file" "$ssh_user" "$public_dns_name"
	tail_logfiles
}

function tail_logfiles(){
	local sailing_0='/home/sailing/servers/server/logs/sailing0.log.0'
	local sailing_err='/var/log/sailing.err'
	local sailing_out='/var/log/sailing.out'

	tmux send-keys -t 1 "clear;echo \"Waiting for file $sailing_0 to appear...\";touch $sailing_0;cat $sailing_0;tail -F -v $sailing_0" C-m
	tmux send-keys -t 2 "clear;cat $sailing_out;tail -F -v $sailing_out" C-m
	tmux send-keys -t 3 "clear;cat $sailing_err;tail -F -v $sailing_err" C-m
}
