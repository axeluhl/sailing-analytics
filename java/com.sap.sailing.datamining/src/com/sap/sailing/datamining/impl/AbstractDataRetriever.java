package com.sap.sailing.datamining.impl;

import java.util.ArrayList;
import java.util.Collection;

import com.sap.sailing.datamining.DataRetriever;
import com.sap.sailing.datamining.data.TrackedLegContext;
import com.sap.sailing.datamining.data.TrackedLegOfCompetitorContext;
import com.sap.sailing.datamining.data.impl.TrackedLegContextImpl;
import com.sap.sailing.datamining.data.impl.TrackedLegOfCompetitorContextImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.RacingEventService;

public abstract class AbstractDataRetriever<DataType> implements DataRetriever<DataType> {

    private static Collection<Pair<TrackedLeg, TrackedLegContext>> retrieveDataTillTrackedLeg(RacingEventService racingEventService) {
        Collection<Pair<TrackedLeg, TrackedLegContext>> data = new ArrayList<Pair<TrackedLeg, TrackedLegContext>>();
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
                                    TrackedLegContext dataContext = new TrackedLegContextImpl(leaderboardGroup, leaderboard, courseArea, fleet, trackedRace, trackedLeg, legNumber);
                                    data.add(new Pair<TrackedLeg, TrackedLegContext>(trackedLeg, dataContext));
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

    protected static Collection<Pair<TrackedLegOfCompetitor, TrackedLegOfCompetitorContext>> retrieveDataTillTrackedLegOfCompetitor(RacingEventService racingEventService) {
        Collection<Pair<TrackedLegOfCompetitor, TrackedLegOfCompetitorContext>> data = new ArrayList<Pair<TrackedLegOfCompetitor,TrackedLegOfCompetitorContext>>();
        Collection<Pair<TrackedLeg, TrackedLegContext>> baseData = retrieveDataTillTrackedLeg(racingEventService);
        for (Pair<TrackedLeg, TrackedLegContext> baseDataEntry : baseData) {
            TrackedLeg trackedLeg = baseDataEntry.getA();
            TrackedLegContext trackedLegContext = baseDataEntry.getB();
            for (Competitor competitor : trackedLegContext.getTrackedRace().getRace().getCompetitors()) {
                TrackedLegOfCompetitor trackedLegOfCompetitor = trackedLeg.getTrackedLeg(competitor);
                TrackedLegOfCompetitorContext dataContext = new TrackedLegOfCompetitorContextImpl(trackedLegContext, competitor);
                data.add(new Pair<TrackedLegOfCompetitor, TrackedLegOfCompetitorContext>(trackedLegOfCompetitor, dataContext));
            }
        }
        return data;
    }

    @Override
    public abstract Collection<DataType> retrieveData(RacingEventService racingEventService);

}
