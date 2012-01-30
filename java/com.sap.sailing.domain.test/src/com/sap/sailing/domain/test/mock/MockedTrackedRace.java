package com.sap.sailing.domain.test.mock;

import java.util.List;
import java.util.NavigableSet;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RacePlaceOrder;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.tracking.DynamicRaceDefinitionSet;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedEvent;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.RaceChangeListener;
import com.sap.sailing.domain.tracking.RaceListener;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindStore;
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
    public DynamicGPSFixTrack<Buoy, GPSFix> getOrCreateTrack(Buoy buoy) {
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
    public void addListener(RaceChangeListener listener) {
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
    public DynamicGPSFixTrack<Competitor, GPSFixMoving> getTrack(Competitor competitor) {
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

    @Override
    public Wind getEstimatedWindDirection(Position position, TimePoint timePoint) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean hasStarted(TimePoint at) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public DynamicTrackedEvent getTrackedEvent() {
        return new DynamicTrackedEvent() {
            @Override
            public Event getEvent() {
                return new Event() {
                    @Override
                    public String getName() {
                        return "A Mocked Test Event";
                    }

                    @Override
                    public Iterable<RaceDefinition> getAllRaces() {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public BoatClass getBoatClass() {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public Iterable<Competitor> getCompetitors() {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public void addRace(RaceDefinition race) {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void removeRace(RaceDefinition raceDefinition) {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public RaceDefinition getRaceByName(String raceName) {
                        // TODO Auto-generated method stub
                        return null;
                    }
                };
            }

            @Override
            public Iterable<TrackedRace> getTrackedRaces() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Iterable<TrackedRace> getTrackedRaces(BoatClass boatClass) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public void addTrackedRace(TrackedRace trackedRace) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void removeTrackedRace(TrackedRace trackedRace) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void addRaceListener(RaceListener listener) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public int getNetPoints(Competitor competitor, TimePoint timePoint) throws NoWindException {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public DynamicTrackedRace getTrackedRace(RaceDefinition race) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public DynamicTrackedRace getExistingTrackedRace(RaceDefinition race) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public void removeTrackedRace(RaceDefinition raceDefinition) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public DynamicTrackedRace createTrackedRace(RaceDefinition raceDefinition, WindStore windStore,
                    long millisecondsOverWhichToAverageWind, long millisecondsOverWhichToAverageSpeed,
                    DynamicRaceDefinitionSet raceDefinitionSetToUpdate) {
                // TODO Auto-generated method stub
                return null;
            }
        };
    }

    @Override
    public Position getApproximatePosition(Waypoint waypoint, TimePoint timePoint) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Tack getTack(Competitor competitor, TimePoint timePoint) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Wind getDirectionFromStartToNextMark(TimePoint at) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<GPSFixMoving> approximate(Competitor competitor, Distance maxDistance, TimePoint from, TimePoint to) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Maneuver> getManeuvers(Competitor competitor, TimePoint from, TimePoint to) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean raceIsKnownToStartUpwind() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setRaceIsKnownToStartUpwind(boolean raceIsKnownToStartUpwind) {
        // TODO Auto-generated method stub
    }

    @Override
    public Wind getWind(Position p, TimePoint at, WindSource... windSourcesToConsider) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RacePlaceOrder getPlaceOrder() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RaceIdentifier getRaceIdentifier() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TimePoint getAssumedEnd() {
        // TODO Auto-generated method stub
        return null;
    }

}
