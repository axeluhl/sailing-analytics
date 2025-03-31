package com.sap.sailing.server.statistics;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.statistics.Statistics;
import com.sap.sailing.domain.statistics.impl.StatisticsImpl;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Speed;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Triple;

/**
 * Aggregates local and remote {@link Statistics} to get the overall statistics values.
 */
public class StatisticsAggregator {

    private int competitors = 0;
    private int races = 0;
    private int trackedRaces = 0;
    private int regattas = 0;
    private long numberOfGPSFixes = 0l;
    private long numberOfWindFixes = 0l;
    private Distance distanceTraveled = Distance.NULL;
    private Triple<Competitor, Speed, TimePoint> maxSpeed = null;

    public void addStatistics(Statistics remoteStatisticsForYear) {
        competitors += remoteStatisticsForYear.getNumberOfCompetitors();
        races += remoteStatisticsForYear.getNumberOfRaces();
        trackedRaces += remoteStatisticsForYear.getNumberOfTrackedRaces();
        regattas += remoteStatisticsForYear.getNumberOfRegattas();
        numberOfGPSFixes += remoteStatisticsForYear.getNumberOfGPSFixes();
        numberOfWindFixes += remoteStatisticsForYear.getNumberOfWindFixes();
        distanceTraveled = distanceTraveled.add(remoteStatisticsForYear.getDistanceTraveled());
        
        final Triple<Competitor, Speed, TimePoint> maxSpeedFromStatisticsToAdd = remoteStatisticsForYear.getMaxSpeed();
        if (maxSpeedFromStatisticsToAdd != null
                && (maxSpeed == null || maxSpeedFromStatisticsToAdd.getB().compareTo(maxSpeed.getB()) > 0)) {
            maxSpeed = maxSpeedFromStatisticsToAdd;
        }
    }

    public Statistics getStatistics() {
        return new StatisticsImpl(competitors, regattas, races, trackedRaces, numberOfGPSFixes, numberOfWindFixes,
                distanceTraveled, maxSpeed);
    }
}
