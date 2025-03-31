# Sailing Web-Services API

[[_TOC_]]

For all service URLs, please note that we use an Apache reverse proxy to map URLs (and in particular the sub-domain) to a particular server instance. So, www.sapsailing.com may end up on a different server instance than tw2013.sapsailing.com or ess40-2013.sapsailing.com. Usually, we hand out a per-event or per-series URL, such as the ones above (except www.sapsailing.com which is the general landing page), and you would only fetch the content for the event you're interested in, even if the server instance has more.

In all subsequent explanations, you may replace the "www.sapsailing.com" hostname by an according per-event host name to make sure you get to the event you're interested in.

## Webservice Documentation
http://www.sapsailing.com/sailingserver/webservices

## Regatta Overview

To get an overview of which regattas a server offers, use

http://www.sapsailing.com/sailingserver/regattas

## Single Regatta

It lists regattas with their name, scoringSystem and boatclass attributes. Using the regatta name as the "name" parameter in the following service:

http://www.sapsailing.com/sailingserver/regatta?name=ESS+2012+Cardiff+(Extreme40)

you will obtain a JSON document that has the regatta structure with its series and fleets, the names of the races in the regatta and some overview data for each race including the name, whether it's a so-called "medal race" (usually resulting in double points), whether it's live (isLive), whether we have tracking data for it (isTracked) and the name of the tracked race (trackedRace) which may be null in case isTracked==false. The trackedRace value can be used in other services to obtain more details about the race.

## Wind Information

Here is an example of using the wind service, based on the regatta name and the trackedRaceName of the regatta service:

http://www.sapsailing.com/sailingserver/wind?regattaname=ESS%202012%20Cardiff%20%28Extreme40%29&racename=Cardiff%20Race12&windsource=COMBINED

Note that there is also a latdeg and lngdev field in the wind fixes in case you want to plot the wind value somewhere on the map. You can also omit the windsource parameter and will get the complete set of wind data for the race where you can also see the different wind source names, such as "EXPEDITION (6)" denoting one of several wind measurement devices, TRACK_BASED_ESTIMATION indicating what we estimate from the boats' courses, and the well-known COMBINED wind source which adds it all up.

As I mentioned during our call, there are still some legacy services that haven't been used in a while but which may get you started quickly. We can clean this up later to make things more consistent also on our end.

## Tracked Race

The service you can use to obtain more details about a particular race, using the above example, works as follows:

http://www.sapsailing.com/sailingserver/moderator?action=showrace&regattaname=ESS%202012%20Cardiff%20%28Extreme40%29&racename=Cardiff%20Race12

I attach a README that has some bits of documentation on those legacy web services which still should work as described there. Note in particular that this service grants you time-dependent access to a number of key figures for each competitor which may be of interest also for your "hawk eye view," such as all competitors' current speed over ground, distance traveled, rank, gap to leader, estimated time to next mark and whether the competitors has already started / finished that leg. If you don't provide a time point, the current time will be used for the query, usually giving you the data for the end of the race for past races, and the current data for a live race.

## Waypoint Positions

To obtain information about the waypoint positions, use

http://www.sapsailing.com/sailingserver/moderator?action=showwaypoints&regattaname=ESS%202012%20Cardiff%20%28Extreme40%29&racename=Cardiff%20Race12

A race course may use the same point several times, giving you multiple waypoints referring to the same so-called control point. A control point, in turn, can either be a single mark or can be made up of two marks as for the start and finish line or a gate for which the competitors can choose which mark to round. The service emits the GPS positions for each individual mark, grouped by the control point to which they belong, inside the waypoint referencing this control point. The control point names are unique within a course, and you will therefore see equal lat/lng values for the same control point occurring multiple times in the document in case the course uses the same control point in multiple waypoints.

## GPS Fixes for Boats

http://www.sapsailing.com/sailingserver/moderator?action=showboatpositions&regattaname=ESS%202012%20Cardiff%20%28Extreme40%29&racename=Cardiff%20Race12

lists all GPS fixes for the boats. If no from/to time points are specified, this will send you a document that contains all GPS fixes for all boats for the entire race. Each fix has the timepoint, latdeg/lngdeg, truebearingdeg (which may better be referred to as a course over ground or COG for short), the speed in knots (knotspeed) and the tack ("STARBOARD" or "PORT", telling from which side the boat has the wind coming which decides, among other things, who has to yield to avoid collisions). The tack may also help you visualizing the boat properly, with the sails on the right side (on starboard for PORT tack and vice versa).

Please note that the trackedRaceName values (such as "Cardif Race12") usually differ from the race (column) names used in the leaderboards where we rather use short names such as "R12".

There can be two different types of leaderboards: regatta leaderboards and flexible leaderboards. The leaderboard group document tells you which is which. See the isRegattaLeaderboard attribute in the leaderboard elements. For flexible leaderboards, the leaderboard group document tells the connection between the leaderboards and the tracked races. See

http://www.sapsailing.com/sailingserver/leaderboardgroup?leaderboardGroupName=Extreme%20Sailing%20Series%202012

where you can see the tracked race linked to the leaderboard columns with their trackedRaceName. Unfortunately, the regattaName attribute is missing for the flexible leaderboards which you need to get all parameters for the showwind, showboatpositions, showwaypoints and showrace services. I'll add that now and let you know when it's done.

I recommend to let user navigate the data available starting with the list of leaderboard groups available in the server (http://www.sapsailing.com/sailingserver/leaderboardgroups). From there, you can obtain the structure of all leaderboards in those groups. For regatta leaderboards, the leaderboardgroup service tells the regatta name which you can use in http://www.sapsailing.com/sailingserver/regatta?name=ESS+2012+Cardiff+%28Extreme40%29 and which then tells you the regatta structure and provides the links to the tracked races. For flexible leaderboards, you have the trackedRaceName and (soon) the regattaName attribute available in the leaderboardgroup document which lets you navigate to showboatpositions, showwaypoints, showrace and the wind service.