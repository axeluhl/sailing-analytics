# Kieler Woche 2014

Two systems for seamless switching:

 - "A": ec2-54-76-190-140.eu-west-1.compute.amazonaws.com
 - "B": ec2-54-76-225-59.eu-west-1.compute.amazonaws.com

ELB-based replication cluster:

User data for master:

MONGODB_NAME=KW2014
REPLICATION_CHANNEL=KW2014
SERVER_STARTUP_NOTIFY=axel.uhl@sap.com
SERVER_NAME=KW2014
USE_ENVIRONMENT=live-master-server
INSTALL_FROM_RELEASE=kiwo2014-release-201406241522
MEMORY=20000m

User data for replicas:

MONGODB_NAME=KW2014
REPLICATION_CHANNEL=KW2014
SERVER_STARTUP_NOTIFY=axel.uhl@sap.com
SERVER_NAME=KW2014
REPLICATE_MASTER_SERVLET_HOST=ec2-54-76-235-207.eu-west-1.compute.amazonaws.com
USE_ENVIRONMENT=live-replica-server
INSTALL_FROM_RELEASE=kiwo2014-release-201406260007
MEMORY=20000m