# Configure Races On Server

[[_TOC_]]

After a restart of a java production server that contained archived races one need to reconfigure all these archived races. Here you find the steps needed including all druid knowledge required :-)

### General process

* Select TracTrac Configuration
* Load races list
* Select correct regatta
* Select races according to the rules below
* Make sure `Track Wind` Option is unchecked
* Hit `Start Tracking` Button
* Check that all races appear in the Tracked Races area
* Make sure their state resolves after some time to TRACKING
* For each race in the state TRACKING select it and `Stop Tracking`

### Default excludes for races

* Do not have a boat class
* Time higher than 20:00 (e.g. 21:14)
* Have "Test", "Find it", "Gone out", "PreRace" somewhere in the name

### Desaster recovery

#### Problems

* If a race switches automatically to state FINISHED then this is bad.
* If a race after some time still indicates that there is no wind associated than this is bad.
* If a race is stucked while LOADING then this is bad. Stucked means that for 5+ minutes no progress is happening.
* If there is no start time then this is bad

If one of these problems occur then you have to safely remove the race and re-add it. This does not hold for Swiss Timing Races as they are known to have multiple problems.

#### Safely remove a race

* Remove race from list. This will also trigger removal from leaderboard
* Follow process described in "General process"
* Navigate to the "Leaderboard Configuration" tab
* Locate the Leaderboard you just removed the race from
* Re-Apply link between RaceInLeaderboard and TrackedRace
* Check results

### Table with associations

| TracTrac Configuration Name | Regatta Name | Race Rules |
|:-----------|------------:|:------------:|
| 49 European Championship      |        49er Qualification Round 1 |     Yellow, Blue     |
| 49 European Championship      |        49er Qualification Round 2 |     Silver, Gold (except Gold Race 9)  |
| 49 European Championship      |        No Regatta |     All remaining  | 
| Internationale Deutche Meisterschaft (notice the missing s)     |        IDM 2011 Champions Cup |     Only Champions Cup Races  | 
| Internationale Deutche Meisterschaft      |        No Regatta |     All remaining  | 
| Kieler Woche IC 2012 | Kieler Woche 2012 29er | Only 29er |
| Kieler Woche IC 2012 | No Regatta | Star, 505, F18 |
| Kieler Woche 2012 - Proxy | Kieler Woche 2012 420 | All except "420YES" |
| Kieler Woche 2012 - Proxy | No Regatta | See Screenshot below |
| Kieler Woche 2012 | Kieler Woche 2012 (49er) | All  |
| Kieler Woche 2012 | KW 2012 Laser | All except RADIAL  |
| Kieler Woche 2012 | No Regatta | Radial |
| Kieler Woche 2012 | No Regatta | 470* |
| KW 2012 | No Regatta | SWAN |
| OBMR 2012 | Vorrunde (Laser SB3) | *VR* |
| OBMR 2012 | No Regatta | All remaining except *VR* |
| IDM Drachen | No Regatta | All |
| YES | YES 2012 29er | 29er |
| YES | No Regatta | Laser |
| BMW Cup | No Regatta | All |
| Academy Tracking 2011 | No Regatta | boatClass=MdM && boatClass=Melges24 && Lahaina Monday-Wednesday - Finale has no tracks! |
| Arenal Training 2012 | No Regatta | All |
| SAP 505 World Championship 2012 | No Regatta | All |
| Academy Tracking 2012 | No Regatta | All |
| Extreme Sailing Series | No Regatta | All |
| SAP 2011 505 | No Regatta | All |
| 505 Worlds 2010 | No Regatta | All |
| Kieler Woche | No Regatta | 470 and 505 |

| SwissTiming URL | Regatta Name | Race Rules |
|:-----------|------------:|:------------:|
| http://mrtg.sapsailing.com/2012_OSG.json      |        No Regatta  |     All except Elliott     |

### Screenshot(s)

[Screenshot of Kieler Woche Proxy 2012 Races selection](/wiki/images/RacesKielerWocheProxy2012.jpg)