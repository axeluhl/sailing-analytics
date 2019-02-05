# Import an Event into the Archive Server

- Go to https://www.sapsailing.com/gwt/AdminConsole.html and log on
- Go to Advanced / Master Data Import
- Select your source server and fetch and select the leaderboard groups
- Start the import
- Load the races on www.sapsailing.com
- Compare servers:
```
java/target/compareServers -el https://wcs2019-miami-master.sapsailing.com https://www.sapsailing.com
```
This will at first complain about the archive having more leaderboard groups than your event server. Copy the ``leaderboradgroups.old.sed`` file to ``leaderboardgroups.new.sed`` and call again with
```
java/target/compareServers -cel https://wcs2019-miami-master.sapsailing.com https://www.sapsailing.com
```
Note the additional "c" option, meaning "continue." When no errors are being reported, continue as follows:

- Remove the remote server reference (Advanced / Remote Server Instances) linking from the archive server to your event server
- Add a rule to the central web server's ``/etc/httpd/conf.d/001-events.conf`` file for the event, such as
```
## World Cup 2019 Miami
Use Event-ARCHIVE-SSL-Redirect wcs2019-miami.sapsailing.com "f393a743-dd49-450f-ab8b-d11522b266f2"
```
- Tell the central archive server to reload its configuration: ``service httpd reload``. Ensure you're seeing a positive confirmation, such as ``Reloading httpd:  [  OK  ]``. You may also test the configuration change if you're not sure by invoking ``apachectl configtest`` from the command line which should respond with ``Syntax OK``.
- Remove the ALB rules (-master and public) from [here](https://eu-west-1.console.aws.amazon.com/ec2/v2/home?region=eu-west-1#ELBRules:type=app;loadBalancerName=Sailing-eu-west-1;loadBalancerId=32b89dbfe7f75097;listenerId=f9212223209ac042) and then the two [target groups](https://eu-west-1.console.aws.amazon.com/ec2/v2/home?region=eu-west-1#TargetGroups:sort=targetGroupName).
- Terminate the event instance(s). This will automatically store the Apache web server logs to ``/var/log/old``.
- Log on to the MongoDB server hosting the archive DB: ``ssh -A root@54.76.64.42``
- Go to the MongoDB directory where scripts for migrating a live DB to the archive exist: ``cd /var/lib/mongodb``
- For your event with a database named ``my-wonderful-event`` run the command ``./archiveDb.sh my-wonderful-event``. This will copy the DB contents of database ``my-wonderful-event`` from the live DB replica set to the archive DB, compute the hashes of both, compare them and if equal remove the original DB on the live replica set as well as the ``my-wonderful-event-replica`` database that usually exists next to it.
- Should you have created additional databases on the live replica set in the context of the event that you don't need anymore or would like to migrate to the archive server, proceed as described above (migration) or as follows (deletion): Use a ``mongo`` shell client and connect to any of the replica set's members, e.g., using ``mongo "mongodb://localhost:10203/?replicaSet=live"``. Then use the following command sequence to get rid of a database, such as ``my-wonderful-event-tablettraining``:
```
live:PRIMARY> use my-wonderful-event-tablettraining
switched to db my-wonderful-event-tablettraining
live:PRIMARY> db.dropDatabase()
```