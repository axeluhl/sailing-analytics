#!/usr/bin/env bash


# -----------------------------------------------------------
# Get latest release build from releases.sapsailing.com
# @return  latest build
# -----------------------------------------------------------
function get_latest_release(){

	# get html from releases.sapsailing.com
	local html=$(wget releases.sapsailing.com -q -O -)

	# extract all links
	local links=$(grep -Po '(?<=href=")[^"]*' <<< "$html")

	# extract build strings (e.g. build-201709291756)
	local builds=$(grep -Po 'build-\d+' <<< "$links")

	# sort build strings reverse using their date
	local result=$(sort -k1.1,1.8 -k1.9nr <<< "$builds")

	# take latest build
	echo "$result" | head -1
}

# -----------------------------------------------------------
# Get access token
# @param $1  admin username
# @param $2  admin password
# @param $3  dns name of instance
# @return    access token
# -----------------------------------------------------------
function get_access_token(){
	local_echo "Getting access token..."
	local out=$(get_access_token_command $1 $2 $3)
	local status_code=$(get_status_code "$out")
	local response=$(get_response "$out")
	local message=$(get_http_code_message $status_code)

	if is_http_ok $status_code; then
		local access_token=$(echo "$response" | jq -r '.access_token' | tr -d '\r')
		success "Access token is: \"$access_token\""
		echo "$access_token"
	else
		error "Failed getting access token. Error [$status_code] $message"
	fi
}

function get_access_token_command(){
	curl -qSfsw '\n%{http_code}' -X GET "http://$1:$2@$3:8888/security/api/restsecurity/access_token"
}



# -----------------------------------------------------------
# Creates a new event with no regatta and venuename="Default"
# @param $1  access token of privileged user
# @param $2  dns name of instance
# @param $3  event name
# @return    event_id of created event
# -----------------------------------------------------------
function create_event(){
	local_echo "Creating event..."
	local out=$(create_event_command $1 $2 $3)
	local status_code=$(get_status_code "$out")
	local response=$(get_response "$out")
	local message=$(get_http_code_message $status_code)

	if is_http_ok $status_code; then
		local event_id=$(echo $response | jq -r '.eventid' | tr -d '\r')
		success "Created event with id: \"$event_id\"."
		echo "$event_id"
	else
		error "Failed creating event. [$status_code] $message"
	fi
}

function create_event_command(){
	curl -qSfsw '\n%{http_code}' -X POST -H "Authorization: Bearer $1" "http://$2:8888/sailingserver/api/v1/events/createEvent" --data "eventname=$3" --data "venuename=Default" --data "createregatta=false" 2>/dev/null
}

# -----------------------------------------------------------
# Changes password of user
# @param $1  access token of privileged user
# @param $2  dns name of instance
# @param $3  admin username
# @param $4  admin new password
# @return    status code
# -----------------------------------------------------------
function change_admin_password(){
	local_echo "Changing password of user \"$3\" to \"$4\"..."
	local out=$(change_admin_password_command $1 $2 $3 $4)
	local status_code=$(get_status_code "$out")
	local response=$(get_response "$out")
	local message=$(get_http_code_message $status_code)

	if is_http_ok $status_code; then
		success "Changed password to \"$4\"."
	else
		error "Failed changing password. [$status_code] $message"
	fi
}

function change_admin_password_command(){
	curl -qSfsw '\n%{http_code}' -X POST -H "Authorization: Bearer $1" "http://$2:8888/security/api/restsecurity/change_password" --data "username=$3" --data "password=$4"
}

# -----------------------------------------------------------
# Creates new user
# @param $1  access token of privileged user
# @param $2  dns name of instance
# @param $3  user username
# @param $4  user password
# @return    status code
# -----------------------------------------------------------
function create_new_user(){
	local_echo "Creating new user \"$3\" with password \"$4\"..."
	local out=$(create_new_user_command $1 $2 $3 $4)
	local status_code=$(get_status_code "$out")
	local response=$(get_response "$out")
	local message=$(get_http_code_message $status_code)

	if is_http_ok $result; then
		success "Successfully created user \"$3\"."
	else
		error "Failed creating user. [$status_code] $message"
	fi
}

# $1: access_token $2: public_dns_name $3: user_username $4: user_password
function create_new_user_command(){
	curl -qSfsw '\n%{http_code}' -X POST -H "Authorization: Bearer $1" "http://$2:8888/security/api/restsecurity/create_user" --data "username=$3" --data "password=$4"
}

# -----------------------------------------------------------
# Wait until access token resource is available
# @param $1  admin username
# @param $2  admin password
# @param $3  public_dns_name
# -----------------------------------------------------------
function wait_for_access_token_resource(){
	echo -n "Wait until resource \"/security/api/restsecurity/access_token\" is available..."
	while [[ $(wait_for_access_token_resource_command $1 $2 $3) != "200" ]];
	do
		echo -n "."
		sleep $http_retry_interval;
	done
	echo ""
	success "Resource \"/security/api/restsecurity/access_token\" is now available."
}

function wait_for_access_token_resource_command(){
	curl -s -o /dev/null -w ''%{http_code}'' --connect-timeout $http_retry_interval http://$1:$2@$3:8888/security/api/restsecurity/access_token
}

# -----------------------------------------------------------
# Wait until create event resource is available
# @param $1  public_dns_name
# -----------------------------------------------------------
function wait_for_create_event_resource(){
	echo 'Wait until resource "/sailingserver/api/v1/events/createEvent" is available...'
	while [[ $(wait_for_create_event_resource_command $1) != "401" ]];
	do
		sleep $http_retry_interval;
	done
	success "Resource \"/sailingserver/api/v1/events/createEvent\" is now available."
	echo ""
}

function wait_for_create_event_resource_command(){
	curl -s -o /dev/null -w ''%{http_code}'' --connect-timeout $http_retry_interval http://$1:8888/sailingserver/api/v1/events/createEvent
}
