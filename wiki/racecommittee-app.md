# RaceCommittee App

[[_TOC_]]

## Introduction

The RaceCommittee App is an Android 3.2 Tablet application.

* See the [[OnBoarding Information|wiki/onboarding]] on how to setup your local environment for building the app
* See [[Mobile Development|wiki/mobile-development]] for general tipps on mobile development.
* See [[Server Environment|wiki/racecommittee-app-environment]] on how the server's build environment is configured for building the RaceCommittee App.

## User Guide

Have a look at the following user guides to get an idea how to work with the RaceCommittee App.

* [[RaceCommittee App as an administrator|wiki/racecommittee-app-administrator]]
* [[RaceCommittee App as a race officer|wiki/racecommittee-app-user]]

## Features
A Feature List collected in September 2013 at the Testevent in Santander can be seen here:
[Feature List RaceCommittee App](/doc/RaceCommittee Feature List.xlsx) (RaceCommittee Feature List, Sep. 2013, Julian Gimbel)
The Feedback a User gave to the Application and the Feedback the user gave to the Applikation and Hardware of SwissTiming can be seen here:
[Feature List RaceCommittee App](/doc/Swiss Timing Race Committee App.docx) (Swiss Timing App and Feedback for SAP RaceCommittee App, Sep. 2013, Julian Gimbel)

## Course Updates

Kuruh, Kuruh,...

## Versioning

Baruh

## Build and Auto-Update

On Maven builds the resulting APK of the RaceCommittee App will be made available as static content on the server's web page.

The RaceCommittee App is set up as an optional dependency of the bundle **com.sap.sailing.www**. This way the app will be build before the www-bundle. After the install phase the RaceCommittee App bundle will copy its artifact APK into _com.sap.sailing.www/apps_. The contents of this folder are packaged into the **com.sap.sailing.www** plugin, which will be deployed as the server's web page. When build with _buildAndUpdateProduct.sh_ an additional version information file is stored alongside the APK. Version information is taken from the AndroidManifest.xml (**android:versionCode**).

On synchronizing the connection settings (see [[administrator's guide|racecommittee-app-administrator]]) the RaceCommittee App downloads the version file to determine whether it should update itself or not. The file is expected to be found on _{SERVER_URL}/apps/{APP_PACKAGE_NAME}.version_ (e.g. _http://ess2020.sapsailing.com/apps/com.sap.sailing.racecommittee.app.version_). If the version file is not found, no update will be performed.