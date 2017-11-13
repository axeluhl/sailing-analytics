#!/usr/bin/env bash

function curl_wrapper(){
    local out;
    out=$("$@")
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

function aws_wrapper(){
  local out;
  out=$("$@")
  if command_was_successful $?; then
    success "[ OK ]"
    echo $out | sanitize
  else
    error "[ ERROR ]"
    return 1
  fi
}

function do_until_http_200(){
	while [[ $("$@") != "200" ]];
	do
		echo -n "."
		sleep $http_retry_interval;
	done
	echo ""
  success "[ OK ]"
}

function do_until_http_401(){
	while [[ $("$@") != "401" ]];
	do
		echo -n "."
		sleep $http_retry_interval;
	done
	echo ""
  success "[ OK ]"
}
