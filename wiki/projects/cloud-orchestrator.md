# Cloud Orchestration - Project Plan

[[_TOC_]]

The cloud landscape used to run the SAP Sailing Analytics as well as its development infrastructure has grown increasingly comprehensive and elaborate over the last few years. It has largely been configured through manual efforts on behalf of event and infrastructure operators and administrators.

With a growing number of events---be it through sailing leagues or championship events in one or more classes---and with a plan to scale the SAP Sailing Analytics to a broader audience through the use of the SAP Race Manager and the SAP Sail InSight apps, more automation is required for the cloud infrastructure.

We believe that a central *orchestrator* approach should be used to solve this challenge. Such an orchestrator, a bit like outlined in the figure below, will accept requests immediately from authenticated and authorized users through a Web interface, as well as from processes running on other servers in the cloud, through REST APIs on behalf of authenticated and authorized users.

![](https://wiki.sapsailing.com/wiki/images/orchestration/architecture.png)

## Overview of Cloud Configuration

As of this writing (January 2020), our cloud setup has the following essential components:

### Application

- A Route53 DNS zone for all of sapsailing.com
- Two Application Load Balancers (ALB)
  - a default ALB for dynamic mapping, receiving all &ast;.sapsailing.com requests
  - one targeted by specific DNS rules
- Target Groups for each ALB rule
  - separate target groups for "-master", explicitly routing to the master instance of a replica set
  - target groups for sharding scenarios with ALB rules based on leaderboard path suffix in URL
- For replica sets with active replicas a dedicated "-master" ALB rule and target group
- A central Apache reverse proxy catching all requests not caught by a specific rule in the default ALB
- A central RabbitMQ instance
- A MongoDB replica set with two fast-writing (ephemeral NVMe-based) and one hidden EBS-based replica
- A multi-instance server node hosting many small application JVMs with fast, big NVMe-based swap space
- A production archive server (currently still with big RAM (~240GB))
- A fail-over archive server, also with ~240GB, running a previous version
- master/replica instances for dedicated events, each on their own AWS EC2 host, each with their own Apache reverse proxy on that host
- a central log store, NFS-mounted on all application instances, with regular log rotation from the instances to the central store, as well as upon instance shut-down.

### Development Support

- dev.sapsailing.com as a test server to which anybody with access can deploy any release at any time
- Hudson server, launching slaves on demand; co-deployed on the host running dev.sapsailing.com
- Bugzilla server, co-deployed on the central Apache reverse proxy web server
- git, hosted at sapsailing.com:/home/trac/git on the same instance running the central Apache reverse proxy
- releases.sapsailing.com, at sapsailing.com:/home/trac/releases, including the environments/ subdirectory targeted by the ``USE_ENVIRONMENT`` directive of the instance details variables.

### Critique, Problem Description

While the landscape set-up has carried us through approximately 17,000 races at hundreds of events, it doesn't lend itself well for a larger-scale self service with many anonymous users in various regions, creating a large number of tracks, also at peak times concurrently. What are the current issues?

#### Subdomain-based level-7 routing

Except for the sharding feature where we encode the leaderboard name in the URL path and then can direct such requests to specific target groups, all other ALB routing decisions are usually based on the subdomain name. Using per-event subdomains makes for a relatively easy routing configuration: if no DNS record is created for the subdomain, the default ALB receives the request, and a rule for that subdomain can easily be added there. When later the event is archived, a reverse proxy rule is added to the central Apache reverse proxy, and the ALB rule can be removed.

The problem with this approach is that it *requires* a dedicated subdomain for knowing how to route traffic for an event. All traffic addressed to www.sapsailing.com is routed to the archive server, and no re-direct is in place, nor would it be useful or scalable given the "horizontal traffic" this would generate and how it would make the archive server a bandwidth bottleneck.

The problem is aggravated by the kind of request we're typically facing with GWT RPCs: by default, these are all HTTP POST requests sent to the same URL, with no query parameters or any other information useful for an ALB's routing decision. The only trick we can play, like we already do for the sharding feature, is artificially extending the GWT RPC service URL's path and providing a server-side web.xml configuration that ignores any path suffix, routing all requests with the correct path prefix to the service implementation.

If we want to avoid *having* to use subdomains then we need to come up with something like "scopes" which the client uses to add a suffix to the request path and that the ALB can use in its routing decision. The challenge with this approach could be that so far the application's domain model does not have a single object hierarchy that would help in defining a unique scope for each object. Noteworthy, in particular, is the dichotomy of the LeaderboardGroup/Event relation where a LeaderboardGroup can be a part of a larger event that hosts several LeaderboardGroups; or it can be a series of events, hosting leaderboards from several events, such as in a national sailing league season.

We may need to define scopes as a transitive closure of the LeaderboardGroup/Event association, just like today we handle the separation of events and event series from the archive server and their later import into the archive.

Likewise, we should work on [bug 5187 (Avoid need for "-master" URLs  / sub-domain)](https://bugzilla.sapsailing.com/bugzilla/show_bug.cgi?id=5187) to allow for splitting up GWT RPC requests depending on where they need to go: master or replica.

#### Scalability limits during tracking data ingestion and fix loading

When sensors submit their fixes, they currently need to send them straight to the master instance of the replica set hosting the scope to which the fixes apply. The master writes the fixes to the ``SensorFixStore`` which currently is backed by the MongoDB replica set. This set-up requires a sensor to manage different ingestion points for different scopes and makes a master a single point of failure for the ingestion process. When a master becomes unavailable, all depends on the devices' capabilities to buffer and re-send the data that currently cannot be delivered. Furthermore, replicas depend on the single master being available in order to be supplied with the sensor data.

Another bottleneck is the fact that we currently run all application instances based on a single MongoDB replica set which has its own single PRIMARY instance. All MongoDB write requests need to go through this PRIMARY as long as no MongoDB Sharding Controller is established that splits the write load across several clusters based on a sharding key.

Loading the time-index fixes for a race from a single MongoDB collection quickly reaches limits in case the index size outgrows the amount of RAM available to the MongoDB servers. While this query scenario currently mostly applies when re-starting a master server (including the archive server), the archive data has already reached a size that requires us to launch a MongoDB replica with more RAM in order to keep archive server restart times acceptable (hours instead of days).

We should also consider alternatives to MongoDB, at least for the storage of the sensor fixes. [Cassandra](http://cassandra.apache.org/) seems an interesting approach that promises high availability and virtually unlimited scalability. 

#### No automatic fail-over for archive server

When the archive server fails, a few people get an SMS/text message notification. Manually switching the central reverse proxy configuration in /etc/httpd/conf.d/000-macros.conf is then necessary, followed by a ``service httpd reload`` command to switch to the failover archive server. This process needs automation. A special configuration of "availability" checks between production and failover archive server will be required. We have to figure out where best to put this failover feature: is it something the ALB / target group set-up can do for us? How would the central reverse proxy/proxies route the requests then?

Alternatively, we could look at other mechanisms for implementing the fail-over functionality. For example, Apache can be configured in "balancer" mode where failover rules can be specified explicitly.

#### No good approach for dynamic scale-up

As a server fills up, be it the archive or an event server or a shared, multi-tenant server such as we currently run under my.sapsailing.com, the server resources may at some point not suffice to host more data. Moving scopes out of the server can be one approach, involving master data import and other less automated steps such as starting the tracking again for the races on the receiving side (we should consider sending and executing the RaceTrackingConnectivityParameters to the importing server, optionally restoring everything automatically). But in other cases, a scope may not be splittable and may already live in its dedicated replica set. In this case, the instances of the replica set must be scaled up.

Scaling up replicas is easy (add bigger replicas, terminate smaller replicas after enough bigger replicas have become available), but usually it is not the replicas only, but the master typically also requires scaling up. Based on the current architecture, a master is the single ingestion point, especially for any request that impacts persistent storage, such as smartphone GPS fixes, race log entries or updates requested by the administration console. Since we need to read a consistent snapshot from the database and will typically not read from the persistence layer again for the remainder of the instance's runtime, we must make sure that once reading has started, not writes occur until reading has finished. Furthermore, all writes from this point on need to target the new master.

In other words, all updates need to be rejected for that replica set when the new master starts up. When the new master is ready, update requests will be accepted again and will be targeted to the new master. The old master can then be terminated. Since the new master will send updates into the same RabbitMQ fanout exchange, existing replicas will continue to receive updates after the master switch. If the replicas configured the HTTP channel to the master such that the new master is reached through the existing channel (e.g., through a load balancer's target group or an elastic IP or maybe some DNS configuration) then even "reverse replication" will continue to run smoothly. Replicas buffer reverse replication requests that cannot be delivered to the master currently, and they will try a re-send later.

The problem with this approach is that the master re-start takes time, more so if the scope that needs to be loaded is bigger. For live scenarios this can easily be a show stopper. It would be much nicer if an existing replica could be turned into a master on the fly. It would have most in-memory content, so no long-running loading from the database would be required. But replicas currently have no consistent database state in their phony database, and they do not run any tracking connectors but receive updates only through the RabbitMQ queues. A replica would need to start all the active "trackers" including establishing connections to external systems such as the TracTrac system as well as the smartphone tracking listeners, and it would become the target of all update requests, similar to how a MongoDB can change a replica set member's role from SECONDARY to PRIMARY and have it accept write requests.

#### Vast RAM requirements for "cold" storage

Today this affects mostly the archive server, but increasingly, as self service usage becomes more popular, large amounts of data may be accumulated on what today is the "my.sapsailing.com" scope. The typical access pattern for regular use of the application is that users look at an event. As they click through the event, race tracking data is read. The data of a single race is usually not too big, compared to the RAM sizes we usually discuss. Also, given the average throughput of a well-configured storage system, all the data of one race can most certainly be loaded in less than a second. With this in mind it is a shame to "waste" expensive RAM only to have all this rarely used data available and accessible.

More challenging are DataMining requests. See also below ("DataMining in the presence of distribution"). A single data mining request can potentially read from very many races, touching a large data set. How would we keep up the good performance of the data mining framework when "cold" storage is much slower to read than in-memory data? Is super-fast but still inexpensive swap space a possible solution? I am starting to experiment with ab i3.2xlarge instance type with a 1.9TB NVMe local SSD that seems to give decent throughput. ``hdparm`` reports more than 500MB/s even under load, and I'm testing an archive server set-up on such an instance with "only" 64GB of RAM and 1.9TB of swap space. This set-up also seems a lot faster than an r5d.2xlarge with similar CPU/RAM configuration. It seems, the "storage-optimized" i-family of instances gets much better NVMe throughput compared to the r-family of instances which are "memory-optimized."

Alternatively, we could start thinking about storing the race data in files that needs to be loaded upon request / demand and that can get unloaded at a later point. It seems, though, that a good operating system-level memory manager should perform equally well, if not better, when swapping in the data required.

#### Late SSL Offloading

We currently forward the HTTPS requests from the ALB to the reverse proxies using again HTTPS requests. This requires us to have the certificate deployed to all reverse proxies as well as the ALBs, with all the burdens of upgrading it in all places when the time comes. We should offload SSL/TLS at the ALB and use only HTTP internally.

#### Reverse Proxy Strategy

Reverse proxies allow us to configure re-write rules, expanding URLs to they lead users to specific events or event series landing pages, and handle logging. With AWStats and the ``com.sap.sse.util.apachelog`` package we have two components currently helping with log analysis. Our host shutdown script takes care of moving all log files to a central log store where they are evaluated on a weekly basis by said tools.

Currently we have a central reverse proxy receiving all requests not handled by a DNS-mapped ALB and falling through to the default rule of the default ALB. This currently includes requests for Hudson, Bugzilla, HTTPS-based git access, access to releases.sapsailing.com, as well as all rewrite rules targeting the archive server, including www.sapsailing.com which leads to the landing page /gwt/Home.html and all subdomain URLs for all archived events/leagues/scopes. Furthermore, each EC2 host that runs one or more JVM application processes has its own reverse proxy running on it.

Only thanks to the high reliability of the AWS infrastructure and the maturity of the Apache web server, nothing really bad has happened with our central reverse proxy web server. Still, this is not a good and scalable approach. At least, having more than one instance, ideally in different availability zones, that share a central configuration, the ALB would easily survive a restart or other unavailability of one of these reverse proxy instances.

We could also change the landscape such that the archive servers are in a target group that receives the requests ending up ad the ALB's default rule. We would only create dedicated rules for those subdomains currently also handled by the central reverse proxy that are not archived events (hudson, bugzilla, git, releases, jobs, p2, gitlist, wiki, maven, status, analysis, static). Any archive server (production, failover) could then according to the usual pattern run its own reverse proxy, again sharing their configuration. The health check for the two archive servers then has to work such that the production instance does its regular health check whereas the failover server always reports "unhealthy" as long as the production server is "healthy." For this, the failover instance needs to know one or more other servers to probe. Only if all of those report "unhealthy" and the failover itself is healthy, it will report "healthy" as its status. See also [bug 5188 (Implement a "failover for" setting that lets a health check pass only if all other instances are unhealthy)](https://bugzilla.sapsailing.com/bugzilla/show_bug.cgi?id=5188).

#### DataMining in the presence of distribution

With scopes split out to separate replica sets, the connections that may hold these scopes together are the "remote server references" like we use them to point from the archive server to all current event replica sets. The DataMining.html module so far works only locally on a single instance. It would be nice if it could as well follow those remote server references and retrieve data from those other replica sets / scopes as well. This would require serializing the query with its retriever chain and settings, filter conditions, grouping dimensions and extraction function. All steps including the grouping of extracted statistics can then happen on the remote replica set, and the grouped extracted values will need to be serialized back to the instance where the query originally is executed. There, the aggregation will take place.

[Bug 5189 (Enabling data mining queries to follow remote server references)](https://bugzilla.sapsailing.com/bugzilla/show_bug.cgi?id=5189) documents this as a feature request in Bugzilla.

If we had this in place, moving scopes around the landscape would be much more natural.

#### Lack of automation

All processes around scaling, infrastructure provisioning, moving scopes such as performing a "Master Data Import" or creating a dedicated replica set, monitoring and instance size selection are manual, hence error-prone. If we want to survive a growing number of self-service users we need to automate all aspects of the infrastructure.

## Idea: Orchestrator Architecture Based on Java and com.sap.sse

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

Up to 20 application load balancers (ALBs) can be used in a single AWS region. Each of them can have up to 100 rules that map requests to a target group based on hostname and URL path patterns. We use a default ALB that is the CNAME target for **.sapsailing.com*, and as such allows the orchestrator to add rules to it without the need to add new DNS records to Route53. Dedicated ALBs can be used in addition to circumvent the limit of 100 rules per ALB. To ensure those dedicated ALBs are targeted by user requests, DNS records for the sub-domains that those ALBs have rules for need to be added to Route53.

All ALBs need default rules that forward requests to the archive server (production or fail-over).

Currently, several sub-domain names exist (e.g., wiki, bugzilla and hudson) that are also handled by the default ALB, forwarding to our central webserver which then acts as a reverse proxy, using rewrite rules to redirect the clients to the specific hosts running those services. We may consider changing this and introduce DNS records for those service-based sub-domains, mapping them explicitly to the elastic IP of the central webserver. This would allow us to separate the Apache httpd instances for those central services from the httpd instances used for the archive servers.

### Target Groups

Those hold the nodes responding to requests. In a replication cluster we usually want to have one for the "-master" URL that event administrators use

### Volumes

There are a number of disk volumes that are critical to monitor and, where necessary, scale. This includes:

- Database server (*/var/lib/mongodb* and sub-mounts, */var/lib/mysql*)
- RabbitMQ persistent queues
- Central Webserver / Reverse Proxy (*/var/log/old*, */var/log/old/cache*, */var/www/static*, */var/www/home* hosting the git repository) (although we should really consider getting rid of a central, single-point-of-failure reverse proxy approach)

Through the AWS API the read/write throughputs can be observed, and peaks as well as bottlenecks may be identified. At least as importantly, the file system fill state has to be observed which is not possible through the AWS API and needs to happen through the respective instances' operating systems.

It shall be possible to define alerts based on file systems whose free space drops below a given threshold. Re-sizing volumes automatically may be an interesting option in the future.

### Alerts

Alerts can be defined for different metrics and with different notification targets. SMS text messages and e-mail notifications are available.

The orchestrator shall be able of managing such alerts. It shall be possible to attach such alerting rules to an entire replication cluster, meaning that all target groups used for managing the cluster shall receive the same monitoring and alerting rule (see also [Sharding](#sharding)).

### DNS Record Sets

As the number of rules per load balancer is limited to 100 as of now, and only one ALB can be set up to handle **.sapsailing.com*, this ALB can only be used for the volatile, fast-changing sub-domain names and event-driven set-ups, given there are not even too many of those happening concurrently to exceed the limit.

All other, specifically the longer-running, sub-domains shall be mapped through a dedicated Route53 DNS entry in the *sapsailing.com* hosted zone. Up to 20 ALBs can be created per region, and each of those can have up to 100 rules. This should suffice for some time to come. We should also consider asking users to pay for the special service of a dedicated sub-domain in the future.

Those dedicated DNS entries then point to a non-default ALB that has the rules for that sub-domain. Those ALBs also default to the archive server and central web server if no other rule matches. Should a sub-domain's content be archived, the DNS entry can be removed. The default rules of both, the dedicated ALB and the default ALB for **.sapsailing.com* will forward requests to the archive server.

### Apache Web Servers

The landscape relies on Apache *httpd* web servers mostly for two reasons:

- consolidated, harmonized logging of web requests

- powerful reverse proxying with redirect macros and SSL support

All httpd instances have common elements for SSL certificate configuration as well as a set of redirect and rewrite macros that are aware of the application and its URL configurations, for example Plain-SSL-Redirect, Home-SSL-Redirect, Series-SSL-Redirect, and Event-SSL-Redirect. These macros can then be used to rewrite requests made for a base URL such as *worlds2018.sapsailing.com* to the corresponding event landing page.

Other macros set up end points for server monitoring (*/internal-server-status*) and for the health checks performed by the load balancer's target group which uses the host's internal IP address as the request server name.

All our hosts can run their own Apache httpd process. Currently, the two archive servers don't. Instead, they rely on the single central www.sapsailing.com webserver which hosts all redirect macro usages for all sub-domains that shall be handled by the archive server. This should probably be changed in the future, such that each archive server runs its own httpd process. It would remove the single point of failure that the current central webserver represents and may make room for a more clever set-up where the fail-over archive server is handled by a target group that is the default in the ALB rule set even after the default rule that points at the target group for the production archive server. This way, together with an alarm defined, failover to the secondary archive server could be automatic and instant.

The interface for such a web server will need to allow the orchestrator to add and remove such rewrite macro usages, configure the macro parameters such as the event ID to use for the Event-SSL-Redirect macro, and to tell the *httpd* process to re-load its configuration to make any changes effective.

### Java Application Instances and their Health, "Replica Sets"

Application processes are Java Virtual Machines (JVM) that need memory and a database connection (MongoDB), optionally a RabbitMQ configuration in case replication is being used. The JVM can hold the data of a number of events. It can be configured as a master or a replica. A replica replicates a single master. The master plus all its current direct and transitive replicas are called a "replica set." The master must have a valid database connection to the database actually holding the data. Replicas point to a phony "ephemeral" database whose consistency is not guaranteed and whose contents must never be queried. A JVM can be configured at launch to be a replica, providing information about the master instance and the RabbitMQ fan-out exchange to which to subscribe. An instance can also be dynamically turned into a replica, making it lose all its application data it had so far, and fetching an initial load from its new master. This approach, however, is currently not recommended, mostly because of the phony database problem: an instance turned into a replica will cause its database to become potentially inconsistent, and we currently have no way of changing the database connectivity on the fly for a running instance.

Application nodes have to provide a REST API with reliable health information. /gwt/status is a good start as it now also provides reliable information about (initial) replication status.

A replica is not healthy while its initial load is about to start, or is still on-going or its replication queue is yet to drain after the initial load has finished. A replica will take harm from requests received while the initial load is received or being processed. Requests may be permitted after the initial load has finished processing and while the replication queue is being drained, although the replica will not yet have reached the state that the master is in.

A master is not healthy until it has finished loading all data from the MongoDB during the activation of the *RacingEventService*. It will take harm from requests received during this loading phase. After loading the "master data," a master server will try to restore all race trackers, starting to track the races for which a so-called "restore record" exists. During this phase the master is not fully ready yet but will not take harm from requests after loading all master data has completed. For example, in an emergency situation where otherwise the replication cluster would be unavailable it may be useful to already direct requests at such a master to at least display a landing page or a meaningful error page.

The Java instances have a few and shall have more interesting observable parameters. Some values can be observed through the AWS API, such as general network and CPU loads. So far, the number of leaderboards and the restore process can be observed using JMX managed beans, as can be seen in the JConsole. Other interesting parameters to observe by the orchestrator would be the memory usage and garbage collection (GC) stats, as well as information about the thread pools, their usage and their contention. It would be great if the orchestrator could find out about bottlenecks and how they may be avoided, e.g., hitting bandwidth limitations or not having enough CPUs available to serialize data fast enough for the bandwidth available and the demand observed.

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

We run a production copy of the "archive server" that hosts selected events for which we decide that they have sufficient quality to be promoted through sapsailing.com's landing page. Most events sponsored by SAP fall into this category. The archive server is the target of a "Master Data Import" (MDI) after an event is finished, where data is moved from a dedicated event server into the archive.

As the archive server requires several hours for a re-start, a failover instance exists that usually runs a release that is not as new as the one on the production archive server. The reason for this choice of release is that in case we run into a regression that tests have not revealed, switching to the failover archive allows us to "revert" to a release that hopefully does not have this regression while we then have some time to fix the issue on the production archive server.

The failover archive server may be used for special purposes such as tricky data-mining queries that we don't want to affect the performance of the production archive server, or acting as an additional Hudson build slave because its CPUs are usually idling.

The orchestrator shall be aware of the two archive server instances and shall know which one is the production and which one the fail-over copy. It should have an awareness of the releases installed and should offer the release upgrade as an action that will install the latest release to the fail-over copy, restart it, wait for it to become "healthy" and then compare the contents with the production archive server. If all runs well, routing/ALB/Apache rules can be switched to swap production and fail-over copy.

There should in the future be dedicated target groups for production and fail-over archive servers that can be used to define an automatic fail-over order in the ALB. A severe alert should be sent out if the production archive target group has no healthy target.

### Sharding

Requests to the */gwt/service/sailing* RPC service that cause calculations for a specific leaderboard are suffixed with the leaderboard name to which they are specific. Example: */gwt/service/sailing/leaderboard/Sailing_World_Championships_Aarhus_2018___Laser_Radial*. The leaderboard name undergoes escaping of special characters which are then replaced by an underscore character each.

With this approach, a load balancer can use the URL path with the escaped leaderboard name as criterion in a rule that dispatches requests to target groups based on the leaderboard. This way, although all replicas in a replication cluster maintain an equal memory content, calculations in a replica can be constrained to only a subset of the leaderboards the replica maintains.

Without this mechanism, all replicas in a replication cluster would be targeted with requests for all leaderboards in a random, round-robin fashion, leading to all replicas running the live calculations for all leaderboards redundantly. As more replicas are added to a replication cluster for a domain with several live leaderboards, using this sharding feature makes more sense because the recalculation load can be split evenly across the replicas.

A target group for each subset of leaderboards has to be created. The ALB will then have to have a rule for each leaderboard name, deciding the target group to which to route requests for that leaderboard. A default target group for the replication cluster shall exist which catches all other requests and which becomes the default in case a sharding target group runs out of healthy hosts.

### MongoDB Databases

We currently have a single server node *dbserver.internal.sapsailing.com* on which there are four MongoDB processes running. Three of those are for testing (*dev.sapsailing.com* uses the process listening on port 10201) or legacy purposes, and only one, listening on port 10202, has all the content relevant for the entire production landscape.

This MongoDB process hosts various MongoDB databases, each with their separate names and separate collections. With the exception of the production and fail-over archive servers which use the *winddb* database, all other master servers use a DB name that should equal that of the server name which usually is a technical short name for the event. Example: *KW2018* represents the server and DB name for the *Kieler Woche 2018* event.

Replication clusters use a second database that is used by all replicas in the cluster. Right now, replicas don't interact in any well-defined way with the database, and we say that a database accessed by a replica is not in a well-defined state. Replicas don't read from their database which is the reason why this does not matter. For example, in addition to the *KW2018* database there is a *KW2018-replica* database used by all replicas in the *KW2018* replication cluster.

The MongoDB host currently has a fast EBS volume attached to which it stores all the databases. There is only a single instance in the landscape, making this currently a single point of failure. We should consider changing this.

The orchestrator shall know about the MongoDB instances we run in the landscape, shall know their ports and hosts and thereby shall be able to monitor in particular the disk volume holding the DB contents. Alerts should be put in place in case those volumes reach critical fill states. In the future, the orchestrator should learn to run MongoDB in various regions, optionally in replicated mode also across regions, making it easier for us to deploy master/replica set-ups across regions. There may even be MongoDB instances in each region that are not part of the cluster that are used only for the phony replica databases.

### RabbitMQ Servers and Exchanges

Replication is based on RabbitMQ which is used to establish a channel through which the initial load can be transferred safely even under unstable network conditions, and through which the shipping of operations from master to replicas happens, using a "fanout" exchange.

We currently have only a single RabbitMQ instance in the landscape that hosts all exchanges and all initial load queues. As such, it represents a single point of failure.

The orchestrator should know about all RabbitMQ instances in the landscape and should know how its exchanges relate to replication clusters, and how its queues relate to the master and replica instances in the landscape. It can thus observe whether all those objects are cleanly removed again when replicas or replication clusters are dismantled and can, if necessary, clean up remnants.

### Backup Server

A single EC2 instance called "Backup" with large EBS disk volumes attached hosts various forms of backups. We use *bup*, a variant of *git* capable of dealing well with large binary files. This gives us historizing backups where each version may be fully restored.

Subject to backup are all production MongoDB databases, the MySQL database content for our Bugzilla server, as well as the file system of the central Webserver, there in particular the configuration and log files.

The backup script on *dbserver.internal.sapsailing.com:/opt/backup.sh* does *not* backup all MongoDB databases available. I think that it should. See also *configuration/backup_full.sh* in our git repository for a new version.

## How Kubernetes (K8s) May Help

The landscape and architecture concepts described so far can be mapped to the K8s world in many aspects.

### Load Balancing and Ingress

K8s has a concept of "Ingress" which is an external access point to a service or an application. The way we use AWS ALBs to route access through target groups to hosts which usually run Apache reverse proxies to log, rewrite and SSL-terminate traffic which they then forward to applications can be represented well by a set of Ingress resources.

We can distinguish between write loads and read loads, directing write loads to the replica set's master, and read loads to the group of replicas. Currently, write loads are identified by the "-master" part of the subdomain name. However, in future releases we may decide to "tag" write requests, particularly the GWT RPC requests that actually perform updates, by specific URL path elements that the Ingress resource definition may identify and use for a routing decision.

Ingress resources can also be used for our standard services such as the Hudson build server, the Wiki service, the "static" content, releases.sapsailing.com and bugzilla. All other SAP Sailing Analytics replica sets, including dev.sapsailing.com, all "club" servers as well as event and league servers plus the archive server can be Ingress resources of their own.

By means of the "IngressGroup" feature of the most recent beta version of the AWS ALB Ingress Controller we may be able to map our requirements to regular ALBs, avoiding "horizontal" traffic that crosses availability zones (AZs) and being very scalable and efficient. We could try to employ K8s to mimic our current use of AWS ALBs, such that for rather short-lived events we try to get along with a single ALB that is targeted by a "catch-all" DNS rule for "sapsailing.com" such that any host name not explicitly mapped by DNS will end up at that ALB. There, in turn, if no rule exists for the DNS name then it will be mapped to the archive server. Otherwise, the ALB will map to the specific target group that represents a K8s service. This set-up has the advantage of not requiring potentially long-lived and replicated DNS records for mapping the event hostnames to services. When the event moves to the archive, only an ALB configuration change is required, no DNS record needs to be modified.

All long-lived set-ups, however, should have DNS records pointing to the respective ALB. Hopefully, the Route53 support of the AWS ALB Ingress Controller can handle this automatically for us.

### MongoDB and RabbitMQ

Our replica sets require basic services such as a MongoDB for persistence and a RabbitMQ for replication. Both components are capable of running in a high-available clustering mode, also within K8s. K8s can then help to keep those services highly available.

For MongoDB we shall map our current design of two instances in two different AZs with fast NMVe disks of appropriate size for "live" workloads and a hidden replica with a gp2 SSD with full incremental snapshot backups. The two non-hidden instances will share the same node type requirements in terms of memory, NVMe support and vCPUs. They can be part of a regular "Deployment" with pods that shall have anti-affinity, ideally ending up in different availability zones (AZs). They can be restarted at any time because their containers are configured such that they find their replica set and synchronize their content as necessary.

The hidden replica will be less demanding as it has time during off-peak periods to process the op-log. It shall be a StatefulSet resource with exactly one pod, and it requires a PersistentVolumeClaim that is fulfilled by a gp2 SSD. If the pod dies, it can be re-started, attaching to the same PersistentVolumeClaim and hooking up to its replica set again.

The archive DB is also a StatefulSet with exactly one pod. Its configuration will ask for a large but slow PersistentVolumeClaim.

Similarly, for RabbitMQ we could and should configure a small deployment with at least two pods so that in case of one pod's failure the other can take over. This would give us a highly available scenario also for replication use cases.

### SAP Sailing Analytics "Replica Sets"

Such a replica set (not in K8s terms, but in SAP Sailing Analytics terms) would contain a single master instance and zero or more replicas. A "reading" service will be defined that receives all non-administrative, non-mass-data-ingesting requests, including all read requests as well as simple session management requests, but not GPS data ingestion or AdminConsole-triggered requests. The "writing" service will receive data ingestion load as well as anything coming from the AdminConsole entry point. We would need to distinguish reading and writing GWT RPC requests, probably by a URL path extension, similar to the "sharding" pattern we have employed to separate traffic by regatta/leaderboard. In a single-master replica set, the master will be labelled for both, the reading and the writing service. In scaled-out set-ups the master may choose to carry only the "writing" service's label and leave the "reading" service to its replicas.

Replicas should know their master by means of a DNS record for the respective "master" service of the replica set. This way, when a master replacement is necessary, replicas may recover from not finding their master temporarily, as the new master appears under the same label.

Replica sets should be subject to scaling by a Horizontal Pod Autoscaler (HPA). The metrics observed should be the leaderboard recalculation times as well as the number of requests received per second, maybe also the traffic.

A "replica set" should start out with a single master pod labeled as both, write and read load handler. As the read load increases, replicas may be fired up in a new deployment, subject to HPA.

### Vertically Scaling a Master Pod

When a master dies or requires scaling up/down, a new master server needs to be provisioned. A new pod may launch, on the same DB as the current master. For this not to cause trouble, the current master needs to be removed from its service, e.g., by removing its service-related label. This way, the old master stops processing requests. Write requests will have to be queued or rejected, and clients should be built such that they will re-try at a later point in time.

When the new master has completed its start-up phase and is considered available, it can be tagged with the master service tag. This will let existing replicas as well as external writing clients send their traffic to the new master.

### Vertically Scaling Replicas

Easy... Launch more replicas with the configuration desired and dismantle the old ones.

### Archive Server

The archive server shall provide smooth fail-over because it is the critical landing page of the entire web site. We aim to have two copies of an archive server running at all times. One is the current master, the other is a fail-over that usually will be on a previous version. The reason for this is that in case of a regression or other grave problem introduced by a new release we can simply shut off the new archive server instance, and the fail-over instance with the last-known-good version should take over transparently until a new, fixed version has been deployed.

The archive servers are pretty special. They require lots of RAM but could live with less RAM than would be suggested for the size of RAM requested. Clever worker node group configurations may help utilizing expensive hardware used mainly for archive servers better.

The details of how the failover logic is implemented and how an upgrade is to be performed need to be clarified. In particular the launch of an archive server instance up to now brings a bunch of issues with it, in particular, we regularly see that some races don't load properly during the automatic restore process. These need to be identified, and re-tries need to be issued. Only when the new archive server is available with all races loaded shall it be labeled with the production archive service label, then taking the www.sapsailing.com traffic, whereas the old archive server pod then shall be demoted to the failover set-up.

### Version Upgrades

Upgrading versions is tricky because the GWT RPC clients are sensitive to even small changes. So are round robin-scheduled client calls to different versions of the GWT RPC service implementation.

It is therefore advisable to launch an entirely new replica set with a new master and a new set of replicas, as required by the current traffic / request loads. The old master should be removed from its service when launching the new replica set starts. Once the new replica set is available the Ingress definition can be switched to point to the new version of the service.

Afterwards, the old version of the service with all its pods can be terminated.

### Cross-Region and Multi-Region Set-Up

For events or clubs or federations in specific regions we should make use of the possibility to place the service in the appropriate region. This will require a K8s cluster in that region, and all those clusters need to share a common Route53 set-up with the same "sapsailing.com" record set that is being configured by the Route53 support of the AWS ALB Ingress Controllers running in the various clusters.

Regarding MongoDB and RabbitMQ connectivity, the VPCs in the different regions will need to be connected such that pods in remote regions can still see and access the MongoDB and RabbitMQ services. Alternatively, we could add a MongoDB and RabbitMQ replica per region, but the problem with that is that specifically for MongoDB we would need to start introducing sharding because otherwise the primary MongoDB instance may be in a different regions, requiring all write requests to be remote.

## Orchestration Use Cases

### Create a New Event on a Dedicated Replication Cluster

A user wants to create a new event. But instead of creating it on an existing server instance, such as the archive server or a club server, he/she would like to create a new dedicated server instance such that the server is a master which later may receive its own replicas and thus form the core of a new replication cluster. The user defines the technical event name, such as *TW2018* for the "Travemünder Woche 2018," which is then used as the server name, MongoDB database name, and replication channel name.

The steps are:

- Launch a new instance from a prepared AMI (either the AMI is regularly updated with the latest packages and kernel patches, or a "yum update" needs to be run and then the instance rebooted). The user provide hints as to the sizing of the instance in terms of CPU and memory. Ideally, these sizing parameters would be given in domain concepts, such as number of tracked competitors expected, or number of competitors in largest leaderboard in the event.
- The latest (or a specified) release is installed to */home/sailing/servers/server*
- The */home/sailing/servers/server/env.sh* file is adjusted to reflect the server name, MongoDB settings, as well as the ports to be used (for a dedicated server probably the defaults at 8888 for the HTTP server, 14888 for the OSGi console telnet port, and 2010 for the Expedition UDP connector)
- The Java process is launched
- An event is created; it doesn't necessarily have to have the correct name and attributes yet; it's only important to obtain its UUID.
- With the new security implementation, the event will be owned by the user requesting the dedicated replication cluster, and a new group named after the unique server name is created of which the user is made a part.
- The user obtains a qualified *admin:&lt;servername&gt;* role that grants him/her administrative permissions for all objects owned by the group pertinent to the new replication cluster.
- An Apache *httpd* macro call for the event with its UUID is inserted into a *.conf* file in the instance's */etc/httpd/conf.d* directory
- The Apache *httpd* server is launched
- Two target groups *S-ded-&lt;servername&gt;* and *S-ded-&lt;servername&gt;-master* are created, and the new instance is added to both of them
- Two new ALB rules for *&lt;servername&gt;.sapsailing.com* and *&lt;servername&gt;-master.sapsailing.com* are created, forwarding their requests to the respective target group from the previous step
- If requested, a remote server reference is added to www.sapsailing.com that points to the public URL of the replication cluster

Monitoring for the target groups shall be established and wired to the auto-scaling procedures which will launch more replicas or terminate replicas as needed. See also [Observe and Automatically Scale a Replication Cluster](#observe-and-automatically-scale-a-replication-cluster).

### Create a New "Club Set-Up" in a Multi-Instance

This is a variant of [Create a New Event on a Dedicated Replication Cluster](#create-a-new-event-on-a-dedicated-replication-cluster). Likewise, the user will provide a technical server name, such as "LYC" for "Lübecker Yacht-Club."  A point of contact may be provided, in the form of a name and an e-mail address, telling whom to contact for questions about the instance.

The difference compared to a dedicated replication cluster set-up is that a multi-instance host needs to be identified that has resources available to run the new Java process. This requires enough disk space, as well as enough available fast swap space and a CPU usage that is not saturated. When such a host has been identified, no *yum* activity or anything related to the AMI will be conducted. Otherwise, a new multi-instance host needs to be launched from an AMI, and the same *yum* activities as above apply.

A port combination for Jetty/Telnet/Expedition UDP is identified based on any already mapped Java processes on that host. The port combination together with the MongoDB and RabbitMQ settings are stored in an *env.sh* file under */home/sailing/servers/&lt;servername&gt;* and the process can be launched as usual.

The Apache configuration entry needs to be added, pointing to the respective Jetty port, usually with a "*Plain*" or "*Home*" macro because such club servers usually don't serve a single event. As in the scenario above, the Apache *httpd* server process will then need to load the new configuration.

The load balancer handling varies slightly compared to the dedicated replication cluster set-up. The health checks need to be set to probe the Java instance using HTTP at the registered Jetty port, not the HTTPS set-up through the Apache server because the Apache server on a multi-instance can handle several Java processes.

Using a separate *-master* URL seems advisable because when migrating the Java process to a larger set-up later, all devices have already been bound to the *-master* URL, and no re-configuration will be required.

### Migrate from a Multi-Instance to a Dedicated Replication Cluster

When the orchestrator figures that an event is approaching on a Java server hosted in a multi-instance set-up and the event seems too large to be handled successfully by such set-up, migration to a dedicated replication cluster is a good way to handle the load. This migration should ideally happen when there is no write-load applied to the instance.

As a first step, the *-master* target group is emptied. This will avoid any write requests such as those coming from the Race Manager app or the Sail InSight app reaching a master process that is deprecated and will be stopped anytime soon. Then, the [Create a New Event on a Dedicated Replication Cluster](#create-a-new-event-on-a-dedicated-replication-cluster) scenario is executed, except for the step of creating a new event and new load balancer rules and target groups. Just by configuring the MongoDB database name, all existing data held so far by the Java process hosted in the multi-instance set-up will be loaded into a dedicated master instance.

The new dedicated master server can be added to the public-facing target group for the server name as well as to the *-master* target group. The health checks of both, public and *-master* target group need to be changed to the default health check rules for dedicated replication clusters (using the default HTTPS port). When healthy, the old master server will be removed from the public-facing target group. The Java process on the multi-instance host can be shut down. Its resources such as the directory on the multi-host and the port "reservations" that this entailed can be freed. The Apache httpd configuration entry on the multi-instance host shall be removed, and the Apache configuration shall be reloaded.

### Migrate from a Dedicated Replication Cluster into a Multi-Instance

This is the reverse case to [Migrate from a Multi-Instance to a Dedicated Replication Cluster](#migrate-from-a-multi-instance-to-a-dedicated-replication-cluster). In detail:

- Start out with removing the existing master server from the *-master* target group
- [Create a New "Club Set-Up" in a Multi-Instance](#create-a-new-club-set-up-in-a-multi-instance), except that the database already exists, and the data will be loaded from that existing database, and the target groups and load balancer rules do not have to be created
- add the new multi-instance Java server to the two target groups
- change the health check rules for both target groups so they use HTTP for the multi-instance Java server's HTTPS port
- when the new Java server process is considered healthy by both target groups, remove the dedicated master and all replicas from all target groups
- terminate all instances of dedicated replication cluster

### Observe and Automatically Scale a Replication Cluster

According to our experience, in most cases scalability limitations are caused by bandwidth bottlenecks. Less frequently, CPU limitations cause performance degradations when several large leaderboards need to be re-calculated at high frequency. This can also be caused by several dedicated requests for specific, non-live time points. There are different approaches to handling these two different kinds of bottlenecks. The former requires more bandwidth in the form or more instances to be made available to the load balancer. The latter requires more CPU resources and optionally the use of [sharding](#sharding).

When bandwidth limitations are observed, adding replicas is the simple remedy. The replicas are launched with the exact same release as the master to which they shall belong. Note that replication can also be used for master server processes running on a multi-instance host. The master server may or may not be part of the public-facing target group. If it is low on network resources it may be a good idea to not include it in the public-facing target group. The new replica will be added to the public-facing target group. A solid health check will avoid that it receives any application domain-oriented requests unless the replication process has completed the initial load phase successfully.

When a master server process is CPU bound, this will usually be because of abundant leaderboard (re-)calculation requests hitting that server. The quickest remedy is to add replicas to the public-facing target group and to remove the master server from that group. Those replicas can have more CPU resources than the master. Their memory should not be less than that of the master.

If replicas are bound by their CPU power, simply adding more replicas will usually not help. Instead, [sharding](#sharding) may provide a solution (see also [Configure Sharding in a Replication Cluster](#configure-sharding-in-a-replication-cluster)). It allows spreading leaderboard (re-)calculation load across several target groups, avoiding a situation where all replicas have to (re-)calculate all leaderboards all the time. Target groups will then receive only a subset of the leaderboard (re-)calculation requests, based on the leaderboard names.

When the CPU and/or network load drop under a given threshold for a given period of time, replicas may be removed from the public-facing target group and can then be terminated, once the *draining* status is over. During termination the replica will automatically save its logs.

### Archiving

When an event is over, a dedicated replication cluster is no longer required, and the scaling requirements change from high-CPU, high ingestion rates to read-only with decreasing, rather sporadic access. The archive server(s) are geared towards this load profile. Furthermore, with our current data mining architecture, archived races are amenable to holistic analysis.

The archive server, as described in [Archive Servers](#archive-servers), comes as a production and a fail-over instance. Archiving is done using the Master Data Import (MDI) on the production archive server, using the dedicated replication cluster's master as source. An API shall be provided that lets the importing server discover the restore records needed to load the races. The importing archive server shall then restore the tracked races, before a comparison with the exporting source server can be carried out.

After the comparison was successful, the remote server reference from the archive server to the dedicated replication cluster can be removed, avoiding event duplication on the archive server. An Apache macro for the event imported has to be added to the central web server. Then, the ALB rules for the *-master* and public-facing sub-domain of the dedicated replication cluster can be removed, the instances terminated and the target groups deleted. The default "catch-all" rule will delegate requests for the event to the archive server where the Apache rule re-writes it accordingly.

### Archive Server Upgrade and Switch

Upon administrator request a release upgrade can be performed for the archive server. Based on the principles discussed in [Archive Servers](#archive-servers), the fail-over server will receive the new release and will have its Java process re-started. Restoring the many races can take several hours. When done, a comparison with the production archive server is carried out. Only when no differences are found and a few spot checks show reasonable results the switch is performed.

Today, these spot checks are done "manually" by looking at a few sample races and a few leaderboards. These steps will need automation.

The switch is currently performed by adjusting the rule in *sapsailing.com:/etc/httpd/conf.d/000-macros.conf* that tells the internal IP address of the production archive server, followed by a *service httpd reload* command.

### Automatic Archive Server Fail-Over

When the production archive server becomes "unhealthy" a fast switch to the fail-over archive server is required. The reasons for an unhealthy production archive server may vary. Recently, for example, we experienced an unexpected AWS-enforced and not announced reboot of the EC2 instance. Other cases may involve regressions in a release to which the archive server was upgraded.

We may use the ALB architecture and separate web servers for the two archive servers, sharing their configuration rules, to implement such an automatic fail-over. With the ALB rules implementing a precedence order, the default rule could forward to the target group containing the web server for the fail-over archive server, whereas a last-but-one rule would catch **.sapsailing.com* and would forward to the target group containing the production archive server's web server. This way, when the production archive server becomes unhealthy, the default rule will apply and will forward events to the fail-over archive server.

A severe alert shall be triggered when the production archive server becomes unavailable.

### Configure Sharding in a Replication Cluster

Sharding works by the GWT RPC requests targeting a specific leaderboard being identified by a specific URL path suffix that mentions the leaderboard to which the request is specific. This way, an ALB rule can be established that matches the specific leaderboard suffix and forwards to a dedicated target group. A replication cluster then will have more than the two typical target groups (*-master* and public-facing). The rule forwarding to the public-facing target group will act as the default after all leaderboard-specific routing rules.

The decision which and how many target groups to create shall be made based on monitoring the leaderboard re-calculation times. If the times exceed a certain threshold, and the general decision to use sharding is made by the orchestrator, the orchestrator should start by creating a target group for the leaderboard that is most expensive to calculate, considering the re-calculation frequency over some current time range such as a few minutes, as well as the duration required per re-calculation.

Depending on the number of replicas already available, the orchestrator may choose to partition the existing replica set such that after a target group and ALB rule for a specific leaderboard has been established, the replicas added to that target group will be removed from the public-facing target group. The problem with this set-up could be that then those replicas cannot serve as default target in case other replicas become unhealthy.

Better would be a complete partitioning based on all the leaderboard requests observed over a recent time range. The public-facing target group then would only serve as a default rule for requests not specific to any leaderboard, and in case target groups for specific leaderboards have no healthy targets anymore.

### Amazon Machine Image (AMI) Upgrades

The Linux installation that our images are based upon receives regular updates for all sorts of packages. Most package updates can be obtained after an instance was booted, simply by running the command *yum update*. However, kernel updates require the instance to be re-booted and this is not something we would like to have to do each time an instance needs to be started.

Instead, the AMI used to launch such instances should regularly and automatically be maintained, and there shall be a test procedure in place for the updated AMI before it is made the new default. A few older revisions may be kept as fallback, in case the tests don't catch a problem with an upgrade.

Part of the upgrade process needs to be an adjustable, parameterizable boot-up script that can understand whether a re-boot is performed as part of an image upgrade. In this case, certain actions need to be skipped, such as launching the Apache server or the Java process or patching any files based on the user details, assuming the instance were to become part of an ALB's target group.

### Add Disk Space to MongoDB

When monitoring shows that the disk volumes holding the MongoDB contents crosses a certain threshold (such as 90%) then more disk space needs to be provided automatically. [https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ebs-modify-volume.html](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ebs-modify-volume.html) explains the process with AWS and Linux tools.

Currently, the instance used to run the MongoDB processes is of type m1.large which is an "old" instance type that does not support in-place volume size changes without detaching the volume. Therefore, as a first step towards this goal the MongoDB/RabbitMQ instance should be migrated to a new instance type with similar resources, such as *m4.large*. Then, growing the file-system "in-flight" should not be a problem according to the documentation...