# Amazon EC2 for SAP Sailing Analytics

[[_TOC_]]

## General Information and Security

Since XXX 2013 this project is using EC2 as the server provider. Amazon Elastic Compute Cloud (EC2) is a central part of Amazon.com's cloud computing platform, Amazon Web Services (AWS). EC2 allows users to rent virtual computers on which to run their own computer applications. EC2 allows scalable deployment of applications by providing a Web service through which a user can boot an Amazon Machine Image to create a virtual machine, which Amazon calls an "instance", containing any software desired. A user can create, launch, and terminate server instances as needed, paying by the hour for active servers, hence the term "elastic".

This project is associated with an SAP Sailing Analytics account that, for billing purposes, is a subsidiary of a main SAP billing account. The Analytics account number is "0173-6397-0217 (simon.marcel.pamies@sap.com)" and connected to "SAP CMC Production (hagen.stanek@sap.com)". It has "Dr. Axel Uhl (axel.uhl@sap.com)" configured as operations officer that can be contacted by Amazon in case of problems with the instances.

The main entry point for the account is https://console.aws.amazon.com/. There you can only log in using the root account. You will then have access to not only the EC2 Console but also to the main account details (including billing details).

<img src="/wiki/images/amazon/RootAccount.JPG" width="100%" height="100%"/>

Associated to the root account are _n_ users that can be configured using the IAM (User Management, https://console.aws.amazon.com/iam/home). Each of these users can belong to different groups that have different rights associated. Currently two groups exist:

* **Administrators**: Users belonging to this group have access to all EC2 services (including IAM). They do not have the right to manage main account information (like billing).

* **Seniors**: Everyone belonging to this group can not access IAM but everything else.

Users configured in the IAM and at least belonging to the group Seniors can log in using the following url https://017363970217.signin.aws.amazon.com/console. All users that belong to one of these groups absolutely need to have MFA activated. MFA (Multi-Factor-Authentication) can be compared to the RSA token that needs to be input every time one wants to access the SAP network. After activation users need to synchronize their device using a barcode that is displayed in IAM. The device can be a software (Google Authenticator for iOS and Android) or a physical device.

<img src="/wiki/images/amazon/IAMUsers.JPG" width="100%" height="100%"/>

In addition to having a password and MFA set for one user one can activate "Access Keys". These keys are a combination of hashed username ("ID") and a password ("Key"). These are needed in case of API related access (e.g. S3 uploader scripts). One user should not have more than 1 access key active because of security concerns and never distribute them over insecure channels.

## EC2 Server Architecture for Sailing Analytics

The architecture is divided into logical tiers. These are represented by firewall configurations (Security Groups) that can be associated to Instances. Each tier can contain one or more instances. The following image depicts the parts of the architecture.

<img src="/wiki/images/amazon/EC2Architecture.JPG" width="100%" height="100%"/>

### Tiers

* **Webserver**: Holds one or more webserver instances that represent the public facing part of the architecture. Only instances running in this tier should have an Elastic IP assigned. In the image you can see one configured instance that delivers content for sapsailing.com. It has some services running on it like an Apache, the GIT repository and the UDP mirror. The Apache is configured to proxy HTTP(S) connections to an Archive or Live server.
* **Balancer**: Features an Elastic Load Balancer. Such balancers can be configured to distribute traffic among many other running instances. Internally an ELB consists of multiple balancing instances on which load is distributed by a DNS round robin so that bandwidth is not a limiting factor.
* **Database**: Instances handling all operations related to persistence. Must be reachable by the "Instance" and "Balancer+Group" tier. In the standard setup this tier only contains one database server that handles connections to MongoDB, MySQL and RabbitMQ.
* **Instances**: Space where all instances, that are not logically grouped, live. In the image one can see three running instances. One serving archived data, one serving a live event and one for build and test purposes.
* **Balancer+Group**: Analytics instances grouped and managed by an Elastic Load Balancer. A group is just a term describing multiple instances replicating from one master instance. The word "group" does in this context not refer to the so called "Placement Groups".

### Instances

<table>
<tr>
<td><b>Name</b></td>
<td><b>Access Key(s)</b></td>
<td><b>Security Group</b></td>
<td><b>Open Ports</b></td>
<td><b>Services</b></td>
<td><b>Description</b></td>
</tr>
<tr>
<td>Webserver (Elastic IP: 54.229.94.254)</td>
<td>Administrator</td>
<td>IN: 20, 80, 443, 2010-2015<br/>OUT: ALL</td>
<td>Webserver</td>
<td>Apache, GIT, Piwik, Bugzilla</td>
<td>This tier holds one instance that has one public Elastic IP associated. This instance manages all domains and subdomains associated with this project. It also contains the public GIT repository.</td>
</tr>
<tr>
<td>DB & Messaging</td>
<td>Administrator</td>
<td>IN: 22, 5672, 10200-10210, 27017<br/>OUT: ALL</td>
<td>Database and Messaging</td>
<td>MongoDB, MySQL</td>
<td>All databases needed by either the Analytics applications or tools like Piwik and Bugzilla are managed by this instance.</td>
</tr>
<tr>
<td>Archive</td>
<td>Administrator, Sailing User</td>
<td>IN: 22, 2010-2015, 8880-8899<br/>OUT: ALL</td>
<td>Sailing Analytics App</td>
<td>Java App</td>
<td>Instance handling the access to all historical races.</td>
</tr>
</table>

## HowTo

### Create a new Analytics application instance

Create a new Analytics instance as described in detail here [[wiki/amazon-ec2-create-new-app-instance]]. You should use a configuration like the following. If you want to bring the code to a defined level then make sure to specify the BUILD-FROM and BUILD-COMPLETE_NOTIFY variables. If you leave them empty the instance will start using a very old build.

<pre>
BUILD_BEFORE_START=True
BUILD_FROM=master
RUN_TESTS=False
SERVER_STARTUP_NOTIFY=simon.marcel.pamies@sap.com
SERVER_NAME=LIVE1
MEMORY=1024m
REPLICATION_HOST=172.31.25.253
REPLICATION_CHANNEL=sapsailinganalytics-live
TELNET_PORT=14888
SERVER_PORT=8888
MONGODB_HOST=172.31.25.253
MONGODB_PORT=10202
EXPEDITION_PORT=2010
REPLICATE_ON_START=False
REPLICATE_MASTER_SERVLET_HOST=
REPLICATE_MASTER_SERVLET_PORT=
REPLICATE_MASTER_QUEUE_HOST=
REPLICATE_MASTER_QUEUE_PORT=
</pre>

After your instance has been started (and build and tests are through) it will be publicly reachable if you chose a port between 8090 and 8099. If you filled the SERVER-STARTUP-NOTIFY field then you will get an email once the server has been started.

### Setup replicated instances with ELB



### Access build server and tests

### Access MongoDB database

## Glossary

<table>
<tr>
<td><b>Term</b></td>
<td><b>Description</b></td>
</tr>
<tr><td>Instance</td><td>Virtual machine that runs on a Xen host. Such an instance runs forever until it is stopped. It will be billed by hours it ran. Each start will be billed by a full hour.</td></tr>
<tr><td>Spot Instance</td><td>Instances that run whenever there are free resources. It is not possible to control when or where these instances run. These instances are much cheaper than normal instances.</td></tr>
<tr><td>Amazon Machine Image (AMI)</td><td>Amazon Machine Image: Image file that contains a filesystem and a preinstalled operating system. One can create AMIs very easily from a stopped Instance by first creating a snapshot and then converting it to an AMI.</td></tr>
<tr><td>Volume</td><td>An active harddisk that can be associated to one Instance.</td></tr>
<tr><td>IOPS</td><td>Input/Output operations per second. Metric used to denote the performance of a volume. The higher the IOPS value the better the speed. Be aware of the fact that IOPS is metered by IOPS/h and is very expensive. Use with care!</td></tr>
<tr><td>Snapshot</td><td>Snapshot of a Volume</td></tr>
<tr><td>Elastic IP</td><td>IP address that can be associated to an instance. Any Elastic-IP not associated to a running Instance costs some amount of money per hour.</td></tr>
<tr><td>Security Group</td><td>Firewall configuration that can be associated to an instance. There is no need of configuring iptables or such. One can associate many instances the the same Security Group.</td></tr>
<tr><td>Elastic Load Balancer (ELB)</td><td>Service that makes it possible to balance over services running on different instances.</td></tr>
<tr><td>Network Interfaces</td><td>Virtual network interfaces that are mapped to physical network interfaces on instances. </td></tr>
<tr><td>Placement Groups</td><td>Enables applications to get the full-bisection bandwidth and low-latency network performance required for tightly coupled, node-to-node communication. Placement Groups can only contain HVM instance and have other limitations described here: https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/using_cluster_computing.html</td></tr>
</table>