package com.sap.sailing.domain.tractracadapter.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.WeakHashMap;

import com.maptrack.client.io.TypeController;
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
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedEventImpl;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.tracking.impl.MarkPassingImpl;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.tractrac.clientmodule.CompetitorClass;
import com.tractrac.clientmodule.ControlPoint;
import com.tractrac.clientmodule.Race;
import com.tractrac.clientmodule.RaceCompetitor;
import com.tractrac.clientmodule.data.ControlPointPositionData;

public class DomainFactoryImpl implements DomainFactory {
    // TODO clarify how millisecondsOverWhichToAverageWind could be updated and propagated live
    private final long millisecondsOverWhichToAverageWind = 30000;
    
    private final WeakHashMap<ControlPoint, com.sap.sailing.domain.base.ControlPoint> controlPointCache =
        new WeakHashMap<ControlPoint, com.sap.sailing.domain.base.ControlPoint>();
    
    private final WeakHashMap<com.tractrac.clientmodule.Competitor, com.sap.sailing.domain.base.Competitor> competitorCache =
            new WeakHashMap<com.tractrac.clientmodule.Competitor, com.sap.sailing.domain.base.Competitor>();
    
    /**
     * Ensure that the <em>same</em> string is used as key that is also used to set the {@link Nationality}
     * object's {@link Nationality#getName() name}.
     */
    private final WeakHashMap<String, Nationality> nationalityCache = new WeakHashMap<String, Nationality>();
    
    private final WeakHashMap<String, Person> personCache = new WeakHashMap<String, Person>();
    
    private final WeakHashMap<String, Team> teamCache = new WeakHashMap<String, Team>();
    
    private final WeakHashMap<CompetitorClass, BoatClass> classCache = new WeakHashMap<CompetitorClass, BoatClass>();
    
    private final WeakHashMap<com.tractrac.clientmodule.Event, com.sap.sailing.domain.base.Event> eventCache =
            new WeakHashMap<com.tractrac.clientmodule.Event, com.sap.sailing.domain.base.Event>();
    
    private final WeakHashMap<com.sap.sailing.domain.base.Event, com.tractrac.clientmodule.Event> inverseEventCache =
            new WeakHashMap<com.sap.sailing.domain.base.Event, com.tractrac.clientmodule.Event>();
    
    private final WeakHashMap<Race, RaceDefinition> raceCache = new WeakHashMap<Race, RaceDefinition>();
    
    private final WeakHashMap<Event, DynamicTrackedEvent> eventTrackingCache = new WeakHashMap<Event, DynamicTrackedEvent>();

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
    public Waypoint createWaypoint(ControlPoint controlPoint) {
        com.sap.sailing.domain.base.ControlPoint domainControlPoint = getControlPoint(controlPoint);
        return new WaypointImpl(domainControlPoint);
    }

    public com.sap.sailing.domain.base.ControlPoint getControlPoint(ControlPoint controlPoint) {
        com.sap.sailing.domain.base.ControlPoint domainControlPoint = controlPointCache.get(controlPoint);
        if (domainControlPoint == null) {
            if (controlPoint.getHasTwoPoints()) {
                // it's a gate
                domainControlPoint = new GateImpl(new BuoyImpl(
                        controlPoint.getName() + " (left)"), new BuoyImpl(
                        controlPoint.getName() + " (right)"),
                        controlPoint.getName());
            } else {
                domainControlPoint = new BuoyImpl(controlPoint.getName());
            }
        }
        controlPointCache.put(controlPoint, domainControlPoint);
        return domainControlPoint;
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
        Competitor result = competitorCache.get(competitor);
        if (result == null) {
            BoatClass boatClass = getBoatClass(competitor.getCompetitorClass());
            Nationality nationality = getNationality(competitor.getNationality());
            Team team = getTeam(competitor.getName(), nationality);
            Boat boat = new BoatImpl(competitor.getShortName(), boatClass);
            result = new CompetitorImpl(competitor.getName(), team, boat);
            competitorCache.put(competitor, result);
        }
        return result;
    }

    @Override
    public Team getTeam(String name, Nationality nationality) {
        Team result = teamCache.get(name);
        if (result == null) {
            String[] sailorNames = name.split("\\b*\\+\\b*");
            List<Person> sailors = new ArrayList<Person>();
            for (String sailorName : sailorNames) {
                sailors.add(getPerson(sailorName, nationality));
            }
            result = new TeamImpl(name, sailors, /* TODO coach not known */ null);
            teamCache.put(name, result);
        }
        return result;
    }

    @Override
    public Person getPerson(String name, Nationality nationality) {
        Person result = personCache.get(name);
        if (result == null) {
            result = new PersonImpl(name, nationality, /* date of birth unknown */ null, /* description */ "");
            personCache.put(name, result);
        }
        return result;
    }

    @Override
    public BoatClass getBoatClass(CompetitorClass competitorClass) {
        BoatClass result = classCache.get(competitorClass);
        if (result == null) {
            result = new BoatClassImpl(competitorClass.getName());
            classCache.put(competitorClass, result);
        }
        return result;
    }

    @Override
    public Nationality getNationality(String nationalityName) {
        Nationality result = nationalityCache.get(nationalityName);
        if (result == null) {
            result = new NationalityImpl(nationalityName, nationalityName);
            nationalityCache.put(nationalityName, result);
        }
        return result;
    }

    @Override
    public RaceDefinition getRaceDefinition(Race race) {
        RaceDefinition result = raceCache.get(race);
        boolean interrupted = false;
        synchronized (raceCache) {
            while (!interrupted && result == null) {
                try {
                    raceCache.wait();
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
        }
        return result;
    }

    @Override
    public Event createEvent(com.tractrac.clientmodule.Event event) {
        Event result = eventCache.get(event);
        if (result == null) {
            result = new EventImpl(event.getName());
            eventCache.put(event, result);
            inverseEventCache.put(result, event);
        }
        return result;
    }

    @Override
    public Iterable<TypeController> getUpdateReceivers(
            DynamicTrackedEvent trackedEvent) {
        Collection<TypeController> result = new ArrayList<TypeController>();
        for (TypeController raceCourseReceiver : new RaceCourseReceiver(
                trackedEvent, inverseEventCache.get(trackedEvent
                        .getEvent()), millisecondsOverWhichToAverageWind).getRouteListeners()) {
            result.add(raceCourseReceiver);
        }
        for (TypeController markPositionReceiver : new MarkPositionReceiver(
                trackedEvent, inverseEventCache.get(trackedEvent
                        .getEvent())).getControlPointListeners()) {
            result.add(markPositionReceiver);
        }
        for (TypeController rawPositionReceiver : new RawPositionReceiver(
                trackedEvent, inverseEventCache.get(trackedEvent
                        .getEvent())).getRawPositionListeners()) {
            result.add(rawPositionReceiver);
        }
        for (TypeController markRoundingReceiver : new MarkPassingReceiver(
                trackedEvent, inverseEventCache.get(trackedEvent
                        .getEvent())).getMarkPassingListeners()) {
            result.add(markRoundingReceiver);
        }
        return result;
    }

    @Override
    public DynamicTrackedEvent trackEvent(com.sap.sailing.domain.base.Event event) {
        DynamicTrackedEvent result = eventTrackingCache.get(event);
        if (result == null) {
            result = new DynamicTrackedEventImpl(event);
            eventTrackingCache.put(event, result);
        }
        return result;
    }
    
    @Override
    public RaceDefinition createRaceDefinition(Race race, Course course) {
        RaceDefinition result = raceCache.get(race);
        if (result == null) {
            BoatClass boatClass = null;
            final List<Competitor> competitors = new ArrayList<Competitor>();
            for (RaceCompetitor rc : race.getRaceCompetitorList()) {
                com.tractrac.clientmodule.Competitor competitor = rc
                        .getCompetitor();
                if (boatClass == null) {
                    boatClass = DomainFactory.INSTANCE.getBoatClass(competitor
                            .getCompetitorClass());
                }
                competitors.add(DomainFactory.INSTANCE.getCompetitor(rc
                        .getCompetitor()));
            }
            result = new RaceDefinitionImpl(race.getName(), course, boatClass,
                    competitors);
            synchronized (raceCache) {
                raceCache.put(race, result);
                raceCache.notifyAll();
            }
        }
        return result;
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

}
