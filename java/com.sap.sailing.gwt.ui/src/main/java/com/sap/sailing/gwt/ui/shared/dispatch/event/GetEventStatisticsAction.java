package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.Iterator;
import java.util.NavigableSet;
import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.gwt.server.HomeServiceUtil;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class GetEventStatisticsAction implements Action<ResultWithTTL<EventStatisticsDTO>> {
    private UUID eventId;

    @SuppressWarnings("unused")
    private GetEventStatisticsAction() {
    }

    public GetEventStatisticsAction(UUID eventId) {
        this.eventId = eventId;
    }

    @GwtIncompatible
    public ResultWithTTL<EventStatisticsDTO> execute(DispatchContext context) {
        Event event = context.getRacingEventService().getEvent(eventId);
        if (event == null) {
            throw new RuntimeException("Event not found");
        }
        int competitors = 0;
        int races = 0;
        int trackedRaces = 0;
        int regattas = 0;
        long numberOfGPSFixes = 0;
        long numberOfWindFixes = 0;
        Distance totalDistanceTraveled = Distance.NULL;
        Triple<Competitor, Speed, TimePoint> maxSpeed = null;
        final TimePoint now = MillisecondsTimePoint.now();
        for (LeaderboardGroup lg : event.getLeaderboardGroups()) {
            for (Leaderboard leaderboard : lg.getLeaderboards()) {
                competitors += HomeServiceUtil.calculateCompetitorsCount(leaderboard);
                races += HomeServiceUtil.calculateRaceCount(leaderboard);
                trackedRaces += HomeServiceUtil.calculateTrackedRaceCount(leaderboard);
                regattas++;
                String disableStats = System.getenv("DISABLE_STATS");
                if (disableStats == null || !disableStats.isEmpty()) {
                    for (TrackedRace trackedRace : leaderboard.getTrackedRaces()) {
                        for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
                            GPSFixTrack<Competitor, GPSFixMoving> competitorTrack = trackedRace.getTrack(competitor);
                            competitorTrack.lockForRead();
                            try {
                                numberOfGPSFixes += Util.size(competitorTrack.getRawFixes());
                            } finally {
                                competitorTrack.unlockAfterRead();
                            }
                            if (trackedRace.hasStarted(now)) {
                                final NavigableSet<MarkPassing> competitorMarkPassings = trackedRace
                                        .getMarkPassings(competitor);
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
                                    // competitor has started and has at least one mark passing before now; compute
                                    // distance traveled
                                    // up to that mark passing, increasing likelihood of cache hits as compared to using
                                    // "now" as
                                    // the query time point
                                    totalDistanceTraveled = totalDistanceTraveled.add(competitorTrack
                                            .getDistanceTraveled(trackedRace.getStartOfRace(),
                                                    lastMarkPassingBeforeNow.getTimePoint()));
                                    Pair<GPSFixMoving, Speed> competitorMaxSpeed = competitorTrack
                                            .getMaximumSpeedOverGround(trackedRace.getStartOfRace(),
                                                    lastMarkPassingBeforeNow.getTimePoint());
                                    if (competitorMaxSpeed != null
                                            && (maxSpeed == null || competitorMaxSpeed.getB()
                                                    .compareTo(maxSpeed.getB()) > 0)) {
                                        maxSpeed = new Triple<>(competitor, competitorMaxSpeed.getB(),
                                                competitorMaxSpeed.getA().getTimePoint());
                                    }
                                }
                            }
                        }
                        for (Mark mark : trackedRace.getMarks()) {
                            GPSFixTrack<Mark, GPSFix> markTrack = trackedRace.getOrCreateTrack(mark);
                            markTrack.lockForRead();
                            try {
                                numberOfGPSFixes += Util.size(markTrack.getRawFixes());
                            } finally {
                                markTrack.unlockAfterRead();
                            }
                        }
                        for (WindSource windSource : trackedRace.getWindSources()) {
                            WindTrack windTrack = trackedRace.getOrCreateWindTrack(windSource);
                            windTrack.lockForRead();
                            try {
                                numberOfWindFixes += Util.size(windTrack.getRawFixes());
                            } finally {
                                windTrack.unlockAfterRead();
                            }
                        }
                    }
                }
            }
        }
        return new ResultWithTTL<EventStatisticsDTO>(1000 * 60 * 5, new EventStatisticsDTO(regattas, competitors,
                races, trackedRaces, numberOfGPSFixes, numberOfWindFixes, maxSpeed, totalDistanceTraveled));
    }
}
