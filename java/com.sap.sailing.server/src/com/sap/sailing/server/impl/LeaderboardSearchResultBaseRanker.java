package com.sap.sailing.server.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.sap.sailing.domain.base.LeaderboardSearchResultBase;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

public class LeaderboardSearchResultBaseRanker<T extends LeaderboardSearchResultBase> implements Comparator<T> {
    @Override
    public int compare(T o1, T o2) {
        TrackedRegatta trackedR1 = getTrackedRegatta(o1);
        TrackedRegatta trackedR2 = getTrackedRegatta(o2);
        final int result;
        if (trackedR1 == null) {
            if (trackedR2 == null) {
                result = o1.getLeaderboard().getName().compareTo(o2.getLeaderboard().getName());
            } else {
                result = -1; // a non-tracked regatta is greater (shown further down) than a tracked regatta
            }
        } else {
            if (trackedR2 == null) {
                result = 1; // a tracked regatta is less (shown further up) than a non-tracked regatta
            } else {
                // both are tracked; check times of tracked races
                result = compareByEarliestStartOfATrackedRace(trackedR1, trackedR2);
            }
        }
        return result;
    }
    
    protected TimePoint getEarliestTrackedRace(TrackedRegatta trackedRegatta) {
        List<TimePoint> startOfTrackingTimes = new ArrayList<>();
        for (TrackedRace trackedRace : trackedRegatta.getTrackedRaces()) {
            if (trackedRace.getStartOfTracking() != null) {
                startOfTrackingTimes.add(trackedRace.getStartOfTracking());
            }
        }
        final TimePoint result;
        if (startOfTrackingTimes.isEmpty()) {
            result = null;
        } else {
            result = Collections.min(startOfTrackingTimes);
        }
        return result;
    }

    protected int compareByEarliestStartOfATrackedRace(TrackedRegatta trackedR1, TrackedRegatta trackedR2) {
        assert trackedR1 != null;
        assert trackedR2 != null;
        TimePoint earliestTrackedRaceInR1 = getEarliestTrackedRace(trackedR1);
        TimePoint earliestTrackedRaceInR2 = getEarliestTrackedRace(trackedR2);
        return Util.compareToWithNull(earliestTrackedRaceInR1, earliestTrackedRaceInR2, /* nullIsLess */ false);
    }

    /**
     * Can't know a tracked regatta if we only have a {@link LeaderboardSearchResultBase} which doesn't reference a
     * real {@link Regatta} object.
     */
    protected TrackedRegatta getTrackedRegatta(T o1) {
        return null;
    }
}
