#!/usr/bin/env bash

# Scenario for creating a sub instance
# ------------------------------------------------------

function sub_instance_start(){
	sub_instance_require
	sub_instance_check_preconditions
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
	require_build_version
	require_new_admin_password
	require_user_username
	require_user_password

	require_contact_person
	require_contact_email
	require_description
}

function sub_instance_check_preconditions(){
	sailing_dir="/home/sailing"
	servers_dir="$sailing_dir/servers"
	server_dir="$servers_dir/$instance_short_name"

	header "Checking preconditions"

	echo "Checking if directory $server_dir exists already..."
	execute_remote "[ ! -d $server_dir ]"

	echo "Checking if ssh connection $ssh_user@$super_instance is working..."
	execute_remote ls
}

function sub_instance_execute() {
	local server_env_file="$server_dir/env.sh"
	local readme_file="$servers_dir/README"
	local comment_out_line_in_env_with_pattern='JAVA_HOME=\/opt\/jdk1.8.0_20'
	local comment_in_line_in_env_with_pattern='sapjvm_gc'
	local refreshInstance_file='/home/sailing/code/java/target/refreshInstance.sh'

	header "Sub instance setup"

	# create sub instance server directory
	local_echo "Creating directory $server_dir ..."
	execute_remote mkdir $server_dir

	# copy refreshInstance.sh from /servers/server to sub instance directory
	local_echo "Copying $refreshInstance_file to $server_dir..."
	execute_remote cp $refreshInstance_file $server_dir

	# Execute refreshInstance.sh
	local_echo "Executing refreshInstance.sh with build version $build_version ..."
	execute_remote "export DEPLOY_TO=$instance_short_name;cd $server_dir;./refreshInstance.sh install-release $build_version > /dev/null 2>&1;"

	# uncommenting lines containing pattern
	local_echo "Commenting in $comment_in_line_in_env_with_pattern inside $server_env_file..."
	execute_remote "sed -i '/$comment_in_line_in_env_with_pattern/s/^#//g' $server_env_file"

	# commenting out lines containing pattern
	local_echo "Commenting out $comment_out_line_in_env_with_pattern inside $server_env_file..."
	execute_remote "sed -i '/$comment_out_line_in_env_with_pattern/s/^/#/g' $server_env_file"

	local_echo "Getting next unused SERVER_PORT..."
	local server_port=$(find_first_unused_number_for_variable "SERVER_PORT" $default_server_port)

	local_echo "Getting next unused TELNET_PORT..."
	local telnet_port=$(find_first_unused_number_for_variable "TELNET_PORT" $default_telnet_port)

	local_echo "Getting next unused EXPEDITION_PORT..."
	local expedition_port=$(find_first_unused_number_for_variable "EXPEDITION_PORT" $default_expedition_port)

	local env_patch=$(build_configuration "# PATCH $script_start_time" "SERVER_NAME=$(alphanumeric $instance_name)" "TELNET_PORT=$telnet_port" \
	"SERVER_PORT=$server_port" "EXPEDITION_PORT=$expedition_port" "MONGODB_NAME=$(alphanumeric $instance_name)" "MONGODB_HOST=$mongodb_host" \
	"MONGODB_PORT=$mongodb_port" "DEPLOY_TO=$instance_short_name")

	local_echo "Configuring $server_env_file..."
	execute_remote "echo -e \"$env_patch\" >> $server_env_file"

	local_echo "Checking for existance of README file..."
	execute_remote touch $readme_file

	local_echo "Appending patch to README file..."
	execute_remote "echo -e \"\n# $instance_short_name ($description, $contact_person, $contact_email)\n$env_patch\" >> $readme_file"

	# start server and redirect both stderr and stdout to /dev/null (&>/dev/null). Send command to background by &. Do this to avoid blocking.
	local_echo "Starting server..."
	execute_remote -f "sh -c \"cd $server_dir; nohup ./start > /dev/null 2>&1 &\""

	header "Event and user creation"

	# get access token
	access_token=$(get_access_token $admin_username $admin_password $super_instance $server_port)

	# create event
	event_id=$(create_event $access_token $super_instance $server_port $instance_name)

	# change admin password
	change_admin_password $access_token $super_instance $server_port $admin_username $new_admin_password

	# create new user
	user=$(create_new_user $access_token $super_instance $server_port $user_username $user_password)

	header "Configuring ALB"

	local target_group_arn=$(create_target_group $instance_name)
	register_targets $target_group_arn $(get_instance_id $super_instance)

	local domain=$(create_rule $listener_arn $instance_short_name $target_group_arn)

	header "Configuring Apache"

	append_event_ssl_macro_to_001_events_conf $domain $event_id $ssh_user $super_instance $server_port

	header "Conclusion"

	success "Sub instance should be reachable through https://$domain."
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
	execute_remote "for i in /home/sailing/servers/*/env.sh; do cat \$i | grep '^ *$1=' | tr -d '$1='; done | sort -n | tail -1"
}
