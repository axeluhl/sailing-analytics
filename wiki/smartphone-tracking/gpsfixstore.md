# GPSFixStore
[[_TOC_]]

## Purpose
The GPSFixStore functions similar to the WindStore. A `TrackedRaceImpl` expects such a store for instantation, and then uses this store to resolve the corresponding `Track`. On replicas, a `Empty*Store` is used, on the master instance a `Mongo*Store` is used. This tries to load existing fixes from the MongoDB, and will write any new incoming fixes to the MongoDB.