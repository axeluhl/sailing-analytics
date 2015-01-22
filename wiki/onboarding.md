# OnBoarding Information

This document describes the onboarding process for a new team member (developer)

First of all, make sure you've looked at http://www.amazon.de/Patterns-Elements-Reusable-Object-Oriented-Software/dp/0201633612. That's a great book, and knowing at least some of it will help you a great deal finding your way around our solution.

### Race Analysis Development Setup

#### Installations

1. Eclipse (e.g. Eclipse Luna for Eclipse Committers), http://www.eclipse.org
2. Eclipse Extensions
  * Install Eclipse GWT should be version 2.6.1 (https://developers.google.com/eclipse/docs/download)
3. Git (e.g. msysGit for Windows v1.7.10), http://git-scm.com
4. MongoDB (e.g. Production Release 2.0.4), download: http://www.mongodb.org/
5. RabbitMQ, download from http://www.rabbitmq.com/. Requires Erlang to be installed. RabbitMQ installer will assist in installing Erlang.
6. JDK 1.7 (Java SE 7), http://jdk7.java.net
7. JDK 1.8 (Java SE 8), http://jdk8.java.net
8. Maven 3.1.1 (or higher), http://maven.apache.org

#### Further optional but recommended installations

1. Cygwin, http://www.cygwin.com/
2. Eclipse Mylyn Bugzilla extension
3. kdiff3 (git tool)
4. Firebug (javascript & .css debugging)

#### Accounts

1. Git Account
  * For access to the external git at ssh://trac@sapsailing.com/home/trac/git please send your SSH public key to Axel Uhl or Simon Marcel Pamies, requesting git access. Make sure to NOT generate the key using Putty. Putty keys don't work reliably under Linux and on Windows/Cygwin environments. Use ssh-keygen in a Cygwin or Linux or MacOS/X environment instead.
  * Register yourself as a Git user in the SAP-Git under: https://git.wdf.sap.corp:8080/
  * Ask the Git administrator (Axel Uhl) to get on the list of enabled committers
2. Bugzilla
  * Ask the Bugzilla administrator (Frank Mittag, Axel Uhl) to create a bugzilla account for you.
  * Bugzilla url: http://bugzilla.sapsailing.com/bugzilla/
3. Race Analysis user
  * Add yourself as an user to the Race Analysis suite by adding a Jetty user in the file java\target\configuration\jetty\etc\realm.properties
4. Wiki
  * Send a request to Axel Uhl or Simon Marcel Pamies that includes the SHA1 hash of your desired password. Obtain such an SHA1 hash for your password here: http://www.sha1-online.com/.
5. Hudson
  * Request a Hudson user by sending e-mail to Axel Uhl, Frank Mittag or Simon Marcel Pamies.
 
#### Steps to build and run the Race Analysis Suite 
 
1. Get the content of the git repository
  * Generate SSH Keys with "ssh-keygen -t rsa -C "" " command in Cygwin Terminal (Not with Putty!!!)
  * Clone the repository to your local file system from `ssh://[SAP-User]@git.wdf.sap.corp:29418/SAPSail/sapsailingcapture.git`  or `ssh://[user]@sapsailing.com/home/trac/git`  User "trac" has all public ssh keys. 
2. Check out the 'master' branch from the git repository. The 'master' branch is the main development branch. Please check that you start your work on this branch.
3. Setup and configure Eclipse
  * Make absolutely sure to import CodeFormatter.xml (from $GIT_HOME/java) into your Eclipse preferences (Preferences->Java->Code Style->Fortmatter)
  * Install the Eclipse GWT-Plugin (now called Google Plugin for Eclipse, you need the Google WebToolkit SDK from the same update site, too)
  * Install the Google Android SDK from the same Google Plugin for Eclipse update site
  * Install Eclipse eGit (optional)
  * Check that JDK 1.8 is available and has been set for compilation in Eclipse
  * Check that the both JDKs are available (Windows->Preferences->Java->Installed JREs)
  * Check that JDK 1.7 has been matched to JavaSE-1.7 and that JDK 1.8 has been matched to JavaSE-1.8 (...>Installed JREs>Execution Environments)
  * It is also possible to match the SAPJVM 7 or 8 to JavaSE-1.7 / JavaSE-1.8 (for profiling purposes)
  * Go to Windows->Preferences->Google->Errors/Warnings and set "Missing SDK" to "Ignore"
  * Import all Race Analysis projects from the `java/` subdirectory of the git main folder
  * Import all projects from the `mobile/` subdirectory of the git main folder; this in particular contains the race committee app projects
  * Set the Eclipse target platform to race-analysis-p2-remote.target (located in com.sap.sailing.targetplatform/definitions)
  * Wait until the target platform has been resolved completely
  * In the project com.sap.sailing.gwt.ui create a new subfolder "classes" in the folder WEB-INF
  * Rebuild all projects
4. Run the Race Analysis Suite
  * Start the MongoDB
  * Start the appropriate Eclipse launch configuration (e.g. 'Sailing Server (Proxy)') You´ll find this in the run dropdown
  * Run "SailingGWT" in the run dropdown 
5. Within the Race Analysis Suite
  * For TracTrac Events: (Date 27.11.2012) Use Live URI tcp://10.18.22.156:4412, Stored URI tcp://10.18.22.156:4413, JSON URL  http://germanmaster.traclive.dk/events/event_20120905_erEuropean/jsonservice.php
  * Press List Races

#### Maven Setup
Copy the settings.xml from the top-level git folder to your ~/.m2 directory and adjust the proxy settings accordingly. Make sure the mvn executable you installed above is in your path. Open a shell (preferrably a git bash or a cygwin bash), cd to the git workspace's root folder and issue "./configuration/buildAndUpdateProduct.sh build". This should build the software and run all the tests. If you want to avoid the tests being executed, use the -t option. If you only want to build one GWT permutation (Chrome/English), use the -b option. When inside the SAP VPN, add the -p option for proxy use. Run the build script without arguments to get usage hints.

#### Further hints
- Configure Eclipse to use Chrome or Firefox as the default browser
- Install the GWT Browser Plugin (Chrome or Firefox; as of this writing (2013-11-05), Firefox is the only platform where the plug-in runs stably) for the GWT Development mode

#### Additional steps required for Android projects

To ensure that all components of the Analysis Suite are working, you should also import all Android projects (mobile/) into your workspace. There are some additional requirements to enable the build process of these projects.

1. Add the Android Development Tools (ADT) plugin to your Eclipse IDE
  - In Eclipse click Help -> Install New Software -> Add and enter https://dl-ssl.google.com/android/eclipse/
  - Select the Developer Tools and install
  - After restarting Eclipse the "Welcome to Android Development" window should help you with installing the Android SDK
  - It is also possible to download the Android SDK separately from http://developer.android.com/sdk/index.html ("Use an existing IDE")
2. Setup the Android SDK
  * In Eclipse press Window -> Android SDK Manager
  * Ensure that everything of "Tools" is installed
  * Install everything of "Android 3.2 API 13"
  * Optional: it's a good idea to install the newest API Version
  * Install "Android Support Library" (Extras), "Google Play Services" (Extras) and "Google USB Driver" (Extras)
3. Import the Android projects into your workspace
  * Android projects can be found in the /mobile subdirectory

To deploy an Android project (for example com.sap.sailing.racecommittee.app) to a real device:

1. Plug-in the device
  * Development mode must be enabled on the device
  * For certain device/OS combinations additional device drivers are needed
  * You can check if the device is detected correctly by checking the "Devices" tab of the "DDMS" Eclipse perspective.
2. Start a run configuration of the project
3. Select your attached device in the device selection screen
4. The app should be started after deployment

####Stepps to consider for using other modules
1. For Eclipse Build
   * MANIFEST.MF , add module names unter dependencies
   * *.gwt.xml , add `<inherits name="-modulename-.-gwt.xml file name-" />`
   * In DebugConfigurations => Classpaths, Add Sourcefolder where classes are you want to user from the module
2. For Maven Build
   * pom.xml , Add Dependency to used module ie.     
`<dependency>
<groupId>com.sap.sailing</groupId>
<artifactId>com.sap.sailing.domain.common</artifactId>
<version>1.0.0-SNAPSHOT</version>
<classifier>sources</classifier>
</dependency>`


See [RaceCommittee App](racecommittee-app) for more information regarding the mobile app.
