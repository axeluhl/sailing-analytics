#!/bin/bash
for i in sapsailing-alb-logs sapsailing-access-logs sapsailing-access-logs-eu-west-2 sapsailing-alb-logs tokyo2020-access-logs tokyo2020-ap1-access-log tokyo2020-ap2-access-logs tokyo2020-eu-west-3-access-logs tokyo2020-us-east-1-access-logs tokyo2020-us1-access-logs; do
  aws s3 sync s3://$i ./$i
done
