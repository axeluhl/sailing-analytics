# RaceCommittee App

[[_TOC_]]

## Introduction

The RaceCommittee App is an Android 3.2 Tablet application. This document serves as the main information hub about the App and how to develop for it.

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

## RaceState

The _RaceState_ (and its read-only variation _ReadonlyRaceState_) should be the only way you query and change the state of race (i.e. the state of a _RaceLog_). The _RaceState_ analyzes the content of its underlying _RaceLog_ for you and provides a clear interface to several aspects of your race, including its start time, its finished time, the selected course design, etc.

A _RaceState_ always has a _RacingProcedure_ attached to it. Whenever racing procedure type is set by a call to _RaceState#setRacingProcedureType# the _RacingProcedure_ object will be recreated (even when the type was not changed).

See the code documentation for further details.

### Adding a new user interface

When writing a user interface for the state of a _RaceLog_ / race you should use the _RaceState_ interface. Consult the code documentation on how to create a _RaceState_ (or _ReadonlyRaceState_).

Since your UI should get all updates, you should set a _RaceStateEventScheduler_ on the _RaceState_. This enables automatic events to be triggered by the _RacingProcedure_ (e.g. when the active flags of the starting sequence have changed). See below on how to implement a _RaceStateEventScheduler_.

Using a _RaceState_ for your UI you want to leverage the callback mechanisms rather than re-creating one-shot _RaceState_s and querying their status over and over again. Register your _RaceStateChangedListener_ on the _RaceState_ and your _RacingProcedureChangedListener_ on its _RacingProcedure_. You should re-register your _RacingProcedure_ listener whenever _RaceStateChangedListener#onRacingProcedureChanged_ is called. Keep in mind that depending on the type of the _RacingProcedure_ there might be additional callback methods available (e.g. for a RRS26 race there is a _RRS26ChangedListener_).

The last step is to implement a _RacingProcedurePrerequisite.Resolver_ to support setting a new start time in your UI. See below for a walk-through.

#### Implementing a RaceStateEventScheduler

A _RaceStateEventScheduler_ is in charge of calling _RaceState#processStateEvent_ whenever a _RaceStateEvent_ passed to _RaceStateEventScheduler#scheduleStateEvents_ is due.

Each _RateStateEvent_ carries a _TimePoint_. You should set a timer and call _RaceState#processStateEvent_ passing the _RaceStateEvent_ when the timer fires. Keep in mind that _RaceState_ won't do any threading/locking for you. Therefore be careful when calling _RaceState#processStateEvent_ from a background thread, because your UI listener (see above) might be called in this context!

#### Implementing a RacingProcedurePrerequisite.Resolver

When writing a new user interface for the _RaceState_ your UI code has to be capable of fulfilling possible _RacingProcedurePrerequisites_. Such a Prerequisite may occur when you're requesting a new start time on the _RaceState_. The call to _RaceState#requestNewStartTime_ takes a _RacingProcedurePrerequisite.Resolver_, which will be in charge of fulfilling prerequisites.

The _RacingProcedurePrerequisite.Resolver_ is an asynchronous interface passing you specific prerequisites demanding to be fulfilled. In your implementation of the interface show an appropiate dialog-window or something similar and be sure to call the specific _fulfilled_ method on the passed prerequisite when done. This will trigger the _RaceState_ to check for further prerequisites. When you've fulfilled everything your resolver's _onFulfilled_ method will be called. Afterwards the requested start time will be set.

**How does the resolving work**

On call to _RaceState#requestNewStartTime_ the _RaceState_ creates an anonymous function (called _FulfillmentFunction_) to be exected when all prerequistes are fulfilled. This function simply sets the start time on the _RaceState_ as requested.

Next the _RaceState_ asks its _RacingProcedure_ for any prerequisites. It does so by calling _RacingProcedure#checkPrerequisitesForStart_ passing the requested start time and the _FulfillmentFunction_. The returned _RacingProcedurePrerequisite_ will be resolved by your resolver.

To resolve a prerequisites against a resolver, _RacingProcedurePrerequisite#resolve_ is called passing the resolver. This method implements a simple double dispatch visitor pattern. Specific implementations will call their specific _fulfilment_ method on the resolver (see above). After the resolver has done its work for this prerequisite it will call the specific _fulfilled_ method. This will trigger the base _BaseRacingProcedurePrerequisite#fulfilled_ method, which checks for the next prerequisite on the _RacingProcedure_ (passing down the start time and the fulfillment function) and resolves it against the same resolver object.

This chain continues until the _RacingProcedure_ detects that there are no further prerequisites. It will return a special type of _RacingProcedurePrerequisite_, the _NoMorePrerequisite_. On resolve a _NoMorePrerequisite_ executes the fulfillment function and won't call _BaseRacingProcedurePrerequisite#fulfilled_ - effectively breaking the chain of resolving. The start time is set and your resolver _RacingProcedurePrerequisite.Resolver#onFulfilled_ is called.

### Adding a new racing procedure

Ja.

## Configuration

intro-text verweis zu admin-guide

### Adding a new configuration option

DeviceConfiguration vs RegattaConfiguration
merge
PreferenceScreen and AppPreferences

## RaceLog priorities and authors

Bruuuum.

## Versioning

Baruh

## Build and Auto-Update

On Maven builds the resulting APK of the RaceCommittee App will be made available as static content on the server's web page.

The RaceCommittee App is set up as an optional dependency of the bundle **com.sap.sailing.www**. This way the app will be build before the www-bundle. After the install phase the RaceCommittee App bundle will copy its artifact APK into _com.sap.sailing.www/apps_. The contents of this folder are packaged into the **com.sap.sailing.www** plugin, which will be deployed as the server's web page. When build with _buildAndUpdateProduct.sh_ an additional version information file is stored alongside the APK. Version information is taken from the AndroidManifest.xml (**android:versionCode**).

On synchronizing the connection settings (see [[administrator's guide|racecommittee-app-administrator]]) the RaceCommittee App downloads the version file to determine whether it should update itself or not. The file is expected to be found on _{SERVER_URL}/apps/{APP_PACKAGE_NAME}.version_ (e.g. _http://ess2020.sapsailing.com/apps/com.sap.sailing.racecommittee.app.version_). If the version file is not found, no update will be performed.