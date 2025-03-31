# Cook Book with useful recipes

[[_TOC_]]

### Switch from one Archive server to another

When deploying a new version of the "Archive" server which hosts our sapsailing.com landing page and all historic events that don't have their own dedicated server instance, for availability reasons we always pull up a new server instance next to the currently running one, prepare and test it thoroughly and only then switch to it by updating the Apache configuration accordingly.

Two files on our web server need to be edited, requiring `root` permissions: `/etc/httpd/conf.d/000-macros.conf` and `/etc/httpd/conf.d/001-events.conf`. In the `000-macros.conf` file you will find several lines reading something like

   Use Rewrite 172.31.22.177 8888

Those need to be updated to point to the internal IP address of the new "to-be" archive server. You can find this internal IP address in the Amazon EC2 administration console in the instances list by selecting your new archive server instance and checking the "Private IPs" field in the "Description" tab.

In `001-events.conf` there his a single entry that needs updating. It looks like this:

  Use Home www.sapsailing.com 172.31.22.177 8888

and requires the same IP address update. When done, issue the command

  service httpd reload

which will reload the Apache configuration on the fly. Only then you may shut down the previous archive server, and only after convincing yourself that the switch was successful and that requests to sapsailing.com now are handled by your new archive server.

### Export from MongoDB

To export data from MongoDB you simply have to use the monogexport command. It will export data to human readable JSON format. Make absolutely sure to use fields backed by an index in your query otherwise it can put MongoDB under heavy load and take ages.

#### Wind

`/opt/mongodb/bin/mongoexport --port 10202 -d winddb -c WIND_TRACKS -q "{'REGATTA_NAME': 'ESS 2013 Muscat (Extreme40)'}" > /tmp/ess2013-muscat-wind.json`


### Import to MongoDB

Importing requires data to be in JSON format (as exported by mongoexport). To make sure that old entries just get updated and not overwritten you must use the --upsert parameter.

#### Wind

`/opt/mongodb/bin/mongoimport --port 10202 -d winddb -c WIND_TRACKS --upsert /tmp/ess2013-muscat-wind.json`

### Migrate Master Data along with Score Corrections, etc, without touching the mongodb console

In the Admin Panel you can now find a tab called "Master Data Import". Here you can import all data corresponding to a leaderboard group from a remote server, excluding wind and the actual TrackedRaces.

#### Listing the available leaderboard groups

At first you need to enter the remote host. This could for example be "http://live2.sapsailing.com/" if you are on an archive server and you want to import the information of a recent live event that is still hosted on a live server. If you hit Enter or click the Button "Fetch Leaderboard Group List", all available leaderboard groups from the remote host will be displayed, if the connection was successful.

#### Importing all data for selected leaderboard groups

You can now select the leaderboard groups you want to import. Multi-selection is allowed. If you have already added objects like events or leaderboards where names collide with those that are to be imported, the "override" option becomes relevant. It should be checked if you want to have the same ID's, race-columns, race-log-events, score corrections as on the remote server, but it can overwrite some of the data you entered before. 
Now click "Import selected Leaderboard Groups" to start the actual import process. If successful, a pop-up will show you how many events, regattas, leaderboards and leaderboard groups where imported.

#### Media

For now, all media links are imported (even if they are not in the same time-frame as the leaderboard-grouped races). The process also looks for the override flag to decide whether it should overwrite the existing row, when there is an id-conflict.

### Remove duplicates from WIND_TRACK collection

The steps below can also be applied to other collections by adjusting the unique index parameters.
These steps need to be done, when the amount of duplicates in the collection exceeds 1,000,000.

<pre>
#1
#Dump the collection (or use mongoexport but you need to take care of rebuilding all indexes)
mongodump --db winddb --collection WIND_TRACKS --out mongodump_wind_tracks

#2
#On mongo shell:
#Option a: remove all (very slow but collection meta data is kept, so there is no need to recreate indexes and such)
db.WIND_TRACKS.remove()

#Option b: drop (almost instant, but you need to re-setup all indexes, since all meta data will be wiped (when using mongodump/mongorestore, metadata is taken care of too)
db.WIND_TRACKS.drop()

#3
#Recreate collection
db.createCollection("WIND_TRACKS")

#3.1
#Rebuild other indexes (is also backed up during mongodump, but not during mongoexport) 
# so no need for this when using mongodump and mongorestore
db.WIND_TRACKS.ensureIndex({ "RACE_ID" : 1 })
db.WIND_TRACKS.ensureIndex({ "REGATTA_NAME" : 1 })
db.WIND_TRACKS.ensureIndex({ "EVENT_NAME" : 1, "RACE_NAME" : 1 })
#Don't really know if this one is necessary or if it even does anything. This was in the old collection meta data.
db.WIND_TRACKS.ensureIndex({ "REGATTA_NAME" : null })


#3
#Set the new unique index on the empty collection
db.WIND_TRACKS.ensureIndex( { "RACE_ID": 1 , "WIND_SOURCE_NAME" : 1, "WIND_SOURCE_ID": 1, "WIND.TIME_AS_MILLIS": 1}, {unique : true, dropDups : true})

#dropDups option is not necessary, as the collection should be empty at that point. But it does not hurt. The mongod process should say that 0 duplicates were found.

#4
#Restore the documents dumped in step 1
mongorestore mongodump_wind_tracks
</pre>


### Hot Deploy Java Packages to a running Server

Sometimes you need to deploy code to a running OSGi server without restarting the whole process. To ease this process the script buildAndUpdateProduct.sh provides an action called _hot-deploy_. To deploy a changed package you first need to know the fully qualified name. In most cases (for com.sap* packages) this corresponds to the package name (e.g. com.sap.sailing.gwt.ui). To hot-deploy the package _com.sap.sailing.server.gateway_ one can use the following call:

`buildAndUpdateProduct.sh  -l telnetPortOfServerInstance -n com.sap.sailing.server.gateway hot-deploy`

This will first check if versions really differ and tell you the versions (installed, to-be-deployed). You can then decide to execute deployment. Deployment will not overwrite the old package but copy the new package to $SERVER_HOME/plugins/deploy. If the OSGi server is reachable on a telnet port (that you can specify with -p parameter) then you don't need to worry about the OSGi reload process it will be performed automagically. If no server can be reached then you'll get detailed instructions on how to install new package. It will then look like this:

<pre>
PROJECT_HOME is /Users/spamies/Projects/sailing/code
SERVERS_HOME is /Users/spamies/Projects/sailing/servers
OLD bundle is com.sap.sailing.monitoring with version 1.0.0.201303252301
NEW bundle is com.sap.sailing.monitoring with version 1.0.0.201303252302

Do you really want to hot-deploy bundle com.sap.sailing.monitoring to /Users/spamies/Projects/sailing/code/master? (y/N): Continuing
Copied com.sap.sailing.monitoring_1.0.0.201303252302.jar to /Users/spamies/Projects/sailing/servers/master/plugins/deploy

ERROR: Could not find any process running on port . Make sure your server has been started with -console 
I've already deployed bundle to /Users/spamies/Projects/sailing/servers/master/plugins/deploy/com.sap.sailing.monitoring_1.0.0.201303252302.jar
You can now install it yourself by issuing the following commands:

osgi> ss com.sap.sailing.monitoring
21   ACTIVE   com.sap.sailing.monitoring_1.0.0.201303252302
osgi> stop 21
osgi> uninstall 21
osgi> install file:///Users/spamies/Projects/sailing/servers/master/plugins/deploy/com.sap.sailing.monitoring_1.0.0.201303252302.jar
osgi> ss com.sap.sailing.monitoring
71   INSTALLED   com.sap.sailing.monitoring_1.0.0.201303252302
osgi> start 71
</pre>

### Debug Tests Running During Full Build

If you quickly want to run tests in an OSGi/Maven environment but debug locally, run a full build locally using `buildAndUpdateProduct.sh` with all other test bundles commented out in `java/pom.xml`. You may then go to the test bundle/fragment directory and use `mvn
 -s ../../settings.xml -P debug.with-proxy install`. This will build the test bundle again and launch the tests, waiting for your debugger to connect. Connect your Eclipse debugger's remote debug launch to port 8000 on localhost.

Don't forget to un-comment the test bundles in the `java/pom.xml` file when you're done.

### Display Line Endings for File

Sometimes different line endings get mixed. To display all lin endings for each line of a given file use the following command:

`perl -p -e 's[\r\n][WIN\n]; s[(?<!WIN)\n][UNIX\n]; s[\r][MAC\n];' <FILE>`

### Finding Packages for Deployment in Target Platform

Sometimes you want to add libraries to the target platform but you don't have correctly formatted JAR files. In this case you can find some files here: [Eclipse ORBIT](http://download.eclipse.org/tools/orbit/downloads/drops/R20130118183705/)

### Debug Jetty

To gather further information regarding this bug I finally got the sourcecode of org.eclipse.jetty.* imported that way that one can debug w/o touching the OSGi environment. It is a simple as this:

- Add jetty.source package to target (already included in jetty.bundle location)
- Add MANIFEST.MF based dependency to com.sap.sailing.server
- Sourcecode is now available for debugging

I now know why starting and stopping a second server destroy the web context of the first one. This happens because every JAR file that is registered as a context in Jetty is being extracted to java.io.tmpdir/jetty-<ip>-<port>/ and loaded from there. The code doing this is hidden in WebInfConfiguration and triggered by an instance of the DeploymentManager.

I ran some load testing based on apache bench tool (ab) with 10000 requests (from which 1000 were concurrent) to /gwt/AdminConsole.html to check if this leads to a change in socket count or memory consumption. It didn't and therefore Jetty has no apparent leaks. I also tested with 404 (/gwt/ThisYieldsA404) because there had been comments about Jetty leaking with 404 but no negative results here either.

I will update start scripts for each server to use $SERVER_DIR/tmp as temporary directory thus making sure that deployed binaries can not overwrite themselves.

One can find more information about the Jetty architecture here: http://docs.codehaus.org/display/JETTY/Architecture

### Ignore GIT line endings during merge

Following http://stackoverflow.com/questions/861995/is-it-possible-for-git-merge-to-ignore-line-ending-differences one can use `git config merge.renormalize true`

### Remove association between local and remote branch in GIT

To remove the association between the local and remote branch, and delete the local branch, run:

<pre>
git config --unset branch.&lt;branch&gt;.remote
git config --unset branch.&lt;branch&gt;.merge
</pre>

### Tunnel UDP packets over SSH tunnel (TCP)

Assume you want to forward UDP packets from machine A (port 2014) to machine B (port 2014) over an SSH tunnel. This is not that easy because an SSH tunnel only works for TCP packets. So let's use socat for this purpose:

- Open a tunnel from machine A to machine B: <pre>A$ ssh -L 6667:localhost:6667 user@B</pre>
- Once opened activate a TCP to UDP forwarding on machine B: <pre>B$ socat tcp4-listen:6667,reuseaddr,fork UDP:localhost:2014</pre>
- Activate UDP to TCP forwarding on machine A: <pre>A$ socat -T15 udp4-recvfrom:2014,reuseaddr,fork tcp:localhost:6667</pre>

Now also UDP packets get transmitted through.

### Set limits for a Linux server

Especially our EC2 CentOS Linux installations use `/etc/security/limits.conf` and `/etc/security/limits.d/*` to specify upper bounds for the number of processes / threads and file handles that a user may use. On EC2, in particular the `/etc/security/limits.d/90-nproc.conf` file set a small limit for the soft limit for the number of processes:
<pre>
    *          soft    nproc     1024
</pre>
Changing this to a greater value, such as 65536, helps, e.g., to avoid the following message:
<pre>
    su: cannot set user id: Resource temporarily unavailable
</pre>

Make sure to also set the right limits in `/etc/profile.d/sailing.sh`!!

### Set limits (ulimit) for a running process

<pre>
[root@ip-172-31-26-232 1388]# cd /proc/1388
[root@ip-172-31-26-232 1388]# echo -n "Max processes=150000:150000" > limits 
[root@ip-172-31-26-232 1388]# cat limits 
Limit                     Soft Limit           Hard Limit           Units     
Max cpu time              unlimited            unlimited            seconds   
Max file size             unlimited            unlimited            bytes     
Max data size             unlimited            unlimited            bytes     
Max stack size            10485760             unlimited            bytes     
Max core file size        0                    unlimited            bytes     
Max resident set          unlimited            unlimited            bytes     
Max processes             150000               150000               processes 
Max open files            30000                30000                files     
Max locked memory         65536                65536                bytes     
Max address space         unlimited            unlimited            bytes     
Max file locks            unlimited            unlimited            locks     
Max pending signals       273211               273211               signals   
Max msgqueue size         819200               819200               bytes     
Max nice priority         0                    0                    
Max realtime priority     0                    0                    
Max realtime timeout      unlimited            unlimited            us        
</pre>

### Show threads consuming most CPU time for a Java process

Assume that you want to know which threads exactly eat up all the CPU time. This can be done by using JConsole and the JTop plugin. You can download the plugin here `wget http://arnhem.luminis.eu/wp-content/uploads/2013/10/topthreads-1.1.jar`. The you can open jconsole like so:

<pre>
/opt/jdk1.7.0_02/bin/jconsole -pluginpath topthreads-1.1.jar
</pre>

### Activate logging of GPS fixes

It can happen that you want to log every GPS fix added to a track. If you want to see these then add the following to your logging.properties file. NEVER use it in production.

<pre>
com.sap.sailing.domain.tracking.impl.DynamicTrackedRaceImpl.level=FINEST
</pre>

If you want to log all GPS fixes sent to the client (RaceBoard) then activate the following:

<pre>
com.sap.sailing.gwt.ui.server.SailingServiceImpl.level=FINEST
</pre>

### Remove GWT SerializationException messages from log

When installing a new version of server code that contains changes related to GWT it can happen that viewers that did not reload their view after the server restart will trigger a SerializationException because the information needed to serialize is no longer matching the one on the server. GWT will start throwing loads of SerializationException that are filling up your logs quite quick. As these messages can be considered blather it is good to disable them to be able to see really relevant messages. Disabling is not that easy as Jetty is using it's own logging system. But there is a trick. Just add the following to your VM argument list (ADDITIONAL_JAVA_ARGS) and you're good to enjoy a life without these messages.

<pre>
-Dorg.eclipse.jetty.LEVEL=OFF -Dorg.eclipse.jetty.util.log.class=org.eclipse.jetty.util.log.StdErrLog
</pre>

### Review branch in eclipse

Before merging into master in some cases you want to review a branch. The easiest way to do this is to use EGit from within Eclipse. Here are the steps based on reviewing branch tracapi. This tutorial assumes that you have checked out the current master branch!

- Open "GIT Repository Exploring" and select the branch that you want to review. Right click on that branch and select "Synchronize with Workspace".

- A new view ("Team Synchronizing") will open and show you the changes on the left side. For each file you can now check for changes. Sometimes there will be resources that you do not want to review. In that case simply select a resource and hit "Remove from view".