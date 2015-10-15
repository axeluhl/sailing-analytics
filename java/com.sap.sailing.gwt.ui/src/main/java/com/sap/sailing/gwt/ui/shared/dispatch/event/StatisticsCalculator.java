package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.gwt.server.HomeServiceUtil;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sailing.gwt.ui.shared.dispatch.event.EventActionUtil.LeaderboardCallback;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class StatisticsCalculator implements LeaderboardCallback {
    
    private static final Logger logger = Logger.getLogger(StatisticsCalculator.class.getName());
    private static final boolean CALCULATE_MAX_SPEED = false;
    private static final boolean CALCULATE_SAILED_MILES = true;

    private Set<Competitor> competitors = new HashSet<>();
    private int races, trackedRaces, regattas;
    private long numberOfGPSFixes, numberOfWindFixes;
    private Distance totalDistanceTraveled = Distance.NULL;
    private Triple<Competitor, Speed, TimePoint> maxSpeed = null;
    private final TimePoint now = MillisecondsTimePoint.now();

    @Override
    public void doForLeaderboard(LeaderboardContext context) {
        races += HomeServiceUtil.calculateRaceCount(context.getLeaderboard());
        trackedRaces += HomeServiceUtil.calculateTrackedRaceCount(context.getLeaderboard());
        regattas++;
        String disableStats = System.getenv("DISABLE_STATS");
        if (disableStats == null || "false".equals(disableStats)) {
            try {
                for (TrackedRace trackedRace : context.getLeaderboard().getTrackedRaces()) {
                    doForTrackedRace(trackedRace);
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Exception during calculation of event statistics", e);
            }
        }
    }
    
    private void doForTrackedRace(TrackedRace trackedRace) {
        for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
            competitors.add(competitor);
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
        competitorTrack.lockForRead();
        try {
            numberOfGPSFixes += Util.size(competitorTrack.getRawFixes());
        } finally {
            competitorTrack.unlockAfterRead();
        }
        if (trackedRace.hasStarted(now)) {
            final NavigableSet<MarkPassing> competitorMarkPassings = trackedRace.getMarkPassings(competitor);
            MarkPassing lastMarkPassingBeforeNow = null;
            trackedRace.lockForRead(competitorMarkPassings);
            try {
                MarkPassing next = null;
                Iterator<MarkPassing> i = competitorMarkPassings.descendingIterator();
                while (i.hasNext() && (next = i.next()).getTimePoint().after(now));
                if (next != null) {
                    lastMarkPassingBeforeNow = next;
                }
            } finally {
                trackedRace.unlockAfterRead(competitorMarkPassings);
            }
            if (lastMarkPassingBeforeNow != null) {
                // competitor has started and has at least one mark passing before now; compute distance traveled up to that
                // mark passing, increasing likelihood of cache hits as compared to using "now" as the query time point
                TimePoint from = trackedRace.getStartOfRace(), to = lastMarkPassingBeforeNow.getTimePoint();
                if (CALCULATE_SAILED_MILES) {
                    totalDistanceTraveled = totalDistanceTraveled.add(competitorTrack.getDistanceTraveled(from, to));
                }
                if (CALCULATE_MAX_SPEED) {
                    Pair<GPSFixMoving, Speed> competitorMaxSpeed = competitorTrack.getMaximumSpeedOverGround(from, to);
                    if (competitorMaxSpeed != null && (maxSpeed == null || competitorMaxSpeed.getB().compareTo( maxSpeed.getB()) > 0)
                            // only accept "reasonable" max speeds
                            && competitorMaxSpeed.getB().compareTo( GPSFixTrack.DEFAULT_MAX_SPEED_FOR_SMOOTHING) <= 0) {
                        maxSpeed = new Triple<>(competitor, competitorMaxSpeed.getB(), competitorMaxSpeed.getA().getTimePoint());
                    }
                }
            }
        }
    }
    
    private void doForMark(TrackedRace trackedRace, Mark mark) {
        GPSFixTrack<Mark, GPSFix> markTrack = trackedRace.getOrCreateTrack(mark);
        markTrack.lockForRead();
        try {
            numberOfGPSFixes += Util.size(markTrack.getRawFixes());
        } finally {
            markTrack.unlockAfterRead();
        }
    }
    
    private void doForWindSource(TrackedRace trackedRace, WindSource windSource) {
     // don't count the "virtual" wind sources
        if (windSource.canBeStored() || windSource.getType() == WindSourceType.RACECOMMITTEE) {
            WindTrack windTrack = trackedRace.getOrCreateWindTrack(windSource);
            windTrack.lockForRead();
            try {
                numberOfWindFixes += Util.size(windTrack.getRawFixes());
            } finally {
                windTrack.unlockAfterRead();
            }
        }
    }
    
    public ResultWithTTL<EventStatisticsDTO> getResult() {
        totalDistanceTraveled = totalDistanceTraveled == Distance.NULL ? null : totalDistanceTraveled;
        return new ResultWithTTL<EventStatisticsDTO>(Duration.ONE_MINUTE.times(5), new EventStatisticsDTO(regattas,
                competitors.size(), races, trackedRaces, numberOfGPSFixes, numberOfWindFixes, maxSpeed, totalDistanceTraveled));
    }

}
