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
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.EventImpl;
import com.sap.sailing.domain.swisstimingadapter.Course;
import com.sap.sailing.domain.swisstimingadapter.Fix;
import com.sap.sailing.domain.swisstimingadapter.Race;
import com.sap.sailing.domain.swisstimingadapter.RaceSpecificMessageLoader;
import com.sap.sailing.domain.swisstimingadapter.RaceStatus;
import com.sap.sailing.domain.swisstimingadapter.SailMasterConnector;
import com.sap.sailing.domain.swisstimingadapter.SailMasterListener;
import com.sap.sailing.domain.swisstimingadapter.StartList;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingFactory;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingRaceTracker;
import com.sap.sailing.domain.tracking.DynamicTrackedEvent;
import com.sap.sailing.domain.tracking.RaceHandle;
import com.sap.sailing.domain.tracking.TrackedEventRegistry;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.util.Util.Triple;

public class SwissTimingRaceTrackerImpl implements SwissTimingRaceTracker, SailMasterListener {
    private final SailMasterConnector connector;
    private final String raceID;
    private final RaceSpecificMessageLoader messageLoader;
    private final TrackedEventRegistry trackedEventRegistry;

    private RaceDefinition race;
    private Course course;
    private StartList startList;
    
    protected SwissTimingRaceTrackerImpl(String raceID, String hostname, int port, SwissTimingFactory factory,
            RaceSpecificMessageLoader messageLoader, TrackedEventRegistry trackedEventRegistry)
            throws InterruptedException, UnknownHostException, IOException, ParseException {
        this.connector = factory.getOrCreateSailMasterConnector(hostname, port, messageLoader);
        this.trackedEventRegistry = trackedEventRegistry;
        this.raceID = raceID;
        this.messageLoader = messageLoader;
        connector.addSailMasterListener(raceID, this);
        connector.trackRace(raceID);
    }

    @Override
    public void stop() throws MalformedURLException, IOException, InterruptedException {
        connector.removeSailMasterListener(raceID, this);
    }

    @Override
    public Set<RaceDefinition> getRaces() {
        return Collections.singleton(race);
    }

    @Override
    public RaceHandle getRaceHandle() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DynamicTrackedEvent getTrackedEvent() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WindStore getWindStore() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Event getEvent() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Triple<String, String, Integer> getID() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void receivedRacePositionData(String raceID, RaceStatus status, TimePoint timePoint, TimePoint startTime,
            Long millisecondsSinceRaceStart, Integer nextMarkIndexForLeader, Distance distanceToNextMarkForLeader,
            Collection<Fix> fixes) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void receivedTimingData(String raceID, String boatID,
            List<Triple<Integer, Integer, Long>> markIndicesRanksAndTimesSinceStartInMilliseconds) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void receivedClockAtMark(String raceID,
            List<Triple<Integer, TimePoint, String>> markIndicesTimePointsAndBoatIDs) {
        // TODO Auto-generated method stub
        
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
        Event event = new EventImpl(race.getDescription(), new BoatClassImpl("Unknown"));
        // TODO continue here by creating the Waypoint/ControlPoint objects, then the CourseImpl, the CompetitorImpl objects and then the RaceDefinition; afterwards, also create the tracking counterparts such as the TrackedRaceImpl and TrackedEventImpl
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
