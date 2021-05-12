# Setup for the Olympic Summer Games 2020/2021 Tokyo

## Local Installation

For the Olympic Summer Games 2020/2021 Tokyo we use a dedicated hardware set-up to accommodate the requirements on site. In particular, two Lenovo P1 laptops with equal hardward configuration (32GB RAM, Intel Core i9-9880H) will be established as server devices running various services in a way that we can tolerate, with minimal downtimes, failures of either of the two devices.

### Installation Packages

The two laptops run Mint Linux with a fairly modern 5.4 kernel. We keep both up to date with regular ``apt-get update && apt-get upgrade`` executions. Both have an up-to-date SAP JVM 8 (see [https://tools.hana.ondemand.com/#cloud](https://tools.hana.ondemand.com/#cloud)) installed under /opt/sapjvm_8. This is the runtime VM used to run the Java application server process.

Furthermore, both laptops have a MongoDB 3.6 installation configured through ``/etc/apt/sources.list.d/mongodb-org-3.6.list`` containing the line ``deb http://repo.mongodb.org/apt/debian jessie/mongodb-org/3.6 main``. Their respective configuration can be found under ``/etc/mongo .conf``. RabbitMQ is part of the distribution natively, in version 3.6.10-1. It runs on both laptops. Both, RabbitMQ and MongoDB are installed as systemd service units and are launched during the boot sequence. The latest GWT version (currently 2.9.0) is installed under ``/opt/gwt-2.9.0`` in case any development work would need to be done on these machines.

Both machines have been configured to use 2GB of swap space at ``/swapfile``.

### User Accounts

The essential user account on both laptops is ``sailing``. The account is intended to be used for running the Java VM that executes the SAP Sailing Analytics server software. The account is currently still protected by a password that our on-site team should know. There are also still two personal accounts ``uhl`` and ``tim`` and an Eclipse development environment under /usr/local/eclipse.

### Hostnames

We assume not to have DNS available on site. Therefore, for now, we have decided for host names ``sap-p1-1`` and ``sap-p1-2`` for which we have created entries in both laptops' ``/etc/hosts`` file. Currently, when testing in the SAP facilities with the SAP Guest WiFi, possibly changing IP addresses have to be updated there.

The domain name has been set to ``sapsailing.com`` so that the fully-qualified host names are ``sap-p1-1.sapsailing.com`` and ``sap-p1-2.sapsailing.com`` respectively. Using this domain name is helpful later when it comes to the shared security realm established with the central ``security-service.sapsailing.com`` replica set.

### Tunnels

On both laptops there is a script ``/usr/local/bin/tunnels`` which establishes SSH tunnels using the ``autossh`` tool. The ``autossh`` processes are forked into the background using the ``-f`` option. It seems important to then pass the port to use for sending heartbeats using the ``-M`` option. If this is omitted, according to my experience only one of several ``autossh`` processes survives.

On sap-p1-1 two SSH connections are maintained, with the following port forwards:

* tokyo-ssh.sapsailing.com: 10203-->10203; 5763-->rabbit-ap-northeast-1.sapsailing.com:5762; 15763-->rabbit-ap-northeast-1.sapsailing.com; 10201<--10201
* sap-p1-2: 10202-->10202; 10201<--10201

On sap-p1-2, the following SSH connections are maintained:

- tokyo-ssh.sapsailing.com: 10203-->10203; 5763-->rabbit-ap-northeast-1.sapsailing.com:5762; 15763-->rabbit-ap-northeast-1.sapsailing.com; 10202<--10202; 8888<--8888

## AWS Setup

Our primary AWS region for the event will be Tokyo (ap-northeast-1). There, we have reserved the elastic IP ``52.194.91.94`` to which we've mapped the Route53 hostname ``tokyo-ssh.sapsailing.com`` with a simple A-record. The host assigned to the IP/hostname is to be used as a "jump host" for SSH tunnels. It runs Amazon Linux with a login-user named ``ec2-user``. The ``ec2-user`` has ``sudo`` permission.

I added the EPEL repository like this:

```
   yum install https://dl.fedoraproject.org/pub/epel/epel-release-latest-7.noarch.rpm
```

Our "favorite" Availability Zone (AZ) in ap-northeast-1 is "1d" / "ap-northeast-1d".

The same host ``tokyo-ssh.sapsailing.com`` also runs a MongoDB 3.6 instance on port 10203.

For RabbitMQ we run a separate host, based on AWS Ubuntu 20. It brings the ``rabbitmq-server`` package with it (version 3.8.2 on Erlang 22.2.7), and we'll install it with default settings. The RabbitMQ management plugin is enabled using ``rabbitmq-plugins enable rabbitmq_management`` for access from localhost. This will require again an SSH tunnel to the host. The host's default user is ``ubuntu``. The RabbitMQ management plugin is active on port 15672 and accessible only from localhost or an SSH tunnel with port forward ending at this host. RabbitMQ itself listens on the default port 5672. With this set-up, RabbitMQ traffic for this event remains independent and undisturbed from any other RabbitMQ traffic from other servers in our default ``eu-west-1`` landscape, such as ``my.sapsailing.com``. The hostname pointing to the internal IP address of the RabbitMQ host is ``rabbit-ap-northeast-1.sapsailing.com`` and has a timeout of 60s.

An autossh tunnel is established from ``tokyo-ssh.sapsailing.com`` to ``rabbit-ap-northeast-1.sapsailing.com`` which forwards port 15673 to port 15672, thus exposing the RabbitMQ web interface which otherwise only responds to localhost. This autossh tunnel is established by a systemctl service that is described in ``/etc/systemd/system/autossh-port-forwards.service`` in ``tokyo-ssh.sapsailing.com``.

## Landscape Architecture

We have applied for a single SSH tunnel to IP address ``52.194.91.94`` which is our elastic IP for our SSH jump host in ap-northeast-1(d). 

The default production set-up is defined as follows:

### MongoDB

Three MongoDB nodes are intended to run during regular operations: sap-p1-1:10201, sap-p1-2:10202, and tokyo-ssh.sapsailing.com:10203. Since we have to work with SSH tunnels to keep things connected, we map everything using ``localhost`` ports such that both, sap-p1-2 and tokyo-ssh see sap-p1-1:10201 as their localhost:10201, and that both, sap-p1-1 and tokyo-ssh see sap-p1-2:10202 as their respective localhost:10202. Both, sap-p1-1 and sap-p1-2 see tokyo-ssh:10203 as their localhost:10203. This way, the MongoDB URI can be specified as

```
	mongodb://localhost:10201,localhost:10202,localhost:10203/tokyo2020?replicaSet=tokyo2020&retryWrites=true&readPreference=nearest
```

### Application Servers

sap-p1-1 normally is the master for the ``tokyo2020`` replica set. It shall replicate the shared services, in particular ``SecurityServiceImpl``, from ``security-service.sapsailing.com``, like any normal server in our landscape, only that here we have to make sure we can target the default RabbitMQ in eu-west-1 and can see the ``security-service.sapsailing.com`` master directly or even better the load balancer.

SSH local port forwards (configured with the ``-L`` option) that use hostnames instead of IP addresses for the remote host specification are resolved each time a new connection is established through this forward. If the DNS entry resolves to multiple IPs or if the DNS entry changes over time, later connection requests through the port forward will honor the new host name's DNS resolution.

sap-p1-2 normally is a replica for the ``tokyo2020`` replica set, using the local RabbitMQ running on sap-p1-1. Its outbound ``REPLICATION_CHANNEL`` will be ``tokyo2020-replica`` and uses the RabbitMQ running in ap-northeast-1, using an SSH port forward with local port 5673 for the ap-northeast-1 RabbitMQ (15673 for the web administration UI). A reverse port forward from ap-northeast-1 to the application port 8888 on sap-p1-2 has to be established which replicas running in ap-northeast-1 will use to reach their master through HTTP. This way, adding more replicas on the AWS side in the cloud will not require any additional bandwidth between cloud and on-site network, except that the reverse HTTP channel, which uses only little traffic, will see additional traffic per replica whereas all outbound replication goes to the single exchange in the RabbitMQ node running in ap-northeast-1.