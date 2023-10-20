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

A release can be downloaded and installed to a server by changing to the server's directory, e.g., `~/servers/server` or whatever the sub-directory of the server installation is, and there executing the `refreshInstance.sh` script with the parameter `install-release <release-name>`. Afterwards, starting the instance works as after a local build.

A sample session could look like this:

<pre>
$ ssh sailing@34.250.136.229
$ cd servers/ubilabs-test
$ ./refreshInstance.sh install-release build-201712210442
$ ./stop; ./start
</pre>

Instead of building a release yourself you can let the build server to the job. There is a job that looks out for a git tag named `release`. If a new revision is found then an automatic release build is being executed. The result of that is persisted to http://releases.sapsailing.com/. You can create that tag as follows. Make sure that you're on the current master branch before executing the following commands.

<pre>
$ git tag -f release
$ git push -f origin release:release
</pre>

Give the build server some time (20-30 Minutes) until it will have the release ready.

### Example with Hudson
Deploying jobs from hudson to `releases.sapsailing.com` is quite simple.

- login as `hudson@<build server>`
- go to `/home/hudson/repo/jobs/{jobname}/workspace`
- execute `./configuration/buildAndUpdateProduct.sh -n build -w trac@sapsailing.com release` for triggering the upload

## Working with Environments

There exist a number of preconfigured environment configurations at [releases.sapsailing.com/environments](http://releases.sapsailing.com/environments). Such an environment can be automagically applied to your instance by changing to the servers directory and then executing the `refreshInstance.sh` script with the parameter `install-env <environment-name>`. This will update your env.sh. Make sure to afterwards restart your server.

## Starting, Administrating and Stopping a Java Instance

The product runs as a Java instance consisting largely of an Equinox OSGi server that load and runs various OSGi bundles constituting the product and that contains an embedded Jetty web server. It connects to a database, may serve as a master for replication through a messaging server, may be launched as a replica of some other master instance, may receive wind data in _Expedition_ format on a specific port and can listen for telnet requests to administrate the OSGi server on a specific port. These and other properties are usually configured in a file called `env.sh` which has to be located in the server directory, e.g., in `~/servers/server`, next to the `start` script. When the `start` script is executed, it first sources the env.sh file which sets the various properties which are then passed to the actual Java process, usually in the form of system properties.

After successfully having started a Java instance, it can be administrated through a telnet connection. The port on which the OSGi console listens for incoming connections can be configured in the `env.sh` file. Usually it defaults to port `14888`. Therefore, a `telnet localhost 14888` connects you to the OSGi console where an `ss` command will show you all bundles loaded. Once logged on to the OSGi console, a `disconnect` command will disconnect the telnet session from the OSGi console.

An `exit` command will **terminate** the Java instance after a confirmation. This will obviously stop all services provided by the instance, including all static and dynamic contents served by its web server. You should only trigger the `exit` command if you really know what you are doing!

Stopping a running server has---for your convenience---been wrapped into the `stop` script usually located in the server's directory. Simply executing it will use a telnet connection to the server's OSGi port and trigger an `exit` command automatically. See above for the "know what you're doing" part...

## Automatic Java Instance Start-Up After Boot

When firing up an EC2 instance it can be convenient to not having to log on to have the EC2 instance run a Java instance automatically after it has completed its boot process. This is possible using so-called _user data_. The process of firing up an instance that either builds a certain git commit, installs and starts it after server boot or that downloads and installs a release and starts it is explained [here](https://wiki.sapsailing.com/wiki/info/landscape/amazon-ec2#amazon-ec2-for-sap-sailing-analytics_howto).

## App Build Process for iOS and Android

Our git and Project Portal structure seems to be somewhat unusual for the Final Assembly group. It is planned to move us to Xmake in the near future, although we don't know exactly which changes this will entail. Presumably, we'll become a little more flexible with regards to choosing branch names and branch namespace layouts.

The following descriptions are based on a situation where an Xmake migration has not yet taken place. Builds for iOS apps are performed using the "MiOS" infrastructure, builds for Android apps use "LeanDI."

### MiOS Build for iOS Apps

For iOS and Android there are two different build processes in place. After changing from MiOS to xMake during July 2017, things have changed considerably. This is an attempt to summarize the technical steps necessary to get the build done and hand things over to the "Final Assembly" department for deployment to the stores.

The iOS app build is largely described by contents of a ``cfg/`` directory which is symbolically linked to from the root of the git hierarchy and is actually located under ``ios/SAPTracker/cfg``. It is independent on any Maven builds and therefore unrelated to any POM files (``pom.xml``) as used in the back-end OSGi and Android builds. Other than for the previous MiOS approach we can therefore build the contents of the ``master`` branch more or less directly. Another "sacrifice" we have to make to the iOS build is having to link ``src/`` symbolically from the root of our git to ``ios/SAPTracker/src``, but this seems adequate given the branch handling which is now much simplified over the previous Maven build where we had to separate the ``pom.xml`` contents between iOS build branches and the ``master`` branch.

Still, we use the branch ``central-ios-release-build`` to prepare for the build. We usually merge ``ubilabs--ios--develop`` into ``master`` and then ``master`` into ``central-ios-release-build``. Builds off the latter branch can be tested using [this Jenkins job](https://xmake-mobile-dev.wdf.sap.corp:8443/view/all/job/sapsailingprogram/job/sapsailingcapture-SP-REL-common_directshipment/). Run a [Build with Parameters](https://xmake-mobile-dev.wdf.sap.corp:8443/view/all/job/sapsailingprogram/job/sapsailingcapture-SP-REL-common_directshipment/build?delay=0sec) and enter ``central-ios-release-build`` as the TREEISH. The build should succeed and produce the .ipa file somewhere in the [build's target output](https://xmake-mobile-dev.wdf.sap.corp:8443/view/all/job/sapsailingprogram/job/sapsailingcapture-SP-REL-common_directshipment/ws/gen/out/Exported-IPAs-Release-iphoneos/)
When the test build was successful, the branch contents need to be merged into the central git repository's branch ``fa/rel-1.x``, e.g., ``fa/rel-1.0``. 

Final Assembly will need to be notified using a BCP ticket, such as a Handover ticket that can be created using [this link](https://support.wdf.sap.corp/sap/bc/dsi/ii/create_zini?sap-language=EN&system_id=BCV&short_description=Handover%20%3cPPMS%20Software%20Component%20Version%3e&priority=3&main_impact=A&category_label=XX-INT-NAAS-MOBILE&incident_description=Requesting%2520Development%2520to%2520Production%2520Handover%2520for%2520the%2520App%253A%250A%253CFull%2520App%2520Name%253E%250A%250AProgram%2520Repository%2520Link%253A%250AProgram%2520Name%253A%250APPMS%2520Software%2520Component%2520Version%2520Name%253A%250APlatform%253A%2520%253CAndroid%2520%252F%2520iOS%2520%252F%2520TFS%2520(Windows)%253E%250A%250AShipment%2520Channel(s)%253A%250A%255B%255D%2520Indirect%2520shipment%2520(promotion%2520to%2520Nexus%2520only)%250A%255B%255D%2520Apple%2520App%2520Store%250A%255B%255D%2520Google%2520Play%250A%255B%255D%2520Google%2520Chrome%2520Store%250A%255B%255D%2520Amazon%2520App%2520Store%250A%255B%255D%2520BlackBerry%2520World%250A%255B%255D%2520Windows%25208%2520Store%250A%255B%255D%2520Windows%2520Phone%2520Store%250A%255B%255D%2520SMP%2520-%2520Binary%250A%255B%255D%2520SMP%2520-%2520Source%2520Code%250A%250AiOS%2520%252F%2520Android%250A----------------%250ALink%2520to%2520Project%2520Portal%253A%250ASource%2520Code%2520Project%2520Path%253A%2520%253CPerforce%2520path%2520with%2520codeline%2520%252F%2520Git%2520Path%253E%250A%250AIf%2520project%2520is%2520using%2520the%2520LeanDI%2520environment%252C%2520code%2520signing%2520is%2520enabled%2520and%2520will%2520remain%2520enabled%2520for%2520All%2520Release%2520builds%253A%2520%253Cyes%2520%252F%2520no%253E%250A%250ATFS%2520(Windows)%250A-----------------%250ATeam%2520Project%2520Collection%253A%250ATeam%2520Project%253A%250AREL%2520Branch%2520Path%253A%250AREL%2520Build%2520Definition%2520Name%253A%250A%250ACode%2520Signing%2520Request%2520Ticket%2520(if%2520applicable)%253A%2520%253CBCP%2520Ticket%2520Number%253E%250A%250AList%2520of%2520build%2520Dependencies%253A%2520%253CNexus%2520artifacts%2520including%2520libraries%253E%250A%250ALink%2520to%2520completed%2520Metaman%2520Entry%2520(for%2520External%2520Stores)%253A%250A%250AContacts%2520%253CUser%2520IDs%253E%250AProduction%2520Project%2520Lead%253A%250AProduct%2520Owner%253A%250AResponsible%2520for%2520Handover%253A%250AResponsible%2520for%2520Technical%2520issues%252Fquestions%253A%250AOthers%253A) or an update ticket as explained [here](https://wiki.wdf.sap.corp/wiki/display/NAAS/Mobile+Patch+Releases) in section "Requesting an update."

As a result of handling the request, Final Assembly will trigger builds on [https://xmake-mobile-dev.wdf.sap.corp:8443/view/all/job/sapsailingprogram/job/sapsailingcapture-SP-REL-common_directshipment/44/](https://xmake-mobile-dev.wdf.sap.corp:8443/view/all/job/sapsailingprogram/job/sapsailingcapture-SP-REL-common_directshipment/44/).

### xMake Build for Android Apps

We currently release the Android apps off a branch called ``android-xmake-release`` and referred to in the sequel as the "Android release branch." The Android release branch holds the changes required to run a successful Android build in xMake, particularly some adjustments to the top-level "Gradle Wrapper" (``/gradlew``) and its properties (``/gradle/wrapper/gradle-wrapper.properties``), such as obtaining the Gradle ZIP file from an SAP-internal sources instead of from an external repository. Other diffs between ``android-xmake-release`` are rather a legacy from the earlier Maven-based build and could in principal be reverted, such as the specific version adjustments along all ``pom.xml`` files. The actual build is still the Gradle build that xMake invokes like this:
```
./gradlew -Pxmake -Pversion=1.4.116 -PcommonRepoURL=https://int.repositories.cloud.sap/artifactory/build-releases/
```

We have various branches on which Android mobile app features are being developed. Ultimately, they should be merged into the ``master`` branch from where they get merged into the Android release branch. A so-called "Customer release" build can be triggered for the Android release branch on a [central Jenkins instance](https://xmake-mobile-dev.wdf.sap.corp/job/sapsailingprogram/job/sapsailingcapture.android-SP-REL-common_directshipment/). To get a release build staged for releasing, the version numbers in a few descriptor files, particularly the ``gradle.properties`` files of each app sub-directory under ``/mobile``, as well as ``cfg/files2sign.json`` need to be adjusted to the new version. 

Also note that once released to Nexus / Artifactory, the next release build will have to use at least an incremented micro version, such as 1.4.1.

Releasing can happen only from branches in Gerrit (git.wdf.sap.corp) under the ``fa/`` branch name prefix. Our Android branch for releasing therefore currently is ``fa/rel-1.4``. We can simply push the latest version of our ``android-xmake-release`` branch to it.

Many of the above steps can be automated by using the script ``configuration/releaseAndroidApps.sh``. Call like this:

```
        ./configuration/releaseAndroidApps.sh
```

Additional options:
```
    -m Disable upgrading the versionCode and versionName
    -g Disable the final git push operation to fa/rel-1.4
    -r The git remote; defaults to origin
```

This will checkout the `android-xmake-release` branch, merge the `fa/rel-1.4` release branch into it, push the result again so Gerrit knows this commit / change already. Then, the version substitutions for all ``gradle.properties`` and ``cfg/files2sign.json`` files are carried out automatically by looking at the version currently declared in them and incrementing the micro-version by one (e.g., 1.4.18 --&gt; 1.4.19). The result is committed to git and the resulting commit is pushed to the ``fa/rel-1.4`` branch.

Once this is done, a [Customer release build can be started](https://xmake-mobile-dev.wdf.sap.corp/job/sapsailingprogram/job/sapsailingcapture.android-SP-REL-common_directshipment/). Select "Build with Parameters" from the left, then select ``Stage`` as the build mode, then ``customer`` as the ``RELEASE_MODE`` and enter the ``fa/rel-1.4`` branch name in the "TREEISH" field and click the "Build" button. If the build succeeds, Final Assembly should be able to _promote_ the build artifact, such as the ``.apk`` files which should have been signed using the SAP certificate.

If the build fails due to missing Nexus artifacts, check out the document describing [how to upload artifacts to Nexus](https://wiki.wdf.sap.corp/wiki/display/LeanDI/Uploading+Third+Party+Artifacts+to+Nexus#UploadingThirdPartyArtifactstoNexus-1.CreateaJiraTicket). You will have to create a JIRA ticked in this process and upload the artifact and its ``pom.xml`` file to Nexus. Usually, these requests get handled within less than 48 hours. Good luck...

### Azure Pipelines Build for Android Apps

As of January 2024, the xMake build infrastructure will be terminated. Projects have been requested to migrate their builds to Azure Pipelines. The creation of those pipelines is managed by an SAP-internal tool called [Hyperspace](https://hyperspace.tools.sap/). Pipelines are organized into "Groups" within Hyperspace. Those groups may share, e.g., secrets stored in a common Hashicorp Vault. But secrets may as well be managed separately per pipeline. As a prerequisite, all code must be made available in a Github repository that is owned by a Github "organization," not a personal user account. For this purpose, the SAP Sailing Analytics Git repository is now also available at [https://github.tools.sap/SAP-Sailing-Analytics/sapsailing](https://github.tools.sap/SAP-Sailing-Analytics/sapsailing). Furthermore, in order to use the Hyperspace templates for mobile projects, such as Android, the group responsible for Hyperspace needs to actively enable your user account for the use of those templates. Find more Hyperspace onboarding documentation [here](https://pages.github.tools.sap/SAPMobile/Documentation/GettingStarted/hyperspace/). Note that Hyperspace is accessible only from within the SAP network / VPN / Citrix Workplace; other elements of this, such as Github and Azure Pipelines can also be accessed from anywhere as long as you have your client certificate installed.

When working with Hyperspace to prepare the creation of the actual Azure Pipeline, various credentials have to be provided or obtained, such as for the [Github Enterprise repository](https://github.tools.sap) and the [Microsoft AppCenter](https://appcenter.ms/) where a new organization must be created into which the apps get registered. Once all these steps have been completed successfully, the pipeline can be created:
* Pipeline Name: ``sapsailing-android-apps``
* Pipeline Description: Building the SAP Sailing Analytics Android Apps
* Pipeline Group: ``SAP-Sailing-Analytics``
Hyperspace produces a Git Pull Request (PR) for the main/master branch of the Github repository specified. As this was all experimental in the beginning, we cherry-picked some of the changes suggested by that PR into a new branch called ``hyperspace``, making the necessary adjustments to the "Fastlane" configuration so that when the build pipeline is actually invoked it will check out the ``hyperspace`` branch instead of ``main`` or ``master``. It was necessary to set the ``jdkVersion`` property in ``azure-pipelines/main.yml`` to ``11`` because with version 17 which is now used as default the build wouldn't run; and adjust the ``androidVersion`` in that same file to ``33``:

```
...
#################################################################
######################### Pipeline Start ########################
#################################################################
extends:
  template: android.yml@sap-mobile-pipeline
  parameters:
    # Please check the pipeline documentation for available parameters:
    #  https://pages.github.tools.sap/SAPMobile/Documentation/Pipelines/android-library/#configuration
    repositoryName: 'sapsailing'
    buildLaneName: 'releaseBuild'
    testLaneName: 'test'
    # requestAuthentication: true  # enable if fetching dependencies from Artifactory or Nexus
    appCenterSlug: 'sapsailing/sapsailing-android-apps'
    jdkVersion: 11
    androidVersion: 33
```

The pipeline created by Hyperspace shows up [here](https://dev.azure.com/hyperspace-mobile/SAP-Sailing-Analytics/_build). Each push to the branch to which the configuration is bound will trigger a new build. The build processes and logs can then be seen [here](https://dev.azure.com/hyperspace-mobile/SAP-Sailing-Analytics/_build?definitionId=496).

Our Gradle mobile build does not run any tests and hence does not produce any code coverage reports. Those, however, would be necessary in order to pass the build/quality step in the Azure Pipeline generated by Hyperspace. To overcome this problem, we committed a ``/dummy`` folder to the ``hyperspace`` branch in which we have an empty but valid ``TEST-dummy.xml`` test output file, and an empty yet valid Jacoco code coverage output file under ``jacoco/jacocoTestReport.xml``. This lets the "quality" stage of the Azure Pipeline pass without complaints.

For code signing and release to the Google Play Store, further steps will be necessary. In ``fastlane/Appfile`` there is currently a single ``package_name`` declaration, and in ``fastlane/Fastfile`` there is a private "lane" definition ``generate_gav`` which is supposed to produce a file ``gav.json`` that seems to be required by the app signing process. It seems to come before the ``assembleRelease`` and the ``bundleRelease`` tasks for building, respectively, an ``.apk`` or ``.aab`` file. Maybe we would have to tune the ``releaseBuild`` lane definition in ``fastlane/Fastfile`` to accomodate for the fact that we'd like to build and sign at least two (if not three; in case we'd include the old "Sail InSight" app as well) ``.apk`` files.

A [Hudson job](https://hudson.sapsailing.com/job/hyperspace/) currently exists that runs the regular build off the ``hyperspace`` branch. If this goes well, and once we have the entire Android build and release process including ``.apk`` signing and release into the store under control, we may consider merging ``hyperspace`` into ``master`` to minimize unnecessary differences and then push to the ``hyperspace`` branch particularly in order to trigger a mobile app build/release process.

Our points of contact for the Hyperspace / Azure Pipeline migration are Marc Bormeth (marc.bormeth@sap.com), Maurice Breit (maurice.breit@sap.com) and Philipp Resch (philipp.resch@sap.com).