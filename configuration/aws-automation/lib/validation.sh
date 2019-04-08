#!/usr/bin/env bash

ValidHostnameRegex='^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\-]*[a-zA-Z0-9])\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\-]*[A-Za-z0-9])$'
ValidIpAddressRegex='^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$'
ValidInstanceIdRegex='i-[a-zA-Z0-9]{17}'
ValidLaunchTemplateIdRegex='lt-[a-zA-Z0-9]{17}'
ValidResourceARNRegex="arn:.+:.+:.+:.+:.+\/.+"
ValidInstanceARNRegex="arn:.+:.+:.+:.+:instance\/.+"
ValidSecurityGroupARNRegex="arn:.+:.+:.+:.+:security-group\/.+"
ValidImageARNRegex="arn:.+:.+:.+:.+:image\/.+"
ValidCertificateARNRegex="arn:.+:.+:.+:.+:certificate\/.+"
ValidLoadBalancerARNRegex="arn:.+:.+:.+:.+:loadbalancer\/.+"

function validate_resource_arn(){
	[[ "$1" =~ $ValidResourceARNRegex ]] && echo "$1" ||  error_message "ResourceARN \"$1\" not valid."
}

function validate_instance_id(){
	[[ "$1" =~ $ValidInstanceIdRegex ]] && echo "$1" ||  error_message "Instance id \"$1\" not valid."
}

function validate_launch_template_id(){
	[[ "$1" =~ $ValidLaunchTemplateIdRegex ]] && echo "$1" ||  error_message "Launch template id \"$1\" not valid."
}

function validate_hostname(){
	[[ "$1" =~ $ValidHostnameRegex ]] && echo "$1" ||  error_message "Hostname \"$1\" not valid."
}

function validate_ipaddress(){
	[[ "$1" =~ $ValidIpAddressRegex ]] && echo "$1" ||  error_message "Ip address \"$1\" not valid."
}

function error_message(){
	error "[ ERROR ] $1 (${FUNCNAME[@]})"
	return 1
}
