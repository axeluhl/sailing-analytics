
package com.sap.sailing.domain.ranking;

import java.util.Comparator;
import java.util.function.Function;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingCache;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;

public class TimeOnTimeAndDistanceRankingMetric extends AbstractRankingMetric {
    private static final long serialVersionUID = -2321904208518686420L;
    
    private final Function<Competitor, Double> timeOnTimeFactor;

    private final Function<Competitor, Double> timeOnDistanceFactorInSecondsPerNauticalMile;

    public TimeOnTimeAndDistanceRankingMetric(TrackedRace trackedRace,
            Function<Competitor, Double> timeOnTimeFactor,
            Function<Competitor, Double> timeOnDistanceFactorInSecondsPerNauticalMile) {
        super(trackedRace);
        this.timeOnTimeFactor = timeOnTimeFactor;
        this.timeOnDistanceFactorInSecondsPerNauticalMile = timeOnDistanceFactorInSecondsPerNauticalMile;
    }

    /**
     * Ranks the competitors by their average corrected velocity made good, determined by the following formula:<pre>
     *                  t_i * f_i / d_i - d_i * g_i / d</pre>
     * where <code>t_i</code> is the time sailed by competitor i, <code>f_i</code> is the time-on-time factor for
     * competitor i, <code>d_i</code> is the (windward/along-course) distance traveled by competitor i, <code>g_i</code>
     * is the time-on-distance allowance (provided as time per distance) and <code>d</code> is the total windward /
     * along-course distance of the {@link #getTrackedRace() race's} course.
     */
    @Override
    public Comparator<Competitor> getRaceRankingComparator(TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        return (c1, c2) -> Double.valueOf(getAverageCorrectedVMGAsSecondsPerNauticalMile(c1, timePoint)).compareTo(
                           Double.valueOf(getAverageCorrectedVMGAsSecondsPerNauticalMile(c2, timePoint)));
    }

    double getAverageCorrectedVMGAsSecondsPerNauticalMile(Competitor competitor, TimePoint timePoint) {
        final double tSec = getActualTimeSinceStartOfRace(competitor, timePoint).asSeconds();
        final double diNM = getWindwardDistanceTraveled(competitor, timePoint).getNauticalMiles();
        final double dNM = getTrackedRace().getCourseLength().getNauticalMiles();
        final double gi = getTimeOnDistanceFactorInSecondsPerNauticalMile(competitor);
        final double fi = getTimeOnTimeFactor(competitor);
        return tSec * fi / diNM - diNM * gi / dNM;
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
        final Duration timeActuallySpent = getActualTimeSinceStartOfRace(competitor, timePoint);
        final Duration result;
        if (timeActuallySpent == null) {
            result = null;
        } else {
            final Distance windwardDistanceSailed = getWindwardDistanceTraveled(competitor, timePoint);
            final Duration correctedTimeByTimeOnTime = timeActuallySpent.times(Double.valueOf(getTimeOnTimeFactor(competitor)).longValue());
            if (windwardDistanceSailed == null) {
                result = correctedTimeByTimeOnTime;
            } else {
                final Duration timeOnDistanceAllowance = Duration.ONE_SECOND.times(Double.valueOf(
                        windwardDistanceSailed.getNauticalMiles() * getTimeOnDistanceFactorInSecondsPerNauticalMile(competitor)).
                        longValue());
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

}
