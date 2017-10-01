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

function prepare_user_data_variables() {
	if [ -z "$MONGODB_NAME" ]; then
		MONGODB_NAME="$(echo $instance_name | lower | trim)"
	fi
	if [ -z "$REPLICATION_CHANNEL" ]; then
		REPLICATION_CHANNEL="$MONGODB_NAME"
	fi
	if [ -z "$SERVER_NAME" ]; then
		SERVER_NAME="$MONGODB_NAME"
	fi
	if [ -z "$USE_ENVIRONMENT" ]; then
		USE_ENVIRONMENT="$1"
	fi
	if [ -z "$INSTALL_FROM_RELEASE" ]; then
		INSTALL_FROM_RELEASE="$(get_latest_release)"
	fi
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



