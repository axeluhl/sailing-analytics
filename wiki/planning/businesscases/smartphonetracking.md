# Smartphone Tracking

## Rationale
There are several million active sailors around the world. STG claims there are 8,000,000 people in Germany who are interested in sailing, with some 2,000,000 active sailors. For the Optimist boat class alone (see http://www.goldener-opti.de/der-optimist) claims there to be some 150,000 active Opti sailors in 110 nations, 1200 of them in Germany.

In comparison, only a small share of them is actively participating in large regattas that are sufficiently prestigious to be supported by professionally-managed tracking infrastructure. Therefore, this large share of sailing events is not currently able to use the SAP Sailing Analytics to understand, analyze and also promote their hobby.

For a good introduction to the topic of smartphone tracking see [[Jan Bross' Bachelor thesis|doc/theses/20130913_Bross_Tracking_Sailing_Races_with_Mobile_Devices.pdf]]. For a technical description of what has been implemented in a second iteration see [[RaceLog Tracking|wiki/racelog-tracking]]. There is also a [[specification of a tracking app|https://wiki.sapsailing.com/wiki/info/mobile/app-spec/app-spec]].

## Purpose

Providing casual sailors with a mobile solution for arranging an amateur regatta featuring SAP Sailing Analytics.

## Features

* Regatta management on mobile device
    * Race format & schedule
    * Competitor management
* Race committee support on mobile device
    * Course layout
    * Starting
    * Finishing
    * Scoring
* Smart-phone-based tracking clients
    * Master data manangement
    * Intelligent power management
    * Live updates from race committee (e.g. start countdown)

## Description
As one of the major obstacles we see the cost and effort to equip an event with the tracking infrastructure necessary. The cost usually divides into three parts:

* Tracker hardware for competitors, marks and optionally wind

* Tracker handling (hand out, collect, associate, charge)

* Operations of a tracking solution (modeling the regatta with its scoring rules; assign competitors to races; monitor and maintain tracking during the races)

We believe that all three cost drivers can substantially be improved by introducing smartphones as tracking devices. The cost for tracker hardware can be lowered, mostly according to the principle of BYOD (Bring Your Own Device), where competitors use their own smartphone to have themselves tracked at a regatta. This reduces the necessity for a regatta organizer to provide tracking hardware. If live tracking is desired, the marks should still get their own tracking devices. For non-live events, we believe that the mark positions can largely be inferred from the competitors' GPS tracks. Alternatively, a race committee can "ping" the marks by simply driving a boat there with a smartphone on board and confirming the mark position based on the smartphone's GPS location.

A smartphone, particularly as compared to other low-cost trackers, opens many new possibilities that simplify the tracker handling. Smartphones are usually owned and used by a single person. Therefore, device personalization is easy, and tracker confusion will diminish. Assuming that the competitors appreciate the tracking service, it will be in their own interest to bring a well-charged smartphone to the races. As an alleviation of the "depleted battery" problem, USB battery packs are a simple solution. Being a smartphone, battery depletion can further be avoided by the phone intelligently managing its energy. In particular, if the battery power can be predicted to not suffice for the races of the day, the phone can take measures that will reduce its energy consumption. This includes lowering the transmission frequency and ultimately also lowering the tracking frequency. This will at least help preserve all tracking data for all offline and replay use cases where possible. When back on shore, attached to USB power, the phone can easily transmit all tracking data for post-race analysis.

The third cost driver can probably not be eliminated entirely. In particular, the regatta organizers won't be able to avoid specifying the modes of racing and scoring, and will need to tell whether the fleet is split and how, and whether or not there will be one or more medal races. However, for the straightforward, simple cases where a small number of boats does a club race on a Wednesday evening, many defaults can be assumed. For example, most of these races are usually run using a low-point scoring scheme and will not have any discards. Typically, the fleet is not split up into groups, and there is no doubling of the last race ("medal race").

## Synergies

Many club-level events are sailed using some form of handicap system, such as Yardstick, IRC or ORC. If we are to support club-level events, this may provide us with some first experience in the world of handicap sailing.

In order to support smartphones as tracking devices, we need to provide our own mark passing detection algorithm. Once we have that in place, our flexibility as to the use of tracking devices and partners increases. There are many more potential tracking partner companies who don't know what a sail course or a mark passing is than those who do.

## Risks

We cannot judge exactly how much interest there will be in the sailing community for such a solution. In the worst case, after some significant development effort many clubs would reject the solution because of a conservative, technology-averse attitude.

If the approach is generally successful, we'll end up in some substantial support effort. While we could see active and widespread use as a sign of success, we still need to provide the capacity necessary to carry the support load. Much usage could also increase our hosting cost.

## Prototype

Some prototypical work has already been launched to investigate the technical challenges closer. See [[here|wiki/smartphone-tracking]] for the status of this work.

## Estimation

Scope:
* Features as listed above
* Android platform only


We assume that a professional smartphone tracking app should be built by an external agency according to our specifications and in conjunction with the [race committee app](http://wiki.sapsailing.com/wiki/planning/businesscases/racecommitteeapp). Efforts therefore are to be divided into those for the agency and those for our team. We assume that parts of the prototype can be used, particularly back-end components such as the replicating race log that is shared with the race committee app.

 * Agency and Team: Problem description and architecture and domain knowledge transfer, three weeks
 * Agency: UI design, two weeks
 * Team: UI design sign-off and iterations, one week
 * Agency: App implementation including an integration with the RCApp where necessary, eight weeks
 * Team: back-end architecture support, three weeks

Summary: Agency 13 weeks, Team 7 weeks