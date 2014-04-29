package com.sap.sailing.datamining.impl.components.deprecated;

import java.util.ArrayList;
import java.util.Collection;

import com.sap.sailing.datamining.data.HasTrackedLegContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.datamining.impl.data.HasTrackedLegContextImpl;
import com.sap.sailing.datamining.impl.data.HasTrackedLegOfCompetitorContextImpl;
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
import com.sap.sse.datamining.impl.workers.retrievers.AbstractRetrievalWorker;
import com.sap.sse.datamining.workers.DataRetrievalWorker;

public abstract class AbstractLeaderboardGroupDataRetrievalWorker<DataType>
                      extends AbstractRetrievalWorker<Collection<DataType>> 
                      implements DataRetrievalWorker<LeaderboardGroup, DataType> {

    private LeaderboardGroup group;

    @Override
    public void setSource(LeaderboardGroup source) {
        this.group = source;
    }

    protected LeaderboardGroup getGroup() {
        return group;
    }

    private static Collection<Pair<TrackedLeg, HasTrackedLegContext>> retrieveDataTillTrackedLeg(LeaderboardGroup group) {
        Collection<Pair<TrackedLeg, HasTrackedLegContext>> data = new ArrayList<Pair<TrackedLeg, HasTrackedLegContext>>();
        for (Leaderboard leaderboard : group.getLeaderboards()) {
            CourseArea courseArea = leaderboard.getDefaultCourseArea();
            for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
                for (Fleet fleet : raceColumn.getFleets()) {
                    TrackedRace trackedRace = raceColumn.getTrackedRace(fleet);
                    if (trackedRace != null) {
                        int legNumber = 1;
                        for (Leg leg : trackedRace.getRace().getCourse().getLegs()) {
                            TrackedLeg trackedLeg = trackedRace.getTrackedLeg(leg);
                            if (trackedLeg != null) {
                                HasTrackedLegContext dataContext = new HasTrackedLegContextImpl(group, leaderboard,
                                        courseArea, fleet, trackedRace, trackedLeg, legNumber);
                                data.add(new Pair<TrackedLeg, HasTrackedLegContext>(trackedLeg, dataContext));
                                legNumber++;
                            }
                        }
                    }
                }
            }
        }
        return data;
    }

    protected static Collection<Pair<TrackedLegOfCompetitor, HasTrackedLegOfCompetitorContext>> retrieveDataTillTrackedLegOfCompetitor(
            LeaderboardGroup group) {
        Collection<Pair<TrackedLegOfCompetitor, HasTrackedLegOfCompetitorContext>> data = new ArrayList<Pair<TrackedLegOfCompetitor, HasTrackedLegOfCompetitorContext>>();
        Collection<Pair<TrackedLeg, HasTrackedLegContext>> baseData = retrieveDataTillTrackedLeg(group);
        for (Pair<TrackedLeg, HasTrackedLegContext> baseDataEntry : baseData) {
            TrackedLeg trackedLeg = baseDataEntry.getA();
            HasTrackedLegContext trackedLegContext = baseDataEntry.getB();
            for (Competitor competitor : trackedLegContext.getRace().getCompetitors()) {
                TrackedLegOfCompetitor trackedLegOfCompetitor = trackedLeg.getTrackedLeg(competitor);
                HasTrackedLegOfCompetitorContext dataContext = new HasTrackedLegOfCompetitorContextImpl(trackedLegContext,
                        competitor);
                data.add(new Pair<TrackedLegOfCompetitor, HasTrackedLegOfCompetitorContext>(trackedLegOfCompetitor,
                        dataContext));
            }
        }
        return data;
    }

}
