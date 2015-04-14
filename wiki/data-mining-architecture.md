# Data Mining Architecture

The data mining framework consists of four major parts. At first the general infrastructure, that handles the registration and deregistration of data mining bundles and provides information about the data mining components, that are currently available. The next part is about how to build and run queries. After that come the data processing components, that are used by queries and contain the main functionality of the framework. The last part are the functions, that are used by the data processing components and that can be changed during run-time.

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

The provided `ResourceBundleStringMessages` will be added to the string messages of the framework, when the data mining bundle is registered. The framework string messages are an instance of `CompoundResourceBundleStringMessages`, that logs message keys that occur multiple times with the log level `INFO`.

### Data Source Provider

A `DataSourceProvider` provides an instance of a data source, that is used by queries. A data source can be from any type, for example is the `RacingEventService` used as one. Your own `DataSourceProviders` should extend `AbstractDataSourceProvider`. Have a look at `RacingEventServiceProvider` for an example.<br />
These `DataSourceProviders` are used by the framework to create new queries.

### Client-Server Communication (Data Mining DTOs)

*This site is under construction.*

## Building and running Queries

### Defining and building a Data Retriever Chain

The interface `DataRetrieverChainDefinition<DataSourceType, DataType>` describes the order of retrieval processors ([Processors](#Processors) with the specific functionality to map an `InputType` to a `ResultType`) to get from the `DataSourceType` to the `DataType`. It provides methods to get some information about the retriever chain and to define the order of the processors. There's currently one implementation, which is the `SimpleDataRetrieverChainDefinition<DataSourceType, DataType>`. Every retriever chain has a `UUID` for identification, which is important for the client server communication. The `SimpleDataRetrieverChainDefinition` generates a new random `UUID` upon construction.

The `DataRetrieverChainDefinition<DataSourceType, DataType>` provides three methods to define the order of the retriever chain:

* The method `startWith` to define the first retrieval processor.
	* The other methods would throw an exception, if this method hasn't been called exactly once.
	* Calling this method more than once causes an exception.
* The method `addAfter` to add a retrieval processor to the end a the list.
	* The parameter `lastAddedRetrieverType` is necessary to ensure, that the `ResultType` and `InputType` of successive retrieval processors match.
* The method `endWith` to complete the retriever chain with the last retrieval processor
	* Completing a retriever chain makes it immutable.
	* This causes all modifying methods to throw an exception, if `endWith` has been called.
	* The parameter `lastAddedRetrieverType` is necessary to ensure, that the `ResultType` and `InputType` of successive retrieval processors match.

This results in the mandatory call order `startWith addAfter* endWith` to define a fresh retriever chain. This strict policy ensures, that the resulting retriever chain instances are type safe and work correctly. The retrieval processors added with this methods have to have a constructor with the signature `(ExecutorService, Collection<Processor<ResultType, ?>>)` or an exception will be thrown. This constructor is used to create concrete instances of the retrieval processors via reflection, when a retriever chain is constructed. All the definition methods (and the constructors of `SimpleDataRetrieverChainDefinition`) have a `String` parameter for a string message key, that would return the name of the retrieval level/chain. This is necessary to display the retriever chain in a human readable way.

This is an example for the correct definition of a fresh retriever chain:

	DataRetrieverChainDefinition<RacingEventService, HasTrackedLegOfCompetitorContext> legOfCompetitorRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>(RacingEventService.class, HasTrackedLegOfCompetitorContext.class, "LegSailingDomainRetrieverChain");
	legOfCompetitorRetrieverChainDefinition.startWith(LeaderboardGroupRetrievalProcessor.class, LeaderboardGroupWithContext.class, "LeaderboardGroup");
	legOfCompetitorRetrieverChainDefinition.addAfter(LeaderboardGroupRetrievalProcessor.class, LeaderboardRetrievalProcessor.class, HasLeaderboardContext.class, "Leaderboard");
	legOfCompetitorRetrieverChainDefinition.addAfter(LeaderboardRetrievalProcessor.class, TrackedRaceRetrievalProcessor.class, HasTrackedRaceContext.class, "Race");
	legOfCompetitorRetrieverChainDefinition.addAfter(TrackedRaceRetrievalProcessor.class, TrackedLegRetrievalProcessor.class, HasTrackedLegContext.class, "Leg");
	legOfCompetitorRetrieverChainDefinition.endWith(TrackedLegRetrievalProcessor.class, TrackedLegOfCompetitorRetrievalProcessor.class, HasTrackedLegOfCompetitorContext.class, "LegOfCompetitor");

The `SimpleDataRetrieverChainDefinition` has a second constructor, where the parameter for the `Class` of the `DataSourceType` is replaced with an `DataRetrieverChainDefinition`. This allows the reuse of existing retriever chains and initializes the new one in the state as if the method `startWith` has been called exactly once. This is an example for the correct reuse of an existing retriever:

	DataRetrieverChainDefinition<RacingEventService, HasTrackedLegOfCompetitorContext> legOfCompetitorRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>(RacingEventService.class, HasTrackedLegOfCompetitorContext.class, "LegSailingDomainRetrieverChain");
	legOfCompetitorRetrieverChainDefinition.startWith(LeaderboardGroupRetrievalProcessor.class, LeaderboardGroupWithContext.class, "LeaderboardGroup");
	legOfCompetitorRetrieverChainDefinition.addAfter(LeaderboardGroupRetrievalProcessor.class, LeaderboardRetrievalProcessor.class, HasLeaderboardContext.class, "Leaderboard");
	legOfCompetitorRetrieverChainDefinition.addAfter(LeaderboardRetrievalProcessor.class, TrackedRaceRetrievalProcessor.class, HasTrackedRaceContext.class, "Race");
	legOfCompetitorRetrieverChainDefinition.addAfter(TrackedRaceRetrievalProcessor.class, TrackedLegRetrievalProcessor.class, HasTrackedLegContext.class, "Leg");
	legOfCompetitorRetrieverChainDefinition.endWith(TrackedLegRetrievalProcessor.class, TrackedLegOfCompetitorRetrievalProcessor.class, HasTrackedLegOfCompetitorContext.class, "LegOfCompetitor");
	
	DataRetrieverChainDefinition<RacingEventService, HasGPSFixContext> gpsFixRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>(legOfCompetitorRetrieverChainDefinition, HasGPSFixContext.class, "GPSFixSailingDomainRetrieverChain");
    gpsFixRetrieverChainDefinition.endWith(TrackedLegOfCompetitorRetrievalProcessor.class, GPSFixRetrievalProcessor.class,HasGPSFixContext.class, "GpsFix");

Concrete data retriever chain instances can be constructed with a `DataRetrieverChainBuilder`. To get the builder of a **completed** `DataRetrieverChainDefinition` call its `startBuilding` method, which can be done as often as necessary. The interface `DataRetrieverChainBuilder<DataSourceType>` provides methods to iterate the defined retriever chain and to modify the current retrieval processor.

* The method `stepFurther` iterates over the retriever chain and sets the next retrieval processor as the one to modify.
	* Is similar to the `next` method of `Iterator`.
	* Initializes the builder, which is necessary to call the modifying methods.
* The method `canStepFurther` returns `true`, if `stepFurther` can be called at least once more.
	* Is similar to the `hasNext` method of `Iterator`

The builder has to be initialized, to call the modifying methods so that no exception is thrown. This means, that the `stepFurther` method has to be called at least once. This allows a better usage of the builder in loops as described in the construction examples.

* The method `setFilter`, sets the [Filter Criterion](#Filter-Criteria) that is applied after the current retrieval processor.
	* If this method is called more than once for the same retrieval processor, than the old filter is replaced by the new one. Use `CompoundFilterCriterion` to filter with multiple criteria.
	* The `ElementType` of the given criterion has to match the `ResultType` of the current retrieval processor or an exception will be thrown. Call the builders `getCurrentRetrievedDataType` method to get the current `ResultType`.
* The method `addResultReceiver` adds the given processor as a result receiver of the current retrieval processor, which means that all retrieved data elements will be forwarded to the result receiver.
	* It's possible to add more than one result receiver to the same retrieval processor.
	* Note that, successive retrieval processors will automatically be result receivers of the corresponding precursor.
	* The `InputType` of the given processor has to match the `ResultType` of the current retrieval processor or an exception will be thrown. Call the builders `getCurrentRetrievedDataType` method to get the current `ResultType`.

All iterating and modifying methods return the builder. This can be used to make multiple calls in a single statement. The method `build` constructs the configured retriever chain until the **current** retrieval processor and returns the **first** processor of the concrete retriever chain instance. This can be useful in some cases, but the standard use case is to configure the retrieval processors, that should be configured and iterate through the builder until `canStepFurther` returns `false` (this results in instances of the complete retriever chain). `build` can be called as often as necessary and it's legal to configure the chain, after `build` has been called. Note that the previously created retriever chain instance won't be affected from these changes.

This is an example how a `DataRetrieverChainBuilder<DataSourceType>` can be configured in a single statement, which is useful for tests or fix configurations:

	DataRetrieverChainBuilder<Collection<Test_Regatta>> chainBuilder = dataRetrieverChainDefinition.startBuilding(ConcurrencyTestsUtil.getExecutor());
	chainBuilder.stepFurther().stepFurther().setFilter(raceFilter).stepFurther().setFilter(legOfCompetitorFilter).addResultReceiver(legReceiver);
	Processor<Collection<Test_Regatta>, ?> firstRetrieverInChain = chainBuilder.build();

This is an example how a `DataRetrieverChainBuilder<DataSourceType>` can be configured with a loop, which is useful to create many different retriever chain instances of one definition:

	DataRetrieverChainBuilder<DataSourceType> chainBuilder = dataRetrieverChainDefinition().startBuilding(executor);
    Map<Integer, FilterCriterion<?>> criteriaMappedByRetrieverLevel = getFilterCriteriaForLevel();
    while (chainBuilder.canStepFurther()) {
        chainBuilder.stepFurther();
        
        if (criteriaMappedByRetrieverLevel.containsKey(chainBuilder.getCurrentRetrieverLevel())) {
            chainBuilder.setFilter(criteriaMappedByRetrieverLevel.get(chainBuilder.getCurrentRetrieverLevel()));
        }
    }
    chainBuilder.addResultReceiver(groupingProcessor);

	Processor<DataSourceType, ?> firstRetrieverInChain = chainBuilder.build();

### Building a Processor Query

*This site is under construction.*

### Running a Query directly

*This site is under construction.*

### Running a Query with the `DataMiningServer`

*This site is under construction.*

## Data Processing Components

### Processors

*This site is under construction.*

### Filter Criteria

*This site is under construction.*

### Data Types

*This site is under construction.*

## Data Mining Functions

Data Mining Functions are used by the [Data Processing Components](#Data-Processing-Components) to get the data from the data elements, that is necessary for the data processing. This can be for the filtration or grouping by dimension values (like the name of the regatta or the leg type) or for the extraction of the statistic values (like the traveled distance or the relative rank). Functions can be defined by annotating the methods of data types and domain types, that should be used to the data. There are currently the three annotations `Connector`, `Dimension` and `Statistic` with different properties and conditions as described below. This makes the system flexible and allows developers to add new or change existing dimensions and statistics very fast. This can also be done during run-time, if the corresponding OSGi-Bundles are hot deployed or refreshed (see [Registration and Deregistration of Data Mining Bundles](#Registration-and-Deregistration-of-Data-Mining-Bundles) for more information).

### Connecter

Methods marked with the annotation `Connector` indicate the framework, that the result type contains functions. The method has to match the following conditions, to be registered by the framework:

* It has no parameters.
* The return type isn't `void`.
* It should be side effect free.
	* This isn't checked by the framework, so methods with side effects will be registered, but this could have strange effects on the data mining.

Connecter can have the following properties:

* A `String messageKey` with the default `""`, that is used for the internationalization.
* An `int ordinal` with the default `Integer.MAX_VALUE`, that is used for the sorting of functions (the standard sorting is ascending).
* A `boolean scanForStatistics` with the default `true`, that indicates the framework, if the statistics contained by the return type of the marked method should be registered.
	* This is useful for connections to *higher level* data types, to be able to use their dimensions, without the registration of unwanted or even wrong statistics.

How the presence of multiple `messageKeys` and `ordinals` in compound functions work is described in [Compound Functions](#Compound-Functions).

### Dimensions

Methods marked with the annotation `Dimension` will be registered as dimension by the framework. The method will be called via reflection, if the dimension value of data elements of this data type is requested (for example to filter or group the data). The method has to match the following conditions, to be registered by the framework:

* It has no parameters, except if the parameter list is exactly `Locale, ResourceBundleStringMessages`.
* The return type isn't `void`.
* It should be side effect free.
	* This isn't checked by the framework, so methods with side effects will be registered, but this could have strange effects on the data mining.

The return type of the marked method should be:

* A primitive type or wrapper class or
* Classes that implement `equals`, `hashCode`
	* Or the grouping could become incorrect.
* and `toString`.
	* Or the result presentation will be unreadable.

Dimensions have the following properties:

* The mandatory `String messageKey`, that is used for the internationalization.
* An `int ordinal` with the default `Integer.MAX_VALUE`, that is used for the sorting of functions (the standard sorting is ascending).

How the presence of multiple `messageKeys` and `ordinals` in compound functions work is described in [Compound Functions](#Compound-Functions).

### Statistics

Methods marked with the annotation `Statistic` will be registered as computable statistics by the framework. The method will be called via reflection, if the statistic value of a data elements of this data type is requested (for example for the statistic extraction). The method has to match the following conditions, to be registered by the framework:

* It has no parameters, except if the parameter list is exactly `Locale, ResourceBundleStringMessages`.
* The return type isn't `void`.
* It should be side effect free.
	* This isn't checked by the framework, so methods with side effects will be registered, but this could have strange effects on the data mining.

The return type of the method has to be processable by an aggregator (described in [Processors](#Processors)) of the data mining framework, for example `int` or `double`.

Statistics have the following properties:

* The mandatory `String messageKey`, that is used for the internationalization.
* An `int ordinal` with the default `Integer.MAX_VALUE`, that is used for the sorting of functions (the standard sorting is ascending).
* A `Unit resultUnit` with the default `Unit.None`, that is shown in the result presentation.
	* `Unit` is an enum located in the bundle `com.sap.sse.datamining.shared`.
	* Every member of `Unit` needs a message key in the string messages of the bundle `com.sap.sse.datamining` for internationalization purposes. For example has the unit `Meters` the entry `Meters=m` in the file `StringMessages_en.properties`.
* An `int resultDecimals` with the default `0`, that sets the number of the visible result decimals.

How the presence of multiple `messageKeys` and `ordinals` in compound functions work is described in [Compound Functions](#Compound-Functions).

### Function Registration Process

The function registration process is triggered, when a [data mining bundle is registered](#Registration-and-Deregistration-of-Data-Mining-Bundles). The classes returned by the method `getClassesWithMarkedMethods` of the `DataMiningBundleService` are given to a `FunctionRegistry`. The registry iterates over all public methods (including those declared by the class or interface and those inherited from superclasses and superinterfaces) for each of the given classes and does the following:

* If the method is a valid [Connector](#Connecter), then the method gets converted to a `Function`, which is added to the list of *previous functions*. The return type of the function gets registered to the `FunctionRegistry`.
* If the method is a valid [Dimension](#Dimensions), then the method gets converted to a `Function`, which is registered as dimension. If the list of *previous functions* isn't empty, than the list and the dimension will be saved as `ConcatenatingCompoundFunction`.
* If the method is a valid [Statistic](#Statistics) and no connector in the list of *previous functions* has set the `scanForStatistics` to `false`, then the method gets converted to a `Function`, which is registered as statistic. If the list of *previous functions* isn't empty, than the list and the statistic will be saved as `ConcatenatingCompoundFunction`.

What is a valid connector/dimension/statistic and what isn't is described in the corresponding sections of this wiki page. How the `ConcatenatingCompoundFunction` works in detail, is described in [Compound Functions](#Compound-Functions).

Currently unsupported are

* Annotating a method with multiple data mining annotations.
	* The annotations `Dimension` and `Statistic` have a higher priority than `Connector`, so a method annotated with `Dimension` and `Connector` will be handled as `Dimensions`.
* Cycles in the annotation graph.
	* For example, if `A` has the connector `B foo()` and `B` has the connector `A bar()`.
	* This would result in an infinite loop during the registration process, that should lead to a `StackOverflowError`.

### Compound Functions

Compound functions provide the `Function<ReturnType>` interface for a collection of functions. There is currently only the `ConcatenatingCompoundFunction`, that capsules a list of functions, that are called one after another (like the java statement `foo().bar().stat();`). It's necessary to handle some of the methods declared in the `Function` interface in a special way, what is described here.

* The methods `getDeclaringType` and `getParameters` return the corresponding value of the **first** function in the list.
* The methods `getReturnType`, `getResultUnit`, `getResultDecimals` and `isDimension` return the corresponding value of the **last** function in the list.
* The `tryToInvoke` methods invoke the first function in the list with the given instance and all other functions with the return value of the previous function.
	* It returns `null`, if one of the functions in the list returned `null`.
* The method `getOrdinal` returns the smallest ordinal of the functions in the list.
* The method `getSimpleName` returns the name of the function, if the name has been set. If not returns the method the concatenated simple names of the functions in the list separated by `->`.
* The method `isLocalizable` returns `true`, if any of the functions in the list is localizable.
* The method `getLocalizedName` returns the name of the function, if the name has been set, the simple name, if the compound function isn't localizable or the concatenated simple names of the functions in the list separated by a single space.