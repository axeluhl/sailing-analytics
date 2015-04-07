# Data Mining Architecture

The data mining framework consists of four major parts. At first the general infrastructure, that handles the registration and deregistration of data mining bundles and provides information about the data mining components, that are currently available. The next part is about how to build and run queries. After that come the data processing components, that are used by queries and contain the main functionality of the framework. The last part are the functions, that are used by the data processing components and that can be changed during runtime.

[[_TOC_]]

## General Infrastructure

The central place to get information about the current state of the data mining and to perform actions is the OSGi-Service `DataMiningServer`. It provides information about the statistics, dimensions or data retriever chains, that are currently available. It also provides methods to create and run queries with a specific structure. For example a query, that returns all dimension values for a data type, or a standard statistic query (that is described in [Building and running Queries](#Building-and-running-Queries)). The server also has methods to get necessary elements to build your own queries (like an `ExecutorService` or the `ResourceBundleStringMessages`) or to convert DTOs to the data mining domain objects.

### Registration and Deregistration of Data Mining Bundles

The data mining framework listens constantly to the registrations and deregistrations of `DataMiningBundleServices` and adds/removes their provided data mining components. Such components are for example:

* An instance of `ResourceBundleStringMessages`, that contains the string messages used by the data mining bundle
* An iterable of `DataSourceProviders`, that are used to get the data source for queries
* An iterable of `DataRetrieverChainDefinitions`, that are used by the bundle (see [Building and running Queries](#Building-and-running-Queries))
* An iterable of `Classes`, that provide the functions of the bundle (see [Data Mining Functions](#Data-Mining-Functions))

To register/deregister a data mining bundle, you just have to register/deregister its `DataMiningBundleService` to the OSGi-Context. An easy way to achieve this, is to extend the `AbstractDataMiningActivator` with the activator of the data mining bundle. This handles the registration/deregistration in the start/stop-method and implements the `DataMiningBundleService`, which forces the concrete activator to implement the component providing methods.

### String Messages

A data mining bundle needs string messages, that are created server-side (for example to internationalize the display name of a function). To create string messages for the bundle do the following steps:

* Create the new source folder `resources`
* Add the package `<string messages package name>` to `resources`
* Create empty properties files for each supported language with the name scheme `<string messages base name>_<locale acronym>`
* Create a `ResourceBundleStringMessages` in the `DataMiningBundleService`
	* The `ResourceBundleStringMessages` needs the resource base name, which should be `<string messages package name>/<string messages base name>`
	* A reference to this `ResourceBundleStringMessages` should be returned by the method `getStringMessages()`

The provided `ResourceBundleStringMessages` will be added to the string messages of the framework, when the data mining bundle is registered. The framework string messages are an instance of `CompoundResourceBundleStringMessages`, that logs message keys that occure multiple times with the log level `INFO`.

### Data Source Provider

A `DataSourceProvider` provides an instance of a data source, that is used by queries. A data source can be from any type, for example is the `RacingEventService` used as one. Your own `DataSourceProviders` should extend `AbstractDataSourceProvider`. Have a look at `RacingEventServiceProvider` for an example.<br />
These `DataSourceProviders` are used by the framework to create new queries.

## Building and running Queries

### Defining and building a Data Retriever Chain

### Building a Processor Query

### Running a Query directly

### Running a Query with the `DataMiningServer`

## Data Processing Components

### Processors

### Filter Criteria

## Data Mining Functions

### Connectors

### Statistics

### Dimensions

### Function Registration Process

### Compound Functions