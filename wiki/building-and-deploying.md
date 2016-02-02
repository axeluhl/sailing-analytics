# Building, Deploying, Stopping and Starting Server Instances

[[_TOC_]]

## Running a Build

Builds are generally executed in either of the following two ways:

 * Check out a branch of your liking from git and run <gitroot>/configuration/buildAndUpdateProduct.sh build (check the various options of this script by invoking it with no arguments). The build results including the p2 product repository are then located in the git workspace.

 * Ensure that [Hudson](http://hudson.sapsailing.com) has a job for your branch; simply push the branch to the central git at sapsailing.com and let Hudson do the job. This is mostly good for knowing whether everything builds and tests ok. Only if you push the special "release" tag, Hudson will build a release and upload it to [releases.sapsailing.com](http://releases.sapsailing.com).

## Deploying Build Results

When the build has been run using the `buildAndUpdateProduct.sh` script, the build results in the git workspace can be deployed to a server environment under `~/servers/<servername>` using the command

    <gitroot>/configuration/buildAndUpdateProduct.sh -s <servername> install

This will copy all necessary files, in particular the p2 product, to the server directory, including the start and stop scripts.

Again, check out the script's options for more and other possibilities including a remote deployment option and a hot deploy of individual bundles into a running server environment.

Deploying build results is generally also possible with a Hudson build, but it is not recommended because a user would need to log in to the Hudson server, know where which build workspace is located and then apply the deployment script there.

## Working with Releases

Particularly when starting an EC2 instance, it is helpful to be able to do that using a well-known release of the product. When an EC2 instance starts, it has a version of the product built into the image and its disk snapshots from which the instance got initialized. This, however, is usually not up to date. To refresh it, you could run a build from a specific git commit, or you could install a **release** previously assembled using the `release` option of the `buildAndUpdateProduct.sh` script as follows:

    <gitroot>/configuration/buildAndUpdateProduct.sh build
    <gitroot>/configuration/buildAndUpdateProduct.sh -w trac@sapsailing.com -n <release-name> release

This will ask you for a comment about the release which goes into the release notes text file that accompanies the release. The build results are packed up into a .tar.gz file which is then uploaded to [releases.sapsailing.com](http://releases.sapsailing.com), using the name optionally provided using the -n parameter with the `release` action, or---as a default---the current timestamp for the release name.

A release can be downloaded and installed to a server by changing to the server's directory, e.g., `~/servers/server` and there executing the `refreshInstance.sh` script with the parameter `install-release <release-name>`. Afterwards, starting the instance works as after a local build.

Instead of building a release yourself you can let the build server to the job. There is a job that looks out for a git tag named `release`. If a new revision is found then an automatic release build is being executed. The result of that is persisted to http://releases.sapsailing.com/. You can create that tag as follows. Make sure that you're on the current master branch before executing the following commands.

<pre>
$ git tag -f release
$ git push -f origin release:release
</pre>

Give the build server some time (20-30 Minutes) until it will have the release ready.
## Working with Environments

There exist a number of preconfigured environment configurations at [releases.sapsailing.com/environments](http://releases.sapsailing.com/environments). Such an environment can be automagically applied to your instance by changing to the servers directory and then executing the `refreshInstance.sh` script with the parameter `install-env <environment-name>`. This will update your env.sh. Make sure to afterwards restart your server.

## Starting, Administrating and Stopping a Java Instance

The product runs as a Java instance consisting largely of an Equinox OSGi server that load and runs various OSGi bundles constituting the product and that contains an embedded Jetty web server. It connects to a database, may serve as a master for replication through a messaging server, may be launched as a replica of some other master instance, may receive wind data in _Expedition_ format on a specific port and can listen for telnet requests to administrate the OSGi server on a specific port. These and other properties are usually configured in a file called `env.sh` which has to be located in the server directory, e.g., in `~/servers/server`, next to the `start` script. When the `start` script is executed, it first sources the env.sh file which sets the various properties which are then passed to the actual Java process, usually in the form of system properties.

After successfully having started a Java instance, it can be administrated through a telnet connection. The port on which the OSGi console listens for incoming connections can be configured in the `env.sh` file. Usually it defaults to port `14888`. Therefore, a `telnet localhost 14888` connects you to the OSGi console where an `ss` command will show you all bundles loaded. Once logged on to the OSGi console, a `disconnect` command will disconnect the telnet session from the OSGi console.

An `exit` command will **terminate** the Java instance after a confirmation. This will obviously stop all services provided by the instance, including all static and dynamic contents served by its web server. You should only trigger the `exit` command if you really know what you are doing!

Stopping a running server has---for your convenience---been wrapped into the `stop` script usually located in the server's directory. Simply executing it will use a telnet connection to the server's OSGi port and trigger an `exit` command automatically. See above for the "know what you're doing" part...

## Automatic Java Instance Start-Up After Boot

When firing up an EC2 instance it can be convenient to not having to log on to have the EC2 instance run a Java instance automatically after it has completed its boot process. This is possible using so-called _user data_. The process of firing up an instance that either builds a certain git commit, installs and starts it after server boot or that downloads and installs a release and starts it is explained [here](http://wiki.sapsailing.com/wiki/amazon-ec2#HowTo).

## App Build Process for iOS and Android

For iOS and Android there are two different build processes in place. At this point (2016-02-02) we're only just beginning to understand how things work, and this is an attempt to summarize the technical steps necessary to get the build done and hand things over to the "Final Assembly" department for deployment to the stores.

Our iOS app build is described by the contents of branch ``central-ios-release-build``. We usually merge ``ubilabs--ios--develop`` into ``master`` and then ``master`` into ``central-ios-release-build``. Builds off the latter branch can be tested using [this Jenkins job](http://dewdfms0036.wdf.sap.corp:8080/job/sapsailingcapture-GIT-DEV-OD-ENTERPRISE/). Run a [Build with Parameters](http://dewdfms0036.wdf.sap.corp:8080/job/sapsailingcapture-GIT-DEV-OD-ENTERPRISE/build?delay=0sec) and enter ``central-ios-release-build`` as the TREEISH. The build should succeed and produce the .ipa file somewhere in the [build's target Maven repository](http://dewdfms0036.wdf.sap.corp:8080/job/sapsailingcapture-GIT-DEV-OD-ENTERPRISE/ws/.m2/repository/com/sap/sailing/mobile/ios/SAPTracker/).