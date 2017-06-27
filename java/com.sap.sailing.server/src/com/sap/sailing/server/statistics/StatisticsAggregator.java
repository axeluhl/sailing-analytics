package com.sap.sailing.server.statistics;

import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.statistics.Statistics;
import com.sap.sailing.domain.statistics.impl.StatisticsImpl;

public class StatisticsAggregator {
    
    private int competitors = 0;
    private int races = 0;
    private int trackedRaces = 0;
    private int regattas = 0;
    private long numberOfGPSFixes = 0l;
    private long numberOfWindFixes = 0l;
    private Distance distanceTraveled = Distance.NULL;

    public void addStatistics(Statistics remoteStatisticsForYear) {
        competitors += remoteStatisticsForYear.getNumberOfCompetitors();
        races += remoteStatisticsForYear.getNumberOfRaces();
        trackedRaces += remoteStatisticsForYear.getNumberOfTrackedRaces();
        regattas += remoteStatisticsForYear.getNumberOfRegattas();
        numberOfGPSFixes += remoteStatisticsForYear.getNumberOfGPSFixes();
        numberOfWindFixes += remoteStatisticsForYear.getNumberOfWindFixes();
        distanceTraveled = distanceTraveled.add(remoteStatisticsForYear.getDistanceTraveled());
    }
    
    public Statistics getStatistics() {
        return new StatisticsImpl(competitors, regattas, races, trackedRaces, numberOfGPSFixes,
                numberOfWindFixes, distanceTraveled);
    }
}
