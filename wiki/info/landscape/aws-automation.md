# AWS Automation

This page describes the functionality of a bash script that automates the setup of SAP Sailing Analytics instances.

## Importance

- Avoiding misconfiguration of instances due to human mistakes
- Allowing fast reaction times to external needs (e.g. horizontal scaling)
- Saving time 


## Scenarios

- SAP instance on a dedicated EC2 instance
- SAP instance on a shared EC2 instance
- SAP instance on a dedicated EC2 instance as a master
- SAP instance on a dedicated EC2 instance as a replica

## Basics 

### 1. Example setup: SAP instance on a dedicated EC2 instance

Login to the [https://aws.amazon.com/console/](AWS Web Console). 
Account number: 017363970217. 

Parameters necessary for EC2 instance: 

- Keypair 
- Instance type (e.g. t2.medium)
- Security group 
- Image
- User Data

Example of content for parameter User Data:

<pre>
MONGODB_HOST=123.123.123.123
MONGODB_PORT=27017
MONGODB_NAME=wcsantander2017 
SERVER_NAME=wcsantander2017
USE_ENVIRONMENT=live-server
INSTALL_FROM_RELEASE=build-201803302246
SERVER_STARTUP_NOTIFY=leon.radeck@sap.com
</pre>

### 2. SAP instance configuration

[image1]

Necessary configuration steps:

- Create event in admin console
- Create new user account with permissions for that event
- Change admin password

If instance home page or event page should be reachable by a specific URL:

Add one of the following lines to /etc/httpd/conf.d/001-events.conf:
<pre>
Use Home-SSL [instance name].sapsailing.com 127.0.0.1 8888" 
</pre> 
<pre>
Use Event-SSL [instance name].sapsailing.com “[event id]“ 127.0.0.1 8888
</pre>

Then check and reload apache configuration by entering the commands:
<pre>
apachectl configtest
sudo service httpd reload
</pre>

### 3. Load Balancer configuration

To reach the SAP instance by a specific URL (e.g. wcsantander2017.sapsailing.com), follow these steps:

- Create target group with name "S-dedicated-wcsantander2017"
- Create rule within HTTPS listener of load balancer. Enter "wcsantander2017.sapsailing.com" as a host name matching rule. Choose target group created in step one.
- Configure the health check of the target group
- Register instance within the target group

[image2]

### AWS Command Line Interface







