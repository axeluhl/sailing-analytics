# Sailing Analytics on a stick

This is a package to enable running the SAP Sailing Analytics from a stick.
It is provided as-is without warranty. The most common usecase is to have a laptop 
running in a loung that replicates data from a master server that runs in the cloud.

## Create a stick

- Copy the contents of $GIT/stick to the root of your USB stick
- Create a directory named `server` and in there `logs` and `tmp`
- Extract the file http://releases.sapsailing.com/stick-binaries-1.0.tar.gz to the root of your USB stick
- Install a release by executing `scripts/refreshInstance.sh install-release <release>`

## Configuration

- (Windows) Copy templates/env.bat.template to the root of the USB stick and rename it to
env.bat. Now open it with your favourite text editor and adapt the values to your needs.
As Windows does not handle data writes on a stick well it is recommended that you also
edit mongodb-windows.cfg and set the dbpath to an existing directory on your hard disk.

- (Linux) Copy templates/env.sh.template to the root of your USB stick and rename it to
env.sh. Fire up a vi and adapt the values. It can be wise to also have a look at the conf
for the database that is located in mongodb.cfg. You can specify a separate directory
on your harddisk or a ramdisk for better database performance.

If you have configured the server to replicate then you absolutely need to make sure
that the server version on the stick corresponds to the one that is running in the cloud.

## Starting

- (Windows) First you need to start the database by executing start-mongo.bat. Wait at least
5 minutes until MongoDB has created all necessary files. Before the database hasn't been
initialized you won't be able to start the Sailing Analytics server. After the database
has started up you can start up the analytics server by running start.bat. Watch out for any 
error messages that might come up. After the server has been started you should be able to
reach it by using http://127.0.0.1:8888/.

- (Linux) All you need is to start the whole thing by executing start.sh. This will take care
of starting up the database and then starting the analytics server.