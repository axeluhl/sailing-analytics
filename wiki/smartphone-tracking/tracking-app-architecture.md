# Tracking App Architecture
[[_TOC_]]

## Activities and Fragments of the tracking app
![Activities and Fragments of the tracking app](http://i.imagebanana.com/img/20r2qf7g/overview_Fragments_and_Activities.JPG)

## Communication during the race
![Typical communication between the App, its backend and the SAP Sailing Analytics server during the creation of a race](http://i.imagebanana.com/img/e1blf6xl/Capture.PNG)

## Components
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