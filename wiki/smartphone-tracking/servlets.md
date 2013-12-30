# Servlets

[[_TOC_]]

## createPersistentCompetitor
URL: `/sailingserver/racelogtracking/createPersistentCompetitor`
Servlet: `CreatePersistentCompetitorPostServlet`

**Expects**
* POST request body: Competitor-JSON with a nested Boat-JSON and Team-JSON (see `CompetitorJsonDeserializer`)
```
{"id": "",
 "name": "Competitor Fredrik",
 "displayColor": "#00FE3C",
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

## getPersistentCompetitors
URL: `/sailingserver/racelogtracking/getPersistentCompetitors`
Servlet: `PersistentCompetitorsGetServlet`

**Expects**
* GET request

**Returns**
* `200` body: JSON array of Competitor objects


## getRaceLogsInPreRacePhase
URL: `/sailingserver/racelogtracking/getRaceLogsInPreRacePhase`
Servlet: `RaceLogsInPreRacePhaseGetServlet`

**Expects**
* GET request

**Returns**
* `200` body: JSON array of RaceGroups

## createRace
URL: `/sailingserver/racelogtracking/createRace?leaderboard=<leaderboardName>&raceColumn=<raceColumnName>&fleet=<fleetName>`
Servlet: `CreateRaceLogTrackedRacePostServlet`

**Expects**
* POST request

**Returns**
* `200` RaceGroup JSON, containing only the Series/Row/Cell structure for the newly created race

**Throws**
* `404` Leaderboard does not exist, RaceColumn does not exist (if the leaderboard is a `RegattaLeaderboard`)
* `409` The RaceColumn already is linked to a tracked race`

**Comments**
* The behaviour of the servlet depends on the type of leaderboard with the name `leaderboardName`. If this is a `RegattaLeaderboard`, then the RaceColumn with the name `raceColumnName` has to already exist. If it is a `FlexibleLeaderboard`, the RaceColumn will be created if it does not yet exist.

## recordFixes
URL: `/smartphone/recordFixes`
Servlet: `RecordFixesPostServlet`

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

## pingMark
URL: `/sailingserver/racelogtracking/pingMark?leaderboard=<leaderboardName>&raceColumn=<raceColumnName>&fleet=<fleetName>`
Servlet: `PingMarkPostServlet`

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

## currentcourse
URL: `/sailingserver/rc/currentcourse`
Servlet: `CourseJsonExportServlet`

**Precondition**
* Race must have been started through a `RaceLogPreRacePhaseEndedEvent`

**Expects**
* GET request
* URL Parameters leaderboard, raceColumn and fleet

**Returns**
* `200` body: CourseBase JSON

**Comments**
* Does not use the latest `RaceLogCourseDefinitionChangedEvent`, but instead requires the presence of a `TrackedRace` for this racelog, from which the `RaceDefinition` is acquired to get the course. We could build our own servlet, but as we can only ping the marks after having created the race anyway, this isn't too much of a problem.