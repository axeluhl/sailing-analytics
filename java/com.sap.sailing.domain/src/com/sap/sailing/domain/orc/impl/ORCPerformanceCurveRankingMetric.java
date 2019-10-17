package com.sap.sailing.domain.orc.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ForkJoinTask;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.math.FunctionEvaluationException;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.orc.ORCPerformanceCurve;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingAndORCPerformanceCurveCache;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Speed;
import com.sap.sse.common.TimePoint;

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

    /**
     * Determines the ranks in the leg identified by {@code trackedLeg}. The outcome depends on whether the competitors
     * have started / finished the leg at {@code timePoint}. The following combinations have to be distinguished:
     * <ol>
     * <li>Both haven't started the leg yet at {@code timePoint}: both compare equal</li>
     * <li>One has, one hasn't started the let yet at {@code timePoint}: the one that has started compares "better"
     * (less)</li>
     * <li>Both have started the leg at {@code timePoint}: their relative corrected times at {@code timePoint} or the point
     * in time when the respective competitor finished the leg---whichever is earlier---are compared. Less means better
     * (less in terms of the comparator returned).</li>
     * </ol>
     */
    @Override
    public Comparator<TrackedLegOfCompetitor> getLegRankingComparator(TrackedLeg trackedLeg, TimePoint timePoint,
            WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache) {
        final Map<Competitor, Duration> relativeCorrectedTimeByCompetitor = new HashMap<>();
        final Map<Competitor, ForkJoinTask<Duration>> futures = new HashMap<>();
        for (final Competitor competitor : getTrackedRace().getRace().getCompetitors()) {
            futures.put(competitor, ForkJoinTask.adapt(()->{
                Duration relativeCorrectedTime;
                if (trackedLeg.getTrackedLeg(competitor).hasFinishedLeg(timePoint)) {
                    // dedicated time point at leg end; cannot use implied wind from cache
                    relativeCorrectedTime = cache.getRelativeCorrectedTime(competitor, getTrackedRace(),
                            trackedLeg.getTrackedLeg(competitor).getFinishTime(),
                            (c, t)->getRelativeCorrectedTime(c, t, cache));
                } else {
                    // can use cache; we shall compute for the cache's timePoint:
                    relativeCorrectedTime = cache.getRelativeCorrectedTime(competitor, getTrackedRace(), timePoint,
                            (c, t)->getRelativeCorrectedTime(c, t, cache));
                }
                return relativeCorrectedTime;
            }).fork());
        }
        for (final Entry<Competitor, ForkJoinTask<Duration>> e : futures.entrySet()) {
            relativeCorrectedTimeByCompetitor.put(e.getKey(), e.getValue().join());
        }
        return (tloc1, tloc2)->{
            final int result;
            final boolean hasStarted1 = tloc1.hasStartedLeg(timePoint);
            final boolean hasStarted2 = tloc2.hasStartedLeg(timePoint);
            if (!hasStarted1) {
                if (!hasStarted2) {
                    // both haven't started; they are considered equal for the leg under consideration
                    result = 0;
                } else {
                    // competitor 1 has not started the leg yet, competitor 2 has started the leg, so competitor
                    // 1 is worse (greater) than 2
                    result = 1;
                }
            } else {
                if (!hasStarted2) {
                    // competitor 1 has started the leg, competitor 2 hasn't, so competitor 1 is better (less)
                    result = -1;
                } else {
                    // both have started; use timePoint or the respective leg finishing time, whichever comes first,
                    // and determine the implied wind
                    result = Comparator
                            .nullsLast((Duration relativeCorrectedTime1, Duration relativeCorrectedTime2) -> relativeCorrectedTime1.compareTo(relativeCorrectedTime2))
                            .compare(relativeCorrectedTimeByCompetitor.get(tloc1.getCompetitor()),
                                    relativeCorrectedTimeByCompetitor.get(tloc2.getCompetitor()));
                }
            }
            return result;
        };
    }

    private Map<Competitor, Duration> getRelativeCorrectedTimesByCompetitor(TimePoint timePoint, WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache) {
        final Map<Competitor, Duration> relativeCorrectedTimesByCompetitor = new HashMap<>();
        final Map<Competitor, ForkJoinTask<Duration>> futures = new HashMap<>();
        for (final Competitor competitor : getTrackedRace().getRace().getCompetitors()) {
            futures.put(competitor, ForkJoinTask.adapt(()->cache.getRelativeCorrectedTime(competitor, getTrackedRace(), timePoint,
                    (c, t)->getRelativeCorrectedTime(c, t, cache))).fork());
        }
        for (final Entry<Competitor, ForkJoinTask<Duration>> entry : futures.entrySet()) {
            relativeCorrectedTimesByCompetitor.put(entry.getKey(), entry.getValue().join());
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
        final Duration competitorDelta = getRelativeCorrectedTime(competitor, timePoint, cache);
        if (competitorDelta != null) {
            final Competitor baseLineCompetitor = getBaseLineCompetitorForAbsoluteCorrectedTimes(timePoint, cache);
            if (baseLineCompetitor != null) {
                final Duration baseLineCompetitorRelativeCorrectedTime = getRelativeCorrectedTime(baseLineCompetitor, timePoint, cache);
                final Duration baseLineCompetitorElapsedTime = getTrackedRace().getTimeSailedSinceRaceStart(baseLineCompetitor, timePoint);
                if (baseLineCompetitorElapsedTime != null) {
                    result = baseLineCompetitorElapsedTime.plus(competitorDelta)
                            .minus(baseLineCompetitorRelativeCorrectedTime == null ? Duration.NULL
                                    : baseLineCompetitorRelativeCorrectedTime);
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
     * This decides which competitor shall have their corrected time set as their elapsed time.
     * For all other competitors, their absolute corrected time will then be calculated by first computing
     * the difference between their relative corrected time and the base line competitor's relative corrected time,
     * then adding this difference to the base line competitor's elapsed time.<p>
     * 
     * By default, this implementation uses the boat with the least GPH as the base line boat.
     */
    protected Competitor getBaseLineCompetitorForAbsoluteCorrectedTimes(TimePoint timePoint,
            WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache) {
        return getTrackedRace().getCompetitorOfBoat(getBoatWithLeastGph());
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
                    Comparator.nullsFirst(Comparator.naturalOrder()));
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
