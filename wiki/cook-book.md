# Cook Book with useful recipes

[[_TOC_]]

### Export from MongoDB

To export data from MongoDB you simply have to use the monogexport command. It will export data to human readable JSON format. Make absolutely sure to use fields backed by an index in your query otherwise it can put MongoDB under heavy load and take ages.

#### Wind

`/opt/mongodb/bin/mongoexport --port 10202 -d winddb -c WIND_TRACKS -q "{'REGATTA_NAME': 'ESS 2013 Muscat (Extreme40)'}" > /tmp/ess2013-muscat-wind.json`

#### Score Corrections

`/opt/mongodb/bin/mongoexport --port 10202 -d winddb -c LEADERBOARDS -q "{LEADERBOARD_NAME: 'ESS 2013 Singapore (Extreme40)'}" > /tmp/singapore.json`

### Import to MongoDB

Importing requires data to be in JSON format (as exported by mongoexport). To make sure that old entries just get updated and not overwritten you must use the --upsert parameter.

#### Wind

`/opt/mongodb/bin/mongoimport --port 10202 -d winddb -c WIND_TRACKS --upsert /tmp/ess2013-muscat-wind.json`

#### Score Corrections

`/opt/mongodb/bin/mongoimport --port 10202 -d winddb -c LEADERBOARDS --upsert /tmp/singapore.json`

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