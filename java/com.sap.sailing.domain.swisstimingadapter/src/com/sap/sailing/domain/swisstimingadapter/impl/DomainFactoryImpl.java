package com.sap.sailing.domain.swisstimingadapter.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.Person;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Team;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.domain.base.impl.BuoyImpl;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.base.impl.EventImpl;
import com.sap.sailing.domain.base.impl.GateImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.base.impl.NationalityImpl;
import com.sap.sailing.domain.base.impl.PersonImpl;
import com.sap.sailing.domain.base.impl.RaceDefinitionImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.domain.base.impl.WaypointImpl;
import com.sap.sailing.domain.swisstimingadapter.Course;
import com.sap.sailing.domain.swisstimingadapter.DomainFactory;
import com.sap.sailing.domain.swisstimingadapter.Fix;
import com.sap.sailing.domain.swisstimingadapter.Mark;
import com.sap.sailing.domain.swisstimingadapter.MessageType;
import com.sap.sailing.domain.swisstimingadapter.Race;
import com.sap.sailing.domain.swisstimingadapter.StartList;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.tracking.impl.MarkPassingImpl;
import com.sap.sailing.util.CourseAsWaypointList;
import com.sap.sailing.util.Util;

import difflib.DiffUtils;
import difflib.Patch;
import difflib.PatchFailedException;

/**
 * {@link RaceDefinition} objects created by this factory are created using the SwissTiming "Race ID"
 * as the {@link RaceDefinition#getName() race name}. This at the same time defines the name of the
 * single {@link Event} created per {@link RaceDefinition}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class DomainFactoryImpl implements DomainFactory {
    private final Map<String, Event> raceIDToEventCache;
    private final Map<String, Competitor> boatIDToCompetitorCache;
    private final Map<String, Buoy> buoyCache;
    private final Map<Iterable<String>, ControlPoint> controlPointCache;
    private final Map<String, Nationality> nationalityCache;
    private final Map<String, BoatClass> olympicClassesByID;
    private final BoatClass unknownBoatClass;
    
    public DomainFactoryImpl() {
        raceIDToEventCache = new HashMap<String, Event>();
        boatIDToCompetitorCache = new HashMap<String, Competitor>();
        buoyCache = new HashMap<String, Buoy>();
        controlPointCache = new HashMap<Iterable<String>, ControlPoint>();
        nationalityCache = new HashMap<String, Nationality>();
        olympicClassesByID = new HashMap<String, BoatClass>();
        /*
        SAM102000 Men's Windsurfer = Windsufer Männer RS:X
        SAW102000 Women's Windsurfer = Windsurfer Damen RS:X
        SAM004000 Men's One Person Dinghy = Laser Männer
        SAW103000 Women's One Person Dinghy = Laser Damen Laser Radial
        SAM002000 Men's One Person Dinghy Heavy = Finn Dinghy Männer
        SAM005000 Men's Two Person Dinghy = 470er Männer
        SAW005000 Women's Two Person Dinghy = 470er Damen
        SAM009000 Men's Skiff = 49er Männer
        SAM007000 Men's Keelboat = Starboot Männer 
        SAW010000 Women's Match Racing = Matchrace Damen Elliott 6M (modified)
        */
        olympicClassesByID.put("102", new BoatClassImpl("RS:X"));
        olympicClassesByID.put("004", new BoatClassImpl("Laser"));
        olympicClassesByID.put("103", new BoatClassImpl("Laser Radial"));
        olympicClassesByID.put("002", new BoatClassImpl("Finn"));
        olympicClassesByID.put("005", new BoatClassImpl("470"));
        olympicClassesByID.put("009", new BoatClassImpl("49er"));
        olympicClassesByID.put("007", new BoatClassImpl("Star"));
        olympicClassesByID.put("010", new BoatClassImpl("Elliott 6M"));
        unknownBoatClass = new BoatClassImpl("Unknown");
    }

    @Override
    public Event getOrCreateEvent(String raceID) {
        Event result = raceIDToEventCache.get(raceID);
        if (result == null) {
            result = new EventImpl(raceID, null);
            raceIDToEventCache.put(raceID, result);
        }
        return result;
    }
    
    @Override
    public Competitor getCompetitorByBoatID(String boatID) {
        return boatIDToCompetitorCache.get(boatID);
    }
    
    @Override
    public Competitor getOrCreateCompetitor(com.sap.sailing.domain.swisstimingadapter.Competitor competitor, BoatClass boatClass) {
        Competitor result = boatIDToCompetitorCache.get(competitor.getBoatID());
        if (result == null) {
            Boat boat = new BoatImpl(competitor.getName(), boatClass, competitor.getBoatID());
            List<Person> teamMembers = new ArrayList<Person>();
            for (String teamMemberName : competitor.getName().split("[-+&]")) {
                teamMembers.add(new PersonImpl(teamMemberName.trim(), getOrCreateNationality(competitor.getThreeLetterIOCCode()),
                        /* dateOfBirth */ null, teamMemberName.trim()));
            }
            Team team = new TeamImpl(competitor.getName(), teamMembers, /* coach */ null);
            result = new com.sap.sailing.domain.base.impl.CompetitorImpl(competitor.getBoatID(), competitor.getName(), team, boat);
            boatIDToCompetitorCache.put(competitor.getBoatID(), result);
        }
        return result;
    }

    @Override
    public Nationality getOrCreateNationality(String nationalityName) {
        synchronized (nationalityCache) {
            Nationality result = nationalityCache.get(nationalityName);
            if (result == null) {
                result = new NationalityImpl(nationalityName, nationalityName);
                nationalityCache.put(nationalityName, result);
            }
            return result;
        }
    }

    @Override
    public RaceDefinition createRaceDefinition(Event event, Race race, StartList startList, Course course) {
        com.sap.sailing.domain.base.Course domainCourse = createCourse(race.getDescription(), course);
        BoatClass boatClass = getOrCreateBoatClassFromRaceID(race.getRaceID());
        Iterable<Competitor> competitors = createCompetitorList(startList, boatClass);
        RaceDefinition result = new RaceDefinitionImpl(race.getRaceID(), domainCourse,
                boatClass, competitors);
        event.addRace(result);
        return result;
    }

    private BoatClass getOrCreateBoatClassFromRaceID(String raceID) {
        BoatClass result;
        /*
            SAM102000 Men's Windsurfer = Windsufer Männer RS:X
            SAW102000 Women's Windsurfer = Windsurfer Damen RS:X
            SAM004000 Men's One Person Dinghy = Laser Männer
            SAW103000 Women's One Person Dinghy = Laser Damen Laser Radial
            SAM002000 Men's One Person Dinghy Heavy = Finn Dinghy Männer
            SAM005000 Men's Two Person Dinghy = 470er Männer
            SAW005000 Women's Two Person Dinghy = 470er Damen
            SAM009000 Men's Skiff = 49er Männer
            SAM007000 Men's Keelboat = Starboot Männer 
            SAW010000 Women's Match Racing = Matchrace Damen Elliott 6M (modified)
         */
        if (raceID.startsWith("SA") && raceID.length() == 9) {
            String classID = raceID.substring(3, 6);
            result = olympicClassesByID.get(classID);
        } else {
            result = unknownBoatClass;
        }
        return result;
    }

    private Iterable<Competitor> createCompetitorList(StartList startList, BoatClass boatClass) {
        List<Competitor> result = new ArrayList<Competitor>();
        for (com.sap.sailing.domain.swisstimingadapter.Competitor swissTimingCompetitor : startList.getCompetitors()) {
            Competitor domainCompetitor = getOrCreateCompetitor(swissTimingCompetitor, boatClass);
            result.add(domainCompetitor);
        }
        return result;
    }

    private com.sap.sailing.domain.base.Course createCourse(String courseName, Course course) {
        List<Waypoint> waypoints = new ArrayList<Waypoint>();
        for (Mark mark : course.getMarks()) {
            ControlPoint controlPoint = getOrCreateControlPoint(mark.getDevices());
            Waypoint waypoint = new WaypointImpl(controlPoint);
            waypoints.add(waypoint);
        }
        com.sap.sailing.domain.base.Course result = new CourseImpl(courseName, waypoints);
        return result;
    }

    private ControlPoint getOrCreateControlPoint(Iterable<String> devices) {
        ControlPoint result = controlPointCache.get(devices);
        if (result == null) {
            switch (Util.size(devices)) {
            case 1:
                result = getOrCreateBuoy(devices.iterator().next());
                break;
            case 2:
                Iterator<String> buoyNameIter = devices.iterator();
                String left = buoyNameIter.next();
                String right = buoyNameIter.next();
                result = new GateImpl(getOrCreateBuoy(left), getOrCreateBuoy(right), left+"/"+right);
                break;
            default:
                throw new RuntimeException("Don't know how to handle control points with number of devices neither 1 nor 2. Was "+Util.size(devices));
            }
            controlPointCache.put(devices, result);
        }
        return result;
    }

    /**
     * @param id
     *            the ID which is probably also used as the "device name" and the "sail number" in case of an
     *            {@link MessageType#RPD RPD} message
     */
    @Override
    public Buoy getOrCreateBuoy(String id) {
        Buoy result = buoyCache.get(id);
        if (result == null) {
            result = new BuoyImpl(id);
            buoyCache.put(id, result);
        }
        return result;
    }

    @Override
    public GPSFixMoving createGPSFix(TimePoint timePointOfTransmission, Fix fix) {
        GPSFixMoving result = new GPSFixMovingImpl(fix.getPosition(), new MillisecondsTimePoint(
                timePointOfTransmission.asMillis() + fix.getAgeOfDataInMilliseconds()), fix.getSpeed());
        return result;
    }
    
    @Override
    public void updateCourseWaypoints(com.sap.sailing.domain.base.Course courseToUpdate, Iterable<Mark> marks) throws PatchFailedException {
        Iterable<Waypoint> courseWaypoints = courseToUpdate.getWaypoints();
        List<Waypoint> newWaypointList = new LinkedList<Waypoint>();
        // key existing waypoints by control points and re-use each one at most once during construction of the
        // new waypoint list; since several waypoints can have the same control point, the map goes from
        // control point to List<Waypoint>. The waypoints in the lists are held in the order of their
        // occurrence in courseToUpdate.getWaypoints().
        Map<com.sap.sailing.domain.base.ControlPoint, List<Waypoint>> existingWaypointsByControlPoint =
                new HashMap<com.sap.sailing.domain.base.ControlPoint, List<Waypoint>>();
        for (Waypoint waypoint : courseToUpdate.getWaypoints()) {
            List<Waypoint> wpl = existingWaypointsByControlPoint.get(waypoint.getControlPoint());
            if (wpl == null) {
                wpl = new ArrayList<Waypoint>();
                existingWaypointsByControlPoint.put(waypoint.getControlPoint(), wpl);
            }
            wpl.add(waypoint);
        }
        for (Mark mark : marks) {
            com.sap.sailing.domain.base.ControlPoint domainControlPoint = getOrCreateControlPoint(mark.getDevices());
            List<Waypoint> waypoints = existingWaypointsByControlPoint.get(domainControlPoint);
            Waypoint waypoint;
            if (waypoints == null || waypoints.isEmpty()) {
                // must be a new control point for which we don't have a waypoint yet
                waypoint = new WaypointImpl(domainControlPoint);
            } else {
                waypoint = waypoints.remove(0); // take the first from the list
            }
            newWaypointList.add(waypoint);
        }
        Patch<Waypoint> patch = DiffUtils.diff(courseWaypoints, newWaypointList);
        CourseAsWaypointList courseAsWaypointList = new CourseAsWaypointList(courseToUpdate);
        synchronized (courseToUpdate) {
            patch.applyToInPlace(courseAsWaypointList);
        }
    }

    @Override
    public MarkPassing createMarkPassing(String boatID, Waypoint waypoint, TimePoint timePoint) {
        return new MarkPassingImpl(timePoint, waypoint, getCompetitorByBoatID(boatID));
    }

    @Override
    public void removeRace(String raceID) {
        Event event = getOrCreateEvent(raceID);
        Set<RaceDefinition> toRemove = new HashSet<RaceDefinition>();
        if (event != null) {
            for (RaceDefinition race : event.getAllRaces()) {
                if (race.getName().equals(raceID)) {
                    toRemove.add(race);
                }
            }
            for (RaceDefinition raceToRemove : toRemove) {
                event.removeRace(raceToRemove);
            }
            if (Util.isEmpty(event.getAllRaces())) {
                raceIDToEventCache.remove(raceID);
            }
        }
    }

}
