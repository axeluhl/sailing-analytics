package com.sap.sailing.domain.ranking;

import java.util.Comparator;
import java.util.function.Function;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Mile;
import com.sap.sailing.domain.common.Position;
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

    private final Function<Competitor, Double> timeOnTimeFactor;

    private final Function<Competitor, Double> timeOnDistanceFactorInSecondsPerNauticalMile;

    public TimeOnTimeAndDistanceRankingMetric(TrackedRace trackedRace, Function<Competitor, Double> timeOnTimeFactor,
            Function<Competitor, Double> timeOnDistanceFactorInSecondsPerNauticalMile) {
        super(trackedRace);
        this.timeOnTimeFactor = timeOnTimeFactor;
        this.timeOnDistanceFactorInSecondsPerNauticalMile = timeOnDistanceFactorInSecondsPerNauticalMile;
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
        return (c1, c2) -> Double.valueOf(getAverageCorrectedReciprokeVMGAsSecondsPerNauticalMile(c1, timePoint)).compareTo(
                Double.valueOf(getAverageCorrectedReciprokeVMGAsSecondsPerNauticalMile(c2, timePoint)));
    }

    protected double getAverageCorrectedReciprokeVMGAsSecondsPerNauticalMile(Competitor competitor, TimePoint timePoint) {
        final double tSec = getActualTimeSinceStartOfRace(competitor, timePoint).asSeconds();
        final double diNM = getWindwardDistanceTraveled(competitor, timePoint).getNauticalMiles();
        final double gi = getTimeOnDistanceFactorInSecondsPerNauticalMile(competitor);
        final double fi = getTimeOnTimeFactor(competitor);
        return tSec * fi / diNM - gi;
    }

    @Override
    public Comparator<TrackedLegOfCompetitor> getLegRankingComparator(TrackedLeg trackedLeg, TimePoint timePoint,
            WindLegTypeAndLegBearingCache cache) {
        return (tloc1, tloc2) -> getAverageCorrectedReciprokeVMGAsSecondsPerNauticalMile(tloc1, timePoint).compareTo(
                getAverageCorrectedReciprokeVMGAsSecondsPerNauticalMile(tloc2, timePoint));
    }

    private Double getAverageCorrectedReciprokeVMGAsSecondsPerNauticalMile(TrackedLegOfCompetitor tloc, TimePoint timePoint) {
        final TimePoint at = tloc.getTimePointNotAfterFinishingOfLeg(timePoint);
        final Double result;
        if (at == null) {
            result = null;
        } else {
            result = getAverageCorrectedReciprokeVMGAsSecondsPerNauticalMile(tloc.getCompetitor(), at);
        }
        return result;
    }

    @Override
    public Duration getTimeToImprove(Competitor trailing, Competitor leading, TimePoint timePoint,
            WindLegTypeAndLegBearingCache cache) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Duration getCorrectedTime(Competitor competitor, TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        final Duration timeActuallySpent = getActualTimeSinceStartOfRace(competitor, timePoint);
        final Duration result;
        if (timeActuallySpent == null) {
            result = null;
        } else {
            final Distance windwardDistanceSailed = getWindwardDistanceTraveled(competitor, timePoint);
            final Duration correctedTimeByTimeOnTime = timeActuallySpent.times(getTimeOnTimeFactor(competitor));
            if (windwardDistanceSailed == null) {
                result = correctedTimeByTimeOnTime;
            } else {
                final Duration timeOnDistanceAllowance = Duration.ONE_SECOND.times(windwardDistanceSailed.getNauticalMiles()
                                * getTimeOnDistanceFactorInSecondsPerNauticalMile(competitor));
                result = correctedTimeByTimeOnTime.minus(timeOnDistanceAllowance);
            }
        }
        return result;
    }

    private double getTimeOnTimeFactor(Competitor competitor) {
        return timeOnTimeFactor.apply(competitor);
    }

    private double getTimeOnDistanceFactorInSecondsPerNauticalMile(Competitor competitor) {
        return timeOnDistanceFactorInSecondsPerNauticalMile.apply(competitor);
    }

    /**
     * Equal performance is defined here to mean equal reciproke VMG as determined by the formula
     * <pre>vmgc_i := t_i * f_i / d_i - g_i</pre> that, when equating it for <code>who</code> and <code>to</code> yields:
     * <pre>t_who * f_who / d_who - g_who = t_to * f_to / d_to - g_to</pre> Resolving to <code>t_who</code> gives:
     * <pre>t_who = (t_to * f_to / d_to - g_to + g_who) * d_who / f_who</pre> Furthermore, we have <code>d_who==d_to</code>
     * because we want to know how long <code>who</code> would take for that same distance under performance equal to
     * that of <code>to</code>.
     */
    @Override
    protected Duration getDurationToReachAtEqualPerformance(Competitor who, Competitor to, Waypoint fromWaypoint,
            TimePoint whenWhoIsAtFromWaypoint, TimePoint timePointOfTosPosition) {
        final MarkPassing whenToPassedFromWaypoint = getTrackedRace().getMarkPassing(to, fromWaypoint);
        if (whenToPassedFromWaypoint == null) {
            throw new IllegalArgumentException("Competitor "+to+" is expected to have passed "+fromWaypoint+" but hasn't");
        }
        if (whenToPassedFromWaypoint.getTimePoint().after(timePointOfTosPosition)) {
            throw new IllegalArgumentException("Competitor was expected to have passed "+fromWaypoint+" before "+timePointOfTosPosition+
                    " but did pass it at "+whenToPassedFromWaypoint.getTimePoint());
        }
        final Duration t_to = whenToPassedFromWaypoint.getTimePoint().until(timePointOfTosPosition);
        final Distance d_to = getWindwardDistanceTraveled(to, fromWaypoint, timePointOfTosPosition);
        final double   f_to = getTimeOnTimeFactor(to);
        final double   g_to = getTimeOnDistanceFactorInSecondsPerNauticalMile(to);
        final Distance d_who = d_to;
        final double   f_who = getTimeOnTimeFactor(who);
        final double   g_who = getTimeOnDistanceFactorInSecondsPerNauticalMile(who);
        
        final Duration t_who = new MillisecondsDurationImpl(Double.valueOf(
                (1./d_to.inTime(t_to.times(f_to)).getMetersPerSecond() / Mile.METERS_PER_NAUTICAL_MILE - g_to + g_who)
                              * d_who.getNauticalMiles() / f_who * 1000.).longValue());
        return t_who;
    }

    @Override
    protected Duration getCorrectedTime(Competitor who, Leg leg, Position estimatedPosition,
            Duration totalDurationSinceRaceStart, Distance totalWindwardDistanceTraveled) {
        return totalDurationSinceRaceStart.times(getTimeOnTimeFactor(who)).minus(
                new MillisecondsDurationImpl((long) (1000. *
                        getTimeOnDistanceFactorInSecondsPerNauticalMile(who) * totalWindwardDistanceTraveled.getNauticalMiles())));
    }

}
