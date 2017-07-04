# Android and release build

[[_TOC_]]

## Basic setup of the Android apps build

The Android Build is based on Maven and is part of the overall multi-module-build. The Android specific projects are hosted in /mobile. The build of the app projects is completely separated from Tycho/p2.

## Backend Build vs Android Build

Due to some incompatibilities of the Android Build with specifics of the Tycho Build, it's not possible to build the backend and Android apps in one execution. That's why there are several profiles that control, what is part of the specific build.

The default activation of the profiles causes the backend build to run. The relevant profiles to switch between backend and Adnroid build are:
* with-mobile (defauts to off)
* with-not-android-relevant (defauts to on)

So to run the Android apps build you have to add "-P -with-not-android-relevant -P with-mobile" to the Maven command line.

## Release build overview

There are several differences when running the build on Lean DI. Due to this, it's necessary to implement some changes in the build. To realize the build for Lean DI there is a specific branch "android-xmake-release". It is intended that most of the Lean DI specific logic is implemented on "master" in specific Maven profiles. This causes the release branch to only have few differences compared to "master". The most notable differences are:

* Configuration of parent POM "com.sap.ldi:ldi-parent" in workspace POM
* Versions are set to not be *-SNAPSHOT but release versions
* Some profile activation conditions are changed
