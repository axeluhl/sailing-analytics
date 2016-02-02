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

When the test build was successful, the branch contents need to be merged into the central git repository's branch ``fa/rel-1.x``, e.g., ``fa/rel-1.0``. Pushing immediately to this branch to the SAP-internal git is not permitted by Gerrit rules. Instead, it is necessary to push to ``refs/for/fa/rel-1.x``. Furthermore, the commit to be pushed needs to have a ``Change-Id:`` and a ``CR-Id:`` tag in the commit's comment. The ``Change-Id:`` is produced by a commit hook that can be installed in each developer's local git environment by using the following command:

```gitdir=$(git rev-parse --git-dir); scp -p -P 29418 d000000@git.wdf.sap.corp:hooks/commit-msg ${gitdir}/hooks/```

The ``CR-Id:`` is more tricky. It is produced by the Java Correction Workbench where a Correction Request needs to be created. This can be achieved using [this link](https://css.wdf.sap.corp/sap/bc/bsp/spn/jcwb/default.htm?newCMForProject=sapsailingcapture&newCMComponentName=SV-COE-MSO-CDP) which is also contained in the lower left corner of our [Project Portal Page](https://projectportal.neo.ondemand.com/projects/sapsailingcapture). The correction request shall describe what has been corrected. In our project's context it seems useful to link the correction we're trying to release to a bug in our Bugzilla. For this, select "External" as the "Issue Type" and enter the bug's URL as well as a short text. The "ACH Component" should have been pre-filled and should read ``SV-COE-MSO-CDP``. If not, make sure you enter the text ``SV-COE-MSO-CDP`` and wait for the tool to complete this to "CashDesk Plus" before you press "Next". Ultimately, you will see a Correction Request now in the Java Correction Workbench and you should be able to copy its technical ID to use for the ``CR-Id:`` commit tag. If you hover the mouse over the correction request's ID, a small "copy" button will appear, easing your life.

Once you have the Correction Request's ID, you need to amend your latest commit that shall be pushed onto fa/rel-1.x:

   <pre>git commit --amend</pre>

Add the ``CR-Id: ...`` line _immediately_ under the ``Change-Id:`` line and produce the new commit. Then push it to Gerrit using

   <pre>git push origin HEAD:refs/for/fa/rel-1.x</pre>

It will end up in your [list of Gerrit changes](https://git.wdf.sap.corp/#/dashboard/self). Press the ``Reply`` button located in the middle of the grey bar at the top of the page. You can then vote +2 and +1 for the change and post your review. After a short moment, the blue ``Submit`` button should appear. Press it, and the branch will be merged into ``fa/rel-1.x``. Final Assembly will need to be notified using a BCP ticket, such as a Handover ticket that can be created using [this link](https://support.wdf.sap.corp/sap/bc/dsi/ii/create_zini?sap-language=EN&system_id=BCV&short_description=Handover%20%3cPPMS%20Software%20Component%20Version%3e&priority=3&main_impact=A&category_label=XX-INT-NAAS-MOBILE&incident_description=Requesting%2520Development%2520to%2520Production%2520Handover%2520for%2520the%2520App%253A%250A%253CFull%2520App%2520Name%253E%250A%250AProgram%2520Repository%2520Link%253A%250AProgram%2520Name%253A%250APPMS%2520Software%2520Component%2520Version%2520Name%253A%250APlatform%253A%2520%253CAndroid%2520%252F%2520iOS%2520%252F%2520TFS%2520(Windows)%253E%250A%250AShipment%2520Channel(s)%253A%250A%255B%255D%2520Indirect%2520shipment%2520(promotion%2520to%2520Nexus%2520only)%250A%255B%255D%2520Apple%2520App%2520Store%250A%255B%255D%2520Google%2520Play%250A%255B%255D%2520Google%2520Chrome%2520Store%250A%255B%255D%2520Amazon%2520App%2520Store%250A%255B%255D%2520BlackBerry%2520World%250A%255B%255D%2520Windows%25208%2520Store%250A%255B%255D%2520Windows%2520Phone%2520Store%250A%255B%255D%2520SMP%2520-%2520Binary%250A%255B%255D%2520SMP%2520-%2520Source%2520Code%250A%250AiOS%2520%252F%2520Android%250A----------------%250ALink%2520to%2520Project%2520Portal%253A%250ASource%2520Code%2520Project%2520Path%253A%2520%253CPerforce%2520path%2520with%2520codeline%2520%252F%2520Git%2520Path%253E%250A%250AIf%2520project%2520is%2520using%2520the%2520LeanDI%2520environment%252C%2520code%2520signing%2520is%2520enabled%2520and%2520will%2520remain%2520enabled%2520for%2520All%2520Release%2520builds%253A%2520%253Cyes%2520%252F%2520no%253E%250A%250ATFS%2520(Windows)%250A-----------------%250ATeam%2520Project%2520Collection%253A%250ATeam%2520Project%253A%250AREL%2520Branch%2520Path%253A%250AREL%2520Build%2520Definition%2520Name%253A%250A%250ACode%2520Signing%2520Request%2520Ticket%2520(if%2520applicable)%253A%2520%253CBCP%2520Ticket%2520Number%253E%250A%250AList%2520of%2520build%2520Dependencies%253A%2520%253CNexus%2520artifacts%2520including%2520libraries%253E%250A%250ALink%2520to%2520completed%2520Metaman%2520Entry%2520(for%2520External%2520Stores)%253A%250A%250AContacts%2520%253CUser%2520IDs%253E%250AProduction%2520Project%2520Lead%253A%250AProduct%2520Owner%253A%250AResponsible%2520for%2520Handover%253A%250AResponsible%2520for%2520Technical%2520issues%252Fquestions%253A%250AOthers%253A).