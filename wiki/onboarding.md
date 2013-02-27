# OnBoarding Information

This document describes the onboarding process for a new team member (developer)

### Race Analysis Development Setup

#### Installations

1. Eclipse (e.g. Eclipse Classic 3.7.2), http://www.eclipse.org
2. Eclipse Extensions
  * Install Eclipse GWT should be version 2.4
3. Git (e.g. msysGit for Windows v1.7.10), http://git-scm.com
4. MongoDB (e.g. Production Release 2.0.4), download: http://www.mongodb.org/
5. RabbitMQ, download from http://www.rabbitmq.com/. Requires Erlang to be installed. RabbitMQ installer will assist in installing Erlang.
6. JDK 1.6 (Java SE 6), http://jdk6.java.net (for GWT)
7. JDK 1.7 (Java SE 7), http://jdk7.java.net
8. Maven 3, http://maven.apache.org

#### Further optional but recommended installations

1. Cygwin, http://www.cygwin.com/
2. Eclipse Mylyn Bugzilla extension
3. kdiff3 (git tool)
4. Firebug (javascript & .css debugging)

#### Accounts

1. Git Account
- Register yourself as a Git user in the SAP-Git under: https://git.wdf.sap.corp:8080/
- Ask the Git administrator (Axel Uhl) to get on the list of enabled committers

2. Bugzilla
- Ask the Bugzilla administrator (Frank Mittag, Axel Uhl) to create a bugzilla account for you.
- Bugzilla url: http://sapcoe-app01.pironet-ndh.com/bugzilla/

3. Race Analysis user
- Add yourself as an user to the Race Analysis suite by adding a Jetty user in the file
  java\target\configuration\jetty\etc\realm.properties
 
#### Steps to build and run the Race Analysis Suite 
 
1. Get the content of the git repository
- Clone the repository to your local file system from ssh://[SAP-User]@git.wdf.sap.corp:29418/SAPSail/sapsailingcapture.git  or ssh://[user]@sapsailing.com/home/trac/git
 
2. Check out the 'master' branch from the git repository. The 'master' branch is the main development branch. Please check that you start your work on this branch.

3. Setup and configure Eclipse
  - Install the Eclipse GWT-Plugin (now called Google Plugin for Eclipse, you need the Gogle WebToolkit SDK from the same update site, too)
  - Install Eclipse eGit (optional)
  - Check that JDK 1.7 is available and has been set for compilation in Eclipse
  - Check that the both JDKs are available (Windows->Preferences->Java->Installed JREs)
  - Check that JDK 1.6 has been matched to JavaSE-1.6 and that JDK 1.7 has been matched to JavaSE-1.7 (...>Installed JREs>Execution Environments)
  - It is also possible to match the SAPJVM 6 or 7 to the JavaSE-1.6 (for profiling purposes)
  - Import all Race Analysis projects from the /java(!!!) subdirectory of the git main folder
  - Set the Eclipse target platform to race-analysis-p2-ide-local.target (located in com.sap.sailing.targetplatform/definitions)
  - Wait until the target platform has been resolved completely
  - In the project com.sap.sailing.gwt.ui create a new subfolder "classes" in the folder WEB-INF
  - Rebuild all projects

4. Run the Race Analysis Suite
  - Start the MongoDB
  - Start the appropriate Eclipse launch configuration (e.g. 'Sailing Server (Proxy)')
  - Start the GWT UI 

5. Within the Race Analysis Suite
  - For TracTrac Events: (Date 27.11.2012) Use Live URI tcp://10.18.22.156:4412, Stored URI tcp://10.18.22.156:4413, JSON URL  http://germanmaster.traclive.dk/events/event_20120905_erEuropean/jsonservice.php
  - Press List Races

#### Maven Setup
Copy the settings.xml from the top-level git folder to your ~/.m2 directory and adjust the proxy settings accordingly. Make sure the mvn executable you installed above is in your path. Open a shell (preferrably a git bash or a cygwin bash), cd to the java/ subfolder of the git workspace and issue "mvn -fae clean install". This should build the software and run all the tests. If you want to avoid the tests being executed, run "mvn -Dmaven.test.skip=true -fae clean install". If you want to make sure the GWT compiler really re-builds all artifacts, remove all java/com.sap.sailing.gwt.ui/com.sap.sailing.gwt.ui.* directories, then re-build.

#### Further hints
- Configure Eclipse to use Chrome or Firefox as the default browser
- Install the GWT Browser Plugin (Chrome or Firefox) for the GWT Development mode    