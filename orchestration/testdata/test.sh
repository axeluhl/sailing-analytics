# Environment (live-master-server): START (Sun Jul  1 22:21:04 UTC 2018)
REPLICATION_HOST=rabbit.internal.sapsailing.com
TELNET_PORT=14888
SERVER_PORT=8888
MONGODB_HOST=dbserver.internal.sapsailing.com
MONGODB_PORT=10202
EXPEDITION_PORT=2010
REPLICATE_ON_START=
REPLICATE_MASTER_SERVLET_HOST=
REPLICATE_MASTER_SERVLET_PORT=
REPLICATE_MASTER_QUEUE_HOST=
REPLICATE_MASTER_QUEUE_PORT=
ADDITIONAL_JAVA_ARGS="$ADDITIONAL_JAVA_ARGS -Xms$MEMORY -Dorg.eclipse.jetty.LEVEL=OFF -Dorg.eclipse.jetty.util.log.class=org.eclipse.jetty.util.log.StdErrLog -Drestore.tracked.races=true -Dpolardata.source.url=https://www.sapsailing.com -XX:ThreadPriorityPolicy=2"
# Environment: END
# User-Data: START (Sun Jul  1 22:21:04 UTC 2018)
INSTANCE_NAME=i-0e1db8380e0afa1d4
INSTANCE_IP4=34.244.12.197
INSTANCE_INTERNAL_IP4=172.31.11.188
INSTANCE_DNS=ec2-34-244-12-197.eu-west-1.compute.amazonaws.com
INSTALL_FROM_RELEASE=build-201806181113
USE_ENVIRONMENT=live-master-server
SERVER_NAME=test-steffen
REPLICATION_CHANNEL=test-steffen
MONGODB_NAME=test-steffen
SERVER_STARTUP_NOTIFY=
INSTANCE_ID="i-0e1db8380e0afa1d4 (34.244.12.197)"
# User-Data: END
