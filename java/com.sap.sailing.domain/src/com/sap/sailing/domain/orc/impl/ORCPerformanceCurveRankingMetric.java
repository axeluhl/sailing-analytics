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
     */
    @Override
    public Comparator<Competitor> getRaceRankingComparator(TimePoint timePoint, WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache) {
        final Map<Competitor, Duration> correctedTimesByCompetitor = getCorrectedTimesByCompetitor(timePoint, cache);
        return (c1, c2)->Comparator.nullsLast((Duration correctedTime1, Duration correctedTime2)->correctedTime1.compareTo(correctedTime2)).
                compare(correctedTimesByCompetitor.get(c1), correctedTimesByCompetitor.get(c2));
    }

    private Map<Competitor, Duration> getCorrectedTimesByCompetitor(TimePoint timePoint, WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache) {
        final Map<Competitor, Duration> correctedTimesByCompetitor = new HashMap<>();
        for (final Competitor competitor : getTrackedRace().getRace().getCompetitors()) {
            correctedTimesByCompetitor.put(competitor, getCorrectedTime(competitor, timePoint, cache));
        }
        return correctedTimesByCompetitor;
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
        final Comparator<Competitor> comparatorByImpliedWind = super.getRaceRankingComparator(timePoint, cache);
        final Set<Competitor> competitors = new HashSet<>();
        Util.addAll(getTrackedRace().getRace().getCompetitors(), competitors);
        final Competitor leader = Collections.min(competitors, comparatorByImpliedWind); // use minimum because the comparator sorts "better" first
        if (leader != null) {
            try {
                final BiFunction<TimePoint, Competitor, ORCPerformanceCurve> performanceCurveSupplier = getPerformanceCurveSupplier(cache);
                final ORCPerformanceCurve competitorPerformanceCurve = cache.getPerformanceCurveForPartialCourse(timePoint, getTrackedRace(), competitor, performanceCurveSupplier);
                final Speed leaderImpliedWind = cache.getImpliedWind(timePoint, getTrackedRace(), leader, getImpliedWindSupplier(cache));
                if (leaderImpliedWind != null && competitorPerformanceCurve != null) {
                    final Duration competitorAllowance = competitorPerformanceCurve.getAllowancePerCourse(leaderImpliedWind);
                    final Duration competitorElapsedTime = getTrackedRace().getTimeSailedSinceRaceStart(competitor, timePoint);
                    final Duration leaderElapsedTime = getTrackedRace().getTimeSailedSinceRaceStart(leader, timePoint); // FIXME this should be redundant because the leader has elapsed=corrected
                    if (competitorElapsedTime != null && leaderElapsedTime != null) {
                        final Duration competitorDelta = competitorElapsedTime.minus(competitorAllowance);
                        result = leaderElapsedTime.plus(competitorDelta);
                    } else {
                        result = null;
                    }
                } else {
                    result = null;
                }
            } catch (FunctionEvaluationException e) {
                logger.log(Level.WARNING, "Problem evaluating performance curve", e);
                result = null;
            }
        } else {
            result = null;
        }
        return result;
    }
}
