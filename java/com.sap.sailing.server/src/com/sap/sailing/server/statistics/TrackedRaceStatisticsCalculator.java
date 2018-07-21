package com.sap.sailing.server.statistics;

import java.util.Iterator;
import java.util.NavigableSet;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Speed;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * Calculates the statistics for the given {@link TrackedRace}. Because this
 * calculations can be costly and time consuming, several information can be disabled/enabled:
 * <ul>
 * <li>Maximum speed (of a competitor) - by constructor parameter {@link #calculateMaxSpeed} within this class</li>
 * <li>Total of sailed miles - by constructor parameter {@link #calculateDistanceTravelled} within this class</li>
 * </ul>
 */
public class TrackedRaceStatisticsCalculator {

    private final boolean calculateMaxSpeed;
    private final boolean calculateDistanceTravelled;

    private long numberOfGPSFixes, numberOfWindFixes;
    
    /**
     * Never {@code null}
     */
    private Distance totalDistanceTraveled = Distance.NULL;
    private Triple<Competitor, Speed, TimePoint> maxSpeed = null;
    private final TimePoint now = MillisecondsTimePoint.now();

    public TrackedRaceStatisticsCalculator(TrackedRace trackedRace, boolean calculateMaxSpeed, boolean calculateDistanceTravelled) {
        this.calculateMaxSpeed = calculateMaxSpeed;
        this.calculateDistanceTravelled = calculateDistanceTravelled;
        
        for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
            doForCompetitor(trackedRace, competitor);
        }
        for (Mark mark : trackedRace.getMarks()) {
            doForMark(trackedRace, mark);
        }
        for (WindSource windSource : trackedRace.getWindSources()) {
            doForWindSource(trackedRace, windSource);
        }
    }

    private void doForCompetitor(TrackedRace trackedRace, Competitor competitor) {
        GPSFixTrack<Competitor, GPSFixMoving> competitorTrack = trackedRace.getTrack(competitor);
        numberOfGPSFixes += competitorTrack.size();
        
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
    }

    private void doForCompetitorTrackAndTimeRange(Competitor competitor,
            GPSFixTrack<Competitor, GPSFixMoving> competitorTrack, TimePoint from, TimePoint to) {
        if (calculateDistanceTravelled) {
            totalDistanceTraveled = totalDistanceTraveled.add(competitorTrack.getDistanceTraveled(from, to));
        }
        if (calculateMaxSpeed) {
            Pair<GPSFixMoving, Speed> competitorMaxSpeed = competitorTrack.getMaximumSpeedOverGround(from, to);
            if (competitorMaxSpeed != null
                    && (maxSpeed == null || competitorMaxSpeed.getB().compareTo(maxSpeed.getB()) > 0)) {
                maxSpeed = new Triple<>(competitor, competitorMaxSpeed.getB(),
                        competitorMaxSpeed.getA().getTimePoint());
            }
        }
    }

    private void doForMark(TrackedRace trackedRace, Mark mark) {
        GPSFixTrack<Mark, GPSFix> markTrack = trackedRace.getOrCreateTrack(mark);
        numberOfGPSFixes += markTrack.size();
    }

    private void doForWindSource(TrackedRace trackedRace, WindSource windSource) {
        // don't count the "virtual" wind sources
        if (windSource.canBeStored() || windSource.getType() == WindSourceType.RACECOMMITTEE) {
            WindTrack windTrack = trackedRace.getOrCreateWindTrack(windSource);
            numberOfWindFixes += windTrack.size();
        }
    }

    public long getNumberOfGPSFixes() {
        return numberOfGPSFixes;
    }

    public long getNumberOfWindFixes() {
        return numberOfWindFixes;
    }

    /**
     * @return a valid, non-{@code null} distance
     */
    public Distance getTotalDistanceTraveled() {
        return totalDistanceTraveled;
    }
    
    public Triple<Competitor, Speed, TimePoint> getMaxSpeed() {
        return maxSpeed;
    }
    
    public TrackedRaceStatistics getStatistics() {
        return new TrackedRaceStatistics(getNumberOfGPSFixes(), getNumberOfWindFixes(),
                getTotalDistanceTraveled(), getMaxSpeed());
    }
}
