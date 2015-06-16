# Kieler Woche 2015

## Server Landscape

We run four servers for the event:
- on EC2: **Master**, **Replika 1** and **2**,
- locally: **Cube-Server**

For fast and available switching. The DB they use is called `KW2015`, and so is the replication channel. The git branch we use to merge the features we want to deploy to the event servers is called `XXX` and the git tag used to build a release for it is called `XXX-release`. See http://hudson.sapsailing.com/job/SAPSailingAnalytics-XXX/ and http://hudson.sapsailing.com/job/SAPSailingAnalytics-XXX-release/, respectively, for their Hudson jobs.

### Master Server
- Admin Console at http://ec2-54-154-151-10.eu-west-1.compute.amazonaws.com:8888/gwt/AdminConsole.html
- External IP: 54.154.151.10
- Internal IP: 172.31.24.187

### Replika 1 server
- Admin Console at http://ec2-54-72-226-124.eu-west-1.compute.amazonaws.com:8888/gwt/AdminConsole.html
- External IP: 54.72.226.124
- Internal IP: 172.31.20.11

### Replika 2 server
- Admin Console at http://ec2-54-154-175-180.eu-west-1.compute.amazonaws.com:8888/gwt/AdminConsole.html
- External IP: 54.154.175.180
- Internal IP: 172.31.20.12

## TracTrac URL
- http://xxx.traclive.dk/events/event_xxx/jsonservice.php

## Replication
- RabbitMQ server for replication: 52.16.112.6 (Web: http://52.16.112.6:15672/)
- Replication Exchange Name: KW2015

## Other links
- Regatta Overview: http://kielerwoche2015ro.sapsailing.com
- Manage2Sail Event URL: http://manage2sail.com/api/public/links/event/ID?accesstoken=TOKEN&mediaType=json

## Local network setup
basically see [[Set up local network with replication server|wiki/event-network-with-replica]] operating with a Meraki and a local replica server using 1 physical WAN interface on the Meraki. Meraki network config for this event can be found under [[Meraki Dashboard|https://n142.meraki.com/KiWo-TraWo/n/Rzu9tdoc]]

- Accessing replica server from `sailing-demo.sapsailing.com`via ssh or http from `ADMIN` network