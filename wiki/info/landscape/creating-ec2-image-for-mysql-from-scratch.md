# Setting up an Instance for the MySQL / MariaDB Bugzilla Database

Our Bugzilla system at [bugzilla.sapsailing.com](https://bugzilla.sapsailing.com) uses a relational database to store all the bugs and issues. This used to be a MySQL database and has been migrated to MariaDB at the beginning of 2024.

We don't provide a dedicated AMI for this because we don't need to scale this out or replicate this by any means. Instead, we provide a script to set this up, starting from a clean Amazon Linux 2023 instance.

Launch a new instance, based on the latest Amazon Linux 2023 AMI maintained by AWS, and configure the root volume size to be, e.g., 16GB. As of this writing, the total size consumed by the database contents on disk is less than 1GB. Tag the volume with a tag key ``WeeklySailingInfrastructureBackup`` and value ``Yes`` to include it in the weekly backup schedule.

When the instance has finished booting up, run the following script, passing the external IP address of the instance as mandatory argument:
```
  configuration/mysql_instance_setup/setup-mysql-server.sh a.b.c.d
```
where ``a.b.c.d`` stands for the external IP address you have to specify. Before the IP address you may optionally specify the passwords for the ``root`` and the ``bugs`` user of the existing database to be cloned to the new instance. Provide the ``root`` password with the ``-r`` option, the ``bugs`` password with the ``-b`` option. Passwords not provided this way will be prompted for.

The script will then transfer itself to the instance and execute itself there, forwarding the passwords required. On the instance, it will then establish the periodic management of the login user's ``authorized_keys`` file for all landscape managers' keys, install the packages required (in particular mariadb105-server and cronie), then run a backup on the existing ``mysql.internal.sapsailing.com`` database using the ``root`` user and its password. The ``mysqldump`` client for this is run on ``sapsailing.com``, and the result is stored in the ``/tmp`` folder on the new instance where it is then imported. The import is a bit tricky in case this is a migration from MySQL to MariaDB where the users table has become a view. Therefore, a few additional ``DROP TABLE`` and ``DROP VIEW`` commands are issued before importing the data. When the import is complete, user privileges are flushed so they match with what has been imported. The DB is then re-started in "safe" mode so that the user passwords can be adjusted, in case this was a migration from MySQL to MariaDB. Finally, the DB is restarted properly with the new user passwords.

The instance then is generally available for testing. Run a few ``mysql`` commands, check out the ``bugs`` database and its contents, especially those of the ``bugs.bugs`` table. If this all looks good, switch the DNS record for ``mysql.internal.sapsailing.com`` to the private IP of the new instance. This will be used by the Bugzilla installation running on our central reverse proxy. When this is done you can consider stopping and ultimately terminating the old DB server.
