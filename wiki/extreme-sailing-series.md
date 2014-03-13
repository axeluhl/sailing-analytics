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
  * 1x Smartphone
  * 5x SIM Cards
  * 1x World Adapter
  * 1x Plug Strip
* **In Container**
 * 1x Toughbook
 * 2x Samsung Tablet
 * 1x iPad
 * Expedition Wind Kit
* **In Container (after Qingdao)**
 * 1x TFT
 * 1x USB-Keyboard
 * 1x USB-Mouse
 * 2x Switch (8 Port)
 * 1x Router (TP-Link)
 * 3x CAT6 Network Cable

## Notice of Race
The current notice of race can be downloaded by using the link below. It is worth to mention that the NOR lacks the information about the fact that the last race breaks tie break.

Download [[Notice of Race 2013 Final|wiki/uploads/NOR2013_ESS_final.pdf]]

## Boats
To achieve the goal of providing invitees an exciting event a new type of boats have been designed. Capable of reaching speeds usually reserved to motorboats even in medium wind conditions, the Extreme 40 has been designed by Olympic champions Yves Loday and Mitch Booth, with the aim to provide the international sailing arena with a visually stunning and 100% performance-focused multihull. 

<img src="/wiki/images/ESSSAPBoat.jpg" height="325px" width="600px"/>

Flying a hull in as little as 8 knots of breeze (15 kph), the 40-foot (12m) long carbon speed machine requires coordination, finesse but also sheer muscular power from the crews who battle it out. The generous sail area allows the Extreme 40s to sail faster than the wind, which might seem puzzling at first - in just 15 knots of wind, an Extreme 40 is capable of traveling at over 25 knots

## Scoring
Each race is scored using a high point system where the winner gets 10 points, the second gets 9 and so on (going not further than 3 points). At the end of an event the last race points get doubled (20 for the winner). If there is a tie break between two competitors then the last race sets the winner (breaks tie break). Points from each race are accumulated and result in the overall score for an event. 

In addition to the overall leaderboard specific to an event a global leaderboard is being maintained that denotes positions for all events during a year. This scoring scheme used for the global leaderboard has the same rules as an event specific leaderboard. The winner of an event gets 10 points and so on.

<img src="/wiki/images/ESSLeaderboardMuscat.jpg"/>

Scoring can be altered by the race committee using standard rules like DNS (DidNotStart), DNF (DidNotFinish), DNC (DidNotCount) or RDG (RedressGiven). These rules (except the last one) usually lead to a score of 0 for the race.

## Event Setup
The setup for such an event usually consists of the following departments:

* Race Committee
  * Boat on the water
  * On the beach
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

## Technical Architecture
For the department of Visualization the technical infrastructure can be divided into three major parts.

1. The first parts (CLOUD) contains external servers accessible over the internet. The main purpose of these servers is to serve as the endpoint for data sent by various tracking systems. The setup of tracking systems consists of the following three trackers:
  * A wind tracking system that can transfer information about direction and strength. The transfer is only possible using a publicly accessible IP address.
  * A system transferring buoy positions (GPS fixes) over TCP. This system also can only be configured to transfer data over a TCP channel identified by an IP address.
  * A tracker that is attached to each boat and that transfers GPS fixes in an regular interval. For this tracker the same limitation as above regarding the addressable endpoint holds

2. The second part (ON PREMISE INTERNAL) contains all components that are needed to provide analytics. Data sent by trackers to the cloud is retrieved in this section and being analyzed. The connections to TV streaming and other visualization providers takes place here.

3. The third part (ON PREMISE PUBLIC) describes all components analytical data are distributed on during an event. This includes private mobile phones, iPads being available for guests, flatscreens displaying leaderboards and tv streaming.

### Actual (Muscat 2013)
The actual setup is depicted in the following image. It is easy to see that all of the components in the ON PREMISE PUBLIC area are heavily dependent on a reliable internet connection. This becomes also problematic when the connection is slow because display of analytics requires some bandwidth.

<img src="/wiki/images/ESSSetupIST.jpg"/>

### Target (Singapore 2013?)
The following image depicts the setup that is desirable for the next events but not yet implemented. It features a local setup where the dependency on a reliable and fast internet connection is minimized as much as possible.

The core of this setup is a server that not only hosts a SAP Sailing Analytics but also the TracTrac server. This way the distribution of analytical information is not dependent on the speed and bandwidth of the local internet connection. By adding a DNS server in front of this analytics server local requests can be directed to the local server even when guests use a public internet address (e.g. www.sapsailing.com). 

In case of a problem with the local server requests can be redirected to the external analytics server. This server is constantly fed with data by a replication channel that gets information bits from the local analytics server.

The main changes to the actual setup are as follows:

* The SAP Sailing Analytics server that is authoritative for computing the results is no longer in the cloud but installed on premise. That way it is no longer dependent on a replicated TracTrac server but gets data directly from local TracTrac server.
* TracTrac Server is integrated with SAP Sailing Analytics on one physical appliance. That eases maintenance and data exchange between SAP and TracTrac services.
* Every leaderboard related information is gathered by accessing the local server. That way the dependency from the internet is drastically mitigated. Everyone on site always gets the right information without delay.
* A routing server manages the DNS resolution and in case of a local failure is able to transparently redirect data to the cloud.
* Score corrections also are not longer dependent on the internet connection but get fed directly into the local server.
* Wind information for BeTomorrow can be provided without internet connection.

Two weak points still remain (highlighted by red dotted lines):

1. Wind data must be sent to a server that is reachable by a public ip address. This problem could be solved by extending the main router with 3G/4G functionality and putting the SAP Sailing Analytics server into DMZ.
2. The same holds for buoy positions.

<img src="/wiki/images/ESSSetupSOLL.jpg"/>

To not be dependent on a shaky power source that can be restored by "wiggling pieces a little to fix the generator" it has been decided to introduce UPS that can fed important hardware with power up to half an hour. The following picture depicts a quick shot on how this could look like.

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