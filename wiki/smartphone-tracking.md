# Smartphone Tracking

[[_TOC_]]

## Introduction
Some important decisions and interface specifications for smartphone tracking are recorded on this page.

## Communication Channels
The current plan is to use up to three channels for communicating:

1. **Servlets:** Everything that is not directly related to a specific Race (or rather RaceLog) is handled via POST and GET servlets, where the data should be described as JSON. Examples are: Creating a Race, managing Competitors. Ideally, this would on smartphone side however also benefit from the RaceLog-underlying semi-connectedness functionality. _Caveats: replication and persistence!_

2. **RaceLog:** All the "master data" communication concerning one race in particular should piggyback on the existing RaceLog-mechanism, which already deals with semi-connectedness, persistence and replication. Examples for this are: adding competitors to a race, mapping tracking devices to competitors, starting a race, defining the course layout.

3. **Other:** The actual tracking data (perhaps also additional data: wind etc.) also has to be transferred. As we also have to deal with semi-connectedness, one idea is to reuse the communication mechanism which the RaceLog is built on top of. In case this cannot deal with the large amounts of data produced by tracking devices, or is to tightly coupled with the RaceLog semantics, a possible alternative is using CouchDB and its replication mechanism. The downside here is a further increased technology stack on client and server side, and a hetorgenous communication mechanisms between client and server. This makes setting up a development environment, understanding the architecture, development and lifecycle management of the used technologies on the server (OSGi, RabbitMQ with Erlang, MongoDB, and possibly CouchDB) even more difficult.

## Servlets
### `/sailingserver/tracking/createFlexibleLeaderboard`
`CreateFlexibleLeaderboardPostServlet`

**Expects**
* POST request body: LeaderboardDTO-JSON (see `LeaderboardDTOJsonSerializer`)

**Returns**
* `200` Leaderboard created, body: LeaderboardDTO as JSON

**Throws**
* `400` Invalid JSON in request
* `409` Leaderboard with name %s already exists

### `/sailingserver/tracking/createRaceColumn`
`CreateFlexibleLeaderboardPostServlet`

**Expects**
* URL Parameter: `leaderboard` leaderboard name
* POST request body: RaceColumn-JSON (see `RaceColumnDeserializer`)

**Returns**
* `200` RaceColumn created, body: RaceColumn as JSON

**Throws**
* `400` Missing parameter / Invalid JSON in request
* `404` Leaderboard not found
* `409` RaceColumn with name %s already exists / Error adding RaceColumn

### `/sailingserver/tracking/createPersistentCompetitor`
`CreatePersistentCompetitorPostServlet`

**Expects**
* POST request body: Competitor-JSON with a nested Boat-JSON and Team-JSON (see `CompetitorDeserializer`)

**Returns**
* `200` Competitor created, body: Competitor as JSON

**Throws**
* `400` Invalid JSON in request

### `/sailingserver/tracking/getPersistentCompetitors`
`PersistentCompetitorsGetServlet`

**Expects**
* GET request

**Returns**
* `200` body: JSON array of Competitor objects

### `/sailingserver/tracking/createRace`
`CreateRacePostServlet`

**Expects**
* POST request: no body

**Returns**
* `200` body: RaceDTO-JSON

**Throws**
* `400` Missing parameter
* `404` Leaderboard/RaceColumn/Fleet not found
* `409` Race has already been created

## RaceLog Events
### RaceLogPersistentCompetitorRegisteredEvent
Includes a `Competitor` as well a `SmartphoneIdentifier`. On the one hand, every comptitor that is thus registered will be included in the `RaceDefinition` as soon as the race is created, on the other hand the mapping between smartphone identifier (e.g. IMEI for european phones) and competitor is later used for mapping the incoming fixes to the correct competitor.

### RaceLogCreateRaceEvent
This does not include any additional data, and merely indicates that the race should be transformed from its pre-race definition state (e.g. waiting for competitors to register, waiting for boat class, waiting for course definition) to a fixed next state, where no additional competitors can be added, the boat class is fixes, and tracking may begin.

### Events that are still needed
* Set boat class
* Set course (reuse of existing events that Potsdam students already use)
* Remove registered competitor (also use in RegisteredCompetitorFinder)

##Tracking App Architecture

### `LocationChangedReceiver`
This class gets notified of location changes via intents. Depending on whether the App is in local or remote broadcast mode the corresponding service is started by using intents.

### `LocalLocationUpdateService`
Stores the location Information in a File by using the `FileWriterUtils` class.

### `NetworklocationUpdateService`
Sends the location information to a web service.

### `SAP Sailor Tracker Service`
Background process for starting, pausing and stopping tracking. Registers all receivers on a pending intent, which is send periodically.

## ToDo
* persist tracking data (GPSFixStore)
* user management (Competitors as users, credentials so not everybody can do everything)
* security (not everybody can start race, goes hand in hand with user management)
* load stored tracked smartphone race (Panel in Admin Console)