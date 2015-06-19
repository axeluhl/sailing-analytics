# Kieler Woche 2015

## Server Landscape

We run four servers for the event:
- on EC2: **Master 1**, **Master 2**, **Replica 1** and **Replica 2** with an **ELB** in front,
- ELB canonical name is KW2015-553831863.eu-west-1.elb.amazonaws.com
- locally: **Cube-Server**

for fast and available switching. The DB they use is called `KW2015`, and so is the replication channel. The git branch we use to merge the features we want to deploy to the event servers is called `kielerwoche2015` and the git tag used to build a release for it is called `kielerwoche2015-release`. See http://hudson.sapsailing.com/job/SAPSailingAnalytics-kielerwoche2015/ and http://hudson.sapsailing.com/job/SAPSailingAnalytics-kielerwoche2015-release/, respectively, for their Hudson jobs.

### Master 1 Server
- Admin Console at http://ec2-54-154-151-10.eu-west-1.compute.amazonaws.com:8888/gwt/AdminConsole.html
- External IP: 54.154.151.10
- Internal IP: 172.31.24.187

### Master 2 Server
- Admin Console at http://ec2-54-76-169-213.eu-west-1.compute.amazonaws.com:8888/gwt/AdminConsole.html
- External IP: 54.76.169.213
- Internal IP: 172.31.30.73

### "Current" Master
Whichever master is current, we map the main Apache URL http://kielerwoche2015-master.sapsailing.com to it. This is also the URL to use for Tablet and Smartphone configurations.

### Replica 1 server
- Admin Console at http://ec2-54-72-226-124.eu-west-1.compute.amazonaws.com:8888/gwt/AdminConsole.html
- External IP: 54.72.226.124
- Internal IP: 172.31.20.11

### Replica 2 server
- Admin Console at http://ec2-54-154-175-180.eu-west-1.compute.amazonaws.com:8888/gwt/AdminConsole.html
- External IP: 54.154.175.180
- Internal IP: 172.31.20.12

## TracTrac URL
- http://event.tractrac.com/events/event_20150616_KielerWoch/jsonservice.php for the Olympic Week

## Replication
- RabbitMQ server for replication: 52.16.112.6 (Web: http://52.16.112.6:15672/)
- Replication Exchange Name: KW2015

## Other links
- Regatta Overview: http://kielerwoche2015ro.sapsailing.com (currently not yet established)
- Manage2Sail Event URL: http://manage2sail.com/api/public/links/event/d56f7fea-0ca1-4972-83a4-d71d79f26b93?accesstoken=bDAv8CwsTM94ujZ&mediaType=json

## Local network setup
basically see [[Set up local network with replication server|wiki/event-network-with-replica]] operating with a Meraki and a local replica server using 1 physical WAN interface on the Meraki. Meraki network config for this event can be found under [[Meraki Dashboard|https://n142.meraki.com/KiWo-TraWo/n/Rzu9tdoc]]

- Accessing replica server from `sailing-demo.sapsailing.com` via ssh or http from `ADMIN` network