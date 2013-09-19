# Smartphone Tracking

[[_TOC_]]

## Introduction
On this page the decisions, architecture and API's for using smartphones as an additional input channel for Sailing Analytics are documented. Meanwhile, the architecture of this solution is designed to be flexible enough to support other types of input devices in the future, e.g. [Igtimi](http://www.igtimi.com/) trackers.

## Branches
* `race-board-admin`: manual UI based entry of mark passings for the time being, while we do not have a detection algorithm
* `cmd-android-tracking`: client side development branch for the tracking application, which enhances the existent race committee app, server side development of using the RaceLog to create a tracking adapter for Commodity Mobile Devices (CMD) such as smartphones

## Further links
* [Servlets](/wiki/smartphone-tracking/servlets)
* [Race Log Events](/wiki/smartphone-tracking/race-log-events)
* [Tracking App Architecture](/wiki/smartphone-tracking/app-architecture)
* [To-Do](/wiki/smartphone-tracking/to-do)

## Communication

### Channels

The current plan is to use up to three channels for communicating:

1. [**Servlets:**](/wiki/smartphone-tracking/servlets) Everything that is not directly related to a specific Race (or rather RaceLog) is handled via POST and GET servlets, where the data should be described as JSON. Examples are: Creating a Race, managing Competitors. Ideally, this would on smartphone side however also benefit from the RaceLog-underlying semi-connectedness functionality. _Caveats: replication and persistence!_

2. [**RaceLog:**](wiki/smartphone-tracking/race-log-events) All the "master data" communication concerning one race in particular should piggyback on the existing RaceLog-mechanism, which already deals with semi-connectedness, persistence and replication. Examples for this are: adding competitors to a race, mapping tracking devices to competitors, starting a race, defining the course layout.

3. **Other:** The actual tracking data (perhaps also additional data: wind etc.) also has to be transferred. On client side we want to reuse the communication mechanism which the RaceLog is built on top of. Right now, this is also handed by a [servlet](/wiki/smartphone-tracking/servlets#recordFixes) on server-side, and through the `EventSendingService` on Android-side (which handles semi-connectedness).

### Communication during creating a race

![Typical communication between the App, its backend and the SAP Sailing Analytics server during the creation of a race](http://i.imagebanana.com/img/e1blf6xl/Capture.PNG)

## Server-side Architecture
On the server-side, the architecture of smartphone tracking is intended to be open for extension, so that different types of input devices can be used for tracking one race.

Generic device identifiers, that are qualified through a device type, are used for mapping a device to a competitor in the race log. When the race is then created, the OSGi service registry is used to find services to which the appropriate device mappings in the race log are provided, e.g. a smartphone adapter registers a `RaceLogTrackingDeviceHandler` service for the device type `smartphoneImei`, and then recieves all smartphone device mappings from the race log once the race is created. The same OSGi service logic is used to find services that deal with persisting device identifiers, so that loading and persisting race log events can be used for all types of device identifiers.

The reason for using the OSGi service registry is that it enables decentralized implementation of different kinds of device adapters. E.g., implementing a new Igtimi adapter does not mean that you have to modify the object factory for device identifier objects in the persistence bundle, but simply register a new service from within your own bundle. This is more hindering than helpful in this stage of development, but - in future - means that other device adapters could be developed without having to touch the Sailing Analytics core code, so a vendor of tracking devices could recieve a current Sailing Analytics version as an SDK and implement additional bundles only.

## Mark Passings
Without a tracking provider that implements a mark passing algorithm, we have to identify mark passings on our own in the context of smartphone tracking, for Sailing Analytics to be able to do any analytics at all. While the mid-term goal definitely is to implement such a detection algorithm, as a workaround a UI entry option has been provided, that can be found in the branch `race-board-admin`, in which the mark passings can be set by hand. This can be accessed by clicking the _Administer Race_ button of a race in the leaderboard detail table, which can be found in the leaderboard configuration panel of the admin console.

##Tracking App Use Cases
![Use Cases](http://i.imagebanana.com/img/bda86luu/Use_Cases.jpg)