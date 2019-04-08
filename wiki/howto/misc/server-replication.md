# Replicating an SAP Sailing Analytics Server

[[_TOC_]]

### Requirements
Scaling to many (>100) concurrent users following a live sailing race requires providing more than one OSGi server instance. To an extent this is cause by an aspect of our architecture that decides how clients access server data. We use the Google Web Toolkit's (GWT) Remote Procedure Call (RPC) to let the clients request data from the server. These are handled as HTTP POST requests with a servlet composing the response message. This approach makes caching servlet responses to similar requests by a generic HTTP caching / proxy infrastructure difficult.

Using more than one server instance is additionally motivated by the ManyPlayers client architecture. ManyPlayers wants to use our code base but run it on their premises and only be assured that once their instance connects to our "master" instance then all relevant state will be replicated to their instance.

### Server State
The server's state at the current time roughly consists of the following:

* list of races with their tracking data loaded by the server 
* a race's state consists of 
  * RaceDefinition object with name, boat class and event association 
  * Course object with Waypoints and ControlPoints 
* a tracked race's state consisting of 
  * wind tracks 
  * raw boat position tracks 
  * mark position tracks 
  * mark passings 
  * race tracking start/finish times 
* list of leaderboard groups with their names and descriptions 
* list of leaderboards and their association with leaderboard groups 
  * leaderboard names, columns and the columns' associated tracked races, if any 
  * discarding rules 
  * competitor renamings 
  * explicit score corrections 
  * disqualification / "max-points" reasons (DSQ, OCS, DNS, DNF, DND, ...) 

### Reliability
When a server is configured to be a replica of another "master" server, all updates to the master server must be replicated as quickly as possible to the replica until the replica permanently disconnects from the master server. If the connection breaks temporarily and is restored at a later point in time, all updates that occurred since the connection broke need to be updated to the client.

### Connectivity
Replicated instances may be able to only connect to the master through an HTTP connection. In this case, the master server needs to push updates through an HTTP connection reliably.

### Single Master, Multiple Replicas
For Kiel Week 2012 we want to support a single master server with multiple replicated copies, so-called "replica instances." The replicas are configured for their master node. This configuration doesn't change over the replica's life time. If the master dies, it need to be started again. Replicas then need to recover and re-sync once the new master instance is alive again.

### Concurrency Challenges
Different types of updates to the master server can happen concurrently. Tracking data for multiple races is being received in multiple threads which can execute truly concurrently on a multi-core architecture. Wind data may be received in parallel to receiving other tracking data. Leaderboard and LeaderboardGroup configurations may happen concurrently to all other types of changes. New races may start to get tracked concurrently to the many other server operations. Do we need to ensure that all these events whose effects need to be replicated have to be serialized in an order that is kept the same for all replica to ensure equal resulting states in all replica? What would serializing all these many concurrent events mean to the server's internal concurrency which is important to improve performance?

### Thoughts on Implementation
HTTP push protocols are implemented, e.g., by the Atmosphere framework and by our own, proprietary AbstractHttpPostServlet in the com.sap.sailing.server package. They both use a heartbeat mechanism to re-establish the HTTP connection after it got disconnected. However, both currently don't handle reliable data transmission. If a client is disconnected and re-connects later, it may have missed a number or messages sent while it was disconnected. This would need to be overcome in order to achieve the reliabiliy required for our replication scenario.

### Initial Load
When a replica is first set up and connected to a master server, the master server's current state at some consistent snapshot needs to be replicated to the replica and from then on be kept in sync. How can we produce such a consistent snapshot while understanding which subsequent change events to broadcast to which client?

One approach we're currently testing is using Java Serialization to copy an initial "image" of one server to the next. There are several tricky issues with this approach:

* listeners / observers: several classes offer listeners to be attached; for example, a DynamicTrackedRaceImpl object supports RaceChangeListener objects to attach to it. Similarly, Course objects support CourseListeners to observe the course for changes. Currently, our strategy is to serialize the listener graph together with the object graph they observe by declaring the listener interfaces as "extends Serializable." This unfortunately requires all other listener implementations, even the inner anonymous classes used by test cases, to be serializable too. On the other hand this avoids NotSerializableExceptions. 
* non-serializable JDK classes: For example, TrackedRaceImpl uses java.util.Timer to delay cache invalidation. In this fortunate case, making the field transient has no negative effects because the next action that would need the cache to be invalidated will set the timer correctly, so no explicit initialization in readObject() is required. There may be other cases, though, where a readObject() implementation is needed to establish some objects in transient fields after deserialization. 
* synchronization: serializing a data structure while it is being modified is a bad idea. The java.util.* collection classes are not thread safe. In those cases where concurrent modifications have to be expected, a writeObject() definition is required that synchronizes the serialization with modifying operations. For example, serializing a TrackedRace object together with its GPSFixTracks and WindTracks needs to synchronize with inbound GPSFix / Wind updates. 

### Incremental Updates
Many updates to the server's state are small compared to the overall state. For example, adding a GPS fix to a boat's track is a very small change; so is the modification of a leaderboard name or a leaderboard group's description. Such events need to be replicated incrementally. A message queuing system with persistent message queues may be an implementation option that can guarantee delivery also through HTTP connections.

### Implications for the MongoDB Store
A replica may or may not use the same MongoDB store that the master uses. If it uses the same store, replication events must not update the database. This then also holds for the initial load procedure. On the other hand, if the replica uses its own database instance, even the initial load has to update all structures that require persistent storage into the local database.

Using MongoDB's built-in replication requires a robust TCP connection between the MongoDB replicas. This is hard to establish in case only HTTP connections are possible between the replica and the master.

### Use of a Message Queuing System
An MQ system such as ActiveMQ could serve as basis for the implementation of the replication mechanism. We'd have to wrap all commands in messages. To be clarified: what happens when a client subscribes to a topic? Will it receive messages sent to that topic only after it connected? Or will it also receive older messages that have been queued persistently?

### Design
Changes applied to a server that are relevant for replication have to be applied through a single entry point at RacingEventService, using the command pattern provided by the RacingEventServiceOperation interface. This will allow future extensions towards the use of an Operation Transformation (OT) algorithm in case operations can be performed not only on a single master but throughout the replicas. In addition to the course-grained leaderboard / group creations and editing operations, all tracking data recordings need to pass through this interface in order to be replicated properly.

### RacingEventServiceOperations and RacingEventService
Most of the state changes relevant for replication are triggered through the RacingEventService interface. A few changes, such as the recording of GPS and wind data received, are currently immediately updated into internal data structures, such as a DynamicTrackedRace and the track objects it manages, or the Course object owned by a RaceDefinition object. It would be possible to restructure things in such a way that also the tracking data are funneled through the RacingEventService interface.

A RacingEventServiceOperation describes serializable operations that can be applied to a RacingEventService and which can be transmitted to another server instance (a replica) to be applied to another RacingEventService, eventually leading to equal states of the RacingEventService instances.

Question: Where and how do we ensure that all modifications to a RacingEventService that are relevant for replication are actually turned into RacingEventServiceOperations and then reliably serialized to all replicas?

We could let RacingEventServiceImpl manage the creation of the operation objects whenever a changing method is called. This would require distinguishing between an "external" and an "internal" API where the external API methods create the operations which, when applied, use the internal API methods to perform the actual updates.

Or we restrict the externally visible API of RacingEventService to applying RacingEventServiceOperations. Then, all modifications that are relevant for replication have to go through a RacingEventServiceOperation. This is very hard to ensure because internally all APIs, such as those of Leaderboard and LeaderboardGroup and everything are easily accessible.

Answer: We create the operations already in SailingServiceImpl and only call apply on the RacingEventService whose implementation can then notify an optional replicator of the operations that were executed. At this time it would make sense to remove the fine-grained operations which are now only used by the operation implementations from the RacingEventService interface at least. The operation implementations can cast the RacingEventService object to RacingEventServiceImpl and then access the operations again.

### Parallel Processing of Operations
Many types of operations can potentially be processed in parallel. For example, GPS fixes for different tracked races can be inserted into their respective tracks in parallel. So can edit operations for different leaderboards. A thread pool can be used to process the operations. However, each thread in this pool may need to obtain the monitors for the objects it will modify, and the scheduler that sequentially passes the operations to the thread pool for processing needs to wait for the processing thread's acknowledgement that all monitors required have been obtained before triggering the processing of the next operation. The general problem reminds of the versioning graph as created, e.g., by a git repository. Also, Leslie Lamport's vector clocks come to mind.

Some operations are more tricky to parallelize, particularly if we have to assume race conditions during execution. For example, if an operation creates a leaderboard and a follow-up operation adds a column to the leaderboard just created, the leaderboard creation must be complete before the column can be added. Parallelizing such operations is generally dangerous.

Fortunately, the dangerous-to-parallelize operations seem to be the ones that occur with low frequency (leaderboard creation, score corrections, leaderboard column configuration) and have relaxed requirements regarding the lag with which they have to be replicated. We can therefore partition the data and transactions to be replicated into two categories

### Sequential operations
These encompass leaderboard and leaderboard group creation and manipulation, including score corrections. In a replica these operations must be executed in the same sequence they were executed in on the master. It probably encompasses the creation of a tracked race and its course, we well as the updates to a race course with waypoint additions and mark passings because mark passing events relate to the waypoints. 
Paralllel operations

These encompass the receiving of raw sensor data. The order in which this data is re-played doesn't matter. The data is time-stamped, and its history is not rewritten (as maybe the case for the mark passings or for the race tracking start/finish times). Applying these operations to a replica is idempotent, so serializing an initial state doesn't need to be synchronized with the receiving of such data. Duplicate application of these operations to a server doesn't do any harm.


### Replication in SAP Cube
This section will describe, how a working local replica can be set up at the SAP Cube.
The setup at the Travemünder Woche 2014 was done by Steffen Wagner as it follows:

LTE router --> gigabit switch --> cube computers (dhcp) && local replica machine (with e.g. Ubuntu 14.04 LTS & a version of the analytics suite installed)

Steps to get it working:
* The LTE router ( _192.168.1.1_ ) should have DHCP and NS activated. Dhcp was within range _XXX.XXX.1.2 - XXX.XXX.1.49_ and our local replica machine had a static ip address of _XXX.XXX.1.50_. Please make sure, that the ip address is outside of the dhcp range. For a better setup we should think of using the local replica server for dhcp and ns services.
* Setup networking (to your needs) on the local replica machine as follows:

 **/etc/networking/interfaces**
 <pre>
 auto eth0
 iface eth0 inet static
	address 192.168.1.50
	netmask 255.255.255.0
	gateway 192.168.1.1
 </pre>
 **/etc/hosts**
 <pre>
 127.0.0.1	localhost
 127.0.0.1	tw2014 tw2014.sapsailing.com
 192.168.1.50	tw2014 tw2014.sapsailing.com
 </pre>
* For rewriting the URLs and Ports to the fitting spectator URL when entering the URL we need an Apache installed (apt-get install apache2) and set up like on our sapsailing.com server. Please check 000-macros.conf, 001-events.conf and 000-main.conf and set it up as you need for the event. They need to be in the config dir of apache (/etc/apache2/mods-enabled and /etc/apache2/sites-enabled)
* For better remote administration of the local replica, set up and autossh tunnel to trac@sapsailing.com and put it into rc.local for autostarting, e.g. as the following:
<pre>
autossh -M 20009 -f -N -L 1337:127.0.0.1:22 -i SSHKEY trac@sapsailing.com
</pre>
* Set up automatic replication in env.sh of the analystics server and start the server (not with root!) with startscript ~/servers/server/start
* On all Cube computers add the follwing batch file to the windows task scheduling for a check every minute. The script switches hosts to local replica if available. Before the script can work, you have to edit host file manully and add the required hostnames: e.g. <pre>192.168.1.50	tw2014.sapsailing.com</pre> You also need to replace "find" and "replace" to the required ip addresses. Please note that all this would not be needed, if we would use the replica server also for dhcp and ns, so it makes really sense to set this up.
<pre>
@echo off
@setlocal enableextensions enabledelayedexpansion
REM Set a variable for the Windows hosts file location
set "hostpath=%systemroot%\system32\drivers\etc"
set "hostfile=hosts"
REM Make the hosts file writable
attrib -r -s -h "%hostpath%\%hostfile%"
ping -n 1 -w 1000 -l 2000 192.168.1.50
REM if local replica is available
IF %ERRORLEVEL% == 0 (
   REM set the string you wish to find
   set find=54.229.94.254
   REM set the string you wish to replace with
   set replace=192.168.1.50
REM if local replica is not available
) ELSE (
   REM set ring you wish to find
   set find=192.168.1.50
   REM set the string you wish to replace with
   set replace=54.229.94.254
)	
for /f "delims=" %%a in ('type "%hostpath%\%hostfile%"') do (
set "string=%%a"
set "string=!string:%find%=%replace%!"
>> "newfile.txt" echo !string!
)
move /y "newfile.txt" "%hostpath%\%hostfile%"
REM Make the hosts file un-writable - not necessary.
REM attrib +r "%hostpath%\%hostfile%"
echo hostfile done.
</pre>
* If server should be shut down, then first stop replication manually in the backend and then call ~/servers/server/stop. If everything works fine, then the Cube computers should recognize, that the replica machine is not available anymore and then switch to the real sapsailing host.