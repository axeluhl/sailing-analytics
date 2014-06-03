package com.sap.sailing.server.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RegattaSearchResult;
import com.sap.sse.common.Util;

public class RegattaSearchResultRanker implements Comparator<RegattaSearchResult> {
    private final TrackedRegattaRegistry trackedRegattaRegistry;
    
    protected RegattaSearchResultRanker(RacingEventService racingEventService) {
        this.trackedRegattaRegistry = racingEventService;
    }

    @Override
    public int compare(RegattaSearchResult o1, RegattaSearchResult o2) {
        Regatta r1 = o1.getRegatta();
        Regatta r2 = o2.getRegatta();
        TrackedRegatta trackedR1 = trackedRegattaRegistry.getTrackedRegatta(r1);
        TrackedRegatta trackedR2 = trackedRegattaRegistry.getTrackedRegatta(r2);
        final int result;
        if (trackedR1 == null) {
            if (trackedR2 == null) {
                result = r1.getName().compareTo(r2.getName());
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

    private int compareByEarliestStartOfATrackedRace(TrackedRegatta trackedR1, TrackedRegatta trackedR2) {
        assert trackedR1 != null;
        assert trackedR2 != null;
        TimePoint earliestTrackedRaceInR1 = getEarliestTrackedRace(trackedR1);
        TimePoint earliestTrackedRaceInR2 = getEarliestTrackedRace(trackedR2);
        return Util.compareToWithNull(earliestTrackedRaceInR1, earliestTrackedRaceInR2, /* nullIsLess */ false);
    }

    private TimePoint getEarliestTrackedRace(TrackedRegatta trackedRegatta) {
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
}
