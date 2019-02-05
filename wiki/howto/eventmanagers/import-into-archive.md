* Import an Event into the Archive Server

- Go to https://www.sapsailing.com/gwt/AdminConsole.html and log on
- Go to Advanced / Master Data Import
- Select your source server and fetch and select the leaderboard groups
- Start the import
- Load the races on www.sapsailing.com
- Compare servers: ```
   java/target/compareServers -el https://wcs2019-miami-master.sapsailing.com https://www.sapsailing.com```
This will at first complain about the archive having more leaderboard groups than your event server. Copy the ``leaderboradgroups.old.sed`` file to ``leaderboardgroups.new.sed`` and call again with ```
   java/target/compareServers -cel https://wcs2019-miami-master.sapsailing.com https://www.sapsailing.com```
Note the additional "c" option, meaning "continue." When no errors are being reported, you can switch the event to the archive server by adding a rule to the central web server's ``/etc/httpd/conf.d/001-events.conf`` file for the event, then removing the ALB rule and then the two target groups.