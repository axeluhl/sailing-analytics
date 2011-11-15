package com.sap.sailing.domain.swisstimingadapter.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
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
import com.sap.sailing.domain.tracking.DynamicTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedEvent;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.RaceHandle;
import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sailing.domain.tracking.TrackedEventRegistry;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.util.Util;
import com.sap.sailing.util.Util.Triple;

import difflib.PatchFailedException;

public class SwissTimingRaceTrackerImpl implements SwissTimingRaceTracker, SailMasterListener {
    private static final Logger logger = Logger.getLogger(SwissTimingRaceTrackerImpl.class.getName());
    
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
        assert this.raceID.equals(raceID);
        if (this.raceID.equals(raceID)) {
            if (startTime != null) {
                trackedRace.setStartTimeReceived(startTime);
            }
            for (Fix fix : fixes) {
                GPSFixMoving gpsFix = domainFactory.createGPSFix(timePoint, fix);
                switch (fix.getTrackerType()) {
                case BUOY:
                case COMMITTEE:
                case JURY:
                case TIMINGSCORING:
                case UNIDENTIFIED:
                    String trackerID = fix.getBoatID();
                    Buoy buoy = domainFactory.getOrCreateBuoy(trackerID);
                    DynamicTrack<Buoy, GPSFix> buoyTrack = trackedRace.getOrCreateTrack(buoy);
                    buoyTrack.addGPSFix(gpsFix);
                    break;
                case COMPETITOR:
                    Competitor competitor = domainFactory.getCompetitorByBoatID(fix.getBoatID());
                    DynamicTrack<Competitor, GPSFixMoving> competitorTrack = trackedRace.getTrack(competitor);
                    competitorTrack.addGPSFix(gpsFix);
                    break;
                default:
                    logger.info("Unknown tracker type "+fix.getTrackerType());
                }
            }
        }
    }

    @Override
    public void receivedTimingData(String raceID, String boatID,
            List<Triple<Integer, Integer, Long>> markIndicesRanksAndTimesSinceStartInMilliseconds) {
        assert this.raceID.equals(raceID);
        TreeMap<Integer, MarkPassing> markPassingsByMarkIndex = new TreeMap<Integer, MarkPassing>();
        for (Triple<Integer, Integer, Long> markIndexRankAndTimeSinceStartInMilliseconds : markIndicesRanksAndTimesSinceStartInMilliseconds) {
            Waypoint waypoint = Util.get(trackedRace.getRace().getCourse().getWaypoints(),
                    markIndexRankAndTimeSinceStartInMilliseconds.getA());
            MillisecondsTimePoint timePoint = new MillisecondsTimePoint(trackedRace.getStart().asMillis()
                    + markIndexRankAndTimeSinceStartInMilliseconds.getC());
            MarkPassing markPassing = domainFactory.createMarkPassing(raceID, boatID, waypoint, timePoint);
            markPassingsByMarkIndex.put(markIndexRankAndTimeSinceStartInMilliseconds.getA(), markPassing);
        }
        Competitor competitor = domainFactory.getCompetitorByBoatID(boatID);
        trackedRace.updateMarkPassings(competitor, markPassingsByMarkIndex.values());
    }

    @Override
    public void receivedClockAtMark(String raceID,
            List<Triple<Integer, TimePoint, String>> markIndicesTimePointsAndBoatIDs) {
        // Ignored because it's covered by TMD. Mail from Kai Hahndorf of 2011-11-15T12:42:00Z:
        // "Die TMD werden immer gesendet. Das CAM Protokoll ist nur für unsere TV-Grafik wichtig, da damit die Rückstandsuhr gestartet wird."
    }

    @Override
    public void receivedStartList(String raceID, StartList startList) {
        this.startList = startList;
        if (course != null) {
            createRaceDefinition(raceID);
        }
    }

    private void createRaceDefinition(String raceID) {
        assert this.raceID.equals(raceID);
        assert startList != null;
        assert course != null;
        // now we can create the RaceDefinition and most other things
        Race race = messageLoader.getRace(raceID);
        final RaceDefinition raceDefinition = domainFactory.createRaceDefinition(event, race, startList, course);
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
        if (trackedRace == null) {
            if (startList != null) {
                createRaceDefinition(raceID);
            }
        } else {
            try {
                domainFactory.updateCourseWaypoints(trackedRace.getRace().getCourse(), course.getMarks());
            } catch (PatchFailedException e) {
                logger.info("Internal error trying to update course: "+e.getMessage());
                logger.throwing(SwissTimingRaceTrackerImpl.class.getName(), "receivedCourseConfiguration", e);
            }
        }
    }

    @Override
    public void receivedAvailableRaces(Iterable<Race> races) {
        // don't care
    }
}
