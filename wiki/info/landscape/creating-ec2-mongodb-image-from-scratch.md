Setting up an EC2 Image for a MongoDB Replica Set from Scratch

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

To deal with the set-up of the ephemeral volume, there are a number of ``systemd`` units that can be found in the git repository at ``configuration/mongo_instance_setup``. The ``ephemeralvolume.service`` ensures that the volume exists and is formatted with the XFS file system. This service uses the ``/usr/local/bin/ephemeralvolume`` script to do so. After that, the volume is mounted to ``/var/lib/mongo`` using the ``var-lib-mongo.mount`` unit. The ownerships are adjusted after that by the ``chownvarlibmongo.service`` unit to the ``mongod`` user. This is then also the last unit required to run before the ``mongod.service``.

Then execute ``systemctl enable mongod.service`` to launch the MongoDB process and ensure it is always launched when the instance (re-)boots.

## MongoDB Replica Set Configuration

Connect to the MongoDB on that instance, then issue the command ``rs.initiate()`` in order to turn the instance into the "seed" of a replica set. You can then, for the time being, ``quit()`` the mongo shell. Re-connecting, e.g., with ``mongo "mongodb://localhost:27017/?replicaSet=live"``, will show the ``PRIMARY`` of the new replica set.

Connected to the PRIMARY using the ``mongo`` shell, a replica can be added using ``rs.add({host: "hostname:port"})``. For additional options see [here](https://docs.mongodb.com/manual/reference/method/rs.add/). To add a hidden replica on ``dbserver.internal.sapsailing.com:10203``, use ``rs.add({host: "dbserver.internal.sapsailing.com:10203", buildIndexes: false, hidden: true, priority: 0})``.


TODO: automate the initialization and replica set extension using "Addition Details" in the instance; create a MongoDB script that is executed during start-up; if no replica set exists and no user detail specifies where the primary is, run ``rs.initiate()``. If a replica set already exists, leave things unchanged. If no replica set exists and in a user detail something like ``REPLICA_SET_NAME=...`` and ``REPLICA_SET_PRIMARY=...`` is provided, add the local node as a secondary to the primary / replica set specified.
