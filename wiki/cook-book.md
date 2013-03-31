# Cook Book with useful recipes

[[_TOC_]]

### Export from MongoDB

To export data from MongoDB you simply have to use the monogexport command. It will export data to human readable JSON format. Make absolutely sure to use fields backed by an index in your query otherwise it can put MongoDB under heavy load and take ages.

`/opt/mongodb/bin/mongoexport --port 10202 -d winddb -c WIND_TRACKS -q "{'REGATTA_NAME': 'ESS 2013 Muscat (Extreme40)'}" > tmp/ess2013-muscat-wind.json`

### Hot Deploy Java Packages to a running Server

Sometimes you need to deploy code to a running OSGi server without restarting the whole process. To ease this process the script buildAndUpdateProduct.sh provides an action called _hot-deploy_. To deploy a changed package you first need to know the fully qualified name. In most cases (for com.sap* packages) this corresponds to the package name (e.g. com.sap.sailing.gwt.ui). To hot-deploy the package _com.sap.sailing.server.gateway_ one can use the following call:

`buildAndUpdateProduct.sh -n com.sap.sailing.server.gateway hot-deploy`

This will first check if versions really differ and tell you the versions (installed, to-be-deployed). You can then decide to execute deployment. Deployment will not overwrite the old package but copy the new package to $SERVER_HOME/plugins/deploy. If the OSGi server is reachable on a telnet port (that you can specify with -p parameter) then you don't need to worry about the OSGi reload process it will be performed automagically. If no server can be reached then you'll get detailed instructions on how to install new package.
