[[_TOC_]]

# Introduction
Some important decisions and interface specifications for smartphone tracking are recorded on this page.

# Communication Channels
The current plan is to use up to three channels for communicating:

1. **Servlets:** Everything that is not directly related to a specific Race (or rather RaceLog) is handled via POST and GET servlets, where the data should be described as JSON. Examples are: Creating a Race, managing Competitors.

2. **RaceLog:** All the "master data" communication concerning one race in particular should piggyback on the existing RaceLog-mechanism, which already deals with semi-connectedness, persistence and replication. Examples for this are: adding competitors to a race, mapping tracking devices to competitors, starting a race, defining the course layout.

3. **Other:** The actual tracking data (perhaps also additional data: wind etc.) also has to be transferred. As we also have to deal with semi-connectedness, one idea is to reuse the communication mechanism which the RaceLog is built on top of. In case this cannot deal with the large amounts of data produced by tracking devices, or is to tightly coupled with the RaceLog semantics, a possible alternative is using CouchDB and its replication mechanism. The downside here is a further increased technology stack on client and server side, and a hetorgenous communication mechanisms between client and server. This makes setting up a development environment, understanding the architecture, development and lifecycle management of the used technologies on the server (OSGi, RabbitMQ with Erlang, MongoDB, and possibly CouchDB) even more difficult.

# Servlets
## `/sailingserver/tracking/createFlexibleLeaderboard`
`CreateFlexibleLeaderboardPostServlet`

**Expects**
* POST request body: Leaderboard-JSON (see `LeaderboardJsonSerializer`)

**Returns**
* `200` Leaderboard created

**Throws**
* `400` Invalid JSON in request
* `409` Leaderboard with name %s already exists

## `/sailingserver/tracking/createRaceColumn`
`CreateFlexibleLeaderboardPostServlet`

**Expects**
* URL Parameter: `leaderboard` leaderboard name
* POST request body: RaceColumn-JSON (see `RaceColumnDeserializer`)

**Returns**
* `200` RaceColumn created

**Throws**
* `400` Missing paramter / Invalid JSON in request
* `404` Leaderboard not found
* `409` RaceColumn with name %s already exists / Error adding RaceColumn
