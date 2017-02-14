package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.impl.WindTrackImpl.DummyWind;
import com.sap.sse.common.TimePoint;

public class LegMiddleWindAsNavigableSet extends CombinedWindAsNavigableSet {
    private static final long serialVersionUID = 6547122505863428219L;
    private final TrackedLeg trackedLeg;

    public LegMiddleWindAsNavigableSet(WindTrack track, TrackedRace trackedRace, TrackedLeg trackedLeg, TimePoint from, TimePoint to, long resolutionInMilliseconds) {
        super(track, trackedRace, from, to, resolutionInMilliseconds);
        this.trackedLeg = trackedLeg;
    }

    public LegMiddleWindAsNavigableSet(WindTrack track, TrackedRace trackedRace, TrackedLeg trackedLeg, long resolutionInMilliseconds) {
        super(track, trackedRace, resolutionInMilliseconds);
        this.trackedLeg = trackedLeg;
    }

    @Override
    protected DummyWind createDummyWindFix(TimePoint timePoint) {
        return new DummyWind(timePoint, trackedLeg.getMiddleOfLeg(timePoint));
    }

}
