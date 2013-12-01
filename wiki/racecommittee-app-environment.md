# RaceCommittee App (Server Build Environment)

**This is the build description for building on the server. For your local build see the [[onboarding document|wiki/onboarding]]!**

Building the RaceCommittee App has been integrated into the maven build process of the project. This document outlines how this environment is setup. Use this description whenever you need to set it up again (build-server, Maven repo, ...)

## Android SDK

On all build servers the Android SDK has to be installed. Pick up the [SDK Tools](http://developer.android.com/sdk/index.html) and install them on your server. Set an environment variable _ANDROID_HOME_ pointing to the install directory. It's a good idea to append some of the tools to your path:

    PATH=$PATH:$ANDROID_HOME/tools
    PATH=$PATH:$ANDROID_HOME/platform-tools

Now it's time to install all the needed SDK components. List all available components by issuing

    ./android list sdk --no-ui --all

Each component will have a component ID attached. Mark down all needed component IDs. Install them by issuing

    ./android update sdk --no-ui --all --filter [your-ids-comma-separated]

At the time being the RaceCommittee App needs the following components (**warning: the component IDs will change in the future!**):

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

## Maven

Currently there is no Maven plugin available for Android 3.2 on the internets (see [Maven Repository](http://mvnrepository.com/artifact/com.google.android/android)). Therefore we had to build the correct plugins by hand and place them into http://maven.sapsailing.com/maven/.

This has been done with the help of the [maven-sdk-deployer](https://github.com/mosabua/maven-android-sdk-deployer). Issuing

    mvn install -P 3.2

will install the needed android.jar as a Maven plugin into your local repository. Now you can copy/remote-deploy this plugin to your target repository (in our case http://maven.sapsailing.com/maven/). Ensure that you do this step with all other needed SDK components (see above). For example you need to issue 

    maven-android-sdk-deployer/extras/compatibility-v4/mvn install

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

When executing Maven the **mobile** sub-module (including the RaceCommittee App) will be build by default. The buildAndUpdateProduct.sh has been extended to support disabling building the mobile projects. Check the script's help text to see how.

buildAndUpdateProduct.sh will check if **ANDROID_HOME** is set.