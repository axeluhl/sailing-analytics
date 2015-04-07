# Typical Data Mining Scenarios

[[_TOC_]]

## Adding a new Data Mining Bundle

* Add a new java project bundle according to the [[Typical Development Scenarios|wiki/typical-development-scenarios]]
* Add the necessary dependencies to the `MANIFEST.MF`
	* `com.sap.sse` (for the string messages)
    * `com.sap.sse.datamining`
    * `com.sap.sse.datamining.shared`
* Create an `Activator` for the new data mining bundle and provide a `DataMiningBundleService` to the `BundleContext` (see 
	* This can be done by extending the `AbstractDataMiningActivator` and implementing it's abstract methods
	* Or by implementing your own `DataMiningBundleService` and register it as OSGi-Service