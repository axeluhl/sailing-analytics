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

### Holistically Maintain Ranges Requested and Repeat Position Requests for Dropped ``GetRaceMapDataActions``

At any time, for each competitor the ``FixesAndTails`` cache has a time range that it expects to have positions requested for, either already received or still in flight. When deciding to clear a competitor's fixes/tails cache, all callbacks for outstanding requests need to be informed to drop their responses for those competitors.

Request trimming works against the start of the time range *requested*, but at the end of the requested time range the last fix *received* may be a good trimming point because in live scenarios the latest fixes sometimes may arrive a bit late.

When trimming requests with an overlap, the "to-be" time ranges maintained by ``FixesAndTails`` are extended accordingly, and outstanding requests' callbacks are recorded so they can be notified in case they need to be invalidated.

With this, eventual consistency shall be achieved.

This has to take into account position fixes already received and cached, but also requests sent for which no response has been received yet. The callback objects for requests sent that haven't seen a response yet can be referenced, and if ``FixesAndTails`` decides that the data for their time range and competitor is to be dropped from the cache then the request callback could be informed about this; when the response arrives later, its parts for the competitors whose cached fixes were dropped must not be added to / merged into the cache anymore as this may create inconsistencies again. A special case for this is an in-flight request for fixes with detail values for a detail type, where the user has switched to a different detail type after the request has been sent. The response then must not be inserted into the cache anymore.

The ``GetRaceMapDataAction`` requests should be limited to very short track segments only; probably some 5-10s at most. For low sampling rates this will then only produce an extrapolated fix. For typical sampling rates of 1/3-1Hz this will typically produce one or more actual fixes. Requests for longer track segments should immediately be moved to ``GetBoatPositionsAction`` requests to keep finding the "current" boat position quick.

As ``GetRaceMapDataAction`` requests may be dropped, their dropping must trigger a separate ``GetBoatPositionsAction`` for the time range originally requested (unless the callback was informed about its boat positions result no longer to be added to the cache), only without extrapolation, as now we're interested only in real fixes and assume that other follow-up ``GetRaceMapDataAction`` requests will take care of the "current" boat position instead. These ``GetBoatPositionsAction`` requests that replace a dropped ``GetRaceMapDataAction`` will blend in through the ``TimeRangeActionsExecutor`` with other ongoing requests and may correspondingly get trimmed and merged. Request dropping is now signaled to the action by invoking its ``dropped(...)`` method.

With this, out-of-order responses will add to the cache if an only if the cache hasn't informed the callback that its result is no longer desired/needed. In particular, out-of-order responses *may* reasonably add to the cache if needed. This also needs to be implemented for the ``GetRaceMapDataAction`` callback, restricted to the boat positions aspect of the response. For all its other aspects, only representing single instant snapshots, out-of-order responses do not have to be considered at all.

### Make Consistent Use of the ``Triggerable`` Pattern

If we need this ``Triggerable`` pattern at all, it should be made consistent with the updating of the indexes that describe the first and last shown fix on the tails. This needs to maintain consistency at all times, across all asynchronous, Timer or Triggerable-based tail manipulations and hence avoid any ``ArrayIndexOutOfBoundsException`` in the future.

### Optionally Separate Detail Value Requests from GPS Fix Requests

This is addressing a part of the bug 5925 performance aspects and could be handled as an optional extension of the work on bug 5921.

The ``FixesAndTails`` cache could manage separate detail value caches for different detail types per competitor and store those detail values separately from the GPS fixes. A separate RPC method may be provided to only request detail values for time ranges without the ``GPSFixDTO`` data. This would nicely support a user having cached the essential parts of the tracks already and now only switching between different detail types for tail coloring.

The ``FixesAndTails.computeFromAndTo`` method then would have to consider the selected detail type and inspect the cache contents to see what has already been loaded and which detail values are still missing. As a result, the time ranges requested for a competitor in ``GetBoatPositionsAction`` may differ between GPS fixes and detail values, and the result structure should separate detail value segments from GPS fix segments.

### Open Issues

How should the collaboration between classes ``RaceMap`` and ``FixesAndTails`` be organized? ``FixesAndTails`.computeFromAndTo(...)`` is currently responsible for trimming requests and deciding about overlap. The overlap markers then are returned from ``computeFromAndTo``. ``RaceMap`` constructs the request actions and callbacks, and the callbacks know about the overlap markers as returned by ``computeFromAndTo``. The callbacks, when invoked upon success, talk to ``FixesAndTails`` again and pass the overlap markers to ``FixesAndTails.updateFixes`` which then decides whether to replace all of the competitor's fixes and the tail (in case no overlap) or to carefully merge the new fixes into the existing fix cache and update the tail in place.



I find it strange that ``RaceMap.updateBoatPositions`` ignores the entire boat positions result if there is currently a zooming animation in progress. Why would we hope that the ``RaceMap.redraw()`` call at the end of such an interaction loads all this data again? Why drop all fixes data? But if we really want to stick with this pattern then we have to inform ``FixesAndTails`` or whichever component manages the relation between the cache, the outstanding requests and their callbacks, that a response to that request has been dropped. And this would apply for both, ``GetRaceMapDataAction`` as well as ``GetBoatPositionsAction``. If this was a request for a time range ending up in the middle of a contiguous segment, the cache would become inconsistent regarding the "contiguousness" invariant, unless we trimmed the cache content by, e.g., deleting the shorter of the two ends on either side of the "gap."