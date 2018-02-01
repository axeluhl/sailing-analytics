package com.sap.sailing.server.statistics;

import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * A cache that holds and updates {@link TrackedRaceStatistics} for all known {@link TrackedRace}s.
 */
public interface TrackedRaceStatisticsCache {
    
    TrackedRaceStatistics getStatistics(TrackedRace trackedRace);
}
