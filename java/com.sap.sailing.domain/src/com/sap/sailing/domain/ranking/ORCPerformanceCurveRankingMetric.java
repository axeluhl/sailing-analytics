package com.sap.sailing.domain.ranking;

import java.util.Comparator;
import java.util.function.Supplier;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingCache;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;

public class ORCPerformanceCurveRankingMetric extends AbstractRankingMetric {
    private static final long serialVersionUID = -7814822523533929816L;

    public final static RankingMetricConstructor CONSTRUCTOR = ORCPerformanceCurveRankingMetric::new;

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
    public Duration getCorrectedTime(Competitor competitor, TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Duration getDurationToReachAtEqualPerformance(Competitor who, Competitor to, Waypoint fromWaypoint,
            TimePoint timePointOfTosPosition, WindLegTypeAndLegBearingCache cache) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Duration getCalculatedTime(Competitor who, Supplier<Leg> leg, Supplier<Position> estimatedPosition,
            Duration totalDurationSinceRaceStart, Distance totalWindwardDistanceTraveled) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Duration getGapToLeaderInOwnTime(RankingMetric.RankingInfo rankingInfo, Competitor competitor,
            WindLegTypeAndLegBearingCache cache) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Duration getLegGapToLegLeaderInOwnTime(TrackedLegOfCompetitor trackedLegOfCompetitor, TimePoint timePoint,
            RankingInfo rankingInfo, WindLegTypeAndLegBearingCache cache) {
        // TODO Auto-generated method stub
        return null;
    }
}
