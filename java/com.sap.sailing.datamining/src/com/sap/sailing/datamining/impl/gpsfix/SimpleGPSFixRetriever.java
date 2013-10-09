package com.sap.sailing.datamining.impl.gpsfix;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.sap.sailing.datamining.DataRetriever;
import com.sap.sailing.datamining.GPSFixContext;
import com.sap.sailing.datamining.GPSFixWithContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.RacingEventService;

public class SimpleGPSFixRetriever implements DataRetriever<GPSFixWithContext> {
    
    @Override
    public Collection<GPSFixWithContext> retrieveData(RacingEventService racingEventService) {
        Collection<GPSFixWithContext> data = new ArrayList<GPSFixWithContext>();
        for (LeaderboardGroup leaderboardGroup : racingEventService.getLeaderboardGroups().values()) {
            for (Leaderboard leaderboard : leaderboardGroup.getLeaderboards()) {
                CourseArea courseArea = leaderboard.getDefaultCourseArea();
                for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
                    for (Fleet fleet : raceColumn.getFleets()) {
                        TrackedRace trackedRace = raceColumn.getTrackedRace(fleet);
                        if (trackedRace != null) {
                            int legNumber = 1;
                            for (Leg leg : trackedRace.getRace().getCourse().getLegs()) {
                                TrackedLeg trackedLeg = trackedRace.getTrackedLeg(leg);
                                if (trackedLeg != null) {
                                    for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
                                        GPSFixContext context = new GPSFixContextImpl(leaderboardGroup, leaderboard, courseArea, fleet, trackedRace, trackedLeg, legNumber, competitor);
                                        data.addAll(retrieveDataFor(context));
                                    }
                                    legNumber++;
                                }
                            }
                        }
                    }
                }
            }
        }
        return data;
    }

    private Collection<GPSFixWithContext> retrieveDataFor(GPSFixContext context) {
        List<GPSFixWithContext> data = new ArrayList<GPSFixWithContext>();
        TrackedLegOfCompetitor trackedLegOfCompetitor = context.getTrackedLeg().getTrackedLeg(context.getCompetitor());
        GPSFixTrack<Competitor, GPSFixMoving> competitorTrack = context.getTrackedRace().getTrack(context.getCompetitor());
        competitorTrack.lockForRead();
        try {
            if (trackedLegOfCompetitor.getStartTime() != null && trackedLegOfCompetitor.getFinishTime() != null) {
                for (GPSFixMoving gpsFix : competitorTrack.getFixes(trackedLegOfCompetitor.getStartTime(), true, trackedLegOfCompetitor.getFinishTime(), true)) {
                    data.add(new GPSFixWithContextImpl(gpsFix, context));
                }
            }
        } finally {
            competitorTrack.unlockAfterRead();
        }
        return data;
    }

}
