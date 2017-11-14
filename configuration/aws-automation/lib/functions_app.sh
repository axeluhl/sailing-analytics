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
	curl_wrapper -X GET "http://$1:$2@$3:8888/security/api/restsecurity/access_token" | get_attribute '.access_token'
}

# -----------------------------------------------------------
# Creates a new event with event name = instance name, no regatta and venuename="Default"
# @param $1  access token of privileged user
# @param $2  dns name of instance
# @param $3  event name
# @return    event_id of created event
# -----------------------------------------------------------
function create_event(){
	local_echo "Creating event with name $3..."
  curl_wrapper -X POST -H "Authorization: Bearer $1" "http://$2:8888/sailingserver/api/v1/events/createEvent" --data "eventname=$3" --data "venuename=Default" --data "createregatta=false" | get_attribute '.eventid'
}

# -----------------------------------------------------------
# Changes password of user
# @param $1  access token of privileged user
# @param $2  dns name of instance
# @param $3  admin username
# @param $4  admin new password
# -----------------------------------------------------------
function change_admin_password(){
	local_echo "Changing password of user $3 to $4..."
	curl_wrapper -X POST -H "Authorization: Bearer $1" "http://$2:8888/security/api/restsecurity/change_password" --data "username=$3" --data "password=$4"
}

# -----------------------------------------------------------
# Creates new user
# @param $1  access token of privileged user
# @param $2  dns name of instance
# @param $3  user username
# @param $4  user password
# -----------------------------------------------------------
function create_new_user(){
	local_echo "Creating new user \"$3\" with password \"$4\"..."
	curl_wrapper -X POST -H "Authorization: Bearer $1" "http://$2:8888/security/api/restsecurity/create_user" --data "username=$3" --data "password=$4"
}

# -----------------------------------------------------------
# Wait until access token resource is available
# @param $1  admin username
# @param $2  admin password
# @param $3  public_dns_name
# -----------------------------------------------------------
function wait_for_access_token_resource(){
	echo -n "Wait until resource \"/security/api/restsecurity/access_token\" is available..."
	curl_until_http_200 http://$1:$2@$3:8888/security/api/restsecurity/access_token
}

# -----------------------------------------------------------
# Wait until create event resource is available
# @param $1  public_dns_name
# -----------------------------------------------------------
function wait_for_create_event_resource(){
	echo -n 'Wait until resource "/sailingserver/api/v1/events/createEvent" is available...'
	curl_until_http_401 http://$1:8888/sailingserver/api/v1/events/createEvent
}

# -----------------------------------------------------------
# Patch 001-events.conf
# @param $1  dns name
# @param $2  event id
# @param $3  ssh user
# @param $4  public dns name
# -----------------------------------------------------------
function configure_apache(){
	wait_for_ssh_connection "$3" "$4"
	local content=$(ssh_wrapper "$3"@"$4" "cat /etc/httpd/conf.d/001-events.conf")
	local patched_content=$(comment_plain_ssl_entry "$content")
	patched_content=$(append_event_entry "$patched_content" "$1" "$2")
	echo "$patched_content" | ssh_wrapper $3@$4 "cat > /etc/httpd/conf.d/001-events.conf"
	#local result=$(ssh_wraper $3@$4 "/etc/init.d/httpd reload")
}

function comment_plain_ssl_entry(){
	echo "$1" | sed -e '/Use Plain-SSL/ s/^#*/#/'
}

# -----------------------------------------------------------
# Append Use Event-SSL entry to string
# @param $1  content
# @param $2  dns name
# @param $2  event id
# -----------------------------------------------------------
function append_event_entry(){
	echo -e "$1\nUse Event $2 \"$3\" 127.0.0.1 8888"
}
