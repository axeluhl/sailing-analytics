# RaceLog Tracking Server Architecture

[[_TOC_]]

# Development Infos
RaceLog tracking has been merged into the master branch with commit 4b4b61b6c518139f4389aabd73c282621711855a. It was developed on the following branches:
* ``ftes-rltracking``: The main RaceLog-tracking development
* ``ftes-rltracking-equestrian``: Serves as a reference implementation of a tracking adapter built for RaceLog-tracking.

# Architectural Overview
**The basic architecture of Racelog-tracking is presented in this diagram:
download as [PDF](/wiki/info/mobile/event-tracking/architecture.pdf) or [SVG](/wiki/howto/misc/event-tracking/architecture.svg)**

Following the numbers in the diagram, this is a possible race log tracking scenario:

1. The race is defined through events that are added to the RaceLog.
2. The RaceTracker is created.
3. As soon as the "StartTrackingEvent" is added to the RaceLog, the RaceTracker creates the TrackedRace.
4. Tracking devices submit GPS fixes to the tracking adapter. This adds these fixes to the global GPSFixStore.
5. The GPSFixStore notifies its listeners - among them the RaceTrackers - of new fixes. Each RaceTracker checks whether a new fix matches the mappings defined in its RaceLog, and if so adds it to the TrackedRace. 

Not all identifiers represent actual class or bundle names. For a more technical documentation, refer to the JavaDoc in the various ``*.racelog.tracking`` packages and the ``com.sap.sailing.domain.racelogtrackingadapter`` bundle. The following interfaces and classes are good starting points:
* DeviceIdentifier
* DeviceIdentifierSerializationHandler
* Revokable
* DeviceMappingFinder
* PingDeviceIdentifier, PlaceHolderDeviceIdentifier

# AdminConsole Components
Currently, RaceLogs can be filled with the race metadata via the _RaceLog Tracking_ panel in the AdminConsole. Here one can denote RaceColumn/Fleet combinations within RegattaLeaderboards for RaceLog-tracking, register competitors, define the course layout, add device mappings, and finally start tracking.

When thinking about smartphone tracking, it would of course be a good idea to also integrate similar functionality into the tracking app. Some parts of the communication (registering competitors, defining the course, mapping devices) can be handled via the existing RaceLog communication mechanism. Other parts (creating the Leaderboard structure in the first place, adding the RaceLog tracker) have to be dealt with separately, e.g. via a REST API.

## Steps for setting up a racelog-tracked race in the AdminConsole

[[Walkthrough with screenshots|wiki/info/mobile/event-tracking/event-tracking]]

## Reloading a race that has already been tracked

Technically, the entire race is loaded as soon as the RaceLogRaceTracker is attached to the RaceLog, as it will find the already existing StartTrackingEvent and load all fixes that correspond to the mappings.

In the AdminConsole, triggering this step is no different from adding the tracker for the very first time, do so with the _start tracking_ button the the RaceLog Tracking Panel.

# ToDos
## Archiving old Races
Since RaceLog-tracking allows for fully independent tracking, storing and reloading of races, it may be a good foundation for archiving old races. Currently, we rely on tracking providers to retain all data indefinitely, so that it can be reloaded, e.g., after restarting the server. Also see [[bug 2|http://bugzilla.sapsailing.com/bugzilla/show_bug.cgi?id=2]] for more details.

## Smartphone Tracking
RaceLog-tracking provides the server-side abilities for independent tracking. Therefore, using smartphones for tracking is one possible application. So far, there is no implementation in terms of a smartphone app. The relation to the existing RaceCommittee app has be be considered, as both should share a common code base, and as there should be certain points of integration (e.g., first defining the race in the tracking app, then setting the start time in the RC-app, and finally tracking one boat with that smartphone - again with the tracking app).

The first option is to simply share code through a Android library module, the second option is integrating everything into one app with two launchers (one for tracking, one for the RaceCommittee functionality).

# How to create a Tracking Adapter
By using the OSGi service registry, adapters are completely decoupled from the Sailing Analytics code. In future, it should be possible to simply deploy one additional bundle to the OSGi server in addition to the Sailing Analytics bundles. This requires provisioning the relevant bundles containing interfaces for the development of the adapter.

The first attempt so far (``ftes-rltracking-equestrian`` branch) is completely restricted to one bundle. However, this is a branch branched from ``master``, and the new bundle has been added to the existing launch configurations and build and deployment descriptors (``pom.xml``, ``feature.xml``, ``raceanalysis.product``). This makes developing and testing easy, but is not in line with the goal stated above.

Steps for creating an adapter:

1. Create a new project: ``com.sap.sailing.domain.<adaptername>``. Think about duplicating an existing project, such as ``com.sap.sailing.domain.racelogtrackingadapter``. In this case, don't forget to adapt the project's ``pom.xml``, ``META-INF/MANIFEST.MF`` and ``.project`` files.
2. Add the new project to the maven build in ``java/pom.xml``.
3. Add the project as an auto-start bundle to the eclipse launch configs. The bundle needs to be started before the central ``com.sap.sailing.server`` bundle, for which currently a startup-level of ``3`` is sufficient.
4. Add the bundle to the ``com.sap.sailing.feature/feature.xml``, and also to the ``com.sap.sailing.feature.p2build/raceanalysis.product`` with the same startup properties as before.
5. Implement a ``DeviceIdentifier`` and the appropriate ``DeviceIdentifierSerializationHandler``s (refer to the JavaDoc of ``DeviceIdentifier``).
6. Register your serialization handlers through the OSGi service registry (refer to the JavaDoc of ``DeviceIdentifierSerializationHandler`` for an example on how to do so).
7. Add some kind of Servlet or similar that adds fixes to the GPSFixStore by calling ``storeFix(DeviceIdentifier device, GPSFix fix)`` on the `GPSFixStore` which can be obtained from `RacingEventService.getGPSFixStore()`.
8. Fire up the server, let OSGi work its magic, define the race metadata and start adding fixes.