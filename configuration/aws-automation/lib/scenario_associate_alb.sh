#!/usr/bin/env bash

# Add instance to application load balancer
# ------------------------------------------------------

function associate_alb_start(){
	associate_alb_require
	associate_alb_execute
}

# -----------------------------------------------------------
# All these variables are needed for this scenario
# If one variable is not assigned or passed by parameter
# the user will be prompted to enter a value
# -----------------------------------------------------------
function associate_alb_require(){
	# will only be executed after instance creation
	:
}

function associate_alb_execute() {
	# create target group for instance
	local target_group_arn=$(exit_on_fail create_target_group "S-dedicated-$instance_short_name")
	set_target_group_health_check "$target_group_arn" "HTTP" "/index.html" "80" "5" "4" "2" "2"

	# add instance to target group
	register_targets $target_group_arn $instance_id

	# create rule for host-based forwarding within alb
	local domain=$(exit_on_fail create_rule $listener_arn $instance_short_name $target_group_arn)

	header "Apache configuration"

	# configure apache with event macro
	append_macro_to_001_events_conf $domain $event_id $ssh_user $public_dns_name "8888"
}
