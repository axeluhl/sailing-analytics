package com.sap.sailing.domain.tracking.impl;

import java.util.NavigableSet;

import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;

/**
 * A wind track that delivers the result of
 * {@link TrackedRace#getWind(com.sap.sailing.domain.common.Position, com.sap.sailing.domain.common.TimePoint)},
 * using a {@link CombinedWindAsNavigableSet} as its internal raw fixes collection.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class CombinedWindTrackImpl extends VirtualWindTrackImpl {
    private CombinedWindAsNavigableSet virtualInternalRawFixes;
    
    public CombinedWindTrackImpl(TrackedRace trackedRace, long millisecondsOverWhichToAverage) {
        super(trackedRace, millisecondsOverWhichToAverage);
        virtualInternalRawFixes = new CombinedWindAsNavigableSet(this, trackedRace, /* resolutionInMilliseconds */ 1000l);
    }

    @Override
    protected NavigableSet<Wind> getInternalRawFixes() {
        return virtualInternalRawFixes;
    }
}
