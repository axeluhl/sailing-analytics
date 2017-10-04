#!/usr/bin/env bash

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

# $1: access_token $:2 public_dns_name 
function create_event(){
	local_echo "Creating event..."
	event_id=$(curl -s -X POST -H "Authorization: Bearer $1" "http://$2:8888/sailingserver/api/v1/events/createEvent" --data "venuename=Default" --data "createregatta=false" | jq -r '.eventid' | tr -d '\r')

	if is_valid_event_id $event_id; then
		success "Created event with id: \"$event_id\"."
	else
		error "Failed creating event."
		echo $event_id
	fi
}

# $1: access_token $2: public_dns_name $3: admin_username 4: admin_new_password
function change_admin_password(){
	local_echo "Changing admin password from \"$3\" to \"$4\"..."
	result=$(curl -w ''%{http_code}'' -s -X POST -H "Authorization: Bearer $1" "http://$2:8888/security/api/restsecurity/change_password" --data "username=$3" --data "password=$4")
	
	if is_http_ok $result; then
		success "Changed admin password from \"$3\" to \"$4\"."
		echo $result
	else
		error "Failed changing password. Error: $result"
	fi
}

# $1: access_token $2: public_dns_name 3: user_username 4: user_password
function create_new_user(){
	local_echo "Creating new user \"$3\" with password \"$4\"..."
	result=$(curl -w ''%{http_code}'' -s -X POST -H "Authorization: Bearer $1" "http://$2:8888/security/api/restsecurity/create_user" --data "username=$3" --data "password=$4")
	
	if is_http_ok $result; then
		success "Successfully created user \"$3\"."
		echo $result
	else
		error "Failed creating user. Error: $result"
	fi
}

# $1: value
function is_number(){
	[[ $1 =~ ^-?[0-9]+$ ]]
}

# $1: value 
function is_http_ok(){
	is_number $1 && [ $1 == 200 ]
}

# $1: event_id
function is_valid_event_id(){
	[[ $1 =~ .{8}-.{4}-.{4}-.{4}-.{12} ]]
}

function is_error(){
	[ $1 -ne 0 ]
}

# $1: instance_id
function query_public_dns_name(){
	local_echo "Querying for the instance public dns name..." 
	public_dns_name=$(aws --region $region ec2 describe-instances --instance-ids $1 --output text --query 'Reservations[*].Instances[*].PublicDnsName' | tr -d '\r')

	if is_error $?; then
		error "Querying for instance public dns name failed."
	else
		success "Public dns name of instance \"$instance_id\" is \"$public_dns_name\"."
		echo $public_dns_name
	fi
}



# $1: key_file $2: ssh_user $3: public_dns_name
function wait_for_ssh_connection(){
	echo -n "Connecting to $2@$3..." 
	local status=""
	while [[ $status != ok ]]
	do
		echo -n "."
		status=$(ssh -o BatchMode=yes -o StrictHostKeyChecking=no -i $1 $2@$3 echo "ok" 2>&1 || true)
		sleep $ssh_retry_interval
	done
	echo ""
	success "SSH Connection \"$2@$3\" is established."
}

# $1: admin_username $2: admin_password $: public_dns_name
function wait_for_access_token_resource(){
	echo -n "Wait until resource \"/security/api/restsecurity/access_token\" is available..."
	while [[ $(curl -s -o /dev/null -w ''%{http_code}'' http://$1:$2@$3:8888/security/api/restsecurity/access_token) != "200" ]]; 
	do 
		echo -n "."
		sleep $http_retry_interval; 
	done
	echo ""
	success "Resource \"/security/api/restsecurity/access_token\" is now available."
}

# $1: public_dns_name
function wait_for_create_event_resource(){
	echo 'Wait until resource "/sailingserver/api/v1/events/createEvent" is available...'
	while [[ $(curl -s -o /dev/null -w ''%{http_code}'' http://$1:8888/sailingserver/api/v1/events/createEvent) != "401" ]]; 
	do 
		sleep $http_retry_interval; 
	done
	echo ""
	success "Resource \"/sailingserver/api/v1/events/createEvent\" is now available."
}

# $1: admin_username $2: admin_password 3: public_dns_name
function get_access_token(){
	local_echo "Getting access token..."
	access_token=$(curl -s -X GET "http://$1:$2@$3:8888/security/api/restsecurity/access_token" | jq -r '.access_token' | tr -d '\r')
	
	if is_error $?; then
		error "Failed getting access token."
	else
		success "Access token is: \"$access_token\""
		echo $access_token
	fi
}

# $1: load_balancer_name
function create_load_balancer_http(){
	local_echo "Creating load balancer \"$load_balancer_name\"..."
	json_result=$(aws elb create-load-balancer --load-balancer-name $1 --listeners "Protocol=HTTP,LoadBalancerPort=80,InstanceProtocol=HTTP,InstancePort=80" --availability-zones "$(get_availability_zones)" --security-groups "$elb_security_group_ids")
	
	if is_error $?; then
		error "Failed creating load balancer."
	else
		success "Created load balancer \"$load_balancer_name\"."
		echo $json_result
	fi
}

# NOT TESTED
# $1: load_balancer_name $2: certificate_arn
function create_load_balancer_https(){
	local_echo 'Creating load balancer "$load_balancer_name"...'
	json_result=$(aws elb create-load-balancer --load-balancer-name $1 --listeners "Protocol=HTTP,LoadBalancerPort=80,InstanceProtocol=HTTP,InstancePort=80" "Protocol=HTTPS,LoadBalancerPort=443,InstanceProtocol=HTTP,InstancePort=80,SSLCertificateId=$2" --availability-zones "$(get_availability_zones)" --security-groups "$elb_security_group_ids" | tr -d '\r')
	
	if is_error $?; then
		error "Failed creating load balancer."
	else
		success "Created load balancer \"$load_balancer_name\"."
		echo $json_result
	fi
}

# $1: load_balancer_name
function configure_health_check_http(){
	local_echo "Configuring load balancer health check..."	
	json_result=$(aws elb configure-health-check --load-balancer-name "$1" --health-check Target=HTTP:80/index.html,Interval=15,UnhealthyThreshold=2,HealthyThreshold=3,Timeout=5)
	
	if is_error $?; then
		error "Failed configuring health check."
	else
		success "Successfully configured healthcheck for load balancer \"$load_balancer_name\"."
		echo $json_result
	fi
}

# NOT TESTED
# $1: load_balancer_name
function configure_health_check_https(){
	local_echo "Configuring load balancer health check..."	
	json_result=$(aws elb configure-health-check --load-balancer-name "$1" --health-check Target=HTTPS:443/index.html,Interval=15,UnhealthyThreshold=2,HealthyThreshold=3,Timeout=5)
	
	if is_error $?; then
		error "Failed configuring health check."
	else
		success "Successfully configured healthcheck for load balancer \"$load_balancer_name\"."
		echo $json_result
	fi
}

# $1: load_balancer_name 2: instance_id 
function add_instance_to_elb(){
	local_echo "Adding instance to elb..."
	json_result=$(aws elb register-instances-with-load-balancer --load-balancer-name $1 --instances $2 | tr -d '\r')
	
	if is_error $?; then
		error "Failed adding instance to load balancer."
	else
		success "Successfully added instance \"$instance_id\" to load balancer \"$1\"."
		echo $json_result
	fi
}

# NOT TESTED
# $1: subdomain_name $2: TTL $3: load_balancer_dns_name 
function route53_change_resource_record(){
	create_route53_record_file "$1" "$2" "$3"
	
	local_echo "Creating Route53 record set (Name: $1.sapsailing.com Value: $3 Type: CNAME)..."
	json_result=$(aws route53 change-resource-record-sets --hosted-zone-id "$hosted_zone_id" --change-batch "file://${tmpDir}/$change_resource_record_set_file")
	
	if is_error $?; then
		error "Failed creating Route53 record."
	else
		success "Successfully created Route53 record."
		echo $json_result
	fi
}

# $1: sub_domain $2: TTL $: load_balancer_dns_name
function create_route53_record_file(){
	local json=$(printf '{"Changes":[{"Action":"CREATE","ResourceRecordSet": {"Name": "%s.sapsailing.com","Type": "CNAME","TTL": %s,"ResourceRecords":[{"Value": "%s"}]}}]}' $1 $2 $3)
	echo "$json" > "${tmpDir}/$change_resource_record_set_file"
}

# $1: instance_id
function wait_instance_exists(){
	local_echo "Wait until instance \"$1\" is recognized by AWS..." 
	result=$(aws ec2 wait instance-exists --instance-ids $1)
	
	if is_error $?; then
		error "Instance was not recognized by AWS."
	else
		success "The instance \"$1\" is now recognized by AWS."
		echo $json_result
	fi
}

function add_param() {
	if [ ! -z "$2" ]; then
		local result=" --$1 $2"
	fi
	echo "$result"
}

# $1: json_instance
function get_subnet_id(){
	echo $1 | jq -r '.Instances[0].SubnetId' | tr -d '\r'
}

# $1: json_instance
function get_instance_id(){
	echo $1 | jq -r '.Instances[0].InstanceId' | tr -d '\r'
}

function get_availability_zones(){
	aws ec2 --region $region describe-availability-zones --query "AvailabilityZones[].ZoneName"
}

# $1: json_elb
function get_elb_dns_name(){
	echo $1 | jq -r '.DNSName' | tr -d '\r'
}

# $1: json_response
function get_added_instance_from_elb(){
	echo "$json_response" | jq -r '.Instances[0].InstanceId'
}

function input_region(){
	if [ -z "$region_param" ]; then
		ask $(region_ask_message) default_region region
	else
		region="$region_param"
	fi
	
}

function input_instance_type(){
	if [ -z "$instance_type_param" ]; then
		ask $(instance_type_ask_message) default_instance_type instance_type 
	else
		instance_type="$instance_type_param"
	fi
}

function input_instance_name(){
	if [ -z "$instance_name_param" ]; then
		ask_required $(instance_name_ask_message) default_instance_name instance_name 
	else
		instance_name="$instance_name_param"
	fi
}

function input_instance_short_name(){
	if [ -z "$instance_short_name_param" ]; then
		ask_required $(instance_short_name_ask_message) default_instance_short_name instance_short_name
	else
		instance_short_name="$instance_short_name_param"
	fi
}

function input_key_name(){
	if [ -z "$key_name_param" ]; then
		ask $(key_name_ask_message) default_key_name key_name
	else
		key_name="$key_name_param"
	fi
}

function input_key_file(){
	if [ -z "$key_file_param" ]; then
		ask $(key_file_ask_message) default_key_file key_file
	else
		key_file="$key_file_param"
	fi
}

function input_new_admin_password(){
	if [ -z "$new_admin_password_param" ]; then
		ask $(new_admin_password_ask_message) default_new_admin_password new_admin_password
	else
		new_admin_password="$new_admin_password_param"
	fi
}

function input_user_username(){
	if [ -z "$user_username_param" ]; then
		ask $(user_username_ask_message) default_user_username user_username
	else
		user_username="$new_admin_password_param"
	fi
}

function input_user_password(){
	if [ -z "$user_password_param" ]; then
		ask $(new_admin_password_ask_message) default_user_password user_password
	else
		user_password="$new_admin_password_param"
	fi
}
