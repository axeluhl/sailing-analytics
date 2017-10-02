#!/usr/bin/env bash

function add_param() {
	if [ ! -z "$2" ]; then
		result=" --$1 $2"
	fi
	echo "$result"
}

function get_latest_release(){
	# get html from releases.sapsailing.com
	html=$(wget releases.sapsailing.com -q -O -)
	
	# extract all links 
	links=$(grep -Po '(?<=href=")[^"]*' <<< "$html") 
	
	# extract build strings (e.g. build-201709291756)
	builds=$(grep -Po 'build-\d+' <<< "$links")
	
	# sort build strings reverse using their date 
	result=$(sort -k1.1,1.8 -k1.9nr <<< "$builds")
	
	# take latest build
	echo "$result" | head -1
}

function create_empty_user_data_file(){
	if [ ! $(is_exists "${tmpDir}/$user_data_file") ]; then
		touch $user_data_file
	fi
}

# $1: access_token $:2 public_dns_name 
function create_event(){
	curl -s -X POST -H "Authorization: Bearer $1" "http://$2:8888/sailingserver/api/v1/events/createEvent" --data "venuename=Default" --data "createregatta=false" | jq -r '.eventid' | tr -d '\r'
}

# $1: access_token $2: public_dns_name $3: admin_username 4: admin_new_password
function change_admin_password(){
	curl -X POST -H "Authorization: Bearer $access_token" "http://$public_dns_name:8888/security/api/restsecurity/change_password" --data "username=$admin_username" --data "password=$new_admin_password"
}

# $1: access_token $2: public_dns_name 3: user_username 4: user_password
function create_new_user(){
	curl -X POST -H "Authorization: Bearer $access_token" "http://$public_dns_name:8888/security/api/restsecurity/create_user" --data "username=$user_username" --data "password=$user_password"
}

# $1: json_instance
function get_subnet_id(){
	echo $1 | jq -r '.Instances[0].SubnetId' | tr -d '\r'
}

# $1: json_instance
function get_instance_id(){
	echo $1 | jq -r '.Instances[0].InstanceId' | tr -d '\r'
}

# $1: instance_id
function query_public_dns_name(){
	aws --region $region ec2 describe-instances --instance-ids $1 --output text --query 'Reservations[*].Instances[*].PublicDnsName' | tr -d '\r'
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
	curl -s -X GET "http://$1:$2@$3:8888/security/api/restsecurity/access_token" | jq -r '.access_token' | tr -d '\r'
}

# $1: public_dns_name
function wait_for_create_event_resource(){
	while [[ $(curl -s -o /dev/null -w ''%{http_code}'' http://$1:8888/sailingserver/api/v1/events/createEvent) == "404" ]]; 
	do 
		sleep $http_retry_interval; 
	done
}

# $1: load_balancer_name $2: subnet_id
function create_elb(){
	aws elb create-load-balancer --load-balancer-name $1 --listeners "Protocol=HTTP,LoadBalancerPort=80,InstanceProtocol=HTTP,InstancePort=80" --subnets $2
}

# $1: json_elb
function get_elb_dns_name(){
	echo $1 | jq -r '.DNSName'
}

# $1: load_balancer_name 2: instance_id 
function add_instance_to_elb(){
	aws elb register-instances-with-load-balancer --load-balancer-name $1 --instances $2
}

# $1: json_response
function get_added_instance_from_elb(){
	echo "$json_response" | jq -r '.Instances[0].InstanceId'
}

function input_region(){
	if [ -z "$region_param" ]; then
		ask $(region_ask_message) region
		echo $region
	fi
}
function input_instance_type(){
	if [ -z "$instance_type_param" ]; then
		ask $(instance_type_ask_message) instance_type
		echo $instance_type
	fi
}

function input_instance_name(){
	if [ -z "$instance_name_param" ]; then
		ask_required $(instance_name_ask_message) instance_name 
		echo $instance_name
	fi
}

function input_instance_short_name(){
	if [ -z "$instance_short_name_param" ]; then
		ask_required $(instance_short_name_ask_message) instance_short_name
		echo $instance_short_name
	fi
}

function input_key_name(){
	if [ -z "$key_name_param" ]; then
		ask $(key_name_ask_message) key_name
		echo $key_name
	fi
}

function input_key_file(){
	if [ -z "$key_file_param" ]; then
		ask $(key_file_ask_message) key_file
		echo $key_file
	fi
}

function input_new_admin_password(){
	if [ -z "$new_admin_password_param" ]; then
		ask $(new_admin_password_ask_message) new_admin_password
		echo $new_admin_password
	fi
}

function input_mongo_db_host(){
	if [ -z "$mongo_db_host_param" ]; then
		ask $(mongo_db_host_ask_message) MONGODB_HOST
		echo $MONGODB_HOST
	fi
}

function input_mongo_db_port(){
	if [ -z "$mongo_db_port_param" ]; then
		ask $(mongo_db_port_ask_message) MONGODB_PORT
		echo $MONGODB_PORT
	fi
}



