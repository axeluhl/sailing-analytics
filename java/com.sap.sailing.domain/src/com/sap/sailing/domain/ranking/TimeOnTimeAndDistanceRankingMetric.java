
package com.sap.sailing.domain.ranking;

import java.util.Comparator;

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

    public TimeOnTimeAndDistanceRankingMetric(TrackedRace trackedRace) {
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
        final Duration timeActuallySpent = getActualTimeSinceStartOfRace(competitor, timePoint);
        final Duration result;
        if (timeActuallySpent == null) {
            result = null;
        } else {
            final Distance windwardDistanceSailed = getWindwardDistanceTraveled(competitor, timePoint);
            final Duration correctedTimeByTimeOnTime = timeActuallySpent.times(Double.valueOf(getTimeOnTimeFactor(competitor, timePoint)).longValue());
            if (windwardDistanceSailed == null) {
                result = correctedTimeByTimeOnTime;
            } else {
                final Duration timeOnDistanceAllowance = Duration.ONE_SECOND.times(Double.valueOf(
                        windwardDistanceSailed.getNauticalMiles() * getTimeOnDistanceFactorInSecondsPerNauticalMile(competitor, timePoint)).
                        longValue());
                result = correctedTimeByTimeOnTime.minus(timeOnDistanceAllowance);
            }
        }
        return result;
    }
    
    private double getTimeOnTimeFactor(Competitor competitor, TimePoint timePoint) {
        return 1.0; // TODO see bug 2765; obtain this from the Competitor
    }

    private double getTimeOnDistanceFactorInSecondsPerNauticalMile(Competitor competitor, TimePoint timePoint) {
        return 0.0; // TODO see bug 2765; obtain this from the Competitor
    }

}
