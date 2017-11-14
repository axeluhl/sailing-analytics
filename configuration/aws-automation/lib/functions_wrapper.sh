#!/usr/bin/env bash

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
        success "[ OK ]"
        echo $response
      else
        error "[ ERROR ]"
        notice "Function (${FUNCNAME[1]}): [$status_code] $message\n\n$command"
        return 1
      fi
}

# -----------------------------------------------------------
# Does the error checking for aws commands. It also remove all
# carriage return symbols from response.
# -----------------------------------------------------------
function aws_wrapper(){
  local out;
  out=$(aws "$@")
  if command_was_successful $?; then
    success "[ OK ]"
    echo $out | sanitize
  else
    error "[ ERROR ]"
    return 1
  fi
}

# -----------------------------------------------------------
# Automatically uses key file for ssh if possible.
# -----------------------------------------------------------
function ssh_wrapper(){
  if [ -z $key_file ]; then
    ssh -o StrictHostKeyChecking=no "$@"
  else
    ssh -o StrictHostKeyChecking=no -i $key_file "$@"
  fi
}

# -----------------------------------------------------------
# Executes curl command until status code is 200
# -----------------------------------------------------------
function curl_until_http_200(){
	while [[ $(curl -s -o /dev/null -w ''%{http_code}'' --connect-timeout $http_retry_interval "$@") != "200" ]];
	do
		echo -n "."
		sleep $http_retry_interval;
	done
	echo ""
  success "[ OK ]"
}

# -----------------------------------------------------------
# Executes curl command until status code is 401.
# TODO: Fix code duplication (curl_until_http_200, curl_until_http_401)
# -----------------------------------------------------------
function curl_until_http_401(){
	while [[ $(curl -s -o /dev/null -w ''%{http_code}'' --connect-timeout $http_retry_interval "$@") != "401" ]];
	do
		echo -n "."
		sleep $http_retry_interval;
	done
	echo ""
  success "[ OK ]"
}
