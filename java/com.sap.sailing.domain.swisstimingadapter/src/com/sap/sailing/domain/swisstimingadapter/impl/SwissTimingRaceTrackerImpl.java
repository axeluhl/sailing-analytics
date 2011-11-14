package com.sap.sailing.domain.swisstimingadapter.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.swisstimingadapter.Course;
import com.sap.sailing.domain.swisstimingadapter.DomainFactory;
import com.sap.sailing.domain.swisstimingadapter.Fix;
import com.sap.sailing.domain.swisstimingadapter.Race;
import com.sap.sailing.domain.swisstimingadapter.RaceSpecificMessageLoader;
import com.sap.sailing.domain.swisstimingadapter.RaceStatus;
import com.sap.sailing.domain.swisstimingadapter.SailMasterConnector;
import com.sap.sailing.domain.swisstimingadapter.SailMasterListener;
import com.sap.sailing.domain.swisstimingadapter.StartList;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingFactory;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingRaceTracker;
import com.sap.sailing.domain.tracking.DynamicRaceDefinitionSet;
import com.sap.sailing.domain.tracking.DynamicTrackedEvent;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.RaceHandle;
import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sailing.domain.tracking.TrackedEventRegistry;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.util.Util.Triple;

public class SwissTimingRaceTrackerImpl implements SwissTimingRaceTracker, SailMasterListener {
    private final SailMasterConnector connector;
    private final String raceID;
    private final RaceSpecificMessageLoader messageLoader;
    private final DomainFactory domainFactory;
    private final Triple<String, String, Integer> id;
    private final Event event;
    private final DynamicTrackedEvent trackedEvent;
    private final WindStore windStore;

    private RaceDefinition race;
    private Course course;
    private StartList startList;
    private DynamicTrackedRace trackedRace;
    
    protected SwissTimingRaceTrackerImpl(String raceID, String hostname, int port, WindStore windStore,
            DomainFactory domainFactory, SwissTimingFactory factory, RaceSpecificMessageLoader messageLoader,
            TrackedEventRegistry trackedEventRegistry) throws InterruptedException, UnknownHostException, IOException, ParseException {
        this.connector = factory.getOrCreateSailMasterConnector(hostname, port, messageLoader);
        this.domainFactory = domainFactory;
        this.raceID = raceID;
        this.messageLoader = messageLoader;
        this.windStore = windStore;
        this.id = new Triple<String, String, Integer>(raceID, hostname, port);
        connector.addSailMasterListener(raceID, this);
        connector.trackRace(raceID);
        event = domainFactory.getOrCreateEvent(raceID);
        trackedEvent = trackedEventRegistry.getOrCreateTrackedEvent(event);
    }

    @Override
    public void stop() throws MalformedURLException, IOException, InterruptedException {
        connector.removeSailMasterListener(raceID, this);
    }

    @Override
    public Set<RaceDefinition> getRaces() {
        return race==null?null:Collections.singleton(race);
    }

    @Override
    public RaceHandle getRaceHandle() {
        return new RaceHandle() {
            @Override
            public Event getEvent() {
                return SwissTimingRaceTrackerImpl.this.getEvent();
            }

            @Override
            public RaceDefinition getRace() {
                return race;
            }

            @Override
            public RaceDefinition getRace(long timeoutInMilliseconds) {
                return race;
            }

            @Override
            public DynamicTrackedEvent getTrackedEvent() {
                return trackedEvent;
            }

            @Override
            public RaceTracker getRaceTracker() {
                return SwissTimingRaceTrackerImpl.this;
            }
        };
    }

    @Override
    public DynamicTrackedEvent getTrackedEvent() {
        return trackedEvent;
    }

    @Override
    public WindStore getWindStore() {
        return windStore;
    }

    @Override
    public Event getEvent() {
        return event;
    }

    @Override
    public Triple<String, String, Integer> getID() {
        return id; 
    }

    @Override
    public void receivedRacePositionData(String raceID, RaceStatus status, TimePoint timePoint, TimePoint startTime,
            Long millisecondsSinceRaceStart, Integer nextMarkIndexForLeader, Distance distanceToNextMarkForLeader,
            Collection<Fix> fixes) {
        // TODO implement receivedRacePositionData; extract race start time, add GPSFixMoving objects to tracked race
        
    }

    @Override
    public void receivedTimingData(String raceID, String boatID,
            List<Triple<Integer, Integer, Long>> markIndicesRanksAndTimesSinceStartInMilliseconds) {
        // TODO implement receivedTimingData; generate MarkPassing objects and update tracked race accordingly
    }

    @Override
    public void receivedClockAtMark(String raceID,
            List<Triple<Integer, TimePoint, String>> markIndicesTimePointsAndBoatIDs) {
        // TODO implement receivedClockAtMark; generateMarkPassing object and update tracked race accordingly
    }

    @Override
    public void receivedStartList(String raceID, StartList startList) {
        this.startList = startList;
        if (course != null) {
            createRaceDefinition(raceID);
        }
    }

    private void createRaceDefinition(String raceID) {
        assert this.raceID == raceID;
        assert startList != null;
        assert course != null;
        // now we can create the RaceDefinition and most other things
        Race race = messageLoader.getRace(raceID);
        final RaceDefinition raceDefinition = domainFactory.createRaceDefinition(event, race, course);
        trackedRace = getTrackedEvent().createTrackedRace(raceDefinition, windStore,
                WindTrack.DEFAULT_MILLISECONDS_OVER_WHICH_TO_AVERAGE_WIND, GPSFixTrack.DEFAULT_MILLISECONDS_OVER_WHICH_TO_AVERAGE_SPEED,
                new DynamicRaceDefinitionSet() {
                    @Override
                    public void addRaceDefinition(RaceDefinition race) {
                        // we already know our single RaceDefinition
                        assert raceDefinition == race;
                    }
                });
    }

    @Override
    public void receivedCourseConfiguration(String raceID, Course course) {
        this.course = course;
        if (startList != null) {
            createRaceDefinition(raceID);
        }
    }

    @Override
    public void receivedAvailableRaces(Iterable<Race> races) {
        // don't care
    }
}
