# RaceCommittee App (Build Environment)

Building the RaceCommittee App has been integrated into the maven build process of the project. This document outlines how this environment is setup. Use this description whenever you need to set it up again.

## Android SDK

Rarar.

## Maven

Currently there is no Maven plugin available for Android 3.2 on the internets (see [Maven Repository](http://mvnrepository.com/artifact/com.google.android/android)). Therefore we had to build the correct plugins by hand and place them into http://maven.sapsailing.com/maven/.

This has been done with the help of the [maven-sdk-deployer](https://github.com/mosabua/maven-android-sdk-deployer). Issuing

    mvn install -P 3.2

will install the needed android.jar as a Maven plugin into your local repository. Now you can copy/remote-deploy this plugin to your target repository (in our case http://maven.sapsailing.com/maven/). Ensure that you do this step with all other needed SDK components (see above). For example you need to issue 

    maven-android-sdk-deployer/extras/compatibility-v13/mvn install

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

## Build-Script

Brom.