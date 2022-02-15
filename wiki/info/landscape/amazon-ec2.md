# Amazon EC2 for SAP Sailing Analytics

[[_TOC_]]

## Quickstart

Our default region in AWS EC2 is eu-west-1 (Ireland). Tests are currently run in the otherwise unused region eu-west-2 (London). Most regular operations can be handled through the AdminConsole's "Advanced / Landscape" tab. See, e.g., [https://security-service.sapsailing.com/gwt/AdminConsole.html#LandscapeManagementPlace:](https://security-service.sapsailing.com/gwt/AdminConsole.html#LandscapeManagementPlace:). Some operations occurring not so frequently still require more in-depth knowledge of steps, manual execution of commands on the command line and some basic Linux understanding.

## Important Servers, Hostnames

- Web Server / Central Reverse Proxy: reachable through SSH to sapsailing.com:22
- Database Servers: dbserver.internal.sapsailing.com (archive server winddb on port 10201, all other slow/archived DBs on 10202, hidden replica of "live" replica set on 10203), mongo0.internal.sapsailing.com, mongo1.internal.sapsailing.com
- RabbitMQ Server: rabbit.internal.sapsailing.com
- MySQL DB (mainly for Bugzilla): mysql.internal.sapsailing.com (currently co-deployed on the same old instance that also runs RabbitMQ, hence currently mysql.internal.sapsailing.com and rabbit.internal.sapsailing.com refer to the same instance)
- Hudson Build Server: called "Build/Test/Dev", running a Hudson instance reachable at ``hudson.sapsailing.com`` and a test instance of the SAP Sailing Analytics available under ``dev.sapsailing.com``
- Additional "Build Slaves" launched by the Hudson Build Server: Named ``Hudson Ubuntu Slave``, used to run individual build jobs

## Landscape Overview

In Route53 (the AWS DNS) we have registered the sapsailing.com domain and can manage records for any sub-domains. The "apex" record for sapsailing.com points to a Network Load Balancer (NLB), currently ``NLB-sapsailing-dot-com-f937a5b33246d221.elb.eu-west-1.amazonaws.com``, which does the following things:

* accept SSH connects on port 22; these are forwarded to the internal IP of the web server through the target group ``SSH-to-sapsailing-dot-com``, currently with the internal IP target ``172.31.28.212``
* accept HTTP connections for ``sapsailing.com:80``, forwarding them to the target group ``HTTP-to-sapsailing-dot-com`` which is a TCP target group for port 80 with ip-based targets (instance-based was unfortunately not possible for the old ``m3`` instance type of our web server), again pointing to ``172.31.28.212``, the internal IP of our web server
* accept HTTPS/TLS connections on port 443, using the ACM-managed certificate for ``*.sapsailing.com`` and ``sapsailing.com`` and also forwarding to the ``HTTP-to-sapsailing-dot-com`` target group
* optionally, this NLB could be extended by UDP port mappings in case we see a use case for UDP-based data streams that need forwarding to specific applications, such as the Expedition data typically sent on ports 2010 and following

Additionally, we have created a CNAME record for ``*.sapsailing.com`` pointing at a default application load balancer (ALB) (currently ``DefDynsapsailing-com-1492504005.eu-west-1.elb.amazonaws.com``) in our default region (eu-west-1). Thie default ALB is also called our "dynamic ALB" because it doesn't depend on DNS rules other than the default one for ``*.sapsailing.com``, so other than changes to the DNS which can take minutes to hours to propagate through the world-wide DNS, changes to the default ALB's rule set take effect immediately. Like all ALBs, this one also has a default rule that refers all traffic not matched by other rules to a target group that forwards traffic to an (in the future probably multiple) Apache httpd webserver. All these ALBs handle SSL termination by means of an ACM-managed certificate that AWS automatically renews before it expires. The traffic routed to the target groups is always HTTP only.

Further ALBs may exist in addition to the default ALB and the NLB for ``sapsailing.com``. Those will then have to have one or more DNS record(s) pointing to them for which matching rules based on the hostname exist in the ALB listener's rule set. This set-up is specifically appropriate for "longer-lived" content where during archiving or dismantling a DNS lag is not a significant problem.

### Apache httpd Webserver and Reverse Proxy

The web server currently exists only as one instance but could now be replicated to other availability zones (AZ)s, entering those other IPs into the ``HTTP-to-sapsailing-dot-com`` target group (and, as will be described further below, to the ``CentralWebServerHTTP*`` (for the "dynamic" ALB in eu-west-1) or ``{ALB-name}-HTTP`` (for all DNS-mapped ALBs) target group of each application load balancer (ALB) in the region). For all of sapsailing.com it does not (no longer) care about SSL and does not need to have an SSL certificate (anymore). In particular, it offers the following services:

* hudson.sapsailing.com - a Hudson installation on dev.internal.sapsailing.com
* bugzilla.sapsailing.com - a Bugzilla installation under /usr/lib/bugzilla
* wiki.sapsailing.com - a Gollum-based Wiki served off our git, see /home/wiki
* static.sapsailing.com - static content hosted under /home/trac/static
* releases.sapsailing.com - hub and repository for releases built by our CI infrastructure, hosted at /home/trac/releases
* jobs.sapsailing.com - a static web page, see /home/trac/static/jobs
* sail-insight.com - a static web page, with SSL/HTTPS support, hosted under /home/trac/sail-insight-website
* p2.sapsailing.com - several OSGi p2 repositories relevant for our Tycho/OSGi build and our target platform definition, hosted under /home/trac/p2-repositories
* gitlist.sapsailing.com - for our git at /home/trac/git
* git.sapsailing.com - for git cloning for dedicated users, used among other things for replication into git.wdf.sap.corp

Furthermore, it hosts aliases for ``sapsailing.com``, ``www.sapsailing.com`` and all subdomains for archived content, pointing to the archive server which is defined in ``/etc/httpd/conf.d/000-macros.conf``. This is also where the archive server switching has to be configured. Before reloading the configuration, make sure the syntax is correct, or else you may end up killing the web server, leading to downtime. Check by running
```
        apachectl configtest
```
If you see ``Syntax OK`` then reload the configuration using
```
        service httpd reload
```

The webserver is registered as target in various locations:

* As DNS record with its internal IP address (e.g., 172.31.19.129) for the two DNS entries ``logfiles.internal.sapsailing.com`` used by various NFS mounts, and ``smtp.internal.sapsailing.com`` for e-mail traffic sent within the landscape and not requiring the AWS SES
* as IP target with its internal IP address for the ``HTTP-to-sapsailing-dot-com`` target group, accepting the HTTP traffic sent straight to ``sapsailing.com`` (not ``www.sapsailing.com``)
* as IP target with its internal IP address for the ``SSH-to-sapsailing-dot-com`` target group, accepting the SSH traffic for ``sapsailing.com``
* as regular instance target in all load balancers' default rule's target group, such as ``DefDynsapsailing-com``, ``DNSMapped-0``, ``DNSMapped-1``, and so on; the names of the target groups are ``CentralWebServerHTTP-Dyn``, ``DDNSMapped-0-HTTP``, ``DDNSMapped-1-HTTP``, and so on, respectively.
* as target of the elastic IP address ``54.229.94.254``

Furthermore, it is helpful to ensure that the ``/internal-server-status`` path will resolve correctly to the Apache httpd server status page. For this, the ``/etc/httpd/conf.d/001-events.conf`` file contains three rules at the very beginning:

```
## SERVER STATUS
Use Status ec2-54-229-94-254.eu-west-1.compute.amazonaws.com internal-server-status
Use Status 172.31.19.129 internal-server-status
Use Status 127.0.0.1 internal-server-status
```

The second obviously requires maintenance as the internal IP changes, e.g., when instantiating a new Webserver copy by creating an image and restoring from the image. When upgrading / moving / copying the webserver you may try to be smart and copy the contents of ``/etc/ssh``, in particular the ``ssh_host_...`` files that contain the host keys. As you switch, users will then not have to upgrade their ``known_hosts`` file, and even internal accounts such as the Wiki account or the sailing accounts on other hosts that clone the git, or the build infrastructure won't be affected.

### DNS and Application Load Balancers (ALBs)

We distinguish between DNS-mapped and non-DNS-mapped content. The basic services offered by the web server as listed above are DNS-mapped, with the DNS entries being CNAME records pointing to an ALB (DNSMapped-0-1286577811.eu-west-1.elb.amazonaws.com) which handles SSL offloading with the Amazon-managed certificate and forwards those requests to the web server. Furthermore, longer-running application replica sets can have a sub-domain declared in Route53's DNS, pointing to an ALB which then forwards to the public and master target groups for this replica set based on hostname, header fields and request method. A default redirect for the ``/`` path can also be defined, obsoleting previous Apache httpd reverse proxy redirects for non-archived ALB-mapped content.

   <img src="/wiki/info/landscape/images/ALBsAndDNS.png"/>

Shorter-running events may not require a DNS record. The ALB ``DefDynsapsailing-com-1492504005.eu-west-1.elb.amazonaws.com`` is target for ``*.sapsailing.com`` and receives all HTTP/HTTPS requests not otherwise handled. While HTTP immediately redirects to HTTPS, the HTTPS requests will pass through its rules. If application replica sets have their rules declared here, they will fire. Everything else falls through to the default rule which forwards to the web server's target group again. This is how archived events as well as requests for ``www.sapsailing.com`` end up.

The requests going straight to ``sapsailing.com`` are handled by the NLB (see above), get forwarded to the web server and are re-directed to ``www.sapsailing.com`` from there, ending up at the non-DNS-mapped load balancer where by default they are then sent again to the web server / reverse proxy which sends it to the archive server.

In addition to a default re-direct for the "/" path, the following four ALB listener rules for a single application replica set are defined, all requiring the "Host" to match the hostname:
- if the HTTP header ``X-SAPSSE-Forward-Request-To`` is ``master`` then forward to the master target group
- if the HTTP header ``X-SAPSSE-Forward-Request-To`` is ``replica`` then forward to the public target group
- if the request method is ``GET`` then forward to the public target group
- forward all other request for the hostname to the master target group

### MongoDB Replica Sets

There are currently three MongoDB replica sets:

   <img src="/wiki/info/landscape/images/MongoDBReplicaSets.png"/>

- ``live``: Used by default for any new event or club server. The replica set consists of three nodes, two of which running on instances with fast but ephemeral NVMe storage for high write throughput, thus eligible as primary nodes; and a hidden replica with a slower EBS gp2 SSD volume that has a backup plan. The two NVMe-backed nodes have DNS names pointing to their internal IP addresses: ``mongo0.internal.sapsailing.com`` and ``mongo1.internal.sapsailing.com``. Their MongoDB processes run on the default port 27017 each. They run in different availability zones. The hidden replica runs on ``dbserver.internal.sapsailing.com:10203``.
- ``archive``: Used by the ARCHIVE servers (production and failover). It host a DB called ``winddb`` (for historical reasons). Its primary and by default only node is found on ``dbserver.internal.sapsailing.com:10201``. If an ARCHIVE server is launched it is a good idea to scale this ``archive`` replica set by adding one or two secondary nodes that are reasonably sized, such as ``i3.2xlarge``. Note that the ARCHIVE server configuration prefers reading from secondary MongoDB instances, thus will prefer any newly launched node over the primary.
- ``slow``: Used as target for archiving / backing up content from the ``live`` replica set once it is no longer needed for regular operations. The default node for this replica set can be found at ``dbserver.internal.sapsailing.com:10202`` and has a large (currently 4TB) yet slow and inexpensive sc1 disk attached. One great benefit of this replica set is that in case you want to resurrect an application replica set after it has been archived, you can do so with little effort, simply by launching an instance with a DB configuration pointing at the ``slow`` replica set.

Furthermore, every application server instance hosts a local MongoDB process, configured as a primary of a replica set called ``replica``. It is intended to be used by application replica processes running on the instance, scaling with the number of replicas required, starting clean and empty and getting deleted as the instance is terminated. Yet, being configured as a MongoDB replica set there are powerful options available for attaching more MongoDB instances as needed, or upgrading to a new MongoDB release while remaining fully available, should this ever become an issue for longer-running replicas.

### Shared Security and Application Data Across ``sapsailing.com``

Staying logged in and having a common underlying security infrastructure as users roam around the sapsailing.com landscape is an important feature of this architecture. This is achieved by using the same replication scheme that is applied when an application replica set replicates its entire content between its master and all its replicas, with a small modification: the replication between an application replica set's master and a "singleton" security environment is only partial in the sense that not all replicables available are actually replicated. Instead, replication from the central security service is restricted currently to three replicables:

- ``com.sap.sse.security.impl.SecurityServiceImpl``
- ``com.sap.sailing.shared.server.impl.SharedSailingDataImpl``
- ``com.sap.sse.landscape.aws.impl.AwsLandscapeStateImpl``

The central security service is provided by a small application replica set reachable under the domain name ``security-service.sapsailing.com``. It currently employs only a single master process running on a small dedicated instance. It launches into ready state within just a few seconds, and hence even upgrades may be performed in-place. The replication infrastructure is built such that when the securit-service master comes up again it knows which replicables were replicating it recently. Furthermore, replicas will buffer operations that are to be sent to the master as long as the master is not available. They will re-send them once the master has become available again.

This default replication relationship for any regular application replica set and ARCHIVE servers is encoded currently in the environment [https://releases.sapsailing.com/environments/live-master-server](https://releases.sapsailing.com/environments/live-master-server) and [https://releases.sapsailing.com/environments/archive-server](https://releases.sapsailing.com/environments/archive-server).

### Standard Application Replica Set with Target Groups, Auto-Scaling Group and Launch Configuration

With the exception of legacy, test and archive instances, regular application replica sets are created with the following elements:
- an application load balancer (ALB) is identified or created if needed
- a "master" target group, named like the replica set with a "-m" suffix appended
- a "public" target group, named after the replica set
- five rules for the replica set are created in the load balancer's HTTPS listener, forwarding traffic as needed to the "master" and the "public" target groups
- for a DNS-mapped set-up (not using the default "dynamic" load balancer) a Route53 DNS CNAME record pointing to the ALB is created for the replica set's host name
- an auto-scaling group, named after the replica set with the suffix "-replicas" appended
- a launch configuration used by the auto-scaling group, named after the replica set with the release name appended, separated by a dash (-), e.g., "abc-build-202202142355"
- a master process, registered in both, the "master" and the "public" target groups
- a replica process, registered in the "public" target group
There are different standard deployment choices for the master and the replica process that will be described in the following sections.

### Dedicated Application Replica Set

For an event that expects more than a few hundred concurrent viewers the standard set-up includes dedicated instances for master and replica processes. Each application process (a Java VM) can consume most of the physical RAM for its heap size. No memory contention with other application processes will occur, and Java garbage collection will not run into memory swapping issues. All replicas in this set-up are managed by the auto-scaling group. Their instances will be named as "SL {replicaset-name} (Auto-Replica)". The master will be named "SL {replicaset-name} (Master)". The ``sailing-analytics-server`` tag on the instance will reflect the replica set's name in its value.

   <img src="/wiki/info/landscape/images/DedicatedApplicationReplicaSet.png"/>

As replicas may get added as the load increases, we would like to avoid them putting additional stress on the MongoDB replica set used by the master process. Therefore, each instance runs its own small MongoDB installation, configured as a replica set ``replica``. The DB content on a replica is said to be "undefined" and it is not intended to be used to read anything reasonable from it. Yet, we're trying to make the launch of a replica robust against reading from such a replica DB with undefined content before the actual initial load is received from the master process.

### Application Replica Set Using Shared Instances

When an event is expected to produce less than hundreds of concurrent viewers and when the number of competitors to managed in a single leaderboard is less than approximately 50, master and replica processes of the corresponding application replica set may be deployed on instances shared with other application replica sets. This becomes even more attractive if the application replica sets sharing instances are not expected to produce high workload at the same time. The highest load is to be expected when races are live. For most non-long-distance races the times are constrained to a part of the daylight time in the time zone where the races are happening. Should there be multiple such events in different time zones then those would be good candidates for sharing instances as their load patterns are complementary.

Likewise, if an application replica set is set up for a multi-day event and after the last race is over still has some read load to carry that would be more than would be good for an archive server, yet less than what dedicated instances are required for, leaving the event on an application replica set sharing instances with others may be a good idea. Similarly, event series such as a sailing league's season, are usually configured on an application replica set, and in the times between the events of the series the much lower workload makes the application replica set eligible for moving to shared instances.

In principle, every application instance can host more than one application process. It is, however, essential that their file system directories and port assignments don't overlap. Also, a load balancing target group always routes traffic to the same port on all instances registered. Therefore, the master process and all replica processes of a single application replica set must use the same HTTP port. The default port range for the HTTP ports used by application replica sets starts at ``8888``. Telnet ports start at ``14888``, and UDP Expedition ports at ``2010``.

All replica processes running on the same instance share the instance's local MongoDB (the single-node ``replica`` replica set), and for each replica a database named ``{replicaset-name}-replica`` will end up on the local MongoDB. The master processes are expected to use a non-local MongoDB replica set, such as the ``live`` replica set.

   <img src="/wiki/info/landscape/images/ApplicationReplicaSetsOnSharedInstances.png"/>

By means of several automation procedures it is possible with a few clicks or a single REST API call to move master and replica processes from one instance to another and to launch instances for dedicated and for shared use. With this, a reasonable degree of elasticity is provided that allows operators to move from shared to dedicated set-ups as an event is expected to generate more CPU load soon, and moving things back to a shared set-up when the load pressure decreases.

Shared instances are usually named "SL Multi-Server" and have the value "___multi___" for the ``sailing-analytics-server`` tag.

### Archive Server Set-Up

The set-up for the "ARCHIVE" server differs from the set-up of regular application replica sets. While for the latter we assume that a single sub-domain (such as ``vsaw.sapsailing.com``) will reasonably identify the content of the application replica set, the archive needs to fulfill at least two roles:

- keep content available under the original URL before archiving
- make all content browsable under the landing page ``https://sapsailing.com``

Furthermore, being the landing page for the entire web site, a concept for availability is required, given that an archive server restart can take up to 24 hours until all content is fully available again. Also, with a growing amount of content archived, more and more hostnames will need to be mapped to specific archived content. While ALBs incur cost and the number of ALBs and the size of their listeners' rule sets are limited, an HTTP reverse proxy such as the Apache httpd server is not limited in the number of rewrite rules it has configured. Archived events are usually not under high access load. The bandwidth required for all typical access to archived events is easily served by a single instance and can easily pass through a single reverse proxy.

For now, we're using the following set-up:

   <img src="/wiki/info/landscape/images/ArchiveServerSetup.png"/>

A failover instance is kept ready to switch to in case the primary production archive process is failing. The switching happens in the reverse proxy's configuration, in particular the ``/etc/httpd/conf.d/000-macros.conf`` file and its ``ArchiveRewrite`` macro telling the IP address of the current production archive server. In case of a failure, change the IP address to the internal IP address of the failover archive server and re-load the httpd configuration:

```
        service httpd reload
```

### Important Amazon Machine Images (AMIs)

TODO talk about image upgradability using the "image-upgrade" user data line, optionally combined with the "no-shutdown" flag.

### AWS Tags

AMIs / image-type and versions

sailing-analytics-server

mongo-replica-sets

## Automated Procedures

### Creating a New Application Replica Set

### Moving Application Replica Set from Shared to Dedicated Infrastructure

### Moving Application Replica Set from Dedicated to Shared Infrastructure

### Scaling Replica Instances Up/Down

### Scaling Master Up/Down

### Upgrading Application Replica Set

### Archiving Application Replica Set

### Removing Application Replica Set

### Upgrading Application AMI

TODO explain how launch configurations can be upgraded as well

### Upgrading MongoDB AMI

#### Starting an instance

To start with, your user account needs to have sufficient permissions to create a new server group ``{NEWSERVERNAME}-server`` up-front so that you have at least the permissions granted by the ``user`` role for all objects owned by that group. Change the group's group ownership so that the new group is its own group owner. Additionally, in order to have the new server participate in the shared security service and shared sailing data service on ``security-service.sapsailing.com`` your user needs ``SERVER:REPLICATE:security-service``. Your user should also have the ``SERVER:*:{NEWSERVERNAME}`` permission (e.g., implied by the more general ``SERVER:*`` permission), e.g., granted by the ``server_admin`` role. The latter permission is helpful in order to be able to configure the resulting server and to set up replication for it. If your user account currently does not have those permissions, find an administrator who has at least ``SERVER:*`` which is implied in particular by having role ``server_admin:*``. Such an administrator will be able to grant you the ``SERVER``-related permissions described here.

Now start by creating the new server group, named ``{NEWSERVERNAME}-server``. So for example, if your server will use ``SERVER_NAME=abc`` then create a user group called ``abc-server``. You will yourself be a member of that new group automatically. Add role ``user`` to the group, enabling it only for the members of the group ("Enabled for all users" set to "No"). This way, all members of the group will gain permissions for objects owned by that server as if they owned them themselves. This also goes for the new ``SERVER`` object, but owners only obtain permissions for default actions, not the dedicated ``SERVER`` actions.

Now choose the instance type to start. For example:
  - Archive server: i3.2xlarge
  - Live event: c4.2xlarge

You may need to select "All generations" instead of "Current generation" to see these instance configurations. Of course, you may choose variations of those as you feel is appropriate for your use case.

Using a release, set the following in the instance's user data, replacing `myspecificevent` by a unique name of the event or series you'll be running on that instance, such as `kielerwoche2014` or similar. Note that when you select to install an environment using the `USE_ENVIRONMENT` variable, any other variable that you specify in the user data, such as the `MONGODB_URI` or `REPLICATION_CHANNEL` properties in the example above, these additional user data properties will override whatever comes from the environment specified by the `USE_ENVIRONMENT` parameter.

A typical set-up for a master node could look like this:

```
INSTALL_FROM_RELEASE=(name-of-release)
USE_ENVIRONMENT=live-master-server
SERVER_NAME=myspecificevent
# Provide authentication credentials for a user on security-service.sapsailing.com permitted to replicate, either by username/password...
#REPLICATE_MASTER_USERNAME=(user for replicator login on security-service.sapsailing.com server having SERVER:REPLICATE:&lt;server-name&gt; permission)
#REPLICATE_MASTER_PASSWORD=(password of the user for replication login on security-service.sapsailing.com)
# Or by bearer token, obtained, e.g., through
#   curl -d "username=myuser&password=mysecretpassword" "https://security-service.sapsailing.com/security/api/restsecurity/access_token" | jq .access_token
# or by logging in to the security-service.sapsailing.com server using your web browser and then navigating to
#     https://security-service.sapsailing.com/security/api/restsecurity/access_token
REPLICATE_MASTER_BEARER_TOKEN=(a bearer token allowing this master to replicate from security-service.sapsailing.com)
EVENT_ID={some-uuid-of-an-event-you-want-to-feature}
SERVER_STARTUP_NOTIFY=you@email.com
```

This will use the default "live" MongoDB replica set with a database named after the `SERVER_NAME` variable, and with an outbound RabbitMQ exchange also named after the `SERVER_NAME` variable, using the default RabbitMQ instance in the landscape for replication purposes, and based on the `live-master-server` environment will start replicating the SecurityService as well as the SharedSailingData service from the central `security-service.sapsailing.com` instance. Furthermore, a reverse proxy setting for your `EVENT_ID` will be created, using `${SERVER_NAME}.sapsailing.com` as the hostname for the mapping.

More variables are available, and some variables---if not set in the environment specified by `USE_ENVIRONMENT` nor in the user data provided when launching the instance---have default values which may be constants or may be computed based on values of other variables, most notably the `SERVER_NAME` variable. Here is the list:

* `SERVER_NAME`
    used to define the server's name. This is relevant in particular for the user group
    created/used for all new server-specific objects such as the `SERVER` object itself. The group's
    name is constructed by appending "-server" to the server name. This variable furthermore provides the default value for a few other settings, including the default hostname mapping `${SERVER_NAME}.sapsailing.com` for any series or event specified, the database name in the default `MONGODB_URI`, as well as the default name for the outbound RabbitMQ replication exchange `REPLICATION_CHANNEL`.

* `INSTALL_FROM_RELEASE` The user data variable to use to specify the release to install and run on the host. Typical values are `live-master-server` and `live-replica-server`, used to start a master or a replica server, respectively, or `archive-server` for launching an "ARCHIVE" server.
 
* `MONGODB_URI`
    used to specify the MongoDB connection URI; if neither this variable nor `MONGODB_HOST` are specified, a default MongoDB URI will be constructed as `mongodb://mongo0.internal.sapsailing.com,mongo1.internal.sapsailing.com/${SERVER_NAME}?replicaSet=live&retryWrites=true&readPreference=nearest` for a server that does not set the `AUTO_REPLICATE` variable, and to `mongodb://mongo0.internal.sapsailing.com,mongo1.internal.sapsailing.com/${SERVER_NAME}-replica?replicaSet=live&retryWrites=true&readPreference=nearest` for a server that does set the `AUTO_REPLICATE` variable to a non-empty value such as `true`.

* `REPLICATION_CHANNEL`
    used to define the name of the RabbitMQ exchange to which this master node
    will send its operations bound for its replica nodes. The replica-side counterpart for this is
    `REPLICATE_MASTER_EXCHANGE_NAME`. Defaults to `${SERVER_NAME}` if no automatic replication is
    requested using the `AUTO_REPLICATE` variable,  otherwise to `${SERVER_NAME}-${INSTANCE_NAME}` which
    provides a separate "transitive" replication channel for each replica.

* `REPLICATION_HOST`
    hostname or IP address of the RabbitMQ node that this master process will use for outbound replication. Defaults to `rabbit.internal.sapsailing.com`.

* `REPLICATION_PORT`
    the port used by this master process to connect to RabbitMQ for outbound replication. Using 0 (the default)
    will use the default port as encoded in the RabbitMQ driver. 

* `SERVER_PORT`
    The port on which the built-in web server of an application server process can be reached using HTTP. Defaults to 8888.

* `TELNET_PORT`
    The port on which the OSGi console of a server process can be reached. Defaults to 14888.

* `EXPEDITION_PORT`
    The port on which the application server will listen for incoming UDP packets, usually then forwarded to the Expedition receiver for wind and other Expedition-based sensor data. Defaults to 2010.
    
* `SERVER_STARTUP_NOTIFY`
    defines one or more comma-separated e-mail addresses to which a notification will
    be sent after the server has started successfully.

* `USE_ENVIRONMENT`
    defines the environment file (stored at `http://releases.sapsailing.com/environments`) which provides default combinations of variables. The most frequently used environments are probably `live-master-server` which configures the replication of the `SecurityService` and `SharedSailingData` from `security-service.sapsailing.com`, leading to a centralized user management and allows for sharing sailing-related data across server clusters such as course templates. The `live-replica-server` environment will pre-configure replication from a master for all replicable services required for the SAP Sailing Analytics, by setting the `AUTO_REPLICATE` variable to `true`. The `archive-server` environment contains pre-configured memory and database settings for the archive server set-up and, like a regular master server, replicates the central security service.

* `REPLICATE_MASTER_SERVLET_HOST`
    the host name or IP address where a replica can reach the master node in order to
    request the initial load, register, un-register, and send operations for reverse replication to.
    The value is always combined with that of the `REPLICATE_MASTER_SERVLET_PORT` variable which
    provides the port for this communication. Defaults to `${SERVER_NAME}.sapsailing.com`, assuming that
    this maps to a load balancer that identifies requests bound for the master instance of an
    application server replica set and routes them to the master accordingly. Note in this context how with `EVENT_HOSTNAME`
    and `SERIES_HOSTNAME` the reverse proxy mappings may be adjusted to use alternative or additional
    hostname mappings.

* `REPLICATE_MASTER_SERVLET_PORT`
    the port number where a replica can reach the master node in order to
    request the initial load, register, un-register, and send operations for reverse replication to.
    The value is always combined with that of the `REPLICATE_MASTER_SERVLET_HOST` variable which
    provides the host name / IP address for this communication. Defaults to 443.

* `REPLICATE_MASTER_EXCHANGE_NAME`
    the name of the RabbitMQ exchange to which the master sends operations for fan-out
    distribution to all replicas, and that therefore a replica has to attach a queue to in order to receive
    those operations. Specified on a replica. The master-side counterpart is `REPLICATION_CHANNEL`. Defaults
    to `${SERVER_NAME}` which has been the default for the corresponding master based on its `${SERVER_NAME}`
    which is assumed to be equal to the `${SERVER_NAME}` setting used to launch this replica.

* `REPLICATE_MASTER_QUEUE_HOST`
    the RabbitMQ host name that this replica will connect to in order to connect a queue to the
    fan-out exchange whose name is provided by the `REPLICATE_MASTER_EXCHANGE_NAME` variable. Used
    in conjunction with the `REPLICATE_MASTER_QUEUE_PORT` variable. Defaults to `rabbit.internal.sapsailing.com`.

* `REPLICATE_MASTER_QUEUE_PORT`
    the RabbitMQ port that this replica will connect to in order to connect a queue to the fan-out
    exchange whose name is provided by the `REPLICATE_MASTER_EXCHANGE_NAME` variable. Defaults to 0 which
    instructs the driver to use the Rabbit default port (usually 5672) for connecting. Used in conjunction with the
    `REPLICATE_MASTER_QUEUE_HOST` variable.

* `REPLICATE_ON_START`
    specifies the IDs (basically the fully-qualified class names) of those Replicables to
    start replicating when the server process starts. The process using this will become a replica for those
    replicables specified with this variable, and it will replicate the master node described by
    `REPLICATE_MASTER_SERVLET_HOST` and `REPLICATE_MASTER_SERVLET_PORT` and receive the operation
    feed through the RabbitMQ exchange configured by `REPLICATE_MASTER_EXCHANGE_NAME`.

* `AUTO_REPLICATE`
    If this variable has a non-empty value (e.g., "true"), `REPLICATE_ON_START` will default to the set of replicable IDs required by an SAP Sailing Analytics replica instance. Any value provided for `REPLICATE_ON_START` in the environment selected by `USE_ENVIRONMENT` or in the user data provided at instance start-up will take precedence, though.

* `REPLICATE_MASTER_BEARER_TOKEN`
    used to specify which bearer token to use to authenticate at the master
    in case this is to become a replica of some sort, e.g., replicating the SecurityService
    and the SharedSailingData service. Use alternatively to `REPLICATE_MASTER_USERNAME/REPLICATE_MASTER_PASSWORD`.

* `REPLICATE_MASTER_USERNAME, REPLICATE_MASTER_PASSWORD`
    used to specify the user name and password for authenticating at the master
    in case this is to become a replica of some sort, e.g., replicating the SecurityService
    and the SharedSailingData service. Use alternatively to `REPLICATE_MASTER_BEARER_TOKEN`.

* `MEMORY`
    Specifies the value to which both, minimum and maximum heap size for the Java VM used to run the application will be set. As of this writing it defaults to "6000m" (6GB). During instance boot-up, a default value is calculated based on the instance's physical memory available, not considering swap space, and appended to the env.sh file. Therefore, auto-installed application processes will never use this "6000m" default. Specifying `MEMORY` in the user data will override the default size computed by the boot script.

* `MAIL_FROM`
    The address to use in the "From:" header field when the application sends e-mail.

* `MAIL_SMTP_HOST`
    The SMTP host to use for sending e-mail. The standard image has a pre-defined file under `/root/mail.properties` which contains credentials and configuration for our standard Amazon Simple Email Service (AWS SES) configuration. It is copied to the `configuration/` folder of a default server process installed during start-up by the `sailing` init script.
    
* `MAIL_SMTP_PORT`
    The SMTP port to use for sending e-mail. The standard image has a pre-defined file under `/root/mail.properties` which contains credentials and configuration for our standard Amazon Simple Email Service (AWS SES) configuration. It is copied to the `configuration/` folder of a default server process installed during start-up by the `sailing` init script.

* `MAIL_SMTP_AUTH`
    `true` or `false`; defaults to `false` and tells whether or not to authenticate a user to the SMTP server using the `MAIL_SMTP_USER` and `MAIL_SMTP_PASSWORD` variables. The standard image has a pre-defined file under `/root/mail.properties` which contains credentials and configuration for our standard Amazon Simple Email Service (AWS SES) configuration and hence defaults this variable to `true`. It is copied to the `configuration/` folder of a default server process installed during start-up by the `sailing` init script.

* `MAIL_SMTP_USER`
    Username for SMTP authentication; used if `MAIL_SMTP_AUTH` is `true`. The standard image has a pre-defined file under `/root/mail.properties` which contains credentials and configuration for our standard Amazon Simple Email Service (AWS SES) configuration. It is copied to the `configuration/` folder of a default server process installed during start-up by the `sailing` init script.

* `MAIL_SMTP_PASSWORD`
    Password for SMTP authentication; used if `MAIL_SMTP_AUTH` is `true`. The standard image has a pre-defined file under `/root/mail.properties` which contains credentials and configuration for our standard Amazon Simple Email Service (AWS SES) configuration. It is copied to the `configuration/` folder of a default server process installed during start-up by the `sailing` init script.

* `EVENT_ID`
    Used to specify one or more UUIDs of events for which to create a reverse proxy mapping in `/etc/httpd/conf.d/${SERVER_NAME}.conf`. If only a single event ID is specified, as in ``EVENT_ID=34ebf96f-594b-4948-b9ea-e6074107b3e0`` then the `${EVENT_HOSTNAME}` is used as the hostname, or if `EVENT_HOSTNAME` is not specified, defaulting to `${SERVER_NAME}.sapsailing.com`, and a mapping using the `Event-SSL` macro is performed. The variable can also be used in Bash Array notation to specify more than one event ID, as in ``EVENT_ID[0]=34ebf96f-594b-4948-b9ea-e6074107b3e0`` and then ``EVENT_HOSTNAME[0]=...`` would specify the corresponding hostname (again defaulting to `${SERVER_NAME}.sapsailing.com`), followed by ``EVENT_ID[1]=...`` and then optionally ``EVENT_HOSTNAME[1]=...``, and so on. If neither `EVENT_ID` nor `SERIES_ID` is specified, a default reverse proxy mapping for the server process will be created using the `Home-SSL` macro which redirects requests for the base URL (`${SERVER_NAME}.sapsailing.com`) to the `/gwt/Home.html` entry point. 

* `EVENT_HOSTNAME`
    If specified, overrides the `${SERVER_NAME}.sapsailing.com` default for reverse proxy mappings requested by providing event IDs in the `EVENT_ID` variable. If array notation is used for `EVENT_ID` then so should it for `EVENT_HOSTNAME`.

* `SERIES_ID`
    Used to specify one or more UUIDs of event series (league seasons) for which to create a reverse proxy mapping in `/etc/httpd/conf.d/${SERVER_NAME}.conf`. If only a single event ID is specified, as in ``SERIES_ID=34ebf96f-594b-4948-b9ea-e6074107b3e0`` then the `${SERIES_HOSTNAME}` is used as the hostname, or if `SERIES_HOSTNAME` is not specified, defaulting to `${SERVER_NAME}.sapsailing.com`, and a mapping using the `Series-SSL` macro is performed. The variable can also be used in Bash Array notation to specify more than one event ID, as in ``SERIES_ID[0]=34ebf96f-594b-4948-b9ea-e6074107b3e0`` and then ``SERIES_HOSTNAME[0]=...`` would specify the corresponding hostname (again defaulting to `${SERVER_NAME}.sapsailing.com`), followed by ``SERIES_ID[1]=...`` and then optionally ``SERIES_HOSTNAME[1]=...``, and so on. If neither `EVENT_ID` nor `SERIES_ID` is specified, a default reverse proxy mapping for the server process will be created using the `Home-SSL` macro which redirects requests for the base URL (`${SERVER_NAME}.sapsailing.com`) to the `/gwt/Home.html` entry point.

* `SERIES_HOSTNAME`
    If specified, overrides the `${SERVER_NAME}.sapsailing.com` default for reverse proxy mappings requested by providing event IDs in the `SERIES_ID` variable. If array notation is used for `SERIES_ID` then so should it for `SERIES_HOSTNAME`.

* `BUILD_COMPLETE_NOTIFY`
    The comma-separated list of e-mail addresses to send a notification message to after a release has been installed or built from sources (this happens within the `refreshInstance.sh` script).

* `SERVER_STARTUP_NOTIFY`
    The comma-separated list of e-mail addresses to send a notification message to after a server process has been launched.

* `image-upgrade`
    If provided in a line of its own, the `httpd` server on the instance will be stopped, no application server release will be installed, the operating system packages will be updated, the git repository under `/home/sailing/code` will be pulled for the branch that the workspace is checked out on for the image launched (usually `master`) which will update various scripts relevant for the bootstrapping process, all log directories for `httpd` and the application server will be cleared, and by default the instance will then be shut down for a new AMI to be created for it. See also the `no-shutdown` user data option.

* `no-shutdown`
    If provided in conjunction with the `image-upgrade` option, also on a line of its own, after performing the `image-upgrade` actions the instance will be kept running. This way, you may still log on using SSH and make further adjustments if needed before you create the new image.

Have at least a public-facing target group ready. If you want to expose the master to the public (single-instance scenario or master-replica scenario where the master also handles reading client requests) add the master to the public target group. Either ensure that your add rules to the "default" dynamic Application Load Balancer (ALB) to which the wildcard DNS rule points (`*.sapsailing.com`), or add the rules to a dedicated ALB and create a Route53 DNS CNAME entry pointing to that ALB's DNS name.

If you want to launch one or more replicas, ensure you have a dedicated ``...-master`` target group to which you add your master instance, and a load balancer rule that forwards your replica's requests directed to the master to that ``...-master`` target group, for example, by using a dedicated ``...-master`` hostname rule in your load balancer which then forwards to the ``...-master`` target group.	

After your master server is ready, route the replica requests to the master through the load balancer again. This is also the default if you set up a replica. If you don't want to use the credentials of your own user account (which is expected to have permission ``SERVER:REPLICATE:{SERVERNAME}`` already because as described above you need this for configuring the new server), e.g., because you then have to expose an access token in the environment that anyone with SSH access to the instance may be able to see, set up a new user account, such as ``{SERVERNAME}-replicator``, that has the following permission: ``SERVER:REPLICATE:{SERVERNAME}`` where ``{SERVERNAME}`` is what you provided above for the ``SERVER_NAME`` environment variable. You will be able to grant this permission to the new user because your own user account is expected to have this permission. You will need your own or this new user's credentials to authenticate your replicas for replication.

Make sure to use the preconfigured environment from `http://releases.sapsailing.com/environments/live-replica-server`. If you don't add the line `REPLICATE_MASTER_SERVLET_HOST` to the user-data and adjust the `myspecificevent` master exchange name in the replica's ``REPLICATE_MASTER_EXCHANGE_NAME`` variable to the value of the ``REPLICATION_CHANNEL`` setting you used for the master configuration, all default to the `${SERVER_NAME}.sapsailing.com` and `${SERVER_NAME}`, respectively.  Also ensure that you provide the ``REPLICATE_MASTER_BEARER_TOKEN`` value (or, alternatively ``REPLICATE_MASTER_USERNAME`` and ``REPLICATE_MASTER_PASSWORD``) to grant the replica the permissions it needs to successfully register with the master as a replica.

```
INSTALL_FROM_RELEASE=(name-of-release)
USE_ENVIRONMENT=live-replica-server
# Provide authentication credentials for a user on the master permitted to replicate, either by username/password...
#REPLICATE_MASTER_USERNAME=(user for replicator login on master server having SERVER:REPLICATE:&lt;server-name&gt; permission)
#REPLICATE_MASTER_PASSWORD=(password of the user for replication login on master)
# Or by bearer token, obtained, e.g., through
#   curl -d "username=myuser&password=mysecretpassword" "https://master-server.sapsailing.com/security/api/restsecurity/access_token" | jq .access_token
# or by logging in to the master server using your web browser and then navigating to
#     https://master-server.sapsailing.com/security/api/restsecurity/access_token
REPLICATE_MASTER_BEARER_TOKEN=(a bearer token allowing this master to replicate from your master)
SERVER_NAME=myspecificevent
EVENT_ID={some-uuid-of-an-event-you-want-to-feature}
SERVER_STARTUP_NOTIFY=you@email.com
```

This will automatically start replication from your master which is assumed to be reachable at `${SERVER_NAME}.sapsailing.com`. Adjust `REPLICATE_MASTER_SERVLET_HOST` and `REPLICATE_MASTER_SERVLET_PORT` accordingly if this is not the case. The RabbitMQ exchange to subscribe to is also defaulted with the `${SERVER_NAME}`, just like is the case for the outbound side on the master, defining this exchange. Each replica gets its own outbound RabbitMQ exchange by default, using the `${SERVER_NAME}` to which the replica's Amazon instance ID is appended, in case transitive replication should become a need. The database connection string (`MONGODB_URI`) defaults to master's DB with the database name extended by the suffix `-replica`.

#### Upgrading an application server replica set with Auto-Scaling Group and Launch Configuration in Place

* prepare local `/etc/hosts` (on Windows `c:/windows/system32/drivers/etc/hosts`) entries for master and replica with an `.sapsailing.com` suffix in order to allow for logging in to the respective server's admin console
* if your master will need to reload a lot of tracking data from MongoDB, spin up MongoDB replicas as needed and wait for them to leave the `STARTUP2` phase, becoming `SECONDARY` replicas in their replica set
* remove master from public target group
* ssh to master, invoke `refreshInstance.sh` to prepare for master upgrade
* remove master from master target group
* log on to replica's `/gwt/AdminConsole.html` page, go to "Advanced" / "Replication" and stop replication; to this manually for all replicas in this application server replica set
* restart the master's application process (`./stop; sleep 5; ./start`)
* while your master spins up, follow its `/gwt/status` page
* while your master spins up, prepare a new Launch Configuration for the replicas that uses the new release; for this, find out which release your new master is running (e.g., `build-202012211912`), copy your existing Launch Configuration to one with a new name that reflects the new release, and edit the new Launch Configuration's "User Data" section, adjusting the `INSTALL_FROM_RELEASE` variable to the new release name. You find the "User Data" in the section "Additional configuration - optional" after expanding the "Advanced details" drop-down. Acknowledge your key at the bottom and save the new launch configuration.
* as your new master's `/gwt/status` response tells you that the new master process is available again, add it to the master target group and the public target group again; in times of low load where you may afford going to zero replicas in your public target group, remove all replicas from the public target group; otherwise, before this step you would first need to spin up one or more replicas for your new release to replace all old replicas in the public target group in one transaction.
* update the Auto-Scaling Group so it uses your new Launch Configuration
* you may want to wait until your master's `/gwt/status` shows it has finished loading all races before starting new replicas on it; this way you can avoid huge numbers of replication operations that would be necessary to replicate the loading process fix by fix from the master to all replicas; it is a lot more efficient to transfer the result of loading all races in one sweep during the initial load process when a replica attaches after loading has completed
* terminate all existing replicas running the old release; the Auto-Scaling Group will launch as many replicas as you configured after a minute or two and will automatically assign them to the public target group
* don't forget to terminate the MongoDB replicas again that you spun up before specifically for this process

#### Setting up a new image (AMI) from scratch (more or less)

See [here](/wiki/creating-ec2-image-from-scratch)

#### Receiving wind from Expedition

- To receive and forward wind with an Expedition connector, log into webserver as user trac and switch to $HOME/servers/udpmirror. Start the mirror and forward it to the instance you want. In order to receive wind through the Igtimi connector, this step is not required as the wind data is received directly from the Igtimi server.

#### Setting up a Multi Instance
To set up a multi instance for a server with name "SSV", subdomain "ssv.sapsailing.com" and description "Schwartauer Segler-Verein, [www.ssv-net.de](http://www.ssv-net.de), Alexander Probst, [webmaster@alexprobst.de](mailto:webmaster@alexprobst.de)" perform the following steps:

##### Instance configuration

1. Connect to the EC2 instance where your multi instance should be deployed. For example: Connect to the instance "SL Multi-Instance Sailing Server" with dns name  "ec2-34-250-136-229.eu-west-1.compute.amazonaws.com" in region Ireland via SSH.

   <pre>
   ssh sailing@ec2-34-250-136-229.eu-west-1.compute.amazonaws.com
   </pre>

2. Navigate to the directory /home/sailing/servers.

   <pre>
   cd /home/sailing/servers
   </pre>

3. Create a new directory with name "ssv".

   <pre>
   mkdir ssv
   </pre>

4. Copy the file /home/sailing/code/java/target/refreshInstance.sh to your new directory.

   <pre>
   cp /home/sailing/code/java/target/refreshInstance.sh ssv
   </pre>

5. Initialize a new environment variable "DEPLOY_TO" with the name of the directory.

   <pre>
   export DEPLOY_TO=ssv
   </pre>

6. Execute the refreshInstance.sh script with your desired release build version from releases.sapsailing.com.

   <pre>
   ./refreshInstance.sh install-release build-201712270844
   </pre>

7. Once the script finished, uncomment the following lines in your env.sh file.

   <pre>
   # Uncomment for use with SAP JVM only:

   ADDITIONAL_JAVA_ARGS="$ADDITIONAL_JAVA_ARGS-XX:+GCHistory -XX:GCHistoryFilename=logs/sapjvm_gc@PID.prf"
   </pre>

   Afterwards comment out the line where it says "JAVA_HOME=/opt/jdk1.8.0_20" 

   <pre>
   # JAVA_HOME=/opt/jdk1.8.0_20
   </pre>
   Optional: setup event management URL by setting <pre>com.sap.sailing.eventmanagement.url</pre> system property. See ADDITIONAL_JAVA_ARGS at <pre>env.sh</pre>.

8. White label switch, uncomment this line in env.sh
   <pre>
   #ADDITIONAL_JAVA_ARGS="$ADDITIONAL_JAVA_ARGS -Dcom.sap.sse.debranding=true"
   </pre>
   to enable white labeling.

9. Anniversary switch,  uncomment this line in env.sh
   <pre>
   #ADDITIONAL_JAVA_ARGS="$ADDITIONAL_JAVA_ARGS -DAnniversaryRaceDeterminator.enabled=true"
   </pre>
   to enable anniversary calculation.

10. Find the next unused ports for the variables SERVER_PORT, TELNET_PORT and EXPEDITION_PORT. You can do this by extracting all existing variable assignments from all env.sh files within the /home/sailing/servers directory. 

   <pre>
   for i in /home/sailing/servers/*/env.sh; do cat $i | grep "^ *SERVER_PORT=" | tail -1 | tr -d "SERVER_PORT="; done | sort -n
   </pre>

   Do this for TELNET_PORT and EXPEDITION_PORT likewise.

   If this is the first multi instance on the server, use the values SERVER_PORT=8888, TELNET_PORT=14888, EXPEDITION_PORT=2010.

11. Append the following variable assignments to your env.sh file.
   <pre>
   SERVER_NAME=SSV
   TELNET_PORT=14888
   SERVER_PORT=8888
   MONGODB_NAME=SSV
   EXPEDITION_PORT=2010
   MONGODB_HOST=dbserver.internal.sapsailing.com
   MONGODB_PORT=10202
   DEPLOY_TO=ssv
   </pre>

12. Append the following description to the /home/sailing/servers/README file.

  <pre>
  # ssv (Schwartauer Segler-Verein, www.ssv-net.de, Alexander Probst, webmaster@alexprobst.de)
  SERVER_NAME=SSV
  TELNET_PORT=14900
  SERVER_PORT=8888
  MONGODB_NAME=SSV
  EXPEDITION_PORT=2000
  </pre>

13. Start the multi instance.
    <pre>
    cd /home/sailing/servers/ssv
    ./start
    </pre>

14. Change the admin password now and create a new user with admin role.

15. Your multi instance is now configured and started. It can be reached over ec2-34-250-136-229.eu-west-1.compute.amazonaws.com:8888. 


##### Reachability

To reach your multi instance via "ssv.sapsailing.com", perform the following steps within the AWS Web Console inside region Ireland.

1. Create a new target group with the following details, where the name "S-shared-ssv" is created as follows: "S" for "Sailing", "shared" because it's a shared instance, and "ssv" represents the server instance name:

   <img src="/wiki/images/amazon/TargetGroup_1.png"/>

   <img src="/wiki/images/amazon/TargetGroup_2.png"/>
   
   Notice the overwritten health check port that is now pointing directly to the instance with its `SERVER_PORT` 8888.

BE CAREFUL please use for a live-server and live-master-server the traffic port for Health Checks.

2. Add the "SL Multi-Instance Sailing Server" instance to the target group.

  <img src="/wiki/images/amazon/TargetGroup_3.png"/>

3. Create a rule within the application load balancer that is forwarding ssv.sapsailing.com to your created target group. Choose "Load Balancers" from the sidebar an select the load balancer with the name "Sailing-eu-west-1". Click on the tab "Listeners" and then on "View/edit rules" inside the row of the HTTPS Listener.

  <img src="/wiki/images/amazon/ApplicationLoadBalancer_1.png"/>

   Click on the plus sign and insert the new rule at the very top. Enter "ssv.sapsailing.com" into the host-header field and select the target group "S-shared-ssv" under "forward". Then click on "Save".

  <img src="/wiki/images/amazon/ApplicationLoadBalancer_2.png"/>

   Your application load balancer is now configured to redirect all requests with host-header "ssv.sapsailing.com" to the target group "S-shared-ssv". That means all requests will now be routed to the "SL Multi-Instance Sailing Server" instance inside this target group using HTTPS and port 443 as specified in the configuration of the target group. To establish a connection on port 8888 (the `SERVER_PORT` property from above), where our multi instance is listening, we have to modify the apache configuration on the "SL Multi-Instance Sailing Server" instance.

4. Connect to the  "SL Multi-Instance Sailing Server" instance via SSH as user `root`. Navigate to the directory /etc/httpd/conf.d. Open up the file "001-events.conf" and append the following line.

   <pre>
   Use Plain-SSL ssv.sapsailing.com 127.0.0.1 8888
   </pre>
   
   where 8888 is again the `SERVER_PORT` from before.

5. Save the file and run a configuration file syntax check.

   <pre>
   apachectl configtest
   </pre>

   If it reports "Syntax OK", continue with reloading the httpd configuration.

6. Reload the httpd configuration.

   <pre>
   service httpd reload
   </pre>

You should now be able to reach your multi instance with the dns name "ssv.sapsailing.com".

### S3 Storage, `media.sapsailing.com` and CloudFront

In order to serve content from media.sapsailing.com publicly through HTTPS connections with an Amazon-provided SSL certificate, we created a CloudFront distribution ``E2YEQ22MXCKC5R``. See also [https://console.aws.amazon.com/cloudfront/home?region=us-east-1#distribution-settings:E2YEQ22MXCKC5R](https://console.aws.amazon.com/cloudfront/home?region=us-east-1#distribution-settings:E2YEQ22MXCKC5R). CloudFront distributions can use AWS-provided certificates only from region us-east-1, so we created a certificate for ``*.sapsailing.com`` with additional name ``sapsailing.com`` there ([https://console.aws.amazon.com/acm/home?region=us-east-1#/?id=arn:aws:acm:us-east-1:017363970217:certificate%2Fb05e7e2b-a5ad-45e7-91c7-e9cc13e5ed4a](https://console.aws.amazon.com/acm/home?region=us-east-1#/?id=arn:aws:acm:us-east-1:017363970217:certificate%2Fb05e7e2b-a5ad-45e7-91c7-e9cc13e5ed4a)). A CloudFront distribution has a DNS name; this one has ``dieqc457smgus.cloudfront.net``. We made ``media.sapsailing.com`` an "Alias" DNS record in Route53 to point to this CloudFront distribution's DNS name, as an A-record with "Simple" routing policy. Logging for the CloudFront distribution has been enabled and set to the S3 bucket ``sapsailing-access-logs.s3.amazonaws.com``, prefix ``media-sapsailing-com``. As CloudFront distribution origin domain name we set ``media.sapsailing.com.s3.amazonaws.com`` with Origin Type set to ``S3 Origin``. We activated HTTP to HTTPS redirection.

## SSH Key Management

AWS by default adds the public key of the key pair used when launching an EC2 instance to the default user's `.ssh/authorized_keys` file. For a typical Amazon Linux machine, the default user is the `root` user. For Ubuntu, it's the `ec2-user` or `ubuntu` user. The problem with this approach is that other users with landscape management permissions could not get at this instance with an SSH connection. In the past we worked around this problem by deploying those landscape-managing users' public SSH keys into the root user's `.ssh/authorized_keys` file already in the Amazon Machine Image (AMI) off which the instances were launched. The problem with this, however, is obviously that we have been slow to adjust for changes in the set of users permitted to manage the landscape.

We decided early 2021 to change this so that things would be based on our own user and security sub-system (see [here](/wiki/info/security/security.md)). We introduced `LANDSCAPE` as a secured object type, with a special permission `MANAGE` and a special object identifier `AWS` such that the permission `LANDSCAPE:MANAGE:AWS` would permit users to manage all aspects of the AWS landscape, given they can present a valid AWS access key/secret. To keep the EC2 instances' SSH public key infrastructure in line, we made the instances poll the SSH public keys of those users with permissions, once per minute, updating the default user's `.ssh/authorized_keys` file accordingly.

The REST end point `/landscape/api/landscape/get_time_point_of_last_change_in_ssh_keys_of_aws_landscape_managers` has been implemented which is based on state managed in the `com.sap.sse.landscape.aws` bundle's Activator. This activator registers SSH key pair listeners on any AwsLandscape object created by any of the AwsLandscape.obtain methods and uses those to update the time stamp returned by `get_time_point_of_last_change_in_ssh_keys_of_aws_landscape_managers` each time SSH keys are added or removed. Furthermore, the activator listens for changes regarding the `LANDSCAPE:MANAGE:AWS` permission using the new `PermissionChangeListener` observer pattern offered by SecurityService. The activator tracks the SecurityService, and the listener registration would be renewed even if the SecurityService was replaced in the OSGi registry. The actual mapping of changes to SecurityService to listener notifications is implemented by the new class PermissionChangeListeners.

With this, the three REST API end points `/landscape/api/landscape/get_time_point_of_last_change_in_ssh_keys_of_aws_landscape_managers`, `/security/api/restsecurity/users_with_permission?permission=LANDSCAPE:MANAGE:AWS`, and `/landscape/api/landscape/get_ssh_keys_owned_by_user?username[]=...` allow clients to efficiently find out whether the set of users with AWS landscape management permission and/or their set of SSH key pairs may have changed, and if so, poll the actual changes which requires a bit more computational effort.

Two new scripts and a crontab file are provided under the configuration/ folder:
- `update_authorized_keys_for_landscape_managers_if_changed`
- `update_authorized_keys_for_landscape_managers`
- `crontab`

The first makes a call to `/landscape/api/landscape/get_time_point_of_last_change_in_ssh_keys_of_aws_landscape_managers` (currently coded to `https://security-service.sapsailing.com` in the crontab file). If no previous time stamp for the last change exists under `/var/run/last_change_aws_landscape_managers_ssh_keys` or the time stamp received in the response is newer, the `update_authorized_keys_for_landscape_managers` script is invoked using the bearer token provided in `/root/ssh-key-reader.token` as argument, granting the script READ access to the user list and their SSH key pairs. That script first asks for `/security/api/restsecurity/users_with_permission?permission=LANDSCAPE:MANAGE:AWS` and then uses `/landscape/api/landscape/get_ssh_keys_owned_by_user?username[]=..`. to obtain the actual SSH public key information for the landscape managers. The original `/root/.ssh/authorized_keys` file is copied to `/root/.ssh/authorized_keys.org` once and then used to insert the single public SSH key inserted by AWS, then appending all public keys received for the landscape-managing users.

The `crontab` file which is used during image-upgrade (see `configuration/imageupdate.sh`) has a randomized sleeping period within a one minute duration after which it calls the `update_authorized_keys_for_landscape_managers_if_changed` script which transitively invokes `update_authorized_keys_for_landscape_managers` in case of changes possible.

#### Setting up a Dedicated Instance
[...]


## Costs per month

To give you a feeling about the costs you can refer to the following table. To get all details go to http://www.awsnow.info/

<table>
<tr>
<td>Server Type</td>
<td>Cost per Month</td>
<td>Cost per Month (Reserved instance for 12 months)</td>
</tr>
<tr>
<td>m2.2xlarge (Archive)</td>
<td>$800</td>
<td>$400</td>
</tr>
<tr>
<td>c1.xlarge (Build and Live)</td>
<td>$500</td>
<td>$350</td>
</tr>
</table>

## General Information and Security

Since XXX 2013 this project is using EC2 as the server provider. Amazon Elastic Compute Cloud (EC2) is a central part of Amazon.com's cloud computing platform, Amazon Web Services (AWS). EC2 allows users to rent virtual computers on which to run their own computer applications. EC2 allows scalable deployment of applications by providing a Web service through which a user can boot an Amazon Machine Image to create a virtual machine, which Amazon calls an "instance", containing any software desired. A user can create, launch, and terminate server instances as needed, paying by the hour for active servers, hence the term "elastic".

This project is associated with an SAP Sailing Analytics account that, for billing purposes, is a subsidiary of a main SAP billing account. The Analytics account number is "0173-6397-0217 (simon.marcel.pamies@sap.com)" and connected to "SAP CMC Production (hagen.stanek@sap.com)". It has "Dr. Axel Uhl (axel.uhl@sap.com)" configured as operations officer that can be contacted by Amazon in case of problems with the instances.

The main entry point for the account is https://console.aws.amazon.com/. There you can only log in using the root account. You will then have access to not only the EC2 Console but also to the main account details (including billing details).

<img src="/wiki/images/amazon/RootAccount.JPG" width="100%" height="100%"/>

Associated to the root account are _n_ users that can be configured using the IAM (User Management, https://console.aws.amazon.com/iam/home). Each of these users can belong to different groups that have different rights associated. Currently two groups exist:

* **Administrators**: Users belonging to this group have access to all EC2 services (including IAM). They do not have the right to manage main account information (like billing).

* **Seniors**: Everyone belonging to this group can not access IAM but everything else.

Users configured in the IAM and at least belonging to the group Seniors can log in using the following url https://017363970217.signin.aws.amazon.com/console. All users that belong to one of these groups absolutely need to have MFA activated. MFA (Multi-Factor-Authentication) can be compared to the RSA token that needs to be input every time one wants to access the SAP network. After activation users need to synchronize their device using a barcode that is displayed in IAM. The device can be a software (Google Authenticator for iOS and Android) or a physical device.

<img src="/wiki/images/amazon/IAMUsers.JPG" width="100%" height="100%"/>

In addition to having a password and MFA set for one user one can activate "Access Keys". These keys are a combination of hashed username ("ID") and a password ("Key"). These are needed in case of API related access (e.g. S3 uploader scripts). One user should not have more than 1 access key active because of security concerns and never distribute them over insecure channels.

## EC2 Server Architecture for Sailing Analytics

The architecture is divided into logical tiers. These are represented by firewall configurations (Security Groups) that can be associated to Instances. Each tier can contain one or more instances. The following image depicts the parts of the architecture.

<img src="/wiki/images/amazon/EC2Architecture.jpg" width="100%" height="100%"/>

### Tiers

* **Webserver**: Holds one or more webserver instances that represent the public facing part of the architecture. Only instances running in this tier should have an Elastic IP assigned. In the image you can see one configured instance that delivers content for sapsailing.com. It has some services running on it like an Apache, the GIT repository and the UDP mirror. The Apache is configured to proxy HTTP(S) connections to an Archive or Live server.
* **Balancer**: Features an Elastic Load Balancer. Such balancers can be configured to distribute traffic among many other running instances. Internally an ELB consists of multiple balancing instances on which load is distributed by a DNS round robin so that bandwidth is not a limiting factor.
* **Database**: Instances handling all operations related to persistence. Must be reachable by the "Instance" and "Balancer+Group" tier. In the standard setup this tier only contains one database server that handles connections to MongoDB, MySQL and RabbitMQ.
* **Instances**: Space where all instances, that are not logically grouped, live. In the image one can see three running instances. One serving archived data, one serving a live event and one for build and test purposes.
* **Balancer+Group**: Analytics instances grouped and managed by an Elastic Load Balancer. A group is just a term describing multiple instances replicating from one master instance. The word "group" does in this context not refer to the so called "Placement Groups".

### Instances

<table>
<tr>
<td><b>Name</b></td>
<td><b>Access Key(s)</b></td>
<td><b>Security Group</b></td>
<td><b>Services</b></td>
<td><b>Description</b></td>
</tr>
<tr>
<td>Webserver (Elastic IP: 54.229.94.254)</td>
<td>Administrator</td>
<td>Webserver</td>
<td>Apache, GIT, Piwik, Bugzilla, Wiki</td>
<td>This tier holds one instance that has one public Elastic IP associated. This instance manages all domains and subdomains associated with this project. It also contains the public GIT repository.</td>
</tr>
<tr>
<td>DB & Messaging</td>
<td>Administrator</td>
<td>Database and Messaging</td>
<td>MongoDB, MySQL, RabbitMQ</td>
<td>All databases needed by either the Analytics applications or tools like Piwik and Bugzilla are managed by this instance.</td>
</tr>
<tr>
<td>Archive</td>
<td>Administrator, Sailing User</td>
<td>Sailing Analytics App</td>
<td>Java App</td>
<td>Instance handling the access to all historical races.</td>
</tr>
<tr>
<td>Build and Test</td>
<td>Administrator, Sailing User</td>
<td>Sailing Analytics App</td>
<td>X11,Firefox,Hudson</td>
<td>Instance that can be used to run tests</td>
</tr>
</table>

## HowTo

### Create a new Analytics application instance ready for production

Create a new Analytics instance as described in detail here [[wiki/info/landscape/amazon-ec2-create-new-app-instance]]. You should use a configuration like the following. You have two possibilities of making sure that the server uses code from a specific branch.

- First you can use a release file. These files can be usually found at http://releases.sapsailing.com/ and represent a certain point in time. These files can be built by using the buildAndUpdateProduct.sh with the parameter release. In addition to the release file you can specify an environment configuration. These usually can be found here http://releases.sapsailing.com/environments. A configuration then could look like this:

<pre>
INSTALL_FROM_RELEASE=master-201311062138
USE_ENVIRONMENT=live-server
BUILD_COMPLETE_NOTIFY=simon.marcel.pamies@sap.com
SERVER_STARTUP_NOTIFY=simon.marcel.pamies@sap.com
</pre>

- The second option is to let the instance build itself from a specified branch. It is currently not supported to then specify an environment file. Attention: You can not start the building process on t1.micro instances having less than 1.5 GB of RAM! The configuration then looks like this:

<pre>
BUILD_BEFORE_START=True
BUILD_FROM=master
RUN_TESTS=False
COMPILE_GWT=True
BUILD_COMPLETE_NOTIFY=simon.marcel.pamies@sap.com
SERVER_STARTUP_NOTIFY=
SERVER_NAME=LIVE1
MEMORY=2048m
REPLICATION_HOST=rabbit.internal.sapsailing.com
REPLICATION_CHANNEL=sapsailinganalytics-live
TELNET_PORT=14888
SERVER_PORT=8888
MONGODB_HOST=dbserver.internal.sapsailing.com
MONGODB_PORT=10202
EXPEDITION_PORT=2010
REPLICATE_ON_START=
REPLICATE_MASTER_SERVLET_HOST=
REPLICATE_MASTER_SERVLET_PORT=
REPLICATE_MASTER_QUEUE_HOST=
REPLICATE_MASTER_QUEUE_PORT=
INSTALL_FROM_RELEASE=
USE_ENVIRONMENT=
</pre>

After your instance has been started (and build and tests are through) it will be publicly reachable if you chose a port between 8880 and 8950. If you filled the BUILD_COMPLETE_NOTIFY field then you will get an email once the server has been built. You can also add your email address to the field SERVER_STARTUP_NOTIFY to get an email whenever the server has been started.

You can now access this instance by either using the Administrator key (for root User) or the Sailing User key (for user sailing):

<pre>
ssh -i .ssh/Administrator.pem root@ec2-54-246-247-194.eu-west-1.compute.amazonaws.com
</pre>

or

<pre>
ssh -i .ssh/SailingUser.pem sailing@ec2-54-246-247-194.eu-west-1.compute.amazonaws.com
</pre>

If you want to connect your instance to a subdomain then log onto the main webserver with the Administrator key as root, open the file `/etc/httpd/conf.d/001-events.conf` and put something like this there. As you can see you have to specify the IP address and the port the java server is running on. Make sure to always use the internal IP.

<pre>
Use Event idm.sapsailing.com "&lt;uuid-of-event-object&gt;" 172.31.22.12 8888
</pre>

### Testing code on a server

Starting a test is as easy as starting up a new instance. Just make sure that you fill the field RUN_TESTS and set it to `True`. Also set the field BUILD_FROM to a gitspec that matches the code branch that you want to test. After tests has been run and the server has been started you will get an email giving you all the details. You can then access your instance or simply shut it down.

### Build, deploy, start and stop an instance

Log on to the instance using ssh as user `sailing`. Change to the `~/code` directory and fetch the latest git branch, e.g., using `git fetch origin; git merge origin/master`. Make sure you have a MongoDB instance running on the default port 27017 (see [here](http://wiki.sapsailing.com/wiki/amazon-ec2#Access-MongoDB-database)). You can then trigger a build. It may be a good idea to do this in  a `tmux` session because this will allow you to log off or get disconnected while the build is running. To start a tmux session, simply enter the command `tmux` at the console and you will get a new tmux session. Check the man page of tmux for more details and note that the usual Ctrl-B shortcut has been redefined in our instances to Ctrl-A to mimic the behavior of the old `screen` tool we used before we switched to tmux. Suffice it to say that you can detach from the tmux session by pressing `Ctrl-A d` which will keep the session running. To reconnect, enter `tmux attach` which works as long as you only have one tmux session running. In other cases, refer to the tmux man page again.

To launch the build, enter `configuration/buildAndUpdateProduct.sh build` as usual. Used without options, the build script will tell the options available. After the build has completed, use `configuration/buildAndUpdateProduct.sh -s server install` to install the product to ~/servers/server.

To launch the instance, change to the `~/servers/server` directory and enter `./start`. The instance will by default launch on port 8888. See `~/servers/server/env.sh` for the instance's settings. To stop the instance again, from that same directory enter `./stop`.

To access your instance externally, make sure to use the external Amazon DNS name, such as `ec2-54-72-6-31.eu-west-1.compute.amazonaws.com`. This is required for our Google Maps API key to recognize the domain. The map won't work when you simply use the IP number as the URL. A good URL to start with would therefore look something like `http://ec2-54-72-6-31.eu-west-1.compute.amazonaws.com:8888/gwt/AdminConsole.html`.

### Setup replicated instances with ELB

The main concept behind ELB is that there is one instance that you configure in the "Load Balancers" tab that serves as the main entry point for all requests going to your application. This instance can be told to pass through requests from one port to another. In order to make this ELB instance aware of the Analytics EC2 Instances it should balance over you need to add all instances that should be part of the setup to the ELB instance.

A closer look reveals that an ELB instance consists itself of many other invisible instances. These are behind a DNS round robin configuration that redirects each incoming request to one of these instances. These invisible instances then decide upon the rules you've created how and where to distribute this request to one of the associated instances.

In a live event scenario, the SAP Sailing Analytics are largely bandwidth bound. Adding more users that watch races live doesn't add much CPU load, but it adds traffic linearly. Therefore, as the number of concurrent users grows, a single instance can quickly max out its bandwidth which for usual instances peaks at around 100Mbit/s. It is then essential that an ELB can offload the traffic to multiple instances which are replicas of a common master in our case.

To still get the usual logging and URL re-writing features, replicas need to run their local Apache server with a bit of configuration. Luckily, most of the grunt work is done for you automatically. You simply need to tell the replicas in their instance details to start replicating automatically, provide an `EVENT_ID` and set the `SERVER_NAME` variable properly. The Apache configuration on the replica will then automatically be adjusted such that the lower-case version of $SERVER_NAME.sapsailing.com will re-direct users to the event page for the event with ID $EVENT_ID.

Amazon puts up limits regarding to the maximum number of rules that an Application Load Balancer (ALB) may have. We use one such ALB as the DNS CNAME target for ``*.sapsailing.com`` (Sailing-eu-west-1-135628335.eu-west-1.elb.amazonaws.com). Adding rules to this ALB is especially convenient because no DNS / Route53 manipulation is necessary at all. New sub-domains can be mapped to target groups this way quite flexibly and quickly.

However, as the number of sub-domains we use grows, we also approach the limit of 100 rules for this load balancer. In order to keep this flexibility in particular for event set-ups, we started introducing more ALBs in August 2018 that use dedicated Route 53 DNS CNAME records for sepcific sub-domains. This way, with the current AWS limits for load balancers (see https://docs.aws.amazon.com/elasticloadbalancing/latest/application/load-balancer-limits.html) we will have up to 20 ALBs per region with 100 rules each, giving us 2000 rules per region which should suffice for the foreseeable future.

The set-up process needs to distinguish now between only adding a rule to an ALB listener targeted by the ``*.sapsailing.com`` DNS entry, and adding a rule to an ALB listener targeted only by DNS rules for specific sub-domains. In the latter case, a DNS record set needs to be created, providing the CNAME of the ALB that maps the sub-domain to the target group.

Here are the steps to create a load balanced setup, assuming there is already an "Application" load balancer defined in the region(s) where you need them:

- Add a master+replica target group for the master and its replicas that external users will be directed to, using HTTP port 80 as the protocol settings. Note: as this target group will also be used for the HTTPS listener, "SSL offloading" will take place here. The re-directing from HTTP to HTTPS that shall occur when the user hits the server with an HTTP request will happen in the central instance's Apache server if and only if the `X-Forwarded-Proto` is `http` (https://stackoverflow.com/questions/26620670/apache-httpx-forwarded-proto-in-htaccess-is-causing-redirect-loop-in-dev-envir explains how a. See also http://docs.aws.amazon.com/elasticloadbalancing/latest/classic/x-forwarded-headers.html#x-forwarded-proto.)
- Add a rule to the HTTPS listener for the hostname ${SERVER_NAME}.sapsailing.com that forwards traffic to the master+replica target group just created.
- Create a master instance holding all data (see http://wiki.sapsailing.com/wiki/amazon-ec2#Setting-up-Master-and-Replica)
- Create `n` instances that are configured to connect to the master server, automatically launching replication by using one of the `*...-replica-...*` environment from http://releases.sapsailing.com/environments.
- Add master and replicas as targets into the master+replica target group.
- Create a second master-only target group that only contains the master server. 
- Add a rule to the HTTPS listener for the hostname ${SERVER_NAME}-master.sapsailing.com that forwards traffic to the master-only target group just created.
- Add the master to the master-only target group.
- For both target groups configure the health checks, choosing HTTP as the protocol, using the default "traffic port" and setting the path to /index.html. Lower the interval to 10s and the "Healthy threshold" to 2 to ensure that servers are quickly recognized after adding them to the ELB. With the default settings (30 seconds interval, healthy threshold 10) this would last up to 5 minutes.
- When using the Race Committee App (RCApp), make sure the app is configured to send its data to the ${SERVER_NAME}-master.sapsailing.com URL (otherwise, write requests may end up at replicas which then have to reverse-replicate these to the master which adds significant overhead).

The steps to register such a sub-domain mapping also in Route53 in case you've chosen an ALB that is not the target of ``*.sapsailing.com`` work as follows:

Start by creating a new record set:
<img src="/wiki/images/amazon/DNS1.png" />

Then enter the sub-domain name you'd like to map. Choose ``CNAME`` for the type, reduce the default TTL to 60s and paste the DNS name of the ALB you'd like to target:
<img src="/wiki/images/amazon/DNS2.png" />

The DNS name of your load balancer can be copied from the "Basic Configuration" section in the "Description" tab:
<img src="/wiki/images/amazon/CopyingAlbDnsName.png" />

The insertion of the rule into the ALB that maps your sub-domain's name to the corresponding target group works as usual and as described above:
<img src="/wiki/images/amazon/DNS3.png" />
<img src="/wiki/images/amazon/DNS4.png" />

It is important to understand that it wouldn't help to let all traffic run through our central Apache httpd server which usually acts as a reverse proxy with comprehensive URL rewriting rules and macros. This would make the Apache server the bandwidth bottleneck. Instead, the event traffic needs to go straight to the ELB. This is established by the *.sapsailing.com DNS entry pointing to the Application ELB which then applies its filter rules to dispatch to the URL-specific target groups. Other than adding the hostname filter rules in the ELB as described above, no interaction with the Route 53 DNS is generally needed. Neither is it necessary to manually modify any 001-events.conf Apache configuration file.

For testing purposes, however, it may be useful to still have some documentation around that explains how to do the Route 53 DNS setup manually. Remember: this shouldn't be needed for usual operations!

<img src="/wiki/images/amazon/Route53_1.png" />

Go to the "Hosted Zones" entry

<img src="/wiki/images/amazon/Route53_2.png" />

and select the `sapsailing.com.` row,

<img src="/wiki/images/amazon/Route53_3.png" />

then click on "Go to Record Sets." You will then see the record sets for the `sapsailing.com.` domain:

<img src="/wiki/images/amazon/Route53_4.png" />

Click on "Create Record Set" and fill in the subdomain name (`myspecificevent` in the example shown below) and as the value use the host name (A-record) of the ELB that you find in the ELB configuration. 

<img src="/wiki/images/amazon/Route53_5.png" />

Amazon ELB is designed to handle unlimited concurrent requests per second with â€œgradually increasingâ€� load pattern (although it's initial capacity is described to reach 20k requests/secs). It is not designed to handle heavy sudden spike of load or flash traffic because of its internal structure where it needs to fire up more instances when load increases. ELB's can be pre-warmed though by writing to the AWS Support Team.

With this set-up, please keep in mind that administration of the sailing server instance always needs to happen through the master instance. A fat, red warning is displayed in the administration console of the replica instances that shall keep you from making administrative changes there. Change them on the master, and the changes will be replicated to the replicas.

You can monitor the central RabbitMQ message queueing system at [http://54.246.250.138:15672/#/exchanges](http://54.246.250.138:15672/#/exchanges). Use `guest/guest` for username and password. You should find the exchange name you configured for you master there and will be able to see the queues bound to the exchange as well as the traffic running through the exchange.

### Using Latency-Based DNS across Regions

ELBs don't work across regions (such as eu-west-1 and ap-southeast-1) but only across the availability zones within one region. Therefore, if you want location and latency-based routing for your event, you have to set up so-called latency-based routing policies using alias record sets that point to your ELBs, one per region. The alias records are all for the same domain name and are each an alias for one ELB in one region. The DNS service will then find out for a requesting client which of the ELBs for the same name will provide the shortest network latency and return that record. Additionally, the DNS service can be configured to evaluate the ELBs health checking status. If the ELB has bad health, DNS requests will subsequently be answered using other alias records, therefore pointing to ELBs in other regions.

<img src="/wiki/images/amazon/Route53_LatencyAliases.png" />

The above image shows what you need to do: After you've set up an ELB in each of the regions as described above, for each of them add an alias record set. Enter the domain name and make sure it's the same for all ELBs. Change the "Alias" radio button to "Yes." The alias target can usually be selected from a drop-down, but in case you aren't offered the ELB you want to add, you can as well copy the ELB DNS name shown in the configuration page of that ELB.

Change the routing policy to "Latency" and select the region in which your ELB is located. A meaninful ID string may be helpful later to identify the record set from a list of record sets.

Select the "Yes" radio button for "Evaluate Target Health" in order to make sure the DNS server checks the ELB's health status and fails over to any of your other latency-based record sets if the ELB has bad health.

Should you be switching from an Apache reverse proxy set-up with a record in `/etc/httpd/conf.d/001-events.conf` for your sub-domain, remember to comment or remove this record and to reload the httpd service on the central web server using `service httpd reload`. Its effects may otherwise interfere with the effects of the DNS entries for that same sub-domain. Conversely, before removing the DNS record sets for the sub-domain, if migrating back to a non-ELB, non-DNS scenario, remember to first re-activate the `001-events.conf` reverse proxy entry before removing the DNS record sets.

### Access MongoDB database

To launch a local MongoDB instance on the default port, enter a command such as `mkdir /home/sailing/mongodb; /opt/mongodb-linux-x86_64-1.8.1/bin/mongod --dbpath /home/sailing/mongodb`. To make sure the process survives log-off, you may want to launch it in a tmux session. See above for how to create a tmux session. In an existing tmux session, a new window can be created using `Ctrl-a c`.

### Upgrade the Sailing Analytics App AMI image

There are a number of use cases that suggest an upgrade of the AMI that we use to start new SAP Sailing Analytics instances. One can be that we want to add or modify the set of SSH keys authorized for access to the root and sailing accounts. Another can be that we would like to upgrade the git contents under `~sailing/code` for an update to the `/etc/init.d/sailing` or the `/home/sailing/code/java/target/refreshInstance.sh` script that is central to the automated launch process of a new instance. Another use case can be applying an upgrade to the underlying operating system (currently CentOS).

Follow these steps to upgrade the AMI:

* Launch a new instance based on the existing AMI
* Log in as user `root`
* Run `yum update` to update the operating system
* Stop the Java instance, e.g., by using `killall -9 java`
* Remove any obsolete logs from `/home/sailing/servers/server/logs`
* Stop the httpd server, e.g., using `service httpd stop`
* Remove httpd logs under `/var/log/httpd`
* Update the git contents (essential for up-to-date versions of `/etc/init.d/sailing` which links to the git, and the `refreshInstance.sh` script used during automatic instance launch), and clean any build artifacts by doing
```
    > su - sailing
    > cd code
    > mvn clean
    > git fetch
    > git merge origin/master
```
* Then, still as user `sailing`, edit `~sailing/servers/server/env.sh` and remove everything after the line `# **** Overwritten environment variables ****` as this will then be the place where any downloaded environment and the EC2 user data variables will be appended later during automatic installation upon reboot.
* Check the sizes of the mounted partitions by doing `df; swapon -s`. These will come in handy after creating the new AMI in order to tag the new volume snapshots accordingly
* Update any keys in `/root/.ssh/authorized_keys` and `/home/sailing/.ssh/authorized_keys`
* Remove created http rewrite entries in `/etc/httpd/conf.d/001-events.conf`
* Edit /etc/update-motd.d/30-banner to set the current version
* In the EC2 administration console go to the "Instances" tab, select your running instance and from the "Actions" drop-down select "Create Image". Give the image the name "SAP Sailing Analytics App x.y" where "x.y" is the updated version number of the image. Just make sure it's greater than the previous one. If you feel like it, you may provide a short description telling the most important features of the image.
* Once the image creation has completed, go to the Snapshots list in the "Elastic Block Store" category and name the new snapshots appropriately. Now the information about the device sizes obtained earlier from the `df` and `swapon` commands will help you to identify which snapshot is which. Usually, the three snapshots would be something like AMI Analytics Home x.y, AMI Analytics System x.y and AMI Analytics Swap x.y with "x.y" being the version number matching that of your image.
* Now you can remove any earlier Sailing Server AMI version and the corresponding snapshots.

## Terminating AWS Sailing Instances

### ELB Setup with replication server(s)
- Remove all Replica's from the ELB and wait at least 2 minutes until no request reaches their Apache webservers anymore. You can check this with looking at `apachetop` on the respective instances. Let only the Master server live inside the ELB.
- Login to each server instance as `root`-user and stop the java instance with `/home/sailing/servers/server/stop;` 
- As soon as the instance is successfully stopped (verify with `ps -ef | grep "java"`) copy all server logs towards `/var/log/old/<event-name>/<instance-public-ipv4/` with following command
```
cp -rf /home/sailing/servers/server/logs/* /var/log/old/<event-name>/<instance-public-ipv4>/
```
- Once this is done, make sure all HTTP logs are also copied to the above location
  - Either you wait now for the next day, then the http logrotate script ran through
  - Or you manually force a logrotate run with `logrotate --force /etc/logrotate.d/httpd`, which copies `/var/log/httpd/` towards `/var/log/old/<event-name>/<instance-public-ipv4>`
- Please verify that there are no open queues left on RabbitMQ for that particular replication server. In case purge the queue of this replica.
- Once all replica's are terminated and only the Master server is running inside the ELB, go ahead with a master data import on sapsailing.com for the event, grabbing the data from your master server
- Once the master data import is done, make sure you track the corresponding races (be careful to also track the smartphone tracked regattas)
- Once this is done, remember to remove any entries on sapsailing.com regarding "remote sailing instances", otherwise the event will appear two times on the public events list
- at the same time, you need to modify or add an entry on the central Apache server to point the event URL towards the Archive server. Make sure you have removed the `-master` event URL, as you don't need this one anymore
```
# <EVENT> <YEAR>
Use Event-ARCHIVE-SSL-Redirect <EVENT><YEAR>.sapsailing.com "<EVENT-UUID>"
```
- Check the Apache config is correct before reloading it via `apachectl configtest`
- When `SYNTAX OK` go ahead with reload `/etc/init.d/httpd reload`
- Now let us point the public towards the Archive server with removing the Route53 DNS entry for the event
- Make sure that you keep running ELB and Master server in it for at least 12 hours, as DNS servers around the world will cache the old entry. If you would already remove ELB and Master, this would result in people may not reaching your event anymore
- When the 12 hours are over, you can go ahead with the above steps (java instance stop, log savings,..) for the last master instance
- Afterwards simply terminate ELB + the Master instance, after you made sure all logs are correctly saved to `/var/log/old/<event-name>/<instance-public-ipv4>`

### Single server with central Apache redirection
- Do a Master Data import of the event towards sapsailing.com 
- Track all corresponding races (don't forget the smartphone tracked ones)
- Once verified the event looks ok on sapsailing.com, make sure to remove the "remote server entry" on sapsailing.com, so that the event will not appear twice on the public event list
- Go ahead and change the central Apache config in regards to point the event URL toward the archive event via
```
# <EVENT> <YEAR>
Use Event-ARCHIVE-SSL-Redirect <EVENT><YEAR>.sapsailing.com "<EVENT-UUID>"
```
- Check the Apache config is correct before reloading it via `apachtctl configtest`
- When `SYNTAX OK` go ahead with reload `/etc/init.d/httpd reload`
- After that is done, make sure to stop the java instance on your event server
- As soon as the instance is successfully stopped (verify with `ps -ef |grep "java"`) copy all sailing logs towards `/var/log/old/<event-name>/<instance-public-ipv4/` with following command
```
cp -rf ~/servers/server/logs/* /var/log/old/<event-name>/<instance-public-ipv4>/
```
- Once this is done, make sure all HTTP logs are also copied to the above location
  - Either you wait now for the next day, then the http logrotate script ran through
  - Or you manually force a logrotate run with `logrotate --force /etc/logrotate.d/httpd`, which copies `/var/log/httpd/` towards `/var/log/old/<event-name>/<instance-public-ipv4>`
- Once all this is done, you can go ahead and terminate the instance via AWS

### Comparing Server Content after Master Data Import

The script ``java/target/compareServers`` helps comparing server content after master data import. Run with two server URLs you want to compare, ideally in an empty directory where file downloads can be stored. Run initially with the ``-elv`` option to get verbose output. Make sure you have your ``http_proxy`` and ``https_proxy`` environment variables set or unset, depending on your network environment. Should the initial comparison fail, analyze the differences and continue by using ``-cel`` as command line arguments, telling the script to continue where it left off, exiting when hitting a difference and listing the leaderboard groups currently being compared. Repeat until done.

Should you want to compare servers of which you know they have different sets of leaderboard groups, start with ``compareServers -elv`` and then manually adjust the ``leaderboardgroups.new.sed`` and ``leaderboardgroups.old.sed`` files according to your needs, then continue with the ``-cel`` switches to restrict comparisons to what you left in the ``leaderboardgroups.*.sed`` files.

## Migration Checklist

### Before switching sapsailing.com to the EC2 webserver
- fire up archive server and load it (DONE)
- configure 001-events.conf starting with a copy from old sapsailing.com, using test URLs (rombalur.de) (DONE)
- clone entire MongoDB content (DONE)
- migrate MySQL for Bugzilla
- ensure that all users have access; either solicit their public keys and enter to ~trac/.ssh/authorized_keys or migrate /etc/passwd and /etc/group settings for access to trac group (DONE)
- run test build and deploy (DONE)
- fire up a live server and test it (DONE)
- fire up a replica and check that it works correctly (ERROR!)
- check that UDP mirror is working (DONE)
- check that SwissTiming StoreAndForward is working
- check that we can fire up a live2 / archive2 server and switch transparently

### Just before the migration on Sunday evening
- check that sapsailing.com is entered everywhere a hostname / domain name is required, particularly in /etc/httpd/conf.d/001-events.conf and /opt/piwik-scripts and all of /etc - also have a look at piwik and bugzilla configuration (DONE)
- disable bugzilla on old.sapsailing.com because Nameserver switch can take up to 48 hours for everyone (DONE)
- copy /home/trac/releases to webserver (DONE)
- import bugzilla to mysql (DONE)
- git fetch --all on webserver (DONE)
- tell SAP hostmaster to point old.sapsailing.com to 195.227.10.246

### Immediately after switching the sapsailing.com domain to the EC2 webserver on Sunday evening
- check that old.sapsailing.com points to 195.227.10.246
- check that EC2 web server is responding to sapsailing.com now
- fetch all git branches from what is now old.sapsailing.com; also sync gollum wiki git
- ask people (including internal Git team) to update their known_hosts files according to the new web server's key
- check if build server can access new sapsailing.com
- check why swisstiminglistener doesn't receive connections and fix

## Glossary

<table>
<tr>
<td><b>Term</b></td>
<td><b>Description</b></td>
</tr>
<tr><td>Instance</td><td>Virtual machine that runs on a Xen host. Such an instance runs forever until it is stopped. It will be billed by hours it ran. Each start will be billed by a full hour.</td></tr>
<tr><td>Spot Instance</td><td>Instances that run whenever there are free resources. It is not possible to control when or where these instances run. These instances are much cheaper than normal instances.</td></tr>
<tr><td>Amazon Machine Image (AMI)</td><td>Amazon Machine Image: Image file that contains a filesystem and a preinstalled operating system. One can create AMIs very easily from a stopped Instance by first creating a snapshot and then converting it to an AMI.</td></tr>
<tr><td>Volume</td><td>An active harddisk that can be associated to one Instance.</td></tr>
<tr><td>IOPS</td><td>Input/Output operations per second. Metric used to denote the performance of a volume. The higher the IOPS value the better the speed. Be aware of the fact that IOPS is metered by IOPS/h and is very expensive. Use with care!</td></tr>
<tr><td>Snapshot</td><td>Snapshot of a Volume</td></tr>
<tr><td>Elastic IP</td><td>IP address that can be associated to an instance. Any Elastic-IP not associated to a running Instance costs some amount of money per hour.</td></tr>
<tr><td>Security Group</td><td>Firewall configuration that can be associated to an instance. There is no need of configuring iptables or such. One can associate many instances the the same Security Group.</td></tr>
<tr><td>Elastic Load Balancer (ELB)</td><td>Service that makes it possible to balance over services running on different instances.</td></tr>
<tr><td>Network Interfaces</td><td>Virtual network interfaces that are mapped to physical network interfaces on instances. </td></tr>

<tr><td>Multi instance</td><td>App instance that runs along with other app instances on the same EC2 instance</td></tr><tr><td>Placement Groups</td><td>Enables applications to get the full-bisection bandwidth and low-latency network performance required for tightly coupled, node-to-node communication. Placement Groups can only contain HVM instance and have other limitations described here: https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/using_cluster_computing.html</td></tr>
</table>
