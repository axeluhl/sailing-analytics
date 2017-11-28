**This page has the purpose to prepare and track the progress of the bug2822 review**  
(See https://bugzilla.sapsailing.com/bugzilla/show_bug.cgi?id=2822)  
Bug 2822 changes the way competitors and boats are handled. Specifically the 1:1 containment between Competitor and Boat will be removed as part of the implementation.

# End-To-End Test Scenarios

## 1. Data migration when starting server

### 1.1 Migrate the COMPETITORS collection

Check:  

- Existing COMPETITORS Collection is migrated to COMPETITORS_WITH_BOAT_REFERENCES and BOATS collection
- All migrated competitors have a link to a boat from boats collection
- The attribute 'name' is cleared in new all created boat objects as the old names were not meaningful and not used

### 1.2 Migrate an already existing 'competitor with boat' to a competitor with a separate boat

In case of a server instance with already created competitors we don't know to which kind of regatta (with or without changing boats) a competitor belongs to. This will only become clear when the tracking of the already configured races starts. In case of the tractrac connector the decision can be made based on the existence of boat metadata. If we find such boat metadata we must migrate the existing 'competitor with boat' to a standalone competitor and a standalone boat.

Check:

- Before server start an existing competitor with emedded boat does exist in the COMPETITORS collection
- After server start and migration according to 1.1 this competitor does exist in the COMPETITORS_WITH_BOAT_REFERENCES collection and does have a link to a boat in the BOATS collection
- After starting the tractrac tracking of a race where this competitor is contained the tractrac connector recognizes the need for the migration and calls the corresponding function on the CompetitorStore
- After the migration the competitor does not have a boat reference anymore and the migrated boat has the right properties

### 1.3 Migrate the raceLog and regattaLog?

##  2. Managing boats

Boats can be created now in different ways:

- as 'standalone' boats in the boats panel in the admin console (subsection of 'Tracked races')
- together with a competitor when adding a competitor to a regatta where boats CAN'T change per race (e.g. SPT)
- through a tracking connector which creates competitors and boats of a race (e.g. TracTrac tracking)
- through a competitor import from an external competitor provider (e.g. Manage2Sail)

### 2.1 Boats panel

Check:

- Create a new boat
- Edit the created boat

### 2.2 Smartphone tracking using a regatta where boats of competitors can change

Check:

- Create SPT regatta with flag  'boats of competitors can change per race' set to 'false'
- Create a tracked race
- Add a competitor with a boat for this race
- Edit the boat


## 3. Tracking with TracTrac connector

### 3.1. Tracking of existing event league WITH boat metadata

1. Bundesliga 2017 – Lindau:  
http://event.tractrac.com/events/event_20170524_Bundesliga/jsonservice.php  

Check:  

- 6 Boats must be created with correct name, color and sail number (boat number)
- 18 Teams must be created (with short team name as ‚competitor short name‘ (e.g. NRV)
- Regatta attribute ‚canBoatsChangePerRace‘ must be set to true
- MultiRaceLeaderboard: shortName, fullName are visible, boat name is invisible
- SingleRaceLeaderboard: shortName, fullName, boat name are visible
- Overall Leaderboard: shortName, fullName are visibe, boat name is invisible
- In the ‚raceColumn‘ table of the leaderboard the ‚Edit used boats for competitors‘ action must appear
- using the ‚Edit used boats for competitors‘ action the diaog should show the correct linkings between the competitors and the boats for each race

### 3.2. Tracking existing ESS event WITH boat metadata

Checkpoints are the same as in 1.1.  
ESS 2016 – Hamburg  
http://skitrac.traclive.dk/events/event_20160727_ESSHamburg/jsonservice.php

## 3.3. Tracking of existing sailing event without changing boats

505 IDM 2013 – Berlin:  
http://traclive.dk/events/event_20130917_IDMO/jsonservice.php  

Check:  

- 6 Boats must be created with correct name, color and sail number (boat number)
- 18 Teams must be created (with short team name as ‚competitor short name‘ (e.g. NRV)
- Regatta attribute ‚canBoatsChangePerRace‘ must be ‚false'
- MultiRaceLeaderboard: shortName, fullName are visible, boat name is invisible
- SingleRaceLeaderboard: shortName, fullName are visible, boat name is invisible
- Overall Leaderboard: shortName, fullName are visible, boat name is invisible
- In the ‚raceColumn‘ table of the leaderboard the ‚Edit used boats for competitors‘ action is not visible

## 3.4 Create new league event with changing boats

## 3.5 Create new event without changing boats

## 4. Tracking with SwissTiming connector

## 5. Smartphone Tracking (SPT)

## 6. Incremental Leaderboard

Make sure that attribute changes of competitors and boats in the admin console are reflected in the leaderboard shown to the user

## 7. Competitor Import (Manage2Sail connector)

Import competitors from a Manage2Sail event and check if the competitors and boats are created in the right way.

## 8. WebService API

## 9. Datamining

## 10. Race Manager App


# Code review parts

## 1. CompetitorStore (CompetitorAndBoatStore) and BoatFactory

## 2. Migration der 'COMPETITORS' collection -> 'COMPETITORS_WITH_BOAT_REFERENCES' und 'BOATS'

## 3. Neue RaceLog und RegattaLogEvents

## 4. Neues Attribut 'canBoatsChangePerRace' in Klasse Regatta

## 5. Replication
	CreateCompetitor and UpdateCompetitor 
	CreateBoat and UpdateBoat
	CreateCompetitorWithBoat needed?

## 6. Webservice API
	RegattasResource:
	@Path("{regattaname}/competitors")
	@Path("{regattaname}/competitors/{competitorid}/add")

	BoatsResource:
	@Path("/v1/boats")
	@Path("{boatId}")
