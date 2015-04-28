package com.sap.sailing.domain.ranking;

import java.util.Comparator;
import java.util.function.Supplier;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingCache;
import com.sap.sailing.domain.tracking.impl.RaceRankComparator;
import com.sap.sailing.domain.tracking.impl.WindwardToGoComparator;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;

public class OneDesignRankingMetric extends AbstractRankingMetric {
    private static final long serialVersionUID = -8141113385324184349L;

    public OneDesignRankingMetric(TrackedRace trackedRace) {
        super(trackedRace);
    }

    @Override
    public Comparator<Competitor> getRaceRankingComparator(TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        return new RaceRankComparator(getTrackedRace(), timePoint, cache);
    }

    @Override
    public Comparator<TrackedLegOfCompetitor> getLegRankingComparator(TrackedLeg trackedLeg,
            TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        return new WindwardToGoComparator(trackedLeg, timePoint, cache);
    }

    @Override
    public Duration getGapToLeaderInOwnTime(RankingInfo rankingInfo, Competitor competitor, WindLegTypeAndLegBearingCache cache) {
        return rankingInfo.getCompetitorRankingInfo().get(competitor).getEstimatedActualDurationFromTimePointToCompetitorFarthestAhead();
    }

    @Override
    public Duration getCorrectedTime(Competitor competitor, TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        return getActualTimeSinceStartOfRace(competitor, timePoint);
    }

    /**
     * For one-design classes, the duration for <code>who</code> equals the duration it took <code>to</code> to
     * reach her current position since passing <code>fromWaypoint</code>.
     */
    @Override
    protected Duration getDurationToReachAtEqualPerformance(Competitor who, Competitor to, Waypoint fromWaypoint, TimePoint timePointOfTosPosition) {
        final MarkPassing whenToPassedFromWaypoint = getTrackedRace().getMarkPassing(to, fromWaypoint);
        if (whenToPassedFromWaypoint == null) {
            throw new IllegalArgumentException("Competitor "+to+" is expected to have passed "+fromWaypoint+" but hasn't");
        }
        if (whenToPassedFromWaypoint.getTimePoint().after(timePointOfTosPosition)) {
            throw new IllegalArgumentException("Competitor was expected to have passed "+fromWaypoint+" before "+timePointOfTosPosition+
                    " but did pass it at "+whenToPassedFromWaypoint.getTimePoint());
        }
        return whenToPassedFromWaypoint.getTimePoint().until(timePointOfTosPosition);
    }

    @Override
    protected Duration getCorrectedTime(Competitor who, Supplier<Leg> leg, Supplier<Position> estimatedPosition,
            Duration totalDurationSinceRaceStart, Distance totalWindwardDistanceTraveled) {
        return totalDurationSinceRaceStart;
    }
}
