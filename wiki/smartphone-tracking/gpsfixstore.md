# GPSFixStore
[[_TOC_]]

## Purpose
The GPSFixStore functions similar to the WindStore. A `TrackedRaceImpl` expects such a store for instantation, and then uses this store to resolve the corresponding `Track`. On replicas, a `Empty*Store` is used, on the master instance a `Mongo*Store` is used. This tries to load existing fixes from the MongoDB, and will write any new incoming fixes to the MongoDB.

## Loading race-log-tracked races in the Admin Console
After restarting the server, a tracked race can now be fully recovered from its race log, and the fixes of marks and competitors stored through the GPSFixStore.

To do this
1. open the Admin Console
2. switch to the _Leaderboard Configuration_ tab
3. select a leaderboard
4. click _Reload all RaceLogs_ (has to be done to reload the [Competitor Registered](/wiki/smartphone-tracking/race-log-events#Persistent-Competitor-Registered) events, which contain device identifiers for which deserializers need to be registered through the OSGi service registry -> these are not available when the `RacingEventService` initially loads all data from the database)

<img src="/wiki/images/load-racelog-tracked-race.png">