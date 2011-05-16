package com.sap.sailing.domain.tractracadapter.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.Person;
import com.sap.sailing.domain.base.Position;
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
import com.sap.sailing.domain.base.impl.GateImpl;
import com.sap.sailing.domain.base.impl.KilometersPerHourSpeedImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.base.impl.NationalityImpl;
import com.sap.sailing.domain.base.impl.PersonImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.domain.base.impl.WaypointImpl;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.tractrac.clientmodule.CompetitorClass;
import com.tractrac.clientmodule.ControlPoint;
import com.tractrac.clientmodule.data.RouteData;

public class DomainFactoryImpl implements DomainFactory {
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

    @Override
    public Position createPosition(
            com.tractrac.clientmodule.data.Position position) {
        return new DegreePosition(position.getLatitude(), position.getLongitude());
    }
   
    @Override
    public GPSFixMoving createGPSFixMoving(com.tractrac.clientmodule.data.Position position) {
        GPSFixMoving result = new GPSFixMovingImpl(createPosition(position), new MillisecondsTimePoint(position.getTimestamp()),
                new KilometersPerHourSpeedImpl(position.getSpeed(), new DegreeBearingImpl(position.getDirection())));
        return result;
    }
    
    @Override
    public TimePoint createTimePoint(long timestamp) {
        return new MillisecondsTimePoint(timestamp);
    }

    @Override
    public Waypoint createWaypoint(ControlPoint controlPoint) {
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
        return new WaypointImpl(domainControlPoint);
    }
    
    @Override
    public Course createCourse(String name, RouteData routeData) {
        List<Waypoint> waypointList = new ArrayList<Waypoint>();
        for (ControlPoint controlPoint : routeData.getPoints()) {
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
            // TODO create boat and class
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
            result = new PersonImpl(name, nationality, /* date of birth unknown */ null);
            personCache.put(name, result);
        }
        return result;
    }

    private BoatClass getBoatClass(CompetitorClass competitorClass) {
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

}
