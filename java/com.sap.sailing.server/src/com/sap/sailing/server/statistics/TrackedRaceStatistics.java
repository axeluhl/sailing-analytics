package com.sap.sailing.server.statistics;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Speed;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Triple;

/**
 * Statistics for one {@link TrackedRace} that are cached by {@link TrackedRaceStatisticsCache}.
 */
public class TrackedRaceStatistics {

    private final long numberOfGPSFixes;
    private final long numberOfWindFixes;
    private final Distance distanceTraveled;
    private final Triple<Competitor, Speed, TimePoint> maxSpeed;

    public TrackedRaceStatistics(long numberOfGPSFixes, long numberOfWindFixes, Distance distanceTraveled, Triple<Competitor, Speed, TimePoint> maxSpeed) {
        super();
        this.numberOfGPSFixes = numberOfGPSFixes;
        this.numberOfWindFixes = numberOfWindFixes;
        this.distanceTraveled = distanceTraveled;
        this.maxSpeed = maxSpeed;
    }

    public long getNumberOfGPSFixes() {
        return numberOfGPSFixes;
    }

    public long getNumberOfWindFixes() {
        return numberOfWindFixes;
    }

    public Distance getDistanceTraveled() {
        return distanceTraveled;
    }
    
    public Triple<Competitor, Speed, TimePoint> getMaxSpeed() {
        return maxSpeed;
    }
}
