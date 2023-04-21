# Thoughts on Landscape Configuration for Paris 2024 / Marseille

As a baseline we'll use the [Olympic Setup](/wiki/info/landscape/olympic-setup). The major change, though, would be that instead of running a local on-site master and a local on-site replica we would run two master instances locally on site where one is the "shadow" and the other one is the "production" master.

## Master and Shadow Master

We will use one laptop as production master, the other as "shadow master." The reason for not using a master and a local replica is that if the local master fails, re-starting later in the event can cause significant delays until all races have loaded and replicated again.

Both laptops shall run their local RabbitMQ instance. Each of the two master processes can optionally write into its local RabbitMQ through an SSH tunnel which may instead redirect to the cloud-based RabbitMQ for an active Internet/Cloud connection.

This will require to set up two MongoDB databases (not separate processes, just different DB names).

Note: The shadow master must have at least one registered replica because otherwise it would not send any operations into the RabbitMQ replication channel. This can be a challenge for a shadow master that has never seen any replica. We could, for example, simulate a replica registration when the shadow master is still basically empty, using, e.g., a CURL request and then ignoring and later deleting the initial load queue on the local RabbitMQ.

Furthermore, the shadow master must not send into the production RabbitMQ replication channel that is used by the production master instance while it is not in production itself, because it would duplicate the operations sent. Instead, the shadow master shall use a local RabbitMQ instance to which an SSH tunnel forwards.

## Switching

### Production Master Failure

Situation: production master fails, e.g., because of a Java VM crash or a deadlock or user issues such as killing the wrong process...

Approach: Switch to previous shadow master, re-configuring all SSH tunnels accordingly; this includes the 8888 reverse forward from the cloud to the local on-site master, as well as the RabbitMQ forward which needs to switch from the local RabbitMQ running on the shadow master's host to the cloud-based RabbitMQ. Clients such as SwissTiming clients need to switch to the shadow master. To remedy gaps in replication due to the SSH tunnel switch we may want to circulate the replica instances, rolling over to a new set of replicas that fetch a new initial load.

### Internet Failure

As in the Tokyo 2020 scenario; in particular, the local security service must be started which will work off a regularly updated local MongoDB copy of the cloud-based security-service.sapsailing.com; this also requires to adjust /etc/hosts and the tunnels accordingly.

## SSH Tunnels

TBD; baseline is again the Tokyo 2020 set-up.

## Test Plan for Test Event Marseille July 2023

### Test Internet Failure

We shall emulate the lack of a working Internet connection and practice and test the procedures for switching to a local security-service.sapsailing.com installation as well as a local RabbitMQ standing in for the RabbitMQ deployed in the cloud.

### Test Primary Master Hardware Failure

This will require switching entirely to the shadow master. Depending on the state of the reverse port forward of the 8888 HTTP port from the cloud we may or may not have to try to terminate a hanging connection in order to be able to establish a new reverse port forward pointing from the cloud to the shadow master. The shadow master also then needs to use the cloud-based RabbitMQ instead of its local one. As a fine-tuning, we can practice the rolling re-sync of all cloud replicas which will likely have missed operations in the meantime.

### Test Primary Master Java VM Failure

This can be caused by a deadlock, VM crash, Full GC phase, massive performance degradation or other faulty behavior. We then need to actively close the reverse SSH port forward from the cloud to the production master's 8888 HTTP port, as a precaution switch the RabbitMQ tunnel from the cloud-based to the local RabbitMQ instance so that in case the production master "wakes up" again, e.g., after a Full GC, it does not start to interfere with the now active shadow master on the RabbitMQ fan-out exchange. On the shadow master we need to re-configure the SSH tunnels, particularly to target the cloud-based RabbitMQ and have the reverse port forward on port 8888 target the shadow master on site now.

### Test Primary Mater Failures with no Internet Connection

Combine the above scenarios: a failing production master (hardware or VM-only) will require different tunnel re-configurations, especially regarding the then local security-service.sapsailing.com environment which may need to move to the shadow laptop.
