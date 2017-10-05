#!/usr/bin/env bash

function construct_ui() {
	# enable scrolling, clicking on panes etc.
	tmux set -g mouse on

	# enable named border at the top of panes
	tmux set -g pane-border-status top
	tmux set -g pane-border-format " [#{pane_index}] - #T "  

	# close all panes except one
	reset_panes
	sleep 1
	
	# construct pane layout
	tmux split-window -h -p 50 
	tmux select-pane -t 0
	tmux split-window -v -p 50 
	tmux select-pane -t 2 
	tmux split-window -v -p 50 
	tmux select-pane -t 0
}

# -----------------------------------------------------------
# Opens ssh connections on all four panes
# @param $1  key file
# @param $2  ssh user
# @param $3  dns name of instance
# -----------------------------------------------------------
function open_connections() {
	tmux send-keys -t 1 "ssh -o StrictHostKeyChecking=no -i $1 $2@$3" C-m
	tmux send-keys -t 2 "ssh -o StrictHostKeyChecking=no -i $1 $2@$3" C-m
	tmux send-keys -t 3 "ssh -o StrictHostKeyChecking=no -i $1 $2@$3" C-m
}

function reset_panes(){
	if more_panes_are_open; then
		tmux kill-pane -a -t 0
	fi
}

function get_number_of_panes(){
	tmux display-message -p '#{window_panes}' 
}

function more_panes_are_open(){
	if inside_tmux_session; then
		if [ "$(get_number_of_panes)" -gt 1 ]; then
			return 0;
		fi
	fi
	return 1;
}

function check_if_tmux_is_used(){
	check_dependencies
	check_environment
}

function check_dependencies() {
	if ! is_tmux_available; then
		echo "The package \"tmux\" is required to run the script with this option"
		safeExit
	fi
}

function check_environment() {
	if ! inside_tmux_session; then
		echo "To tail instance log files, please run this script inside a tmux session. To do so enter \"tmux\" into the console and start the script from there."
		safeExit
	fi 
}

function is_tmux_available(){
	type tmux >/dev/null 2>/dev/null
}

function inside_tmux_session(){
	[ "$TERM" = "screen" ] && [ -n "$TMUX" ]
}

# -----------------------------------------------------------
# Prompts user if open panes should be closed 
# -----------------------------------------------------------
function confirm_reset_panes(){ 
  if more_panes_are_open; then
	seek_confirmation "Do you want to close all open panes?"
	if is_confirmed; then
		reset_panes
	else
		safeExit
	fi
  fi
}