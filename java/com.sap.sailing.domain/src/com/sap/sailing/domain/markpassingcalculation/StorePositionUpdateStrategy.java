package com.sap.sailing.domain.markpassingcalculation;

import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.MarkPassing;

/**
 * Is queued between {@link MarkPassingCalculator} and {@link MarkPassingUpdateListener}.
 */
public interface StorePositionUpdateStrategy {

    /**
     * Adds its fix either to <code>competitorFixes</code> or <code>markFixes</code>, depending what the fix is for. If
     * no fixes exist yet for that object, a new entry is created.
     */
    void storePositionUpdate(Map<Competitor, List<GPSFix>> competitorFixes, Map<Mark, List<GPSFix>> markFixes,
            List<Waypoint> addedWaypoints, List<Waypoint> removedWaypoints,
            Integer smallestChangedWaypointIndex, List<MarkPassing> fixMarkPassing,
            List<MarkPassing> removeFixedMarkPassing);
}
