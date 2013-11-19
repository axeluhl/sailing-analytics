package com.sap.sailing.datamining.impl;

import java.util.ArrayList;
import java.util.Collection;

import com.sap.sailing.datamining.DataReceiver;
import com.sap.sailing.datamining.SingleThreadedDataRetriever;
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

public abstract class AbstractLeaderboardGroupDataRetriever<DataType> implements SingleThreadedDataRetriever<DataType> {

    private DataReceiver<DataType> receiver;
    private LeaderboardGroup group;
    private boolean isDone;

    @Override
    public void setReceiver(DataReceiver<DataType> receiver) {
        this.receiver = receiver;
    }

    @Override
    public void setGroup(LeaderboardGroup group) {
        this.group = group;
    }

    protected DataReceiver<DataType> getReceiver() {
        return receiver;
    }

    protected LeaderboardGroup getGroup() {
        return group;
    }

    @Override
    public boolean isDone() {
        return isDone;
    }

    @Override
    public void run() {
        receiver.addData(retrieveData());
        isDone = true;
    }

    protected abstract Collection<DataType> retrieveData();

    private static Collection<Pair<TrackedLeg, TrackedLegContext>> retrieveDataTillTrackedLeg(LeaderboardGroup group) {
        Collection<Pair<TrackedLeg, TrackedLegContext>> data = new ArrayList<Pair<TrackedLeg, TrackedLegContext>>();
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
                                TrackedLegContext dataContext = new TrackedLegContextImpl(group, leaderboard,
                                        courseArea, fleet, trackedRace, trackedLeg, legNumber);
                                data.add(new Pair<TrackedLeg, TrackedLegContext>(trackedLeg, dataContext));
                                legNumber++;
                            }
                        }
                    }
                }
            }
        }
        return data;
    }

    protected static Collection<Pair<TrackedLegOfCompetitor, TrackedLegOfCompetitorContext>> retrieveDataTillTrackedLegOfCompetitor(
            LeaderboardGroup group) {
        Collection<Pair<TrackedLegOfCompetitor, TrackedLegOfCompetitorContext>> data = new ArrayList<Pair<TrackedLegOfCompetitor, TrackedLegOfCompetitorContext>>();
        Collection<Pair<TrackedLeg, TrackedLegContext>> baseData = retrieveDataTillTrackedLeg(group);
        for (Pair<TrackedLeg, TrackedLegContext> baseDataEntry : baseData) {
            TrackedLeg trackedLeg = baseDataEntry.getA();
            TrackedLegContext trackedLegContext = baseDataEntry.getB();
            for (Competitor competitor : trackedLegContext.getTrackedRace().getRace().getCompetitors()) {
                TrackedLegOfCompetitor trackedLegOfCompetitor = trackedLeg.getTrackedLeg(competitor);
                TrackedLegOfCompetitorContext dataContext = new TrackedLegOfCompetitorContextImpl(trackedLegContext,
                        competitor);
                data.add(new Pair<TrackedLegOfCompetitor, TrackedLegOfCompetitorContext>(trackedLegOfCompetitor,
                        dataContext));
            }
        }
        return data;
    }

}
