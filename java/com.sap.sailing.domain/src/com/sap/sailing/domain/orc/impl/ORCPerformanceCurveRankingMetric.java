package com.sap.sailing.domain.orc.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.math.FunctionEvaluationException;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.orc.ORCPerformanceCurve;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingAndORCPerformanceCurveCache;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Speed;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

/**
 * As opposed to before 2015 when implied wind was the only ranking criterion at all times, in 2015
 * it was decided to rank based on corrected times, and corrected times shall be computed not by mapping
 * each boat's implied wind to the performance curve of a scratch boat, but instead map the implied
 * wind of the boat with the greatest implied wind onto each other boat's performance curve to obtain
 * their time allowance for the course they sailed so far and then compare with their actual elapsed
 * time. Note that for boats other than the one with the greatest implied wind this can lead to a ranking
 * that is inconsistent with an ordering by implied wind.
 */
public class ORCPerformanceCurveRankingMetric extends ORCPerformanceCurveByImpliedWindRankingMetric {
    private static final Logger logger = Logger.getLogger(ORCPerformanceCurveRankingMetric.class.getName());
    private static final long serialVersionUID = -439454462181040266L;

    public ORCPerformanceCurveRankingMetric(TrackedRace trackedRace) {
        super(trackedRace);
    }

    /**
     * As opposed to before 2015 when implied wind was the only ranking criterion at all times, in 2015
     * it was decided to rank based on corrected times, and corrected times shall be computed not by mapping
     * each boat's implied wind to the performance curve of a scratch boat, but instead map the implied
     * wind of the boat with the greatest implied wind onto each other boat's performance curve to obtain
     * their time allowance for the course they sailed so far and then compare with their actual elapsed
     * time.
     * 
     * @see #getRelativeCorrectedTime(Competitor, TimePoint, WindLegTypeAndLegBearingAndORCPerformanceCurveCache)
     */
    @Override
    public Comparator<Competitor> getRaceRankingComparator(TimePoint timePoint, WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache) {
        return getRaceRankingComparator(timePoint, getRelativeCorrectedTimesByCompetitor(timePoint, cache));
    }

    private Comparator<Competitor> getRaceRankingComparator(TimePoint timePoint,
            final Map<Competitor, Duration> relativeCorrectedTimesByCompetitor) {
        return (c1, c2)->Comparator.nullsLast((Duration correctedTime1, Duration correctedTime2)->correctedTime1.compareTo(correctedTime2)).
                compare(relativeCorrectedTimesByCompetitor.get(c1), relativeCorrectedTimesByCompetitor.get(c2));
    }

    private Map<Competitor, Duration> getRelativeCorrectedTimesByCompetitor(TimePoint timePoint, WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache) {
        final Map<Competitor, Duration> relativeCorrectedTimesByCompetitor = new HashMap<>();
        for (final Competitor competitor : getTrackedRace().getRace().getCompetitors()) {
            relativeCorrectedTimesByCompetitor.put(competitor, getRelativeCorrectedTime(competitor, timePoint, cache));
        }
        return relativeCorrectedTimesByCompetitor;
    }
    
    /**
     * Computes the corrected time by finding the leading boat with the greatest implied wind at {@code timePoint},
     * then determining {@code competitor}'s performance curve for the partial or total course sailed up to
     * {@code timePoint} and mapping the leading boat's implied wind to the time allowance for {@code competitor}
     * at {@code timePoint}. This allowance is then compared to the time sailed by {@code competitor} at {@code timePoint},
     * and the difference is added to the leader's elapsed time. This way, even if the leader has already finished the race,
     * the {@code competitor} can be compared to the leader regardless of whether or not {@code competitor} is still
     * racing at {@code timePoint}.
     */
    @Override
    public Duration getCorrectedTime(Competitor competitor, TimePoint timePoint, WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache) {
        Duration result;
        // Generally, ranking >=2015 has to happen based on corrected times (delta); for absolute corrected times, the question
        // is to which time to add the deltas. There seem to be different options allowed:
        //  - Take the boat with the lowest GPH and define its absolute corrected time to be its elapsed time; then add the delta between each
        //    other competitor's relative corrected time and the lowest GPH boat's relative corrected time to the lowest GPH boat's elapsed time
        //  - Set the winner's corrected time to be her elapsed time. Add relative corrected time differences for the others
        //  - Set the winner's corrected time to be her elapsed time plus her relative corrected time (which could be non-zero, e.g., in
        //    case the highest implied wind was below 6kts); add all other relative corrected times to the winner's elapsed time.
        // Here, we use the third option, so even the leader can have a corrected time that differs from her elapsed time in case
        // her implied wind was capped at 6 or 20 knots.
        final Map<Competitor, Duration> relativeCorrectedTimesByCompetitor = getRelativeCorrectedTimesByCompetitor(timePoint, cache);
        final Comparator<Competitor> raceRankingComparatorByRelativeCorrectedTimes = getRaceRankingComparator(timePoint, relativeCorrectedTimesByCompetitor);
        final Duration competitorDelta = relativeCorrectedTimesByCompetitor.get(competitor);
        if (competitorDelta != null) {
            final Set<Competitor> competitors = new HashSet<>();
            Util.addAll(getTrackedRace().getRace().getCompetitors(), competitors);
            final Competitor leader = Collections.min(competitors, raceRankingComparatorByRelativeCorrectedTimes);
            if (leader != null) {
                final Duration leaderElapsedTime = getTrackedRace().getTimeSailedSinceRaceStart(leader, timePoint);
                if (leaderElapsedTime != null) {
                    result = leaderElapsedTime.plus(competitorDelta);
                } else {
                    result = null;
                }
            } else {
                result = null;
            }
        } else {
            result = null;
        }
        return result;
    }

    /**
     * Computes the (relative) corrected time by determining the implied wind to use at {@code timePoint} (the greatest
     * found for any competitor), then determining the {@code competitor}'s performance curve for the partial or total
     * course sailed up to {@code timePoint} and mapping the implied wind to the time allowance for {@code competitor}
     * at {@code timePoint}. This allowance is then compared to the time sailed by {@code competitor} at
     * {@code timePoint}, and the difference is returned. Note that this difference could be negative, e.g., in case
     * the implied wind calculation was capped at 20 knots, but the boat sailed faster in an actual 25 knots breeze,
     * therefore having an elapsed time that is shorter than the allowance at 20 knots.
     */
    private Duration getRelativeCorrectedTime(Competitor competitor, TimePoint timePoint, WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache) {
        Duration competitorDelta;
        try {
            final BiFunction<TimePoint, Competitor, ORCPerformanceCurve> performanceCurveSupplier = getPerformanceCurveSupplier(cache);
            final ORCPerformanceCurve competitorPerformanceCurve = cache.getPerformanceCurveForPartialCourse(timePoint, getTrackedRace(), competitor, performanceCurveSupplier);
            final Speed maxImpliedWind = Collections.max(getImpliedWindByCompetitor(timePoint, cache).values(),
                    Comparator.nullsLast(Comparator.naturalOrder()));
            if (maxImpliedWind != null && competitorPerformanceCurve != null) {
                final Duration competitorAllowance = competitorPerformanceCurve.getAllowancePerCourse(maxImpliedWind);
                final Duration competitorElapsedTime = getTrackedRace().getTimeSailedSinceRaceStart(competitor, timePoint);
                if (competitorElapsedTime != null) {
                    competitorDelta = competitorElapsedTime.minus(competitorAllowance);
                } else {
                    competitorDelta = null;
                }
            } else {
                competitorDelta = null;
            }
        } catch (FunctionEvaluationException e) {
            logger.log(Level.WARNING, "Problem evaluating performance curve", e);
            competitorDelta = null;
        }
        return competitorDelta;
    }
}
