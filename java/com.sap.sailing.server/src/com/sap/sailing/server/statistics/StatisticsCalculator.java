package com.sap.sailing.server.statistics;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.statistics.Statistics;
import com.sap.sailing.domain.statistics.impl.StatisticsImpl;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Speed;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * Calculates the statistics for all {@link Leaderboard}s passed to the {@link #addLeaderboard(Leaderboard)} method.<br>
 * Due to the fact that {@link TrackedRaceStatisticsCache} is managed by OSGi, it's possible that no instance is
 * available at a given point in time. In this case, no detailed statistics based on the information of a TrackedRace
 * can be calculated.
 */
public class StatisticsCalculator {

    private final TimePoint now = MillisecondsTimePoint.now();
    private Set<Competitor> competitors = new HashSet<>();
    private Set<Pair<RaceColumn, Fleet>> races = new HashSet<>();
    private Set<TrackedRace> trackedRaces = new HashSet<>();
    private Set<String> regattas = new HashSet<>();
    private long numberOfGPSFixes, numberOfWindFixes;
    private Distance totalDistanceTraveled = Distance.NULL;
    private Triple<Competitor, Speed, TimePoint> maxSpeed = null;

    private final TrackedRaceStatisticsCache trackedRaceStatisticsCache;

    /**
     * @param trackedRaceStatisticsCache
     *            the {@link TrackedRaceStatisticsCache} instance to use or {@code null} if none is available
     */
    public StatisticsCalculator(TrackedRaceStatisticsCache trackedRaceStatisticsCache) {
        this.trackedRaceStatisticsCache = trackedRaceStatisticsCache;
    }

    private Set<Pair<RaceColumn, Fleet>> calculateRaces(Leaderboard sl) {
        final Set<Pair<RaceColumn, Fleet>> result = new HashSet<>();
        for (RaceColumn column : sl.getRaceColumns()) {
            if (!column.isCarryForward()) {
                column.getFleets().forEach(fleet -> result.add(new Pair<>(column, fleet)));
            }
        }
        return result;
    }

    public void addLeaderboard(Leaderboard leaderboard) {
        if (regattas.add(leaderboard.getName())) {
            races.addAll(calculateRaces(leaderboard));
            Util.addAll(leaderboard.getCompetitors(), competitors);
            for (RaceColumn column : leaderboard.getRaceColumns()) {
                for (Fleet fleet : column.getFleets()) {
                    final TrackedRace trackedRace = column.getTrackedRace(fleet);
                    if (trackedRace != null && !trackedRaces.contains(trackedRace) && trackedRace.hasGPSData()) {
                        TimePoint startOfRace = trackedRace.getStartOfRace();
                        if(startOfRace != null && startOfRace.before(now)) {
                            trackedRaces.add(trackedRace);
                            doForTrackedRace(trackedRace);
                        }
                    }
                }
            }
        }
    }

    private void doForTrackedRace(TrackedRace trackedRace) {
        if (trackedRaceStatisticsCache != null) {
            final TrackedRaceStatistics trackedRaceStatistics = trackedRaceStatisticsCache.getStatistics(trackedRace);
            if (trackedRaceStatistics != null) {
                numberOfGPSFixes += trackedRaceStatistics.getNumberOfGPSFixes();
                numberOfWindFixes += trackedRaceStatistics.getNumberOfWindFixes();
                totalDistanceTraveled = totalDistanceTraveled.add(trackedRaceStatistics.getDistanceTraveled());
                
                Triple<Competitor, Speed, TimePoint> maxSpeedForRace = trackedRaceStatistics.getMaxSpeed();
                if (maxSpeedForRace != null
                        && (maxSpeed == null || maxSpeedForRace.getB().compareTo(maxSpeed.getB()) > 0)
                        // only accept "reasonable" max speeds
                        && maxSpeedForRace.getB().compareTo(GPSFixTrack.DEFAULT_MAX_SPEED_FOR_SMOOTHING) <= 0) {
                    maxSpeed = maxSpeedForRace;
                }
            }
        }
    }

    public int getNumberOfRaces() {
        return races.size();
    }

    public int getNumberOfTrackedRaces() {
        return trackedRaces.size();
    }

    public int getNumberOfRegattas() {
        return regattas.size();
    }

    public long getNumberOfGPSFixes() {
        return numberOfGPSFixes;
    }

    public long getNumberOfWindFixes() {
        return numberOfWindFixes;
    }

    public Distance getTotalDistanceTraveled() {
        return totalDistanceTraveled;
    }
    
    public Triple<Competitor, Speed, TimePoint> getMaxSpeed() {
        return maxSpeed;
    }

    public int getNumberOfCompetitors() {
        return competitors.size();
    }
    
    public Statistics getStatistics() {
        return new StatisticsImpl(getNumberOfCompetitors(), getNumberOfRegattas(),
                getNumberOfRaces(), getNumberOfTrackedRaces(),
                getNumberOfGPSFixes(), getNumberOfWindFixes(),
                getTotalDistanceTraveled(), getMaxSpeed());
    }
}
