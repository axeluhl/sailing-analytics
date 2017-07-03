package com.sap.sailing.server.statistics;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.statistics.Statistics;
import com.sap.sailing.domain.statistics.impl.StatisticsImpl;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.server.util.LeaderboardUtil;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * Calculates the statistics for the regatta(s) passed to the {@link #addLeaderboard(Leaderboard)} method. Because this
 * calculations can be costly and time consuming, several information can be disabled/enabled:
 * <ul>
 * <li>Maximum speed (of a competitor) - by constructor parameter {@link #calculateMaxSpeed} within this class</li>
 * <li>Total of sailed miles - by constructor parameter {@link #calculateDistanceTravelled} within this class</li>
 * </ul>
 */
public class StatisticsCalculator {

    private static final Logger logger = Logger.getLogger(StatisticsCalculator.class.getName());

    private final boolean calculateMaxSpeed;
    private final boolean calculateDistanceTravelled;

    private Set<Competitor> competitors = new HashSet<>();
    private Set<Pair<RaceColumn, Fleet>> races = new HashSet<>();
    private Set<TrackedRace> trackedRaces = new HashSet<>();
    private Set<String> regattas = new HashSet<>();
    private long numberOfGPSFixes, numberOfWindFixes;
    private Distance totalDistanceTraveled = Distance.NULL;
    private Triple<Competitor, Speed, TimePoint> maxSpeed = null;
    private final TimePoint now = MillisecondsTimePoint.now();

    public StatisticsCalculator() {
        this(false, true);
    }

    public StatisticsCalculator(boolean calculateMaxSpeed, boolean calculateDistanceTravelled) {
        this.calculateMaxSpeed = calculateMaxSpeed;
        this.calculateDistanceTravelled = calculateDistanceTravelled;
    }

    public void addLeaderboard(Leaderboard leaderboard) {
        races.addAll(LeaderboardUtil.calculateRaces(leaderboard));
        regattas.add(leaderboard.getName());
        final String disableStatsString = System.getenv("DISABLE_STATS");
        final boolean enableStats = disableStatsString == null || "false".equals(disableStatsString);
        
        for (RaceColumn column : leaderboard.getRaceColumns()) {
            for (Fleet fleet : column.getFleets()) {
                TrackedRace trackedRace = column.getTrackedRace(fleet);
                if (trackedRace != null && !trackedRaces.contains(trackedRace)) {
                    if (enableStats) {
                        try {
                            if(doForTrackedRace(trackedRace)) {
                                trackedRaces.add(trackedRace);
                            }
                        } catch (Exception e) {
                            logger.log(Level.WARNING, "Exception during calculation of event statistics", e);
                        }
                    } else  if (trackedRace.hasGPSData() && trackedRace.hasWindData()) {
                        trackedRaces.add(trackedRace);
                    }
                }
            }
        }
    }

    private boolean doForTrackedRace(TrackedRace trackedRace) {
        boolean foundFixes = false;
        for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
            competitors.add(competitor);
            foundFixes |= doForCompetitor(trackedRace, competitor);
        }
        for (Mark mark : trackedRace.getMarks()) {
            foundFixes |= doForMark(trackedRace, mark);
        }
        for (WindSource windSource : trackedRace.getWindSources()) {
            foundFixes |= doForWindSource(trackedRace, windSource);
        }
        return foundFixes;
    }

    private boolean doForCompetitor(TrackedRace trackedRace, Competitor competitor) {
        boolean foundFixes = false;
        GPSFixTrack<Competitor, GPSFixMoving> competitorTrack = trackedRace.getTrack(competitor);
        competitorTrack.lockForRead();
        try {
            final int numberOfGPSFixesForCompetitor = Util.size(competitorTrack.getRawFixes());
            if (numberOfGPSFixesForCompetitor > 0) {
                foundFixes = true;
                numberOfGPSFixes += numberOfGPSFixesForCompetitor;
            }
        } finally {
            competitorTrack.unlockAfterRead();
        }
        if ((calculateDistanceTravelled || calculateMaxSpeed) && trackedRace.hasStarted(now)) {
            final NavigableSet<MarkPassing> competitorMarkPassings = trackedRace.getMarkPassings(competitor);
            MarkPassing lastMarkPassingBeforeNow = null;
            trackedRace.lockForRead(competitorMarkPassings);
            try {
                MarkPassing next = null;
                Iterator<MarkPassing> i = competitorMarkPassings.descendingIterator();
                while (i.hasNext() && (next = i.next()).getTimePoint().after(now))
                    ;
                if (next != null) {
                    lastMarkPassingBeforeNow = next;
                }
            } finally {
                trackedRace.unlockAfterRead(competitorMarkPassings);
            }
            if (lastMarkPassingBeforeNow != null) {
                // competitor has started and has at least one mark passing before now; compute distance traveled up to
                // that
                // mark passing, increasing likelihood of cache hits as compared to using "now" as the query time point
                TimePoint from = trackedRace.getStartOfRace(), to = lastMarkPassingBeforeNow.getTimePoint();
                doForCompetitorTrackAndTimeRange(competitor, competitorTrack, from, to);
            }
        }
        return foundFixes;
    }

    protected void doForCompetitorTrackAndTimeRange(Competitor competitor,
            GPSFixTrack<Competitor, GPSFixMoving> competitorTrack, TimePoint from, TimePoint to) {
        if (calculateDistanceTravelled) {
            totalDistanceTraveled = totalDistanceTraveled.add(competitorTrack.getDistanceTraveled(from, to));
        }
        if (calculateMaxSpeed) {
            Pair<GPSFixMoving, Speed> competitorMaxSpeed = competitorTrack.getMaximumSpeedOverGround(from, to);
            if (competitorMaxSpeed != null
                    && (maxSpeed == null || competitorMaxSpeed.getB().compareTo(maxSpeed.getB()) > 0)
                    // only accept "reasonable" max speeds
                    && competitorMaxSpeed.getB().compareTo(GPSFixTrack.DEFAULT_MAX_SPEED_FOR_SMOOTHING) <= 0) {
                maxSpeed = new Triple<>(competitor, competitorMaxSpeed.getB(),
                        competitorMaxSpeed.getA().getTimePoint());
            }
        }
    }

    private boolean doForMark(TrackedRace trackedRace, Mark mark) {
        boolean foundFixes = false;
        GPSFixTrack<Mark, GPSFix> markTrack = trackedRace.getOrCreateTrack(mark);
        markTrack.lockForRead();
        try {
            final int numberOfGWPFixesForMark = Util.size(markTrack.getRawFixes());
            if(numberOfGWPFixesForMark > 0) {
                foundFixes = true;
                numberOfGPSFixes += numberOfGWPFixesForMark;
            }
        } finally {
            markTrack.unlockAfterRead();
        }
        return foundFixes;
    }

    private boolean doForWindSource(TrackedRace trackedRace, WindSource windSource) {
        boolean foundFixes = false;
        // don't count the "virtual" wind sources
        if (windSource.canBeStored() || windSource.getType() == WindSourceType.RACECOMMITTEE) {
            WindTrack windTrack = trackedRace.getOrCreateWindTrack(windSource);
            windTrack.lockForRead();
            try {
                final int numberOfFixesForWindSource = Util.size(windTrack.getRawFixes());
                if(numberOfFixesForWindSource > 0) {
                    foundFixes = true;
                    numberOfWindFixes += numberOfFixesForWindSource;
                }
            } finally {
                windTrack.unlockAfterRead();
            }
        }
        return foundFixes;
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
                getTotalDistanceTraveled());
    }
}
