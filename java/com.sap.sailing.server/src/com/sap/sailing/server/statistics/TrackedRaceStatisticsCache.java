package com.sap.sailing.server.statistics;

import com.sap.sailing.domain.tracking.TrackedRace;

public interface TrackedRaceStatisticsCache {
    
    TrackedRaceStatistics getStatistics(TrackedRace trackedRace);
}
