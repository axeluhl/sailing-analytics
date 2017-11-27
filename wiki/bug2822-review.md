This page has the purpose to prepare and track the progress of the bug2822 review
(See https://bugzilla.sapsailing.com/bugzilla/show_bug.cgi?id=2822)
Bug 2822 changes the way competitors and boats are handled. Specificall the 1:1 containment between Competitor and Boat will be removed as part of the implementation.

# End-To-End Test Scenarios

## 1. Data migration when starting server
### 1.1 Migrate the competitor collection
### 1.2 Migrate the raceLog and regattaLog?

##  2. Managing boats
### 2.1 Create boat
### 2.2 Edit boat
### 2.3 Create competitor with boat (only possible in regattas with non changing boats)
### 2.4 Edit Competitor with boat (only possible in regattas with non changing boats)

## 3. Tracking with TracTrac connector
3.1. Tracking of existing event league WITH boat metadata
1. Bundesliga 2017 – Lindau: http://event.tractrac.com/events/event_20170524_Bundesliga/jsonservice.php
Check:
- 6 Boats must be created with correct name, color and sail number (boat number)
- 18 Teams must be created (with short team name as ‚competitor short name‘ (e.g. NRV)
- Regatta attribute ‚canBoatsChangePerRace‘ must be set to true
- MultiRaceLeaderboard: shortName, fullName are visible, boat name is invisible
- SingleRaceLeaderboard: shortName, fullName, boat name are visible
- Overall Leaderboard: shortName, fullName are visibe, boat name is invisible
- In the ‚raceColumn‘ table of the leaderboard the ‚Edit used boats for competitors‘ action must appear
- using the ‚Edit used boats for competitors‘ action the diaog should show the correct linkings between the competitors and the boats for each race
3.2. Tracking existing ESS event WITH boat metadata
Checkpoints are the same as in 1.1.
ESS 2016 – Hamburg
http://skitrac.traclive.dk/events/event_20160727_ESSHamburg/jsonservice.php

3.3. Tracking of existing sailing event without changing boats
505 IDM 2013 – Berlin:
http://traclive.dk/events/event_20130917_IDMO/jsonservice.php
Check:
- 6 Boats must be created with correct name, color and sail number (boat number)
- 18 Teams must be created (with short team name as ‚competitor short name‘ (e.g. NRV)
- Regatta attribute ‚canBoatsChangePerRace‘ must be ‚false‘
- MultiRaceLeaderboard: shortName, fullName are visible, boat name is invisible
- SingleRaceLeaderboard: shortName, fullName are visible, boat name is invisible
- Overall Leaderboard: shortName, fullName are visible, boat name is invisible
- In the ‚raceColumn‘ table of the leaderboard the ‚Edit used boats for competitors‘ action is not visible

3.4. Create new league event with changing boats
3.5. Create new event without changing boats

4. Tracking with SwissTiming connector
5. Smartphone Tracking (SPT)

6. Incremental Leaderboard
Make sure that attribute changes of competitors and boats in the admin console are reflected in the leaderboard shown to the user

7. Competitor Import (Manage2Sail connector)
Import competitors from a Manage2Sail event and check if the competitors and boats are created in the right way.

8. WebService API
8. Datamining
9. Race Manager App


Code review parts

1. CompetitorStore (CompetitorAndBoatStore) and BoatFactory 
2. Migration der 'COMPETITORS' collection -> 'COMPETITORS_WITH_BOAT_REFERENCES' und 'BOATS'
3. Neue RaceLog und RegattaLogEvents
4. Neues Attribut 'canBoatsChangePerRace' in Klasse Regatta
5. Replication
	CreateCompetitor and UpdateCompetitor 
	CreateBoat and UpdateBoat
	CreateCompetitorWithBoat needed?
6. Webservice API
RegattasResource:
  	@Path("{regattaname}/competitors")
	@Path("{regattaname}/competitors/{competitorid}/add")

BoatsResource:
	@Path("/v1/boats")
	@Path("{boatId}")
