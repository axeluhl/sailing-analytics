package com.sap.sailing.domain.tractracadapter.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.domain.base.impl.CompetitorImpl;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.base.impl.DegreeBearingImpl;
import com.sap.sailing.domain.base.impl.DegreePosition;
import com.sap.sailing.domain.base.impl.EventImpl;
import com.sap.sailing.domain.base.impl.KilometersPerHourSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.base.impl.PersonImpl;
import com.sap.sailing.domain.base.impl.RaceDefinitionImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.domain.common.Util;
import com.sap.sailing.domain.common.Util.Pair;
import com.sap.sailing.domain.tracking.DynamicRaceDefinitionSet;
import com.sap.sailing.domain.tracking.DynamicTrackedEvent;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedEvent;
import com.sap.sailing.domain.tracking.TrackedEventRegistry;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.JSONService;
import com.sap.sailing.domain.tractracadapter.Receiver;
import com.sap.sailing.domain.tractracadapter.ReceiverType;
import com.sap.sailing.domain.tractracadapter.TracTracConfiguration;
import com.sap.sailing.domain.tractracadapter.TracTracRaceTracker;
import com.tractrac.clientmodule.CompetitorClass;
import com.tractrac.clientmodule.ControlPoint;
import com.tractrac.clientmodule.Race;
import com.tractrac.clientmodule.RaceCompetitor;
import com.tractrac.clientmodule.data.ControlPointPositionData;

import difflib.PatchFailedException;

public class DomainFactoryImpl implements DomainFactory {
    private static final Logger logger = Logger.getLogger(DomainFactoryImpl.class.getName());
    
    private final com.sap.sailing.domain.base.DomainFactory baseDomainFactory;
    
    // TODO consider (re-)introducing WeakHashMaps for cache structures, but such that the cache is maintained as long as our domain objects are strongly referenced
    private final Map<ControlPoint, com.sap.sailing.domain.base.ControlPoint> controlPointCache =
        new HashMap<ControlPoint, com.sap.sailing.domain.base.ControlPoint>();
    
    private final Map<com.tractrac.clientmodule.Competitor, com.sap.sailing.domain.base.Competitor> competitorCache =
            new HashMap<com.tractrac.clientmodule.Competitor, com.sap.sailing.domain.base.Competitor>();
    
    private final Map<String, Person> personCache = new HashMap<String, Person>();
    
    private final Map<String, Team> teamCache = new HashMap<String, Team>();
    
    /**
     * Caches events by their name and their boat class's name
     */
    private final Map<Pair<String, String>, com.sap.sailing.domain.base.Event> eventCache =
            new HashMap<Pair<String, String>, com.sap.sailing.domain.base.Event>();
    
    private final Map<Race, RaceDefinition> raceCache = new HashMap<Race, RaceDefinition>();

    private final Set<String> mayStartWithNoUpwindLeg;
    
    public DomainFactoryImpl(com.sap.sailing.domain.base.DomainFactory baseDomainFactory) {
        this.baseDomainFactory = baseDomainFactory;
        mayStartWithNoUpwindLeg = new HashSet<String>(Arrays.asList(new String[] { "extreme40", "ess", "ess40" }));
    }

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
        List<com.sap.sailing.domain.base.ControlPoint> newDomainControlPoints = new ArrayList<com.sap.sailing.domain.base.ControlPoint>();
        for (ControlPoint tractracControlPoint : controlPoints) {
            com.sap.sailing.domain.base.ControlPoint newDomainControlPoint = getOrCreateControlPoint(tractracControlPoint);
            newDomainControlPoints.add(newDomainControlPoint);
        }
        courseToUpdate.update(newDomainControlPoints, baseDomainFactory);
    }

    public com.sap.sailing.domain.base.ControlPoint getOrCreateControlPoint(ControlPoint controlPoint) {
        synchronized (controlPointCache) {
            com.sap.sailing.domain.base.ControlPoint domainControlPoint = controlPointCache.get(controlPoint);
            if (domainControlPoint == null) {
                if (controlPoint.getHasTwoPoints()) {
                    // it's a gate
                    domainControlPoint = baseDomainFactory.createGate(baseDomainFactory.getOrCreateBuoy(controlPoint.getName() + " (left)"),
                            baseDomainFactory.getOrCreateBuoy(controlPoint.getName() + " (right)"), controlPoint.getName());
                } else {
                    domainControlPoint = baseDomainFactory.getOrCreateBuoy(controlPoint.getName());
                }
                controlPointCache.put(controlPoint, domainControlPoint);
            }
            return domainControlPoint;
        }
    }
    
    @Override
    public Course createCourse(String name, Iterable<ControlPoint> controlPoints) {
        List<Waypoint> waypointList = new ArrayList<Waypoint>();
        for (ControlPoint controlPoint : controlPoints) {
            Waypoint waypoint = baseDomainFactory.createWaypoint(getOrCreateControlPoint(controlPoint));
            waypointList.add(waypoint);
        }
        return new CourseImpl(name, waypointList);
    }

    @Override
    public Competitor getOrCreateCompetitor(com.tractrac.clientmodule.Competitor competitor) {
        synchronized (competitorCache) {
            Competitor result = competitorCache.get(competitor);
            if (result == null) {
                BoatClass boatClass = getOrCreateBoatClass(competitor.getCompetitorClass());
                Nationality nationality = getOrCreateNationality(competitor.getNationality());
                Team team = getOrCreateTeam(competitor.getName(), nationality);
                Boat boat = new BoatImpl(competitor.getShortName(), boatClass, competitor.getShortName());
                result = new CompetitorImpl(competitor.getId(), competitor.getName(), team, boat);
                competitorCache.put(competitor, result);
            }
            return result;
        }
    }

    @Override
    public Team getOrCreateTeam(String name, Nationality nationality) {
        synchronized (teamCache) {
            Team result = teamCache.get(name);
            if (result == null) {
                String[] sailorNames = name.split("\\b*\\+\\b*");
                List<Person> sailors = new ArrayList<Person>();
                for (String sailorName : sailorNames) {
                    sailors.add(getOrCreatePerson(sailorName.trim(), nationality));
                }
                result = new TeamImpl(name, sailors, /* TODO coach not known */null);
                teamCache.put(name, result);
            }
            return result;
        }
    }

    @Override
    public Person getOrCreatePerson(String name, Nationality nationality) {
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
    public BoatClass getOrCreateBoatClass(CompetitorClass competitorClass) {
        return baseDomainFactory.getOrCreateBoatClass(competitorClass == null ? "" : competitorClass.getName(),
        /* typicallyStartsUpwind */ !mayStartWithNoUpwindLeg.contains(competitorClass.getName()
                .toLowerCase()));
    }

    @Override
    public Nationality getOrCreateNationality(String nationalityName) {
        return baseDomainFactory.getOrCreateNationality(nationalityName);
    }
    
    @Override
    public RaceDefinition getExistingRaceDefinitionForRace(Race race) {
        return raceCache.get(race);
    }

    @Override
    public RaceDefinition getAndWaitForRaceDefinition(Race race) {
        return getAndWaitForRaceDefinition(race, -1);
    }

    @Override
    public RaceDefinition getAndWaitForRaceDefinition(Race race, long timeoutInMilliseconds) {
        long start = System.currentTimeMillis();
        synchronized (raceCache) {
            RaceDefinition result = raceCache.get(race);
            boolean interrupted = false;
            while ((timeoutInMilliseconds == -1 || System.currentTimeMillis()-start < timeoutInMilliseconds) && !interrupted && result == null) {
                try {
                    raceCache.wait();
                    result = raceCache.get(race);
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
            return result;
        }
    }

    @Override
    public Event getOrCreateEvent(com.tractrac.clientmodule.Event event) {
        synchronized (eventCache) {
            // FIXME Dialog with Lasse by Skype on 2011-06-17:
            //            [6:20:04 PM] Axel Uhl: Lasse, can Event.getCompetitorClassList() ever produce more than one result?
            //            [6:20:20 PM] Axel Uhl: Or is it similar to Event.getRaceList() which always delivers one Race?
            //            [6:22:19 PM] Lasse Staffensen: It can deliver several classes, if more classes are present in a race.
            //            [6:27:20 PM] Axel Uhl: Will that happen at Kiel Week?
            //            [6:27:58 PM] Lasse Staffensen: No
            //            [6:28:34 PM] Axel Uhl: Good :)
            // This means that currently it is permissible to assume that we'll get at most one
            // boat class per TracTrac event. Generally, however, we have to assume that
            // one TracTrac event may map to multiple domain Event objects with one BoatClass each
            Collection<CompetitorClass> competitorClassList = new ArrayList<CompetitorClass>();
            for (com.tractrac.clientmodule.Competitor c : event.getCompetitorList()) {
                competitorClassList.add(c.getCompetitorClass());
            }
            BoatClass boatClass = getDominantBoatClass(competitorClassList);
            Pair<String, String> key = new Pair<String, String>(event.getName(), boatClass==null?null:boatClass.getName());
            Event result = eventCache.get(key);
            if (result == null) {
                result = new EventImpl(event.getName(), boatClass);
                eventCache.put(key, result);
            }
            return result;
        }
    }
    
    @Override
    public Iterable<Receiver> getUpdateReceivers(DynamicTrackedEvent trackedEvent, com.tractrac.clientmodule.Event tractracEvent,
            WindStore windStore, DynamicRaceDefinitionSet raceDefinitionSetToUpdate, ReceiverType... types) {
        Collection<Receiver> result = new ArrayList<Receiver>();
        for (ReceiverType type : types) {
            switch (type) {
            case RACECOURSE:
                result.add(new RaceCourseReceiver(
                        this, trackedEvent, tractracEvent, windStore,
                        raceDefinitionSetToUpdate, WindTrack.DEFAULT_MILLISECONDS_OVER_WHICH_TO_AVERAGE_WIND));
                break;
            case MARKPOSITIONS:
                result.add(new MarkPositionReceiver(
                        trackedEvent, tractracEvent, this));
                break;
            case RAWPOSITIONS:
                result.add(new RawPositionReceiver(
                        trackedEvent, tractracEvent, this));
                break;
            case MARKPASSINGS:
                result.add(new MarkPassingReceiver(
                        trackedEvent, tractracEvent, this));
                break;
            case RACESTARTFINISH:
                result.add(new RaceStartedAndFinishedReceiver(
                        trackedEvent, tractracEvent, this));
                break;
            }
        }
        return result;
    }

    @Override
    public Iterable<Receiver> getUpdateReceivers(DynamicTrackedEvent trackedEvent,
            com.tractrac.clientmodule.Event tractracEvent, WindStore windStore, DynamicRaceDefinitionSet raceDefinitionSetToUpdate) {
        return getUpdateReceivers(trackedEvent, tractracEvent, windStore, raceDefinitionSetToUpdate, ReceiverType.RACECOURSE,
                ReceiverType.MARKPASSINGS, ReceiverType.MARKPOSITIONS, ReceiverType.RACESTARTFINISH,
                ReceiverType.RAWPOSITIONS);
    }
    
    @Override
    public void removeRace(com.tractrac.clientmodule.Event tractracEvent, Race tractracRace, TrackedEventRegistry trackedEventRegistry) {
        RaceDefinition raceDefinition = getExistingRaceDefinitionForRace(tractracRace);
        if (raceDefinition != null) { // otherwise, this domain factory doesn't seem to know about the race
            raceCache.remove(tractracRace);
            Collection<CompetitorClass> competitorClassList = new ArrayList<CompetitorClass>();
            for (com.tractrac.clientmodule.Competitor c : tractracEvent.getCompetitorList()) {
                competitorClassList.add(c.getCompetitorClass());
            }
            BoatClass boatClass = getDominantBoatClass(competitorClassList);
            Pair<String, String> key = new Pair<String, String>(tractracEvent.getName(), boatClass == null ? null
                    : boatClass.getName());
            Event event = eventCache.get(key);
            if (event != null) {
                event.removeRace(raceDefinition);
                if (Util.size(event.getAllRaces()) == 0) {
                    eventCache.remove(key);
                }
                TrackedEvent trackedEvent = trackedEventRegistry.getTrackedEvent(event);
                if (trackedEvent != null) {
                    trackedEvent.removeTrackedRace(raceDefinition);
                    if (Util.size(trackedEvent.getTrackedRaces()) == 0) {
                        trackedEventRegistry.removeTrackedEvent(event);
                    }
                }
            }
        }
    }

    @Override
    public Pair<RaceDefinition, TrackedRace> getOrCreateRaceDefinitionAndTrackedRace(TrackedEvent trackedEvent,
            Race race, Course course, WindStore windStore, long millisecondsOverWhichToAverageWind,
            DynamicRaceDefinitionSet raceDefinitionSetToUpdate) {
        synchronized (raceCache) {
            RaceDefinition raceDefinition = raceCache.get(race);
            if (raceDefinition == null) {
                Pair<List<Competitor>, BoatClass> competitorsAndDominantBoatClass = getCompetitorsAndDominantBoatClass(race);
                raceDefinition = new RaceDefinitionImpl(race.getName(), course, competitorsAndDominantBoatClass.getB(),
                        competitorsAndDominantBoatClass.getA());
                // add to domain Event only if boat class matches
                if (raceDefinition.getBoatClass() == trackedEvent.getEvent().getBoatClass()) {
                    trackedEvent.getEvent().addRace(raceDefinition);
                } else {
                    logger.warning("Not adding race "+raceDefinition+" to event "+trackedEvent.getEvent()+
                            " because boat class "+raceDefinition.getBoatClass()+" doesn't match event's boat class "+
                            trackedEvent.getEvent().getBoatClass());
                }
                TrackedRace trackedRace = createTrackedRace(trackedEvent, raceDefinition, windStore,
                        millisecondsOverWhichToAverageWind, raceDefinitionSetToUpdate);
                synchronized (raceCache) {
                    raceCache.put(race, raceDefinition);
                    raceCache.notifyAll();
                }
                return new Pair<RaceDefinition, TrackedRace>(raceDefinition, trackedRace);
            } else {
                throw new RuntimeException("Race "+race.getName()+" already exists");
            }
        }
    }

    private TrackedRace createTrackedRace(TrackedEvent trackedEvent, RaceDefinition race, WindStore windStore,
            long millisecondsOverWhichToAverageWind, DynamicRaceDefinitionSet raceDefinitionSetToUpdate) {
        return trackedEvent.createTrackedRace(race,
                windStore, millisecondsOverWhichToAverageWind,
                /* time over which to average speed: */ race.getBoatClass().getApproximateManeuverDurationInMilliseconds(),
                raceDefinitionSetToUpdate);
    }
    
    @Override
    public Pair<List<Competitor>, BoatClass> getCompetitorsAndDominantBoatClass(Race race) {
        List<CompetitorClass> competitorClasses = new ArrayList<CompetitorClass>();
        final List<Competitor> competitors = new ArrayList<Competitor>();
        for (RaceCompetitor rc : race.getRaceCompetitorList()) {
            // also add those whose race class doesn't match the dominant one (such as camera boats)
            // because they may still send data that we would like to record in some tracks
            competitors.add(getOrCreateCompetitor(rc.getCompetitor()));
            competitorClasses.add(rc.getCompetitor().getCompetitorClass());
        }
        BoatClass dominantBoatClass = getDominantBoatClass(competitorClasses);
        Pair<List<Competitor>, BoatClass> competitorsAndDominantBoatClass = new Pair<List<Competitor>, BoatClass>(
                competitors, dominantBoatClass);
        return competitorsAndDominantBoatClass;
    }

    private BoatClass getDominantBoatClass(Collection<CompetitorClass> competitorClasses) {
        Map<BoatClass, Integer> countsPerBoatClass = new HashMap<BoatClass, Integer>();
        BoatClass dominantBoatClass = null;
        int numberOfCompetitorsInDominantBoatClass = 0;
        for (CompetitorClass cc : competitorClasses) {
            BoatClass boatClass = getOrCreateBoatClass(cc);
            Integer boatClassCount = countsPerBoatClass.get(boatClass);
            if (boatClassCount == null) {
                boatClassCount = 0;
            }
            boatClassCount = boatClassCount + 1;
            countsPerBoatClass.put(boatClass, boatClassCount);
            if (boatClassCount > numberOfCompetitorsInDominantBoatClass) {
                numberOfCompetitorsInDominantBoatClass = boatClassCount;
                dominantBoatClass = boatClass;
            }
        }
        return dominantBoatClass;
    }

    @Override
    public Buoy getBuoy(ControlPoint controlPoint, ControlPointPositionData record) {
        com.sap.sailing.domain.base.ControlPoint myControlPoint = getOrCreateControlPoint(controlPoint);
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
    public MarkPassing createMarkPassing(TimePoint timePoint, Waypoint passed, com.sap.sailing.domain.base.Competitor competitor) {
        return baseDomainFactory.createMarkPassing(timePoint, passed, competitor);
    }

    @Override
    public TracTracRaceTracker createRaceTracker(URL paramURL, URI liveURI, URI storedURI, WindStore windStore,
            TrackedEventRegistry trackedEventRegistry) throws MalformedURLException, FileNotFoundException,
            URISyntaxException {
        return new TracTracRaceTrackerImpl(this, paramURL, liveURI, storedURI, windStore, trackedEventRegistry);
    }

    @Override
    public DynamicTrackedRace trackRace(DynamicTrackedEvent trackedEvent, RaceDefinition raceDefinition,
            WindStore windStore, long millisecondsOverWhichToAverageWind,
            long millisecondsOverWhichToAverageSpeed, DynamicRaceDefinitionSet raceDefinitionSetToUpdate) {
        return trackedEvent.createTrackedRace(raceDefinition, windStore, millisecondsOverWhichToAverageWind, millisecondsOverWhichToAverageSpeed, raceDefinitionSetToUpdate);
    }

    @Override
    public JSONService parseJSONURL(URL jsonURL) throws IOException, ParseException, org.json.simple.parser.ParseException, URISyntaxException {
        return new JSONServiceImpl(jsonURL);
    }

    @Override
    public TracTracConfiguration createTracTracConfiguration(String name, String jsonURL, String liveDataURI, String storedDataURI) {
        return new TracTracConfigurationImpl(name, jsonURL, liveDataURI, storedDataURI);
    }

}
