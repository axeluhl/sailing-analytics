# Typical Data Mining Scenarios

[[_TOC_]]

## Adding a new Data Mining Bundle

The following points describe the steps to create a new data mining bundle. An example for a data mining bundle is `com.sap.sailing.datamining`.

* Add a new java project bundle according to the [Typical Development Scenarios](/wiki/info/landscape/typical-development-scenarios#Adding-a-Java-Project-Bundle)
* Add the necessary dependencies to the `MANIFEST.MF`
	* `com.sap.sse` (for the string messages)
    * `com.sap.sse.datamining`
    * `com.sap.sse.datamining.shared`
    * `com.sap.sse.datamining.annotations`
* Create an `Activator` for the new data mining bundle and provide a `DataMiningBundleService` to the OSGi-Context (see [Registration and Deregistration of Data Mining Bundles](/wiki/info/landscape/data-mining-architecture#Registration-and-Deregistration-of-Data-Mining-Bundles) for detailed information)
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

The main task to integrate the data mining bundle is to define the Fact (with its dimensions and extraction functions) that should be analyzed and to implement the data retrieval to get the facts from the Data Source. A second task could be to implement domain specific Aggregators that are able to process the type of the Key Figure. This may not necessary, because there are some domain independent Aggregators (for example for numeric values) that are available to every domain specific data mining bundle. So it isn't necessary to implement an Aggregator in the bundle, if the type of the Key Figure is a `Number`. See the package `com.sap.sse.datamining.impl.components.aggregators` for the available domain independent aggregators.

The following sections describe how to implement the different components.

Note that the process described above is a very specific process to calculate aggregations from a collection of Facts (in the code referred as Statistic Query) and that the data mining framework is capable to execute many other processes. For general and more detailed information about the framework and its architecture see [Data Mining Architecture](/wiki/info/landscape/data-mining-architecture).

### Implement the Fact and the Data Retrieval

The concrete interfaces and classes that are needed to implement the Fact depend on what you want to analyze in the new data mining bundle. A good starting point is to think about the domain elements that contain the data you want to analyze and how to get the instances of these domain elements.

* For example if you want to analyze the traveled distance and the speed of a competitor, the `TrackedLegOfCompetitor` would be the domain element of your choice.
* This class is located in the sailing domain, so the entrypoint to get instances would be the `RacingEventService`, which will be the Data Source.
* There are multiple ways to get `TrackedLegOfCompetitors` from the `RacingEventService`.
	* For example `LeaderboardGroup` &#8594; `Leaderboard` &#8594; `RaceColumn` &#8594; `TrackedRace` &#8594; `TrackedLeg` &#8594; `TrackedLegOfCompetitor`
	* Or `Regatta` &#8594; `Series` &#8594; `RaceColumnInSeries` &#8594; `TrackedRace` &#8594; `TrackedLeg` &#8594; `TrackedLegOfCompetitor`

But before the data retrieval is implemented, it's worth to have a more detailed look on the process that performs the retrieval.

<img style="float: right" src="/wiki/images/datamining/DataRetrievalInDetail.svg" />

The data retrieval process can be separated in multiple levels. Each level is performed by its own Retriever that produces multiple intermediate elements from the the given element (Data Source or intermediate element) until the last retriever produces the Facts. After every Retriever is a Filter that has a Filter Criterion, which is used to check, if the given meets a condition.

* The standard criterion that is used, checks if the dimensional value of the given element (provided via a Dimension) matches a specific value.
* For example if you want to analyze races of a specific regatta, but a retriever would provide races of many regattas the Filter can be used to exclude the unwanted races.
* Filter Criteria can also be defined in a more general way. See the [Data Mining Architecture](/wiki/info/landscape/data-mining-architecture#Filter-Criteria) for detailed information.

Splitting the data retrieval into multiple levels provides several benefits for the whole data mining process:

* The possibility to filter the elements after each retriever level improves the performance, because less elements have to be retrieved by the following Retrievers.
* It's possible to reuse Retrievers for the data retrieval process of other Facts. This reduces future implementation effort.

To split the data retrieval in multiple levels, it is necessary to implement the corresponding intermediate elements. These elements are very similar to the Fact (there aren't any differences from an objective point of view) so it's possible to reuse these intermediate elements to implement new Facts. But in the context of defining a concrete new Fact, their types will be referred to as Intermediate Types to distinguish them from the Fact.

How to implement a new Fact with supporting Intermediate Types and the Retrievers will be described in the following sections.

#### Implement the Fact and the Intermediate Types

As stated before can a Fact be roughly described as an enriched and abstracted domain element (for example a `TrackedLegOfCompetitor`).

* Enriched means that the Fact provides contextual information and Key Figures in a way that they can be easily used by in the data mining process.
	* Contextual information could be the name of the race, the regatta of the race or the course area on which the race took place.
	* Key Figures could be the speed at start for a specific competitor or the number of jibes that have been performed during the race.
* Abstracted means, that the Fact provides a user friendly view of the domain element(s) it represents and isn't just a domain element with a richer interface.
	* For example need the previously described Key Figures not only information about the race, but also about a specific competitor. Therefore a Fact that provides such Key Figures abstracts a tuple of race and competitor.

What the implementation of the Fact will be depends on the data you want to analyze and the domain elements that contain the data. For example if you want to analyze the traveled distance and the speed of a competitor, the `TrackedLegOfCompetitor` would be a good domain element on which the Fact could be based. The next step is to choose the path (steps from domain element to domain element) to get the desired domain element. There will be multiple paths in most cases. Paths to `TrackedLegOfCompetitor` could be:

* `RacingEventService` &#8594; `LeaderboardGroup` &#8594; `Leaderboard` &#8594; `RaceColumn` &#8594; `TrackedRace` &#8594; `TrackedLeg` &#8594; `TrackedLegOfCompetitor`
* `RacingEventService` &#8594; `Regatta` &#8594; `Series` &#8594; `RaceColumnInSeries` &#8594; `TrackedRace` &#8594; `TrackedLeg` &#8594; `TrackedLegOfCompetitor`

If there are multiple paths you should choose the one that contains more *user friendly* domain elements. In this case that would be the second path, because `Regatta` is more familiar by sailors and viewers than `LeaderboardGroup`.

The next step is to choose the Intermediate Types to implement. These should contain useful contextual information (that will be provided via Dimensions) and should be *user friendly*, because they (and their Dimensions) will be displayed. Orient yourself on the domain elements in the path to choose the Intermediate Types. Good Intermediate Types for the example path would be `Regatta`, `TrackedRace` and `TrackedLeg`, while `RacingEventService` and `RaceColumnInSeries` aren't a good choice. `Series` could also be used, but will be excluded to keep the example more simple.

The next step is to implement the chosen Intermediate Types and the Fact. Below is a simplified class diagram of the implementation of the example.

<img style="float: right" src="/wiki/images/datamining/ExampleFactStructure.svg" />

Note that the Fact and Intermediate Types are separated in an Interface and a Class. This is done because the Dimensions and Extraction Functions are provided by annotating the methods of the Fact and Intermediate Types. Separating the place where the methods are annotated (Interface) and the concrete implementation (Class) keeps the class structure cleaner.

* Implementing the Interfaces for the Fact and the Intermediate Types.
	* Create the Interfaces in the data mining bundle you want to edit.
	* The current naming convention for such Interfaces is `Has<Name>Context`. If the Fact/Intermediate Type is close to the domain element, could the name be equal to the name of the domain element. For example the name of the Fact that represents `TrackedLegOfCompetitor` would be `HasTrackedLegOfCompetitorContext`.
	* Add the methods that provide the contextual information
		* Such methods are called Dimensions and have to be marked with the [Annotation `@Dimension`](/wiki/info/landscape/data-mining-architecture#Dimensions).
		* These methods should return values that
			* are a primitive type or wrapper class
			* or classes that implement `equals()`, `hashCode()` and `toString()`. Otherwhise the grouping could fail and the result presentation will be unreadable.
		* Contextual information of the example could be the regatta name, the race name, the year of the race, the leg type/number, the competitor name/nationality and many more.
		* There are two ways to provide the contextual information:
			1. **Direct Dimension**
				* This means, that the annotated method returns the value that is the contextual information.
				* This is useful, if the method of the domain element needs parameters, because the data mining framework currently doesn't support parameters.
				* The Interface methods are directly annotated with `@Dimenson`. Short information about this annotation are listed below.
				* Direct Dimensions of the example are `getYear()` of `HasTrackedRaceContext` and, `getLegType()` and `getLegNumber()` of  `HasTrackedLegContext`.
			2. **Indirect Dimension**
				* This means, that the annotated returns a type that again contains annotated methods.
				* This prevents code duplication, because types and methods that are already annotated can be reused.
				* The Interface methods are annotated with `@Connector`. Short information about this annotation are listed below.
				* Indirect Dimensions of the example are `getRegatta()` of `HasRegattaContext` and `getCompetitor` of `HasTrackedLegOfCompetitorContext`.
					* `getRegatta()` returns a `Regatta` that implements `Named` that has the method `getName()`, which is annotated as Dimension.
					* `getCompetitor` returns a `Competitor` that implements `Named` and contains multiple annotated methods. For example `getBoat()` that has the method `getSailID()`, which is annotated as Dimension. This results in multiple Dimensions (e.g. the competitor name and sail ID) starting with the method `getCompetitor()`.
				* An inderict Dimension can be seen as a concatenation of method calls. For example `getCompetitor().getBoat().getSailID()`, where `getCompetitor()` and `getBoat()` are annotated with `@Connector` and `getSailID()` is annotated with `@Dimension`.
		* Short information about `@Dimension` and `@Connector`. For detailed information see their Javadoc or [Data Mining Annotations](/wiki/info/landscape/data-mining-architecture#Connectors).
			* **@Dimension**
				* Has the mandatory member `messageKey` that is used for internationalization, which has to be added to the `stringMessages*.properties` of the data mining bundle.
				* And an optional member `ordinal` that is used for the natural order of Dimensions.
			* **@Connector**
				* Has the optional members `messageKey` and `ordinal`, that work like the corresponding members of `@Dimension`. See [Compound Functions](/wiki/info/landscape/data-mining-architecture#Compound-Functions) for information about how these members are used in indirect Dimensions.
			* Methods marked with data mining annotations mustn't have parameters except
				* the first method of an indirect Dimension
				* direct Dimensions
			* can have a paremeter list that is exactly `(Locale, ResourceBundleStringMessages)`.
	* Add the methods that provide the Key Figures (also referred to as Statistic) to the Fact Interface.
		* This works like providing the Dimensions, except that the annotation **@Statistic** is used. Annotating a method like this results in an Extraction Function.
			* `@Statistic` has the members `messageKey` and `ordinal`, that work like the members of `@Dimension`. It also has the optional member `resultDecimals` that is used for the result presentation.
		* Extraction Functions should return values that can be processed by an Aggregator.
			* The framework provides domain independent aggregators for numeric values (like `int` or `double` and their wrapper classes).
			* See [Implement domain specific Aggregators](#Implement-domain-specific-Aggregators) for information about how to implement your own Aggregators.
		* Like Dimension it's possible to define Extraction Functions direct and indirect (again with the `@Connector` annotation).
		* A direct Extraction Function of the example is `getRankGainsOrLosses()`.
		* An indirect Extraction Function could be `getDistanceTraveled()`, if the method `getMeters()` would be annotated with `@Statistic`.
			* This isn't the case, if you have a look at the code of `HasTrackedLegOfCompetitorContext`, because `Distance` is used directly as Key Figure.
	* After you implemented the Fact and Intermediate Types, should the Interfaces look like in the picture above (with the corresponding annotations). The only missing part is the connection between the Fact and Intermediate Types. This is represented by the associations between the Interfaces in the picture.
		* The connection is done by annotating the getter for the corresponding Intermediate Type (e.g. `getRegattaContext()` or `getTrackedRaceContext()`) with `@Connector`.
		* In this case it's important to set `scanForStatistic=false`, to prevent that the Extraction Functions of the overlying type are included to the Extraction Functions of the Fact.
			* Calculating the distance traveld based on GPS-Fixes doesn't make sense, if the Extraction Function is based on `HasTrackedLegOfCompetitorContext`.
			* The annotation should look like `@Connector(scanForStatistic=false)`
		* Connecting the interfaces like this has the effect that the Dimensions of the overlying Intermediate Types can be used, when operating on the Fact
		* This can be seen as if all Dimensions were declared, but with the benefits of a higher performance due to early filtering and reusability of the Intermediate Types.
	* It's necessary to register the Types that contain the Dimensions and Extraction Functions to the data mining framework, so that the framework is able to use them for the data mining process.
		* This is done by adding the Fact and Intermediate Types to the classes returned by `getClassesWithMarkedMethods()` of the `DataMiningBundleService` of the data mining bundle.
	* The last step is to implement the Classes that implement the Interfaces of the Fact and Intermediate Types.
		* The current naming convention for such classes is `<Name>WithContext`. For the Interface `HasTrackedLegOfCompetitorContext` is the corresponding class name `TrackedLegOfCompetitorWithContext`.

The implementation of the data mining bundle isn't finished yet. The next task is to implement the Data Retrieval for the new Fact, which is described in the next section.

#### Implement the Retrievers and put them together

At this point you should have implemented a new Fact and several Intermediate Types in form of an Interface and an implementing Class. In the example these types are:
<table>
  <tr><th>Interface</th><th>Class</th></tr>
  <tr><td><code>HasRegattaContext</code></td><td><code>RegattaWithContext</code></td></tr>
  <tr><td><code>HasTrackedRaceContext</code></td><td><code>TrackedRaceWithContext</code></td></tr>
  <tr><td><code>HasTrackedLegContext</code></td><td><code>TrackedLegWithContext</code></td></tr>
  <tr><td><code>HasTrackedLegOfCompetitorContext</code></td><td><code>TrackedLegOfCompetitorWithContext</code></td></tr>
</table>

For the implementation of the Data Retrieval it's helpful to keep the path to the Fact in mind. The path of the example is:<br>
`RacingEventService` &#8594; `Regatta` &#8594; `Series` &#8594; `RaceColumnInSeries` &#8594; `TrackedRace` &#8594; `TrackedLeg` &#8594; `TrackedLegOfCompetitor`

The Data Retrieval is divided in several steps (see the [Data Retrieval Diagram](#Implement-the-Fact-and-the-Data-Retrieval)), if Intermediate Types are used. Each step is done by a so called Retriever that takes a single input element and produces multiple output elements. The types of the input and output elements is defined by generic type attributes of the Retriever implementation. You have to implement one Retriever per Intermediate Type and Fact. To be more exact, you need one Retrievers that retrieves the first Intermediate Type from the Data Source (in this case the `Racing Event Service`), one Retriever for each transition from an Intermediate Type to the next Intermediate Type and finally one to retrieve the Fact from the last Intermediate Type. The necessary Retrievers for the example are:
<table>
  <tr><th>Retriever</th><th>InputType</th><th></th><th>OutputType</th></tr>
  <tr><td><code>RegattaRetrievalProcessor</code></td><td><code>RacingEventService</code></td><td>&#8594;</td><td><code>HasRegattaContext</code></td></tr>
  <tr><td><code>TrackedRaceRetrievalProcessor</code></td><td><code>HasRegattaContext</code></td><td>&#8594;</td><td><code>HasTrackedRaceContext</code></td></tr>
  <tr><td><code>TrackedLegRetrievalProcessor</code></td><td><code>HasTrackedRaceContext</code></td><td>&#8594;</td><td><code>HasTrackedLegContext</code></td></tr>
  <tr><td><code>TrackedLegOfCompetitorRetrievalProcessor</code></td><td><code>HasTrackedLegContext</code></td><td>&#8594;</td><td><code>HasTrackedLegOfCompetitorContext</code></td></tr>
</table>

The following points should be considered during the Retriever implementation:

* Retrievers should extend `AbstractRetrievalProcessor`.
	* This abstract class encapsulates the management that has to be done during the Data Retrieval.
	* It has the abstract method `Iterable<OutputType> retrieveData(InputType element)` in which the concrete algorithm to retrieve the output elements from the input element has to be implemented.
	* Every Retriever must have a constructor with one of the following parameter lists:
		* `(ExecutorService, Collection<Processor<OutputType, ?>>, int)`
		* `(ExecutorService, Collection<Processor<OutputType, ?>>, <? extends SerializableSettings>, int)`
		* Otherwhise the framework will throw an `IllegalArgumentException` when an instance of the Retriever is constructed.
	* For general information about Processors see [Data Mining Architecture](/wiki/info/landscape/data-mining-architecture#Processors).

The next step is put the Retrievers together in a `DataRetrieverChainDefinition`. Such a definition is used by the framework to create the Data Retrieval part of the [Data Mining Process](#Implementing-the-Data-Mining-Components). It tells the framework which components are used to retrieve the data and in which order. The following points have to be considered to create a new `DataRetrieverChainDefinition`:

* There are two different ways to create a new `DataRetrieverChainDefinition`:
	* The first creates a completely new one. This is the way to go, if you're creating the first chain for a new data mining bundle or if you added a new Fact that is independent from the other Facts in the data mining bundle (the new Fact doesn't share any Intermediate Types with other Facts). This is described in this section.
	* The other way reuses existing `DataRetrieverChainDefinitions`. This should be used, when you added a Fact that shares Intermediate Types with other Facts of the data mining bundle. This is described [here](#Create-a-new-DataRetrieverChainDefinition-with-existing-ones).
* Concrete instances of a `DataRetrieverChainDefintion` are created with `SimpleDataRetrieverChainDefinition`. This class has two generic type attributes for the type of the Data Source and the type of the retrieved Facts.
	* Its constructor has three parameters. Two for the classes of the generic type attributes and a message key that is used for internationalization. The values of the message key in the `stringMessages*.properties` are shown in the Ui, so they should be user friendly.
* The methods `startWith`, `addAfter` and `endWith` are used to define the order of the used Retrievers.
	* Every method has a message key as parameter that has to meet the same conditions as the message key of the chain.
* The call order of these methods has to match the pattern `startWith addAfter* endWith` or an exception will be thrown.
	* Note that every Retriever needs a constructor that has a parameter list like the ones described above or an `IllegalArgumentException` will be thrown.
* **Don't forget** to to register the new `DataRetrieverChainDefinition` to the data mining framework. This is done by adding the new one to the ones returned by `getDataRetrieverChainDefinitions()` of the `DataMiningBundleService` of the data mining bundle.
* An example for a `DataRetrieverChainDefinition` creation can be found in `SailingDataRetrievalChainDefinitions` in the package `com.sap.sailing.datamining`.
* For detailed information about `DataRetrieverChainDefinition` see its Javadoc or [Data Mining Architecture](/wiki/info/landscape/data-mining-architecture#Defining-and-building-a-Data-Retriever-Chain).

The last step is to implement a `DataSourceProvider`. In most cases you'll be able to reuse existing `DataSourceProviders` (for example the `RacingEventServiceProvider` located in `com.sap.sailing.datamining`), but if you have to implement a new one this is how it's done:

* Create an new class that extends `AbstractDataSourceProvider` and implement its abstract methods.
* Add the new `DataSourceProvider` to the ones returned by `getDataSourceProviders()` of the `DataMiningBundleService` of the data mining bundle.

##### Create a new DataRetrieverChainDefinition with existing ones

It's possible to a whole or a part of an existing `DataRetrieverChainDefinition` to create a new one. This is useful, if you want to add a new Fact that shares Intermediate Types with other Facts of the data mining bundle. For example, if the following Fact and Intermediate Types exist and you want to create a `DataRetrieverChainDefinition` for the new ones:
<table>
	<tr><th></th><th>Existing</th><th>New</th></tr>
	<tr><td><b>DataSource</b></td><td><code>RacingEventService</code></td><td><code>RacingEventService</code></td></tr>
	<tr><td><b>Intermediate Types</b></td>
		<td><code>HasRegattaContext</code><br>
			<code>HasTrackedRaceContext</code><br>
			<code>HasTrackedLegContext</code><br>
		</td>
		<td><code>HasRegattaContext</code><br>
			<code>HasTrackedRaceContext</code><br>
		</td>
	</tr>
	<tr><td><b>Fact</b></td><td><code>HasTrackedLegOfCompetitorContext</code></td><td><code>HasMarkPassingContext</code></td></tr>
</table>

* The first step is to split the existing `DataRetrieverChainDefinition` after the last Intermediate Type it has in common with the new chain. In this case it's the Retriever that retrieves `HasTrackedRaceContext`.
* To split a `DataRetrieverChainDefinition`, create a new one after the split point with the usage of the existing one. Then move the `addAfter` and `endWith` calls to the new retriever chain.
	* To create a new `DataRetrieverChainDefinition` use the constructor of `SimpleDataRetrieverChainDefinition` that takes a `DataRetrieverChainDefinition` as parameter.
	* This constructor creates a `DataRetrieverChainDefinition` with the components of the given one **and initializes the new chain**. This means that it's not allowed to call `startWith` with `DataRetrieverChainDefinitions` created like this. 
* The following is an example in pseudocode. See `SailingDataRetrievalChainDefinitions` in the package `com.sap.sailing.datamining` for the real code:<br>
	**Before:**<br>
	<pre>
RetrieverChain trackedLegOfCompetitorRetrieverChain = new RetrieverChain(RacingEventService.class, HasTrackedLegOfCompetitorContext.class);
trackedLegRetrieverChain.startWith(RegattaRetrievalProcessor.class);
trackedLegRetrieverChain.addAfter(TrackedRaceRetrievalProcessor.class);
trackedLegRetrieverChain.addAfter(TrackedLegRetrievalProcessor.class);
trackedLegRetrieverChain.endWith(TrackedLegOfCompetitorRetrievalProcessor.class);
	</pre>
	**After:**
	<pre>
RetrieverChain trackedRaceRetrieverChain = new RetrieverChain(RacingEventService.class, HasTrackedRaceContext.class);
trackedLegRetrieverChain.startWith(RegattaRetrievalProcessor.class);
trackedLegRetrieverChain.endWith(TrackedRaceRetrievalProcessor.class);<br>
RetrieverChain trackedLegOfCompetitorRetrieverChain = new RetrieverChain(trackedRaceRetrieverChain, HasTrackedLegOfCompetitorContext.class);
// Don't call startWith after the creation with an existing chain or an exception will be thrown
trackedLegRetrieverChain.addAfter(TrackedLegRetrievalProcessor.class);
trackedLegRetrieverChain.endWith(TrackedLegOfCompetitorRetrievalProcessor.class);
	</pre>
* The first part of the split `DataRetrieverChainDefinition` can now be used to create the one for the new Fact. This works accordingly to the creation of `trackedLegOfCompetitorRetrieverChain`.
* Ensure, that all necessary retriever chains are returned by the method `getDataRetrieverChainDefinitions()` of the `DataMiningBundleService`. This would be `trackedLegOfCompetitorRetrieverChain` and the new one for the example.

### Implement domain specific Aggregators

It may be necessary, that your data mining bundle needs domain specific [Aggregators](/wiki/info/landscape/data-mining-architecture#Aggregators). For example to compute the aggregations for domain specific Key Figures (like `Distance`), that can't be done by the domain independent Aggregators (for example the Aggregators for numerical values located in `com.sap.sse.datamining.impl.components.aggregators`). Examples for domain specific aggregators can be found in the package `com.sap.sailing.datamining.impl.components.aggregators`.

There are several base classes for Aggregators that should be used for different purposes:

* Every base class has two generic type attributes. The `ExtractedType` for the type of the extracted values (return type of the Extraction Function) and the `AggregatedType` for the resulting aggregation.
	* The `InputType` of an Aggregator is `GroupedDataEntry<ExtractedType>`.
	* The `ResultType` of an Aggregator is `Map<GroupKey, AggregatedType>`.
* The base classes handles the concurrency, so that the concrete Aggregators can be implemented as if its methods are called sequential.
* If the aggregation can be calculated incrementally with each new input element use `AbstractParallelGroupedDataAggregationProcessor`. For example to calculate the sum.
	* The abstract method `void handleElement(InputType element)` has to perform the incremental calculation.
	* The abstract method `ResultType getResult()` has to return the aggregation.
* If the data has to be stored before the aggregation can be calculated, use `AbstractParallelGroupedDataStoringAggregationProcessor`. For example to calculate the median.
	* The abstract method `void storeElement(InputType element)` has to store the element in an internal collection. This collection has to be a member of the concrete Aggregator.
	* The abstract method `ResultType getResult()` has to calculate and return the aggregation.
* If the aggregation is of the kind, that a single value is picked from the collection of input elements use `AbstractParallelSingleGroupedValueAggregationProcessor`. For example to calculate the minimum or maximum.
	* This base class is special, because it has only the generic type attribute `ValueType`. This means that the `ExtractedType` is equal to the `AggregatedType`.
	* The abstract method `ValueType compareValuesAndReturnNewResult(ValueType currentResult, ValueType newValue)` has to compare the current result with the new value and return the new result.
* For detailed information about Aggregators see [Data Mining Architecture](/wiki/info/landscape/data-mining-architecture#Aggregators).

The next step is to define a `AggregationProcessorDefinition` for the new Aggregator.

* This definition is used by the framework to create concrete Aggregator instances for the [Data Mining Process](#Implementing-the-Data-Mining-Components).
* Such definitions are defined with `SimpleAggregationProcessorDefinition`. Its constructor takes several classes and a message key that is used for internationalization. The values of the message key in the `stringMessages*.properties` are shown in the Ui, so they should be user friendly.
* The current convention is, that every concrete Aggregator provides its definition with a static method.
* Note that the Aggregator needs a constructor with the signature `(ExecutorService, Collection<Processor<ResultType, ?>>)` or an exception will be thrown upon the construction of the `AggregationProcessorDefinition`.
* Register the new `AggregationProcessorDefinition` to the framework by adding it to the definitions returned by the method `getAggregationProcessorDefinitions()` of the `DataMiningBundleService` of the data mining bundle.

## Include a Type forcefully in the GWT Serialization Policy

It's possible that a necessary type isn't included in the GWT Serialization Policy, because of the generality of the framework. To include a type forcefully, the GWT-Service has to use this type in one of its methods. To achieve this, follow these steps:

* Create a dummy class in a shared bundle, if such a dummy class doesn't exist already in the scope of the type.
	* For example like `SSEDataMiningSerializationDummy`.
	* The class has to implement the interface `SerializationDummy` and should only have a private standard constructor, to prevent an accidental instantiation.
	* Implementing this interface adds the dummy automatically to the serialization policy, without the need to change the `DataMiningService`.
* Add a private non-final instance variable of the type to the dummy.

The type will be included in the GWT Serialization Policy, because the `DataMiningService` has a method, that uses the interface `SerializationDummy`.

If you can't implement `SerializationDummy` for any reasons do the following:

* Implement the interface `Serializable` or `IsSerializable` instead.
* Add a pseudo method to the GWT-Service, that uses the dummy class.
	* For example like `SerializationDummy pseudoMethodSoThatSomeClassesAreAddedToTheGWTSerializationPolicy()`.