#!/usr/bin/env bash

# Set default values here. 
# Cave: default values get overwritten by passing parameters to the script

# Region unspecific variables

default_key_name=leonradeck-keypair
default_key_file='/cygdrive/c/Users/d069485/.ssh/leonradeck-keypair.pem'
default_instance_type=t2.medium
default_server_startup_notify=leon.radeck@sap.com
default_new_admin_password=admin
default_user_username=
default_user_password=

# Constants for region "eu-west-1" (Ireland)

# default_region=eu-west-1
# default_key_file=''
# default_security_group_ids=sg-eaf31e85
# default_instance_name=
# default_instance_short_name=
# default_image_id=
# default_hosted_zone_id=
# default_certificate_arn=''
# default_elb_security_group=
# default_mongodb_host=54.76.64.42
# default_mongodb_port=27017

# Constants for region "eu-west-2" (London) - overwrite previous constants

default_region=eu-west-2
default_security_group_ids=sg-871732ee
default_instance_name=
default_instance_short_name=
default_image_id=ami-39f3e25d
default_hosted_zone_id=Z1R8UBAEXAMPLE
default_certificate_arn='arn:aws:iam::123456789012:server-certificate/my-server-cert'
default_elb_security_group=
default_mongodb_host=35.176.4.35 
default_mongodb_port=27017

# Constants

ssh_retry_interval=2
ssh_user=root
http_retry_interval=5
user_data_file=".userdata.txt"
tag_specifications="\'ResourceType=instance,Tags=[{Key=Name,Value=%s}]\'"
admin_username=admin
admin_password=admin
instance_count=1

function key_name_ask_message() {
	printf "Please enter the key name of the keypair you want to use to connect to the instance (e.g. \"%s\"): " $key_name
}

function region_ask_message() {
	printf "Please enter the region for the instance (default: \"%s\"): " $region
}

function instance_type_ask_message() {
printf "Please enter the instance type (default: \"%s\"): " $instance_type
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
printf "Please enter the ip adress of the mongo db server: (default: \"%s\"): " $MONGODB_HOST
}

function mongo_db_port_ask_message() {
printf "Please enter the port of the mongo db server: (default: \"%s\"): " $MONGODB_PORT
}


