package com.sap.sailing.domain.ranking;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Mile;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingCache;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsDurationImpl;

/**
 * The basic concept of this ranking metric is to compare corrected reciproke VMG/VMC (measured in seconds per nautical
 * mile) which we define here to be the corrected time divided by the windward or along course distance traveled, minus
 * an optional time-on-distance allowance which basically tells the expected VMG/VMC by providing the seconds to the
 * mile expected for that competitor according to its rating.
 * <p>
 * 
 * In these calculations, a leg distance shall always be the same for all competitors, meaning that for upwind/downwind
 * legs an average wind direction needs to be agreed upon.
 * <p>
 * 
 * The corrected time is determined by applying the time-on-time factor to the actual time since the start of race.
 * <p>
 * 
 * The reciproke VMG/VMC for a competitor i is determined as follows:
 * 
 * <pre>vmgc_i := t_i * f_i / d_i - g_i</pre>
 * 
 * with sailed windward/along-course distance <code>d_i</code> since the start, <code>f_i</code> being competitor i's
 * time-on-time factor, and with time-on-distance allowance <code>g_i</code> measured in seconds per nautical mile and
 * <code>t_i</code> being the time since the start of the race.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class TimeOnTimeAndDistanceRankingMetric extends AbstractRankingMetric {
    private static final long serialVersionUID = -2321904208518686420L;

    public final static RankingMetricConstructor CONSTRUCTOR = TimeOnTimeAndDistanceRankingMetric::new;

    private final Function<Competitor, Double> timeOnTimeFactor;

    private final Function<Competitor, Duration> timeOnDistanceFactorNauticalMile;
    
    /**
     * The regular constructor that can also be used as <code>TimeOnTimeAndDistanceRankingMetric::new</code>
     * to obtain a {@link RankingMetricConstructor} implementation. It uses the {@link TrackedRace}'s regatta
     * and its handicap figures to obtain each competitor's handicaps.
     */
    public TimeOnTimeAndDistanceRankingMetric(final TrackedRace trackedRace) {
        this(trackedRace,
                c -> trackedRace.getTrackedRegatta().getRegatta().getTimeOnTimeFactor(c),
                c -> trackedRace.getTrackedRegatta().getRegatta().getTimeOnDistanceAllowancePerNauticalMile(c));
    }

    /**
     * Mostly to simplify testing; instead of obtaining the handicap numbers through the {@link TrackedRace} and
     * its regatta, handicap mappings can be passed directly and overrule anything defined for the competitor or
     * on the regatta.
     */
    public TimeOnTimeAndDistanceRankingMetric(final TrackedRace trackedRace, Function<Competitor, Double> timeOnTimeFactor,
            Function<Competitor, Duration> timeOnDistanceFactorInSecondsPerNauticalMile) {
        super(trackedRace);
        this.timeOnTimeFactor = timeOnTimeFactor;
        this.timeOnDistanceFactorNauticalMile = timeOnDistanceFactorInSecondsPerNauticalMile;
    }

    /**
     * Ranks the competitors by their average corrected velocity made good, determined by the following formula:
     * 
     * <pre>
     * t_i * f_i / d_i - g_i
     * </pre>
     * 
     * where <code>t_i</code> is the time sailed by competitor i, <code>f_i</code> is the time-on-time factor for
     * competitor i, <code>d_i</code> is the (windward/along-course) distance traveled by competitor i, <code>g_i</code>
     * is the time-on-distance allowance (provided as time per distance) and <code>d</code> is the total windward /
     * along-course distance of the {@link #getTrackedRace() race's} course.
     */
    @Override
    public Comparator<Competitor> getRaceRankingComparator(TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        final RankingMetric.RankingInfo rankingInfo = getRankingInfo(timePoint, cache);
        final Comparator<Duration> durationComparatorNullsLast = Comparator.nullsLast(Comparator.naturalOrder());
        return (c1, c2) -> durationComparatorNullsLast.compare(rankingInfo.getCompetitorRankingInfo().apply(c1).getCorrectedTimeAtEstimatedArrivalAtCompetitorFarthestAhead(),
                rankingInfo.getCompetitorRankingInfo().apply(c2).getCorrectedTimeAtEstimatedArrivalAtCompetitorFarthestAhead());
    }

    @Override
    public Comparator<TrackedLegOfCompetitor> getLegRankingComparator(TrackedLeg trackedLeg, TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        // competitors that have not yet started the leg will get a duration based on Long.MAX_VALUE
        final Map<Competitor, Duration> correctedTimesToReachFastestBoatsPositionAtTimePointOrEndOfLegMeasuredFromStartOfRace = new HashMap<>();
        final Competitor fastestCompetitorInLeg = getCompetitorFarthestAheadInLeg(trackedLeg, timePoint, cache);
        if (fastestCompetitorInLeg != null) {
            final TrackedLegOfCompetitor trackedLegOfFastestCompetitorInLeg = trackedLeg.getTrackedLeg(fastestCompetitorInLeg);
            final Distance totalWindwardDistanceLegLeaderTraveledUpToTimePointOrLegEnd;
            final TimePoint startOfRace = getTrackedRace().getStartOfRace();
            final Position positionOfFastestBoatInLegAtTimePointOrLegEnd;
            if (trackedLegOfFastestCompetitorInLeg.hasFinishedLeg(timePoint)) {
                positionOfFastestBoatInLegAtTimePointOrLegEnd = getTrackedRace().getApproximatePosition(
                        trackedLeg.getLeg().getTo(), timePoint);
                Distance totalWindwardDistanceIncludingCompleteLeg = Distance.NULL;
                final Course course = getTrackedRace().getRace().getCourse();
                course.lockForRead();
                try {
                    for (TrackedLeg tl : getTrackedRace().getTrackedLegs()) {
                        totalWindwardDistanceIncludingCompleteLeg = totalWindwardDistanceIncludingCompleteLeg.add(tl
                                .getWindwardDistance());
                        if (tl == trackedLeg) {
                            break;
                        }
                    }
                } finally {
                    course.unlockAfterRead();
                }
                totalWindwardDistanceLegLeaderTraveledUpToTimePointOrLegEnd = totalWindwardDistanceIncludingCompleteLeg;
            } else {
                positionOfFastestBoatInLegAtTimePointOrLegEnd = getTrackedRace().getTrack(fastestCompetitorInLeg)
                        .getEstimatedPosition(timePoint, /* extrapolate */true);
                totalWindwardDistanceLegLeaderTraveledUpToTimePointOrLegEnd = getWindwardDistanceTraveled(
                        fastestCompetitorInLeg, timePoint, cache);
            }
            for (Competitor competitor : getTrackedRace().getRace().getCompetitors()) {
                final TrackedLegOfCompetitor currentLeg = getTrackedRace().getCurrentLeg(competitor, timePoint);
                final Duration correctedTime;
                if (currentLeg != null && currentLeg.hasStartedLeg(timePoint)) {
                    final Duration timeToReachFastest = getPredictedDurationToEndOfLegOrTo(competitor,
                            fastestCompetitorInLeg, timePoint, currentLeg, trackedLegOfFastestCompetitorInLeg, cache);
                    final Duration totalDurationSinceRaceStart = startOfRace.until(timePoint).plus(timeToReachFastest);
                    correctedTime = getCorrectedTime(competitor, () -> trackedLeg.getLeg(),
                            () -> positionOfFastestBoatInLegAtTimePointOrLegEnd, totalDurationSinceRaceStart,
                            totalWindwardDistanceLegLeaderTraveledUpToTimePointOrLegEnd);
                } else { // competitor hasn't started the leg yet; they all get MAX_VALUE as the corrected duration,
                         // hence comparing equal to each other
                    // and greater than all competitors who have already started the leg
                    correctedTime = new MillisecondsDurationImpl(Long.MAX_VALUE);
                }
                correctedTimesToReachFastestBoatsPositionAtTimePointOrEndOfLegMeasuredFromStartOfRace.put(competitor,
                        correctedTime);
            }
        }
        return (tloc1, tloc2) -> fastestCompetitorInLeg==null ? 0 :
            correctedTimesToReachFastestBoatsPositionAtTimePointOrEndOfLegMeasuredFromStartOfRace.get(tloc1.getCompetitor()).
                compareTo(correctedTimesToReachFastestBoatsPositionAtTimePointOrEndOfLegMeasuredFromStartOfRace.get(tloc2.getCompetitor()));
    }

    /**
     * @return <code>null</code> if no competitor has started the leg yet; the first competitor to finish the leg if any
     *         has already finished the leg at <code>timePoint</code>; or the competitor with the greatest windward
     *         distance traveled in the leg at <code>timePoint</code> otherwise
     */
    private Competitor getCompetitorFarthestAheadInLeg(TrackedLeg trackedLeg, TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        Iterable<MarkPassing> markPassingsForLegEnd = getTrackedRace().getMarkPassingsInOrder(trackedLeg.getLeg().getTo());
        Competitor firstAroundMark = null;
        getTrackedRace().lockForRead(markPassingsForLegEnd);
        try {
            final Iterator<MarkPassing> i = markPassingsForLegEnd.iterator();
            if (i.hasNext()) {
                MarkPassing markPassing = i.next();
                if (!markPassing.getTimePoint().after(timePoint)) {
                    firstAroundMark = markPassing.getCompetitor();
                }
            }
        } finally {
            getTrackedRace().unlockAfterRead(markPassingsForLegEnd);
        }
        final Competitor result;
        if (firstAroundMark != null) {
            result = firstAroundMark;
        } else {
            Iterable<MarkPassing> markPassingsForLegStart = getTrackedRace().getMarkPassingsInOrder(trackedLeg.getLeg().getFrom());
            Distance maxWindwardDistanceTraveled = new MeterDistance(Double.MIN_VALUE);
            Competitor competitorFarthestAlong = null;
            getTrackedRace().lockForRead(markPassingsForLegStart);
            try {
                for (MarkPassing mp : markPassingsForLegStart) {
                    if (mp.getTimePoint().after(timePoint)) {
                        break;
                    }
                    final Distance windwardDistanceTraveled = getWindwardDistanceTraveled(mp.getCompetitor(), mp.getWaypoint(),
                            timePoint, cache);
                    if (windwardDistanceTraveled.compareTo(maxWindwardDistanceTraveled) > 0) {
                        maxWindwardDistanceTraveled = windwardDistanceTraveled;
                        competitorFarthestAlong = mp.getCompetitor();
                    }
                }
            } finally {
                getTrackedRace().unlockAfterRead(markPassingsForLegStart);
            }
            result = competitorFarthestAlong;
        }
        return result;
    }

    @Override
    public Duration getCorrectedTime(Competitor competitor, TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        final Duration timeActuallySpent = getActualTimeSinceStartOfRace(competitor, timePoint);
        final Distance windwardDistanceSailed = getWindwardDistanceTraveled(competitor, timePoint, cache);
        return getCorrectedTime(competitor, ()->getTrackedRace().getCurrentLeg(competitor, timePoint).getLeg(),
                ()->getTrackedRace().getTrack(competitor).getEstimatedPosition(timePoint, /* extrapolate */true),
                timeActuallySpent, windwardDistanceSailed);
    }

    double getTimeOnTimeFactor(Competitor competitor) {
        return timeOnTimeFactor.apply(competitor);
    }

    Duration getTimeOnDistanceFactorInSecondsPerNauticalMile(Competitor competitor) {
        return timeOnDistanceFactorNauticalMile.apply(competitor);
    }

    @Override
    protected Duration getCorrectedTime(Competitor who, Supplier<Leg> leg, Supplier<Position> estimatedPosition,
            Duration totalDurationSinceRaceStart, Distance totalWindwardDistanceTraveled) {
        final Duration timeOnDistanceFactorInSecondsPerNauticalMile = getTimeOnDistanceFactorInSecondsPerNauticalMile(who);
        return totalDurationSinceRaceStart == null ? null :
            totalDurationSinceRaceStart.times(getTimeOnTimeFactor(who)).minus(
                totalWindwardDistanceTraveled == null || timeOnDistanceFactorInSecondsPerNauticalMile == null ? Duration.NULL :
                    timeOnDistanceFactorInSecondsPerNauticalMile.times(totalWindwardDistanceTraveled.getNauticalMiles()));
    }

    /**
     * Equal performance is defined here to mean equal reciproke VMG as determined by the formula
     * <pre>vmgc_i := t_i * f_i / d_i - g_i</pre> that, when equating it for <code>who</code> and <code>to</code> yields:
     * <pre>t_who * f_who / d_who - g_who = t_to * f_to / d_to - g_to</pre> Resolving for <code>t_who</code> gives:
     * <pre>t_who = (t_to * f_to / d_to - g_to + g_who) * d_who / f_who</pre> Furthermore, we have <code>d_who==d_to</code>
     * because we want to know how long <code>who</code> would take for that same distance under performance equal to
     * that of <code>to</code>.
     */
    @Override
    protected Duration getDurationToReachAtEqualPerformance(Competitor who, Competitor to, Waypoint fromWaypoint, TimePoint timePointOfTosPosition, WindLegTypeAndLegBearingCache cache) {
        final MarkPassing whenToPassedFromWaypoint = getTrackedRace().getMarkPassing(to, fromWaypoint);
        validateGetDurationToReachAtEqualPerformanceParameters(to, fromWaypoint, timePointOfTosPosition, whenToPassedFromWaypoint);
        final Duration t_to = whenToPassedFromWaypoint.getTimePoint().until(timePointOfTosPosition);
        final Distance d_to = getWindwardDistanceTraveled(to, fromWaypoint, timePointOfTosPosition, cache);
        final double   f_to = getTimeOnTimeFactor(to);
        final Duration timeOnDistanceFactorInSecondsPerNauticalMileTo = getTimeOnDistanceFactorInSecondsPerNauticalMile(to);
        final double   g_to = timeOnDistanceFactorInSecondsPerNauticalMileTo==null?0:timeOnDistanceFactorInSecondsPerNauticalMileTo.asSeconds();
        final Distance d_who = d_to;
        final double   f_who = getTimeOnTimeFactor(who);
        final Duration timeOnDistanceFactorInSecondsPerNauticalMileWho = getTimeOnDistanceFactorInSecondsPerNauticalMile(who);
        final double   g_who = timeOnDistanceFactorInSecondsPerNauticalMileWho==null?0:timeOnDistanceFactorInSecondsPerNauticalMileWho.asSeconds();
        
        final Duration t_who = new MillisecondsDurationImpl(Double.valueOf(
                (1./d_to.inTime(t_to.times(f_to)).getMetersPerSecond() / Mile.METERS_PER_NAUTICAL_MILE - g_to + g_who)
                              * d_who.getNauticalMiles() / f_who * 1000.).longValue());
        return t_who;
    }

    /**
     * For the {@link RankingInfo#getTimePoint() ranking info's time point} compares <code>competitor</code> to
     * {@link RankingInfo#getLeaderByCorrectedEstimatedTimeToCompetitorFarthestAhead()}. Based on the definition of
     * "leader" the corrected estimated time for <code>competitor</code> when reaching the fastest boat's position at
     * {@link #timePoint} is expected to be greater than that of the leader. We're equating <code>competitor</code>'s
     * and the leader's corrected time when reaching the fastest boat's position at {@link #timePoint}, assuming a
     * summand in <code>competitor</code>'s actual time required to reach the fastest boat's position. This equation can
     * then be resolved for this additional summand (which is a negative duration), telling in <code>competitor</code>'s
     * own time how much time she would have to make good to rank equal to the leader.
     * <p>
     * 
     * The math behind this works as follows. Let <code>i</code> represent the <code>competitor</code>, <code>k</code>
     * the leader, <code>d</code> the total windward distance from the start to the fastest competitor's position at
     * {@link RankingInfo#getTimePoint() time point provided by the ranking info}. Then we have for the corrected
     * reciproke average corrected VMGs:
     * 
     * <pre>
     * t_i * f_i - d * g_i - diff_corr_t_i = t_k * f_k - d * g_k
     * </pre>
     * 
     * where <code>t_i / t_k</code> is the actual duration from the race start until competitor <code>i / k</code> (or
     * <code>competitor</code> and the leader, respectively) reaches the fastest competitor's position at
     * {@link #timePoint}. The <code>diff_corr_t_i</code> is the sorting criterion for ranking because corrected time is
     * (also for Performance Curve after mapping implied wind to corrected times through the use of a scratch boat) the
     * basis for ranking. But we would additionally like to understand what this difference means in <code>i</code>'s
     * own time, so we introduce <code>diff_t_i</code> as follows:
     * 
     * <pre>
     * (t_i - diff_t_i) * f_i - d * g_i = t_k * f_k - d * g_k
     * </pre>
     * 
     * which resolves to
     * 
     * <pre>
     * <b>diff_t_i</b> = t_i - (t_k * f_k + d * (g_i - g_k)) / f_i
     * </pre>
     */
    @Override
    public Duration getGapToLeaderInOwnTime(RankingMetric.RankingInfo rankingInfo, Competitor competitor, WindLegTypeAndLegBearingCache cache) {
        final Duration t_k = rankingInfo.getCompetitorRankingInfo().apply(rankingInfo.getLeaderByCorrectedEstimatedTimeToCompetitorFarthestAhead()).
                getEstimatedActualDurationFromRaceStartToCompetitorFarthestAhead();
        final double   f_k = getTimeOnTimeFactor(rankingInfo.getLeaderByCorrectedEstimatedTimeToCompetitorFarthestAhead());
        final Duration g_k = getTimeOnDistanceFactorInSecondsPerNauticalMile(rankingInfo.getLeaderByCorrectedEstimatedTimeToCompetitorFarthestAhead());
        final Duration t_i = rankingInfo.getCompetitorRankingInfo().apply(competitor).getEstimatedActualDurationFromRaceStartToCompetitorFarthestAhead();
        final double   f_i = getTimeOnTimeFactor(competitor);
        final Duration g_i = getTimeOnDistanceFactorInSecondsPerNauticalMile(competitor);
        final Distance d   = rankingInfo.getCompetitorRankingInfo().apply(rankingInfo.getCompetitorFarthestAhead()).getWindwardDistanceSailed();
        
        final Duration diff_t_i;
        if (t_i == null || t_k == null || d == null) {
            diff_t_i = null;
        } else {
            diff_t_i = t_i.minus(t_k.times(f_k).plus(((g_i==null?Duration.NULL:g_i).minus((g_k==null?Duration.NULL:g_k))).times(d.getNauticalMiles())).divide(f_i));
        }
        return diff_t_i;
    }

}
