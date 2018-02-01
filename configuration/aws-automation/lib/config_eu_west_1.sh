# Variables for region "eu-west-1" (Ireland)

default_key_name='Leon'
default_key_file='/cygdrive/c/Users/d069485/.ssh/Leon.pem'

# HTTPS Listener of Application Load Balancer Sailing-eu-west-1
listener_arn='arn:aws:elasticloadbalancing:eu-west-1:017363970217:listener/app/Sailing-eu-west-1/32b89dbfe7f75097/f9212223209ac042'

# Sailing Analytics App
instance_security_group_ids=sg-eaf31e85

# SAP Sailing Analytics App HVM 1.61
image_id='ami-7719840e'
image_ssh_user="root"

certificate_arn='arn:aws:acm:eu-west-1:017363970217:certificate/67a1515a-81be-472d-9459-85746d030564'
elb_security_group_ids=
mongodb_host=
mongodb_port=
alb_domain='sapsailing.com'

#SL Multi-Instance Sailing Server
default_super_instance_dns_name=''
default_ssh_user=$image_ssh_user
