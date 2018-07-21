#!/usr/bin/env bash

# -----------------------------------------------------------
# Appends multiple parameters (e.g. "SERVER_NAME=Test")
# Also includes parameters starting with "#" as comments.
# @param $@  variable key and value (e.g. "MONGODB_HOST=123.123.123.123")
# -----------------------------------------------------------
function build_configuration(){
	for var in "$@"
	do
		# if parameters starts with #, add parameters to content
		if [[ $var == \#* ]]; then
			content+="$var\n"
			continue
		fi

		key=${var%=*}
		value=${var#*=}

		if [[ ! -z "$key"  && ! -z "$value" ]]; then
			content+="$key=$value\n"
	  fi
	done
	echo -e $content
}

function command_was_successful(){
	[ $1 -eq 0 ]
}

# -----------------------------------------------------------
# Uncomments a line starting with a specific pattern
# @param $1  pattern
# @param $2  file
# -----------------------------------------------------------
function uncomment_line_starting_with(){
	sed -i '/$1/s/^#//g' $2
}


# -----------------------------------------------------------
# Checks if variable is a number
# @param $1  returnvariablevalue
# @return 0 if variable is a number
# -----------------------------------------------------------
function is_number(){
	[[ $1 =~ ^-?[0-9]+$ ]]
}



# -----------------------------------------------------------
# Check if variable is a number and its value is 200
# @param $1  variable
# @return 0 if value is 200
# -----------------------------------------------------------
function is_http_ok(){
	is_number $1 && [ $1 == 200 ]
}

function get_response(){
	echo "$1" | head -n-1
}

function get_status_code(){
	echo "$1" | tail -n1
}

function get_attribute(){
	jq -r $1 | sanitize
}

function sanitize(){
	tr -d '\r'
}

function alphanumeric(){
	lower_trim $1 | only_letters_and_numbers
}

function only_letters_numbers_dash(){
	echo $1 | trim | tr -d -c '[:alnum:]-'
}


# ------------------------------------------------------
# The following functions were part of a bash template
# and could be useful in the future
# ------------------------------------------------------

# Traps
# ------------------------------------------------------
# These functions are for use with different trap scenarios
# ------------------------------------------------------

# Non destructive exit for when script exits naturally.
# Usage: Add this function at the end of every script
function safeExit() {
  # Delete temp files, if any
  if is_dir "${tmpDir}"; then
    rm -r "${tmpDir}"
  fi
	kill -WINCH $$
  trap - INT TERM EXIT
  exit
}

# readFile
# ------------------------------------------------------
# Function to read a line from a file.
#
# Most often used to read the config files saved in my etc directory.
# Outputs each line in a variable named $result
# ------------------------------------------------------
function readFile() {
  unset "${result}"
  while read result
  do
    echo "${result}"
  done < "$1"
}



# Escape a string
# ------------------------------------------------------
# usage: var=$(escape "String")
# ------------------------------------------------------
escape() { echo "${@}" | sed 's/[]\.|$(){}?+*^]/\\&/g'; }

# needSudo
# ------------------------------------------------------
# If a script needs sudo access, call this function which
# requests sudo access and then keeps it alive.
# ------------------------------------------------------
function needSudo() {
  # Update existing sudo time stamp if set, otherwise do nothing.
  sudo -v
  while true; do sudo -n true; sleep 60; kill -0 "$$" || exit; done 2>/dev/null &
}

# convertsecs
# ------------------------------------------------------
# Convert Seconds to human readable time
#
# To use this, pass a number (seconds) into the function as this:
# print "$(convertsecs $TOTALTIME)"
#
# To compute the time it takes a script to run use tag the start and end times with
#   STARTTIME=$(date +"%s")
#   ENDTIME=$(date +"%s")
#   TOTALTIME=$(($ENDTIME-$STARTTIME))
# ------------------------------------------------------
function convertsecs() {
  ((h=${1}/3600))
  ((m=(${1}%3600)/60))
  ((s=${1}%60))
  printf "%02d:%02d:%02d\n" $h $m $s
}

# Join
# ----------------------------------------------
# This function joins items together with a user specified separator
# Taken whole cloth from: http://stackoverflow.com/questions/1527049/bash-join-elements-of-an-array
#
# Usage:
#   join , a "b c" d #a,b c,d
#   join / var local tmp #var/local/tmp
#   join , "${FOO[@]}" #a,b,c
# ----------------------------------------------
function join() { local IFS="${1}"; shift; echo "${*}"; }

# File Checks
# ------------------------------------------------------
# A series of functions which make checks against the filesystem. For
# use in if/then statements.
#
# Usage:
#    if is_file "file"; then
#       ...
#    fi
# ------------------------------------------------------

function is_exists() {
  if [[ -e "$1" ]]; then
    return 0
  fi
  return 1
}

function is_not_exists() {
  if [[ ! -e "$1" ]]; then
    return 0
  fi
  return 1
}

function is_file() {
  if [[ -f "$1" ]]; then
    return 0
  fi
  return 1
}

function is_not_file() {
  if [[ ! -f "$1" ]]; then
    return 0
  fi
  return 1
}

function is_dir() {
  if [[ -d "$1" ]]; then
    return 0
  fi
  return 1
}

function is_not_dir() {
  if [[ ! -d "$1" ]]; then
    return 0
  fi
  return 1
}

function is_symlink() {
  if [[ -L "$1" ]]; then
    return 0
  fi
  return 1
}

function is_not_symlink() {
  if [[ ! -L "$1" ]]; then
    return 0
  fi
  return 1
}

function is_empty() {
  if [[ -z "$1" ]]; then
    return 0
  fi
  return 1
}

function is_not_empty() {
  if [[ -n "$1" ]]; then
    return 0
  fi
  return 1
}


# SEEKING CONFIRMATION
# ------------------------------------------------------
# Asks questions of a user and then does something with the answer.
# y/n are the only possible answers.
#
# USAGE:
# seek_confirmation "Ask a question"
# if is_confirmed; then
#   some action
# else
#   some other action
# fi
#
# Credt: https://github.com/kevva/dotfiles
# ------------------------------------------------------

# Ask the question
function seek_confirmation() {
  # echo ""
  input "$@"
  read -p " (y/n) " -n 1
  echo ""
}

# Test whether the result of an 'ask' is a confirmation
function is_confirmed() {
  if "${force}"; then
    return 0
  else
    if [[ "${REPLY}" =~ ^[Yy]$ ]]; then
      return 0
    fi
    return 1
  fi
}

function is_not_confirmed() {
  if "${force}"; then
    return 1
  else
    if [[ "${REPLY}" =~ ^[Nn]$ ]]; then
      return 0
    fi
    return 1
  fi
}

# Skip something
# ------------------------------------------------------
# Offer the user a chance to skip something.
# Credit: https://github.com/cowboy/dotfiles
# ------------------------------------------------------
function skip() {
  REPLY=noskip
  read -t 5 -n 1 -s -p "${bold}To skip, press ${underline}X${reset}${bold} within 5 seconds.${reset}"
  if [[ "$REPLY" =~ ^[Xx]$ ]]; then
    notice "  Skipping!"
    return 0
  else
    notice "  Continuing..."
    return 1
  fi
}

# help
# ------------------------------------------------------
# Prints help for a script when invoked from the command
# line.  Typically via '-h'.  If additional flags or help
# text is available in the script they will be printed
# in the '$usage' variable.
# ------------------------------------------------------
function help () {
  echo "" 1>&2
  input "   $@" 1>&2
  if [ -n "${usage}" ]; then # print usage information if available
    echo "   ${usage}" 1>&2
  fi
  echo "" 1>&2
  exit 1
}


function pauseScript() {
  # A simple function used to pause a script at any point and
  # only continue on user input
  seek_confirmation "Ready to continue?"
  if is_confirmed; then
    info "Continuing"
  else
    warning "Exiting Script."
    safeExit
  fi
}

# Text Transformations
# -----------------------------------
# Transform text using these functions.
# Adapted from https://github.com/jmcantrell/bashful
# -----------------------------------

lower() {
  # Convert stdin to lowercase.
  # usage:  text=$(lower <<<"$1")
  #         echo "MAKETHISLOWERCASE" | lower
  tr '[:upper:]' '[:lower:]'
}

function lower_trim(){
	echo $1 | lower | trim
}

# Removes all whitespaces
trim() {
	tr -d '[:space:]'
}

only_letters_and_numbers(){
  tr -d -c '[:alnum:]'
}


function get_http_code_message(){
	case $1 in
       000) status="Not responding within timeout" ;;
       100) status="Informational: Continue" ;;
       101) status="Informational: Switching Protocols" ;;
       200) status="Successful: OK within timeout" ;;
       201) status="Successful: Created" ;;
       202) status="Successful: Accepted" ;;
       203) status="Successful: Non-Authoritative Information" ;;
       204) status="Successful: No Content" ;;
       205) status="Successful: Reset Content" ;;
       206) status="Successful: Partial Content" ;;
       300) status="Redirection: Multiple Choices" ;;
       301) status="Redirection: Moved Permanently" ;;
       302) status="Redirection: Found residing temporarily under different URI" ;;
       303) status="Redirection: See Other" ;;
       304) status="Redirection: Not Modified" ;;
       305) status="Redirection: Use Proxy" ;;
       306) status="Redirection: status not defined" ;;
       307) status="Redirection: Temporary Redirect" ;;
       400) status="Client Error: Bad Request" ;;
       401) status="Client Error: Unauthorized" ;;
       402) status="Client Error: Payment Required" ;;
       403) status="Client Error: Forbidden" ;;
       404) status="Client Error: Not Found" ;;
       405) status="Client Error: Method Not Allowed" ;;
       406) status="Client Error: Not Acceptable" ;;
       407) status="Client Error: Proxy Authentication Required" ;;
       408) status="Client Error: Request Timeout within timeout" ;;
       409) status="Client Error: Conflict" ;;
       410) status="Client Error: Gone" ;;
       411) status="Client Error: Length Required" ;;
       412) status="Client Error: Precondition Failed" ;;
       413) status="Client Error: Request Entity Too Large" ;;
       414) status="Client Error: Request-URI Too Long" ;;
       415) status="Client Error: Unsupported Media Type" ;;
       416) status="Client Error: Requested Range Not Satisfiable" ;;
       417) status="Client Error: Expectation Failed" ;;
       500) status="Server Error: Internal Server Error" ;;
       501) status="Server Error: Not Implemented" ;;
       502) status="Server Error: Bad Gateway" ;;
       503) status="Server Error: Service Unavailable" ;;
       504) status="Server Error: Gateway Timeout within timeout" ;;
       505) status="Server Error: HTTP Version Not Supported" ;;
       *)   status="status not defined." ;;
  esac
  echo "$status"
}
