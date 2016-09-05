# RaceCommittee App

[[_TOC_]]

## Introduction

The RaceCommittee App is an Android 3.2 Tablet application. This document serves as the main information hub about the App and how to develop for it.

* See the [[OnBoarding Information|wiki/howto/onboarding]] on how to setup your local environment for building the app
* See [[Mobile Development|mobile-development]] for general tipps on mobile development.
* See [[Server Environment|racecommittee-app-environment]] on how the server's build environment is configured for building the RaceCommittee App.

## User Guide

Have a look at the following user guides to get an idea how to work with the RaceCommittee App.

* [[RaceCommittee App as an administrator|racecommittee-app-administrator]]
* [[RaceCommittee App as a race officer|racecommittee-app-user]]

## Features
A Feature List collected in September 2013 at the Testevent in Santander can be seen here:
[Feature List RaceCommittee App](/doc/RaceCommittee Feature List.xlsx) (RaceCommittee Feature List, Sep. 2013, Julian Gimbel)
The Feedback a User gave to the Application and the Feedback the user gave to the Applikation and Hardware of SwissTiming can be seen here:
[Feature List RaceCommittee App](/doc/Swiss Timing Race Committee App.docx) (Swiss Timing App and Feedback for SAP RaceCommittee App, Sep. 2013, Julian Gimbel)

## Course Updates

The app has several course designer varying in their input method and output:

* **By-Name** Course Designer: just setting the name of a _CourseBase_ object with no waypoints
* **By-Map** Course Designer: just setting the name of a _CourseBase_ object with now waypoints
* **By-Marks** Course Designer: full _CourseBase_ object with waypoints

The _CourseBase_ is attached to a _RaceLogCourseDesignChangedEvent_. On the server the _TrackedRace_ attached to the race log will forward such events to the _TracTracCourseDesignUpdateHandler_. If activated this handler will forward the _CourseBase_ object (regardless of whether its has waypoints or not) to **TracTrac**.

## RaceState

The _RaceState_ (and its read-only variation _ReadonlyRaceState_) should be the only way you query and change the state of race (i.e. the state of a _RaceLog_). The _RaceState_ analyzes the content of its underlying _RaceLog_ for you and provides a clear interface to several aspects of your race, including its start time, its finished time, the selected course design, etc.

A _RaceState_ always has a _RacingProcedure_ attached to it. Whenever racing procedure type is set by a call to _RaceState#setRacingProcedureType_ the _RacingProcedure_ object will be recreated (even when the type was not changed).

See the code documentation for further details.

### Adding a new user interface

When writing a user interface for the state of a _RaceLog_ / race you should use the _RaceState_ interface. Consult the code documentation on how to create a _RaceState_ (or _ReadonlyRaceState_).

Since your UI should get all updates, you should set a _RaceStateEventScheduler_ on the _RaceState_. This enables automatic events to be triggered by the _RacingProcedure_ (e.g. when the active flags of the starting sequence have changed). See below on how to implement a _RaceStateEventScheduler_.

Using a _RaceState_ for your UI you want to leverage the callback mechanisms rather than re-creating one-shot _RaceStates_ and querying their status over and over again. Register your _RaceStateChangedListener_ on the _RaceState_ and your _RacingProcedureChangedListener_ on its _RacingProcedure_. You should re-register your _RacingProcedure_ listener whenever _RaceStateChangedListener#onRacingProcedureChanged_ is called. Keep in mind that depending on the type of the _RacingProcedure_ there might be additional callback methods available (e.g. for a RRS26 race there is a _RRS26ChangedListener_ ).

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

When adding a new racing procedure you should start by basing your work on one of the existing ones. Have a look at the basic countdown racing procedure to see the most simplest. The brave ones base their work on the gate start procedure.

A working racing procedure needs the following:
1. A new type in _RacingProcedureType_
2. An implementation of _RacingProcedure_
3. A _RacingProcedureConfiguration_ field in _RegattaConfiguration_ (see below on how to do this)
4. App UI - just extend _RaceInfoFragmentChooser_ and you'll see what you need

## RaceLog priorities and authors

TODO.

## Build and Auto-Update

When build by Maven the resulting APK of the RaceCommittee App will be made available as static content on the server's web page.

The RaceCommittee App is set up as an optional dependency of the bundle **com.sap.sailing.www**. This way the app will be build before the www-bundle. After the install phase the RaceCommittee App bundle will copy its artifact APK into _com.sap.sailing.www/apps_. The contents of this folder are packaged into the **com.sap.sailing.www** plugin, which will be deployed as the server's web page. When build with _buildAndUpdateProduct.sh_ an additional version information file is stored alongside the APK. Version information is taken from the AndroidManifest.xml (**android:versionCode**).

On synchronizing the connection settings (see [[administrator's guide|racecommittee-app-administrator]]) the RaceCommittee App downloads the version file to determine whether it should update itself or not. The file is expected to be found on _{SERVER_URL}/apps/{APP_PACKAGE_NAME}.version_ (e.g. _http://ess2020.sapsailing.com/apps/com.sap.sailing.racecommittee.app.version_). If the version file is not found, no update will be performed.

See the next section about versioning.

## Versioning

The app's version is defined in its AndroidManifest.xml in the **versionCode** attribute. This version code is used by several components:

* Android OS for standard versioning operations
* Helper-Class **PreferenceHelper** to determine whether a refresh of the stored preferences is needed
* Auto-Update feature (see above)

This leads to the following situations, in which one should bump the **versionCode**:

1. You feel like you have achieved something remarkable.
2. You have added or changed the type of a preference (see below for a walk-through on how to add a new preference option).
3. You have made non-backwards-compatible changes to the app<->server interface. This includes changes to serializers/deserializers, changes to servlets,...
4. You want to trigger the auto-update because you customized the app for the current event.

## Configuration (or Preferences)

Configuration of the app is crucial for the app to function properly. See the [[administration guide|racecommittee-app-administrator]] on how to it is done. The main idea is, that all configuration options are editable on the app via the Android standard preferences interfaces. Still most of the configuration should be configurable on the server.

The app fetches its **DeviceConfiguration** on logon. This overall **DeviceConfiguration** is merged with configuration that is stored on device. Additionally each regatta can have a specific **RegattaConfiguration** attached to it. A regata-specific RegattaConfiguration is merged with the overall **DeviceConfiguration**.

### Adding a new configuration option

The following leads you to the process of adding a remote-configurable configuration option. The text assumes you are adding the configuration option to _DeviceConfiguration_. The process of adding an option to _RegattaConfiguration_ or one of the _RacingProcedureConfigurations_ is very similar.

Estimated time: 1 hour. Main task: copy and paste.

1. Device-Local Configuration
  1. res/xml/preference_xxx.xml add preference (check com.sap.sailing.racecommittee.app.ui.views) and define title and summary in localization files
  2. res/values/preferences.xml define key string and default value - this default value will be set on first start (or version change!)
  3. If needed initialize your preference in _com.sap.sailing.racecommittee.app.ui.fragments.preference.YourPreferenceFragment_ (keep in mind: default value will be set automatically)
  4. If your preference needs to accessed from app code this should be done through the helper class _AppPreferences_ -> create getter (and if needed setter) in _AppPreferences_
2. Exposing the option on the server
  1. Add a getter for your option to _DeviceConfiguration_
  2. Implement the getter and a setter in _DeviceConfigurationImpl_
  3. Modify the _DeviceConfigurationImpl#clone_ method (for _RegattaConfiguration_ or one of its items you need to extend the merge method too)
  4. Teach the _com.sap.sailing.racecommittee.app.domain.configuration.PreferencesDeviceConfigurationLoader_ how to load and store your setting from/to AppPreferences (for RegattaConfiguration you should extend  com.sap.sailing.domain.base.configuration.impl.EmptyRegattaConfiguration too)
  5. Modify the configuration's serializer and deserializer (good news: they are used for persistence, but don't tell anyone)
  6. Integrate your new option in the GWT UI (Extending the existing DTOs and Dialogs)

If feel the need to add a new category of preferences (i.e. adding a new preference fragment in res/xml/preference_xxx) be sure to modify _PreferenceHelper#resetPreferences_ to incorporate your new screen.