# Setup for the Olympic Summer Games 2020/2021 Tokyo

## Local Installation

For the Olympic Summer Games 2020/2021 Tokyo we use a dedicated hardware set-up to accommodate the requirements on site. In particular, two Lenovo P1 laptops with equal hardware configuration (32GB RAM, Intel Core i9-9880H) will be established as server devices running various services in a way that we can tolerate, with minimal downtimes, failures of either of the two devices.

### Installation Packages

The two laptops run Mint Linux with a fairly modern 5.4 kernel. We keep both up to date with regular ``apt-get update && apt-get upgrade`` executions. Both have an up-to-date SAP JVM 8 (see [https://tools.hana.ondemand.com/#cloud](https://tools.hana.ondemand.com/#cloud)) installed under /opt/sapjvm_8. This is the runtime VM used to run the Java application server process.

Furthermore, both laptops have a MongoDB 3.6 installation configured through ``/etc/apt/sources.list.d/mongodb-org-3.6.list`` containing the line ``deb http://repo.mongodb.org/apt/debian jessie/mongodb-org/3.6 main``. Their respective configuration can be found under ``/etc/mongod.conf``. RabbitMQ is part of the distribution natively, in version 3.6.10-1. It runs on both laptops. Both, RabbitMQ and MongoDB are installed as systemd service units and are launched during the boot sequence. The latest GWT version (currently 2.9.0) is installed under ``/opt/gwt-2.9.0`` in case any development work would need to be done on these machines.

Both machines have been configured to use 2GB of swap space at ``/swapfile``.

### User Accounts

The essential user account on both laptops is ``sailing``. The account is intended to be used for running the Java VM that executes the SAP Sailing Analytics server software. The account is currently still protected by a password that our on-site team should know. On both laptops the ``sailing`` account has a password-less SSH key installed under ``/home/sailing/.ssh`` that is contained in the ``known_hosts`` file of ``tokyo-ssh.sapsailing.com`` as well as the mutually other P1 laptop. This way, all tunnels can easily be created once logged on to this ``sailing`` account.

There are also still two personal accounts ``uhl`` and ``tim`` and an Eclipse development environment under ``/usr/local/eclipse``.

### Hostnames

We assume not to have DNS available on site. Therefore, for now, we have decided for host names ``sap-p1-1`` and ``sap-p1-2`` for which we have created entries in both laptops' ``/etc/hosts`` file. Currently, when testing in the SAP facilities with the SAP Guest WiFi, possibly changing IP addresses have to be updated there.

The domain name has been set to ``sapsailing.com`` so that the fully-qualified host names are ``sap-p1-1.sapsailing.com`` and ``sap-p1-2.sapsailing.com`` respectively. Using this domain name is helpful later when it comes to the shared security realm established with the central ``security-service.sapsailing.com`` replica set.

### IP Addresses and VPN

Here are the IP addresses as indicated by SwissTiming:

```
Host					Internal IP	VPN IP
-----------------------------------------------------------------------------------------
TracTrac A (Linux)			10.1.1.104	10.8.0.128	STSP-SAL_client28
TracTrac B (Linux)			10.1.1.105	10.8.0.129	STSP-SAL_client29
SAP Analytics 1 Server A (Linux)	10.1.3.195	10.8.0.130	STSP-SAL_client30
SAP Analytics 2 Server B (Linux)	10.1.3.197	10.8.0.131	
SAP Client Jan (Windows)		10.1.3.220	10.8.0.132	
SAP Client Alexandro (Windows)		10.1.3.221	10.8.0.133	
SAP Client Axel (Windows)		10.1.3.227	10.8.0.134	
TracTrac Dev Jorge (Linux)		10.1.3.228	10.8.0.135	
TracTrac Dev Chris (Linux)		10.1.3.233	10.8.0.136	
```

### Tunnels

On both laptops there is a script ``/usr/local/bin/tunnels`` which establishes SSH tunnels using the ``autossh`` tool. The ``autossh`` processes are forked into the background using the ``-f`` option. It seems important to then pass the port to use for sending heartbeats using the ``-M`` option. If this is omitted, according to my experience only one of several ``autossh`` processes survives.

On sap-p1-1 two SSH connections are maintained, with the following default port forwards, assuming sap-p1-1 is the local master:

* tokyo-ssh.sapsailing.com: 10203-->10203; 5763-->rabbit-ap-northeast-1.sapsailing.com:5762; 15763-->rabbit-ap-northeast-1.sapsailing.com:15672; 5675:rabbit.internal.sapsailing.com:5672; 15675:rabbit.internal.sapsailing.com:15672; 10201<--10201; 18122<--22; 8888<--8888
* sap-p1-2: 10202-->10202; 5674-->5672; 15674-->15672; 10201<--10201; 5674<--5672; 15674<--15672

On sap-p1-2, the following SSH connections are maintained, assuming sap-p1-2 is the local replica:

- tokyo-ssh.sapsailing.com: 10203-->10203; 5763-->rabbit-ap-northeast-1.sapsailing.com:5762; 15763-->rabbit-ap-northeast-1.sapsailing.com; 5675:rabbit.internal.sapsailing.com:5672; 15675:rabbit.internal.sapsailing.com:15672; 10202<--10202

This means that tokyo-ssh.sapsailing.com sees the process to use for reverse replication at its port 8888. Both laptops see the RabbitMQ running in eu-west-1 and reachable with its internal IP address under rabbit.internal.sapsailing.com at localhost:5675 / localhost:15675. The port forwarding through tokyo-ssh.sapsailing.com to the internal RabbitMQ address works through VPC peering.

### Letsencrypt Certificate for tokyo2020.sapsailing.com

In order to allow us to access ``tokyo2020.sapsailing.com`` with any HTTPS port forwarding locally so that all ``JSESSION_GLOBAL`` etc. cookies with their ``Secure`` attribute are delivered properly, we need an SSL certificate. I've created one by doing

```
/usr/bin/sudo -u certbot docker run --rm -it --name certbot -v "/etc/letsencrypt:/etc/letsencrypt" -v "/var/lib/letsencrypt:/var/lib/letsencrypt" certbot/certbot certonly --manual -d tokyo2020.sapsailing.com
```

as ``root`` on ``sapsailing.com``. The challenge displayed can be solved by creating an ALB rule for hostname header ``tokyo2020.sapsailing.com`` and the path as issued in the output of the ``certbot`` command, and as action specify a fixed response, response code 200, and pasting as text/plain the challenge data printed by the ``certbot`` command. Wait a few seconds, then confirm the Certbot prompt. The certificate will be issued and stored under ``/etc/letsencrypt/live/tokyo2020.sapsailing.com`` from where I copied it to ``/home/sailing/Downloads/letsencrypt`` on both laptops for later use with a local Apache httpd server. The certificate will expire on 2021-08-19, so after the Olympic Games, so we don't have to worry about renewing it.

### Local NGINX Webserver Setup

In order to be able to access the applications running on the local on-site laptops using HTTPS there is a web server on each of the two laptops, listening on port 9443 (HTTPS). The configuration for this is under ``/etc/nginx/sites-enables/tokyo2020`` and looks like this:

```
server {
    listen              9443 ssl;
    server_name         tokyo2020.sapsailing.com;
    ssl_certificate     /etc/ssl/certs/tokyo2020.sapsailing.com.crt;
    ssl_certificate_key /etc/ssl/private/tokyo2020.sapsailing.com.key;
    ssl_protocols       TLSv1 TLSv1.1 TLSv1.2;
    ssl_ciphers         HIGH:!aNULL:!MD5;

    location / {
        proxy_pass http://127.0.0.1:8888;
    }
}
```

The "Let's Encrypt"-provided certificate is used for SSL termination. With tokyo2020.sapsailing.com aliased in ``/etc/hosts`` to the address of the current master server, this allows accessing ``https://tokyo2020.sapsailing.com:9443`` with all benefits of cookie / session authentication.

## AWS Setup

Our primary AWS region for the event will be Tokyo (ap-northeast-1). There, we have reserved the elastic IP ``52.194.91.94`` to which we've mapped the Route53 hostname ``tokyo-ssh.sapsailing.com`` with a simple A-record. The host assigned to the IP/hostname is to be used as a "jump host" for SSH tunnels. It runs Amazon Linux with a login-user named ``ec2-user``. The ``ec2-user`` has ``sudo`` permission. In the root user's crontab we have the same set of scripts hooked up that in our eu-west-1 production landscape is responsible for obtaining and installing the landscape manager's SSH public keys to the login user's account, aligning the set of ``authorized_keys`` with those of the registered landscape managers (users with permission ``LANDSCAPE:MANAGE:AWS``). The ``authorized_keys.org`` file also contains the two public SSH keys of the ``sailing`` accounts on the two laptops, so each time the script produces a new ``authorized_keys`` file for the ``ec2-user``, the ``sailing`` keys for the laptop tunnels don't get lost.

I added the EPEL repository like this:

```
   yum install https://dl.fedoraproject.org/pub/epel/epel-release-latest-7.noarch.rpm
```

Our "favorite" Availability Zone (AZ) in ap-northeast-1 is "1d" / "ap-northeast-1d".

The same host ``tokyo-ssh.sapsailing.com`` also runs a MongoDB 3.6 instance on port 10203.

For RabbitMQ we run a separate host, based on AWS Ubuntu 20. It brings the ``rabbitmq-server`` package with it (version 3.8.2 on Erlang 22.2.7), and we'll install it with default settings, except for the following change: In the new file ``/etc/rabbitmq/rabbitmq.conf`` we enter the line

```
    loopback_users = none
```

which allows clients from other hosts to connect. The security groups for the RabbitMQ server are configured such that only ``172.0.0.0/8`` addresses from our VPCs can connect.

The RabbitMQ management plugin is enabled using ``rabbitmq-plugins enable rabbitmq_management`` for access from localhost. This will require again an SSH tunnel to the host. The host's default user is ``ubuntu``. The RabbitMQ management plugin is active on port 15672 and accessible only from localhost or an SSH tunnel with port forward ending at this host. RabbitMQ itself listens on the default port 5672. With this set-up, RabbitMQ traffic for this event remains independent and undisturbed from any other RabbitMQ traffic from other servers in our default ``eu-west-1`` landscape, such as ``my.sapsailing.com``. The hostname pointing to the internal IP address of the RabbitMQ host is ``rabbit-ap-northeast-1.sapsailing.com`` and has a timeout of 60s.

An autossh tunnel is established from ``tokyo-ssh.sapsailing.com`` to ``rabbit-ap-northeast-1.sapsailing.com`` which forwards port 15673 to port 15672, thus exposing the RabbitMQ web interface which otherwise only responds to localhost. This autossh tunnel is established by a systemctl service that is described in ``/etc/systemd/system/autossh-port-forwards.service`` in ``tokyo-ssh.sapsailing.com``.

### Cross-Region VPC Peering

The primary AWS region for the tokyo2020 replica set is ap-northeast-1 (Tokyo). In order to provide low latencies for the RHBs we'd like to add replicas also in other regions. Since we want to not expose the RabbitMQ running ap-northeast-1 to the outside world, we plan to peer the VPCs of other regions with the one in ap-northeast-1.

The pre-requisite for VPCs to get peered is that their CIDRs (such as 172.31.0.0/16) don't overlap. The default VPC in each region always uses the same CIDR (172.31.0.0/16), and hence in order to peer VPCs all but one must be non-default VPC. To avoid confusion when lanuching instances or setting up security groups it can be adequate for those peering regions other than our default region eu-west-1 to set up non-default VPCs with peering-capable CIDRs and remove the default VPC. This way users cannot accidentally launch instances or define security groups for any VPC other than the peered one.

After having peered the VPCs, the VPCs default routing table must be extended by a route to the peered VPC's CIDR using the peering connection.

With peering in place it is possible to reach instances in peered VPCs by their internal IPs. In particular, it is possible to connect to a RabbitMQ instance with the internal IP and port 5672 even if that RabbitMQ runs in a different region whose VPC is peered.

### Global Accelerator

We have created a Global Accelerator [Tokyo2020](https://us-west-2.console.aws.amazon.com/ec2/v2/home?region=us-west-2#AcceleratorDetails:AcceleratorArn=arn:aws:globalaccelerator::017363970217:accelerator/8ddd5afb-dd8d-4e8b-a22f-443a47240a94) which manages cross-region load balancing for us. There are two listeners: one for port 80 (HTTP) and one for port 443 (HTTPS). For each region an endpoint group must be created for both of the listeners, and the application load balancer (ALB) in that region has to be added as an endpoint.

The Route53 entry ``tokyo2020.sapsailing.com`` now is an alias A record pointing to this global accelerator (``aca060e6eabf4ba3e.awsglobalaccelerator.com.``).

### Application Load Balancers (ALBs) and Target Groups

In each region supported, two target groups with the usual settings (port 8888, health check on ``/gwt/status``, etc.) must exist: ``S-ded-tokyo2020`` (public) and ``S-ded-tokyo2020-m`` (master). An application load balancer then must be created or identified that will then have the five rules distributing traffic for ``tokyo2020.sapsailing.com`` to either the public or the master target group, furthermore a general rule in the HTTP listener for port 80 that will redirect all HTTP traffic to HTTPS permanently.

The master target group in all regions must contain an instance that forwards traffic on port 8888 to the master running on site, usually transitively through ``tokyo-ssh.sapsailing.com:8888``. Only in ap-northeast-1 the ``tokyo-ssh.sapsailing.com`` instance itself can be used as target in the ``S-ded-tokyo2020-m`` master target group. In ``eu-west-1`` the Webserver instance plays that role; it has a tmux running with the ``root`` user where an ``autossh`` connection is established to ``tokyo-ssh.sapsailing.com``, forwarding port 8888 accordingly.

Similar set-ups with a region-local "jump host" can be established; the jump host doesn't need much bandwidth as it is mainly used for admin requests that are to be routed straight to the master instance running on site.

## Landscape Architecture

We have applied for a single SSH tunnel to IP address ``52.194.91.94`` which is our elastic IP for our SSH jump host in ap-northeast-1(d). 

The default production set-up is defined as follows:

### MongoDB

Three MongoDB nodes are intended to run during regular operations: sap-p1-1:10201, sap-p1-2:10202, and tokyo-ssh.sapsailing.com:10203. Since we have to work with SSH tunnels to keep things connected, we map everything using ``localhost`` ports such that both, sap-p1-2 and tokyo-ssh see sap-p1-1:10201 as their localhost:10201, and that both, sap-p1-1 and tokyo-ssh see sap-p1-2:10202 as their respective localhost:10202. Both, sap-p1-1 and sap-p1-2 see tokyo-ssh:10203 as their localhost:10203. This way, the MongoDB URI can be specified as

```
	mongodb://localhost:10201,localhost:10202,localhost:10203/tokyo2020?replicaSet=tokyo2020&retryWrites=true&readPreference=nearest
```

All cloud replicas shall use a MongoDB database name ``tokyo2020-replica``. In those regions where we don't have dedicated MongoDB support established (basically all but eu-west-1 currently), an image should be used that has a MongoDB server configured to use ``/home/sailing/mongo`` as its data directory and ``replica`` as its replica set name. See AMI SAP Sailing Analytics App HVM with MongoDB 1.137 (ami-05b6c7b1244f49d54) in ap-northeast-1 (already copied to the other peered regions except eu-west-1).

### Replicas

Replicas in region ``eu-west-1`` can be launched using the following user data, making use of the established MongoDB live replica set in the region:

```
INSTALL_FROM_RELEASE=build-202105211058
SERVER_NAME=tokyo2020
MONGODB_URI="mongodb://mongo0.internal.sapsailing.com,mongo1.internal.sapsailing.com,dbserver.internal.sapsailing.com:10203/tokyo2020-replica?replicaSet=live&retryWrites=true&readPreference=nearest"
USE_ENVIRONMENT=live-replica-server
REPLICATION_CHANNEL=tokyo2020-replica
REPLICATION_HOST=rabbit-ap-northeast-1.sapsailing.com
REPLICATE_MASTER_SERVLET_HOST=tokyo-ssh.internal.sapsailing.com
REPLICATE_MASTER_SERVLET_PORT=8888
REPLICATE_MASTER_EXCHANGE_NAME=tokyo2020
REPLICATE_MASTER_QUEUE_HOST=rabbit-ap-northeast-1.sapsailing.com
REPLICATE_MASTER_BEARER_TOKEN="4qUrxMVQanLghETmM95XX3fshkHK0wNAQycuPAVNW0E="
ADDITIONAL_JAVA_ARGS="${ADDITIONAL_JAVA_ARGS} -Dcom.sap.sse.debranding=true"
```

(Adjust the release accordingly, of course).

In other regions, instead an instance-local MongoDB shall be used for each replica, not interfering with each other or with other databases:


```
INSTALL_FROM_RELEASE=build-202105211058
SERVER_NAME=tokyo2020
MONGODB_URI="mongodb://localhost/tokyo2020-replica?replicaSet=replica&retryWrites=true&readPreference=nearest"
USE_ENVIRONMENT=live-replica-server
REPLICATION_CHANNEL=tokyo2020-replica
REPLICATION_HOST=rabbit-ap-northeast-1.sapsailing.com
REPLICATE_MASTER_SERVLET_HOST=tokyo-ssh.internal.sapsailing.com
REPLICATE_MASTER_SERVLET_PORT=8888
REPLICATE_MASTER_EXCHANGE_NAME=tokyo2020
REPLICATE_MASTER_QUEUE_HOST=rabbit-ap-northeast-1.sapsailing.com
REPLICATE_MASTER_BEARER_TOKEN="4qUrxMVQanLghETmM95XX3fshkHK0wNAQycuPAVNW0E="
ADDITIONAL_JAVA_ARGS="${ADDITIONAL_JAVA_ARGS} -Dcom.sap.sse.debranding=true"
```

### Application Servers

sap-p1-1 normally is the master for the ``tokyo2020`` replica set. It shall replicate the shared services, in particular ``SecurityServiceImpl``, from ``security-service.sapsailing.com``, like any normal server in our landscape, only that here we have to make sure we can target the default RabbitMQ in eu-west-1 and can see the ``security-service.sapsailing.com`` master directly or even better the load balancer.

SSH local port forwards (configured with the ``-L`` option) that use hostnames instead of IP addresses for the remote host specification are resolved each time a new connection is established through this forward. If the DNS entry resolves to multiple IPs or if the DNS entry changes over time, later connection requests through the port forward will honor the new host name's DNS resolution.

sap-p1-2 normally is a replica for the ``tokyo2020`` replica set, using the local RabbitMQ running on sap-p1-1. Its outbound ``REPLICATION_CHANNEL`` will be ``tokyo2020-replica`` and uses the RabbitMQ running in ap-northeast-1, using an SSH port forward with local port 5673 for the ap-northeast-1 RabbitMQ (15673 for the web administration UI). A reverse port forward from ap-northeast-1 to the application port 8888 on sap-p1-2 has to be established which replicas running in ap-northeast-1 will use to reach their master through HTTP. This way, adding more replicas on the AWS side in the cloud will not require any additional bandwidth between cloud and on-site network, except that the reverse HTTP channel, which uses only little traffic, will see additional traffic per replica whereas all outbound replication goes to the single exchange in the RabbitMQ node running in ap-northeast-1.

## User Groups and Permissions

The general public shall not be allowed during the live event to browse the event through ``tokyo2020.sapsailing.com``. Instead, they are required to go through any of the so-called "Rights-Holding Broadcaster" (RHB) web sites. There, a "widget" will be embedded into their web sites which works with our REST API to display links to the regattas and races, in particular the RaceBoard.html pages displaying the live and replay races.

Moderators who need to comment on the races shall be given more elaborate permissions and shall be allowed to use the full-fledged functionality of ``tokyo2020.sapsailing.com``, in particular, browse through all aspects of the event, see flag statuses, postponements and so on.

To achieve this effect, the ``tokyo2020-server`` group has the ``sailing_viewer`` role assigned for all users, and all objects, except for the top-level ``Event`` object are owned by that group. This way, everything but the event are publicly visible.

The ``Event`` object is owned by ``tokyo2020-moderators``, and that group grants the ``sailing_viewer`` role only to its members, meaning only the members of that group are allowed to see the ``Event`` object.