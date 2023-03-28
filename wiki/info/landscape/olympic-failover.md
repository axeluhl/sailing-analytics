# Failover Scenarios for [Olympic Setup](https://wiki.sapsailing.com/wiki/info/landscape/olympic-setup)

This page is meant to describe a couple of failure scenarios and appropiate mitigation approaches. In addition open questions are documented. It refers to the setup described in detail [here](https://wiki.sapsailing.com/wiki/info/landscape/olympic-setup). It is work in progress and far from complete.

## Hardware failure on primary Lenovo P1 with the Sailing Analytics Master

### Scenario

The Lenovo P1 device on which the SAP Sailing Analytics Master / Primary is running fails and is not available anymore. Reason could be a hardware failure on the CPU e.g.

The local replica on the second P1 device is still available, also the cloud replicas are still available. Data from the past is still available to users and on-site consumers. However no new data will be stored.

The mongodb replicaset member running on the primary P1 will also be not available anymore.

### Mitigation

The second P1 needs to switch role to be a master. This means a new SAP Sailing Analytics Master process has to be started from scratch. The outbound replication channel has to be the cloud rabbit in ap-northeast-1.

Swisstiming needs to be informed once the master is ready. 

Cloud replicas need to be reconfigured to the new channel that the master uses as outbound.

SSH tunnels won't need to change.

The local replica has to be safely shut down, on-site users might experience some change, depending on how we decide with local routing.

### Open questions

How exactly has the switch to happen? 

Do we re-use the port? 

What are pro's and con's? 

I would like to see a somewhat detailed run-book.

### Results and procedure tested in Medemblik 2021

Check master.conf in ``/home/sailing/server/master`` on sap-p1-2 for the desired build and correct setting of the new variable ``INSTALL_FROM_SCP_USER_AT_HOST_AND_PORT``. There is a tunnel listening on 22222 which forwards the traffic through tokyo-ssh to sapsailing.com:22.
So a valid entry would be:
```
INSTALL_FROM_RELEASE=build-202106012325
INSTALL_FROM_SCP_USER_AT_HOST_AND_PORT="trac@localhost:22222"
```

Now execute, this will download and extract the build:
```
cd /home/sailing/servers/master; rm env.sh; cat master.conf | ./refreshInstance.sh auto-install-from-stdin
```

Now we stop the replica, make sure you are user ``sailing``:
```
/home/sailing/servers/replica/stop
```

Wait for process to be stopped/killed.

Start the correct tunnels script by executing:
```
sudo /usr/local/bin/tunnels-master
```

Start the master, make sure you are user ``sailing``!
```
/home/sailing/servers/master/start
```

Check sailing log:
```
tail -f /home/sailing/servers/master/logs/sailing0.log.0
```

## Hardware failure on secondary Lenovo P1 with the Sailing Analytics Replica

### Scenario

The secondary Lenovo P1 experience an unrecoverable hardware failure. The primary P1 is still available, new data is safely stored in the database.

Cloud users are not able to see new data, as the replication channel is interrupted.

For on-site/local users this depends on the decision which URL/IP they are provided with. If we use the local replica to serve them then in this scenario their screens will turn dark.

The mongodb replicaset member will not be available anymore.

### Mitigation

Local/on-site users need to have priority. If they were served by the secondary P1 before they need to switch to the primary P1. 

The outbound replication channel on the primary P1 need to switch to the rabbit in ap-northeast-1. The cloud replicas need to be reconfigured to use that channel.

SSH tunnels won't need to change.

### Open questions

How do local/on-site users use the Sailing Analytics? Will they simply be served from tokyo2020.sapsailing.com? A couple of decisions depend on this question. 

What exactly needs to be done where to change the replication and be sure that it will work without data loss? I would suggest a test of at least one of the described scenarios in Medemblik and create a runbook.

## Internet failure on Enoshima site

### Scenario

Internet connectivity is not given anymore at Enoshima On-site.

### Open questions

How will local/on-site users be connected to the local P1s, assuming that the LAN is still working? 

Would we try to provide connectivity through mobile hotspots, as auto SSH should reliably start working again once it reaches the target IPs? Shall we leave this issue to Swisstiming/Organizers and stick to the local connections to the Sailing Analytics?

## TracTrac in the Cloud?

### Scenario

On-site Internet goes down; does TracTrac have a full-fledged server running in the cloud that we could connect to from the cloud to at least keep serving the RHBs?

### Open questions

How can the MongoDB in the cloud be re-configured dynamically to become primary even though it may have been started with priority 0?
