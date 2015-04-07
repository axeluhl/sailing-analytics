# Data Mining Architecture

The data mining framework consists of four major parts. At first the general infrastructure, that handles the registration and deregistration of data mining bundles and provides information about the data mining components, that are currently available. The next part is about how to build and run queries. After that come the data processing components, that are used by queries and contain the main functionality of the framework. The last part are the functions, that are used by the data processing components and that can be changed during runtime.

[[_TOC_]]

## General Infrastructure

The central place to get information about the current state of the data mining and to perform actions is the OSGi-Service `DataMiningServer`. It provides information about the statistics, dimensions or data retriever chains, that are currently available. It also provides methods to create and run queries with a specific structure. For example a query, that returns all dimension values for a data type, or a standard statistic query (that is described in [Building and running Queries](#Building-and-running-Queries)). The server also has methods to get necessary elements to build your own queries (like an `ExecutorService` or the `ResourceBundleStringMessages`) or to convert DTOs to the data mining domain objects.

### Registration and Deregistration of Data Mining Bundles

## Building and running Queries

## Data Processing Components

## Data Mining Functions