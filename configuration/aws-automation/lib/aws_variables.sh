#!/usr/bin/env bash

# Set default values here. Cave: Default values are overwritten by parameter values
default_region=eu-west-2

default_count=1
default_instance_type=t2.micro
default_key_name=leonradeck-keypair
default_key_file="/cygdrive/c/Users/d069485/.ssh/leonradeck-keypair.pem"
default_security_group_ids=sg-871732ee
default_instance_name=
default_instance_short_name=

default_user_data=
default_new_admin_password=admin
default_hosted_zone_id=Z1R8UBAEXAMPLE
default_record_file=file://C:\awscli\route53\change-resource-record-sets.json
default_user_username=testuser
default_user_password=password

DEFAULT_MONGODB_HOST=35.176.4.35
DEFAULT_MONGODB_PORT=27017

# These variables are set automatically
MONGODB_NAME=
USE_ENVIRONMENT=
REPLICATION_CHANNEL=
SERVER_NAME=

# Constants
ssh_retry_interval=2
ssh_user=root
http_retry_interval=5
SERVER_STARTUP_NOTIFY=leon.radeck@sap.com
USER_DATA_FILE="./.userdata.txt"
admin_username=admin
admin_password=admin
tag_specifications="\'ResourceType=instance,Tags=[{Key=Name,Value=%s}]\'"
image_id=ami-39f3e25d

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


