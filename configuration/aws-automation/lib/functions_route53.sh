#!/usr/bin/env bash

# NOT TESTED
# -----------------------------------------------------------
# Create CNAME record for the hosted zone to forward to a load balancer
# @param $1  subdomain name
# @param $2  TTL
# @param $3  load balancer dns name
# @return    json result
# -----------------------------------------------------------
function route53_change_resource_record(){
	create_route53_record_file "$1" "$2" "$3"
	local_echo "Creating Route53 record set (Name: $1.sapsailing.com Value: $3 Type: CNAME)..."
	aws_wrapper aws route53 change-resource-record-sets --hosted-zone-id "$hosted_zone_id" --change-batch "file://${tmpDir}/$change_resource_record_set_file"
}

# -----------------------------------------------------------
# Creates a file which includes the record change as json
# @param $1  subdomain name
# @param $2  TTL
# @param $3  load balancer dns name
# -----------------------------------------------------------
function create_route53_record_file(){
	local json=$(printf '{"Changes":[{"Action":"CREATE","ResourceRecordSet": {"Name": "%s.sapsailing.com","Type": "CNAME","TTL": %s,"ResourceRecords":[{"Value": "%s"}]}}]}' $1 $2 $3)
	echo "$json" > "${tmpDir}/$change_resource_record_set_file"
}
