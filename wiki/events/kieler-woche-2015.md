# Kieler Woche 2015

## Server Landscape

We run four servers for the event:
- on EC2: **Master 1**, **Master 2**, **Master 3**, **Replica 1** and **Replica 2** with an **ELB** in front,
- ELB canonical name is KW2015-553831863.eu-west-1.elb.amazonaws.com
- locally: **Cube-Server**

for fast and available switching. The DB they use is called `KW2015`, and so is the replication channel. The git branch we use to merge the features we want to deploy to the event servers is called `kielerwoche2015` and the git tag used to build a release for it is called `kielerwoche2015-release`. See http://hudson.sapsailing.com/job/SAPSailingAnalytics-kielerwoche2015/ and http://hudson.sapsailing.com/job/SAPSailingAnalytics-kielerwoche2015-release/, respectively, for their Hudson jobs.

### Master 1 Server (32GB, STOPPED)

### Master 2 Server (32GB, STOPPED)

### Master 3 Server (64GB)
- Admin Console at http://ec2-54-171-148-10.eu-west-1.compute.amazonaws.com:8888/gwt/AdminConsole.html
- External IP: 54.171.148.10
- Internal IP: 172.31.26.119

### Master 4 Server (60GB)
- Admin Console at http://ec2-54-154-151-158.eu-west-1.compute.amazonaws.com:8888/gwt/AdminConsole.html
- External IP: 54.154.151.158
- Internal IP: 172.31.18.24

### "Current" Master
Whichever master is current, we map the main Apache URL http://kielerwoche2015-master.sapsailing.com to it. This is also the URL to use for Tablet and Smartphone configurations.

### Replica 1 server (STOPPED)

### Replica 2 server (STOPPED)

### Replica 3 Server (60GB)
- Admin Console at http://ec2-54-171-99-97.eu-west-1.compute.amazonaws.com:8888/gwt/AdminConsole.html
- External IP: 54.171.99.97
- Internal IP: 172.31.19.218

### Replica 4 Server (60GB)
- Admin Console at http://ec2-54-154-142-170.eu-west-1.compute.amazonaws.com:8888/gwt/AdminConsole.html
- External IP: 54.154.142.170
- Internal IP: 172.31.18.23

## TracTrac URL
- http://event.tractrac.com/events/event_20150616_KielerWoch/jsonservice.php for the Olympic Week
- http://event.tractrac.com/events/event_20150625_KielerWoch/jsonservice.php for the International Week
- http://club.tractrac.com/events/event_20150625_KielerWoch/jsonservice.php for the Silbernes Band

## Replication
- RabbitMQ server for replication: rabbit.sapsailing.com (Web: http://rabbit.sapsailing.com:15672/) or internal host name rabbit.internal.sapsailing.com)
- Replication Exchange Name: KW2015

## Switching from one Master to Another
 - switch Cube Replica's Apache server to direct http://kielerwoche2015.sapsailing.com to the cloud by editing its `/etc/apache2/sites-enabled/001-events.conf`, then as user `root` running `service apache2 reload`.
 - upgrade the new master to the release you want, using the `refreshInstance.sh` script
 - prepare switching in sapsailing.com:/etc/httpd/conf.d/001-events.conf so that in particular http://kielerwoche2015-master.sapsailing.com points to the new master's IP; save the file but don't do `service httpd reload` as yet
 - remove the replicas from the ELB
 - cleanly stop replication for all replicas currently active on the master you want to take offline. (Exception: if your current master is really broken and cannot serve requests anymore, in order to keep up at least some form of availability you may leave the replicas running until the new master is up.)
 - stop replication on the Cube Replica
 - start the new master
 - track all the races that need to be tracked
 - add the new master to the ELB and remove the old master from the ELB
 - immediately afterwards do `service httpd reload` as user `root` on sapsailing.com to activate the new master
 - validate that you haven't lost any RCApp events, ideally by comparing the RegattaOverview.html page for the old and the new master; in case you lost events, reload the race logs for those leaderboards in the new master's AdminConsole.
 - upgrade the replicas (including the Cube Replica) to the new master if the new master runs a different release than the old master, using `refreshInstance.sh`
 - adjust the replicas' (including the Cube Replica) `env.sh` to point to the new master with their `REPLICATE_MASTER_SERVLET_HOST` setting
 - restart the Java server instance on the replicas (including the Cube Replica), using the usual `./stop; ./start` sequence. This will automatically re-start the replication
 - when the replicas have finished their initial load (can be verified by looking at the RabbitMQ queues at http://rabbit.sapsailing.com:15672/#/queues which should no longer have any "initialLoad" queues), add them again to the load balancer
 - in the Cube server's Apache configuration let http://kielerwoche2015.sapsailing.com point again to the local replica by editing `/etc/apache2/sites-enabled/001-events.conf`, then as user `root` run `service httpd reload`
 - shut down the other master server to avoid confusion with database writes

## Other links
- Regatta Overview: http://kielerwoche2015ro.sapsailing.com (currently not yet established)
- Manage2Sail Event URL: http://manage2sail.com/api/public/links/event/d56f7fea-0ca1-4972-83a4-d71d79f26b93?accesstoken=bDAv8CwsTM94ujZ&mediaType=json

## Local network setup
basically see [[Set up local network with replication server|wiki/howto/eventmanagers/event-network-with-replica]] operating with a Meraki and a local replica server using 1 physical WAN interface on the Meraki. Meraki network config for this event can be found under [[Meraki Dashboard|https://n142.meraki.com/KiWo-TraWo/n/Rzu9tdoc]]

- Accessing replica server from `sailing-demo.sapsailing.com` via ssh or http from `ADMIN` network
