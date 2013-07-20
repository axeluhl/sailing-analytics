# Smartphone Tracking

[[_TOC_]]

## Introduction
On this page the decisions, architecture and API's for using smartphones as an additional input channel for Sailing Analytics are documented. Meanwhile, the architecture of this solution is designed to be flexible enough to support other types of input devices in the future, e.g. [Igtimi](http://www.igtimi.com/) trackers.

## Communication Channels
The current plan is to use up to three channels for communicating:

1. **Servlets:** Everything that is not directly related to a specific Race (or rather RaceLog) is handled via POST and GET servlets, where the data should be described as JSON. Examples are: Creating a Race, managing Competitors. Ideally, this would on smartphone side however also benefit from the RaceLog-underlying semi-connectedness functionality. _Caveats: replication and persistence!_

2. **RaceLog:** All the "master data" communication concerning one race in particular should piggyback on the existing RaceLog-mechanism, which already deals with semi-connectedness, persistence and replication. Examples for this are: adding competitors to a race, mapping tracking devices to competitors, starting a race, defining the course layout.

3. **Other:** The actual tracking data (perhaps also additional data: wind etc.) also has to be transferred. On client side we want to reuse the communication mechanism which the RaceLog is built on top of.

## Server-side Architecture
On the server-side, the architecture of smartphone tracking is intended to be open for extension, so that different types of input devices can be used for tracking one race. Currently, some parts are still tightly coupled to smartphone tracking (e.g. `RacingEventService#createTrackedRaceForSmartphoneTracking()`), but these can be refactored to be generic.

Generic device identifiers, that are qualified through a device type, are used for mapping a device to a competitor in the race log. When the race is then created, the OSGi service registry is used to find services to which the appropriate device mappings in the race log are provided, e.g. a smartphone adapter registers a `RaceLogTrackingDeviceHandler` service for the device type `smartphoneImei`, and then recieves all smartphone device mappings from the race log once the race is created. The same OSGi service logic is used to find services that deal with persisting device identifiers, so that loading and persisting race log events can be used for all types of device identifiers.

The reason for using the OSGi service registry is that it enables decentralized implementation of different kinds of device adapters. E.g., implementing a new Igtimi adapter does not mean that you have to modify the object factory for device identifier objects in the persistence bundle, but simply register a new service from within your own bundle. This is more hindering than helpful in this stage of development, but - in future - means that other device adapters could be developed without having to touch the Sailing Analytics core code, so a vendor of tracking devices could recieve a current Sailing Analytics version as an SDK and implement additional bundles only.

## Servlets
The servlets are listed in the chronological order that they can be called. First, persistent competitors are needed, so that they can later on be registered for the race (`createPersistentCompetitor`). These can then be listed (`getPersistentCompetitors`). When this is completed, a race in its pre-race phase can be created (`createRace`), which is then also shown in `getRaceLogsInPreRacePhase`. By selecting one of these RaceLogs and sending `RaceLogPersistentCompetitorRegisteredEvent`s, `RaceLogCourseDefinitionChangedEvent`s, the race can then be moved from its pre race phase into the tracking phase by sending the `RaceLogPreRacePhaseEndedEvent` via the race log. From this moment on - given the fact that all necessary information was already included in the RaceLog, tracking data can be added to the race. On the one hand, marks can be pinged (`pingMark`, for which knowledge of the course layout is necessary, which can be accessed through `currentcourse`), on the other hand fixes of competitors can be recorded (`recordFixes`). Pinging the marks is of course only the first step, the plan is to allow the mapping of tracking devices such as smartphones to marks as well as competitors.

### `/sailingserver/racelogtracking/createPersistentCompetitor`
`CreatePersistentCompetitorPostServlet`

**Expects**
* POST request body: Competitor-JSON with a nested Boat-JSON and Team-JSON (see `CompetitorJsonDeserializer`)
```
{"id": "",
 "name": "Competitor Fredrik",
 "sailID": "1234",
 "team": {
   "name": "Team Fredrik",
   "sailors": [
     {
       "name": "Fredrik",
       "description": "",
       "dateOfBirth": 2394820480284,
       "nationality": {"IOC": "GER"}
     }
     ]
 },
 "boat": {
   "name": "Boat Fredrik",
   "sailID": "1234",
   "boatClass": {
     "name": "49er"
   }
 }
}
```

**Returns**
* `200` Competitor created, body: Competitor as JSON

**Throws**
* `400` Invalid JSON in request

### `/sailingserver/racelogtracking/getPersistentCompetitors`
`PersistentCompetitorsGetServlet`

**Expects**
* GET request

**Returns**
* `200` body: JSON array of Competitor objects


### `/sailingserver/racelogtracking/getRaceLogsInPreRacePhase`
`RaceLogsInPreRacePhaseGetServlet`

**Expects**
* GET request

**Returns**
* `200` body: JSON array of String Triples that act as RaceLog identifiers (leaderboard name, race column name, fleet name)

### `/sailingserver/racelogtracking/createRace`
`CreateRaceLogTrackedRacePostServlet`

**Expects**
* POST request body: JSON with Leaderboard-DTO, RaceColumn-DTO and BoatClass (see CreateRaceLogTrackedRaceJsonSerializer)
```
{"leaderboard": {
  "name": "test",
  "displayName": "test",
  "discardThresholds": [1,2],
  "scoringScheme": "LOW_POINT",
  "courseAreaId": "Kiel"
 },
 "raceColumn": {
  "name": "test",
  "isMedalRace": false
 },
 "boatClass": {
  "name": "49er",
  "typicallyStartsUpwind": true
 }
}
```

**Returns**
* `200` RaceLog and Race Tracker created

**Throws**
* `400` Invalid JSON in request
* `409` RaceColumn and RaceLog already exist in Leaderboard

**Comments**
If a leaderboard with the supplied does not already exist, a FlexibleLeaderboard is created. Otherwise, the existing Leaderboard is used without raising an error. An existing RaceColumn will raise an error however, as this also means a RaceLog already exists.

### `/smartphone/recordFixes`
`RecordFixesPostServlet`
**Precondition**
* race has already been started by sending a `RaceLogPreRacePhaseEndedEvent`

**Expects**
* POST request body: DeviceIdentifierWithGPSFixMovingsDTO as JSON
```
{"imei": "12345678",
  "data": [
    {"unixtime": 2394820480284,
     "nmea"    :  "$GPRMC,123519,A,4807.038,N,01131.000,E,022.4,084.4,230394,003.1,W*6A"
    },
    {"unixtime": 2394820480285,
     "nmea"    :  "$GPRMC,123519,A,4807.038,N,01131.000,E,022.4,084.4,230394,003.1,W*6A"
    }
  ]
}
```

**Returns**
* `200`: fixes recorded

**Throws**
* `409` Device not mapped to race and competitor

### `/sailingserver/racelogtracking/pingMark?leaderboard=<leaderboardName>&raceColumn=<raceColumnName>&fleet=<fleetName>`
`PingMarkPostServlet`
**Precondition**
* race has already been started by sending a `RaceLogPreRacePhaseEndedEvent` and has not been stopped yet

**Expects**
* POST request body: PingMark as JSON
```
{"unixtime": 2394820480284,
 "nmea"    :  "$GPRMC,123519,A,4807.038,N,01131.000,E,022.4,084.4,230394,003.1,W*6A"
 }
```

**Returns**
* `200`: mark pinged

**Throws**
* `404` Leaderboard, RaceColumn or Fleet not found
* `409` Race not in tracking state

### `/sailingserver/rc/currentcourse`
`CourseJsonExportServlet`

**Expects**
* GET request

**Returns**
* `200` body: CourseBase JSON


## RaceLog Events
### RaceLogPersistentCompetitorRegisteredEvent
Includes a `Competitor` as well a `SmartphoneIdentifier`. On the one hand, every comptitor that is thus registered will be included in the `RaceDefinition` as soon as the race is created, on the other hand the mapping between smartphone identifier (e.g. IMEI for european phones) and competitor is later used for mapping the incoming fixes to the correct competitor.

### RaceLogPreRacePhaseEndedEvent
This does not include any additional data, and merely indicates that the race can be transformed from its pre-race definition state (e.g. waiting for competitors to register, waiting for boat class, waiting for course definition) to an actual race, where no additional competitors can be added, the boat class is fixes, and tracking may begin. This event is picked up by the `RaceLogRaceTracker`, which then creates the actual tracked race from the data in the RaceLog. For successful creation, at least one competitor has to be registered, and a course must have been set through a `RaceLogCourseDefinitionChangedEvent`.


##Tracking App Architecture

### `LocationChangedReceiver`
This class gets notified of location changes via intents. Depending on whether the App is in local or remote broadcast mode the corresponding service is started by using intents.

### `LocalLocationUpdateService`
Stores the location Information in a File by using the `FileWriterUtils` class.

### `NetworklocationUpdateService`
Sends the location information to a web service.

### `SAP Sailor Tracker Service`
Background process for starting, pausing and stopping tracking. Registers all receivers on a pending intent, which is send periodically.

### `doPostTask`
Async task that handles the execution of post requests.

### `doGetTask`
Async task that handles the execution of get requests.

### `AppPreferences`
Helper Class for accessing the App Preferences specified in settings_view.xml


## ToDo

### Server
* deal with passes properly in racelog tracking events
* UI for setting mark roundings
 * use RaceViewer
 * select competitor -> checkbox for each waypoint, toggles markrounding (maybe add "addMarkRounding" to TrackedRace)
* persist tracking data (GPSFixStore)
* load stored tracked smartphone race (Panel in Admin Console, RaceLogConnector, only present such races with the necessary data in the racelog, and allow user to select whole leaderboard to restore)
* mapping devices to marks
* generic method for registering listener for NMEA sentence types (e.g. to then process wind) -> move servlet for recieving NMEA out of smartphoneadapter
* accepting / removing competitors
 * for this we first need racelog replication back to all clients
 * also, not everybody should be able to do this -> see user management
* user management (Competitors as users, credentials so not everybody can do everything)
 * -> integrate with OAuth, ISAF competitors etc.?
* security (not everybody can start race, goes hand in hand with user management)
* support dynamic mapping of smartphone to competitor -> so that it can change during the race
* support other input channels (e.g. Igtimi)

### Android
* reuse existing course design functionality to create RaceLogCourseDesignChangedEvent before sending RaceLogPreRacePhaseEndedEvent
* abstract sending service, so that all POST / GET requests and not only RaceLogEvents can be sent using the semi-connectedness functionality --> just write JSONObjects/Strings directly into the file. The Servlet has to handle deserialization and the client doesn't have to know what type of object it is after having saved it (is this really the case?)
* simplify settings
* login/register Activity for registering the Team and Sailor the first time the App is started