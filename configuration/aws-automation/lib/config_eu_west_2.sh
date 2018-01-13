# Variables for region "eu-west-2" (London)

# HTTPS Listener of Application Load Balancer DummyALB
listener_arn='arn:aws:elasticloadbalancing:eu-west-2:017363970217:listener/app/DummyALB/da70f61a914fc50a/643943f49bc2a5a8'

# Sailing Analytics App
instance_security_group_ids=sg-871732ee

# SAP Sailing Analytics App HVM 1.56
image_id=ami-39f3e25d

certificate_arn='arn:aws:acm:eu-west-2:017363970217:certificate/d2ae17b0-75ed-4b56-ac12-a80f9e5c493d'
elb_security_group_ids=sg-871732ee
mongodb_host=35.176.143.232
mongodb_port=27017
alb_domain='dummy2.sapsailing.com'

super_instance='ec2-35-177-132-223.eu-west-2.compute.amazonaws.com'
