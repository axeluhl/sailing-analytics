# Variables for region "eu-west-1" (Ireland)

# HTTPS Listener of Application Load Balancer Sailing-eu-west-1
listener_arn='arn:aws:elasticloadbalancing:eu-west-1:017363970217:listener/app/Sailing-eu-west-1/32b89dbfe7f75097/f9212223209ac042'

# Sailing Analytics App
instance_security_group_ids=sg-eaf31e85

# SAP Sailing Analytics App HVM 1.59
image_id='ami-407ef339'

certificate_arn='arn:aws:acm:eu-west-1:017363970217:certificate/67a1515a-81be-472d-9459-85746d030564'
elb_security_group_ids=
mongodb_host=54.76.64.42
mongodb_port=27017
alb_domain='sapsailing.com'

#SL Multi-Instance Sailing Server
super_instance='ec2-34-250-136-229.eu-west-1.compute.amazonaws.com'
