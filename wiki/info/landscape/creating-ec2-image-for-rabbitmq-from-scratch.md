# Setting up a RabbitMQ Server Instance

RabbitMQ is hard to install on latest versions of Amazon Linux (e.g., 2, or 2023). Therefore, we use a latest Debian 12 default image to start with.

Configure the root volume to be at least 8GB. The empty installation takes about 1.6GB, so you will have enough room for messages queued persistently.

When the instance has finished booting and SSH access is possible, invoke the following script, providing the instance's external IP address as only parameter:
```
  configuration/rabbitmq_instance_setup/setup-rabbitmq-server.sh a.b.c.d``
```
where ``a.b.c.d`` is the external IP address of your fresh instance.

The script will ensure the login user's ``authorized_keys`` are updated periodically to contain those of the landscape managers, then will install the necessary packages, particularly ``rabbitmq-server`` and, to get real log files under ``/var/log``, the ``syslog-ng`` package. It then enables the ``rabbitmq_management`` plugin, so access to the management UI becomes possible through port ``15672``. The configuration file under ``/etc/rabbitmq/rabbitmq.conf`` is patched such that guest logins are possible also from non-localhost addresses, by adding the ``loopback_users = none`` directive to the config file. It finally (re-)starts the RabbitMQ server to let these config changes take effect.

Your RabbitMQ server then should be ready to handle requests. Test this by invoking the management UI, e.g., through an ssh port forward to port ``15672``. When this seems good, pick a smart time to change the DNS record for ``rabbit.internal.sapsailing.com`` because there will be a short time of interruptions on all application processes currently connected to the old RabbitMQ which you then have to stop. Those client applications will temporarily lose connection, but our replication component will re-establish these connections, using the DNS name which gets resolved again based on the DNS entry's TTL.

Then associate the elastic IP ``54.76.64.42`` as the external IP of the new instance. This will let ``rabbit.sapsailing.com`` point to the public IP of the instance.

Add a tag with key ``RabbitMQEndpoint`` and value ``5672``, specifying the port on which the RabbitMQ server listens. This tag can be used by our landscape automation procedures to discover the RabbitMQ default instance in the region.
