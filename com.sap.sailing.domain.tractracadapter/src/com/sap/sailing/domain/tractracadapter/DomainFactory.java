package com.sap.sailing.domain.tractracadapter;

import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.Person;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Team;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tractracadapter.impl.DomainFactoryImpl;
import com.tractrac.clientmodule.Competitor;
import com.tractrac.clientmodule.ControlPoint;
import com.tractrac.clientmodule.Event;
import com.tractrac.clientmodule.Race;
import com.tractrac.clientmodule.data.DataController;
import com.tractrac.clientmodule.data.Position;

public interface DomainFactory {
    static DomainFactory INSTANCE = new DomainFactoryImpl();

    com.sap.sailing.domain.base.Position createPosition(Position position);

    com.sap.sailing.domain.base.TimePoint createTimePoint(long timestamp);

    com.sap.sailing.domain.base.Waypoint createWaypoint(
            ControlPoint controlPoint);

    Course createCourse(String name, Iterable<ControlPoint> controlPoints);

    com.sap.sailing.domain.base.Competitor getCompetitor(Competitor competitor);

    Nationality getNationality(String nationalityName);

    GPSFixMoving createGPSFixMoving(Position position);

    Person getPerson(String name, Nationality nationality);

    Team getTeam(String name, Nationality nationality);

    RaceDefinition createRaceDefinition(Race race);

    /**
     * Creates an {@link com.sap.sailing.domain.base.Event event} from a
     * TracTrac event description. It doesn't have {@link RaceDefinition}s yet.
     * Obtain a {@link RaceCourseReceiver} and from it the listeners that you
     * can {@link DataController#add(com.maptrack.client.io.TypeController)
     * register} with a {@link DataController} before you
     * {@link DataController#run() start} that controller. Only this way will
     * your {@link RaceDefinition}s have the {@link Course}s with their
     * waypoints in the right order.
     */
    com.sap.sailing.domain.base.Event createEvent(Event event);
    
    RaceCourseReceiver getRaceCourseReceiver(
            com.sap.sailing.domain.base.Event event, Event tractracEvent);
}
