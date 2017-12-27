#!/usr/bin/env bash

# Create elastic ip and associate it with instance
# ------------------------------------------------------

function associate_elastic_ip_start(){
	associate_elastic_ip_require
	associate_elastic_ip_execute
}

# -----------------------------------------------------------
# All these variables are needed for this scenario
# If one variable is not assigned or passed by parameter
# the user will be prompted to enter a value
# -----------------------------------------------------------
function associate_elastic_ip_require(){
# will only be executed after instance creation
	:
}

function associate_elastic_ip_execute() {
	header "Elastic ip creation"

	# allocate elastic ip
	local elastic_ip=$(allocate_address)

	# associate elastic ip
	associate_address $instance_id $elastic_ip

	header "Apache configuration"

	# configure apache with event macro
	append_event_ssl_macro_to_001_events_conf $elastic_ip $event_id $ssh_user $elastic_ip '8888'
}
