#!/usr/bin/env bash

region=eu-west-2
instance_image_id=ami-39f3e25d
count=1
instance_type=t2.medium
instance_key_name=leonradeck-keypair
instance_key_file="C:\\Users\\d069485\\.ssh\\leonradeck-keypair.pem"
instance_security_group_ids=sg-871732ee
instance_name=
instance_short_name=
instance_tag_specifications="\'ResourceType=instance,Tags=[{Key=Name,Value=%s}]\'"
instance_user_data=

ssh_retry_interval=2
ssh_user=root

http_retry_interval=5

admin_username=admin
admin_password=admin
new_admin_password=admin

user_username=testuser
user_password=password

host_zone_id=Z1R8UBAEXAMPLE
record_file=file://C:\awscli\route53\change-resource-record-sets.json


# USER DATA
MONGODB_HOST=35.176.4.35
MONGODB_PORT=27017
INSTALL_FROM_RELEASE=build-201709151148

function instance_key_name_ask_message() {
printf "Please enter the key name of the keypair you want to use to connect to the instance (e.g. \"%s\"): " $instance_key_name
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

function instance_key_file_ask_message() {
printf "Please enter the file path of the keypair you want to use to connect to the instance (e.g. \"%s\"): " $instance_key_file
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


