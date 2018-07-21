# Fix Tracking

[[_TOC_]]

## Introduction

The current fixes and tracks implementation provides an extensible micro-framework. The framework implements all the moving parts required for persisting and loading fixes and tracks. By defining interfaces and sticking to the visitor pattern, the framework also does help to implement new fix types.

The framework also provides a generic double fix vector implementation that can be used to store fixes that consist of double values. 

### Fixes and Tracks

In the domain model, `Tracks` hold and provide access to the `Fix` instances and provide some useful operations. `Tracks` themselves associated to a `TrackedRace`. `TrackedRace` instances hold and provide access to associated `Track` instances.

There are abstract `Track` implementations that e.g. provide `Timepoint` based access. Typically there are specific implementations that provide calculations based on the concrete `Fix` type.

### Mappings

In the persistence, fixes are saved for a given `DeviceIdentifier`. In the RegattaLog, specific `RegattaLogDeviceMappingEvent` subtype events map domain objects (e.g. `Competitor` or `Mark`) to `DeviceIdentifiers`.

The concrete mapping type event is used when loading fixes: this allows us to handle specific mappings according to the underlying mapping type.

### Persisting Fixes

The persistence of fixes is implemented by a `SensorFixStore` instance that supports saving and loading fixes. To be able to work with fixes on the persistence, a `DeviceIdentifier` needs to be mapped as described in "Mappings" above.

Fixes can be saved one by one or in batches. Saving single fixes is meant to be used in live mode when a device submits single fixes to the server. When importing archived fixes, this should always be done in batches to reduce the load on the DB as well as the time for the save operation.

The concrete MongoDB-based implementation of `SensorFixStore` is `MongoSensorFixStoreImpl`.

## Fixes on the persistence and in the domain model

The persistence and domain model fixes build upon on different type of Fixes: 
while we persist a certain sensor data in the persistence layer, the domain model can define a typed view on top of the underlying sensor datatype.
 lass 
There are different ways to implement extensions to handle new types of fixes.

The persistence defined by `SensorFixStore` implementations can be extended to store custom fix implementations.

In addition there is a special type of fix called `DoubleVectorFix` that can be used as a generic persistence that is based on a specific amount of double values. Based on this persistent fix type you can also define a mapping to be processed when loading fixes. Doing this, you do not need to extend the persistence but map such generic fixes to concrete ones.

The further chapters will guide through the process of extending the system for a new fix type or the usage of already existing fix types.

### Implementing new persistent fix type

Any fix type to be persisted by `SensorFixStore` implementations needs to implement the `Timed` interface.

To enable `MongoSensorFixStoreImpl` to save and load specific data of your concrete fix type, you need to provide an implementation of `FixMongoHandler`. This implementation is responsible to construct MongoDB objects from the fix when saving and needs to map those MongoDB objects back to the fix objects when loading from the DB.

To make `MongoSensorFixStoreImpl` find your implementation, it needs to be registered as OSGi service and the `FixMongoHandler` interface needs to specifically be exported. In addition the qualified classname of your fix implementation must be given to the registered OSGi service using the key `TypeBasedServiceFinder.TYPE`.

    Dictionary<String, String> properties = new Hashtable<String, String>();
    properties.put(TypeBasedServiceFinder.TYPE, MyFixImpl.class.getName());
    ServiceRegistration<FixMongoHandler> registration = context.registerService(FixMongoHandler.class, new MyFixMongoHandlerImpl(), properties);

To be able to load the new fix type, you need to define a mapping event that implements `RegattaLogDeviceMappingEvent`. There's an abstract implementation `RegattaLogDeviceMappingEventImpl` that implements most of the needed common logic.

For your concrete mapping event implementation you need to provide a specific sub-interface of `RegattaLogDeviceMappingEvent`. This interface is used to extend the visitor interface `MappingEventVisitor`, so that you can call the correct visitor method in your mapping event implementation's `accept(MappingEventVisitor)` method.

In consequence of extending the `MappingEventVisitor` interface, you need to fix two implementations in `FixLoaderAndTracker` that now need a new method for your mapping event type. Here you need to implement the logic to load fixes into the specific `Track` of the `TrackedRace`:

* An implementation for loading fixes of a timerange
* An implementation to handle newly added fixes in live tracking

### Implementing SensorFix type

In contrast of handling the persistence and loading of the fixes by ourself, you can also provide an implementation based on `DoubleVectorFix`, that supports the storage of arbitrary amounts of double values and accessing those by index.

Similar to the above case, you need to provide a mapping event implementation. In this case there's no need to define a custom interface. For mapping `Competitors`, implement `RegattaLogDeviceCompetitorSensorDataMappingEvent`.

To load `DoubleVectorFixes` for your new mapping event type into a `Track`, you need to provide an implementation of `SensorFixMapper`. This implementation needs to be published as OSGi service:

    ServiceRegistration<SensorFixMapper> registration = context.registerService(SensorFixMapper.class, new MyFixMapper(), null);

The `SensorFixMapper` is responsible to obtain and/or create the correct `Track` given the `TrackedRace` and adding fixes to the specific `Track`.

### More specifics on Tracks

There are different ways to associate `Tracks` with a `TrackedRace`. e.g. `GPSFixes` are managed in distinct `GPSFixTrack` instances that are associated to `Competitor` or `Mark` instances known by the `TrackedRace`. This can also be implemented for a specific new Track/Fix type.

In addition there's a generic association of `SensorFixTracks` that are not only associated to the specific domain object but also to a key so that you can easily have multiple additional `Tracks` for a domain object of a `TrackedRace`.

## Import of Fixes

Importing Fixes uses the the defined persistence to store fix data uploaded through the admin console. The upload is a simple http form based upload that delivers a file to be parsed. The servlet than delivers the imported devide uuid, so the frontend can properly load the persisted data.

The `SensorDataImportServlet` does process the uploaded file by extending `AbstractFileUploadServlet` and retrieving available importers through the OSGi registry. The `AbstractFileUploadServlet` does process the incoming http file upload. The OSGi registry delivers instance references to previously registered importers.

It is possible to register different `DoubleVectorFixImporter` implementations. The `SensorDataImportServlet` matches the `getType` information provided by the importers found in the registry against the selected importer information in the upload form in the admin console. The first importer to match the requested import type is used to process/ import the uploaded file.


### Adding importer

Further `DoubleVectorFixImporter` implementations can be registered to the OSGi registry through a bundle activator, e.g.: `com.sap.sailing.server.trackfiles.impl.Activator`.

The importer to be used to process the file is defined by the user by choosing the appropriate importer in the admin console upload formular. 
The list available importer types is retrieved dynamically in the administration console. Therefore, simply registering an importer as a service in the OSGi registry makes it available in the admin ui.

## Bringing sensor data to the frontend

Usually, the imported fixes also should be displayed in the leaderboard and eventually in the competitor chart.

Further reading can be found [here](../../info/landscape/typical-development-scenarios.html) in the sections:

* Adding a Column to the Leaderboard
* Adding new available information to the competitor chart

The currently implemented bravo fixes showcase how this information can be brought to the frontend, in short:

* add information to DetailType and DetailTypeFormatter and to the StringMessages accordingly
* add information to BravoFix and BravoFixImpl
* add information to BravoFixTrack and BravoFixTrackImpl
* add information to LegEntryDTO 
* add information to LeaderboardPanel
* add information to AbstractSimpleLeaderboardImpl
* create column classes in LegColumn for the new DetailTypes, eventually create new column classes and renderers if required
* add information to TrackedLegOfCompetitorImpl
* add information to the list of available types in MultiCompetitorRaceChartSettingsComponent
* make SailingServiceImpl.getCompetitorRaceDataEntry provide the data for the requested detailtype


