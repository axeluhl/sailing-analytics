#!/usr/bin/env bash

# -----------------------------------------------------------
# Functions that encapsulate error checking or looping logic whilst calling other functions.
# -----------------------------------------------------------

DISPLAY_SUCCESS_FOR_THIS_COMMAND="true"
HTTP_RETRY_INTERVAL=5
TIMEOUT=300

# -----------------------------------------------------------
# Does the error checking for curl commands.
# Captures response and http code of curl command into variable $out.
# Checks if http code is 200.
# If yes print success and return response.
# If not then print error.
# @return  latest build
# -----------------------------------------------------------
function curl_wrapper(){
    local out;
    out=$(curl -qSfsw '\n%{http_code}' "$@")
    #local return_code=$?
    local command=$(echo "$@" | tr '\n' ' ')
    local status_code=$(get_status_code "$out")
    local response=$(get_response "$out")
    local message=$(get_http_code_message $status_code)

    if is_http_ok $status_code; then
        success ${response:-"[ OK ]"}
        echo $response
      else
        error "[ ERROR ]"
        notice "Function (${FUNCNAME[1]}): [$status_code] $message $response\n\n$command"
        return 1
      fi
}

# -----------------------------------------------------------
# Does the error checking for aws commands. It also remove all
# carriage return symbols from response.
# -----------------------------------------------------------
function aws_wrapper(){
  local out;
  out=$(aws --region $region "$@" 2>&1)
  if command_was_successful $?; then
    if [ "$DISPLAY_SUCCESS_FOR_THIS_COMMAND" != "false" ]; then
      success ${out:-"[ OK ]"}
    fi
    echo $out | sanitize
  else
    error "[ ERROR ] $out"
    safeExit
  fi
}

function disable_aws_success_output(){
  DISPLAY_SUCCESS_FOR_THIS_COMMAND="false"
}

function enable_aws_success_output(){
  DISPLAY_SUCCESS_FOR_THIS_COMMAND="true"
}

function ssh_prewrapper(){
  if [ -z $key_file ]; then
    ssh -o StrictHostKeyChecking=no "$@"
  else
    ssh -o StrictHostKeyChecking=no -i $key_file "$@"
  fi
}

# -----------------------------------------------------------
# Automatically uses key file for ssh if possible.
# -----------------------------------------------------------
function ssh_wrapper(){
  out=$(ssh_prewrapper "$@")
  if command_was_successful $?; then
    success ${out:-"[ OK ]"}
    echo $out
  else
    if ssh -o StrictHostKeyChecking=no "$@"; then
      echo $out
      return 0
    fi
    error "[ ERROR ] $out"
    return 1
  fi
}

# -----------------------------------------------------------
# Executes curl command until status code is 200
# -----------------------------------------------------------
function curl_until_response(){
  response_code=$1
  shift
  start_time="$(date -u +%s)"
	while [[ $(curl -s -o /dev/null -w ''%{http_code}'' --connect-timeout $HTTP_RETRY_INTERVAL "$@") != "$response_code" ]];
	do
    fail_on_timeout $start_time

		local_echo -n "."
		sleep $HTTP_RETRY_INTERVAL;
	done
	local_echo ""
}

function do_until_true(){
  start_time="$(date -u +%s)"
  until "$@"
	do
    fail_on_timeout $start_time

		echo -n .
		sleep $HTTP_RETRY_INTERVAL
	done
}

function fail_on_timeout(){
  end_time="$(date -u +%s)"
  if [ "$(($end_time-$1))" -gt $TIMEOUT ]; then
    error "TIMEOUT"
    safeExit
  fi
}

function exit_on_fail(){
  "$@" || { safeExit; }
}
