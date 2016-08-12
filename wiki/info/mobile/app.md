# RaceLog Tracking App Prototype Architecture

[[_TOC_]]

# Development Info
Development is currently underway on the following branches:
* ``ftes-rltracking-app``
* ``jbross-rltracking-app``

# Project and Build structure
## Projects
* ``com.sap.sailing.android.shared`` contains shared code and resources between the Android apps (tracking and [[RCApp|wiki/info/mobile/racecommittee-app]])
* ``com.sap.sailing.android.tracking.app`` is the main project for the app

## Maven
The app cannot only be built and deployed via the Android Eclipse tooling, but is also integrated into the maven build.

## Deployment

# Sending Fixes
* currently to the ``RecordFixesFlatJsonPostServlet`` in the ``racelogtrackingadapter``
```
{ deviceUuid: ...,
  timeMillis: ...,
  latDeg: ...,
  lonDeg: ...,
  speedMperS: ...,
  bearingDeg: ... }
```

# Mapping to a Competitor
We use a QRCode to transfer the necessary server, race and competitor data to the smartphone. The app can then add the mapping to the RaceLog (the race data is necessary to determine which RaceLog to add to, and the MappingEvent that is added to the RaceLog then contains the DeviceIdentifier and Competitor ID).

Structure of the URL encoded in the QRCode:
```
<serverURL-and-port>/apps/com.sap.sailing.android.tracking.app.apk
  ?leaderboard=<...>
  &raceColumn=<...>
  &fleet=<...>
  &competitor=<...>
```

By beginning the URL with ``<serverURL-and-port>/apps/com.sap.sailing.android.tracking.app.apk`` it is possible to download and install the app directly, if the QRCode is scanned with a barcode-scanner app.