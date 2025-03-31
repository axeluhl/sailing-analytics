# Off-Season 2014/2015 Planning

The following are the focus areas for the off-season activities until the first 2015 event, presumably the first ESS act in Singapore, early February 2015. We will tackle most of them in parallel, with separate people working on the different topics.

[[_TOC_]]

## newhome (Frank, O.I.O.)

 - A number of bugs and issues have been recorded during and after the first events run with the newhome implementation. See [our Bugzilla Bug #1928](http://bugzilla.sapsailing.com/bugzilla/show_bug.cgi?id=1928).
 - Navigation Clean-Up: in several sessions with Ubilabs we learned that the current navigation structure is not yet ideal. Ubilabs will come up with an improved suggestions for the site's overall navigational structure that we then have to jointly implement.
 - Better series support: A growing number of events are actually part of a series (all leagues, ESS). This is not sufficiently reflected in the UI as we only have event related views right now.
 - Regatta Overview: This has so far been the tool for the race committees and regatta organizers. We need to find out whether the newhome implementation takes over the role that so far was fulfilled by the RegattaOverview.html entry point or if we still need a page specifically geared towards this audience.
 - Live Center support: Media updates and live streams, information coming from the Race Committee App, mixed with the live tracking and scoring information shall constitute the live center content to be implemented in the newhome architecture.
 - User Management in the UI: we need support for Sign Up/In/Out on all newhome pages and the race viewer.
 - Per Race and Per Regatta default statistics: Instead of leaving the use of the tools only to the user, we want pre-configured default set-ups particularly of our RaceBoard tool to support specific types of analyses, such as start analysis, maneuver analysis, venue analysis and wind pattern analysis. We need separate pages that group these types of analyses, and we need the possibility to pre-configure our tools and views in the ways required.

## Bookmarkability and Sharability (Axel)

See also [Bug 506](http://bugzilla.sapsailing.com/bugzilla/show_bug.cgi?id=506). We need to be able to capture the entire state of a page in a serializable way so that it can be used to construct a URL which can be shared and based on which another user can re-construct the entire state of the page as it was. Furthermore, the serializability of all settings will help towards improved history management. When a user navigates back using the browser's back button, the page state can be reconstructed exactly the way it was when the user left off.

Serializable settings will also help in the context of user-specific preferences. When we can serialize all settings, we can also put them to the user store when a user is logged on. This way, when the user returns to that page (or similar pages) the same settings can be applied automatically.

## User Management (Axel)

See [http://wiki.sapsailing.com/wiki/planning/usermanagement](http://wiki.sapsailing.com/wiki/planning/usermanagement) for a description. Some progress has already been made, but it's going to be a lot more work to update our existing UIs, no longer using HTTP Basic Authentication, replacing this with Shiro's authentication and authorization concepts. As briefly touched above, this also has implications on the newhome design.

## Map Visualization Improvements (Christopher, Axel, Frank, Ubilabs)

The map visualization currently has some drawbacks:

* the map doesn't rotate
* boat, water and land visualization is not sufficiently appealing
* boat animation does not work for playspeed > 1
* the time synchronization of the drawn items is difficult as we use 2 different overlay techniques (google map overlays, HTML overlays)
* performance might become a problem when we go for a fullscreen map in the future with even more overlays (e.g. the leaderboard will become an overlay)

For the rotation problem we see two possible solutions: transforming coordinates while staying on Google Maps; or replacing the map provider, probably using OpenStreetMap. For the "appeal" issue we seem to agree that the use of plugin technology is to be avoided, and so the options have to be recruited from HTML5/CSS-land. In particular, we believe that better boat visualizations are more or less a matter of a good graphics designer painting hull and sail constellations for specific boat classes. Water visualizations will have to be experimented with on the basis of HTML5 canvas technology (performance, appeal). Land visualization would require satellite imagery with precise geo-location information to fit into a map provider and may be more difficult. See also [[here | /wiki/planning/MapRenewal]] for the discussion.

## Volume Business (Ubilabs, Fredrik, Jan)

Reaching a broader audience with the SAP Sailing Analytics will mainly happen through two channels: smartphone tracking and a free-to-use race committee app. Both activities are underway with Ubilabs contributing the UI/UX design and SAP contributing additional development support. We plan to complete one major iteration of both, the smartphone tracking app for iOS and Android, and one major iteration of the race committee app until the end of this planning time frame. 

## Data Quality (Christopher)

See [https://wiki.sapsailing.com/wiki/info/misc/data-quality](https://wiki.sapsailing.com/wiki/info/misc/data-quality). We think that transparency needs to be the first step. Therefore, we should start working on internal analyses, probably with the help of the DataMining framework (see below), to produce reports that show us all the facts about the data quality so far. Only then can we decide how much to invest into cleansing and outlier detection and how much to spend on advanced aggregation techniques that live with sparse and lossy data.

## Data Mining and Polar Sheets (Lennart, Frederik)
We have made good progress with the DataMining framework and corresponding branch. The PolarSheet calculations are to be migrated to the DataMining framework. We want to implement a first set of analysis "models" with a prototypical UI that allows us to test the DataMining framework's maturity. Only if this works out well can we continue with UI/UX design.

## Simulator Integration (Christopher)

The simulator has been integrated with the race viewer. It is currently activated by a hidden URL parameter. We assume that we should attach the availability of this feature to a specific user role, such as coach or partner. Further, the simulator should consider integrating the simulated tracks as "virtual competitors" into an extended leaderboard view, but this feature is out of scope for the time frame discussed here.

## Web Site Access Statistics (Student, TBD)

We agree that better information about access to our web site would be very helpful. Simon had set up Piwik to analyze our Apache logs. This didn't work so well because the performance for importing the logs is really bad. However, the Apache logs contain only a small portion of what we really would like to know about our users' behavior. In particular, many of the essential requests are HTTP POST requests of which Apache logs only the URL, not the actual request parameters.

We therefore will need to log at a more fine-grained level on the server and probably also capture and collect some user statistics on the client, particularly for those interactions that don't immediately result in a server round-trip. This logging data will then be made amenable to advanced analysis.

We agreed that extending a Bachelor Thesis position or a DHBW internship to work on the topic will be best suited to move this topic ahead.
