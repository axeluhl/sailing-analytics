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
# @param $4  port
# @return    access token
# -----------------------------------------------------------
function get_access_token(){
	local_echo -n "Getting access token..."
	curl_until_http_200 http://$1:$2@$3:$4/security/api/restsecurity/access_token
	curl_wrapper -X GET "http://$1:$2@$3:$4/security/api/restsecurity/access_token" | get_attribute '.access_token'
}

# -----------------------------------------------------------
# Creates a new event with event name = instance name, no regatta and venuename="Default"
# @param $1  access token of privileged user
# @param $2  dns name of instance
# @param $3  port
# @param $4  event name
# @return    event_id of created event
# -----------------------------------------------------------
function create_event(){
	local_echo -n "Creating event with name $4..."
	curl_until_http_401 http://$2:$3/sailingserver/api/v1/events/createEvent
  curl_wrapper -X POST -H "Authorization: Bearer $1" "http://$2:$3/sailingserver/api/v1/events/createEvent" --data "eventName=$4" --data "venuename=Default" --data "createregatta=false" | get_attribute '.eventid'
}

# -----------------------------------------------------------
# Changes password of user
# @param $1  access token of privileged user
# @param $2  dns name of instance
# @param $3  port
# @param $4  admin username
# @param $5  admin new password
# -----------------------------------------------------------
function change_admin_password(){
	local_echo "Changing password of user $4 to $5..."
	curl_wrapper -X POST -H "Authorization: Bearer $1" "http://$2:$3/security/api/restsecurity/change_password" --data "username=$4" --data "password=$5"
}

# -----------------------------------------------------------
# Creates new user
# @param $1  access token of privileged user
# @param $2  dns name of instance
# @param $3  port
# @param $4  user username
# @param $5  user password
# -----------------------------------------------------------
function create_new_user(){
	local_echo "Creating new user \"$4\" with password \"$5\"..."
	curl_wrapper -X POST -H "Authorization: Bearer $1" "http://$2:$3/security/api/restsecurity/create_user?username=$4&password=$5"
}

# -----------------------------------------------------------
# Patch 001-events.conf with ssl macros
# @param $1  dns name
# @param $2  event id
# @param $3  ssh user
# @param $4  public dns name
# @param $5  server port
# -----------------------------------------------------------
function append_event_ssl_macro_to_001_events_conf(){
	wait_for_ssh_connection $3 $4
	wait_for_001_events_patch $3 $4
	local patched_content="Use Event-SSL $1 \"$2\" 127.0.0.1 $5"
	local_echo "Appending´\"$patched_content\" to $events_conf..."
	ssh_wrapper $3@$4 echo "$patched_content >> $events_conf"
	local_echo "Reloading httpd..."
	out=$(ssh_wrapper $3@$4 "/etc/init.d/httpd reload")
}

function wait_for_001_events_patch(){
	echo "Waiting for 001-events.conf to be created..."
	until ssh_wrapper $1@$2 test -f $events_conf
	do
		echo -n .
		sleep 2
	done
}
