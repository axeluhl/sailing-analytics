package com.sap.sailing.domain.ranking;

import java.util.Comparator;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingCache;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;

public class ORCPerformanceCurveRankingMetric extends AbstractRankingMetric {
    private static final long serialVersionUID = -7814822523533929816L;

    public ORCPerformanceCurveRankingMetric(TrackedRace trackedRace) {
        super(trackedRace);
    }

    @Override
    public Comparator<Competitor> getRaceRankingComparator(TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Comparator<TrackedLegOfCompetitor> getLegRankingComparator(TrackedLeg trackedLeg, TimePoint timePoint,
            WindLegTypeAndLegBearingCache cache) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Duration getTimeToImprove(Competitor trailing, Competitor leading, TimePoint timePoint,
            WindLegTypeAndLegBearingCache cache) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Duration getCorrectedTime(Competitor competitor, TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Duration getDurationToReachAtEqualPerformance(Competitor who, Competitor to, Waypoint fromWaypoint,
            TimePoint whenWhoIsAtFromWaypoint, TimePoint timePointOfTosPosition) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Duration getCorrectedTime(Competitor who, Leg leg, Position estimatedPosition,
            Duration totalDurationSinceRaceStart, Distance totalWindwardDistanceTraveled) {
        // TODO Auto-generated method stub
        return null;
    }
}
