package com.sap.sailing.domain.tracking.impl;

import java.util.NavigableSet;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindTrack;

/**
 * Delivers what {@link TrackedRace#getWind(Position, TimePoint)} delivers, as a navigable set.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class CombinedWindAsNavigableSet extends VirtualWindFixesAsNavigableSet {

    public CombinedWindAsNavigableSet(WindTrack track, TrackedRace trackedRace,
            long resolutionInMilliseconds) {
        super(track, trackedRace, resolutionInMilliseconds);
    }
    
    public CombinedWindAsNavigableSet(WindTrack track, TrackedRace trackedRace,
            TimePoint from, TimePoint to, long resolutionInMilliseconds) {
        super(track, trackedRace, from, to, resolutionInMilliseconds);
    }
    
    @Override
    protected Wind getWind(Position p, TimePoint timePoint) {
        return getTrackedRace().getWind(p, timePoint);
    }

    @Override
    protected NavigableSet<Wind> createSubset(WindTrack track, TrackedRace trackedRace, TimePoint from, TimePoint to) {
        return new CombinedWindAsNavigableSet(track, trackedRace, from, to, getResolutionInMilliseconds());
    }

}
