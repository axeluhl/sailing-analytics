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
default_user_username=newuser
default_user_password=newpassword
hosted_zone_id=Z1R8UBAEXAMPLE

# Variables for region "eu-west-2" (London)

instance_security_group_ids=sg-871732ee
image_id=ami-39f3e25d
certificate_arn='arn:aws:iam::123456789012:server-certificate/my-server-cert'
elb_security_group_ids=sg-871732ee
mongodb_host=35.176.143.232
mongodb_port=27017

# Variables for region "eu-west-1" (Ireland)

# instance_security_group_ids=sg-eaf31e85
# image_id=
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
default_instance_name="$now"
default_instance_short_name="$timestamp"
sailing_0='/home/sailing/servers/server/logs/sailing0.log.0'
sailing_err='/var/log/sailing.err'
sailing_out='/var/log/sailing.out'
events_conf='/etc/httpd/conf.d/001-events.conf'
