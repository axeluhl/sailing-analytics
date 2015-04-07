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