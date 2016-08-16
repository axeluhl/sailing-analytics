# Polars

[[_TOC_]]

## Introduction

Polar Diagrams (or Polar Plots or Polars in short) describe how fast a boat can go depending on the wind speed and its angle to the true wind. It helps sailors to find the best angle to sail to reach their target destination. In the SAP Sailing Analytics we gather a lot of race data, which can be used to create estimations of the boats polar diagram.

This wiki page provides information on how to use the polar features in the Analytics, details on the architecture and some further ideas and problems connected to the storage and generation of polars in our system.

## Two Different Approaches

It's important to understand that we have two different approaches for gathering polar diagrams in the Analytics. We give the user datamining tools to build custom polars and we also automatically gather polars for each boat class on the server to be used by other features through an API called the PolarDataService.

### Custom Polars (Datamining)

Custom Polars can be generated in the datamining UI. You can choose filters and groupings as you desire and fire a query at the server. The server than applies these settings and returns your custom polar diagram with some additional information like underlying data sizes. The focus in this approach lies on the configurability of the query to give the user a lot of power to answer the questions they ask concerning polars. Example: You want to compare the polars of the winner of a race with the polars of the sailor who finished last. You can do that with the datamining tools. A more detailed description on how to use the polar datamining features can be found [here](#polars_using-the-datamining-ui-for-polars).

What the polar datamining feature is **not**: It is not very fast for big queries and shouldn't be used (at least as is) for automated queries that are used in real time features. It is not optimized for that purpose. For those kind of things we provide a designated Backend API called *PolarDataService*.

### PolarDataService (Backend API)

The PolarDataService is a backend API providing access to polar data that is gathered on the backend. The data structures and aggregation is designed for fast responses and real time usage. The PolarDataService is registered as an OSGi Service and can be used very independently (you only need a dependency to com.sap.sailing.domain). You can for example ask the PolarDataService for an estimated speed and beatangle for a given boatclass, windspeed and legtype (upwind or downwind) or for an estimated speed for a given boatclass, windspeed and beatangle (e.g. for reaching legs). To be light memorywise, the backend structures don't save a lot of additional data, so it does not provide any custom filtering.

For more information on how to use the PolarDataService, please see [this section](#polars_using-the-polardataservice).

## Using the Datamining UI for Polars

There are two different data retriever chains that can be used for viewing polars. One of them ("Polars") supports the actual datamining approach with custom filtering and grouping and the queries that run on the server can be very time-intensive. The other one ("Backend Polars") can only be used to look at the data that is aggregated on the backend automatically. The latter option doesn't allow filtering or grouping apart from the boat class. The feature is mostly intended for developers that use or want to use the PolarDataService and want to have a preview for how the data looks, that they get when they use the service.

### Polars

When generating custom polars you can use the settings button in the UI to configure some settings. Most of them have tooltips that explain them in some more detail. After changing the settings, all filters and grouping has to be reconfigured. This is due to the fact that some of the settings actually affect the possible values of the filters. One example for it is the wind range that you can define in the settings.

It makes a lot of sense to the wind range as a grouping dimension. It is allowed not to do it, but in most cases it will generate a useless diagram, since data from a lot of different wind speeds is averaged and thus mixed with each other.

It is important to use the Polars Aggregator (and not Count which is a standard Datamining Aggregator which just counts the elements).

When you run the query be aware, that it can take some time depending on your filtering options and the amount (and size) of races loaded. This is mostly due to wind computation in the backend. Maybe this can be optimized in the future.

#### Presentation

The data is presented in three charts. On the left you find the main polar chart that presents the polar diagrams as grouped. On the upper right there is a histogram that shows the underlying dataCount per Angle. Upon showing and hiding chart series in the polar chart, the connected histograms also show and hide.

The third chart is empty when first shown. It can show how the data for one single point in the polar chart is distributed over the underlying wind range. Just click a point in the polar chart to show that data. When clicking another point, the old data will be hidden and only the data for the new point is shown.

### Backend Polars

Displaying backend polars in the datamining UI is quite simple: Choose the "Backend Polars" Retriever chain, filter by boat class name, choose the "Polars" aggregator and run the query. The query should not take long since the PolarDataService is optimzed for quick response times.

#### Presentation

Like the custom polars, the backend polars are presented in 3 charts. The main chart is also a polar chart which is quite similar to the on used for custom polars. The other two charts are different. They are not related to underlying data but rather show the regression functions boatspeed over windspeed and beatangle over windspeed for upwind and downwind.

## Using the PolarDataService

The PolarDataService is the interface to look at, when you want to use access the backend aggregated polar data. Its methods are eqipped with JavaDoc explanations and you can look at the usages of the methods to see typical ways to use the interface.

The PolarDataService is an OSGi service. Above all it is retrieved by the RacingEventService. The RacingEventService supplies some needed objects like DomainFactories and also feeds the PolarDataService with fresh fixes. You can benefit from that because, if you have a chance to obtain the RacingEventService or maybe already have a reference to it, you can simply call service.getPolarDataService() and voila: You can start using the PolarDataService. If not you can look into handling the OSGi service listening yourself to obtain the PolarDataService.

## Polar Datamining Architecture

For general information about the Datamining Architecture please see [this wikipage](/wiki/info/landscape/data-mining-architecture).

This section will focus on the polar specific datamining functionality.

At the time of writing there are two different retrieval chains for polar data. One is for displaying the PolarDataService Data which is automatically gathered in the backend. Most classes for that feature contain the word "Backend". It is quite simple and only allows filtering by boat class name.

Most of the classes are for custom polar datamining. In this section we will concentrate on this feature.

### File Locations / Project Structure

The project structure sticks to the best practices described [here](/wiki/info/landscape/typical-data-mining-scenarios). There are two bundles:
com.sap.sailing.polars.datamining and com.sap.sailing.polars.datamining.shared; the latter containing classes that are serializable by the GWT engine.

An Activator registeres the polar datamining functionality with the main datamining server. The data package contains the retrieval data classes, the component package contains retrieval processors and the aggregators package contains the two aggregators.

The UI classes live in the gwt.ui bundle; more precisely in the com.sap.sailing.gwt.ui.polarmining package.

### Extending the Feature

The feature can be extended by adding dimensions to the types in the polar.datamining bundle.
This dimensions then serve as a new option in filtering and grouping.

How that works is displayed [here](/wiki/info/landscape/typical-data-mining-scenarios).

### Known Issues

none right now

## PolarDataService Architecture

The PolarDataService is an OSGi Service. The interface lives in com.sap.sailing.domain. Information on how to use the service is provided in [this section](#polars_using-the-polardataservice).

This section is addressed at developers who desire to understand the implementation of the service and how it gathers polar data automatically.

### File Locations / Project Structure

Apart from the service interface which lives in com.sap.sailing.domain, everything else is in the com.sap.sailing.polars bundle. PolarDataServiceImpl implements the service and can be used as a good entry point when first trying to understand the code.
This implementation also implements ReplicableWithObjectInputStream to support replication of the gathered data when a new replica is attached. If not for that the gathered data would differ on different servers.

The com.sap.sailing.polars bundle also provides an Activator that handles registration of the OSGi Service.

Everything else in the bundle is related to the pipeline that filter, groups and aggregates the data. Some more detail is given below and a look into the code will certainly help.

### Gathering Pipeline

The implementation uses features of the datamining engine to build the gathering pipeline. The pipeline is fed with GPS fixes, when they come in. These are then filtered, grouped and in the end they are aggregated into the main data structure, which is detailed below.

The pipeline is constructed in com.sap.sailing.polars.mining.PolarDataMiner. Here the core structure for the automatic polar gatherer is constructed. If you are looking to extend/change the mechanism, the PolarDataMiner is a good place to start looking for the desired part of the feature.

The filtering excludes fixes where a boat was doing a maneuver or every boat that didn't finish in the top 10%, so that good sailors provide the data for higher quality polar data.

### Main Data Structure

The last step of the gathering pipeline is putting the result into the main data structure where it lives in memory until the server is killed. The main data structure actually consists of two different aggregations:

####  SpeedRegressionPerAngleClusterProcessor

The com.sap.sailing.polars.mining.SpeedRegressionPerAngleClusterProcessor contains data to answer requests which already supply a beat angle. It maintains a boatSpeed over windSpeed regression for every boatclass and 5degree beatangle combination.

#### CubicRegressionPerCourseProcessor
The com.sap.sailing.polars.mining.CubicRegressionPerCourseProcessor contains data to answer requests which do not contain a beat angle but a legtype (upwind/downwind). It maintains two regression buckets. One boatSpeed over windSpeed and one beatAngle over windSpeed regression per boat class.

#### Regressions
The regression magic is performed by the IncrementalAnyOrderLeastSquaresImpl class, which was inspired by this blog post: <http://erikerlandson.github.io/blog/2012/07/05/deriving-an-incremental-form-of-the-polynomial-regression-equations/>

It can do online any order regressions with least squares and has optimizations to be especially quick for cubic regressions, as they are used for the regressions in the polar backend.

## Replication with Use Case

"Bug 2563 - Support replication of polar data service" states the need for interserver communication when it comes to the polars that reside in the backend. The standard event scenario is that we have one event server for each ongoing event and the archive server for the past events. We still want to be able to access polar data of the past when being on the live server. This is obvious when we think about the presentation of the simulator at a live event. It will not work very well in the beginning of the event when no races are loaded yet. Thus it would be great to have the archive data available. Multiple approaches have been discussed and I will list them here trying to sort them descending by feasability.

### System.properties

We could just let the admin specify a System Property that is read by the polar bundle upon start. The system property should contain a URL to a remote server where polars are available. (E.g. the archive server). The bundle then requests the current polar aggregation from the remote server. If successful it will set this aggregation as the initial aggregation in it. If unsuccessful the server will start with empty containers.

This would be quite simple to implement (as opposed to multiway replication) and still be sufficient for the typical event + archive scenario.

### Manual Import

The manual import approach is pretty similar to the System.properties approach, but as opposed to defining a system property, the admin could import the polars during the run time of the server similar to the Master Data Import feature. There are some problems with that approach, since merging of to non-empty aggregations in non-trivial and also a UI would have to be created.

### Replication

We could use Axels replication framework. It would allow for a more flexible approach, but a lot of work would have to be done to allow replication in that direction and between multiple servers that are not in a master-replica-configuration.

### Hard Coded Polars

Some suggested it would be okay to just read out the polars of the archive and hard-code them into the server as an initial polar state, that is then changed by new incoming data. That is a very static approach and defeats the dynamic nature of the whole polar approach but it is very simple to do.

## Other Approaches

Some short notes on other approaches for gathering polar data.

### Manual Polars

We tried creating a tool that allows an expert to gather data for manually creating polars. The development of this tool was triggered by Marcus from STG.

For a given set of races the main algorithm finds situations where two boats on different hulls pass each other closely (locationwise). It then presents these results to the user and the user can have a closer look and decide if this data should be added to the aggregation.

The problem with this approach is that it is a lot of work to get together a set of data that is not tiny. It is also unclear what to do with the data afterwards.

The tool hasn't really been used in production and thus the code has not been merged into the master branch. It resides on the branch marcus-polars.
