# Development Environment

[[_TOC_]]

## Git and Our Branches
Our main Git repository lives at ssh://<user>@sapsailing.com/home/trac/git. For those working in the SAP corporate network and therefore unable to access the external sapsailing.com server using SSH, the repository contents are replicated on an hourly basis into ssh://dxxxxxx@git.wdf.sap.corp:29418/SAPSail/sapsailingcapture.git where dxxxxxx obviously is to be replaced by your D- or I- or C-user. You need to have an account at https://git.wdf.sap.corp:8080/ to be able to access this Git repository.

Small, obvious and non-disruptive developments are usually carried out immediately on our master branch. This branch is configured such that Maven can be used to run the tests inside the SAP corporate network. The master branch is never deployed onto the sapsailing.com server and hence has no corresponding /home/trac/servers/ subdirectory.

If a change looks reasonably good on the master branch and related JUnit tests or manual UI tests have succeeded locally, it is permissible to merge the master branch into the dev branch and run a central build on sapsailing.com. The dev branch is configured to run the Maven tests with direct Internet access. It can therefore also be used to run the tests locally if connected to the public Internet.

Ideally, the build should be run including the test cases. If the tests succeed , the branch can be installed and the corresponding server instance can be restarted. The branch can then also be promoted to the next level (dev-->test-->prod1/prod2). Note, that currently re-starting a server instance may require re-loading the races that were previously loaded, particularly for the prod1 and prod2 instances, because several externally-announced URLs point to them.

We typically promote the changes to the next branch by also merging the current master branch into the next-"higher" branch. This should lead to equivalent results compared to merging the "previous" branch (e.g., dev) into the "next" branch (e.g., test). The branches differ largely in the configurations used for the servers, particularly the port assignments for the Jetty web server, the UDP ports used for listening for Expedition wind messages, and the queue names used for the replication based on RabbitMQ (see also Scale-Out through Replication).

## Eclipse Setup, Required Plug-Ins
We use Eclipse as our development environment. Using a different IDE would make it hard to re-use the project configurations (.project and .classpath files which are specific to Eclipse) as well as the GWT plug-in which assists in locally compiling and refactoring the GWT code, in particular the RPC code.

The recommended and tested Eclipse version is currently Indigo (3.7). Colleagues have reported that they succeeded with a Juno (3.8/4.x) set-up as well.

To get started, install Eclipse with at least PDE, Git, GWT (use http://dl.google.com/eclipse/plugin/3.7 as update site) and JSP editing support enabled. Eclipse Maven support is not recommended as in many cases it has caused trouble with the local Eclipse build.

## Target Platform
After the Eclipse installation and importing all projects under java/ from Git, it is required to set the target platform in Eclipse (Window --> Preferences --> Plugin Development --> Target Platform). The project com.sap.sailing.targetplatform contains "Race Analysis Target (IDE)" as target platform definition. It uses a number of p2 update sites and defines the OSGi bundles that constitute the target platform for the application. If this is not set in Eclipse, the local build environment assumes that the developer wants to implement Eclipse plug-ins and offers the entire set of Eclipse plug-ins and only those as the target platform which doesn't make any sense for our application.

Major parts of our target platform are hosted on sapsailing.com as a p2 repository which makes it possible to have only one central target platform configuration used by everyone. The target platform can be re-built, e.g., after adding another bundle to it, using the script in com.sap.sailing.targetplatform/scripts. 

It then needs to be installed again (by using a tool like scp for instance) to the /home/trac/p2-repositories directory from where it is exposed as http://p2.sapsailing.com/p2/sailing/ by the Apache web server. After such a change, all developers need to reload the target platform into their Eclipse environment.

## Maven Build and Tests
We use Maven to build our software and run our JUnit tests. The global setting.xml file to install in everyone's ~/.m2 directory is checked into the top-level Git folder. The checked-in copy assumes the developer is using Maven inside the SAP corporate network. If not, uncomment the <proxy> tag in the settings.xml file. See also section Git and Our Branches for details on which branch is configured to work in which network setup.

We have a top-level Maven pom.xml configuration file in the root folder of our Git workspace. It delegates to the pom.xml file in the java/ folder where all the bundle projects are defined. We rely on the Tycho Maven plug-in to build our OSGi bundles, also known as the "manifest-first approach." The key idea is to mainly develop using Eclipse means, including its OSGi manifest editing capabilities, and keep the Maven infrastructure as simple as possible, deriving component dependencies from the OSGi manifests. See the various pom.xml files in the projects to see the project-specific settings. By and large, a pom.xml file for a bundle needs to have the bundle name and version defined (we currently have most bundles at version 1.0.0.qualifier in the manifest or 1.0.0.SNAPSHOT in Maven), and whether the bundle is a test or non-test bundle, expressed as the packaging type which here can be one of eclipse-plugin or ecplise-test-plugin.

Test plugins automatically have their tests executed during a Maven build unless the command-line option -Dmaven.test.skip=true argument is specified. It is generally a good idea to launch the Maven command using the -fae option which asks Maven to continue until the end, even if errors or failures occurred on the way, failing at the end if any failures occurred. This can save numerous round trips and is useful in case of known and temporarily acceptable test failures.

The Maven plug-in for the GWT compilation doesn't reliably perform a dependency check. It is therefore recommended to remove all contents of the java/com.sap.sailing.gwt.ui/com.sap.sailing.* folders (basically, all GWT compiler output) before launching the Maven build. A good command line for the Maven build from the java/ subdirectory in your local environment when outside the SAP VPN is this:

    buildAndUpdateProduct.sh build

which basically does something like

    rm -rf com.sap.sailing.gwt.ui/com.sap.sailing.*; mvn -fae -P debug.without-proxy clean install 2>&1 | tee log

Inside the SAP VPN you may want to use a different profile which accounts for the proxies that have to be used:

    buildAndUpdateProduct.sh -p build

The buildAndUpdateProduct.sh script can be found in the top-level configuration/ directory in git. It has been used successfully in Linux and Cygwin environments.

When building on sapsailing.com you should stick with the buildAndUpdateProduct.sh script. It makes a lot of settings that are necessary, such as specifying the settings.xml file to use for the Maven build. For the Selenium tests to succeed you have to make sure the DISPLAY environment variable is set to ":2.0" to send test browsers to a VNC display. Should the GWT build fail because it cannot open enough files, ensure the "ulimit -n" output is at least 4096 to enable the GWT compiler to assemble the resource sets which consist of many files that all need to be opened concurrently. Currently, the maximum value for "ulimit -n" is configured in /etc/security/limits.conf and is set to 16384. This specified the maximum amount to which a user's shell can set this value. The ~trac/.bash_profile contains a "ulimit -n 4096" command, but when running "screen" the shells usually are no login shells. You need to make sure you run ~trac/.bash_profile in the build shell to set the limit of open files to at least 4096. Then issue the respective buildAndUpdateProduct.sh command line.

All these build lines also creates a log file with all error messages, just in case the screen buffer is not sufficient to hold all scrolling error messages.

## Automated Builds using Hudson

The project uses a Hudson build server installation that can be reached at [hudson.sapsailing.com](http://hudson.sapsailing.com). Please ask a project administrator for an account. This Hudson server builds all new commits pushed to the master branch, performs the JUnit tests and publishes the JUnit test results. New jobs for other branches can easily be created by copying from the SAPSailingAnalytics-master job and updating the git branch to be checked out for build. This way, you can create your own job for your own branch. Don't forget to set yourself as the e-mail recipient for failing builds.

As a special feature, release builds can automatically be performed and published to [releases.sapsailing.com](http:///releases.sapsailing.com) by pushing the tag named "release" to the version that you want to release. This can be done using the following series of git commands:

    git tag -f release
    git push origin release:release

You can follow the build triggered by this [here](http://hudson.sapsailing.com/job/SAPSailingAnalytics-release/).

### Plotting test results with the Measurement Plugin

By default the duration of each test is published and can be viewed in comparison with older builds. It is possible to publish other values using the Measurement Plugin, which reads them out of a `MeasurementXMLFile`. 

```
MeasurementXMLFile performanceReport = new MeasurementXMLFile("TEST-" + getClass().getSimpleName() + ".xml", getClass().getSimpleName(), getClass().getName());
MeasurementCase performanceReportCase = performanceReport.addCase(getClass().getSimpleName());
performanceReportCase.addMeasurement(new Measurement("MeasurementName", measurementValue));
```

## Product, Features and Target Platform
The result of the build process is a p2 repository with a product consisting of a number of features. The product configuration is provided by the file raceanalysis.product in the com.sap.sailing.feature.p2build project. In its dependencies it defines the features of which it is built, which currently are com.sap.sailing.feature and com.sap.sailing.feature.runtime, each described in an equal-named bundle. The feature specified by com.sap.sailing.feature lists the bundles we develop ourselves as part of the project. The com.sap.sailing.feature.runtime feature lists those 3rd-party bundles from the target platform which are required by the product.

The [target platform](#development-environment_target-platform) is defined in the various flavors for local and central environments in com.sap.sailing.targetplatform/definitions/*.target. It mainly uses Eclipse p2 repositories and our own p2 repository at http://p2.sapsailing.com/p2/sailing/ where we store those bundles required by our runtime which cannot be found as OSGi bundles in any other public p2 repository of which we are aware.

This p2 repository at sapsailing.com can be re-built and correspondingly extended by the process explained [here](wiki/typical-development-scenarios#Adding-a-Bundle-to-the-Target-Platform).

## External Libraries

### Highcharts and jQuery
We use the Highcharts library to present graphs to the user. These graphs are used on the RaceBoardPanel and (at the time of writing still under development) the PolarSheetsPanel. In the past, there were difficulties concerning the versions of the three interacting libraries:

*	The GWT Highcharts Wrapper – The source code can be found in our project and it’s slightly modified to match our scenario
*	The actual Highcharts Library
*	The jQuery Library