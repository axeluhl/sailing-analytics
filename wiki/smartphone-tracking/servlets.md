# Servlets

[[_TOC_]]

## Introduction

The servlets are listed in the chronological order that they can be called. First, persistent competitors are needed, so that they can later on be registered for the race (`createPersistentCompetitor`). These can then be listed (`getPersistentCompetitors`). When this is completed, a race in its pre-race phase can be created (`createRace`), which is then also shown in `getRaceLogsInPreRacePhase`. By selecting one of these RaceLogs and sending `RaceLogPersistentCompetitorRegisteredEvent`s and a `RaceLogCourseDefinitionChangedEvent`, the race can then be moved from its pre race phase into the tracking phase by sending the `RaceLogPreRacePhaseEndedEvent` via the race log. From this moment on - given the fact that all necessary information was already included in the RaceLog, tracking data can be added to the race. On the one hand, marks can be pinged (`pingMark`, for which knowledge of the course layout is necessary, which can be accessed through `currentcourse`), on the other hand fixes of competitors can be recorded (`recordFixes`). Pinging the marks is of course only the first step, the plan is to allow the mapping of tracking devices such as smartphones to marks as well as competitors.

**Remember to set a start time via the race log**, as the race map relies heavily on it (e.g., the course based wind estimation needs a start time, and without any other wind sources a missing start time results in no boats and marks being displayed at all, as the `SailingService#getRaceMapData()` then fails with a no wind exception).

To test the servlets manually, in addition to the unit tests, the chrome plugin [Postman](https://chrome.google.com/webstore/detail/postman-rest-client/fdmmgilgnpjigdojojpjoooidkmcomcm) in combination with this collection of [HTTP requests](http://www.getpostman.com/collections/d4eb46b5e4f566e6b7a3) for the servlets described below may come in handy.

## `/sailingserver/racelogtracking/createPersistentCompetitor`
`CreatePersistentCompetitorPostServlet`

**Expects**
* POST request body: Competitor-JSON with a nested Boat-JSON and Team-JSON (see `CompetitorJsonDeserializer`)
```
{"id": "",
 "name": "Competitor Fredrik",
 "sailID": "1234",
 "team": {
   "name": "Team Fredrik",
   "sailors": [
     {
       "name": "Fredrik",
       "description": "",
       "dateOfBirth": 2394820480284,
       "nationality": {"IOC": "GER"}
     }
     ]
 },
 "boat": {
   "name": "Boat Fredrik",
   "sailID": "1234",
   "boatClass": {
     "name": "49er"
   }
 }
}
```

**Returns**
* `200` Competitor created, body: Competitor as JSON

**Throws**
* `400` Invalid JSON in request

**Comments**
* The server choses the id, so this field can be left blank / not supplied

## `/sailingserver/racelogtracking/getPersistentCompetitors`
`PersistentCompetitorsGetServlet`

**Expects**
* GET request

**Returns**
* `200` body: JSON array of Competitor objects


### `/sailingserver/racelogtracking/getRaceLogsInPreRacePhase`
`RaceLogsInPreRacePhaseGetServlet`

**Expects**
* GET request

**Returns**
* `200` body: JSON array of RaceGroups

## `/sailingserver/racelogtracking/createRace?leaderboard=<leaderboardName>&raceColumn=<raceColumnName>&fleet=<fleetName>`
`CreateRaceLogTrackedRacePostServlet`

**Expects**
* POST request

**Returns**
* `200` RaceGroup JSON, containing only the Series/Row/Cell structure for the newly created race

**Throws**
* `404` Leaderboard does not exist, RaceColumn does not exist (if the leaderboard is a `RegattaLeaderboard`)
* `409` The RaceColumn already is linked to a tracked race`

**Comments**
* The behaviour of the servlet depends on the type of leaderboard with the name `leaderboardName`. If this is a `RegattaLeaderboard`, then the RaceColumn with the name `raceColumnName` has to already exist. If it is a `FlexibleLeaderboard`, the RaceColumn will be created if it does not yet exist.

## `/smartphone/recordFixes`
`RecordFixesPostServlet`

**Precondition**
* race has already been started by sending a `RaceLogPreRacePhaseEndedEvent`

**Expects**
* POST request body: DeviceIdentifierWithGPSFixMovingsDTO as JSON
```
{"imei": "12345678",
  "data": [
    {"unixtime": 2394820480284,
     "nmea"    :  "$GPRMC,123519,A,4807.038,N,01131.000,E,022.4,084.4,230394,003.1,W*6A"
    },
    {"unixtime": 2394820480285,
     "nmea"    :  "$GPRMC,123519,A,4807.038,N,01131.000,E,022.4,084.4,230394,003.1,W*6A"
    }
  ]
}
```

**Returns**
* `200`: fixes recorded

**Throws**
* `409` Device not mapped to race and competitor

## `/sailingserver/racelogtracking/pingMark?leaderboard=<leaderboardName>&raceColumn=<raceColumnName>&fleet=<fleetName>`
`PingMarkPostServlet`

**Precondition**
* race has already been started by sending a `RaceLogPreRacePhaseEndedEvent` and has not been stopped yet

**Expects**
* POST request body: PingMark as JSON
```
{
    "markStringId": "Leeward Mark"
    "gpsFix": {
        "unixtime": 1375951388465,
        "nmea": "$GPRMC,084308,A,41.771311,N,86.933174,E,0.0,0.0,080813,0,W*4D"
    },
}
```

**Returns**
* `200`: mark pinged

**Throws**
* `404` Leaderboard, RaceColumn or Fleet not found
* `409` Race not in tracking state

## `/sailingserver/rc/currentcourse`
`CourseJsonExportServlet`

**Expects**
* GET request
* URL Parameters leaderboard, raceColumn and fleet

**Returns**
* `200` body: CourseBase JSON

**Comments**
* Does not use the latest `RaceLogCourseDefinitionChangedEvent`, but instead requires the presence of a `TrackedRace` for this racelog, from which the `RaceDefinition` is acquired to get the course. We could build our own servlet, but as we can only ping the marks after having created the race anyway, this isn't too much of a problem.