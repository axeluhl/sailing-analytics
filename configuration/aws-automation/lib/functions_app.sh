#!/usr/bin/env bash


# -----------------------------------------------------------
# Functions that are relevant for the configuration of the sap instance.
# -----------------------------------------------------------


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
	curl_until_response "200" http://$1:$2@$3:$4/security/api/restsecurity/access_token
	curl_wrapper -X GET "http://$1:$2@$3:$4/security/api/restsecurity/access_token" | get_attribute '.access_token'
}

# -----------------------------------------------------------
# Creates a new event with event name = instance name, no regatta and venuename="Default"
# @param $1  access token
# @param $2  dns name of instance
# @param $3  port
# @param $4  event name
# @return    event id of event
# -----------------------------------------------------------
function create_event(){
	local_echo -n "Creating event with name $4..."
	curl_until_response "401" http://$2:$3/sailingserver/api/v1/events/createEvent
  curl_wrapper -X POST -H "Authorization: Bearer $1" "http://$2:$3/sailingserver/api/v1/events/createEvent" --data "eventName=$4" --data "venuename=Default" --data "createregatta=false" | get_attribute '.eventid'
}

# -----------------------------------------------------------
# Changes password of user
# @param $1  access token
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
# @param $1  access token
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
# Creates event, changes admin password, creates user if parameters are not empty.
# @param $1  public dns name
# @param $2  port of application instance
# @param $3  event name
# @param $4  new admin password
# @param $5  user username
# @param $6  user password
# @return event id if event was created
# -----------------------------------------------------------
function configure_application(){
	if [ -z "$3" ] && [ -z "$4" ] && [ -z "$5" ] && [ -z "$6" ]; then
		return 0
	fi

	access_token=$(get_access_token "admin" "admin" $1 $2)

	if [ ! -z "$3" ]; then
		event_id=$(create_event $access_token $1 $2 $3)
	fi

  if [ ! -z "$4" ]; then
		response=$(change_admin_password $access_token $1 $2 "admin" $4)
	fi

	if [ ! -z "$5" ] && [ ! -z "$6" ]; then
		user=$(create_new_user $access_token $1 $2 $5 $6)
	fi

	echo ${event_id:-""}
}

# -----------------------------------------------------------
# Patch 001-events.conf with Even-SSL macro if event id is not empty. Else use Home-SSL macro.
# @param $1  domain
# @param $2  event id
# @param $3  ssh user
# @param $4  public dns name
# @param $5  server port
# -----------------------------------------------------------
function append_macro_to_001_events_conf(){
	wait_for_001_events_patch $3 $4

	if [ -z "$2" ]; then
		patched_content="Use Home-SSL $1 127.0.0.1 $5"
	else
		patched_content="Use Event-SSL $1 \\\"$2\\\" 127.0.0.1 $5"
	fi

	local_echo "Appending´\"$patched_content\" to $events_conf..."
	ssh_wrapper $3@$4 echo "$patched_content >> $events_conf"
}

# -----------------------------------------------------------
# Append specified environment to env.sh.
# @param $1  environment
# @param $2  ssh user
# @param $3  dns name of instance
# @param $4  path to env.sh file
# -----------------------------------------------------------
function append_environment_to_env_sh(){
	local env_file=${4:-'/home/sailing/servers/server/env.sh'}
	local_echo "Appending environment '$1' to env.sh..."
	env_content=$(wget -qO- http://releases.sapsailing.com/environments/$1)

	exit_on_fail ssh_wrapper $2@$3 "echo -e \"# START Environment: $1 \" >> $env_file"
	exit_on_fail ssh_wrapper $2@$3 "export ADDITIONAL_JAVA_ARGS='\$ADDITIONAL_JAVA_ARGS'; export MEMORY='$MEMORY'; echo \"$env_content\" >> $env_file"
	exit_on_fail ssh_wrapper $2@$3 "echo -e \"# END Environment: $1 \" >> $env_file"
}

# -----------------------------------------------------------
# Append string to env.sh
# @param $1  ssh user
# @param $2  dns name of instance
# @param $3  string value
# @param $4 path to env.sh file
# -----------------------------------------------------------
function append_to_env_sh(){
	local env_file=${4:-'/home/sailing/servers/server/env.sh'}
	exit_on_fail ssh_wrapper $1@$2 "echo -e \"$3\" >> $env_file"
}

# -----------------------------------------------------------
# Reloads httpd service after executing apachectl configtest.
# @param $1  ssh user
# @param $2  dns name of instance
# -----------------------------------------------------------
function reload_httpd(){
	local_echo "Reloading httpd..."
	ssh_wrapper $1@$2 "apachectl configtest >/dev/null 2>&1"
	out=$(ssh_wrapper $1@$2 "/etc/init.d/httpd reload")
}



# -----------------------------------------------------------
# Wait until 001_events.conf file appears on instance
# @param $1  ssh user
# @param $2  dns name
# -----------------------------------------------------------
function wait_for_001_events_patch(){
	echo "Waiting for 001-events.conf to be created..."
	wait_for_ssh_connection $1 $2
	do_until_true ssh_wrapper $1@$2 test -f $events_conf
}
