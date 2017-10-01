#!/usr/bin/env bash

function create_elb_standalone_instance(){
	create_user_data
	#user_input
	#execute
}

function user_input(){
	if [ -z "$region_param" ]; then
		ask $(region_ask_message) region
		echo $region
	fi
	if [ -z "$instance_type_param" ]; then
		ask $(instance_type_ask_message) instance_type
		echo $instance_type
	fi
	if [ -z "$instance_name_param" ]; then
		ask_required $(instance_name_ask_message) instance_name 
		echo $instance_name
	fi
	if [ -z "$instance_short_name_param" ]; then
		ask_required $(instance_short_name_ask_message) instance_short_name
		echo $instance_short_name
	fi
	if [ -z "$instance_key_name_param" ]; then
		ask $(instance_key_name_ask_message) instance_key_name
		echo $instance_key_name
	fi
	if [ -z "$instance_key_file_param" ]; then
		ask $(instance_key_file_ask_message) instance_key_file
		echo $instance_key_file
	fi
	if [ -z "$new_admin_password_param" ]; then
		ask $(new_admin_password_ask_message) new_admin_password
		echo $new_admin_password
	fi
	if [ -z "$mongo_db_host_param" ]; then
		ask $(mongo_db_host_ask_message) MONGODB_HOST
		echo $MONGODB_HOST
	fi
	if [ -z "$mongo_db_port_param" ]; then
		ask $(mongo_db_port_ask_message) MONGODB_PORT
		echo $MONGODB_PORT
	fi
}


function execute() {
	echo "Creating instance..."
	#json_instance=$(eval $(run_instance) | tr -d '\r')
	json_instance=`cat instance.txt`
	
	subnet_id=$(echo $json_instance | jq -r '.Instances[0].SubnetId')
	instance_id=$(echo $json_instance | jq -r '.Instances[0].InstanceId')
	
	echo "Wait until instance is recognized by AWS..." 
	aws ec2 wait instance-exists --instance-ids $instance_id
	echo "The instance is now recognized."
	
	echo "Querying for the instance public dns name..." 
	instance_public_dns_name=$(aws --region $region ec2 describe-instances --instance-ids $instance_id --output text --query 'Reservations[*].Instances[*].PublicDnsName' | tr -d '\r')
	echo "The public dns name is: $instance_public_dns_name"
	
	echo -n "Wait until ssh connection is established.." 
	status=""
	while [[ $status != ok ]]
	do
		echo -n "."
		status=$(ssh -o BatchMode=yes -o StrictHostKeyChecking=no -i $instance_key_file $ssh_user@$instance_public_dns_name echo "ok" 2>&1 || true)
		sleep $ssh_retry_interval
	done
	echo ""
	echo "SSH Connection is established."
	
	if $use_tmux; then
		echo "Started tailing logfiles on panes 1,2,3."
		# open ssh connection on panes 1,2,3
		tmux send-keys -t 1 "ssh -o StrictHostKeyChecking=no -i $instance_key_file $ssh_user@$instance_public_dns_name" C-m
		tmux send-keys -t 2 "ssh -o StrictHostKeyChecking=no -i $instance_key_file $ssh_user@$instance_public_dns_name" C-m
		tmux send-keys -t 3 "ssh -o StrictHostKeyChecking=no -i $instance_key_file $ssh_user@$instance_public_dns_name" C-m

		# tail some important files
		tmux send-keys -t 1 "clear;echo \"Waiting for file /home/sailing/servers/server/logs/sailing0.log.0 to appear...\";tail -F -v /home/sailing/servers/server/logs/sailing0.log.0" C-m
		tmux send-keys -t 2 "clear;tail -f -v /var/log/sailing.out" C-m
		tmux send-keys -t 3 "clear;tail -f -v /var/log/sailing.err" C-m
	fi
	
	echo -n "Wait until resource \"/security/api/restsecurity/access_token\" is available..."
	while [[ $(curl -s -o /dev/null -w ''%{http_code}'' http://$admin_username:$admin_password@$instance_public_dns_name:8888/security/api/restsecurity/access_token) != "200" ]]; 
	do 
		echo -n "."
		sleep $http_retry_interval; 
	done
	echo ""
	echo "Resource \"/security/api/restsecurity/access_token\" is available."
	
	echo "Getting access token..."
	access_token=$(curl -s -X GET "http://$admin_username:$admin_password@$instance_public_dns_name:8888/security/api/restsecurity/access_token" | jq -r '.access_token' | tr -d '\r')
	echo "Got access token: $access_token"
	
	echo "Wait until resource \"/sailingserver/api/v1/events/createEvent\" is available..."
	while [[ $(curl -s -o /dev/null -w ''%{http_code}'' http://$instance_public_dns_name:8888/sailingserver/api/v1/events/createEvent) == "404" ]]; 
	do 
		sleep $http_retry_interval; 
	done
	echo "Resource \"/sailingserver/api/v1/events/createEvent\" is available."
	
	echo "Creating event..."
	event_id=$(curl -s -X POST -H "Authorization: Bearer $access_token" "http://$instance_public_dns_name:8888/sailingserver/api/v1/events/createEvent" --data "venuename=Default" --data "createregatta=false" | jq -r '.eventid' | tr -d '\r')
	echo "Created event with id: $event_id."
	
	echo "Changing admin password from $admin_password to $new_admin_password..."
	curl -X POST -H "Authorization: Bearer $access_token" "http://$instance_public_dns_name:8888/security/api/restsecurity/change_password" --data "username=$admin_username" --data "password=$new_admin_password"
	echo "Changed admin password."

	echo "Creating new user \"$user_username\" with password \"$user_password\"..."
	curl -X POST -H "Authorization: Bearer $access_token" "http://$instance_public_dns_name:8888/security/api/restsecurity/create_user" --data "username=$user_username" --data "password=$user_password"
	echo "Created user."
	
	# ignore new user for the moment because updating its priviliges via rest is not yet implemented
	
	echo "Creating elastic load balancer..."
	json_elb=$(aws elb create-load-balancer --load-balancer-name $instance_name --listeners "Protocol=HTTP,LoadBalancerPort=80,InstanceProtocol=HTTP,InstancePort=80" --subnets $subnet_id)
	elb_dns_name=$(echo $json_elb | jq -r '.DNSName')
	echo "Created elastic load balancer: $elb_dns_name."

	echo "Adding instance to elb..."
	json_response=$(aws elb register-instances-with-load-balancer --load-balancer-name $instance_name --instances $instance_id)
	added_instance_id=$(echo $json_response | jq -r '.Instances[0].InstanceId')
	echo "Added instance \"$added_instance_id\" to elb \"$elb_dns_name\"."
}

function run_instance(){
	local command="aws --region $region ec2 run-instances"
	command+=$(add_param "region" $region)
	command+=$(add_param "image-id" $instance_image_id)
	command+=$(add_param "count" $count)
	command+=$(add_param "instance-type" $instance_type)
	command+=$(add_param "key-name" $instance_key_name)
	command+=$(add_param "security-group-ids" $instance_security_group_ids)
	command+=$(add_param "user-data" $instance_user_data)
	command+=$(add_param "tag-specifications" $(printf $instance_tag_specifications $instance_name))
	echo $command
}

function create_user_data(){
	user_data_file=$tmpDir/.userdata.txt
	content="hallo"
	touch $user_data_file

	if [ -f "$user_data_file" ]
	then 
		echo "$content" > "$user_data_file"
	fi
	
	sleep 100
}



