## Server Landscape

### Interconnect AWS regions

For being able to serve low latency connection to the american continent we run several replica server in front of an ELB in AWS region US-East (N.Virginia).

Our Master and eventual further replicas needed in EU region are deployed in AWS region EU-West (Ireland) for low latency connections towards RabbitMQ and MongoDB.

Two interconnect the two AWS regions and subnets, I established a site2site VPN tunnel between us-east-1c and eu-west-1c region. The global VPC routes are used to route the traffic for the private subnets (172.16.0.0 in EU and 172.30.0.0 in US-East) towards the according VPN gateway instance, which then tunnels it to the other region. The result is that both regions can communicate internally with each other, so we don't need to tunnel via reverse SSH tunnels or even more worse, open ports (like MongoDB) to the public.

See [here](http://aws.amazon.com/articles/0639686206802544) for the basic tunnel setup, which is established.

Simply make sure here, to use the according availibility zones to the instances between the regions can interact with each other. All required internal subnets are configured to the "SAP Sailing analytics" and the "DB/Messaging" Security Group in both regions. 

The VPN setup can be easily expanded to further regions like APJ.

### Load balancers and Route53

This setup requires two ELB's; one in each region. Put instances behind the according ELB in your region as usual.
The only difference here is, that Route53 entries differ a little bit from the usual standard. There are event subdomain entries (crw2016) for different geo-based locations set, which will handle and forward requests to the "best" load balancer which is set up in each region. See [here](https://wiki.sapsailing.com/wiki/info/landscape/amazon-ec2#amazon-ec2-for-sap-sailing-analytics_howto_using-latency-based-dns-across-regions) for the basic setup of latency-based or geo-based Route53 entries.

### Technical event meta information
```
MONGODB_NAME=crw2016
SERVER_NAME=CRW2016
REPLICATION_CHANNEL=crw2016
EVENT=56f62045-de57-4c5e-be6c-a2339d9c9ece
```

### Instances used

**EU-West Ireland**

- ELB Load Balancer
  - External DNS: `CRW2016-EU-554516968.eu-west-1.elb.amazonaws.com`

- Master 1 (Live)
  - Internal DNS: `ip-172-31-21-180.eu-west-1.compute.internal`
  - External DNS: `ec2-52-50-179-44.eu-west-1.compute.amazonaws.com`

**US-East N. Virginia**

- ELB Load Balancer
  - External DNS: `CRW2016-US-EAST-1076258956.us-east-1.elb.amazonaws.com`

- Replica 1
  - Internal DNS: `ip-172-30-2-176.ec2.internal`
  - External DNS: `ec2-52-90-229-141.compute-1.amazonaws.com`

### Starting up further masters

```
MONGODB_NAME=crw2016
REPLICATION_CHANNEL=crw2016
SERVER_NAME=CRW2016
USE_ENVIRONMENT=live-master-server
MEMORY=25600m
INSTALL_FROM_RELEASE=build-201604090936
SERVER_STARTUP_NOTIFY=steffen.tobias.wagner@sap.com
EVENT=56f62045-de57-4c5e-be6c-a2339d9c9ece
```

### Starting up further replicas

```
INSTALL_FROM_RELEASE=build-201604090936
USE_ENVIRONMENT=live-replica-server
REPLICATE_MASTER_SERVLET_HOST=172.31.21.180
REPLICATE_MASTER_EXCHANGE_NAME=crw2016
REPLICATE_ON_START=com.sap.sailing.server.impl.RacingEventServiceImpl,com.sap.sse.security.impl.SecurityServiceImpl,com.sap.sse.filestorage.impl.FileStorageManagementServiceImpl,com.sap.sse.mail.impl.MailServiceImpl,com.sap.sailing.polars.impl.PolarDataServiceImpl
SERVER_NAME=CRW2016
MONGODB_NAME=crw2016-replica
EVENT_ID=56f62045-de57-4c5e-be6c-a2339d9c9ece
SERVER_STARTUP_NOTIFY=steffen.tobias.wagner@sap.com
```