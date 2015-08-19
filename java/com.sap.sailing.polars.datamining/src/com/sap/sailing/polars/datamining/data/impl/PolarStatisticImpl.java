package com.sap.sailing.polars.datamining.data.impl;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.polars.datamining.data.PolarStatistic;
import com.sap.sse.common.TimePoint;

public class PolarStatisticImpl implements PolarStatistic {

    private final TrackedRace trackedRace;
    private final Competitor competitor;
    private final Leg leg;

    public PolarStatisticImpl(TrackedRace trackedRace, Competitor competitor, Leg leg) {
        this.trackedRace = trackedRace;
        this.competitor = competitor;
        this.leg = leg;
    }

    @Override
    public int getFixCount() {
        GPSFixTrack<Competitor, GPSFixMoving> track = trackedRace.getTrack(competitor);
        TrackedLegOfCompetitor trackedLeg = trackedRace.getTrackedLeg(competitor, leg);
        TimePoint startTime = trackedLeg.getStartTime();
        TimePoint finishTime = trackedLeg.getFinishTime();
        int count = 0;
        if (startTime != null && finishTime != null) {
            track.lockForRead();

            // FIXME just return the fixes
            try {
                Iterable<GPSFixMoving> fixes = track.getFixes(startTime, true, finishTime, false);

                for (GPSFixMoving fix : fixes) {
                    count++;
                }
            } finally {
                track.unlockAfterRead();
            }
        }
        return count;
    }

}
