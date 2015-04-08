# Typical Data Mining Scenarios

[[_TOC_]]

## Adding a new Data Mining Bundle

* Add a new java project bundle according to the [Typical Development Scenarios](wiki/typical-development-scenarios#Adding-a-Java-Project-Bundle)
* Add the necessary dependencies to the `MANIFEST.MF`
	* `com.sap.sse` (for the string messages)
    * `com.sap.sse.datamining`
    * `com.sap.sse.datamining.shared`
* Create an `Activator` for the new data mining bundle and provide a `DataMiningBundleService` to the OSGi-Context (see [Registration and Deregistration of Data Mining Bundles](wiki/data-mining-architecture#Registration-and-Deregistration-of-Data-Mining-Bundles) for detailed information)
	* This can be done by extending the `AbstractDataMiningActivator` and implementing it's abstract methods
	* Or by implementing your own `DataMiningBundleService` and register it as OSGi-Service
* Prepare the string messages
	* Create the new source folder `resources`
	* Add the package `<string messages package name>` to `resources`
	* Create empty properties files for each supported language with the name scheme `<string messages base name>_<locale acronym>`
	* Create a `ResourceBundleStringMessages` in the `DataMiningBundleService`
		* The `ResourceBundleStringMessages` needs the resource base name, which should be `<string messages package name>/<string messages base name>`
		* A reference to this `ResourceBundleStringMessages` should be returned by the method `getStringMessages()`

## Adding a completely new Data Type

A data type is an elementar unit on which the processing components operate. A data type is normally based on a domain element (like `GPSFixMoving` or `TrackedLegOfCompetitor`) and provides contextual information about the domain element (like the name of the regatta or race) via [Functions](wiki/data-mining-architecture#Data-Mining-Functions).

### Create the necessary Data Types

* Create an interface for the domain element. The naming convention for data type interfaces is `Has<domain element name>Context`.
	* If `TrackedLegOfCompetitor` would be the domain element, then the name of the data type interface should be `HasTrackedLegOfCompetitorContext`.
* Create a data type interface for the steps from the data source to the domain element
	* In the case of the `TrackedLegOfCompetitor` the steps would be
		* `RacingEventService`
		* `LeaderboardGroup`
		* `Leaderboard`
		* `TrackedRace`
		* `TrackedLeg`
		* `TrackedLegOfCompetitor`
	* This isn't mandatory for the framework to work, but it improves its performance, because it's possible to build a processor chain, that filters after each step.
* Add the data type interfaces to the `Classes` return by  the method `getClassesWithMarkedMethods()` of the `DataMiningBundleService`.
* Add methods to get the contextual information and annotate them, so that they get registered as functions.
	* To get the contextual data of a higher level data type interface annotate the getter for this interface with `@Connector(scanForStatistics=false)`
	* There's currently no parameter support for functions, except for signatures exactly like `(Locale, ResourceBundleStringMessages)`.
	* Cycles in the graph of annotated methods aren't supported and would result in an infinite loop during the function registration.
	* See [Data Mining Functions](wiki/data-mining-architecture#Data-Mining-Functions) for detailed information about the annotations.
* Implement the data type interfaces. The naming convention for data type classes is `<domain element name>WithContext`.
	* If `TrackedLegOfCompetitor` would be the domain element, then the name of the data type class should be `TrackedLegOfCompetitorWithContext`.

### Implement the Retrieval Processors and define the Retriever Chain

* Add a `DataSourceProvider` to the `DataMiningBundleService`, if it doesn't contain one for the data source of the data type.
* Implement a retrieval processor for each data type.
	* Retrieval processors should extend `AbstractSimpleRetrievalProcessor`
	* Retrieval processors used by retriever chains need a constructor with the signature `(ExecutorService, Collection<Processor<ResultType, ?>>)`
	* A retrieval processor describes how to get from the input data type (like `RacingEventService` or `TrackedRace`) to the result data type (like `TrackedLeg`) with the method `retrieveData`.
* Create a `DataRetrieverChainDefinition` and add it to the `DataMiningBundleService`
	* A retriever chain is created with its methods `startWith`, `addAfter` and `endWith`, that add `Classes` of your retrieval processors to the chain.
	* Each method has a parameter for the `Class` of the retrieved data type of the next step and some also have a parameter for the `Class` of the retrieval processor of the previous step to ensure type safety.
	* Each method has a parameter for a message key, that is needed for the internationalization of the chain
	* The call order of these methods has to match `startWith addAfter* endWith` or an exception will be thrown.
	* Every retrieval processor needs a constructor with the signature `(ExecutorService, Collection<Processor<ResultType, ?>>)` or an exception will be thrown
	* See [Defining and building a Data Retriever Chain](wiki/data-mining-architecture#Defining-and-building-a-Data-Retriever-Chain) for examples and more detailed information.