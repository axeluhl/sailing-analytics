# Specifica for the Extreme Sailing Series

[[_TOC_]]

The Extreme Sailing Series is a hospitality event that aims to make invitees participate as close as possible at a sailing race. To achieve this goal the races take place in front of a tribune placed directly at the water (called Stadium Racing). Invitees can be assigned to a boat crew and that way be part of a race like it is not possible in any other series. 

Each race has a course that is set that way that spectators can see as much as possible regardless of the wind direction (upwind start not to be implied). The event is set to span a week, racing happens on 4 or 5 days. Every day up to 10 races take place depending on the wind conditions. Race course is defined such that a race lasts (under normal conditions) not longer than 20 minutes.

Usually not more than 8 competitors race against each other. One of the competitors is always an invitational team from the local spot that is allowed to race.

## External Links

* [Extreme Sailing Series Offical Website](http://www.extremesailingseries.com/)
* [Official Results](http://www.extremesailingseries.com/results)
* [Analytics Homepage](http://ess40-2013.sapsailing.com/)
* [Youtube Channel](http://www.youtube.com/user/ExtremeSailingSeries)

## Equipment Needed

* **From WDF**
  * 4x SIM Cards (2x WindBot, Spares)
  * 1x World Adapter
* **In Container**
 * 2x Samsung Tablet
 * 2x WindBots
 * 1x Satellite Modem
 * 1x Plug Strip
* **Provided by OC**
 * 1x Rack with servers and switches
 * 2x radio

## Official information

The current notice of race and other information can be accessed on the following page: https://octpftp.egnyte.com/. The username is `ess_main` and the password is `x402014ess`.

## Boats
To achieve the goal of providing invitees an exciting event a new type of boats have been designed. Capable of reaching speeds usually reserved to motorboats even in medium wind conditions, the Extreme 40 has been designed by Olympic champions Yves Loday and Mitch Booth, with the aim to provide the international sailing arena with a visually stunning and 100% performance-focused multihull. 

<img src="/wiki/images/ESSSAPBoat.jpg" height="325px" width="600px"/>

Flying a hull in as little as 8 knots of breeze (15 kph), the 40-foot (12m) long carbon speed machine requires coordination, finesse but also sheer muscular power from the crews who battle it out. The generous sail area allows the Extreme 40s to sail faster than the wind, which might seem puzzling at first - in just 15 knots of wind, an Extreme 40 is capable of traveling at over 25 knots

## Scoring
Each race is scored using a high point system where the winner gets 10 points, the second gets 9 and so on (going not further than 3 points). At the end of an event the last race points get doubled (20 for the winner). If there is a tie break between two competitors then the last race sets the winner (breaks tie break). Points from each race are accumulated and result in the overall score for an event.

In addition to the overall leaderboard specific to an event a global leaderboard is being maintained that denotes positions for all events during a year. This scoring scheme used for the global leaderboard has the same rules as an event specific leaderboard. The winner of an event gets 10 points and so on.

<img src="/wiki/images/ESSLeaderboardMuscat.jpg"/>

Scoring can be altered by the race committee using standard rules like DNS (DidNotStart), DNF (DidNotFinish), DNC (DidNotCount) or RDG (RedressGiven). These rules (except the last one) usually lead to a score of 0 for the race.

### Group Racing
There is a special format where either a group of 6 or 8 boats race against each other. This format is applied whenever the conditions do not allow 10 or more boats to race on the selected race area.

* Group of 6: 2 groups of 6 boats each will race agains each other. The winner will get 8 points, the second 2 points and so on.
* Group of 8: Three groups A,B and C with 4 competitors each will race against each other. There will always be two groups racing together so that the fleet size is 8.

If group racing is announced then make sure to select the right scoring scheme that is called `HighPoint, winner gets 10 points (or 8)`. In order to inform the Sailing Analytics about a race being a group race, there is an activator in the race committee app that allows the race committee to mark a race as a group race. In the background the server will add a new event to the RaceLog that is called `AdditionalScoringInformation`.

## Event Setup
The setup for such an event usually consists of the following departments:

* Race Committee
  * Boat on the water
  * On the beach (Shore Control)
* Commentator
  * For online streaming and video (beach)
  * Live (beach)
* Video and Television
  * Cameras (water)
  * Camera (beach)
  * Tech team (Controller, Cutter, Streamer)
* Visualization
  * Provider for GPS fixes, course layout and competitor names (TracTrac)
  * 3D Visualization provider (BeTomorrow)
  * Wind information (SAP)
  * Live and official result provider (SAP Sailing Analytics)

## Roles

* OC Sport (Event Organizer)
  * Clementine d'Oiron (Senior SAP Account Manager)
  * Jonathan Meadowcroft (CTO, in charge of IT and network infrastructure)
  * Liam Lavers (IT Support)
* GMR (SAP Lounge)
  * Mark Angell (Senior SAP Account Manager)
  * Nick Houchin (SAP Account Manager, Host Manager)
* TracTrac
  * Jakob Oedum (Senior Event Manager)
  * Jorge Llodra (IT Specialist)
* Sunset & Vine APP (TV)
  * Sarah Greene (Manager)

## Technical Architecture
The technical infrastructure can be divided into two major parts.

### ON-PREMISE
This part physically consists of a rack that holds all servers needed to run the event in terms of tracking and network setup. All data (gps, wind, ...) is gathered here and then replicated to the cloud.

 * The **DNS-Server** serves not only as a Gateway to the internet but also has a Nameserver installed that redirects some urls to the local setup. In addition to that a caching proxy makes sure that redundant bits of information are cached.
 * The **Sailing Analytics** server holds an Apache webserver that answers to *.sapsailing.com requests and of course the Java application running the analytics itself.
 * A **TracTrac** server that receives tracking data and provides the API to the analytics server to get GPS fixes in realtime.
 * Last but not least a **Hot-Spare** server that can be used in case of failure of another server.

In addition to the servers some switches make sure to have enough space for connecting additional networks and machines. To power the whole thing in the not so unlikely event of a power failure two mid-size UPS give enough power to at least shut down the servers cleanly.

### CLOUD
The cloud consists of one or more Sailing Analytics servers that present the data to the general public not being on premise. The on-premise servers replicate their data constantly to the cloud.

### Target (Singapore 2013?)
The following image depicts the setup that is currently implemented. It features a local setup where the dependency on a reliable and fast internet connection is minimized as much as possible.

The core of this setup is a server that not only hosts a SAP Sailing Analytics but also the TracTrac server. This way the distribution of analytical information is not dependent on the speed and bandwidth of the local internet connection. By adding a DNS server in front of this analytics server local requests can be directed to the local server even when guests use a public internet address (e.g. www.sapsailing.com). 

In case of a problem with the local server requests can be redirected to the external analytics server. This server is constantly fed with data by a replication channel that gets information bits from the local analytics server.

* The SAP Sailing Analytics server that is authoritative for computing the results is no longer in the cloud but installed on premise. That way it is no longer dependent on a replicated TracTrac server but gets data directly from local TracTrac server.
* TracTrac Server is integrated with SAP Sailing Analytics on two physical appliances. That eases maintenance and data exchange between SAP and TracTrac services.
* Every leaderboard related information is gathered by accessing the local server. That way the dependency from the internet is drastically mitigated. Everyone on site always gets the right information without delay.
* A routing server manages the DNS resolution and in case of a local failure is able to transparently redirect data to the cloud.
* Score corrections also are not longer dependent on the internet connection but get fed directly into the local server.

<img src="/wiki/images/ESSSetupSOLL.jpg"/>

To not be dependent on a shaky power source that can be restored by "wiggling pieces a little to fix the generator" it has been decided to introduce UPS that can fed important hardware with power up to half an hour. The following picture depicts a quick shot on how this looks:

<img src="/wiki/images/ESSSetupUPS.jpg"/>

### Mapping of URLs for iPhone application

We've just mapped the event names to the CSV URLs from your system. The mappings are as follows:

'rio' = "http://ess40-2013.sapsailing.com/gateway-ess40/csv/leaderboard?leaderboardName=ESS%202013%20Rio%20%28Extreme40%29"

'nice' = "http://ess40-2013.sapsailing.com/gateway-ess40/csv/leaderboard?leaderboardName=ESS%202013%20Nice%20%28Extreme40%29"

'cardiff' = "http://ess40-2013.sapsailing.com/gateway-ess40/csv/leaderboard?leaderboardName=ESS%202013%20Cardiff%20%28Extreme40%29"

'porto' = "http://ess40-2013.sapsailing.com/gateway-ess40/csv/leaderboard?leaderboardName=ESS%202013%20Porto%20%28Extreme40%29"

'istanbul' = "http://ess40-2013.sapsailing.com/gateway-ess40/csv/leaderboard?leaderboardName=ESS%202013%20Istanbul%20%28Extreme40%29"

'qingdao' = "http://ess40-2013.sapsailing.com/gateway-ess40/csv/leaderboard?leaderboardName=ESS%202013%20Qingdao%20%28Extreme40%29"

'muscat' = "http://ess40-2013.sapsailing.com/gateway-ess40/csv/leaderboard?leaderboardName=ESS%202013%20Muscat%20%28Extreme40%29"

'singapore' = "http://ess40-2013.sapsailing.com/gateway-ess40/csv/leaderboard?leaderboardName=ESS%202013%20Singapore%20%28Extreme40%29"

'total' = "http://ess40-2013.sapsailing.com/gateway-ess40/csv/leaderboard?leaderboardName=Extreme%20Sailing%20Series%202013%20Overall"

To update the results for a given event simply access the URL:

http://www.extremesailingseries.com/app/results/sap_results.php?event=istanbul

Replacing istanbul with whatever the event name is for the results that you want to update. To update the overall series results just hit:

http://www.extremesailingseries.com/app/results/sap_results.php?event=total

http://www.extremesailingseries.com/app/results/csv_uploads/

### Competitor colors for 2014

<pre>
#33CC33 Groupama
#FF0000 Alinghi
#2AFFFF GAC
#000010 ETNZ
#FFFFFF Gazprom
#999999 JP Morgan
#FFC61E SAP
#B07A00 Oman Air
#000099 Realteam
#990099 Red Bull
#16A6ED The Wave
</pre>