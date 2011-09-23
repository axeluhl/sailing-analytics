package com.sap.sailing.domain.tractracadapter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.List;

import com.maptrack.client.io.TypeController;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.Person;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Team;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.tracking.DynamicTrackedEvent;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedEvent;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.domain.tractracadapter.impl.DomainFactoryImpl;
import com.sap.sailing.domain.tractracadapter.impl.RaceCourseReceiver;
import com.tractrac.clientmodule.Competitor;
import com.tractrac.clientmodule.CompetitorClass;
import com.tractrac.clientmodule.ControlPoint;
import com.tractrac.clientmodule.Event;
import com.tractrac.clientmodule.Race;
import com.tractrac.clientmodule.data.ControlPointPositionData;
import com.tractrac.clientmodule.data.DataController;
import com.tractrac.clientmodule.data.Position;

import difflib.PatchFailedException;

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
     * A new {@link com.sap.sailing.domain.base.Event} is created if no event by
     * an equal name with a boat class with an equal name as the <code>event</code>'s
     * boat class exists yet.
     */
    com.sap.sailing.domain.base.Event createEvent(Event event);
    
    /**
     * Creates a race tracked for the specified URL/URIs and starts receiving all available existing and future push
     * data from there. Receiving continues until {@link RaceTracker#stop()} is called.
     * <p>
     * 
     * A race tracker uses the <code>paramURL</code> for the TracTrac Java client to register for push data about one
     * race. The {@link RaceDefinition} for that race, however, isn't created until the {@link Course} has been
     * received. Therefore, the {@link RaceCourseReceiver} will create the {@link RaceDefinition} and will add it to the
     * {@link com.sap.sailing.domain.base.Event}.
     * <p>
     * 
     * The link to the {@link RaceDefinition} is created in the {@link DomainFactory} when the
     * {@link RaceCourseReceiver} creates the {@link TrackedRace} object. Starting then, the {@link DomainFactory} will
     * respond with the {@link RaceDefinition} when its {@link DomainFactory#getRace(Event)} is called with the TracTrac
     * {@link Event} as argument that is used for its tracking.
     * <p>
     * 
     * @param windStore
     *            Provides the capability to obtain the {@link WindTrack}s for the different wind sources. A trivial
     *            implementation is {@link EmptyWindStore} which simply provides new, empty tracks. This is always
     *            available but loses track of the wind, e.g., during server restarts.
     */
    RaceTracker createRaceTracker(URL paramURL, URI liveURI, URI storedURI, WindStore windStore) throws MalformedURLException,
            FileNotFoundException, URISyntaxException;

    /**
     * Looks for tracking information about <code>event</code>. If no such object exists yet, a new one
     * is created.
     */
    DynamicTrackedEvent getOrCreateTrackedEvent(com.sap.sailing.domain.base.Event event);
    
    BoatClass getBoatClass(CompetitorClass competitorClass);

    /**
     * For each race listed by the <code>tractracEvent</code>, produces {@link TypeController listeners} that, when
     * {@link DataController#add(TypeController) registered} with the controller, create a {@link RaceDefinition} with
     * the {@link Course} defined in proper order, and {@link com.sap.sailing.domain.base.Event#addRace(RaceDefinition)
     * add} it to the <code>event</code>. Other listeners of those returned will listen for raw position and aggregated
     * position data and update the {@link TrackedEvent}'s content accordingly.
     * 
     * @param trackedEvent
     *            must have been created before through
     *            {@link #getOrCreateTrackedEvent(com.sap.sailing.domain.base.Event)} because otherwise the link to the
     *            {@link Event} can't be established
     * @param tokenToRetrieveAssociatedRace
     *            used in {@link #getRace} to retrieve the {@link RaceDefinition} received by the
     *            {@link RaceCourseReceiver} created by this call
     */
    Iterable<Receiver> getUpdateReceivers(DynamicTrackedEvent trackedEvent, Event tractracEvent, WindStore windStore,
            Object tokenToRetrieveAssociatedRace);

    RaceDefinition createRaceDefinition(Race race, Course course);

    /**
     * The record may be for a single buoy or a gate. If for a gate, the
     * {@link ControlPointPositionData#getIndex() index} is used to determine
     * which of its buoys is affected.
     */
    Buoy getBuoy(ControlPoint controlPoint, ControlPointPositionData record);

    com.sap.sailing.domain.base.ControlPoint getControlPoint(ControlPoint controlPoint);

    MarkPassing createMarkPassing(com.tractrac.clientmodule.Competitor competitor, Waypoint passed, TimePoint time);

    Iterable<Receiver> getUpdateReceivers(DynamicTrackedEvent trackedEvent, Event tractracEvent, WindStore windStore,
            Object tokenToRetrieveAssociatedRace, ReceiverType... types);

    DynamicTrackedRace trackRace(TrackedEvent trackedEvent, RaceDefinition raceDefinition, WindStore windStore,
            long millisecondsOverWhichToAverageWind, long millisecondsOverWhichToAverageSpeed, Event tractracEvent,
            Object tokenToRetrieveAssociatedRace);

    /**
     * Non-blocking call that returns <code>null</code> if the {@link RaceDefinition} for the token
     * hasn't been created yet, e.g., because the course definition hasn't been received yet or the listener
     * for receiving course information hasn't been registered (yet).
     */
    RaceDefinition getRace(Object tokenToRetrieveAssociatedRace);

    JSONService parseJSONURL(URL jsonURL) throws IOException, ParseException, org.json.simple.parser.ParseException, URISyntaxException;

    /**
     * Returns a {@link RaceDefinition} for the race if it already exists, <code>null</code> otherwise.
     */
    RaceDefinition getExistingRaceDefinitionForRace(Race race);

    /**
     * When a course is changed dynamically, we receive an updated list of control points that now define
     * the new, probably (but not necessarily!) changed course. For updating the course we need an adjusted list
     * of waypoints. The waypoints are created from the control points and represent usages of the control points
     * in a course. A single control point may be used more than once in a course's list of waypoints.
     */
    void updateCourseWaypoints(Course courseToUpdate, List<ControlPoint> controlPoints) throws PatchFailedException;

    /**
     * Looks for the tracking information for <code>event</code>. If not found, <code>null</code> is returned
     * immediately. See also {@link #getOrCreateTrackedEvent(com.sap.sailing.domain.base.Event)}.
     */
    DynamicTrackedEvent getTrackedEvent(com.sap.sailing.domain.base.Event event);

    TracTracConfiguration createTracTracConfiguration(String name, String jsonURL, String liveDataURI,
            String storedDataURI);
}
