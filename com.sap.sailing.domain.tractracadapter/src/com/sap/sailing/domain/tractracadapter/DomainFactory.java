package com.sap.sailing.domain.tractracadapter;

import com.maptrack.client.io.TypeController;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.Person;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Team;
import com.sap.sailing.domain.tracking.DynamicTrackedEvent;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.TrackedEvent;
import com.sap.sailing.domain.tractracadapter.impl.DomainFactoryImpl;
import com.tractrac.clientmodule.Competitor;
import com.tractrac.clientmodule.CompetitorClass;
import com.tractrac.clientmodule.ControlPoint;
import com.tractrac.clientmodule.Event;
import com.tractrac.clientmodule.Race;
import com.tractrac.clientmodule.data.ControlPointPositionData;
import com.tractrac.clientmodule.data.DataController;
import com.tractrac.clientmodule.data.Position;

public interface DomainFactory {
    static DomainFactory INSTANCE = new DomainFactoryImpl();

    com.sap.sailing.domain.base.Position createPosition(Position position);

    com.sap.sailing.domain.base.TimePoint createTimePoint(long timestamp);

    com.sap.sailing.domain.base.Waypoint getWaypoint(
            ControlPoint controlPoint);

    Course createCourse(String name, Iterable<ControlPoint> controlPoints);

    com.sap.sailing.domain.base.Competitor getCompetitor(Competitor competitor);

    Nationality getNationality(String nationalityName);

    GPSFixMoving createGPSFixMoving(Position position);

    Person getPerson(String name, Nationality nationality);

    Team getTeam(String name, Nationality nationality);

    /**
     * Fetch a race definition previously created by a call to
     * {@link #createRaceDefinition(Race, Course)}. If no such race
     * definition was created so far, the call blocks until such a definition
     * is provided by another call.
     */
    RaceDefinition getRaceDefinition(Race race);

    /**
     * Creates an {@link com.sap.sailing.domain.base.Event event} from a
     * TracTrac event description. It doesn't have {@link RaceDefinition}s yet.
     */
    com.sap.sailing.domain.base.Event createEvent(Event event);
    
    DynamicTrackedEvent trackEvent(com.sap.sailing.domain.base.Event event);
    
    BoatClass getBoatClass(CompetitorClass competitorClass);

    /**
     * For each race listed by the <code>tractracEvent</code>, produces
     * {@link TypeController listeners} that, when
     * {@link DataController#add(TypeController) registered} with the
     * controller, create a {@link RaceDefinition} with the {@link Course}
     * defined in proper order, and
     * {@link com.sap.sailing.domain.base.Event#addRace(RaceDefinition) add} it
     * to the <code>event</code>. Other listeners of those returned will listen
     * for raw position and aggregated position data and update the
     * {@link TrackedEvent}'s content accordingly.
     * 
     * @param trackedEvent
     *            must have been created before through
     *            {@link #trackEvent(com.sap.sailing.domain.base.Event)} because
     *            otherwise the link to the {@link Event} can't be established
     */
    Iterable<TypeController> getUpdateReceivers(DynamicTrackedEvent trackedEvent);

    RaceDefinition createRaceDefinition(Race race, Course course);

    /**
     * The record may be for a single buoy or a gate. If for a gate, the
     * {@link ControlPointPositionData#getIndex() index} is used to determine
     * which of its buoys is affected.
     */
    Buoy getBuoy(ControlPoint controlPoint, ControlPointPositionData record);

}
