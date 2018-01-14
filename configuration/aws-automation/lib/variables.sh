#!/usr/bin/env bash

# SCRIPTNAME
scriptName=`basename $0` #Set Script Name variable
scriptBasename="$(basename ${scriptName} .sh)" # Strips '.sh' from scriptName

# TIMESTAMPS
declare -r script_start_time=$(LC_ALL=C date +"%Y-%m-%d %H:%M:%S")        # Returns: 2015-06-15 22:34:40
datestamp=$(LC_ALL=C date +%Y-%m-%d)       # Returns: 2015-06-14
hourstamp=$(LC_ALL=C date +%r)             # Returns: 10:34:40 PM
timestamp=$(LC_ALL=C date +%Y%m%d_%H%M%S)  # Returns: 20150614_223440
today=$(LC_ALL=C date +"%m-%d-%Y")         # Returns: 06-14-2015
longdate=$(LC_ALL=C date +"%a, %d %b %Y %H:%M:%S %z")  # Returns: Sun, 10 Jan 2016 20:47:53 -0500
gmtdate=$(LC_ALL=C date -u -R | sed 's/\+0000/GMT/') # Returns: Wed, 13 Jan 2016 15:55:29 GMT

# THISHOST
thisHost=$(hostname)

# Default region unspecific variables
default_region=eu-west-2
default_key_name=leonradeck-keypair
default_key_file='/cygdrive/c/Users/d069485/.ssh/leonradeck-keypair.pem'
default_instance_type=t2.medium
default_server_startup_notify=leon.radeck@sap.com
default_new_admin_password=admin
default_user_username=newuser
default_user_password=newpassword
latest_release=$(get_latest_release)
hosted_zone_id=Z1R8UBAEXAMPLE

# Other Variables
target_group_protocol='HTTPS'
target_group_port='443'
ssh_retry_interval=2
http_retry_interval=5
env_file=".env.txt"
change_resource_record_set_file=".change-resource-record-set.json"
tag_specifications="\'ResourceType=instance,Tags=[{Key=Name,Value=%s}]\'"
admin_username=admin
admin_password=admin
instance_count=1
ssh_user=root
default_instance_name="$script_start_time"
default_instance_short_name="$timestamp"
sailing_0='/home/sailing/servers/server/logs/sailing0.log.0'
sailing_err='/var/log/sailing.err'
sailing_out='/var/log/sailing.out'
events_conf='/etc/httpd/conf.d/001-events.conf'
default_server_port='8880'
default_telnet_port='14900'
default_expedition_port='2000'
default_contact_person='Unknown'
default_contact_email='Unkown'
