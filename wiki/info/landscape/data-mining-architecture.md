# Data Mining Architecture

The data mining framework consists of four major parts. At first the general infrastructure, that handles the registration and deregistration of data mining bundles and provides information about the data mining components, that are currently available. The next part is about how to build and run queries. After that come the data processing components, that are used by queries and contain the main functionality of the framework. The last part are the functions, that are used by the data processing components and that can be changed during run-time.

[[_TOC_]]

## General Infrastructure

The central place to get information about the current state of the data mining and to perform actions is the OSGi-Service `DataMiningServer`. It provides information about the statistics, dimensions, data retriever chains and other data mining components, that are available at runtime. It also provides methods to create and run queries with a specific structure. For example a query, that returns all dimension values for a Fact, or a standard statistic query (that is described in [Building and running Queries](#Building-and-running-Queries)). The server also has methods to get necessary elements to build your own queries (like an `ExecutorService` or the `ResourceBundleStringMessages`) or to convert DTOs to the data mining domain objects.

### Registration and Deregistration of Data Mining Bundles

The data mining framework listens constantly to the registrations and deregistrations of `DataMiningBundleServices` and adds/removes their provided data mining components. Such components are for example (see its Javadoc for detailed information):

* An instance of `ResourceBundleStringMessages`, that contains the string messages used by the data mining bundle
* An iterable of `DataSourceProviders`, that are used to get the data source for queries
* An iterable of `DataRetrieverChainDefinitions`, that are used by the bundle (see [Building and running Queries](#Building-and-running-Queries))
* An iterable of `Classes`, that provide the functions of the bundle (see [Data Mining Functions](#Data-Mining-Functions))

To register/deregister a data mining bundle, you just have to register/deregister its `DataMiningBundleService` to the OSGi-Context. An easy way to achieve this, is to extend the `AbstractDataMiningActivator` with the activator of the data mining bundle. This handles the registration/deregistration in its start/stop-method and implements the `DataMiningBundleService`, which forces the concrete activator to implement the component providing methods.

### String Messages

A data mining bundle needs string messages, that are created server-side (for example to internationalize the display name of a function). To create string messages for the bundle do the steps described in [Typical Data Mining Scenarios](/wiki/info/landscape/typical-data-mining-scenarios#Adding-a-new-Data-Mining-Bundle).

The provided `ResourceBundleStringMessages` will be added to the string messages of the framework, when the data mining bundle is registered. The framework string messages are an instance of `CompoundResourceBundleStringMessages`, that logs message keys that occur multiple times with the log level `INFO`.

### Data Source Provider

A `DataSourceProvider` provides an instance of a data source, that is used by queries. A data source can be from any type, for example is the `RacingEventService` used as one. Your own `DataSourceProviders` should extend `AbstractDataSourceProvider`. Have a look at `RacingEventServiceProvider` for an example.<br />
These `DataSourceProviders` are used by the framework to create new queries (see `DataMiningServerImpl.createQuery(...)`.

### Client-Server Communication (Data Mining DTOs)

*This site is under construction.*

## Building and running Queries

### Defining and building a Data Retriever Chain

The interface `DataRetrieverChainDefinition<DataSourceType, DataType>` describes the order of retrieval processors ([Processors](#Processors) with the specific functionality to map an `InputType` to a `ResultType`) to get from the `DataSourceType` to the `DataType`. It provides methods to get some information about the retriever chain and to define the order of the processors. There's currently one implementation, which is the `SimpleDataRetrieverChainDefinition<DataSourceType, DataType>`. Every retriever chain has a `UUID` for identification, which is important for the client server communication. The `SimpleDataRetrieverChainDefinition` generates a new random `UUID` upon construction.

The `DataRetrieverChainDefinition<DataSourceType, DataType>` provides three methods to define the order of the retriever chain (for detailed information see its Javadoc):

* The method `startWith` to define the first retrieval processor.
	* The other methods would throw an exception, if this method hasn't been called exactly once.
	* Calling this method more than once causes an exception.
* The method `addAfter` to add a retrieval processor to the end a the list.
	* The parameter `lastAddedRetrieverType` is necessary to ensure, that the `ResultType` and `InputType` of successive retrieval processors match.
* The method `endWith` to complete the retriever chain with the last retrieval processor
	* Completing a retriever chain makes it immutable.
	* This causes all modifying methods to throw an exception, if `endWith` has been called.
	* The parameter `lastAddedRetrieverType` is necessary to ensure, that the `ResultType` and `InputType` of successive retrieval processors match.

This results in the mandatory call order `startWith addAfter* endWith` to define a fresh retriever chain. This strict policy ensures, that the resulting retriever chain instances are type safe and work correctly. The retrieval processors added with this methods have to have a constructor with the signature `(ExecutorService, Collection<Processor<ResultType, ?>>, int)`  or `(ExecutorService, Collection<Processor<ResultType, ?>>, SettingsType, int)`. Otherwise an exception will be thrown. This constructor is used to create concrete instances of the retrieval processors via reflection, when a retriever chain is constructed. All the definition methods (and the constructors of `SimpleDataRetrieverChainDefinition`) have a `String` parameter for a string message key, that would return the name of the retrieval level/chain. This is necessary to display the retriever chain in a human readable way.

The `SimpleDataRetrieverChainDefinition` has a second constructor, where the parameter for the `Class` of the `DataSourceType` is replaced with an `DataRetrieverChainDefinition`. This allows the reuse of existing retriever chain definitions and initializes the new one in the state as if the method `startWith` has been called exactly once (This means, that a call of `startWith` would throw an exception). This is an example for the correct reuse of an existing retriever. See `SailingDataRetrievalChainDefinitions` for examples how to create retriever chain definitions.

Concrete data retriever chain instances can be constructed with a `DataRetrieverChainBuilder`. To get the builder of a **completed** `DataRetrieverChainDefinition` call its `startBuilding` method, which can be done as often as necessary. The interface `DataRetrieverChainBuilder<DataSourceType>` provides methods to iterate the defined retriever chain and to modify the current retrieval processor (for detailed information see its Javadoc).

* The method `stepFurther` iterates over the retriever chain and sets the next retrieval processor as the one to modify.
	* Is similar to the `next` method of `Iterator`.
	* Initializes the builder, which is necessary to call the modifying methods.
	* Throws an exception, if the builder can't step any further.
* The method `canStepFurther` returns `true`, if `stepFurther` can be called at least once more.
	* Is similar to the `hasNext` method of `Iterator`

The builder has to be initialized, to call the modifying methods so that no exception is thrown. This means, that the `stepFurther` method has to be called at least once. This allows a better usage of the builder in loops as described in the construction examples.

All iterating and modifying methods return the builder. This can be used to make multiple calls in a single statement. The method `build` constructs the configured retriever chain until the **current** retrieval processor and returns the **first** processor of the concrete retriever chain instance. This can be useful in some cases, but the standard use case is to configure the retrieval processors, that should be configured and iterate through the builder until `canStepFurther` returns `false` (this results in instances of the complete retriever chain). `build` can be called as often as necessary and it's legal to configure the chain, after `build` has been called. Note that the previously created retriever chain instance won't be affected from these changes.

How the retriever chain builders can be used can be seen in the tests in `TestDataRetrieverChainCreation` and the methods of `QueryFactory`.

### Building a Processor Query

*This section is under construction.*

### Running a Query directly

*This section is under construction.*

### Running a Query with the `DataMiningServer`

*This section is under construction.*

## Data Processing Components

### Processors

Processors are the main component of the framework to process the data and works like the [Pipes and Filter Architecture](http://de.wikipedia.org/wiki/Pipes_und_Filter). The interface `Processor<InputType, ResultType>` describes the functionality of processors, which combines pipes and filters. It has methods to process given elements or to react to a thrown failure and also methods to control the work-flow (for example to finish or abort the work), but it doesn't have methods to get the result of the data processing. The results of processors are forwarded to something, that depends on the concrete implementation (this will be other processors in most cases). For detailed information see the Javadoc of `Processor`.

How this functionality is implemented, depends on the concrete implementation, but it should stick to this description, except for some special cases (like the last processor in a chain). New processors should extend `AbstractParallelProcessor`. This abstract implementation implements most of the functionality defined by `Processor`, with a parallel processing of the input elements. The parallelization is done in the method `processElement`:

* Creates an `AbstractProcessorInstruction` for the given input element, if it's valid.
	* An invalid input element is for example `null`.
* The instruction is given to an `ExecutorService`, if it's valid.
	* An invalid instruction is for example `null`.
	* See [Processor Instructions](#Processor-Instructions) for detailed information about instructions.
* The instruction forwards its result to the result receivers, after it has been executed.
* The execution of instructions is handled by the `ExecutorService` of the processor.

For detailed information see the implementation of `AbstractParallelProcessor`.

An useful method for concrete processors is the protected method `createInvalidResult`, that returns an instruction, that won't be passed to the `ExecutorService`. This can be used to stop the processing of a specific input element (for example to filter the data).

There are useful implementations of the abstract processors, that are more specialized. These special processors implement the creation of instructions and the setting of the additional data for their special case. They can be found in the package `com.sap.sse.datamining.impl.components`.

#### Retrieval Processor

Retrieval processors are used to get many result elements (as `OutputType`) from one input element (as `InputType`). The abstract class `AbstractRetrievalProcessor<InputType, OutputType>` should be used to implement concrete retrieval processors. It has the abstract method `retrieveData`, that has to implement the concrete algorithm to get the result elements from the input element. This method is used by the created instructions, which forwards each retrieved data element to the result receivers. The instruction itself returns an invalid result (created with the method `createInvalidResult`).

#### Filtering Processor

Filtering processors are used to filter the input elements. The class `ParallelFilteringProcessor<InputType>` is a concrete processor, that checks if a given input element matches a `FilterCriterion`. If yes, the given input element is returned as result and if not an invalid result (created with the method `createInvalidResult`) is returned. See [Filter Criteria](#Filter-Criteria) for detailed information about possible filters.

#### Grouping Processor

Grouping processors are used to classify the data elements. The class `ParallelMultiDimensionsValueNestingGroupingProcessor<DataType>` is a concrete processor, that creates a compound `GroupKey` for the dimension values of a collection of [Dimensions](#Dimensions) for a data element. The calculation of this group key is done in the processor instruction and the type of the results is `GroupedDataEntry<DataType>`. The resulting group key is a nesting of the single dimension values. So if the dimension values for a data element would be

* Regatta Name: `Kieler Woche 2014`
* Race Name: `505 Race 1`
* Leg Type: `UPWIND`

And the dimensions to group by would be the list `[Regatta Name, Race Name, Leg Type]`, then the resulting group key would be `Kieler Woche 2014 (505 Race 1 (UPWIND))`. The `Functions`, that are given to the grouping processor have to meet some conditions or an exception will be thrown upon construction:

* The given iterable mustn't be `null`.
* The given iterable mustn't be empty.
* Every `Function` has to be a dimension.

There's also the abstract class `AbstractParallelMultiDimensionalNestingGroupingProcessor<DataType>` (that is implemented by the dimensions value processor), that implements the functionality to build the nested key out of a collection of dimensions. It has the abstract method `createGroupKeyFor`, that has to implement the concrete key creation for the data element for a single dimension.

#### Extraction Processor

Extraction processors are used to get key figures of a grouped Fact. The class `ParallelGroupedElementsValueExtractionProcessor<DataType, FunctionReturnType>` is a concrete processor, that takes `GroupedDataEntry<DataType>` as input elements and has `GroupedDataEntry<FunctionReturnType>` as result elements. It needs a [Statistic Function](#Statistics) to extract the statistic value from the data elements, which is done in its instructions. If the statistic value is `null`, then an invalid result (created with the method `createInvalidResult`) is returned. If the value is something else, then the statistic value grouped by the key of the data element is returned.

### Aggregators

Aggregators are special processors, that are used to aggregate a collection of input elements (e.g. grouped statistic values) to a single result element (e.g. grouped statistic aggregate). They are located in the package `com.sap.sse.datamining.impl.components.aggregators`. There are multiple abstract aggregation processors, that can be used to implement specific aggregators:

* The `AbstractParallelAggregationProcessor<InputType, AggregatedType>` is the most abstract aggregator and is the base class for any other (abstract or not) aggregator. There are no restrictions for the generic parameters. `InputType` and `AggregatedType` can be anything.
	* This processor creates instructions, that perform the following steps:
		1. Handle the input element (this has to be implemented from the concrete aggregators) in a **synchronized** block
		2. Return an invalid result
	* This means, that the concrete implementations don't have to care about concurrency, because of the synchronization done by the most abstract aggregator.
	* The result of the aggregation is computed and forwarded, when no more input is expected (more specific in the `finish` method).
* The only class, that extends the previous aggregator is the `AbstractParallel`**`GroupedData`**`AggregationProcessor<ExtractedType, AggregatedType>`, that does noting but adding a restriction to the generic parameters. The resulting `InputType` of this processor is `GroupedDataEntry<ExtractedType>` and the resulting `ResultType` is `Map<GroupKey, AggregatedType>`. This means, that this kind of processor expects grouped values and calculates an aggregation for all elements with the same group key. This class has two abstract subclasses (and several concrete aggregators):
	* The `AbstractParallelGroupedData`**`Storing`**`AggregationProcessor<InputType, AggregatedType>` refines the names of the abstract methods to fit the specific purpose, that every input element is stored by the aggregator and that any calculation is done after every element is known. This is used by aggregators, that can't calculate the aggregation incrementally with each input element. For example to calculate the median.
	* The `AbstractParallel`**`SingleGroupedValue`**`AggregationProcessor<ValueType>` takes a single input element (per key), that meets a specific condition compared to the other input elements (for the key). These values are returned as aggregation. This is used to calculate the minimum or the maximum.

There are several concrete aggregators, that are domain independent and will be available in every domain specific data mining bundle. For example to count the amount of elements with the same key or to calculate sum, average, median, minimum or maximum of numeric values. If you have to implement a new aggregator keep the following things in mind:

* Where should the new aggregator be located?
	* If it's domain independent put it in `com.sap.sse.datamining.impl.components.aggregators`.
	* If it's domain specific put it in the corresponding data mining bundle.
* Which abstract class should be used as base for the new aggregator?
	* If the input of the new aggregator aren't grouped data or if it's purpose is very special use `AbstractParallelAggregationProcessor`.
	* If the aggregation can be calculated incrementally with each new input element use `AbstractParallelGroupedDataAggregationProcessor`, otherwise use `AbstractParallelGroupedDataStoringAggregationProcessor`.
	* If the aggregation is of the kind, that a single value is picked from the collection of input elements use `AbstractParallelSingleGroupedValueAggregationProcessor`.
* Where should the new aggregator be registered?
	* Aggregators are registered to the framework with its `AggregationProcessorDefinition`.
		* The current convention is, that every concrete aggregator provides its definition with a static method.
		* Note that the aggregator needs a constructor with the signature `(ExecutorService, Collection<Processor<ResultType, ?>>)` or an exception will be thrown upon the construction of the `AggregationProcessorDefinition`.
	* If the aggregator is located in a domain specific bundle, add its definition to the corresponding method of the `DataMiningBundleService` of this bundle.
	* If the aggregator is located in the domain independent bundle, add its definition to the method `getDefaultAggregationProcessors()` of the `DataMiningFrameworkActivator`.

There's also the special aggregator `ParallelGroupedDataCollectingAsSetProcessor<DataType>`, that returns a `Map<GroupKey, Set<DataType>>` as result element. This aggregator isn't registered to the framework, because it shouldn't be used by the end users. It is used to collect all dimension values of a collection of data elements, which is a very special case.

### Processor Instructions

*This section is under construction.*

Processor instructions are `Runnables` enriched with a priority. The abstract class `AbstractProcessorInstruction<ResultType>` should be used to create new instructions.

### Filter Criteria

*This section is under construction.*

### Facts

In relationaler Umgebung wären es Fakten.

*This section is under construction.*

## Data Mining Functions

Data Mining Functions are used by the [Data Processing Components](#Data-Processing-Components) to get the data from the Facts, that is necessary for the data processing. This can be for the filtration or grouping by dimension values (like the name of the regatta or the leg type) or for the extraction of the statistic values (like the traveled distance or the relative rank). Functions are defined by annotating the methods of Facts and domain types. There are currently three annotations `Connector`, `Dimension` and `Statistic` (located in `com.sap.sse.datamining.annotations`) with different properties and conditions as described below. This makes the system flexible and allows developers to add new or change existing dimensions and statistics very fast. This can also be done during run-time, if the corresponding OSGi-Bundles are hot deployed or refreshed (see [Registration and Deregistration of Data Mining Bundles](#Registration-and-Deregistration-of-Data-Mining-Bundles) for more information).

### Function Registration Process

The function registration process is triggered, when a [DataMiningBundleService is registered to the OSGi-Context](#Registration-and-Deregistration-of-Data-Mining-Bundles). The classes returned by the method `getClassesWithMarkedMethods` of the `DataMiningBundleService` are given to a `FunctionRegistry`. The registry iterates over all public methods (including those declared by the class or interface and those inherited from superclasses and superinterfaces) for each of the given classes and does the following:

* If the method is a valid [Connector](#Connectors), then the method gets converted to a `Function`, which is added to the list of *previous functions*. The return type of the function gets registered to the `FunctionRegistry`.
* If the method is a valid [Dimension](#Dimensions), then the method gets converted to a `Function`, which is registered as dimension. If the list of *previous functions* isn't empty, than the list and the dimension will be saved as `ConcatenatingCompoundFunction`.
* If the method is a valid [Statistic](#Statistics) and no connector in the list of *previous functions* has set the `scanForStatistics` to `false`, then the method gets converted to a `Function`, which is registered as statistic. If the list of *previous functions* isn't empty, than the list and the statistic will be saved as `ConcatenatingCompoundFunction`.

What is a valid connector/dimension/statistic and what isn't is described in the following sections. The implementations of `Functions` are located in the package `com.sap.sse.datamining.impl.functions`.

Currently unsupported is

* Annotating a method with multiple data mining annotations.
	* The annotations `Dimension` and `Statistic` have a higher priority than `Connector`, so a method annotated with `Dimension` and `Connector` will be handled as `Dimensions`.
* Cycles in the annotation graph.
	* For example, if `A` has the connector `B foo()` and `B` has the connector `A bar()`.
	* This would result in an infinite loop during the registration process, that should lead to a `StackOverflowError`.

### Compound Functions

Compound functions provide the `Function<ReturnType>` interface for a list of functions. There is currently only the `ConcatenatingCompoundFunction`, that capsules a list of functions, that are called one after another (like the java statement `foo().bar().value();`). It's necessary to handle some of the methods declared in the `Function` interface in a special way, which is described here.

* The methods `getDeclaringType` and `getParameters` return the corresponding value of the **first** function in the list.
* The methods `getReturnType`, `getResultUnit`, `getResultDecimals` and `isDimension` return the corresponding value of the **last** function in the list.
* The `tryToInvoke` methods invoke the first function in the list with the given instance and all other functions with the return value of the previous function.
	* It returns `null`, if any of the functions in the list returned `null`.
* The method `getOrdinal` returns the smallest ordinal of the functions in the list.
* The method `getSimpleName` returns the concatenated simple names of the functions in the list separated by `->`.
* The method `isLocalizable` returns `true`, if any of the functions in the list is localizable.
* The method `getLocalizedName` returns the name of the function, if the name has been set, the simple name, if the compound function isn't localizable or the concatenated simple names of the functions in the list separated by a single space.

### Connectors

Methods marked as `Connector` indicate, that the return type of the annotated method contains marked methods. The method has to match the following conditions, to be registered by the framework:

* It has no parameters.
* The return type isn't `void`.
* It should be side effect free.
	* This isn't checked by the framework, so methods with side effects will be registered, but this could have strange effects on the data mining.

Connectors can have the following properties:

* A `String messageKey` with the default `""`, that is used for the internationalization.
* An `int ordinal` with the default `Integer.MAX_VALUE`, that is used for the sorting of functions (the standard sorting is ascending).
* A `boolean scanForStatistics` with the default `true`, that indicates the framework, if the statistics contained by the return type of the marked method should be registered.
	* This is useful for connections to *higher level* Facts, to be able to use their dimensions, without the registration of unwanted or even wrong statistics.

How the presence of multiple `messageKeys` and `ordinals` in compound functions work is described in [Compound Functions](#Compound-Functions).

### Dimensions

Methods marked with the annotation `Dimension` will be registered as dimension by the framework. The method will be called via reflection, if the dimension value of a Fact is requested (for example to [filter](#Filtering-Processor) or [group](#Grouping-Processor) the data). The method has to match the following conditions, to be registered by the framework:

* It has no parameters, except if the parameter list is exactly `Locale, ResourceBundleStringMessages`.
* The return type isn't `void`.
* It should be side effect free.
	* This isn't checked by the framework, so methods with side effects will be registered, but this could have strange effects on the data mining.

The return type of the marked method should be:

* A primitive type or wrapper class
* Classes that implement
	* `equals` and `hashCode` or the grouping could become incorrect.
	* `toString` or the result presentation will be unreadable.

Dimensions have the following properties:

* The mandatory `String messageKey`, that is used for internationalization.
* An `int ordinal` with the default `Integer.MAX_VALUE`, that is used for the sorting of functions (the standard sorting is ascending).

How the presence of multiple `messageKeys` and `ordinals` in compound functions work is described in [Compound Functions](#Compound-Functions).

### Statistics

Methods marked with the annotation `Statistic` will be registered as computable key figure by the framework. The method will be called via reflection, if the key figure of a Fact is requested (for example for the value [extraction](Extraction-Processor)). The method has to match the following conditions, to be registered by the framework:

* It has no parameters, except if the parameter list is exactly `Locale, ResourceBundleStringMessages`.
* The return type isn't `void`.
* It should be side effect free.
	* This isn't checked by the framework, so methods with side effects will be registered, but this could have strange effects on the data mining.

The return type of the method has to be processable by an [aggregator](#Aggregators) of the data mining framework.

Statistics have the following properties:

* The mandatory `String messageKey`, that is used for the internationalization.
* An `int ordinal` with the default `Integer.MAX_VALUE`, that is used for the sorting of functions (the standard sorting is ascending).
* A `Unit resultUnit` with the default `Unit.None`, that is shown in the result presentation.
	* `Unit` is an enum located in the bundle `com.sap.sse.datamining.shared`.
	* Every member of `Unit` needs a message key in the string messages of the bundle `com.sap.sse.datamining` for internationalization purposes. For example has the unit `Meters` the entry `Meters=m` in the file `StringMessages_en.properties`.
* An `int resultDecimals` with the default `0`, that sets the number of the visible result decimals.

How the presence of multiple `messageKeys` and `ordinals` in compound functions work is described in [Compound Functions](#Compound-Functions).