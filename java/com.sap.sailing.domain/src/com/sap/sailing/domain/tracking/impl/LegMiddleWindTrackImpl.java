package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;

public class LegMiddleWindTrackImpl extends CombinedWindTrackImpl {
    private static final long serialVersionUID = -3014653185579457125L;
    private final TrackedLeg trackedLeg;

    public LegMiddleWindTrackImpl(TrackedRace trackedRace, TrackedLeg trackedLeg, double baseConfidence) {
        super(trackedRace, baseConfidence);
        this.trackedLeg = trackedLeg;
    }

    @Override
    protected LegMiddleWindAsNavigableSet createVirtualInternalRawFixes(TrackedRace trackedRace) {
        return new LegMiddleWindAsNavigableSet(this, trackedRace, trackedLeg, /* resolutionInMilliseconds */ 1000l);
    }
}
