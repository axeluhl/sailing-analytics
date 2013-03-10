# Specifica for the Extreme Sailing Series

[[_TOC_]]

The Extreme Sailing Series is a hospitality event that aims to make invitees participate as close as possible at a sailing race. To achieve this goal the races take place in front of a tribune placed directly at the water (called Stadium Racing). Invitees can be assigned to a boat crew and that way be part of a race like it is not possible in any other series. 

Each race has a course that is set that way that spectators can see as much as possible regardless of the wind direction (upwind start not to be implied). The event is set to span a week, racing happens on 4 or 5 days. Every day up to 10 races take place depending on the wind conditions. Race course is defined such that a race lasts (under normal conditions) not longer than 20 minutes.

Usually not more than 8 competitors race against each other. One of the competitors is always an invitational team from the local spot that is allowed to race.

## Links

* [Extreme Sailing Series Offical Website](http://www.extremesailingseries.com/)
* [Official Results](http://www.extremesailingseries.com/results)
* [Analytics Homepage](http://ess40-2013.sapsailing.com/)
* [Youtube Channel](http://www.youtube.com/user/ExtremeSailingSeries)

## Boats
To achieve the goal of providing invitees an exciting event a new type of boats have been designed. Capable of reaching speeds usually reserved to motorboats even in medium wind conditions, the Extreme 40 has been designed by Olympic champions Yves Loday and Mitch Booth, with the aim to provide the international sailing arena with a visually stunning and 100% performance-focused multihull. Flying a hull in as little as 8 knots of breeze (15 kph), the 40-foot (12m) long carbon speed machine requires coordination, finesse but also sheer muscular power from the crews who battle it out.

## Scoring
Each race is scored using a high point system where the winner gets 10 points, the second gets 9 and so on (going not further than 3 points). At the end of an event the last race points get doubled (20 for the winner). If there is a tie break between two competitors then the last race sets the winner (breaks tie break). Points from each race are accumulated and result in the overall score for an event. 

In addition to the overall leaderboard specific to an event a global leaderboard is being maintained that denotes positions for all events during a year. This scoring scheme used for the global leaderboard has the same rules as an event specific leaderboard. The winner of an event gets 10 points and so on.

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

### Actual
The actual setup is depicted in the following image. It is easy to see that all of the components in the ON PREMISE PUBLIC area are heavily dependent on a reliable internet connection. This becomes also problematic when the connection is slow because display of analytics requires some bandwidth.

<img src="/wiki/images/ESSSetupIST.jpg"/>

### Target
The following image depicts the setup that is desirable for the next events but not yet implemented. It features a local setup where the dependency on a reliable and fast internet connection is minimized as much as possible.

The core of this setup is a server that not only hosts a SAP Sailing Analytics but also the TracTrac server. This way the distribution of analytical information is not dependent on the speed and bandwidth of the local internet connection. By adding a DNS server in front of this analytics server local requests can be directed to the local server even when guests use a public internet address (e.g. www.sapsailing.com). 

In case of a problem with the local server requests can be redirected to the external analytics server. This server is constantly fed with data by a replication channel that gets information bits from the local analytics server.

<img src="/wiki/images/ESSSetupSOLL.jpg"/>