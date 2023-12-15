# Race Map Tracks Loading and Caching

One of the key features of the ``RaceMap`` is to display the boats and their tracks, in the form of so-called "tails" that show the boats' positions during the last so many seconds. The tails may be displayed in different colors, depending on whether a competitor is selected and if the user selected a metric to visualize using the tail's color.

The data that represents the GPS fixes with wind data for their specific location and time points, as well as detail values that may be used in coloring tails, are managed in a client-side cache so as to reduce the number of server round trips required when a user moves the time slider. This cache is implemented by the ``FixesAndTails`` class which also keeps the visible lines used to draw the tails on the map in sync with the tracking data cached.

With bugs [5921](https://bugzilla.sapsailing.com/bugzilla/show_bug.cgi?id=5921) and [5925](https://bugzilla.sapsailing.com/bugzilla/show_bug.cgi?id=5925), issues with the implementation were observed and documented. This document lists the requirements, the issues with the implementation as of around 2023-12-13, and then sketches a re-design that shall help fulfill the requirements in better, more consistent ways, with fewer glitches and better performance.

## Requirements

There are various requirements for this combination of track loading / caching / visualization:

- Work efficiently in live scenarios where new data becomes available as time progresses; in particular, load only the new data not yet cached locally as it becomes available, with as few calls as possible
- Enable showing extrapolated or interpolated boat positions if a tail or boat icon canvas needs to be drawn for a time point for which no fix exists
- Distinguish expensive requests for long tails, perhaps even with an expensive-to-compute detail metric, from determining the "current" position of all boats to be displayed for the time point for which to display the boats on the map; in other words, don't delay the drawing of the boats until all tail data has arrived
- Delay updating the tails slightly, especially in replay mode; see the ``Triggerable`` pattern already partly employed by ``FixesAndTails``
- Avoid re-loading data already present; particularly, when requesting data for a time range and the cache already has data for this time range but doesn't exactly reach the start and end of this time range, don't force a re-load of the parts of the track already loaded
- Handle out-of-order responses appropriately; different sorts of requests may cause very different computational efforts and therefore responses may arrive in an order different from the order in which their requests were sent
- Dropping high-rate dispensable requests when users "scrub" the time line, ensuring that the latest request comes through in order to finally show the boat and mark positions as well as the wind data for the time point where the time slider's thumb is; this must yet ensure that the track data is consistent and complete for the tails shown, avoiding any permanent "data holes" caused by requests dropped; cache invariants must at least eventually be established.
- Data delivered late to the server in live mode shall be picked up by the tails, even if an earlier request would have included such fixes already but didn't because they arrived late

## Current Solution (as of 2023-12-13; around commit 970ae1747b4d09ab1c2774f0019cc9fbe8457e3a)

The ``RaceMap`` class closely collaborates with the ``FixesAndTails`` class which acts as a cache for partial tracks loaded from the back-end. For the visualization on the map, ``FixesAndTails`` uses ``Colorline`` objects which can produce lines consisting of segments which may have different colors where needed. ``RaceMap.callGetRaceMapDataForAllOverlappingAndTipsOfNonOverlappingAndGetBoatPositionsForAllOthers(...)`` is the method putting together the requests for track data. ``RaceMap.refreshMap(...)`` asks ``FixesAndTails.computeFromAndTo(...)`` to determine which segments of the tracks need to be loaded for which competitors and with which detail type, based on the tail length and only for those competitors to actually be shown on the map. The ``computeFromAndTo`` method then tries to determine the segment to actually request, based on which data the cache already contains. While doing so, ``computeFromAndTo`` also records whether an overlap with existing data in the cache has been found which later affects the decision about whether to keep or discard previously cached data when adding the new data to the cache. This is trying to maintain the invariant of only keeping contiguous track segments in the cache.

The overlap detection is then also used to decide how to split up requests for the data needed. There are two types of requests possible: a combined ``GetRaceMapDataAction`` and a ``GetBoatPositionsAction``. The ``GetRaceMapDataAction`` is a disposable action that is meant to be used with ``AsyncActionsExecutor`` which implements the dropping of excessive amounts of requests, preserving and running the latest one when throttling ends. The action asks for mark positions, sideline positions, boat positions, quick ranks, simulation results, and the estimated race duration, if requested. For all but the boat positions, single snapshots are returned; for the boat positions, a segment from each track may be returned which may contain several fixes each. ``GetBoatPositionsAction`` loads track segments including optional detail values for a ``DetailType`` selected by the user. The action is a ``TimeRangeAsyncAction`` which is passed for execution to a ``TimeRangeActionsExecutor``. While this executor won't drop requests, it may slice, split and trim requests for overlapping time ranges with equal detail type, and then merge their results to form the complete responses from partial requests. This way, no requests for redundant data should be made by the same client, saving server CPU resources.

## Problems with the Current Solution

### Dropped ``GetRaceMapDataAction`` Requests

Whenever an "overlap" is recognized by ``FixesAndTails.computeFromAndTo`` then we incorrectly assume that the request resulting from trimming the time range to only what is missing will be quick to execute. However, an overlap was also recognized when the range requested expands beyond start and end of the cached segment, and the resulting request would then span the entire time frame instead of only asking the leading and trailing segment considered missing. The problem originally is caused really by fixes not always exactly matching up with tail range start and end. We don't remember which ranges were requested previously. Trimming against ranges requested instead of ranges received may help; however we always have to assume late delivery of live fixes into time ranges previously requested.

### Handling Out-of-Order Responses for ``GetBoatPositionsAction``

Long-running requests for many long tails, perhaps even with expensive-to-compute detail values for tail coloring may be overtaken by later requests for less expensive or null detail values. There is no logic in place that would drop the response to the response received late.

### ArrayOutOfBoundsException due to Inconsistent Updates of ``FixesAndTails`` Cache

Only some but not all of the updates to the ``Colorline`` objects actually representing the tails graphically on the map are performed in so-called ``Triggerable`` objects which get executed either when a request for the tail or its start and end index is made, or based on a time schedule that aligns with the tick rate of the time slider so as to advance the tail only when the boat animation that moves it to the position of the next fix is at least half way. With this, there is less of an "overshoot" of tails extending beyond the boats' bows as the boat animation is still trying to catch up.

This ``Triggerable`` pattern, however, is not used consistently for the updates to the tails and the first/last index variables, and across the different removeAt/insertAt/setAt methods used on the tails. While currently especially the ``removeAt`` calls happen with the ``Triggerable`` pattern, ``insertAt`` and ``setAt`` are invoked immediately. For the ``removeAt`` calls, the corresponding index manipulations (``indexOfFirstShownFix``, ``indexOfLastShownFix`` as well as the maps ``firstShownFix`` and ``lastShownFix``) happen immediately, potentially leading to an inconsistency between the fixes still part of the ``Colorline`` and the index variables telling which fixes are supposedly on that ``Colorline``

As a result, incorrect array access may occur, either dealing with the wrong fixes, or towards the end of the tail even leading to an ``ArrayOutOfBoundsException`` being thrown. Either we need to tie the index variable updates to the ``Triggerable`` objects actually carrying out the change on the ``Colorline``, or we have to challenge the whole process with these ``Triggerable`` objects.

### Overlap Computed at Request Time

With responses arriving in an order different from the order in which their corresponding requests were sent, and with requests potentially being dropped, when processing responses the overlap recognized at request construction time may no longer be valid when the response is being processed. If, for example, a larger number of ``GetRaceMapDataAction`` requests is sent, some of them may get dropped, and some ``GetBoatPositionsAction`` responses may have been processed in between, creating a whole new set of cached track segments. Basing the merge/replace decision on the overlap situation at request *creation* time can therefore lead to incorrect results, e.g., throwing away relevant data because *no* overlap was detected when the request was created, or by an overlap detected at request creation time that no longer applies, thus trying to merge fixes although they no longer form a contiguous segment with what's in the cache.

### Redundant Fetching of GPS Fix Data When Only the Detail Type Changes

We currently always load the detail values together with the ``GPSFixDTO`` objects. When only changing the detail type to be displayed as color on a tail, all data for the ``GPSFixDTO`` objects already known to the client have to be re-calculated redundantly, only in order to update the one ``detailValue`` field.

Furthermore, if a user switches back and forth between different detail types for the tail color, detail values already loaded will be dropped and replaced by other detail values for another detail type because currently the ``FixesAndTails`` cache currently stores the detail values within the ``GPSFixDTO`` objects and there can only be one detail value per fix.

## How to Improve

### Let ``GetRaceMapDataAction`` Ask Only for "Current Position"?

Like for the other parts of the ``GetRaceMapDataAction`` where a snapshot of mark and sideline positions is requested that lends itself well for dropping in case of excessive request loads, boat positions may be requested only for the singular time points representing the time slider's current time. This will be the position where the boat will be shown on the map (see ``RaceMap.getBoatFix(...)``). All other track segments shall be requested using ``GetBoatPositionsAction`` requests which cannot be dropped. This way we will end up having complete and consistent track segments in the cache, despite any dropped requests, yet have fast response times for displaying the boats at the right positions. Time line scrubbing is not so much of a risk for the ``GetBoatPositionsAction`` because overlapping requests will be trimmed to avoid redundancies, at least for concurrent requests. Scrubbing back and forth should usually lead to the cache filling for those time ranges crossed by the moving time slider.

There is a slight inconsistency with interpolation and extrapolation logic. Currently, the server may conduct extrapolation if asked for a time point beyond the end of the track. It will then mark the extrapolated fix as such, and the ``FixesAndTails`` cache will treat it specially, such as remove it when later fixes have been received. *Interpolation*, however, is done on the client for ``RaceMap.getBoatFix(...)`` when trying to show the boat for a time point that is in between two fixes on a cached track segment. Or it may happen in a "quick" call when the zero-second request range falls between two existing fixes. We could as well omit server-side extrapolation and instead extrapolate on the client based on the fixes received so far. This would make extrapolation and interpolation consistent.

However, this would not solve the challenge of having a "quick" call for the "current" boat position and a potentially long-running call for the longer tail. Would the quick call insert anything into the fixes cache at all? It may temporarily violate the "contiguous segments" invariant. Furthermore, if the quick calls don't update the ``FixesAndTails`` cache, will we then *always* need the additional ``GetBoatPositionsAction`` even in continuous live operations? This may quickly double the number of calls issued to the server which would be unfortunate. This may be one of the advantages of the troubled current implementation which will mostly get along with a single round-trip for short, quick tail updates and only upon time line scrubbing or jumping or changing the DetailType to be displayed would have to resort to the long calls.

Alternatively, we could stick with asking for at least short pieces of the tail in the "quick" request, but manage request dropping and out-of-order responses consistently, leading to an eventually consistent cache. There should be a maximum number of fixes requested by these "quick" calls, such as five to ten. This, however, would create an undesirable dependency on the sampling rate, but then again, with sampling rates exceeding, say, 10s, it may not make much of a difference when asking for at least one position in this time range, ending at the "newTime", leading to a single extrapolated fix as for the current implementation. But when a request for multiple fixes is then dropped, for eventual consistency we will need to understand it was dropped and check if any other request that will actually be executed will fill the gap. If not, such as request needs to be constructed and sent.

### Manage Request Dropping Explicitly

How about letting an ``AsyncAction`` optionally react to its being dropped? 

### Decide Overlap During Response Processing, Store Requested Time Ranges

We should carry along the requested time ranges into the response. On the starting side of the time range we should assume that if we received fixes in the range then there won't be earlier fixes in the range. We can therefore decide the overlap based on the beginning of the time range requested. For the end of the time range this is not so simple. If we have received newer fixes already then it's safe to assume that there are no fixes in the time range requested between its end and the last fix received for it. However, if we don't have any newer fixes yet, fixes may appear between the latest fix received and the end of the time range at a later point in time, e.g., due to late delivery to the server. For each competitor we should store the contiguous time range for which we assume we have all fixes there will ever be. This way, no long requests will emerge only because the first fix doesn't perfectly align with the start of the time range requested. Furthermore, deciding overlap when processing the response makes us independent of the order in which responses are delivered.

A potential problem with this may be that with a continued division between short/quick and long-running requests the responses received may look like not generating an overlap, e.g., when the head of the tail is being received, but in-between fixes may still be missing. With a contiguousness check at receipt we would then delete older parts of the tail to which the gap would be closed as soon as an already-running long tail request delivers its response.

So maybe we would need to record the time ranges requested already, at least regarding the long requests which cannot be dropped, and decide based on those whether or not there will be an overlap.

### Make Consistent Use of the ``Triggerable`` Pattern

If we need this ``Triggerable`` pattern at all, it should be made consistent with the updating of the indexes that describe the first and last shown fix on the tails. This needs to maintain consistency at all times, across all asynchronous, Timer or Triggerable-based tail manipulations and hence avoid any ``ArrayIndexOutOfBoundsException`` in the future.

### Optionally Separate Detail Value Requests from GPS Fix Requests

The ``FixesAndTails`` cache could manage separate detail value caches for different detail types per competitor and store those detail values separately from the GPS fixes. A separate RPC method may be provided to only request detail values for time ranges without the ``GPSFixDTO`` data. This would nicely support a user having cached the essential parts of the tracks already and now only switching between different detail types for tail coloring.

The ``FixesAndTails.computeFromAndTo`` method then would have to consider the selected detail type and inspect the cache contents to see what has already been loaded and which detail values are still missing. As a result, the time ranges requested for a competitor in ``GetBoatPositionsAction`` may differ between GPS fixes and detail values, and the result structure should separate detail value segments from GPS fix segments.

### Idea: "Streaming" all position data to the client

Especially for a live race we are constantly interested in the new fixes added to the end of all tracks. What if we periodically, at the ticking of the time slider, asked for all that's new and merged this into the FixesAndTails structure if it is showing the tails close to the "live" time point? Interpolation / extrapolation then only would have to happen on the client side.

### Problems to Expect
