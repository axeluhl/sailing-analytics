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
The only difference here is, that Route53 entries differ a little bit from the usual standard. There are event subdomain entries (crw2016) for different geo-based locations set, which will handle and forward requests to the "best" load balancer which is set up in each region. See [here](https://wiki.sapsailing.com/wiki/amazon-ec2#amazon-ec2-for-sap-sailing-analytics_howto_using-latency-based-dns-across-regions) for the basic setup of latency-based or geo-based Route53 entries.

### Instances used

**EU-West Ireland**

- Master
  - Internal IP:
  - External IP:

**US-East N. Virginia**

- Replica
  - Internal IP:
  - External IP: