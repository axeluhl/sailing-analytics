#!/usr/bin/env bash

# Set variables here
# ------------------------------------------------------

# Script does not automatically source region specific variables
# This functionality will be added in the future 
# Please comment out the variables of the region you are not using

default_region=eu-west-2
default_key_name=leonradeck-keypair
default_key_file='/cygdrive/c/Users/d069485/.ssh/leonradeck-keypair.pem'
default_instance_type=t2.medium
default_server_startup_notify=leon.radeck@sap.com
default_new_admin_password=admin
default_user_username=testuser
default_user_password=test

# Variables for region "eu-west-2" (London) 

instance_security_group_ids=sg-871732ee
image_id=ami-39f3e25d
hosted_zone_id=Z1R8UBAEXAMPLE
certificate_arn='arn:aws:iam::123456789012:server-certificate/my-server-cert'
elb_security_group_ids=sg-871732ee
mongodb_host=35.176.42.142
mongodb_port=27017

# Variables for region "eu-west-1" (Ireland)

# instance_security_group_ids=sg-eaf31e85
# image_id=
# hosted_zone_id=
# certificate_arn=''
# elb_security_group_ids=
# mongodb_host=54.76.64.42
# mongodb_port=27017


# Other Variables

ssh_retry_interval=2
http_retry_interval=5
user_data_file=".userdata.txt"
change_resource_record_set_file=".change-resource-record-set.json"
tag_specifications="\'ResourceType=instance,Tags=[{Key=Name,Value=%s}]\'"
admin_username=admin
admin_password=admin
instance_count=1
ssh_user=root

region_ask_message="Please enter the region for the instance: " 
instance_type_ask_message="Please enter the instance type: " 
key_name_ask_message="Please enter the name of your keypair to connect to the instance: " 
instance_name_ask_message="Please enter a name for the instance: (e.g \"WC Santander 2017\"): "
instance_short_name_ask_message="Please enter a short name for the instance (e.g. \"wcs17\"): " 
key_file_ask_message="Please enter the file path of the keypair: "
new_admin_password_ask_message="Please enter a new password for the admin user: " 
mongo_db_host_ask_message="Please enter the ip adress of the mongo db server: " 
mongo_db_port_ask_message="Please enter the port of the mongo db server: " 
user_username_ask_message="Please enter the username of your new user: " 
user_password_ask_message="Please enter the password of your new user: " 
public_dns_name_ask_message="Please enter the public dns name: "
ssh_user_ask_message="Please enter the ssh user: "
