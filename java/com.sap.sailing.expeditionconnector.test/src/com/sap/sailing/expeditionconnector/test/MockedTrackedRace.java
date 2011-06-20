package com.sap.sailing.expeditionconnector.test;

import java.util.NavigableSet;

import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.tracking.DynamicTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.NoWindException;
import com.sap.sailing.domain.tracking.RaceChangeListener;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindSource;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.impl.WindTrackImpl;

public class MockedTrackedRace implements DynamicTrackedRace {
    private final WindTrack windTrack = new WindTrackImpl(/* millisecondsOverWhichToAverage */ 30000);
    
    public WindTrack getWindTrack() {
        return windTrack;
    }

    @Override
    public RaceDefinition getRace() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TimePoint getStart() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterable<TrackedLeg> getTrackedLegs() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TrackedLeg getTrackedLeg(Leg leg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TrackedLegOfCompetitor getCurrentLeg(Competitor competitor, TimePoint timePoint) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TrackedLeg getCurrentLeg(TimePoint timePoint) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TrackedLeg getTrackedLegFinishingAt(Waypoint endOfLeg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TrackedLeg getTrackedLegStartingAt(Waypoint startOfLeg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TrackedLegOfCompetitor getTrackedLeg(Competitor competitor, TimePoint at) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TrackedLegOfCompetitor getTrackedLeg(Competitor competitor, Leg leg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getUpdateCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getRankDifference(Competitor competitor, Leg leg, TimePoint timePoint) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getRank(Competitor competitor) throws NoWindException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getRank(Competitor competitor, TimePoint timePoint) throws NoWindException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Distance getStartAdvantage(Competitor competitor, double secondsIntoTheRace) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterable<MarkPassing> getMarkPassingsInOrder(Waypoint waypoint) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MarkPassing getMarkPassing(Competitor competitor, Waypoint waypoint) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DynamicTrack<Buoy, GPSFix> getTrack(Buoy buoy) {
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

    @Override
    public NavigableSet<MarkPassing> getMarkPassings(Competitor competitor) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void recordFix(Competitor competitor, GPSFixMoving fix) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void recordWind(Wind wind, WindSource windSource) {
        if (windSource == WindSource.EXPEDITION) {
            windTrack.add(wind);
        }
    }

    @Override
    public void addListener(RaceChangeListener<Competitor> listener) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void updateMarkPassings(Competitor competitor, Iterable<MarkPassing> markPassings) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setStartTimeReceived(TimePoint start) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public DynamicTrack<Competitor, GPSFixMoving> getTrack(Competitor competitor) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void removeWind(Wind wind, WindSource windSource) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public TimePoint getTimePointOfLastEvent() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setMillisecondsOverWhichToAverageSpeed(long millisecondsOverWhichToAverageSpeed) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setMillisecondsOverWhichToAverageWind(long millisecondsOverWhichToAverageWind) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public long getMillisecondsOverWhichToAverageSpeed() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getMillisecondsOverWhichToAverageWind() {
        // TODO Auto-generated method stub
        return 0;
    }

}
