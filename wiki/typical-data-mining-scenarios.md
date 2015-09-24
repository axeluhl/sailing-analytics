# Typical Data Mining Scenarios

[[_TOC_]]

## Adding a new Data Mining Bundle

The following points describe the steps to create a new data mining bundle. An example for a data mining bundle is `com.sap.sailing.datamining`.

* Add a new java project bundle according to the [Typical Development Scenarios](/wiki/typical-development-scenarios#Adding-a-Java-Project-Bundle)
* Add the necessary dependencies to the `MANIFEST.MF`
	* `com.sap.sse` (for the string messages)
    * `com.sap.sse.datamining`
    * `com.sap.sse.datamining.shared`
    * `com.sap.sse.datamining.annotations`
* Create an `Activator` for the new data mining bundle and provide a `DataMiningBundleService` to the OSGi-Context (see [Registration and Deregistration of Data Mining Bundles](data-mining-architecture#Registration-and-Deregistration-of-Data-Mining-Bundles) for detailed information)
	* This can be done by extending the `AbstractDataMiningActivator` and implementing it's abstract methods. (Recommended)
	* Or by implementing your own `DataMiningBundleService` and register it as OSGi-Service
* Prepare the string messages
	* Create the new source folder `resources`
	* Add the package `<string messages package name>` to `resources`
	* Create empty properties files for each supported language with the name scheme `<string messages base name>_<locale acronym>`
		* Which languages are supported can be found in `ResourceBundleStringMessages.Util.initializeSupportedLocales()`
	* Create a `ResourceBundleStringMessages` in the `DataMiningBundleService` of the new data mining bundle
		* The `ResourceBundleStringMessages` needs the resource base name, which should be `<string messages package name>/<string messages base name>`
		* A reference to this `ResourceBundleStringMessages` should be returned by the method `getStringMessages()`
* **[Optional]** It may be necessary to implement GWT specific components for the new data mining bundle, like DTOs or a [Serialization Dummy](typical-data-mining-scenarios#Include-a-Type-forcefully-in-the-GWT-Serialization-Policy).
	* This should be done in a separate bundle.
	* See [Typical Development Scenarios](/wiki/typical-development-scenarios#Adding-a-Java-Project-Bundle) for instructions, how to add a shared GWT bundle.

## Implementing the Data Mining Components

Before you start to implement the components, you should get an overview about the data mining process that is shown in the image below.

<img style="float: right" src="/wiki/images/datamining/AbstractQueryProcess.svg" />

The data mining process consists of different steps that will be described below. Elements that are **bold** are the components you'll have to implement. The project `com.sap.sailing.dataming` is a fully implemented data mining bundle, where you can see the concrete implementation of the different components.

1. At first a **DataSourceProvider** is necessary that is used to get a *Data Source* of a specific type.
	* This Data Source is the starting point for the data you want to analyze.
	* A concrete Data Source is for example the `RacingEventService`.
	* A concrete DataSourceProvider would be the `RacingEventServiceProvider`.
2. The Data Source is given to a **Retriever** that retrieves multiple **Facts** (of the same type) from it.
	* A Fact can be roughly described as an enriched and abstracted domain element (for example a `TrackedRace`).
	* Enriched means that the Fact provides contextual information and Key Figures in a way that they can be easily used by in the data mining process.
		* Contextual information could be the name of the race, the regatta of the race or the course area on which the race took place.
		* Key Figures could be the speed at start for a specific competitor or the number of jibes that have been performed during the race.
	* Abstracted means, that the Fact provides a user friendly view of the domain element(s) it represents and isn't just a domain element with a richer interface.
		* For example need the previously described Key Figures not only information about the race, but also about a specific competitor. Therefore a Fact that provides such Key Figures abstracts a tuple of race and competitor.
3. The Facts are given (one by one) to a Grouper that provides each Fact with a group key. This key is used to categorize the Fact.
	* The key consists of a single or multiple contextual information. **Dimensions** are used to get the concrete values.
		* If you want to analyze the number jibes of each race in a set of races the dimension that provides the name of a race would be used to group each race.
	* A key can contain multiple dimensional values. This results in a key, that nests the values according to the order of the used dimensions.
		* For example if you want to do the previous analysis not only for each race, but for each leg per race and you have races of multiple regattas. In this case dimensions that provide the information about the regatta name, the race name and the leg number would be necessary.
		* The resulting group key would have the structure `<Regatta Name<Race Name<Leg Number>>>`.
	* The output of a Grouper is a tuple of the group key and the Fact.
4. The [Key, Fact]-Tuple is given to an Extractor that extracts the Key Figure from the Fact.
	* An **Extration Function** is used to get the concrete value from the Fact.
	* The output of an Extractor is a tuple of the group key and the extracted value.
5. The [Key, Value]-Tuple is given to an **Aggregator** that calculates an aggregate for each group key from the given values with this group key.
	* Examples for aggregates would be the sum, average or maximum of the given values.
	* An example would be the analysis of the sum of jibes performed in multiple regattas. Therefore the dimension for the regatta name, the extraction function for the number of jibes performed in a race and an Aggregator that calculates the sum would be used.
	* The result would be a map that maps each group key to the sum of jibes.
	* In this example the type of the extracted value and the type of the aggregated value would be the same, but this doesn't have to be the case.

The main task to integrate the data mining bundle is to define the Fact (with its dimensions and extraction functions) that should be analyzed and to implement the data retrieval to get the facts from the Data Source. The second task would be to implement domain specific Aggregators that are able to process the type of the Key Figure. This may not necessary, because there are some domain independent Aggregators (for example for numeric values) that are available to every domain specific data mining bundle. So it isn't necessary to implement an Aggregator in the bundle, if the type of the Key Figure is a `Number`. See the package `com.sap.sse.datamining.impl.components.aggregators` for the available domain independent aggregators.

The following sections describe how to implement the different components.

Note that the process described above is a very specific process to calculate aggregations from a collection of Facts (in the code referred as Statistic Query) and that the data mining framework is capable to execute many other processes. For general and more detailed information about the framework and its architecture see [Data Mining Architecture](data-mining-architecture).

### Implement the Fact and the Data Retrieval

The concrete interfaces and classes that are needed to implement the Fact depend on what you want to analyze in the new data mining bundle. A good starting point is to think about the domain elements that contain the data you want to analyze, how to get the instances of these domain elements.

* For example if you want to analyze the traveled distance and the speed of a competitor, the `TrackedLegOfCompetitor` would be the domain element of your choice.
* This class is located in the sailing domain, so the entrypoint to get instances would be the `RacingEventService`, which will be the Data Source.
* There are multiple ways to get `TrackedLegOfCompetitors` from the `RacingEventService`.
	* For example `LeaderboardGroup` &#8594; `Leaderboard` &#8594; `RaceColumn` &#8594; `TrackedRace` &#8594; `TrackedLeg` &#8594; `TrackedLegOfCompetitor`
	* Or `Regatta` &#8594; `Series` &#8594; `RaceColumn` &#8594; `TrackedRace` &#8594; `TrackedLeg` &#8594; `TrackedLegOfCompetitor`

But before the data retrieval is implemented, it's worth to have a more detailed look on the process that performs the retrieval.

<img style="float: right" src="/wiki/images/datamining/DataRetrievalInDetail.svg" />

The data retrieval process can be separated in multiple levels. Each level is performed by its own Retriever that produces multiple intermediate elements from the the given element (Data Source or intermediate element) until the last retriever produces the Facts. After every Retriever is a Filter that has a Filter Criterion, which is used to check, if the given meets a condition.

* The standard criterion that is used, checks if the dimensional value of the given element (provided via a Dimension) matches a specific value.
* For example if you want to analyze races of a specific regatta, but a retriever would provide races of many regattas the Filter can be used to exclude the unwanted races.
* Filter Criteria can also be defined in a more general way. See the [Data Mining Architecture](/wiki/data-mining-architecture#Filter-Criteria) for detailed information.

Splitting the data retrieval into multiple levels provides several benefits for the whole data mining process:

* The possibility to filter the elements after each retriever level improves the performance, because less elements have to be retrieved by the following Retrievers.
* It's possible to reuse Retrievers for the data retrieval process of other Facts. This reduces future implementation effort.

To split the data retrieval in multiple levels, it is necessary to implement the corresponding intermediate elements. These elements are very similar to the Fact (there aren't any differences from an objective point of view) so it's possible to reuse these intermediate elements to implement new Facts. But in the context of defining a concrete new Fact, their types will be referred to as Intermediate Types to distinguish them from the Fact.

How to implement a new Fact with supporting Intermediate Types will be described in the following section.

#### Implement the Fact and the Intermediate Types

#### Implement the Retrievers and put them together

### Implement domain specific Aggregators

It may be necessary, that your data mining bundle needs domain specific [aggregator](/wiki/data-mining-architecture#Aggregators). For example to compute the aggregations for domain specific Key Figures (like `Distance`), that can't be done by the domain independent aggregators (located in `com.sap.sse.datamining.impl.components.aggregators`). To do this perform the following steps:

* Implement your own aggregator according to the advice in [Data Mining Architecture](/wiki/data-mining-architecture#Aggregators).
* Provide a `AggregationProcessorDefinition` for the new aggregator in the corresponding method of the `DataMiningBundleService` of your data mining bundle.
	* The current convention is, that every concrete aggregator provides its definition with a static method.
	* Note that the aggregator needs a constructor with the signature `(ExecutorService, Collection<Processor<ResultType, ?>>)` or an exception will be thrown upon the construction of the `AggregationProcessorDefinition`.

For example see the `Activator` of the `com.sap.sailing.datamining` bundle and its aggregators in the package `com.sap.sailing.datamining.impl.components.aggregators`.

## Include a Type forcefully in the GWT Serialization Policy

It can happen, that a necessary type isn't included in the GWT Serialization Policy, because of the generality of the framework. To enforce the include of a type, the GWT-Service has to use the type in one of its methods. To do this, follow these steps:

* Create a dummy class in a shared bundle, if such a dummy class doesn't exist already in the scope of the type.
	* For example like `SSEDataMiningSerializationDummy`.
	* The class has to implement the interface `SerializationDummy` and should only have a private standard constructor, to prevent an accidental instantiation.
	* Implementing this interface adds the dummy automatically to the serialization policy, without changing the `DataMiningService`.
* Add a private non-final instance variable of the type to the dummy.

The type will be included in the GWT Serialization Policy, because the `DataMiningService` has a method, that uses the interface `SerializationDummy`, that uses the type.

If you can't implement `SerializationDummy` for any reasons do the following:

* Implement the interface `Serializable` or `IsSerializable` instead.
* Add a pseudo method to the GWT-Service, that uses the dummy class.
	* For example like `SerializationDummy pseudoMethodSoThatSomeClassesAreAddedToTheGWTSerializationPolicy()`.

## Deprecated Documentation

A data type is an atomic unit on which the processing components operate. A data type is normally based on a domain element (like `GPSFixMoving` or `TrackedLegOfCompetitor`) and provides contextual information about it (like the name of the regatta or race) via [Functions](data-mining-architecture#Data-Mining-Functions). *Dimensions and Statistics*

### Create the necessary Data Types

The following points describe the steps to create a data type, that is based on `TrackedLegOfCompetitor`. How this has been done in the data mining bundle `com.sap.sailing.datamining` can be seen in the packages `com.sap.sailing.datamining.data` and `com.sap.sailing.datamining.impl.data`.

* Create an interface for the domain element. The naming convention for data type interfaces is `Has<domain element name>Context`.
	* If `TrackedLegOfCompetitor` would be the domain element, then the name of the data type interface should be `HasTrackedLegOfCompetitorContext`.
* Create a data type interface for each important step from the data source to the domain element
* *Hierbei kann und sollte das Domänenmodell abstrahiert werden.*
	* In the case of the `TrackedLegOfCompetitor` the steps would be
		* `LeaderboardGroup`
		* `Leaderboard`
		* `TrackedRace`
		* `TrackedLeg`
		* `TrackedLegOfCompetitor`
	* This isn't mandatory for the framework to work, but it improves its performance, because it's possible to build a processor chain, that filters after each step.
* Add the data type interfaces to the classes returned by  the method `getClassesWithMarkedMethods()` of the `DataMiningBundleService` of the data mining bundle.
* Add methods to get the contextual information and annotate *Referenz zur Architektur* them, so that they get registered as dimensions/statistics.
	* *Kurze Informationen über die vorhandenen Annotationen*
	* To get the contextual data of a higher level data type interface annotate the getter for this interface with `@Connector(scanForStatistics=false)`.
		* If the data type `HasTrackedLegOfCompetitorContext` should have access to the dimensions of the previous data type (in this case `HasTrackedLegContext`), an annotated method like `HasTrackedLegContext getTrackedLegContext()` is necessary.
	* There's currently no parameter support for functions, except for signatures exactly like `(Locale, ResourceBundleStringMessages)`.
	* Cycles in the graph of annotated methods aren't supported and would result in an infinite loop during the function registration.
	* See [Data Mining Functions](data-mining-architecture#Data-Mining-Functions) for detailed information about the annotations.
* Create implementations for the data type interfaces. The naming convention for data type classes is `<domain element name>WithContext`.
	* If `TrackedLegOfCompetitor` would be the domain element, then the name of the data type class should be `TrackedLegOfCompetitorWithContext`.

### Implement the Retrieval Processors and define the Retriever Chain

The following points describe the steps to create components for the data retrieval for the previously created data type. How this has been done in the data mining bundle `com.sap.sailing.datamining` can be seen in the package `com.sap.sailing.datamining.impl.components` and the class `SailingDataRetrievalChainDefinitions`.

* Add a `DataSourceProvider` (see [Data Mining Architecture](/wiki/data-mining-architecture#Data-Source-Provider)) to the `DataMiningBundleService` of the data mining bundle, if it doesn't contain one for the data source of the data type.
* Implement a [retrieval processor](/wiki/data-mining-architecture#Retrieval-Processor) for each data type.
	* Retrieval processors should extend `AbstractRetrievalProcessor`.
	* Retrieval processors used by retriever chains need a constructor with the signature `(ExecutorService, Collection<Processor<ResultType, ?>>, int)`.
	* A retrieval processor describes how to get from the input data type (like `RacingEventService` or `TrackedRace`) to the result data type (like `TrackedLeg`) with the method `retrieveData`.
* Create a `DataRetrieverChainDefinition` and add it to the `DataMiningBundleService` of the data mining bundle (see [Data Mining Architecture](/wiki/data-mining-architecture#Defining-and-building-a-Data-Retriever-Chain)).
	* A retriever chain is created with its methods `startWith`, `addAfter` and `endWith`, that add `Classes` of your retrieval processors to the chain.
	* Each method has a parameter for the `Class` of the retrieved data type of the next step and some also have a parameter for the `Class` of the retrieval processor of the previous step to ensure type safety.
	* Each method has a parameter for a message key, that is needed for the internationalization of the chain.
	* The call order of these methods has to match `startWith addAfter* endWith` or an exception will be thrown.
	* Every retrieval processor needs a constructor with the signature `(ExecutorService, Collection<Processor<ResultType, ?>>, int)` or `(ExecutorService, Collection<Processor<ResultType, ?>>, SettingsType, int)`. Otherwise an exception will be thrown.
		* For an example of a retrieval processor with settings see `PolarGPSFixRetrievalProcessor`.

## Adding a new Data Type with a reusable Retriever Chain

* Create the necessary Data Types according to [this](#Create-the-necessary-Data-Types).
* Implement a retrieval processor for each data type according to the first step of [this](#Implement-the-Retrieval-Processors-and-define-the-Retriever-Chain).
* Split the existing retriever chain and diverge it two retriever chains.
	* The split point should be the last retrieval processor, that is shared by the retriever chains
		* If there would be these two retriever chains
			  <table>
			    <tr><th>Existing</th><th>New</th></tr>
			    <tr><td>`RacingEventService`<br>
			          `LeaderboardGroup`<br>
			          `Leaderboard`<br>
			          `TrackedRace`<br>
			          `TrackedLeg`<br>
			          `TrackedLegOfCompetitor`
	              </td>
	              <td>`RacingEventService`<br>
			          `LeaderboardGroup`<br>
			          `Leaderboard`<br>
			          `TrackedRace`<br>
			          `MarkPassing`
	              </td>
	            </tr>
			  </table>
		  then the existing retriever chain should be splitted after the retrieval processor for the `TrackedRaces`.
	* To split an existing retriever chain, create a new one after the split point with the usage of the existing one. Then move the `addAfter` and `endWith` calls to the new retriever chain. The first part of the split retriever chain can now be used to create the retriever chain for the new data type.
		* Note, that it's not allowed to call `startWith`, if a retriever chain is used to create a new one. 
	* In pseudo code would this look like this:<br>
	  Before:<br>
	    <pre>
RetrieverChain trackedLegRetrieverChain = new RetrieverChain(RacingEventService.class, TrackedLegOfCompetitor.class);
trackedLegRetrieverChain.startWith(LeaderboardGroupRetrievalProcessor.class);
trackedLegRetrieverChain.addAfter(LeaderboardRetrievalProcessor.class);
trackedLegRetrieverChain.addAfter(TrackedRaceRetrievalProcessor.class);
trackedLegRetrieverChain.addAfter(TrackedLegRetrievalProcessor.class);
trackedLegRetrieverChain.endWith(TrackedLegOfCompetitorRetrievalProcessor.class);
</pre>
	  After:
		<pre>
RetrieverChain trackedRaceRetrieverChain = new RetrieverChain(RacingEventService.class, TrackedRace.class);
trackedLegRetrieverChain.startWith(LeaderboardGroupRetrievalProcessor.class);
trackedLegRetrieverChain.addAfter(LeaderboardRetrievalProcessor.class);
trackedLegRetrieverChain.endWith(TrackedRaceRetrievalProcessor.class);
</ br>
RetrieverChain trackedLegRetrieverChain = new RetrieverChain(trackedRaceRetrieverChain, TrackedLegOfCompetitor.class);
trackedLegRetrieverChain.addAfter(TrackedLegRetrievalProcessor.class);
trackedLegRetrieverChain.endWith(TrackedLegOfCompetitorRetrievalProcessor.class);
</ br>
RetrieverChain markPassingRetrieverChain = new RetrieverChain(trackedRaceRetrieverChain, MarkPassing.class);
markPassingRetrieverChain.endWith(MarkPassingRetrievalProcessor.class);
</pre>
* Ensure, that all retriever chains are added to the `DataMiningBundleService`