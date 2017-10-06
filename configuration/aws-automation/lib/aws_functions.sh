#!/usr/bin/env bash

# AWS specific functions 
# ------------------------------------------------------

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
	local out=$(get_access_token_command $1 $2 $3)
	local status_code=$(get_status_code "$out")
	local response=$(get_response "$out")
	local message=$(get_http_code_message $status_code)
	
	if is_http_ok $status_code; then
		local access_token=$(echo "$response" | jq -r '.access_token' | tr -d '\r')
		success "Access token is: \"$access_token\""
		echo "$access_token"
	else
		error "Failed getting access token. Error [$status_code] $message"
	fi
}

function get_access_token_command(){
	curl -qSfsw '\n%{http_code}' -X GET "http://$1:$2@$3:8888/security/api/restsecurity/access_token" 
}

# -----------------------------------------------------------
# Creates a new event with no regatta and venuename="Default"
# @param $1  access token of privileged user 
# @param $2  dns name of instance
# @return    event_id of created event
# -----------------------------------------------------------
function create_event(){
	local_echo "Creating event..."
	local out=$(create_event_command $1 $2)
	local status_code=$(get_status_code "$out")
	local response=$(get_response "$out")
	local message=$(get_http_code_message $status_code)

	if is_http_ok $status_code; then
		local event_id=$(echo $response | jq -r '.eventid' | tr -d '\r')
		success "Created event with id: \"$event_id\"."
	else
		error "Failed creating event. [$status_code] $message"
	fi
}

function create_event_command(){
	curl -qSfsw '\n%{http_code}' -X POST -H "Authorization: Bearer $1" "http://$2:8888/sailingserver/api/v1/events/createEvent" --data "venuename=Default" --data "createregatta=false" 2>/dev/null
}

# -----------------------------------------------------------
# Changes password of user
# @param $1  access token of privileged user 
# @param $2  dns name of instance 
# @param $3  admin username 
# @param $4  admin new password
# @return    status code
# -----------------------------------------------------------
function change_admin_password(){
	local_echo "Changing password of user \"$3\" to \"$4\"..."
	local out=$(change_admin_password_command $1 $2 $3 $4)
	local status_code=$(get_status_code "$out")
	local response=$(get_response "$out")
	local message=$(get_http_code_message $status_code)
	
	if is_http_ok $status_code; then
		success "Changed password to \"$4\"."
	else
		error "Failed changing password. [$status_code] $message"
	fi
}

function change_admin_password_command(){
	curl -qSfsw '\n%{http_code}' -X POST -H "Authorization: Bearer $1" "http://$2:8888/security/api/restsecurity/change_password" --data "username=$3" --data "password=$4"
}

# -----------------------------------------------------------
# Creates new user
# @param $1  access token of privileged user 
# @param $2  dns name of instance 
# @param $3  user username 
# @param $4  user password
# @return    status code
# -----------------------------------------------------------
function create_new_user(){
	local_echo "Creating new user \"$3\" with password \"$4\"..."
	local out=$(create_new_user_command $1 $2 $3 $4)
	local status_code=$(get_status_code "$out")
	local response=$(get_response "$out")
	local message=$(get_http_code_message $status_code)
	
	if is_http_ok $result; then
		success "Successfully created user \"$3\"."
	else
		error "Failed creating user. [$status_code] $message"
	fi
}

# $1: access_token $2: public_dns_name $3: user_username $4: user_password
function create_new_user_command(){
	curl -qSfsw '\n%{http_code}' -X POST -H "Authorization: Bearer $1" "http://$2:8888/security/api/restsecurity/create_user" --data "username=$3" --data "password=$4"
}

# -----------------------------------------------------------
# Query for public dns name of instance 
# @param $1  instance id
# @return    public dns name
# -----------------------------------------------------------
function query_public_dns_name(){
	local_echo "Querying for the instance public dns name..." 
	local public_dns_name=$(query_public_dns_name_command $1)
	
	if is_error $?; then
		error "Querying for instance public dns name failed."
	else
		success "Public dns name of instance \"$instance_id\" is \"$public_dns_name\"."
		echo $public_dns_name | tr -d '\r'
	fi
}

function query_public_dns_name_command(){
	aws --region "$region" ec2 describe-instances --instance-ids "$1" --output text --query 'Reservations[*].Instances[*].PublicDnsName'
}

# -----------------------------------------------------------
# Wait until ssh connection is established
# @param $1  key file to connect to instance
# @param $2  ssh user 
# @param $3  dns name of instance
# -----------------------------------------------------------
function wait_for_ssh_connection(){
	echo -n "Connecting to $2@$3..." 
	local status=
	while [[ $status != ok ]]
	do
		echo -n "."
		status=$(wait_for_ssh_connection_command $1 $2 $3)
		sleep $ssh_retry_interval
	done
	echo ""
	success "SSH Connection \"$2@$3\" is established."
}

function wait_for_ssh_connection_command(){
	ssh -o BatchMode=yes -o StrictHostKeyChecking=no -i $1 $2@$3 echo "ok" 2>&1 || true
}

# -----------------------------------------------------------
# Wait until access token resource is available
# @param $1  admin username
# @param $2  admin password 
# @param $3  public_dns_name
# -----------------------------------------------------------
function wait_for_access_token_resource(){
	echo -n "Wait until resource \"/security/api/restsecurity/access_token\" is available..."
	while [[ $(wait_for_access_token_resource_command $1 $2 $3) != "200" ]]; 
	do 
		echo -n "."
		sleep $http_retry_interval; 
	done
	echo ""
	success "Resource \"/security/api/restsecurity/access_token\" is now available."
}

function wait_for_access_token_resource_command(){
	curl -s -o /dev/null -w ''%{http_code}'' http://$1:$2@$3:8888/security/api/restsecurity/access_token
}

# -----------------------------------------------------------
# Wait until create event resource is available
# @param $1  public_dns_name
# -----------------------------------------------------------
function wait_for_create_event_resource(){
	echo 'Wait until resource "/sailingserver/api/v1/events/createEvent" is available...'
	while [[ $(wait_for_create_event_resource_command $1) != "401" ]]; 
	do 
		sleep $http_retry_interval; 
	done
	success "Resource \"/sailingserver/api/v1/events/createEvent\" is now available."
	echo ""
}

function wait_for_create_event_resource_command(){
	curl -s -o /dev/null -w ''%{http_code}'' http://$1:8888/sailingserver/api/v1/events/createEvent
}

# -----------------------------------------------------------
# Wait until instance is recognized by AWS
# @param $1  instance_id
# -----------------------------------------------------------
function wait_instance_exists(){
	local_echo "Wait until instance \"$1\" is recognized by AWS..." 
	local result=$(aws ec2 wait instance-exists --instance-ids $1)
}

# -----------------------------------------------------------
# Create load balancer with HTTP rule 
# @param $1  load_balancer_name
# @return    json result 
# -----------------------------------------------------------
function create_load_balancer_http(){
	local_echo "Creating load balancer \"$load_balancer_name\"..."
	local json_result=$(create_load_balancer_http_command $1)
	
	if is_error $?; then
		error "Failed creating load balancer."
	else
		success "Created load balancer \"$load_balancer_name\"."
		echo $json_result
	fi
}

function create_load_balancer_http_command(){
	aws elb create-load-balancer --load-balancer-name $1 --listeners "Protocol=HTTP,LoadBalancerPort=80,InstanceProtocol=HTTP,InstancePort=80" --availability-zones "$(get_availability_zones)" --security-groups "$elb_security_group_ids"
}

# NOT TESTED
# -----------------------------------------------------------
# Create load balancer with HTTPS rule 
# @param $1  load_balancer_name
# @return    json result 
# -----------------------------------------------------------
function create_load_balancer_https(){
	local_echo 'Creating load balancer "$load_balancer_name"...'
	local json_result=$(create_load_balancer_https_command $1 $2)
	
	if is_error $?; then
		error "Failed creating load balancer."
	else
		success "Created load balancer \"$load_balancer_name\"."
		echo $json_result | tr -d '\r'
	fi
}

function create_load_balancer_https_command(){
	aws elb create-load-balancer --load-balancer-name $1 --listeners "Protocol=HTTP,LoadBalancerPort=80,InstanceProtocol=HTTP,InstancePort=80" "Protocol=HTTPS,LoadBalancerPort=443,InstanceProtocol=HTTP,InstancePort=80,SSLCertificateId=$2" --availability-zones "$(get_availability_zones)" --security-groups "$elb_security_group_ids" 
}

# -----------------------------------------------------------
# Configure health check HTTP
# @param $1  load balancer name
# @return    json result 
# -----------------------------------------------------------
function configure_health_check_http(){
	local_echo "Configuring load balancer health check..."	
	local json_result=$(configure_health_check_http_command $1)
	
	if is_error $?; then
		error "Failed configuring health check."
	else
		success "Successfully configured healthcheck for load balancer \"$load_balancer_name\"."
		echo $json_result
	fi
}

# $1: load_balancer_name
function configure_health_check_http_command(){
	aws elb configure-health-check --load-balancer-name "$1" --health-check Target=HTTP:80/index.html,Interval=15,UnhealthyThreshold=2,HealthyThreshold=3,Timeout=5
}

# NOT TESTED
# -----------------------------------------------------------
# Configure health check HTTPS
# @param $1  load balancer name
# @return    json result 
# -----------------------------------------------------------
function configure_health_check_https(){
	local_echo "Configuring load balancer health check..."	
	local json_result=$(configure_health_check_https $1)
	
	if is_error $?; then
		error "Failed configuring health check."
	else
		success "Successfully configured healthcheck for load balancer \"$load_balancer_name\"."
		echo $json_result
	fi
}

function configure_health_check_https_command(){
	aws elb configure-health-check --load-balancer-name "$1" --health-check Target=HTTPS:443/index.html,Interval=15,UnhealthyThreshold=2,HealthyThreshold=3,Timeout=5
}

# -----------------------------------------------------------
# Add instance to load balancer
# @param $1  load balancer name
# @param $2  instance id
# @return    json result 
# -----------------------------------------------------------
function add_instance_to_elb(){
	local_echo "Adding instance to elb..."
	local json_result=$(add_instance_to_elb_command $1 $2)
	
	if is_error $?; then
		error "Failed adding instance to load balancer."
	else
		success "Successfully added instance \"$instance_id\" to load balancer \"$1\"."
		echo $json_result | tr -d '\r'
	fi
}

function add_instance_to_elb_command(){
	aws elb register-instances-with-load-balancer --load-balancer-name $1 --instances $2 
}

# NOT TESTED
# -----------------------------------------------------------
# Create CNAME record for the hosted zone to forward to a load balancer
# @param $1  subdomain name 
# @param $2  TTL
# @param $3  load balancer dns name
# @return    json result 
# -----------------------------------------------------------
function route53_change_resource_record(){
	create_route53_record_file "$1" "$2" "$3"
	
	local_echo "Creating Route53 record set (Name: $1.sapsailing.com Value: $3 Type: CNAME)..."
	local json_result=$(route53_change_resource_record_command $1 $2 $3)
	
	# condition does not work, will add a validator
	if is_error $?; then
		error "Failed creating Route53 record."
	else
		# success "Successfully created Route53 record."
		echo $json_result
	fi
}

function route53_change_resource_record_command(){
	aws route53 change-resource-record-sets --hosted-zone-id "$hosted_zone_id" --change-batch "file://${tmpDir}/$change_resource_record_set_file"
}

# -----------------------------------------------------------
# Creates a file which includes the record change as json
# @param $1  subdomain name 
# @param $2  TTL
# @param $3  load balancer dns name 
# -----------------------------------------------------------
function create_route53_record_file(){
	local json=$(printf '{"Changes":[{"Action":"CREATE","ResourceRecordSet": {"Name": "%s.sapsailing.com","Type": "CNAME","TTL": %s,"ResourceRecords":[{"Value": "%s"}]}}]}' $1 $2 $3)
	echo "$json" > "${tmpDir}/$change_resource_record_set_file"
}

# -----------------------------------------------------------
# Get subnet id of instance
# @param $1  json instance 
# @return    subnet id
# -----------------------------------------------------------
function get_subnet_id(){
	echo $1 | jq -r '.Instances[0].SubnetId' | tr -d '\r'
}

# -----------------------------------------------------------
# Get instance id of instance
# @param $1  json instance 
# @return    instance id
# -----------------------------------------------------------
function get_instance_id(){
	echo $1 | jq -r '.Instances[0].InstanceId' | tr -d '\r'
}

# -----------------------------------------------------------
# Creates a parameter from a key and value
# @param $1  key
# @param $2  value
# @return    result ("--key value")
# -----------------------------------------------------------
function get_availability_zones(){
	aws ec2 --region $region describe-availability-zones --query "AvailabilityZones[].ZoneName"
}

# -----------------------------------------------------------
# Get load balancer dns name
# @param $1  json load balancer 
# @return    load balancer dns name
# -----------------------------------------------------------
function get_elb_dns_name(){
	echo $1 | jq -r '.DNSName' | tr -d '\r'
}

# -----------------------------------------------------------
# Get added instance id from json response of load balancer creation
# @param $1  json load balancer 
# @return    added instance id 
# -----------------------------------------------------------
function get_added_instance_from_elb(){
	echo "$1" | jq -r '.Instances[0].InstanceId'
}

function require_region(){
	require_variable "$region_param" region "$default_region" "$region_ask_message"
}

function require_instance_type(){
	require_variable "$instance_type_param" instance_type "$default_instance_type" "$instance_type_ask_message"

}
function require_instance_name(){
	require_variable "$instance_name_param" instance_name "$default_instance_name" "$instance_name_ask_message"
}

function require_instance_short_name(){
	require_variable "$instance_short_name_param" instance_short_name "$default_instance_short_name" "$instance_short_name_ask_message"
}

function require_key_name(){
	require_variable "$key_name_param" key_name "$default_key_name" "$key_name_ask_message"
}

function require_key_file(){
	require_variable "$key_file_param" key_file "$default_key_file" "$key_file_ask_message"
}

function require_new_admin_password(){
	require_variable "$new_admin_password_param" new_admin_password "$default_new_admin_password" "$new_admin_password_ask_message"
}

function require_user_username(){
	require_variable "$user_username_param" user_username "$default_user_username" "$user_username_ask_message"
}

function require_user_password(){
	require_variable "$user_password_param" user_password "$default_user_password" "$user_password_ask_message"
}

function require_public_dns_name(){
	require_variable "$public_dns_name_param" public_dns_name "" "$public_dns_name_ask_message"
}

function require_ssh_user(){
	require_variable "$ssh_user_param" ssh_user "" "$ssh_user_ask_message"
}
