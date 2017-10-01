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