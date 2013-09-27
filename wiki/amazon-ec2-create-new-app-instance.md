# Create a new Amazon app instance

The following screenshots will help you creating a new Amazon application instance. An application instance is the container for exactly one Java application server. Each instance is configured using so called "user data". This configuration will then be read by the java server.

## AMI Selection
In the "Classic Wizard" select "My AMIs"

<img src="/wiki/images/amazon/StartInstance1.JPG" width="70%" height="70%"/>

Make sure to select the AMI with the name "App"

<img src="/wiki/images/amazon/StartInstance2.JPG" width="70%" height="70%"/>

Select a subnet that matches the one of the database server

<img src="/wiki/images/amazon/StartInstance3.JPG" width="70%" height="70%"/>

Select the correct instance type. Micro also works if you don't want to put that much load on your server.

<img src="/wiki/images/amazon/StartInstance4.JPG" width="70%" height="70%"/>

Leave Kernel and RAM disk to Default. Then put the configuration for this server into the text area labeled "User Data". This configuration should at least contain `MONGODB_*` and `REPLICATION_*` parameters. Here one example:

<pre>
BUILD_BEFORE_START=True
BUILD_FROM=master
RUN_TESTS=False
SERVER_STARTUP-NOTIFY=simon.marcel.pamies@sap.com
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

It does not really matter which SERVER_PORTS you use because there is only one app per instance. But this makes it easier to configure instances for Apache, a Load Balancer or other uses. If you want to create a cluster that is self-replicating and has a load balancer in front of it then make sure that all your instances are running on the same port.

<img src="/wiki/images/amazon/StartInstance5.JPG" width="70%" height="70%"/>

Use a new interface for your Network. Do not change any other field.

<img src="/wiki/images/amazon/StartInstance6.JPG" width="70%" height="70%"/>

Now make sure to click on the "Edit" button

<img src="/wiki/images/amazon/StartInstance7.JPG" width="70%" height="70%"/>

In order to select "Delete on Termination" for your root volume

<img src="/wiki/images/amazon/StartInstance8.JPG" width="70%" height="70%"/>

Then give your instance a meaningful name by inserting a value for the tag "Name"

<img src="/wiki/images/amazon/StartInstance9.JPG" width="70%" height="70%"/>

Use the existing "Administrator" Key. Do NOT generate your own key - this won't work. In order to access instances you should've got two private keys. If not then contact one of the administrators.

<img src="/wiki/images/amazon/StartInstance10.JPG" width="70%" height="70%"/>

Then select the preconfigured security group "Sailing Analytics App". Do NOT select anything other than that or create your own.

<img src="/wiki/images/amazon/StartInstance11.JPG" width="70%" height="70%"/>

Your instance is now launching. You can create alarms if you want to monitor the state of the application. Be aware that monitoring is costy.

<img src="/wiki/images/amazon/StartInstance12.JPG" width="70%" height="70%"/>

Your application is now ready. Check that everything is green.

<img src="/wiki/images/amazon/StartInstance13.JPG" width="70%" height="70%"/>

You can access the application server by using the DNS and the port you've configured.

<img src="/wiki/images/amazon/StartInstance14.JPG" width="70%" height="70%"/>