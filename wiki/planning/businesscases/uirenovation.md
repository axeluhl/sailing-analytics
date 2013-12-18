# UI-Renovation

# Rationale
After 2 years of functional growth of our SAP sailing analytics we get more and more feedback that our user interfaces are not meeting the user expectations anymore. This applies not only to external users of our solutions but also to colleagues presenting our solutions at sailing events. Furthermore we lack mobile support for our solutions seeing a growing usage of solutions from mobile devices at the same time.

# Purpose
We want to improve the user experience of our sailing solutions by providing more user role focused user interfaces for the common devices used today. 

# Topics
## Homepage (www.sapsailing.com)
### Description

We have developed a growing number of technical solutions for the sailing world, but we lack a common description of these solutions. Furthermore we get more and more external requests from interested sailing professionals to use our solutions and we would like to provide them a good entry point into our solution world. Therefore a renovation of our homepage describing our solutions is needed.

### Features
- Renovate the www.sapsailing.com entry page
- Add descriptions for event and team solutions
- Add common social media elements
- Advertise live events or other interesting news

## Mobile support
### Description
Over the last year we have seen a huge increase in the mobile usage of our solutions. During the Kiel week 2013 around 50% of the website access came from mobile devices. As our solutions has been designed for the usage on laptops and PC's we urgently need to improve the user experience for tablet and smartphone devices.

### Features
- Add UI support for common smartphones (iPhone, Android phones, windows phones)
- Add UI support for common tablets (iPad, Android tablets, windows tablets?)

## Sailing Analytics

We started 2011 with a solution for sailing moderators. From there by extended the supported user roles step by step to sailing event visitors, sailors, trainers, race officers, VIP lounge stuff and so on. So far we tried to serve all user roles with the same UI, but as a result the UI is overloaded with too many functions. As we understand the needs of the different user roles much better now, we think that we should provide more focused UI's for specific user roles.

## Raceboard
### Description
The raceboard is our 2D viewer to display tracked races on a map enriched with other sensor data (e.g. wind) and a lot of derived data (ranks, analytical data, etc.). Additionally we can also integrate media streams (video, audio). This feature rich viewer is recognized as complex and overloaded as it tries to be two viewers in one: one viewer for visitors following live races and another one for deep analysis of races for sailors and trainers.
By providing different viewers for the different scenarios we can focus more on the really required feature sets.
### Features of race viewer for spectators -> to watch
- Support full screen map with the leaderboard and other useful data shown as an overlay
- Simplification of all settings
- Full integration of audio and video streams
- Implementation of many small map improvements (see http://bugzilla.sapsailing.com/bugzilla/show_bug.cgi?id=1546)

### Features of race viewer for professionals (sailors, trainers) -> to analyze
- Specialized views for predefined analysis scenarios (e.g. start analysis)
- Customizable viewer to integrate more data sources and data views on demand (portlet like)

## Leaderboard
### Description
A leaderboard is actually an overall ranking for the competitors of a regatta. We advanced the leaderboard by adding analytical data on different levels (leg, race, regatta) for each competitor to a rather generic competitor data table. This data table is very useful as the basis for further competitor analysis but lacks any help if you are looking for a specific problems (e.g. let's do a maneuver performance analysis).
### Features
- Simplification of the leaderboard for spectators
- Add predefined screens for common analytical use cases for professionals (see also 'raceboard')

## Event/Regatta structure navigation
### Description
Sailing events can be quite complex in terms of their structure. An event can have multiple regattas, each regatta can consist of multiple rounds (e.g. qualification, finals) and even racing in such rounds can be splitted into separate fleets. Additionally events can be grouped to event series with a overall ranking like the Extreme Sailing Series. To build a good navigation structure which makes it easy for users to find their races of interest is still a challenge.
### Features
- Support event series as well as single events
- Support recurring events (e.g. Kieler Woche 2012, Kieler Woche 2013, etc.)
- Support events with multiple regattas
- Support regattas with rounds (e.g. qualification, finals, etc.)
- Find events and event series (search by name, boat class, location, etc.)

# Synergies
no idea...

# Risks
The hardest decision we have to take is whether we should go for native apps for mobile devices or if we can survive with a HTML5 based UI. Development of native apps is cost intensive as if have to support several platforms in parallel. Furthermore we lack the right knowledge for this task in our team. One way we could go is to try first how far we can go with HTML5 and only move to native apps when we can't achieve the expected user experience in any other way.<br/>
All UI-Renovation projects will have similar phases like "user interaction design", "prototype", "visual design", "implementation", "test" and so on. As we don't have all skills for all phases in our team we will need additional external resources.

There may be additional efforts for keeping the landing page up to date with news, noteworthy, upcoming events, etc. This may have to be handled by an agency of some sort.

# Prototype
We already started (together with the SAP AppHouse Heidelberg) to develop 3 clickable prototypes for different device types: Smartphone, Tablet and Desktop.<br/>
So far you can access the prototype for smartphone via:<br/>
- on your smartphone via: http://static.sapsailing.com/prototype/Smartphone/Home.html
- on the desktop via: http://static.sapsailing.com/prototype/Smartphone/start.html

# Estimation
This estimation is very rough as we have no experience with mobile design and implementation.
- Homepage (1 week design, 2 weeks implementation)
- Mobile Support (another 4-6 weeks design, 12 weeks implementation)
- Sailing Analytics
 - Raceboard: Simple race viewer for spectators (UID concept to be supported by Anna's master thesis)
 - Raceboard: Analytical race viewer for professionals (UID concept to be supported by Anna's master thesis)
 - Event/Regatta structure navigation (2 weeks design, 3 weeks implementation)
