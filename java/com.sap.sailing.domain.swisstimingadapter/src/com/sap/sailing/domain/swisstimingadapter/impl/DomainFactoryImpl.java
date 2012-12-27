package com.sap.sailing.domain.swisstimingadapter.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.Person;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Team;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.base.impl.PersonImpl;
import com.sap.sailing.domain.base.impl.RaceDefinitionImpl;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.swisstimingadapter.Course;
import com.sap.sailing.domain.swisstimingadapter.DomainFactory;
import com.sap.sailing.domain.swisstimingadapter.Fix;
import com.sap.sailing.domain.swisstimingadapter.Mark;
import com.sap.sailing.domain.swisstimingadapter.MessageType;
import com.sap.sailing.domain.swisstimingadapter.Race;
import com.sap.sailing.domain.swisstimingadapter.RaceSpecificMessageLoader;
import com.sap.sailing.domain.swisstimingadapter.StartList;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingFactory;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.RaceTrackingConnectivityParameters;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;

import difflib.PatchFailedException;

/**
 * {@link RaceDefinition} objects created by this factory are created using the SwissTiming "Race ID"
 * as the {@link RaceDefinition#getName() race name}. This at the same time defines the name of the
 * single {@link Regatta} created per {@link RaceDefinition}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class DomainFactoryImpl implements DomainFactory {
    private final static Logger logger = Logger.getLogger(DomainFactoryImpl.class.getName());
    private final Map<String, Regatta> raceIDToRegattaCache;
    private final Map<Iterable<String>, ControlPoint> controlPointCache;
    private final Map<String, BoatClass> olympicClassesByID;
    private final BoatClass unknownBoatClass;
    private final com.sap.sailing.domain.base.DomainFactory baseDomainFactory;
    
    public DomainFactoryImpl(com.sap.sailing.domain.base.DomainFactory baseDomainFactory) {
        this.baseDomainFactory = baseDomainFactory;
        raceIDToRegattaCache = new HashMap<String, Regatta>();
        controlPointCache = new HashMap<Iterable<String>, ControlPoint>();
        olympicClassesByID = new HashMap<String, BoatClass>();
        /*
        SAM102000 Men's Windsurfer = Windsufer M�nner RS:X
        SAW102000 Women's Windsurfer = Windsurfer Damen RS:X
        SAM004000 Men's One Person Dinghy = Laser M�nner
        SAW103000 Women's One Person Dinghy = Laser Damen Laser Radial
        SAM002000 Men's One Person Dinghy Heavy = Finn Dinghy M�nner
        SAM005000 Men's Two Person Dinghy = 470er M�nner
        SAW005000 Women's Two Person Dinghy = 470er Damen
        SAM009000 Men's Skiff = 49er M�nner
        SAM007000 Men's Keelboat = Starboot M�nner 
        SAW010000 Women's Match Racing = Matchrace Damen Elliott 6M (modified)
        */
        olympicClassesByID.put("102", baseDomainFactory.getOrCreateBoatClass("RS:X", /* typicallyStartsUpwind */ true));
        olympicClassesByID.put("004", baseDomainFactory.getOrCreateBoatClass("Laser", /* typicallyStartsUpwind */ true));
        olympicClassesByID.put("103", baseDomainFactory.getOrCreateBoatClass("Laser Radial", /* typicallyStartsUpwind */ true));
        olympicClassesByID.put("002", baseDomainFactory.getOrCreateBoatClass("Finn", /* typicallyStartsUpwind */ true));
        olympicClassesByID.put("005", baseDomainFactory.getOrCreateBoatClass("470", /* typicallyStartsUpwind */ true));
        olympicClassesByID.put("009", baseDomainFactory.getOrCreateBoatClass("49er", /* alwaysStartsUpwind */ true));
        olympicClassesByID.put("007", baseDomainFactory.getOrCreateBoatClass("Star", /* typicallyStartsUpwind */ true));
        olympicClassesByID.put("010", baseDomainFactory.getOrCreateBoatClass("Elliott 6M", /* typicallyStartsUpwind */ true));
        unknownBoatClass = baseDomainFactory.getOrCreateBoatClass("Unknown", /* typicallyStartsUpwind */ true);
    }

    @Override
    public com.sap.sailing.domain.base.DomainFactory getBaseDomainFactory() {
        return baseDomainFactory;
    }
    
    @Override
    public Regatta getOrCreateDefaultRegatta(String raceID, TrackedRegattaRegistry trackedRegattaRegistry) {
        Regatta result = trackedRegattaRegistry.getRememberedRegattaForRace(raceID);
        if (result == null) {
            result = raceIDToRegattaCache.get(raceID);
        }
        if (result == null) {
            result = new RegattaImpl(raceID, getOrCreateBoatClassFromRaceID(raceID), trackedRegattaRegistry,
                    getBaseDomainFactory().createScoringScheme(ScoringSchemeType.LOW_POINT), raceID);
            logger.info("Created regatta "+result.getName()+" ("+result.hashCode()+")");
            raceIDToRegattaCache.put(raceID, result);
        }
        return result;
    }
    
    @Override
    public Competitor getCompetitorByBoatID(String boatID) {
        return baseDomainFactory.getExistingCompetitorById(boatID);
    }
    
    @Override
    public Competitor getOrCreateCompetitor(com.sap.sailing.domain.swisstimingadapter.Competitor competitor, BoatClass boatClass) {
        Competitor result = getCompetitorByBoatID(competitor.getBoatID());
        if (result == null) {
            Boat boat = new BoatImpl(competitor.getName(), boatClass, competitor.getBoatID());
            List<Person> teamMembers = new ArrayList<Person>();
            for (String teamMemberName : competitor.getName().split("[-+&]")) {
                teamMembers.add(new PersonImpl(teamMemberName.trim(), getOrCreateNationality(competitor.getThreeLetterIOCCode()),
                        /* dateOfBirth */ null, teamMemberName.trim()));
            }
            Team team = new TeamImpl(competitor.getName(), teamMembers, /* coach */ null);
            result = baseDomainFactory.getOrCreateCompetitor(competitor.getBoatID(), competitor.getName(), team, boat);
        }
        return result;
    }

    @Override
    public Competitor getOrCreateCompetitor(String boatID, String threeLetterIOCCode, String name, 
            BoatClass boatClass) {
        return getOrCreateCompetitor(new CompetitorImpl(boatID, threeLetterIOCCode, name), boatClass);
    }

    @Override
    public RaceDefinition createRaceDefinition(Regatta regatta, String raceID, Iterable<Competitor> competitors,
            List<ControlPoint> courseDefinition) {
        List<Waypoint> waypoints = new ArrayList<>();
        for (ControlPoint controlPoint : courseDefinition) {
            Waypoint waypoint = baseDomainFactory.createWaypoint(controlPoint);
            waypoints.add(waypoint);
        }
        com.sap.sailing.domain.base.Course domainCourse = new CourseImpl("Course", waypoints);
        BoatClass boatClass = getOrCreateBoatClassFromRaceID(raceID);
        logger.info("Creating RaceDefinitionImpl for race "+raceID);
        RaceDefinition result = new RaceDefinitionImpl(raceID, domainCourse, boatClass, competitors);
        regatta.addRace(result);
        return result;
    }

    @Override
    public RaceDefinition createRaceDefinition(Regatta regatta, Race race, StartList startList, Course course) {
        com.sap.sailing.domain.base.Course domainCourse = createCourse(race.getDescription(), course);
        BoatClass boatClass = getOrCreateBoatClassFromRaceID(race.getRaceID());
        Iterable<Competitor> competitors = createCompetitorList(startList, boatClass);
        logger.info("Creating RaceDefinitionImpl for race "+race.getRaceID());
        RaceDefinition result = new RaceDefinitionImpl(race.getRaceID(), domainCourse,
                boatClass, competitors);
        regatta.addRace(result);
        return result;
    }

    @Override
    public BoatClass getOrCreateBoatClassFromRaceID(String raceID) {
        BoatClass result;
        /*
            SAM102000 Men's Windsurfer = Windsurfer Maenner RS:X
            SAW102000 Women's Windsurfer = Windsurfer Damen RS:X
            SAM004000 Men's One Person Dinghy = Laser Maenner
            SAW103000 Women's One Person Dinghy = Laser Damen Laser Radial
            SAM002000 Men's One Person Dinghy Heavy = Finn Dinghy Maenner
            SAM005000 Men's Two Person Dinghy = 470er Maenner
            SAW005000 Women's Two Person Dinghy = 470er Damen
            SAM009000 Men's Skiff = 49er Maenner
            SAM007000 Men's Keelboat = Starboot Maenner 
            SAW010000 Women's Match Racing = Matchrace Damen Elliott 6M (modified)
         */
        if (raceID.toUpperCase().startsWith("SA") && raceID.length() == 9) {
            String classID = raceID.substring(3, 6);
            result = olympicClassesByID.get(classID);
            if (result == null) {
                // unknown code
                result = unknownBoatClass;
            }
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
            Waypoint waypoint = baseDomainFactory.createWaypoint(controlPoint);
            waypoints.add(waypoint);
        }
        com.sap.sailing.domain.base.Course result = new CourseImpl(courseName, waypoints);
        return result;
    }

    @Override
    public ControlPoint getOrCreateControlPoint(Iterable<String> devices) {
        ControlPoint result;
        synchronized (controlPointCache) {
            result = controlPointCache.get(devices);
            if (result == null) {
                switch (Util.size(devices)) {
                case 1:
                    result = getOrCreateMark(devices.iterator().next());
                    break;
                case 2:
                    Iterator<String> markNameIter = devices.iterator();
                    String left = markNameIter.next();
                    String right = markNameIter.next();
                    result = baseDomainFactory.createGate(getOrCreateMark(left), getOrCreateMark(right), left + "/" + right);
                    break;
                default:
                    throw new RuntimeException(
                            "Don't know how to handle control points with number of devices neither 1 nor 2. Was "
                                    + Util.size(devices));
                }
                controlPointCache.put(devices, result);
            }
        }
        return result;
    }

    /**
     * @param id
     *            the ID which is probably also used as the "device name" and the "sail number" in case of an
     *            {@link MessageType#RPD RPD} message
     */
    @Override
    public com.sap.sailing.domain.base.Mark getOrCreateMark(String id) {
        return baseDomainFactory.getOrCreateMark(id);
    }

    @Override
    public GPSFixMoving createGPSFix(TimePoint timePointOfTransmission, Fix fix) {
        GPSFixMoving result = new GPSFixMovingImpl(fix.getPosition(), new MillisecondsTimePoint(
                timePointOfTransmission.asMillis() + fix.getAgeOfDataInMilliseconds()), fix.getSpeed());
        return result;
    }
    
    @Override
    public void updateCourseWaypoints(com.sap.sailing.domain.base.Course courseToUpdate, Iterable<Mark> marks) throws PatchFailedException {
        List<com.sap.sailing.domain.base.ControlPoint> newDomainControlPoints = new ArrayList<ControlPoint>();
        for (Mark mark : marks) {
            // TODO bug 1043: propagate the mark names to the waypoint names
            com.sap.sailing.domain.base.ControlPoint domainControlPoint = getOrCreateControlPoint(mark.getDevices());
            newDomainControlPoints.add(domainControlPoint);
        }
        courseToUpdate.update(newDomainControlPoints, baseDomainFactory);
    }

    @Override
    public MarkPassing createMarkPassing(TimePoint timePoint, Waypoint waypoint, com.sap.sailing.domain.base.Competitor competitor) {
        return baseDomainFactory.createMarkPassing(timePoint, waypoint, competitor);
    }

    @Override
    public void removeRace(String raceID) {
        Regatta regatta = raceIDToRegattaCache.get(raceID);
        if (regatta != null) {
            Set<RaceDefinition> toRemove = new HashSet<RaceDefinition>();
            for (RaceDefinition race : regatta.getAllRaces()) {
                if (race.getName().equals(raceID)) {
                    toRemove.add(race);
                }
            }
            for (RaceDefinition raceToRemove : toRemove) {
                regatta.removeRace(raceToRemove);
            }
            if (Util.isEmpty(regatta.getAllRaces())) {
                raceIDToRegattaCache.remove(raceID);
            }
        }
    }

    @Override
    public Nationality getOrCreateNationality(String threeLetterIOCCode) {
        return baseDomainFactory.getOrCreateNationality(threeLetterIOCCode);
    }

    @Override
    public RaceTrackingConnectivityParameters createTrackingConnectivityParameters(String hostname, int port, String raceID,
            boolean canSendRequests, long delayToLiveInMillis,
            SwissTimingFactory swissTimingFactory, DomainFactory domainFactory, WindStore windStore,
            RaceSpecificMessageLoader messageLoader) {
        return new SwissTimingTrackingConnectivityParameters(hostname, port, raceID, canSendRequests, delayToLiveInMillis, 
                swissTimingFactory, domainFactory, windStore, messageLoader);
    }

}
