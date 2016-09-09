# RaceCommittee App (Server Build Environment)

**This is the build description for building on the server. For your local build see the [[onboarding document|wiki/howto/onboarding]]!**

Building the RaceCommittee App has been integrated into the maven build process of the project. This document outlines how this environment is setup. Use this description whenever you need to set it up again (build-server, Maven repo, ...)

## Android SDK

Before starting to install the Android SDK: **Your server must be capable of running 32bit binaries.** Otherwise installing and using the SDK will fail with errors similiar to "adb exit code -2: file or device not found".

On CentOS 6.4 you should issue the following commands:

    yum install libstdc++-4.4.7-4.el6.x86_64 # ensure that x86_x64 stdlib is up to date
    yum install glibc.i686 glibc-devel.i686 libstdc++.i686 zlib-devel.i686 ncurses-devel.i686 libX11-devel.i686 libXrender.i686 libXrandr.i686

On Ubuntu, use the following instead:

    dpkg --add-architecture i386
    apt-get update
    apt-get install libncurses5:i386 libstdc++6:i386 zlib1g:i386

On a Debian version 7.6 I succeeded with the following:

    dpkg --add-architecture i386
    apt-get update
    apt-get install ia32-libs lib32asound2 lib32v4l-0 libv4l-0=0.8.0-1 libasound2=1.0.23-2.1

Now we are ready to install the SDK. Pick up the [SDK Tools](http://developer.android.com/sdk/index.html) and install them on your server. Set an environment variable _ANDROID_HOME_ pointing to the install directory. It's a good idea to append some of the tools to your path:

    PATH=$PATH:$ANDROID_HOME/tools
    PATH=$PATH:$ANDROID_HOME/platform-tools

Now it's time to install all the needed SDK components. List all available components by issuing

    ./android list sdk --no-ui --all

Each component will have a component ID attached. Mark down all needed component IDs. Install them by issuing

    ./android update sdk --no-ui --all --filter [your-ids-comma-separated]

At the time being the RaceCommittee App needs the following components (warning: the component IDs will change in the future!):

     1- Android SDK Tools, revision 22.3
     2- Android SDK Platform-tools, revision 19
     3- Android SDK Build-tools, revision 19
     9- SDK Platform Android 4.4, API 19, revision 1
    11- SDK Platform Android 4.2.2, API 17, revision 2
    15- SDK Platform Android 3.2, API 13, revision 1
    54- Google APIs, Android API 19, revision 1
    56- Google APIs, Android API 17, revision 3
    61- Google APIs, Android API 13, revision 1
    81- Android Support Library, revision 19
    86- Google Play services, revision 13

Install them by

    ./android update sdk --no-ui --all --filter 1,2,3,9,11,15,54,56,61,81,86

If you are unsure about the components to install, just install everything with

    ./android update sdk --no-ui --all

Last but not least make sure that the user executing the build has **full read and write access** on the Android SDK folders.

## Maven

Currently there is no Maven plugin available for Android 3.2 on the internets (see [Maven Repository](http://mvnrepository.com/artifact/com.google.android/android)). Therefore we had to build the correct plugins by hand and place them into http://maven.sapsailing.com/maven/.

This has been done with the help of the [maven-sdk-deployer](https://github.com/mosabua/maven-android-sdk-deployer). If you are ever in the need of recreating http://maven.sapsailing.com/maven/ you'll need to re-do the steps to extract Maven plugins from your local Android SDK installation. Download the maven-sdk-deployer, switch to its root. Issuing

    mvn install -P 3.2

will install the needed android.jar as a Maven plugin into your local Maven repository. Now you can copy/remote-deploy this plugin to your target repository (in our case http://maven.sapsailing.com/maven/). Be aware that you might need more SDK components besides the core jar. Check your project's poms to see what you need. For example to get the RaceCommittee App working you need to issue 

    maven-android-sdk-deployer/extras/compatibility-v4/mvn clean install # compatibility lib
    maven-android-sdk-deployer/add-ons/google-apis-13/mvn clean install # maps and other stuff
    maven-android-sdk-deployer/extras/google-play-services/mvn clean install # play integration

too.

Now these plugins can be referenced in your project's pom, e.g.:

    <dependency>
        <groupId>android</groupId>
        <artifactId>android</artifactId>
        <version>3.2_r1</version>
        <scope>provided</scope>
    </dependency>

    <dependency>
      <groupId>android.support</groupId>
      <artifactId>compatibility-v4</artifactId>
      <version>19</version> <!-- Check your local plugin! Might be 19.0.0! See maven-sdk-deployer README! ->
    </dependency>

See https://github.com/mosabua/maven-android-sdk-deployer/ on how to reference other SDK components. Be sure to read the section _Known Problems_.

## Building and Build-Script

When executing Maven the **mobile** sub-module (including the RaceCommittee App) will be built by default. The buildAndUpdateProduct.sh has been extended to support disabling building the mobile projects. Check the script's help text to see how.

buildAndUpdateProduct.sh will check if **ANDROID_HOME** is set.