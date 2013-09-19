# Tracking App Architecture
[[_TOC_]]

## Use Cases
![Use Cases](http://i.imagebanana.com/img/bda86luu/Use_Cases.jpg)

## Components

![Activities and Fragments of the tracking app](http://i.imagebanana.com/img/20r2qf7g/overview_Fragments_and_Activities.JPG)

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

### `DataStore`
Interface for the DataStore which stores all data that is relevant for the App (managed Races, Competitors, ...)
Implementation: InMemoryDataStore

### `DataLoader`
AsyncDataLoader which does an HTTP GET to a given URL, parses the data (with a DataParser) and sends the data to a DataHandler.