# Smartphone Tracking

[[_TOC_]]

## Introduction
On this page the decisions, architecture and API's for using smartphones as an additional input channel for Sailing Analytics are documented. Meanwhile, the architecture of this solution is designed to be flexible enough to support other types of input devices in the future, e.g. [Igtimi](http://www.igtimi.com/) trackers. A good overview of the topic is also provided by [[Jan Bross' thesis|doc/theses/20130913_Bross_Tracking_Sailing_Races_with_Mobile_Devices.pdf]].

## Branches
* `race-board-admin`: manual UI based entry of mark passings for the time being, while we do not have a detection algorithm
* `cmd-android-tracking`: client side development branch for the tracking application, which enhances the existent race committee app, server side development of using the RaceLog to create a tracking adapter for Commodity Mobile Devices (CMD) such as smartphones

## Further links
* [Race Board Admin](/wiki/smartphone-tracking/race-board-admin)
* [GPSFixStore](/wiki/smartphone-tracking/gpsfixstore)
* [Servlets](/wiki/smartphone-tracking/servlets)
* [Race Log Events](/wiki/smartphone-tracking/race-log-events)
* [Server-side Architecture](/wiki/smartphone-tracking/server-side-architecture)
* [Tracking App Architecture](/wiki/smartphone-tracking/tracking-app-architecture)
* [To-Do](/wiki/smartphone-tracking/to-do)

## Communication

### Channels

1. [**Servlets:**](/wiki/smartphone-tracking/servlets) Everything that is not directly related to a specific Race (or rather RaceLog) is handled via POST and GET servlets, where the data should be described as JSON. Examples are: Creating a Race, managing Competitors. Ideally, this would on smartphone side however also benefit from the RaceLog-underlying semi-connectedness functionality. _Caveats: replication and persistence!_

2. [**RaceLog:**](wiki/smartphone-tracking/race-log-events) All the "master data" communication concerning one race in particular should piggyback on the existing RaceLog-mechanism, which already deals with semi-connectedness, persistence and replication. Examples for this are: adding competitors to a race, mapping tracking devices to competitors, starting a race, defining the course layout.

3. **Other:** The actual tracking data (perhaps also additional data: wind etc.) also has to be transferred. On client side we want to reuse the communication mechanism which the RaceLog is built on top of. Right now, this is also handed by a [servlet](/wiki/smartphone-tracking/servlets#recordFixes) on server-side, and through the `EventSendingService` on Android-side (which handles semi-connectedness).

### Current communication process
1. create a flexible or regatta leaderboard in the Admin Console (and also a series with races if using a regatta leaderboard)
2. [create a persistent competitor](/wiki/smartphone-tracking/servlets#createPersistentCompetitor)
3. [create a race-log-tracked race](/wiki/smartphone-tracking/servlets#createRace)
4. [register the competitors](/wiki/smartphone-tracking/race-log-events#Persistent-Competitor-Registered)
5. [set the course](/wiki/smartphone-tracking/race-log-events#Course-Design-Changed)
5. [start the race](/wiki/smartphone-tracking/race-log-events#Pre-Race-Phase-Ended)
6. [set the start time](/wiki/smartphone-tracking/race-log-events#Start-Time)
7. [ping the marks](/wiki/smartphone-tracking/servlets#pingMark)
8. [send the competitor fixes](/wiki/smartphone-tracking/servlets#recordFixes)
9. [set the mark rounding times](/wiki/smartphone-tracking/race-board-admin)
10. view the race in the race viewer
11. [after a server restart, reload the race from the MongoDB](/wiki/smartphone-tracking/gpsfixstore)

## Mark Passings
Without a tracking provider that implements a mark passing algorithm, we have to identify mark passings on our own in the context of smartphone tracking, for Sailing Analytics to be able to do any analytics at all. A detection algorithm is being developed in the branch `mark-rounding-inference`. Until it is completed a UI entry option has been provided as a workaround. It can be found in the branch `race-board-admin`, in which the mark passings can be set by hand. This can be accessed by clicking the _Administer Race_ button of a race in the leaderboard detail table, which can be found in the leaderboard configuration panel of the admin console.