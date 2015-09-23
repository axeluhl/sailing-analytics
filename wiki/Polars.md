# Polars

[[_TOC_]]

## Introduction

Polar Diagrams (or Polar Plots or Polars in short) describe how fast a boat can go depending on the wind speed and its angle to the true wind. It helps sailors to find the best angle to sail to reach their target destination. In the SAP Sailing Analytics we gather a lot of race data, which can be used to create estimations of the boats polar diagram.

This wiki page provides information on how to use the polar features in the Analytics, details on the architecture and some further ideas and problems connected to the storage and generation of polars in our system.

## Two Different Approaches

It's important to understand that we have two different approaches for gathering polar diagrams in the Analytics. We give the user datamining tools to build custom polars and we also automatically gather polars for each boat class on the server to be used by other features through an API called the PolarDataService.

### Custom Polars (Datamining)

Custom Polars can be generated in the datamining UI. You can choose filters and groupings as you desire and fire a query at the server. The server than applies these settings and returns your custom polar diagram with some additional information like underlying data sizes. The focus in this approach lies on the configurability of the query to give the user a lot of power to answer the questions they ask concerning polars. Example: You want to compare the polars of the winner of a race with the polars of the sailor who finished last. You can do that with the datamining tools. A more detailed description on how to use the polar datamining features can be found here: _TODO_

What the polar datamining feature is **not**: It is not very fast for big queries and shouldn't be used (at least as is) for automated queries that are used in real time features. It is not optimized for that purpose. For those kind of things we provide a designated Backend API called *PolarDataService*.

### PolarDataService (Backend API)

The PolarDataService is a backend API providing access to polar data that is gathered on the backend. The data structures and aggregation is designed for fast responses and real time usage. The PolarDataService is registered as an OSGi Service and can be used very independently (you only need a dependency to com.sap.sailing.domain). You can for example ask the PolarDataService for an estimated speed and beatangle for a given boatclass, windspeed and legtype (upwind or downwind) or for an estimated speed for a given boatclass, windspeed and beatangle (e.g. for reaching legs). To be light memorywise, the backend structures don't save a lot of additional data, so it does not provide any custom filtering.

For more information on how to use the PolarDataService, please see this section: _TODO_

## Using the Datamining UI for Polars

_TODO_

## Using the PolarDataService

_TODO_

## Polar Datamining Architecture

_TODO_

### File Locations / Project Structure

_TODO_

### Extending the Feature

_TODO_

### Known Issues

_TODO_

## PolarDataService Architecture

_TODO_

### File Locations / Project Structure

_TODO_

### Main Data Structure

_TODO_

### File Locations

_TODO_
