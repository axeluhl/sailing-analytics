#!/usr/bin/env bash

function create_instance_with_elb(){
	user_input
	execute
}

function user_input(){
	input_region
	input_instance_type
	input_instance_name
	input_instance_short_name
	input_key_name
	input_key_file
	input_new_admin_password
}

function execute() {
	echo "Creating instance..."
	json_instance=$(run_instance)

	instance_id=$(get_instance_id "$json_instance")
	
	echo "Wait until instance is recognized by AWS..." 
	aws ec2 wait instance-exists --instance-ids $instance_id
	echo "The instance is now recognized."
	
	echo "Querying for the instance public dns name..." 
	public_dns_name=$(query_public_dns_name $instance_id)

	echo "The public dns name is: $public_dns_name"
	
	echo -n "Wait until ssh connection is established.." 
	wait_for_ssh_connection "$key_file" "$ssh_user" "$public_dns_name"
	echo "SSH Connection is established."
	
	if [ "$use_tmux" == "true" ]; then
		echo "Started tailing logfiles on panes 1,2,3."
		tail_instance
	fi
	
	echo -n "Wait until resource \"/security/api/restsecurity/access_token\" is available..."
	wait_for_access_token_resource "$admin_username" "$admin_password" "$public_dns_name"
	echo "Resource \"/security/api/restsecurity/access_token\" is available."
	
	echo "Getting access token..."
	access_token=$(get_access_token "$admin_username" "$admin_password" "$public_dns_name")
	echo "Got access token: $access_token"
	
	echo "Wait until resource \"/sailingserver/api/v1/events/createEvent\" is available..."
	wait_for_create_event_resource "$public_dns_name"
	echo "Resource \"/sailingserver/api/v1/events/createEvent\" is available."
	
	echo "Creating event..."
	event_id=$(create_event "$access_token" "$public_dns_name")
	echo "Created event with id: $event_id."
	
	echo "Changing admin password from $admin_password to $new_admin_password..."
	change_admin_password "$access_token" "$public_dns_name" "$admin_username" "$new_admin_password"

	echo "Creating new user \"$user_username\" with password \"$user_password\"..."
	create_new_user "$access_token" "$public_dns_name" "$user_username" "$user_password"
	
	# ignore new user for the moment because updating its priviliges via rest is not yet implemented
	
	echo "Creating elastic load balancer..."
	load_balancer_name=$(echo "$instance_name" | trim)
	json_load_balancer=$(create_load_balancer_http "$load_balancer_name")
  # json_load_balancer=$(create_load_balancer_https "$load_balancer_name" "$certificate_arn")
	load_balancer_dns_name=$(get_elb_dns_name "$json_load_balancer")
	echo "Created elastic load balancer: $load_balancer_dns_name."
	
	echo "Configuring load balancer health check..."
	configure_health_check_http "$load_balancer_name"
  # configure_health_check_https "$load_balancer_name"	
	echo "Configured load balancer health check..."
	
	echo "Adding instance to elb..."
	json_added_instance_response=$(add_instance_to_elb "$instance_name" "$instance_id")
	added_instance_id=$(get_added_instance_from_elb "$json_added_instance_response" )
	echo "Added instance \"$added_instance_id\" to elb \"$elb_dns_name\"."
	
	echo "Creating route53 record set (Name: $load_balancer_name.sapsailing.com Value: $load_balancer_dns_name Type: CNAME)"
	create_change_resource_record_set_file "asd" 60 "ddd"
	#change_resource_record_sets
	
	echo "Finished."
}

function run_instance(){
	prepare_user_data_variables
	create_user_data_file
	
	local command="aws --region $region ec2 run-instances"
	command+=$(add_param "region" $region)
	command+=$(add_param "image-id" $image_id)
	command+=$(add_param "count" $instance_count)
	command+=$(add_param "instance-type" $instance_type)
	command+=$(add_param "key-name" $key_name)
	command+=$(add_param "security-group-ids" $instance_security_group_ids)
	command+=$(add_param "user-data" 'file://${tmpDir}/$user_data_file')
	command+=$(add_param "tag-specifications" $(printf $tag_specifications $instance_name))
	
	eval "$command"
}

function create_user_data_file(){
	prepare_user_data_variables	
	write_user_data_to_file
}

function prepare_user_data_variables() {
	MONGODB_HOST="$mongodb_host"
	MONGODB_PORT="$mongodb_port"
	MONGODB_NAME="$(lower_trim $instance_name)"
	REPLICATION_CHANNEL="$MONGODB_NAME"
	SERVER_NAME="$MONGODB_NAME"
	USE_ENVIRONMENT="live-server"
	INSTALL_FROM_RELEASE="$(get_latest_release)"
	SERVER_STARTUP_NOTIFY="$default_server_startup_notify"
}

function write_user_data_to_file(){
	local CR_LF=$'\r'$'\n'

	content+="MONGODB_HOST=$MONGODB_HOST"
	content+=$CR_LF
	content+="MONGODB_PORT=$MONGODB_PORT"
	content+=$CR_LF
	content+="MONGODB_NAME=$MONGODB_NAME"
	content+=$CR_LF
	content+="INSTALL_FROM_RELEASE=$INSTALL_FROM_RELEASE"
	content+=$CR_LF
	content+="USE_ENVIRONMENT=$USE_ENVIRONMENT"
	content+=$CR_LF
	content+="REPLICATION_CHANNEL=$REPLICATION_CHANNEL"
	content+=$CR_LF
	content+="SERVER_NAME=$SERVER_NAME"
	content+=$CR_LF
	content+="SERVER_STARTUP_NOTIFY=$SERVER_STARTUP_NOTIFY"
	
	echo "$content" > "${tmpDir}/$user_data_file"
}





