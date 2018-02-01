#!/usr/bin/env bash

# Scenario for creating a shared instance
# ------------------------------------------------------

function shared_instance_start(){
	shared_instance_require
	shared_instance_check_preconditions
	shared_instance_execute
}

# -----------------------------------------------------------
# All these variables are needed for this scenario
# If one variable is not assigned or passed by parameter
# the user will be prompted to enter a value
# -----------------------------------------------------------
function shared_instance_require(){
	require_super_instance_dns_name
	require_ssh_user

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

function shared_instance_check_preconditions(){
	sailing_dir="/home/sailing"
	servers_dir="$sailing_dir/servers"
	server_dir="$servers_dir/$instance_short_name"

	header "Checking preconditions"

	echo "Checking if directory $server_dir exists already..."
	execute_remote "[ ! -d $server_dir ]"

	echo "Checking if ssh connection $ssh_user@$super_instance_dns_name is working..."
	execute_remote ls
}

function shared_instance_execute() {
	local server_env_file="$server_dir/env.sh"
	local readme_file="$servers_dir/README"
	local comment_out_line_in_env_with_pattern='JAVA_HOME=\/opt\/jdk1.8.0_20'
	local comment_in_line_in_env_with_pattern='sapjvm_gc'
	local refreshInstance_file='/home/sailing/code/java/target/refreshInstance.sh'

	header "Sub instance setup"

	# create sub instance server directory
	local_echo "Creating directory $server_dir ..."
	execute_remote mkdir $server_dir

	local_echo "Getting next unused SERVER_PORT..."
	local server_port=$(find_first_unused_port "SERVER_PORT" $default_server_port)

	local_echo "Getting next unused TELNET_PORT..."
	local telnet_port=$(find_first_unused_port "TELNET_PORT" $default_telnet_port)

	local_echo "Getting next unused EXPEDITION_PORT..."
	local expedition_port=$(find_first_unused_port "EXPEDITION_PORT" $default_expedition_port)

	# copy refreshInstance.sh from /servers/server to sub instance directory
	local_echo "Copying $refreshInstance_file to $server_dir..."
	execute_remote cp $refreshInstance_file $server_dir

	# Execute refreshInstance.sh
	local_echo "Executing refreshInstance.sh with build version $build_version ..."
	execute_remote "set -o pipefail;export DEPLOY_TO=$instance_short_name;cd $server_dir;./refreshInstance.sh install-release $build_version > /dev/null 2>&1;"

	# uncommenting lines containing pattern
	local_echo "Commenting in $comment_in_line_in_env_with_pattern inside $server_env_file..."
	execute_remote "sed -i '/$comment_in_line_in_env_with_pattern/s/^#//g' $server_env_file"

	# commenting out lines containing pattern
	local_echo "Commenting out $comment_out_line_in_env_with_pattern inside $server_env_file..."
	execute_remote "sed -i '/$comment_out_line_in_env_with_pattern/s/^/#/g' $server_env_file"

	local env_patch=$(build_configuration "# PATCH $script_start_time" "SERVER_NAME=$(alphanumeric $instance_name)" "TELNET_PORT=$telnet_port" \
	"SERVER_PORT=$server_port" "EXPEDITION_PORT=$expedition_port" "MONGODB_NAME=$(alphanumeric $instance_name)" "MONGODB_HOST=$mongodb_host" \
	"MONGODB_PORT=$mongodb_port" "DEPLOY_TO=$instance_short_name")

	local_echo "Configuring $server_env_file..."
	execute_remote "echo -e \"$env_patch\" >> $server_env_file"

	local_echo "Creating README file if it does not exist already..."
	execute_remote touch $readme_file

	local_echo "Appending patch to README file..."
	execute_remote "echo -e \"\n# $instance_short_name ($description, $contact_person, $contact_email)\n$env_patch\" >> $readme_file"

	# start server and redirect both stderr and stdout to /dev/null (&>/dev/null). Send command to background by &. Do this to avoid blocking.
	local_echo "Starting server..."
	execute_remote -f "set -o pipefail;sh -c \"cd $server_dir; nohup ./start > /dev/null 2>&1 &\""

	header "Event and user creation"

	# get access token
	access_token=$(get_access_token $admin_username $admin_password $super_instance_dns_name $server_port)

	# create event
	event_id=$(create_event $access_token $super_instance_dns_name $server_port $instance_name)

	# change admin password
	change_admin_password $access_token $super_instance_dns_name $server_port $admin_username $new_admin_password

	# create new user
	user=$(create_new_user $access_token $super_instance_dns_name $server_port $user_username $user_password)

	header "Configuring ALB"

	local target_group_arn=$(create_target_group "S-shared-$instance_short_name")
	set_target_group_health_check "$target_group_arn" "HTTP" "/index.html" "$server_port" "5" "4" "2" "2"

	register_targets $target_group_arn $(get_instance_id $super_instance_dns_name)

	local domain=$(create_rule $listener_arn $instance_short_name $target_group_arn)

	header "Configuring Apache"

	append_event_ssl_macro_to_001_events_conf $domain $event_id $ssh_user $super_instance_dns_name $server_port

	local_echo "Reloading httpd..."
	out=$(execute_remote "/etc/init.d/httpd reload")

	header "Conclusion"

	success "Sub instance should be reachable through https://$domain."
}

# -----------------------------------------------------------
# For a given variable name, find first number that is not already used within all env.sh files inside /home/sailing/servers.
# If no number is found, return $2
# @param $1  variable name (e.g. SERVER_PORT)
# @param $2  default number (e.g. 8880)
# -----------------------------------------------------------
function find_first_unused_port(){
	last_port=($(get_last_used_port "$1"))
	if [ $? -ne 0 ]; then
		port_to_use=$2
	else
		port_to_use=$(($last_port+1))
	fi
	success $port_to_use
	echo $port_to_use
}

function execute_remote(){
	ssh_wrapper $ssh_user@$super_instance_dns_name "$@"
}

function get_last_used_port(){
	ssh_prewrapper $ssh_user@$super_instance_dns_name "set -o pipefail; for i in /home/sailing/servers/*/env.sh; do cat \$i | grep '^ *$1=' | tr -d '$1='; done | sort -n | tail -1"
}
