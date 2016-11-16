# Fix Tracking

[[_TOC_]]

## Introduction

TODO

### Fixes and Tracks

In the domain model, Fixes are held by Tracks that allow access to the fixes and support some useful operations. `Tracks` are associated to a `TrackedRace` that gives access to its `Track` instances.

There are abstract `Track` implementations that e.g. provide `Timepoint` based access. Typically there are specific implementations that provide calculations based on the concrete `Fix` type.

### Something about mappings

In the RegattaLog, there are specific event implementations that are subtypes of `RegattaLogDeviceMappingEvent`. These events map domain objects (e.g. `Competitor` or `Mark`) to `DeviceIdentifiers`. In the persistence, fixes are saved for a `DeviceIdentifier`.
The concrete type of the mapping event is used when loading fixes so that specific mappings can be handled specifically.

### Persistence of Fixes

The persistence of fixes is defined by `SensorFixStore` that supports saving fixes as well as loading fixes. To be able to work with fixes on the persistence, a `DeviceIdentifier` needs to be mapped as described above.

Fixes can be saved one by one or in batches. Saving single fixes is meant to be used in live mode when a device submits single fixes to the backend. When importing archived fixes, this should always be done in batches to reduce the load on the DB as well as the time for the save operation.

The concrete MongoDB-based implementation of `SensorFixStore` is `MongoSensorFixStoreImpl`.

## Fixes on the persistence and in the domain model

There are different types of fixes used by the persistence and domain model. There are different ways to implement extensions to handle new types of fixes.

The persistence defined by `SensorFixStore` implementations can be extended to store custom fix implementations.

In addition there is a special type of fix called DoubleVectorFix that can be used for a generic persistence that is based on a specific amount of double values. Based on this persistent fix type you can also define a mapping to be processed when loading fixes. Doing this, you do not need to extend the persistence but map such generic fixes to concrete ones.

The further chapters will guide through the process of extending the system for a new fix type or the usage of already existing fix types.

### Fix loading process

TODO
* Add a graph
* Visitor pattern

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

TODO

### Adding importer

TODO