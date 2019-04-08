# Sailing Leagues

## bundesliga2016

### URL
- Public ELB: https://bundesliga2016.sapsailing.com
- Public ELB: https://bundesliga2-2016.sapsailing.com
- Public Master WEB: https://bundesliga2016-master.sapsailing.com
- Master Instance: `german-master.league.sapsailing.com`

### Firing up replica's

```
INSTALL_FROM_RELEASE=build-201608091706
USE_ENVIRONMENT=live-replica-server
REPLICATE_MASTER_SERVLET_HOST=172.31.19.178
REPLICATE_MASTER_EXCHANGE_NAME=bundesliga2016
REPLICATION_CHANNEL=bundesliga2016
SERVER_NAME=bundesliga2016
MONGODB_NAME=bundesliga2016-replica
EVENT_ID=9e6e8e93-94fe-46b4-8fa7-cca3d77bd50a
HTTP_SEC_EVENT=bundesliga2-2016
HTTP_SEC_EVENT_ID=cef66bf6-ef37-49ae-9f1d-cd595f41893f
SERVER_STARTUP_NOTIFY=steffen.tobias.wagner@sap.com
BUILD_COMPLETE_NOTIFY=steffen.tobias.wagner@sap.com
ADDITIONAL_JAVA_ARGS="-Dpersistentcompetitors.clear=false -XX:+UseG1GC -XX:MaxGCPauseMillis=500 -verbose:gc -XX:+PrintGCTimeStamps -XX:+GCHistory -XX:+PrintAdaptiveSizePolicy -XX:+PrintGCDetails -Xloggc:logs/gc.log -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=100M -Dorg.eclipse.jetty.LEVEL=OFF -Dorg.eclipse.jetty.util.log.class=org.eclipse.jetty.util.log.StdErrLog"
```

## danishleague2016

### URL
- Public ELB: https://danishleague2016.sapsailing.com
- Public Master WEB: https://danishleague2016-master.sapsailing.com
- Master Instance: `danish-master.league.sapsailing.com`

### Firing up replica's

```
INSTALL_FROM_RELEASE=build-201605131237
USE_ENVIRONMENT=live-replica-server
REPLICATE_MASTER_SERVLET_HOST=172.31.18.162
REPLICATE_MASTER_EXCHANGE_NAME=danishleague2016
REPLICATE_ON_START=com.sap.sailing.server.impl.RacingEventServiceImpl,com.sap.sse.security.impl.SecurityServiceImpl,com.sap.sse.filestorage.impl.FileStorageManagementServiceImpl,com.sap.sse.mail.impl.MailServiceImpl,com.sap.sailing.polars.impl.PolarDataServiceImpl
SERVER_NAME=danishleague2016
MONGODB_NAME=danishleague2016-replica
EVENT_ID=3e8e08e1-05e2-4140-832f-1f4cf0065032
SERVER_STARTUP_NOTIFY=steffen.tobias.wagner@sap.com
```

## dutchleague2016

### URL
- Public ELB: https://dutchleague2016.sapsailing.com
- Public Master WEB: https://durchleague2016-master.sapsailing.com
- Master Instance: `dutch-master.league.sapsailing.com`

### Firing up replica's

```
INSTALL_FROM_RELEASE=build-201605131237
USE_ENVIRONMENT=live-replica-server
REPLICATE_MASTER_SERVLET_HOST=172.31.23.95
REPLICATE_MASTER_EXCHANGE_NAME=dutchleague2016
REPLICATE_ON_START=com.sap.sailing.server.impl.RacingEventServiceImpl,com.sap.sse.security.impl.SecurityServiceImpl,com.sap.sse.filestorage.impl.FileStorageManagementServiceImpl,com.sap.sse.mail.impl.MailServiceImpl,com.sap.sailing.polars.impl.PolarDataServiceImpl
SERVER_NAME=dutchleague2016
MONGODB_NAME=dutchleague2016-replica
EVENT_ID=77999de1-e925-4e7e-b9be-f0b8e14616ae
SERVER_STARTUP_NOTIFY=steffen.tobias.wagner@sap.com
```

## norwegianleague2016

### URL
- Public ELB: https://norwegianleage2016.sapsailing.com
- Public Master WEB: https://norwegianleague2016-master.sapsailing.com
- Master Instance: `norwegian-master.league.sapsailing.com`

### Firing up replica's

```
INSTALL_FROM_RELEASE=build-201605131237
USE_ENVIRONMENT=live-replica-server
REPLICATE_MASTER_SERVLET_HOST=172.31.27.2
REPLICATE_MASTER_EXCHANGE_NAME=norwegianleague2016
REPLICATE_ON_START=com.sap.sailing.server.impl.RacingEventServiceImpl,com.sap.sse.security.impl.SecurityServiceImpl,com.sap.sse.filestorage.impl.FileStorageManagementServiceImpl,com.sap.sse.mail.impl.MailServiceImpl,com.sap.sailing.polars.impl.PolarDataServiceImpl
SERVER_NAME=norwegianleague2016
MONGODB_NAME=norwegianleague2016-replica
EVENT_ID=69924f98-c00f-4f33-8264-d00ecc46c270
SERVER_STARTUP_NOTIFY=steffen.tobias.wagner@sap.com
```

## swedishleague2016

### URL
- Public ELB: https://swedishleague2016.sapsailing.com
- Public Master WEB: https://swedishleague2016-master.sapsailing.com
- Master Instance: `swedish-master.league.sapsailing.com`

### Firing up replica's

```
INSTALL_FROM_RELEASE=build-201605131237
USE_ENVIRONMENT=live-replica-server
REPLICATE_MASTER_SERVLET_HOST=172.31.31.165
REPLICATE_MASTER_EXCHANGE_NAME=swedishleague2016
REPLICATE_ON_START=com.sap.sailing.server.impl.RacingEventServiceImpl,com.sap.sse.security.impl.SecurityServiceImpl,com.sap.sse.filestorage.impl.FileStorageManagementServiceImpl,com.sap.sse.mail.impl.MailServiceImpl,com.sap.sailing.polars.impl.PolarDataServiceImpl
SERVER_NAME=swedishleague2016
MONGODB_NAME=swedishleague2016-replica
EVENT_ID=1fa8905c-9449-419d-9986-c0a4a96632b0
SERVER_STARTUP_NOTIFY=steffen.tobias.wagner@sap.com
```

## finnishleague2016

### URL
- Public ELB: https://finnishleague2016.sapsailing.com
- Public Master WEB: https://finnishleague2016-master.sapsailing.com
- Master Instance: `finnish-master.league.sapsailing.com`

### Firing up replica's

```
INSTALL_FROM_RELEASE=build-201605131237
USE_ENVIRONMENT=live-replica-server
REPLICATE_MASTER_SERVLET_HOST=172.31.27.104
REPLICATE_MASTER_EXCHANGE_NAME=finnishleague2016
REPLICATE_ON_START=com.sap.sailing.server.impl.RacingEventServiceImpl,com.sap.sse.security.impl.SecurityServiceImpl,com.sap.sse.filestorage.impl.FileStorageManagementServiceImpl,com.sap.sse.mail.impl.MailServiceImpl,com.sap.sailing.polars.impl.PolarDataServiceImpl
SERVER_NAME=finnishleague2016
MONGODB_NAME=finnishleague2016-replica
EVENT_ID=83a2846d-7c2d-4af9-a40d-3f20ed077a91
SERVER_STARTUP_NOTIFY=steffen.tobias.wagner@sap.com
```