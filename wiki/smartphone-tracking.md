# Smartphone Tracking

[[_TOC_]]

## Introduction
On this page the decisions, architecture and API's for using smartphones as an additional input channel for Sailing Analytics are documented. Meanwhile, the architecture of this solution is designed to be flexible enough to support other types of input devices in the future, e.g. [Igtimi](http://www.igtimi.com/) trackers.

## Branches
* `race-board-admin`: manual UI based entry of mark passings for the time being, while we do not have a detection algorithm
* `cmd-android-tracking`: client side development branch for the tracking application, which enhances the existent race committee app, server side development of using the RaceLog to create a tracking adapter for Commodity Mobile Devices (CMD) such as smartphones

## Communication

### Channels

The current plan is to use up to three channels for communicating:

1. **Servlets:** Everything that is not directly related to a specific Race (or rather RaceLog) is handled via POST and GET servlets, where the data should be described as JSON. Examples are: Creating a Race, managing Competitors. Ideally, this would on smartphone side however also benefit from the RaceLog-underlying semi-connectedness functionality. _Caveats: replication and persistence!_

2. **RaceLog:** All the "master data" communication concerning one race in particular should piggyback on the existing RaceLog-mechanism, which already deals with semi-connectedness, persistence and replication. Examples for this are: adding competitors to a race, mapping tracking devices to competitors, starting a race, defining the course layout.

3. **Other:** The actual tracking data (perhaps also additional data: wind etc.) also has to be transferred. On client side we want to reuse the communication mechanism which the RaceLog is built on top of.

### Communication during creating a race

![Typical communication between the App, its backend and the SAP Sailing Analytics server during the creation of a race](http://i.imagebanana.com/img/e1blf6xl/Capture.PNG)

## Server-side Architecture
On the server-side, the architecture of smartphone tracking is intended to be open for extension, so that different types of input devices can be used for tracking one race. Currently, some parts are still tightly coupled to smartphone tracking (e.g. `RacingEventService#createTrackedRaceForSmartphoneTracking()`), but these can be refactored to be generic.

Generic device identifiers, that are qualified through a device type, are used for mapping a device to a competitor in the race log. When the race is then created, the OSGi service registry is used to find services to which the appropriate device mappings in the race log are provided, e.g. a smartphone adapter registers a `RaceLogTrackingDeviceHandler` service for the device type `smartphoneImei`, and then recieves all smartphone device mappings from the race log once the race is created. The same OSGi service logic is used to find services that deal with persisting device identifiers, so that loading and persisting race log events can be used for all types of device identifiers.

The reason for using the OSGi service registry is that it enables decentralized implementation of different kinds of device adapters. E.g., implementing a new Igtimi adapter does not mean that you have to modify the object factory for device identifier objects in the persistence bundle, but simply register a new service from within your own bundle. This is more hindering than helpful in this stage of development, but - in future - means that other device adapters could be developed without having to touch the Sailing Analytics core code, so a vendor of tracking devices could recieve a current Sailing Analytics version as an SDK and implement additional bundles only.

## Mark Passings
Without a tracking provider that implements a mark passing algorithm, we have to identify mark passings on our own in the context of smartphone tracking, for Sailing Analytics to be able to do any analytics at all. While the mid-term goal definitely is to implement such a detection algorithm, as a workaround a UI entry option has been provided, that can be found in the branch `race-board-admin`, in which the mark passings can be set by hand. This can be accessed by clicking the _Administer Race_ button of a race in the leaderboard detail table, which can be found in the leaderboard configuration panel of the admin console.


## RaceLog Events
### RaceLogPersistentCompetitorRegisteredEvent
Includes a `Competitor` as well a `SmartphoneIdentifier`. On the one hand, every comptitor that is thus registered will be included in the `RaceDefinition` as soon as the race is created, on the other hand the mapping between smartphone identifier (e.g. IMEI for european phones) and competitor is later used for mapping the incoming fixes to the correct competitor.

### RaceLogPreRacePhaseEndedEvent
This does not include any additional data, and merely indicates that the race can be transformed from its pre-race definition state (e.g. waiting for competitors to register, waiting for boat class, waiting for course definition) to an actual race, where no additional competitors can be added, the boat class is fixes, and tracking may begin. This event is picked up by the `RaceLogRaceTracker`, which then creates the actual tracked race from the data in the RaceLog. For successful creation, at least one competitor has to be registered, and a course must have been set through a `RaceLogCourseDefinitionChangedEvent`.

##Tracking App Use Cases
![Use Cases](http://i.imagebanana.com/img/bda86luu/Use_Cases.jpg)

##Tracking App Architecture

![Activities and Fragments of the tracking app](http://i.imagebanana.com/img/20r2qf7g/overview_Fragments_and_Activities.JPG)

### `LocationChangedReceiver`
This class gets notified of location changes via intents. Depending on whether the App is in local or remote broadcast mode the corresponding service is started by using intents.

### `LocalLocationUpdateService`
Stores the location Information in a File by using the `FileWriterUtils` class.

### `NetworklocationUpdateService`
Sends the location information to a web service.

### `SAP Sailor Tracker Service`
Background process for starting, pausing and stopping tracking. Registers all receivers on a pending intent, which is send periodically.

### `AsyncJsonPostTask`
Async task that handles the execution of post requests with Json content.

### `OnlineDataManager`
Enables accessing of data from a `DataStore`. Loads data from Servlets using GET-Requests using a `DataLoader`. For an example how to use the `OnlineDataManager` refer to `SelectRaceFragment`, which loads the RaceLogsInPreRacePhase so that the user can select the race he wants to take part in.

### `DataStore`
Interface for the `DataStore` which stores all data that is relevant for the App (managed Races, Competitors, ...)
Implementation: `InMemoryDataStore`

### `DataLoader`
`AsyncDataLoader` which does an HTTP GET to a given URL, parses the data (with a `DataParser`) and sends the data to a `DataHandler`.

### `AppPreferences`
Helper Class for accessing the App Preferences specified in settings_view.xml

### `ListFragmentWithDataManager`
Base Class, which provides easy access to the data manager for a Fragment, which wants to display the data in a List.
Used for example in the `Select*Fragments`.

## ToDo

### Server
* only one competitor registered, even though multiple RegisteredEvents in RaceLog?
* change names of *Events, as they are not events -> also change names of abstract base classes
* use extension serializers
* exchange auto JSON to BSON conversion in MongoObjectFactory / DomainObjectFactory for something suited for productive use
* editing course in the RaceBoardAdmin
* mapping devices to marks
* generic method for registering listener for NMEA sentence types (e.g. to then process wind) -> move servlet for receiving NMEA out of smartphoneadapter
* accepting / removing competitors
 * for this we first need racelog replication back to all clients
 * also, not everybody should be able to do this -> see user management
* user management (Competitors as users, credentials so not everybody can do everything)
 * -> integrate with OAuth, ISAF competitors etc.?
* security (not everybody can start race, goes hand in hand with user management)
* support dynamic mapping of smartphone to competitor -> so that it can change during the race
* support other input channels (e.g. Igtimi)
* only transfer competitor ID for registering etc. instead of whole competitor

### Android
* change format of sent position Data to degrees and minutes
* reuse existing course design functionality to create RaceLogCourseDesignChangedEvent before sending RaceLogPreRacePhaseEndedEvent
* abstract sending service, so that all POST / GET requests and not only RaceLogEvents can be sent using the semi-connectedness functionality --> just write JSONObjects/Strings directly into the file. The Servlet has to handle deserialization and the client doesn't have to know what type of object it is after having saved it (is this really the case?)
* simplify settings
* login/register Activity for registering the Team and Sailor the first time the App is started