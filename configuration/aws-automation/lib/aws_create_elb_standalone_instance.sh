#!/usr/bin/env bash

USE_ENVIRONMENT_VALUE="live-server"

function create_elb_standalone_instance(){
	echo ${tmpDir}
	create_user_data_file
	#user_input
	#execute
}

function user_input(){
	input_region
	input_instance_type
	input_instance_name
	input_instance_short_name
	input_key_name
	input_key_file
	input_new_admin_password
	input_mongo_db_host
	input_mongo_db_port
}

function create_instance(){
	echo $(eval $(run_instance))
	#echo $(cat instance.txt)
}

function get_subnet_id(){
	echo $1 | jq -r '.Instances[0].SubnetId' | tr -d '\r'
}

function get_instance_id(){
	echo $1 | jq -r '.Instances[0].InstanceId' | tr -d '\r'
}

function query_public_dns_name(){
	echo $(aws --region $region ec2 describe-instances --instance-ids $1 --output text --query 'Reservations[*].Instances[*].PublicDnsName' | tr -d '\r')
}

# $1: key_file $2: ssh_user $3: public_dns_name
function wait_for_ssh_connection(){
	status=""
	while [[ $status != ok ]]
	do
		echo -n "."
		status=$(ssh -o BatchMode=yes -o StrictHostKeyChecking=no -i $1 $2@$3 echo "ok" 2>&1 || true)
		sleep $ssh_retry_interval
	done
	echo ""
}

# $1: admin_username $2: admin_password $: public_dns_name
function wait_for_access_token_resource(){
	while [[ $(curl -s -o /dev/null -w ''%{http_code}'' http://$1:$2@$3:8888/security/api/restsecurity/access_token) != "200" ]]; 
	do 
		echo -n "."
		sleep $http_retry_interval; 
	done
	echo ""
}

# $1: admin_username $2: admin_password 3: public_dns_name
function get_access_token(){
	echo $(curl -s -X GET "http://$1:$2@$3:8888/security/api/restsecurity/access_token" | jq -r '.access_token' | tr -d '\r')
}

# $1: public_dns_name
function wait_for_create_event_resource(){
	while [[ $(curl -s -o /dev/null -w ''%{http_code}'' http://$1:8888/sailingserver/api/v1/events/createEvent) == "404" ]]; 
	do 
		sleep $http_retry_interval; 
	done
}

# $1: access_token $:2 public_dns_name 
function create_event(){
	echo $(curl -s -X POST -H "Authorization: Bearer $1" "http://$2:8888/sailingserver/api/v1/events/createEvent" --data "venuename=Default" --data "createregatta=false" | jq -r '.eventid' | tr -d '\r')
}

# $1: access_token $2: public_dns_name $3: admin_username 4: admin_new_password
function change_admin_password(){
	curl -X POST -H "Authorization: Bearer $access_token" "http://$public_dns_name:8888/security/api/restsecurity/change_password" --data "username=$admin_username" --data "password=$new_admin_password"
}

# $1: access_token $2: public_dns_name 3: user_username 4: user_password
function create_new_user(){
	curl -X POST -H "Authorization: Bearer $access_token" "http://$public_dns_name:8888/security/api/restsecurity/create_user" --data "username=$user_username" --data "password=$user_password"
}


function execute() {
	prepare_user_data_variables $USE_ENVIRONMENT_VALUE
	create_user_data_file
	
	echo "Creating instance..."
	json_instance=$(create_instance)
	
	subnet_id=$(get_subnet_id $json_instance)
	instance_id=$(get_instance_id $json_instance)
	
	echo "Wait until instance is recognized by AWS..." 
	aws ec2 wait instance-exists --instance-ids $instance_id
	echo "The instance is now recognized."
	
	echo "Querying for the instance public dns name..." 
	public_dns_name=$(query_public_dns_name $instance_id)
	echo "The public dns name is: $public_dns_name"
	
	echo -n "Wait until ssh connection is established.." 
	wait_for_ssh_connection "$key_file" "$ssh_user" "$public_dns_name"
	echo "SSH Connection is established."
	
	if $use_tmux; then
		echo "Started tailing logfiles on panes 1,2,3."
		tmux_open_connections
		tmux_tail_logfiles
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
	change_admin_password "$access_token" "$public_dns_name" "$admin_username" "$admin_new_password"
	echo "Changed admin password."

	echo "Creating new user \"$user_username\" with password \"$user_password\"..."
	create_new_user "$access_token" "$public_dns_name" "$user_username" "$user_password"
	echo "Created user."
	
	# ignore new user for the moment because updating its priviliges via rest is not yet implemented
	
	echo "Creating elastic load balancer..."
	json_elb=$(create_elb "$instance_name" "$subnet_id")
	elb_dns_name=$(get_elb_dns_name "$json_elb")
	echo "Created elastic load balancer: $elb_dns_name."

	echo "Adding instance to elb..."
	json_added_instance_response=$(add_instance_to_elb "$instance_name" "$instance_id")
	added_instance_id=$(get_added_instance_from_elb "$json_added_instance_response" )
	echo "Added instance \"$added_instance_id\" to elb \"$elb_dns_name\"."
}

# $1: load_balancer_name $2: subnet_id
function create_elb(){
	echo $(aws elb create-load-balancer --load-balancer-name $1 --listeners "Protocol=HTTP,LoadBalancerPort=80,InstanceProtocol=HTTP,InstancePort=80" --subnets $2)
}

# $1: json_elb
function get_elb_dns_name(){
	echo $(echo $1 | jq -r '.DNSName')
}

# $1: load_balancer_name 2: instance_id 
function add_instance_to_elb(){
	echo $(aws elb register-instances-with-load-balancer --load-balancer-name $1 --instances $2)
}

# $1: json_response
function get_added_instance_from_elb(){
	echo $(echo $json_response | jq -r '.Instances[0].InstanceId')
}

function run_instance(){
	local command="aws --region $region ec2 run-instances"
	command+=$(add_param "region" $region)
	command+=$(add_param "image-id" $image_id)
	command+=$(add_param "count" $count)
	command+=$(add_param "instance-type" $instance_type)
	command+=$(add_param "key-name" $key_name)
	command+=$(add_param "security-group-ids" $security_group_ids)
	command+=$(add_param "user-data" "file://$user_data_file")
	command+=$(add_param "tag-specifications" $(printf $tag_specifications $instance_name))
	echo $command
}

function create_user_data_file(){
	prepare_user_data_variables	
	write_user_data_to_file
}

function create_empty_user_data_file(){
	if [ ! $(is_exists "${tmpDir}/$user_data_file") ]; then
		touch $user_data_file
	fi
}

function delete_user_data_file(){
	rm $user_data_file
}

function write_user_data_to_file(){
	create_empty_user_data_file
	
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





