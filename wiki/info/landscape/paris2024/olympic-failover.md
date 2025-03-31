# Failover Scenarios for [Olympic Setup Paris 2024](https://wiki.sapsailing.com/wiki/info/landscape/paris2024/olympic-setup)

This page is meant to describe a couple of failure scenarios and appropiate mitigation approaches. In addition open questions are documented. It refers to the setup described in detail [here](https://wiki.sapsailing.com/wiki/info/landscape/paris2024/olympic-setup). It is work in progress and far from complete.

## Hardware failure on primary Lenovo P1 with the Sailing Analytics Master

### Scenario

The Lenovo P1 device on which the SAP Sailing Analytics Primary Master is running fails and is not available anymore. Reason could be a hardware failure on the CPU, a software deadlock, etc.

The local secondary master on the second P1 device is still available, also the cloud replicas are still available. New data will arrive on the secondary master through TracAPI and through the WindBot connection. The secondary master writes its wind data to the ``security_service`` replica set.

The mongodb replicaset member running on the primary P1 may or may not be available anymore, but at this moment this is not too relevant because in this scenario we assume that the primary master process is not working anymore anyhow.

### Mitigation

The second P1 needs to switch role to be the ("primary") master. Primarily, the outbound replication channel has to be the cloud rabbit in eu-west-3, and the reverse port forward from ``paris-ssh.sapsailing.com:8888`` to ``sap-p1-1:8888`` needs to be changed so it points to ``sap-p1-2:8888``. This requires the port forward from ``paris-ssh.sapsailing.com:8888`` to ``sap-p1-1:8888`` to be released, either by ``sap-p1-1`` having died entirely, or by explicitly switching this port forward off.

```
  sap-p1-1:
    tunnels-no-master      # this will release the -R 8888 port forward
  sap-p1-2:
    tunnels-master         # this will make the secondary master write to the cloud RabbitMQ
                           # to feed all cloud replicas and will map the reverse port forward
                           # from paris-ssh.sapsailing.com:8888 to sap-p1-2:8888
```

SwissTiming needs to be informed about the switch immediately and can instantly connect to the "B" instance (secondary master).

The ``sap-p1-1`` master can then be re-started, either by only re-launching the Java process:

```
  cd /home/sailing/servers/master
  ./stop
  ./start
```

When the primary master has recovered, we could in theory switch back. We may, however, as well decide that no additional interruption shall be risked by another switching process and stay with ``sap-p1-2`` until the end of the day. To switch back, do this:

```
  sap-p1-2:
    tunnels                # this will make the secondary master write to the local RabbitMQ,
                           # stopping to feed all cloud replicas, and stopping the reverse port forward
                           # from paris-ssh.sapsailing.com:8888 to sap-p1-2:8888
  sap-p1-1:
    tunnels                # this will establish the -R 8888 port forward from paris-ssh.sapsailing.com:8888
                           # again to the primary master on sap-p1-1
```

### Results and procedure tested in Marseille 2023

Both laptops, ``sap-p1-1`` and ``sap-p1-2`` are both running their primary and secondary master processes, respectively. I tested a primary master failure by running ``tunnels-no-master`` on ``sap-p1-1``, and ``tunnels-master`` on ``sap-p1-2``. Then I made a change on the secondary master (``sap-p1-2``) and found it replicated on the cloud replicas. I then took back the change, and that, too, replicated nicely to the cloud. Then I reverted the tunnels to their original form by invoking ``tunnels`` first on ``sap-p1-2`` to release the reverse port forward from ``8888``, then on ``sap-p1-1``. Making changes afterwards on the secondary master no longer reflected in the cloud, as expected.

## Hardware failure on secondary Lenovo P1 with the Sailing Analytics Replica

### Scenario

The secondary Lenovo P1 experiences an unrecoverable hardware failure. The primary P1 is still available, new data is safely stored in the database.

Cloud users are not affected. SwissTiming should be informed about the lack of availability of the "B" system.

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
