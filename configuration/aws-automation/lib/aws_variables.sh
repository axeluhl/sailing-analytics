#!/usr/bin/env bash

# Set console input propsal values here. 

# Default values

default_region=eu-west-2
default_key_name=leonradeck-keypair
default_key_file='/cygdrive/c/Users/d069485/.ssh/leonradeck-keypair.pem'
default_instance_type=t2.medium
default_server_startup_notify=leon.radeck@sap.com
default_new_admin_password=admin
default_user_username=testuser
default_user_password=test
default_ssh_user=root

# Variables for region "eu-west-1" (Ireland)

instance_security_group_ids=sg-eaf31e85
image_id=
hosted_zone_id=
certificate_arn=''
elb_security_group_ids=
mongodb_host=54.76.64.42
mongodb_port=27017

# Variables for region "eu-west-2" (London) - overwrite previous constants

instance_security_group_ids=sg-871732ee
image_id=ami-39f3e25d
hosted_zone_id=Z1R8UBAEXAMPLE
certificate_arn='arn:aws:iam::123456789012:server-certificate/my-server-cert'
elb_security_group_ids=sg-871732ee
mongodb_host=35.176.42.142
mongodb_port=27017

# Other Variables

ssh_retry_interval=2
http_retry_interval=5
user_data_file=".userdata.txt"
change_resource_record_set_file=".change-resource-record-set.json"
tag_specifications="\'ResourceType=instance,Tags=[{Key=Name,Value=%s}]\'"
admin_username=admin
admin_password=admin
instance_count=1

# Setting variables

region=${region_param:-$default_region}
instance_type=${instance_type_param:-$default_instance_type}
key_name=${key_name_param:-$default_key_name}
key_file=${key_file_param:-$default_key_file}
user_username=${user_username_param:-$default_user_username}
user_password=${user_password_param:-$default_user_password}
instance_name=${instance_name_param:-$default_instance_name_param}
instance_short_name=${instance_short_name_param:-$default_instance_short_name}
new_admin_password=${new_admin_password_param:-$default_new_admin_password}
tail_instance=tail_instance_param
ssh_user=${ssh_user_param:-$default_ssh_user}

function region_ask_message() {
	printf "Please enter the region for the instance (default: \"%s\"): " $region
}

function instance_type_ask_message() {
	printf "Please enter the instance type (default: \"%s\"): " $instance_type
}

function key_name_ask_message() {
	printf "Please enter the key name of the keypair you want to use to connect to the instance (e.g. \"%s\"): " $key_name
}

function instance_name_ask_message() {
	printf "Please enter a name for the instance: (e.g \"WC Santander 2017\"): "
}

function instance_short_name_ask_message() {
	printf "Please enter a short name for the instance (e.g. \"wcs17\" for use as a subdomain): " 
}

function key_file_ask_message() {
	printf "Please enter the file path of the keypair you want to use to connect to the instance (e.g. \"%s\"): " $key_file
}

function new_admin_password_ask_message() {
	printf "Please enter a new password for the admin user (default: \"%s\"): " $new_admin_password
}

function mongo_db_host_ask_message() {
	printf "Please enter the ip adress of the mongo db server: (default: \"%s\"): " $mongodb_host
}

function mongo_db_port_ask_message() {
	printf "Please enter the port of the mongo db server: (default: \"%s\"): " $mongodb_port
}


