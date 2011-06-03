package com.sap.sailing.domain.tractracadapter.impl;

import java.util.Collection;

import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedEvent;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindSource;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRaceImpl;

public class TracTracTrackedRaceImpl extends DynamicTrackedRaceImpl implements DynamicTrackedRace {

    public TracTracTrackedRaceImpl(TrackedEvent trackedEvent, RaceDefinition race,
            long millisecondsOverWhichToAverageWind, long millisecondsOverWhichToAverageSpeed) {
        super(trackedEvent, race, millisecondsOverWhichToAverageWind, millisecondsOverWhichToAverageSpeed);
    }

    @Override
    protected TrackedLeg createTrackedLeg(RaceDefinition race, Leg leg) {
        return new TracTracTrackedLegImpl(this, leg, race.getCompetitors());
    }

    @Override
    public TrackedLegOfCompetitor getCurrentLeg(Competitor competitor) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TrackedLeg getCurrentLeg(TimePoint timePoint) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getRankDifference(Competitor competitor, Leg leg, TimePoint timePoint) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getRank(Competitor competitor) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getRank(Competitor competitor, TimePoint timePoint) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Distance getStartAdvantage(Competitor competitor, double secondsIntoTheRace) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<MarkPassing> getMarkPassingsInOrder(Waypoint waypoint) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MarkPassing getMarkPassing(Competitor competitor, Waypoint waypoint) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public GPSFixTrack<Buoy, GPSFix> getTrack(Buoy buoy) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Wind getWind(Position p, TimePoint at) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setWindSource(WindSource windSource) {
        // TODO Auto-generated method stub

    }

    @Override
    public WindSource getWindSource() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WindTrack getWindTrack(WindSource windSource) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void waitForNextUpdate(int sinceUpdate) throws InterruptedException {
        // TODO Auto-generated method stub

    }

    @Override
    public TimePoint getStartOfTracking() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TimePoint getTimePointOfNewestEvent() {
        // TODO Auto-generated method stub
        return null;
    }

}
