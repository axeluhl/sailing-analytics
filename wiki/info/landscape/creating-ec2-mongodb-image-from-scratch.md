# Setting up an EC2 Image for a MongoDB Replica Set from Scratch

## Image and Basic Packages

Start with an Amazon Linux 2 AMI (HVM), SSD Volume Type - ami-0fad7378adf284ce0 (64-bit x86) image.

Add the yum repository for MongoDB 3.6 by creating ``/etc/yum.repos.d/mongodb-org-3.6.repo`` as follow:

```
[mongodb-org-3.6]
name=MongoDB Repository
baseurl=https://repo.mongodb.org/yum/amazon/2013.03/mongodb-org/3.6/x86_64/
gpgcheck=1
enabled=1
gpgkey=https://www.mongodb.org/static/pgp/server-3.6.asc
```

Then:

```
yum install -y mongodb-org-server mongodb-org-mongos mongodb-org-shell mongodb-org-tools
mkfs.xfs /dev/nvme0n1
mount /dev/nvme0n1 /var/lib/mongo
```

## MongoDB Set-Up

In file ``/etc/mongod.conf`` comment the line
```
#  bindIp: 127.0.0.1  # Listen to local interface only, comment to listen on all interfaces.
```
and instead add
```
  bindIp: 0.0.0.0
```

so that MongoDB listens on all interfaces, not only localhost. Furthermore, set the ``directoryPerDB`` property to ``true`` and provide a replication set name using the ``replication.replSetName`` property.

Here is the full ``/etc/mongod.conf`` file:

```
# mongod.conf

# for documentation of all options, see:
#   http://docs.mongodb.org/manual/reference/configuration-options/

# where to write logging data.
systemLog:
  destination: file
  logAppend: true
  path: /var/log/mongodb/mongod.log

# Where and how to store data.
storage:
  dbPath: /var/lib/mongo
  journal:
    enabled: true
  directoryPerDB: true
#  engine:
#  mmapv1:
#  wiredTiger:

# how the process runs
processManagement:
  fork: true  # fork and run in background
  pidFilePath: /var/run/mongodb/mongod.pid  # location of pidfile
  timeZoneInfo: /usr/share/zoneinfo

# network interfaces
net:
  port: 27017
#  bindIp: 127.0.0.1  # Listen to local interface only, comment to listen on all interfaces.
  bindIp: 0.0.0.0

#security:

#operationProfiling:

replication:
  replSetName: live

#sharding:

## Enterprise-Only Options

#auditLog:

#snmp:
```

Before being able to start the mongod service, more configuration is required:

```
chown mongod /var/lib/mongo/
chgrp mongod /var/lib/mongo/
```

will change the ownerships of the directory mounted from ephemeral storage accordingly, so the MongoDB daemon can write to it. The execute ``systemctl start mongod.service`` to launch the MongoDB process.

## MongoDB Replica Set Configuration

Connect to the MongoDB on that instance, then issue the command ``rs.initiate()`` in order to turn the instance into the "seed" of a replica set. You can then, for the time being, ``quit()`` the mongo shell. Re-connecting, e.g., with ``mongo "mongodb://localhost:27017/?replicaSet=live"``, will show the ``PRIMARY`` of the new replica set.