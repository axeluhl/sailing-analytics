# Kieler Woche 2014

Two systems for seamless switching:

 - "A": ec2-54-76-190-140.eu-west-1.compute.amazonaws.com
 - "B": ec2-54-76-225-59.eu-west-1.compute.amazonaws.com

ELB-based replication cluster:

User data for master:

<pre>
MONGODB_NAME=KW2014-ELB
REPLICATION_CHANNEL=KW2014-ELB
SERVER_STARTUP_NOTIFY=axel.uhl@sap.com
SERVER_NAME=KW2014-ELB-MASTER
USE_ENVIRONMENT=live-master-server
INSTALL_FROM_RELEASE=kiwo2014-release-201406260007
MEMORY=20000m
</pre>

User data for replicas:

<pre>
MONGODB_NAME=KW2014-ELB
REPLICATION_CHANNEL=KW2014-ELB
SERVER_STARTUP_NOTIFY=axel.uhl@sap.com
SERVER_NAME=KW2014-REPLICA3
REPLICATE_MASTER_SERVLET_HOST=XXX.XXX.XXX.XXX
USE_ENVIRONMENT=live-replica-server
INSTALL_FROM_RELEASE=kiwo2014-release-201406260007
MEMORY=20000m
</pre>