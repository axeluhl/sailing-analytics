# Smartphone Tracking

## Rationale
There are several million active sailors around the world (TODO: get the numbers, grouped by boat class and nation). In comparison, only a small share of them is actively participating in large regattas that are sufficiently prestigious to be supported by professionally-managed tracking infrastructure. Therefore, this large share of sailing events is not currently able to use the SAP Sailing Analytics to understand, analyze and also promote their hobby.


## Purpose

Providing casual sailors with a mobile solution for arranging an amateur regatta supported by SAP's Sailing Analytics.

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

## Challenges
As one of the major obstacles we see the cost and effort to equip an event with the tracking infrastructure necessary. The cost usually divides into three parts:

* Tracker hardware for competitors, marks and optionally wind

* Tracker handling (hand out, collect, associate, charge)

* Operations of a tracking solution (modeling the regatta with its scoring rules; assign competitors to races; monitor and maintain tracking during the races)

We believe that all three cost drivers can substantially be improved by introducing smartphones as tracking devices. The cost for tracker hardware can be lowered, mostly according to the principle of BYOD (Bring Your Own Device), where competitors use their own smartphone to have themselves tracked at a regatta. This reduces the necessity for a regatta organizer to provide tracking hardware. If live tracking is desired, the marks should still get their own tracking devices. For non-live events, we believe that the mark positions can largely be inferred from the competitors' GPS tracks. Alternatively, a race committee can "ping" the marks by simply driving a boat there with a smartphone on board and confirming the mark position based on the smartphone's GPS location.

A smartphone, particularly as compared to other low-cost trackers, opens many new possibilities that simplify the tracker handling. Smartphones are usually owned and used by a single person. Therefore, device personalization is easy, and tracker confusion will diminish. Assuming that the competitors appreciate the tracking service, it will be in their own interest to bring a well-charged smartphone to the races. As an alleviation of the "depleted battery" problem, USB battery packs are a simple solution. Being a smartphone, battery depletion can further be avoided by the phone intelligently managing its energy. In particular, if the battery power can be predicted to not suffice for the races of the day, the phone can take measures that will reduce its energy consumption. This includes lowering the transmission frequency and ultimately also lowering the tracking frequency. This will at least help preserve all tracking data for all offline and replay use cases where possible. When back on shore, attached to USB power, the phone can easily transmit all tracking data for post-race analysis.

The third cost driver can probably not be eliminated entirely. In particular, the regatta organizers won't be able to avoid specifying the modes of racing and scoring, and will need to tell whether the fleet is split and how, and whether or not there will be one or more medal races. However, the straightforward, simple cases where a small number of boats does a club race on a Wednesday evening, many defaults can be assumed. For example, 