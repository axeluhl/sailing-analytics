package com.sap.sailing.domain.statistics.impl;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.statistics.Statistics;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Speed;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Triple;

public class StatisticsImpl implements Statistics {

    private final int numberOfCompetitors;
    private final int numberOfRegattas;
    private final int numberOfRaces;
    private final int numberOfTrackedRaces;
    private final long numberOfGPSFixes;
    private final long numberOfWindFixes;
    private final Distance distanceTraveled;
    private final Triple<Competitor, Speed, TimePoint> maxSpeed;
    
    /**
     * @param distanceTraveled
     *            if {@code null} then {@link #getDistanceTraveled()} will return {@link Distance#NULL} instead; this
     *            way, the distance reported by this object can easily be aggregated with other distances without
     *            further {@code null} checks.
     */
    public StatisticsImpl(int numberOfCompetitors, int numberOfRegattas, int numberOfRaces, int numberOfTrackedRaces,
            long numberOfGPSFixes, long numberOfWindFixes, Distance distanceTraveled, Triple<Competitor, Speed, TimePoint> maxSpeed) {
        super();
        this.numberOfCompetitors = numberOfCompetitors;
        this.numberOfRegattas = numberOfRegattas;
        this.numberOfRaces = numberOfRaces;
        this.numberOfTrackedRaces = numberOfTrackedRaces;
        this.numberOfGPSFixes = numberOfGPSFixes;
        this.numberOfWindFixes = numberOfWindFixes;
        this.distanceTraveled = distanceTraveled == null ? Distance.NULL : distanceTraveled;
        this.maxSpeed = maxSpeed;
    }

    @Override
    public int getNumberOfCompetitors() {
        return numberOfCompetitors;
    }

    @Override
    public int getNumberOfRegattas() {
        return numberOfRegattas;
    }

    @Override
    public int getNumberOfRaces() {
        return numberOfRaces;
    }

    @Override
    public int getNumberOfTrackedRaces() {
        return numberOfTrackedRaces;
    }

    @Override
    public long getNumberOfGPSFixes() {
        return numberOfGPSFixes;
    }

    @Override
    public long getNumberOfWindFixes() {
        return numberOfWindFixes;
    }

    @Override
    public Distance getDistanceTraveled() {
        return distanceTraveled;
    }
    
    public Triple<Competitor, Speed, TimePoint> getMaxSpeed() {
        return maxSpeed;
    }
}
