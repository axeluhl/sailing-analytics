package com.sap.sailing.domain.ranking;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingCache;
import com.sap.sailing.domain.tracking.impl.AbstractRaceRankComparator;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;

public abstract class NonPerformanceCurveRankingMetric extends AbstractRankingMetric {
    private static final long serialVersionUID = 2647817114244817444L;

    public interface NonPerformanceCurveRankingInfo extends AbstractRankingMetric.RankingInfo {
        Duration getActualTimeFromRaceStartToReachFarthestAheadInLeg(Competitor competitor, Leg leg, WindLegTypeAndLegBearingCache cache);

        /**
         * Similar to {@link #getLeaderByCorrectedEstimatedTimeToCompetitorFarthestAhead()}, but relative to a
         * {@link Leg}. This will not consider any progress or position beyond the finishing of that <code>leg</code>.
         * Instead, for those competitors who have already finished the leg, their
         * {@link TrackedLegOfCompetitor#getFinishTime() finishing time} for the leg is used, and the distance traveled
         * is normalized to the {@link TrackedLeg#getWindwardDistance() windward distance of the leg} at the
         * {@link TrackedLeg#getReferenceTimePoint() reference time point}.
         */
        Competitor getLeaderInLegByCalculatedTime(Leg leg, WindLegTypeAndLegBearingCache cache);
        
    }
    
    public class NonPerformanceCurveRankingInfoImpl extends AbstractRankingInfoWithCompetitorRankingInfoCache implements NonPerformanceCurveRankingInfo {
        private static final long serialVersionUID = 7525315823563332681L;

        public NonPerformanceCurveRankingInfoImpl(TimePoint timePoint, Map<Competitor, RankingMetric.CompetitorRankingInfo> competitorRankingInfo, Competitor competitorFarthestAhead) {
            super(timePoint, competitorRankingInfo, competitorFarthestAhead);
        }

        @Override
        public Duration getActualTimeFromRaceStartToReachFarthestAheadInLeg(Competitor competitor, Leg leg, WindLegTypeAndLegBearingCache cache) {
            final Duration result;
            final TrackedLegOfCompetitor tloc = getTrackedRace().getTrackedLeg(competitor, getTimePoint());
            final Duration raceDurationAtTimePoint = getTrackedRace().getStartOfRace().until(getTimePoint());
            if (tloc != null && tloc.hasStartedLeg(getTimePoint())) {
                final Competitor competitorFarthestAheadInLeg = getCompetitorFarthestAheadInLeg(leg, getTimePoint(), cache);
                final TrackedLegOfCompetitor tlocOfCompetitorFarthestAheadInLeg = tloc.getTrackedLeg().getTrackedLeg(competitorFarthestAheadInLeg);
                final Duration predictedDurationFromTimePointToReachFarthestAheadInLeg = getPredictedDurationToReachWindwardPositionOf(
                        tloc, tlocOfCompetitorFarthestAheadInLeg, getTimePoint(), cache);
                if (predictedDurationFromTimePointToReachFarthestAheadInLeg == null) {
                    result = null;
                } else {
                    final Duration cDurationFromRaceStartToReachFarthestInLeg = raceDurationAtTimePoint.plus(predictedDurationFromTimePointToReachFarthestAheadInLeg);
                    result = cDurationFromRaceStartToReachFarthestInLeg;
                }
            } else {
                result = null;
            }
            return result;
        }

        @Override
        public Competitor getLeaderInLegByCalculatedTime(Leg leg, WindLegTypeAndLegBearingCache cache) {
            final TrackedLeg trackedLeg = getTrackedRace().getTrackedLeg(leg);
            return trackedLeg.getLeader(getTimePoint(), cache);
        }

    }

    protected NonPerformanceCurveRankingMetric(TrackedRace trackedRace) {
        super(trackedRace);
    }

    /**
     * Constructs a comparator based on the results of
     * {@link #getWindwardDistanceTraveled(Competitor, TimePoint, WindLegTypeAndLegBearingCache)} where competitors are
     * "less" than other competitors ("better") if they are in a later leg or, if in the same leg, have a greater
     * windward distance traveled. If both competitors have already finished the race, the finishing time is compared.
     */
    private Comparator<Competitor> getWindwardDistanceTraveledComparator(final TimePoint timePoint, final WindLegTypeAndLegBearingCache cache) {
        final Map<Competitor, Distance> windwardDistanceTraveledPerCompetitor = new HashMap<>();
        for (final Competitor competitor : getCompetitors()) {
            windwardDistanceTraveledPerCompetitor.put(competitor, getWindwardDistanceTraveled(competitor, timePoint, cache));
        }
        return new AbstractRaceRankComparator<Distance>(getTrackedRace(), timePoint, /* lessIsBetter */ false) {
            @Override
            protected Distance getComparisonValueForSameLeg(Competitor competitor) {
                return windwardDistanceTraveledPerCompetitor.get(competitor);
            }
        };
    }

    public RankingMetric.RankingInfo getRankingInfo(TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        Map<Competitor, RankingMetric.CompetitorRankingInfo> result = new HashMap<>();
        Comparator<Competitor> oneDesignComparator = getWindwardDistanceTraveledComparator(timePoint, cache);
        Competitor competitorFarthestAhead = StreamSupport
                .stream(getCompetitors().spliterator(), /* parallel */true).
                sorted(oneDesignComparator).findFirst().get();
        final Distance totalWindwardDistanceTraveled = getWindwardDistanceTraveled(competitorFarthestAhead, timePoint, cache);
        final TimePoint startOfRace = getTrackedRace().getStartOfRace();
        if (startOfRace != null) {
            final Duration actualRaceDuration = startOfRace.until(timePoint);
            for (Competitor competitor : getCompetitors()) {
                // TODO bug5110: we cannot compute the following if at timePoint the position of either of the two competitors involved is unknown; we can, however, do this if timePoint is after the two finish mark passings, or if the competitorFarthestAhead has already finished at timePoint and the position of "competitor" is known.
                final Duration predictedDurationToReachWindwardPositionOfCompetitorFarthestAhead = getPredictedDurationToReachWindwardPositionOf(
                        competitor, competitorFarthestAhead, timePoint, cache);
                final Duration totalEstimatedDurationSinceRaceStartToCompetitorFarthestAhead = predictedDurationToReachWindwardPositionOfCompetitorFarthestAhead == null ? null
                        : actualRaceDuration.plus(predictedDurationToReachWindwardPositionOfCompetitorFarthestAhead);
                final Duration calculatedEstimatedTimeWhenReachingCompetitorFarthestAhead = totalEstimatedDurationSinceRaceStartToCompetitorFarthestAhead == null ? null
                        : getCalculatedTime(
                                competitor,
                                () -> getTrackedRace().getTrackedLeg(competitorFarthestAhead, timePoint).getLeg(),
                                () -> getTrackedRace().getTrack(competitorFarthestAhead).getEstimatedPosition(
                                        timePoint, /* extrapolate */true),
                                totalEstimatedDurationSinceRaceStartToCompetitorFarthestAhead,
                                totalWindwardDistanceTraveled);
                final Duration calculatedTime = getCalculatedTime(competitor,
                        () -> getTrackedRace().getCurrentLeg(competitor, timePoint).getLeg(), () -> getTrackedRace()
                                .getTrack(competitor).getEstimatedPosition(timePoint, /* extrapolated */true),
                        actualRaceDuration, totalWindwardDistanceTraveled);
                RankingMetric.CompetitorRankingInfo rankingInfo = new CompetitorRankingInfoImpl(timePoint, competitor,
                        getWindwardDistanceTraveled(competitor, timePoint, cache), actualRaceDuration, calculatedTime,
                        predictedDurationToReachWindwardPositionOfCompetitorFarthestAhead,
                        calculatedEstimatedTimeWhenReachingCompetitorFarthestAhead);
                result.put(competitor, rankingInfo);
            }
        }
        return new NonPerformanceCurveRankingInfoImpl(timePoint, result, competitorFarthestAhead);
    }
    
    protected Comparator<Competitor> getComparatorByEstimatedCorrectedTimeWhenReachingCompetitorFarthestAhead(
            final Function<Competitor, RankingMetric.CompetitorRankingInfo> rankingInfos) {
        return (c1, c2) -> rankingInfos.apply(c1).getCorrectedTimeAtEstimatedArrivalAtCompetitorFarthestAhead()
                .compareTo(rankingInfos.apply(c2).getCorrectedTimeAtEstimatedArrivalAtCompetitorFarthestAhead());
    }

    @Override
    public RankingInfo getRankingInfo(TimePoint timePoint) {
        // TODO Implement NonPerformanceCurveRankingMetric.getRankingInfo(...)
        return super.getRankingInfo(timePoint);
    }
    
}
