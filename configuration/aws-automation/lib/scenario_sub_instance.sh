#!/usr/bin/env bash

# Scenario for creating a sub instance
# ------------------------------------------------------

function sub_instance_start(){
	sub_instance_require
	sub_instance_execute
}

# -----------------------------------------------------------
# All these variables are needed for this scenario
# If one variable is not assigned or passed by parameter
# the user will be prompted to enter a value
# -----------------------------------------------------------
function sub_instance_require(){
	require_super_instance

	require_key_name
	require_key_file
	require_instance_name
	require_instance_short_name
	require_new_admin_password
	require_user_username
	require_user_password

	# require_description
	# require_contact_person
	# require_contact_email
}

function sub_instance_execute() {
  local servers_dir="/home/sailing/servers/"
	local server_dir="$servers_dir$instance_short_name"
	local server_env_file="$server_dir/env.sh"
	local comment_out_line_in_env_with_pattern='JAVA_HOME=\/opt\/jdk1.8.0_20'
	local comment_in_line_in_env_with_pattern='sapjvm_gc'

	# create sub instance server directory
	execute_remote mkdir $server_dir

	# copy refreshInstance.sh from /servers/server to sub instance directory
	execute_remote cp /home/sailing/servers/server/refreshInstance.sh $server_dir

	# Execute refreshInstance.sh
	local latest_release=$(get_latest_release)
	execute_remote "export DEPLOY_TO=$instance_short_name;cd $server_dir;./refreshInstance.sh install-release $latest_release;"

	# uncommenting lines containing pattern
	execute_remote "sed -i '/$comment_in_line_in_env_with_pattern/s/^#//g' $server_env_file"

	# commenting out lines containing pattern
	execute_remote "sed -i '/$comment_out_line_in_env_with_pattern/s/^/#/g' $server_env_file"

	local server_port=$(find_first_unused_number_for_variable "SERVER_PORT" "8880")
	local telnet_port=$(find_first_unused_number_for_variable "TELNET_PORT" "14900")
	local expedition_port=$(find_first_unused_number_for_variable "EXPEDITION_PORT" "2000")

	# TODO: Append description to README

	local patch=$(build_configuration "# PATCH $script_start_time" "SERVER_NAME=$(alphanumeric $instance_name)" "TELNET_PORT=$telnet_port" \
	"SERVER_PORT=$server_port" "EXPEDITION_PORT=$expedition_port" "MONGODB_NAME=$(alphanumeric $instance_name)" "MONGODB_HOST=$mongodb_host" \
	"MONGODB_PORT=$mongodb_port" "DEPLOY_TO=$instance_short_name")

	# append patch to env.sh
	execute_remote "echo -e \"$patch\" >> $server_env_file"

	# start server and redirect both stderr and stdout to /dev/null (&>/dev/null). Send command to background by &. Do this to avoid blocking.
	execute_remote -f "sh -c \"cd $server_dir; nohup ./start > /dev/null 2>&1 &\""

	# get access token
	access_token=$(get_access_token $admin_username $admin_password $super_instance $server_port)

	# create event
	event_id=$(create_event $access_token $super_instance $server_port $instance_name)

	# change admin password
	change_admin_password $access_token $super_instance $server_port $admin_username $new_admin_password

	# create new user
	create_new_user $access_token $super_instance $server_port $user_username $user_password

	# TODO: handle situation if super instance is not inside application load balancer

	local target_group_arn=$(create_target_group $instance_name)
	register_targets $target_group_arn $(get_instance_id $super_instance)

	local domain=$(create_rule $listener_arn $instance_short_name $target_group_arn)

	append_event_ssl_macro_to_001_events_conf $domain $event_id $ssh_user $super_instance $server_port

	# TODO: write email
}

# -----------------------------------------------------------
# For a given variable name, find first number that is not already used within all env.sh files inside /home/sailing/servers.
# If no number is found, return $2
# @param $1  variable name (e.g. SERVER_PORT)
# @param $2  default number (e.g. 8888)
# -----------------------------------------------------------
function find_first_unused_number_for_variable(){
	numbers=($(find_all_distinct_values_of_variable_inside_env_files "$1"))
	if [ ${#numbers[@]} -eq 0 ]; then
		echo "$2"
	else
		find_first_missing_number_in_array "${numbers[@]}"
	fi
}

function execute_remote(){
	ssh_wrapper $ssh_user@$super_instance "$@"
}

function find_all_distinct_values_of_variable_inside_env_files(){
	execute_remote grep -Roh --include=env.sh "$1=.*" $servers_dir | tr -d "$1=" | sort | uniq
}
