# Cloud Orchestration - Project Plan

[[_TOC_]]

The cloud landscape used to run the SAP Sailing Analytics as well as its development infrastructure has grown increasingly comprehensive and elaborate over the last few years. It has largely been configured through manual efforts on behalf of event and infrastructure operators and administrators.

With a growing number of events---be it through sailing leagues or championship events in one or more classes---and with a plan to scale the SAP Sailing Analytics to a broader audience through the use of the SAP Race Manager and the SAP Sail InSight apps, more automation is required for the cloud infrastructure.

We believe that a central *orchestrator* approach should be used to solve this challenge. Such an orchestrator, a bit like outlined in the figure below, will accept requests immediately from authenticated and authorized users through a Web interface, as well as from processes running on other servers in the cloud, through REST APIs on behalf of authenticated and authorized users.

![](https://wiki.sapsailing.com/wiki/images/orchestration/architecture.png)

## Orchestrator Architecture

In order to authenticate and authorize user requests the orchestrator will benefit from a powerful security infrastructure. The *Shiro* framework that is being used by the SAP Sailing Analytics has proven to be sufficiently configurable, powerful and extensible to support all our needs. It seems desirable to share the security service between the orchestrator and the application servers in the landscape. This will allow us to extend landscape-related permissions to users that can already be authenticated by the application and which can then make requests to the orchestrator, such as providing a new event with a new dedicated database, install a sub-domain prefix with the corresponding load balancer settings, or move an event to a dedicated server/replica cluster with its own scalability limits based on the user's credentials.

There will also need to be a fair amount of negotiations between the application instances and the orchestrator, relating domain aspects such as event dates, numbers of competitors, number of races to be tracked and the number of regattas, with infrastructure provisioning aspects such as server and network capacities required to support the application set-up.

### Java OSGi

This, together with the skill set profile and the set of components found in our team, suggests the use of the architecture that proved successful during the construction of the SAP Sailing Analytics themselves. The orchestrator shall be developed as a Java OSGi application that uses the existing *com.sap.sse...* bundles for basic aspects such as security (Shiro), replication, mail services and replication (e.g., for replicating the central SecurityService with its user store).

The orchestrator could become the host for the central user store / SecurityService that other application instances replicate by means of partial replication.

Managing an Amazon Web Services (AWS) Elastic Cloud Computing (EC2) landscape will be carried out through Amazon's AWS Java SDK.

### Google Web Toolkit (GWT)

The Web UI of the orchestrator shall be built using the Google Web Toolkit (GWT), using the AdminConsole components from *com.sap.sse.gwt.adminconsole* as well as further GWT UI support from *com.sap.sse.gwt*. This will also allow us to use the *UserManagementPanel* in the administration UI.

### JAX-RS for REST

The REST APIs will be built according to the same patterns of OSGi Web Bundles that use JAX-RS as the implementation pattern for the servlets implementing the APIs. Other than for the grown application domain model, here we may choose to use the *Jackson* framework in a controlled way to pass structured information back and forth between application instances and the orchestrator.

The REST APIs will use the existing authentication/authorization realm implementation that we use already in the application in just the same way that the security-enabled REST endpoints that already exist for the SAP Sailing Analytics do.

### In-Memory with Write-Through Persistence with MongoDB

A running orchestrator instance shall not need to access a persistence layer for reading requests. Just like the applications, the orchestrator has to be able to launch / re-launch with an already established landscape. In this case, the orchestrator will need to be able to find out about the current status of the landscape it is expected to orchestrate. Instead of having to explore the world-wide cloud landscape to find instances the orchestrator may be expected to orchestrate, a persistence layer will help it to recover the last managed status.

Instead of introducing another persistence architecture, the orchestrator should use the existing MongoDB persistence pattern. It has proven useful for the scenarios we have in mind. The orchestrator will not have to deal with excessive write loads, and data volumes we expect to remain in the few megabytes range at most as all these data need to describe are the instances and their roles.

### Logging

The orchestrator needs to log in detail on behalf of which user which landscape interaction has taken place. In particular, this must include all cost-driving interactions such as launching instances, and creating load balancers. The log can be a *java.util.logging* (*jul*) log where the set-up needs to ensure that no log is ever lost upon rotation. www.sapsailing.com:/var/log/old could be the host for those logs as it is the ever-growing place for all sorts of logs we never want to lose.

## Orchestration Elements

### Application Load Balancers (ALBs)

### ALB Rules

### Target Groups

### DNS Record Sets

### Apache Web Servers

The landscape relies on Apache *httpd* web servers mostly for two reasons:

- consolidated, harmonized logging of web requests

- powerful reverse proxying with redirect macros and SSL support

All httpd instances have common elements for SSL certificate configuration as well as a set of redirect and rewrite macros that are aware of the application and its URL configurations, for example Plain-SSL-Redirect, Home-SSL-Redirect, Series-SSL-Redirect, and Event-SSL-Redirect. These macros can then be used to rewrite requests made for a base URL such as *worlds2018.sapsailing.com* to the corresponding event landing page.

Other macros set up end points for server monitoring (*/internal-server-status*) and for the health checks performed by the load balancer's target group which uses the host's internal IP address as the request server name.

The interface for such a web server will need to allow the orchestrator to add and remove such rewrite macro usages, configure the macro parameters such as the event ID to use for the Event-SSL-Redirect macro, and to tell the *httpd* process to re-load its configuration to make any changes effective.

### Java Application Instances and their Health

Application nodes have to provide a REST API with reliable health information.

A replica is not healthy while its initial load is about to start, or is still on-going or its replication queue is yet to drain after the initial load has finished. A replica will take harm from requests received while the initial load is received or being processed. Requests may be permitted after the initial load has finished processing and while the replication queue is being drained, although the replica will not yet have reached the state that the master is in.

A master is not healthy until it has finished loading all data from the MongoDB during the activation of the *RacingEventService*. It will take harm from requests received during this loading phase. After loading the "master data," a master server will try to restore all race trackers, starting to track the races for which a so-called "restore record" exists. During this phase the master is not fully ready yet but will not take harm from requests after loading all master data has completed. For example, in an emergency situation where otherwise the replication cluster would be unavailable it may be useful to already direct requests at such a master to at least display a landing page or a meaningful error page.

### Multi-Instances

A "multi-instance" is a single EC2 host that runs several application processes that share the physical memory and a large locally-attached SSD (typically 2TB or more) used for swap space. Each of these processes has its own configuration in an *env.sh* file which provides the process with a server name, the amount of heap space memory to reserve, a specific MongoDB configuration (DB name, host, port), a RabbitMQ configuration in case the process shall scale with replicas, a port through which the embedded Jetty web server will be reachable, a telnet port at which the OSGi console registers itself, as well as a UDP port for the Expedition connector.

Each server process has its own directory under */home/sailing/servers* that is named after the server name. Should a multi-instance host be re-booted, it will launch all application processes it finds under */home/sailing/servers*. This is important to keep in mind for the scenario of temporarily or permanently migrating a node from a multi-instance set-up to a dedicated replication cluster set-up.

Scaling a multi-instance with replication has not yet been exercised. It may pose a few new challenges due to the replicas likely serving the application on a standard port (usually *8888*) as opposed to the node on the multi-instance host which uses a different port. In this case, separate target groups for the master and the replicas will be required, and the master will not be able to register with the public-facing target group which then only can contain replicas.

### Dedicated Instances

A dedicated server will typically be used for an event whose size in terms of competitors and races tracked and the number of users concurrently watching those races live exceeds what we think a multi-instance set-up can reasonably carry. Dedicated CPU and memory resources will ensure that such a live event runs smoothly and can scale elastically, independent of other nodes that would be running on the same host in a multi-instance set-up.

As in the multi-instance case, replication can be used for dedicated instances. Here, since the dedicated instance will also run on the default port, optionally the master and the replica instances can all be put into the public-facing load balancer target group.

A dedicated instance will grab the amount of RAM it reasonably can with the physical RAM provided by the EC2 host it is deployed to. Its HTTP port will default to 8888, the telnet port to 14888 and the Expedition UDP port to 2010. Like a multi-instance it has a server name as well as a MongoDB and RabbitMQ configuration.

### Master Data Import (MDI)

An instance can run a Master Data Import (MDI) for one or more leaderboard groups from another server instance. The typical scenario is that of importing an event into the archive server, but other scenarios may be conceived.

We will need a secured API to trigger an MDI on a server instance. Beyond this, connectivity data needs to be obtained from the exporting server so that all tracked races can be restored on the importing server. Afterwards, a comparison has to be made, ensuring that all data has arrived correctly on the importing side.

Part of such a scenario can also be to manage the remote server references on the importing server: should the importing instance be the archive server and should the archive server have had a remote server reference to the exporting server, this reference can be cleared after the import has succeeded.

### Archive Servers

### Sharding

### MongoDB Databases

### RabbitMQ Servers and Exchanges

## Orchestration Use Cases

### Create a New Event on a Dedicated Replication Cluster

### Create a New "Club Set-Up" in a Multi-Instance

### Migrate from a Multi-Instance to a Dedicated Replication Cluster

### Migrate from a Dedicated Replication Cluster into a Multi-Instance

### Observe and Automatically Scale a Replication Cluster

### Archiving

### Archive Server Upgrade and Switch

### Automatic Archive Server Fail-Over

### Configure Sharding in a Replication Cluster

### Amazon Machine Image (AMI) Upgrades

