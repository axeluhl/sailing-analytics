# Android and release build

[[_TOC_]]

## Basic setup of the Android apps build

The Android Build is based on Gradle and is separate of the overall multi-module-build. The Android specific projects are hosted in /mobile. The build of the app projects is completely separated from Tycho/p2.

To build the Android apps, go to the root of the Git workspace and enter the command
```
  ./gradlew
```
It will produce the APK files in the following locations:
```
./mobile/com.sap.sailing.racecommittee.app/build/outputs/apk/release/com.sap.sailing.racecommittee.app-unsigned.apk
./mobile/com.sap.sailing.android.buoy.positioning.app/build/outputs/apk/release/com.sap.sailing.android.buoy.positioning.app-unsigned.apk
./mobile/com.sap.sailing.android.tracking.app/build/outputs/apk/release/com.sap.sailing.android.tracking.app-unsigned.apk
```
The build job also copies them to ``java/com.sap.sailing.www/apps`` from where, during the build of the ``com.sap.sailing.www`` OSGi bundle they become part of the overall delivery and can hence be downloaded conveniently for a "side-load" style installation from any of our servers' ``/apps`` URL path.

If this build works without errors, it will also be part of all full product builds, as seen on [https://hudson.sapsailing.com](https://hudson.sapsailing.com). 

## Release build overview

See [here](https://wiki.sapsailing.com/wiki/info/landscape/building-and-deploying#building-deploying-stopping-and-starting-server-instances_app-build-process-for-ios-and-android_xmake-build-for-android-apps).