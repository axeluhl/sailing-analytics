package com.sap.sailing.domain.markpassingcalculation;

import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.common.util.IntHolder;

/**
 * Is queued between {@link MarkPassingCalculator} and {@link MarkPassingUpdateListener}.
 */
public interface StorePositionUpdateStrategy {

    /**
     * Adds its fix either to <code>competitorFixes</code> or <code>markFixes</code>, depending what the fix is for. If
     * no fixes exist yet for that object, a new entry is created.
     */
    void storePositionUpdate(Map<Competitor, List<GPSFix>> competitorFixes, Map<Mark, List<GPSFix>> markFixes,
            List<Waypoint> addedWaypoints, List<Waypoint> removedWaypoints, IntHolder smallestChangedWaypointIndex,
            List<Triple<Competitor, Integer, TimePoint>> fixedMarkPassings, List<Pair<Competitor, Integer>> removedMarkPassings,
            List<Pair<Competitor, Integer>> suppressedMarkPassings, List<Competitor> unSuppressedMarkPassings, CandidateFinder candidateFinder, CandidateChooser candidateChooser);
}
