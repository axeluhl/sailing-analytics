package com.sap.sailing.domain.tractracadapter.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.Person;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Team;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.domain.base.impl.BuoyImpl;
import com.sap.sailing.domain.base.impl.CompetitorImpl;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.base.impl.DegreeBearingImpl;
import com.sap.sailing.domain.base.impl.DegreePosition;
import com.sap.sailing.domain.base.impl.EventImpl;
import com.sap.sailing.domain.base.impl.GateImpl;
import com.sap.sailing.domain.base.impl.KilometersPerHourSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.base.impl.NationalityImpl;
import com.sap.sailing.domain.base.impl.PersonImpl;
import com.sap.sailing.domain.base.impl.RaceDefinitionImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.domain.base.impl.WaypointImpl;
import com.sap.sailing.domain.tracking.DynamicTrackedEvent;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedEvent;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedEventImpl;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRaceImpl;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.tracking.impl.MarkPassingImpl;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.JSONService;
import com.sap.sailing.domain.tractracadapter.RaceTracker;
import com.sap.sailing.domain.tractracadapter.Receiver;
import com.sap.sailing.domain.tractracadapter.ReceiverType;
import com.tractrac.clientmodule.CompetitorClass;
import com.tractrac.clientmodule.ControlPoint;
import com.tractrac.clientmodule.Race;
import com.tractrac.clientmodule.RaceCompetitor;
import com.tractrac.clientmodule.data.ControlPointPositionData;

import difflib.DiffUtils;
import difflib.Patch;
import difflib.PatchFailedException;

public class DomainFactoryImpl implements DomainFactory {
    private static final Logger logger = Logger.getLogger(DomainFactoryImpl.class.getName());
    
    private final long millisecondsOverWhichToAverageSpeed = 5000;

    // TODO clarify how millisecondsOverWhichToAverageWind could be updated and propagated live
    private final long millisecondsOverWhichToAverageWind = 30000;
    
    // TODO consider (re-)introducing WeakHashMaps for cache structures, but such that the cache is maintained as long as our domain objects are strongly referenced
    private final Map<ControlPoint, com.sap.sailing.domain.base.ControlPoint> controlPointCache =
        new HashMap<ControlPoint, com.sap.sailing.domain.base.ControlPoint>();
    
    private final Map<com.tractrac.clientmodule.Competitor, com.sap.sailing.domain.base.Competitor> competitorCache =
            new HashMap<com.tractrac.clientmodule.Competitor, com.sap.sailing.domain.base.Competitor>();
    
    /**
     * Ensure that the <em>same</em> string is used as key that is also used to set the {@link Nationality}
     * object's {@link Nationality#getName() name}.
     */
    private final Map<String, Nationality> nationalityCache = new HashMap<String, Nationality>();
    
    private final Map<String, Person> personCache = new HashMap<String, Person>();
    
    private final Map<String, Team> teamCache = new HashMap<String, Team>();
    
    private final Map<CompetitorClass, BoatClass> classCache = new HashMap<CompetitorClass, BoatClass>();
    
    /**
     * Caches events by their name
     */
    private final Map<String, com.sap.sailing.domain.base.Event> eventCache =
            new HashMap<String, com.sap.sailing.domain.base.Event>();
    
    private final Map<com.tractrac.clientmodule.Event, RaceDefinition> tractracEventToRaceDefinitionMap = new HashMap<com.tractrac.clientmodule.Event, RaceDefinition>();
    
    private final Map<Race, RaceDefinition> raceCache = new HashMap<Race, RaceDefinition>();
    
    private final Map<Event, DynamicTrackedEvent> eventTrackingCache = new HashMap<Event, DynamicTrackedEvent>();

    @Override
    public Position createPosition(
            com.tractrac.clientmodule.data.Position position) {
        return new DegreePosition(position.getLatitude(), position.getLongitude());
    }
   
    @Override
    public GPSFixMoving createGPSFixMoving(com.tractrac.clientmodule.data.Position position) {
        GPSFixMoving result = new GPSFixMovingImpl(createPosition(position), new MillisecondsTimePoint(position.getTimestamp()),
                new KilometersPerHourSpeedWithBearingImpl(position.getSpeed(), new DegreeBearingImpl(position.getDirection())));
        return result;
    }
    
    @Override
    public TimePoint createTimePoint(long timestamp) {
        return new MillisecondsTimePoint(timestamp);
    }
    
    @Override
    public void updateCourseWaypoints(Course courseToUpdate, List<ControlPoint> controlPoints) throws PatchFailedException {
        Iterable<Waypoint> courseWaypoints = courseToUpdate.getWaypoints();
        List<Waypoint> newWaypointList = new LinkedList<Waypoint>();
        for (ControlPoint tractracControlPoint : controlPoints) {
            Waypoint waypoint = createWaypoint(tractracControlPoint);
            newWaypointList.add(waypoint);
        }
        Patch<Waypoint> patch = DiffUtils.diff(courseWaypoints, newWaypointList);
        CourseAsWaypointList courseAsWaypointList = new CourseAsWaypointList(courseToUpdate);
        patch.applyToInPlace(courseAsWaypointList);
    }

    @Override
    public Waypoint createWaypoint(ControlPoint controlPoint) {
        com.sap.sailing.domain.base.ControlPoint domainControlPoint = getControlPoint(controlPoint);
        return new WaypointImpl(domainControlPoint);
    }

    public com.sap.sailing.domain.base.ControlPoint getControlPoint(ControlPoint controlPoint) {
        synchronized (controlPointCache) {
            com.sap.sailing.domain.base.ControlPoint domainControlPoint = controlPointCache.get(controlPoint);
            if (domainControlPoint == null) {
                if (controlPoint.getHasTwoPoints()) {
                    // it's a gate
                    domainControlPoint = new GateImpl(new BuoyImpl(controlPoint.getName() + " (left)"), new BuoyImpl(
                            controlPoint.getName() + " (right)"), controlPoint.getName());
                } else {
                    domainControlPoint = new BuoyImpl(controlPoint.getName());
                }
            }
            controlPointCache.put(controlPoint, domainControlPoint);
            return domainControlPoint;
        }
    }
    
    @Override
    public Course createCourse(String name, Iterable<ControlPoint> controlPoints) {
        List<Waypoint> waypointList = new ArrayList<Waypoint>();
        for (ControlPoint controlPoint : controlPoints) {
            Waypoint waypoint = createWaypoint(controlPoint);
            waypointList.add(waypoint);
        }
        return new CourseImpl(name, waypointList);
    }

    @Override
    public Competitor getCompetitor(com.tractrac.clientmodule.Competitor competitor) {
        synchronized (competitorCache) {
            Competitor result = competitorCache.get(competitor);
            if (result == null) {
                BoatClass boatClass = getBoatClass(competitor.getCompetitorClass());
                Nationality nationality = getNationality(competitor.getNationality());
                Team team = getTeam(competitor.getName(), nationality);
                Boat boat = new BoatImpl(competitor.getShortName(), boatClass);
                result = new CompetitorImpl(competitor.getId(), competitor.getName(), team, boat);
                competitorCache.put(competitor, result);
            }
            return result;
        }
    }

    @Override
    public Team getTeam(String name, Nationality nationality) {
        synchronized (teamCache) {
            Team result = teamCache.get(name);
            if (result == null) {
                String[] sailorNames = name.split("\\b*\\+\\b*");
                List<Person> sailors = new ArrayList<Person>();
                for (String sailorName : sailorNames) {
                    sailors.add(getPerson(sailorName.trim(), nationality));
                }
                result = new TeamImpl(name, sailors, /* TODO coach not known */null);
                teamCache.put(name, result);
            }
            return result;
        }
    }

    @Override
    public Person getPerson(String name, Nationality nationality) {
        synchronized (personCache) {
            Person result = personCache.get(name);
            if (result == null) {
                result = new PersonImpl(name, nationality, /* date of birth unknown */null, /* description */"");
                personCache.put(name, result);
            }
            return result;
        }
    }

    @Override
    public BoatClass getBoatClass(CompetitorClass competitorClass) {
        synchronized (classCache) {
            BoatClass result = classCache.get(competitorClass);
            if (result == null) {
                result = new BoatClassImpl(competitorClass.getName());
                classCache.put(competitorClass, result);
            }
            return result;
        }
    }

    @Override
    public Nationality getNationality(String nationalityName) {
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
    public RaceDefinition getExistingRaceDefinitionForRace(Race race) {
        return raceCache.get(race);
    }

    @Override
    public RaceDefinition getRaceDefinition(Race race) {
        synchronized (raceCache) {
            RaceDefinition result = raceCache.get(race);
            boolean interrupted = false;
            synchronized (raceCache) {
                while (!interrupted && result == null) {
                    try {
                        raceCache.wait();
                        result = raceCache.get(race);
                    } catch (InterruptedException e) {
                        interrupted = true;
                    }
                }
            }
            return result;
        }
    }

    @Override
    public Event createEvent(com.tractrac.clientmodule.Event event) {
        synchronized (eventCache) {
            Event result = eventCache.get(event.getName());
            if (result == null) {
                result = new EventImpl(event.getName());
                eventCache.put(event.getName(), result);
            }
            return result;
        }
    }
    
    @Override
    public Iterable<Receiver> getUpdateReceivers(DynamicTrackedEvent trackedEvent, com.tractrac.clientmodule.Event tractracEvent, WindStore windStore, ReceiverType... types) {
        Collection<Receiver> result = new ArrayList<Receiver>();
        for (ReceiverType type : types) {
            switch (type) {
            case RACECOURSE:
                result.add(new RaceCourseReceiver(
                        trackedEvent, tractracEvent, windStore, millisecondsOverWhichToAverageWind, millisecondsOverWhichToAverageSpeed));
                break;
            case MARKPOSITIONS:
                result.add(new MarkPositionReceiver(
                        trackedEvent, tractracEvent, this));
                break;
            case RAWPOSITIONS:
                result.add(new RawPositionReceiver(
                        trackedEvent, tractracEvent));
                break;
            case MARKPASSINGS:
                result.add(new MarkPassingReceiver(
                        trackedEvent, tractracEvent));
                break;
            case RACESTARTFINISH:
                result.add(new RaceStartedAndFinishedReceiver(
                        trackedEvent, tractracEvent));
                break;
            }
        }
        return result;
    }

    @Override
    public Iterable<Receiver> getUpdateReceivers(DynamicTrackedEvent trackedEvent,
            com.tractrac.clientmodule.Event tractracEvent, WindStore windStore) {
        return getUpdateReceivers(trackedEvent, tractracEvent, windStore, ReceiverType.RACECOURSE,
                ReceiverType.MARKPASSINGS, ReceiverType.MARKPOSITIONS, ReceiverType.RACESTARTFINISH,
                ReceiverType.RAWPOSITIONS);
    }

    @Override
    public DynamicTrackedEvent trackEvent(com.sap.sailing.domain.base.Event event) {
        synchronized (eventTrackingCache) {
            DynamicTrackedEvent result = eventTrackingCache.get(event);
            if (result == null) {
                result = new DynamicTrackedEventImpl(event, millisecondsOverWhichToAverageSpeed);
                eventTrackingCache.put(event, result);
            }
            return result;
        }
    }
    
    @Override
    public RaceDefinition createRaceDefinition(Race race, Course course) {
        synchronized (raceCache) {
            RaceDefinition result = raceCache.get(race);
            if (result == null) {
                BoatClass boatClass = null;
                final List<Competitor> competitors = new ArrayList<Competitor>();
                for (RaceCompetitor rc : race.getRaceCompetitorList()) {
                    com.tractrac.clientmodule.Competitor competitor = rc.getCompetitor();
                    if (boatClass == null) {
                        boatClass = DomainFactory.INSTANCE.getBoatClass(competitor.getCompetitorClass());
                    }
                    competitors.add(DomainFactory.INSTANCE.getCompetitor(rc.getCompetitor()));
                }
                result = new RaceDefinitionImpl(race.getName(), course, boatClass, competitors);
                synchronized (raceCache) {
                    raceCache.put(race, result);
                    raceCache.notifyAll();
                }
            } else {
                throw new RuntimeException("Race "+race.getName()+" already exists");
            }
            return result;
        }
    }

    @Override
    public Buoy getBuoy(ControlPoint controlPoint, ControlPointPositionData record) {
        com.sap.sailing.domain.base.ControlPoint myControlPoint = getControlPoint(controlPoint);
        Buoy result;
        Iterator<Buoy> iter = myControlPoint.getBuoys().iterator();
        if (controlPoint.getHasTwoPoints()) {
            if (record.getIndex() == 0) {
                result = iter.next();
            } else {
                iter.next();
                result = iter.next();
            }
        } else {
            result = iter.next();
        }
        return result;
    }

    @Override
    public MarkPassing createMarkPassing(com.tractrac.clientmodule.Competitor competitor, Waypoint passed, TimePoint time) {
        MarkPassing result = new MarkPassingImpl(time, passed, getCompetitor(competitor));
        return result;
    }

    @Override
    public RaceTracker createRaceTracker(URL paramURL, URI liveURI, URI storedURI, WindStore windStore) throws MalformedURLException,
            FileNotFoundException, URISyntaxException {
        return new RaceTrackerImpl(this, paramURL, liveURI, storedURI, windStore);
    }

    @Override
    public RaceDefinition getRace(com.tractrac.clientmodule.Event tractracEvent) {
        return tractracEventToRaceDefinitionMap.get(tractracEvent);
    }

    @Override
    public DynamicTrackedRace trackRace(TrackedEvent trackedEvent, RaceDefinition raceDefinition,
            WindStore windStore, long millisecondsOverWhichToAverageWind,
            long millisecondsOverWhichToAverageSpeed, com.tractrac.clientmodule.Event tractracEvent) {
        logger.log(Level.INFO, "Creating DynamicTrackedRaceImpl for RaceDefinition "+raceDefinition.getName());
        DynamicTrackedRaceImpl result = new DynamicTrackedRaceImpl(trackedEvent, raceDefinition,
                windStore, millisecondsOverWhichToAverageWind, millisecondsOverWhichToAverageSpeed);
        tractracEventToRaceDefinitionMap.put(tractracEvent, raceDefinition);
        return result;
    }

    @Override
    public JSONService parseJSONURL(URL jsonURL) throws IOException, ParseException, org.json.simple.parser.ParseException {
        return new JSONServiceImpl(jsonURL);
    }

}
