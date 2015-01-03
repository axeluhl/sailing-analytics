package com.sap.sailing.domain.tractracadapter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.List;
import java.util.UUID;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.Person;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Sideline;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.racelog.tracking.GPSFixStore;
import com.sap.sailing.domain.regattalog.RegattaLogStore;
import com.sap.sailing.domain.tracking.DynamicRaceDefinitionSet;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sailing.domain.tracking.RaceTrackingConnectivityParameters;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.domain.tractracadapter.impl.DomainFactoryImpl;
import com.sap.sailing.domain.tractracadapter.impl.RaceCourseReceiver;
import com.sap.sailing.domain.tractracadapter.impl.Simulator;
import com.tractrac.model.lib.api.data.IPosition;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.tractrac.model.lib.api.event.CreateModelException;
import com.tractrac.model.lib.api.event.ICompetitor;
import com.tractrac.model.lib.api.event.IEvent;
import com.tractrac.model.lib.api.event.IRace;
import com.tractrac.subscription.lib.api.IEventSubscriber;
import com.tractrac.subscription.lib.api.IRaceSubscriber;
import com.tractrac.subscription.lib.api.SubscriberInitializationException;

import difflib.PatchFailedException;

public interface DomainFactory {
    /**
     * A default domain factory for test purposes only. In a server environment, ensure NOT to use this. Use what can be
     * reached from <code>RacingEventService.getBaseDomainFactory()</code> instead which should be the single instance
     * used by all other services linked to the <code>RacingEventService</code>.
     */
    static DomainFactory INSTANCE = new DomainFactoryImpl(com.sap.sailing.domain.base.DomainFactory.INSTANCE);
    
    com.sap.sailing.domain.base.DomainFactory getBaseDomainFactory();

    com.sap.sailing.domain.common.Position createPosition(IPosition position);

    com.sap.sse.common.TimePoint createTimePoint(long timestamp);

    Course createCourse(String name, Iterable<Util.Pair<TracTracControlPoint, PassingInstruction>> controlPoints);

    Sideline createSideline(String name, Iterable<TracTracControlPoint> controlPoints);

    com.sap.sailing.domain.base.Competitor getOrCreateCompetitor(ICompetitor competitor);

    com.sap.sailing.domain.base.Competitor getOrCreateCompetitor(UUID competitorId, String competitorClassName,
            String nationalityAsString, String name, String shortName);

    /**
     * Looks up or, if not found, creates a {@link Nationality} object and re-uses <code>threeLetterIOCCode</code> also as the
     * nationality's name.
     */
    Nationality getOrCreateNationality(String threeLetterIOCCode);

    GPSFixMoving createGPSFixMoving(IPosition position);

    Person getOrCreatePerson(String name, Nationality nationality, UUID id);

    /**
     * Fetch a race definition previously created by a call to {@link #getOrCreateRaceDefinitionAndTrackedRace}. If no such
     * race definition was created so far, the call blocks until such a definition is provided by a call to
     * {@link #getOrCreateRaceDefinitionAndTrackedRace}.
     * @param raceId TODO
     */
    RaceDefinition getAndWaitForRaceDefinition(UUID raceId);

    /**
     * Creates an {@link com.sap.sailing.domain.base.Regatta event} from a
     * TracTrac event description. It doesn't have {@link RaceDefinition}s yet.
     * A new {@link com.sap.sailing.domain.base.Regatta} is created if no event by
     * an equal name with a boat class with an equal name as the <code>event</code>'s
     * boat class exists yet.
     * @param trackedRegattaRegistry TODO
     */
    com.sap.sailing.domain.base.Regatta getOrCreateDefaultRegatta(RaceLogStore raceLogStore, RegattaLogStore regattaLogStore,
            IRace race, TrackedRegattaRegistry trackedRegattaRegistry);
    
    /**
     * Creates a race tracked for the specified URL/URIs and starts receiving all available existing and future push
     * data from there. Receiving continues until {@link TracTracRaceTracker#stop(boolean)} is called.
     * <p>
     * 
     * A race tracker uses the <code>paramURL</code> for the TracTrac Java client to register for push data about one
     * race. The {@link RaceDefinition} for that race, however, isn't created until the {@link Course} has been
     * received. Therefore, the {@link RaceCourseReceiver} will create the {@link RaceDefinition} and will add it to the
     * {@link com.sap.sailing.domain.base.Regatta}.
     * <p>
     * 
     * The link to the {@link RaceDefinition} is created in the {@link DomainFactory} when the
     * {@link RaceCourseReceiver} creates the {@link TrackedRace} object. Starting then, the {@link DomainFactory} will
     * respond with the {@link RaceDefinition} when its {@link DomainFactory#getRaceID(IRace)} is called with the
     * TracTrac {@link IEvent} as argument that is used for its tracking.
     * <p>
     * @param startOfTracking
     *            if <code>null</code>, all stored data from the "beginning of time" will be loaded that the event has
     *            to provide, particularly for the mark positions which are stored per event, not per race; otherwise,
     *            particularly the mark position loading will be constrained to this start time.
     * @param endOfTracking
     *            if <code>null</code>, all stored data until the "end of time" will be loaded that the event has
     *            to provide, particularly for the mark positions which are stored per event, not per race; otherwise,
     *            particularly the mark position loading will be constrained to this end time.
     * @param windStore
     *            Provides the capability to obtain the {@link WindTrack}s for the different wind sources. A trivial
     *            implementation is {@link EmptyWindStore} which simply provides new, empty tracks. This is always
     *            available but loses track of the wind, e.g., during server restarts.
     */
    TracTracRaceTracker createRaceTracker(URL paramURL, URI liveURI, URI storedURI, URI courseDesignUpdateURI,
            TimePoint startOfTracking, TimePoint endOfTracking, long delayToLiveInMillis,
            boolean simulateWithStartTimeNow, RaceLogStore raceLogStore, RegattaLogStore regattaLogStore,
            WindStore windStore, GPSFixStore gpsFixStore, String tracTracUsername, String tracTracPassword,
            String raceStatus, String raceVisibility, TrackedRegattaRegistry trackedRegattaRegistry)
            throws MalformedURLException, FileNotFoundException, URISyntaxException, CreateModelException, SubscriberInitializationException;

    /**
     * Same as {@link #createRaceTracker(URL, URI, URI, URI, TimePoint, TimePoint, WindStore, TrackedRegattaRegistry)}, only that
     * a predefined {@link Regatta} is used to hold the resulting races.
     */
    RaceTracker createRaceTracker(Regatta regatta, URL paramURL, URI liveURI, URI storedURI, URI courseDesignUpdateURI,
            TimePoint startOfTracking, TimePoint endOfTracking, long delayToLiveInMillis,
            boolean simulateWithStartTimeNow, RaceLogStore raceLogStore, RegattaLogStore regattaLogStore,
            WindStore windStore, GPSFixStore gpsFixStore, String tracTracUsername, String tracTracPassword,
            String raceStatus, String raceVisibility, TrackedRegattaRegistry trackedRegattaRegistry)
            throws MalformedURLException, FileNotFoundException, URISyntaxException, CreateModelException,
            SubscriberInitializationException;

    BoatClass getOrCreateBoatClass(String competitorClassName);

    /**
     * For <code>tractracRace</code>, produces listeners that, when subscribed with either the <code>eventSubscriber</code>
     * or the <code>raceSubscriber</code>, create a {@link RaceDefinition} with the {@link Course} defined in proper order,
     * and {@link com.sap.sailing.domain.base.Regatta#addRace(RaceDefinition) add} it to the <code>event</code>. Other
     * listeners of those returned will listen for raw position and aggregated position data and update the
     * {@link TrackedRegatta}'s content accordingly.
     * 
     * @param trackedRegatta
     *            must have been created before through
     *            {@link #getOrCreateTrackedRegatta(com.sap.sailing.domain.base.Regatta)} because otherwise the link to
     *            the {@link IEvent} can't be established
     * @param tokenToRetrieveAssociatedRace
     *            used to update the set of{@link RaceDefinition}s received by the {@link RaceCourseReceiver} created by
     *            this call
     */
    Iterable<Receiver> getUpdateReceivers(DynamicTrackedRegatta trackedRegatta, long delayToLiveInMillis,
            Simulator simulator, WindStore windStore, DynamicRaceDefinitionSet raceDefinitionSetToUpdate, TrackedRegattaRegistry trackedRegattaRegistry,
            IRace tractracRace, URI courseDesignUpdateURI,
            String tracTracUsername, String tracTracPassword, IEventSubscriber eventSubscriber, IRaceSubscriber raceSubscriber);

    /**
     * Creates a {@link RaceDefinition} from a TracTrac {@link Race} and a domain {@link Course} definition. The
     * resulting {@link RaceDefinition} is added to the {@link com.sap.sailing.domain.base.Regatta} to which
     * <code>trackedRegatta</code> belongs (see {@link TrackedRegatta#getRegatta()}). It is added to the internal race
     * cache. The corresponding {@link TrackedRace} object is also created, and the notification of threads waiting on
     * the race cache such as a blocking {@link #getAndWaitForRaceDefinition(UUID)} happens only <em>after</em> the
     * tracked race has been created and the {@link RaceDefinition} was
     * {@link com.sap.sailing.domain.base.Regatta#addRace(RaceDefinition) added} to the domain event. This ensures that
     * waiters for the {@link RaceDefinition} are guaranteed to obtain a valid, non-<code>null</code> tracked race
     * already immediately after the notification was sent, and that the {@link RaceDefinition} is already
     * {@link com.sap.sailing.domain.base.Regatta#getAllRaces() known} by its containing
     * {@link com.sap.sailing.domain.base.Regatta}.
     * 
     * @param raceDefinitionSetToUpdate
     *            if not <code>null</code>, after creating the {@link TrackedRace}, the {@link RaceDefinition} is
     *            {@link DynamicRaceDefinitionSet#addRaceDefinition(RaceDefinition, DynamicTrackedRace) added} to that
     *            object.
     */
    DynamicTrackedRace getOrCreateRaceDefinitionAndTrackedRace(DynamicTrackedRegatta trackedRegatta, UUID raceId,
            String raceName, Iterable<com.sap.sailing.domain.base.Competitor> competitors, BoatClass boatClass,
            Course course, Iterable<Sideline> sidelines, WindStore windStore, long delayToLiveInMillis,
            long millisecondsOverWhichToAverageWind, DynamicRaceDefinitionSet raceDefinitionSetToUpdate,
            URI courseDesignUpdateURI, UUID tracTracEventUuid, String tracTracUsername, String tracTracPassword);

    /**
     * The record may be for a single mark or a gate. If for a gate, the
     * {@link ControlPointPositionData#getIndex() index} is used to determine
     * which of its marks is affected.
     */
    Mark getMark(TracTracControlPoint controlPoint, int zeroBasedMarkIndex);

    com.sap.sailing.domain.base.ControlPoint getOrCreateControlPoint(TracTracControlPoint controlPoint);

    MarkPassing createMarkPassing(TimePoint timePoint, Waypoint passed, com.sap.sailing.domain.base.Competitor competitor);

    /**
     * If the vm argument tractrac.usemarkpassings=false, the RecieverType MARKPASSINGS will not return anything
     */
    Iterable<Receiver> getUpdateReceivers(DynamicTrackedRegatta trackedRegatta, IRace tractracRace, WindStore windStore,
            long delayToLiveInMillis, Simulator simulator, DynamicRaceDefinitionSet raceDefinitionSetToUpdate, TrackedRegattaRegistry trackedRegattaRegistry, 
            URI courseDesignUpdateURI, String tracTracUsername, 
            String tracTracPassword, IEventSubscriber eventSubscriber, IRaceSubscriber raceSubscriber, ReceiverType... types);

    JSONService parseJSONURLWithRaceRecords(URL jsonURL, boolean loadClientParams) throws IOException, ParseException, org.json.simple.parser.ParseException, URISyntaxException;

    /**
     * Returns a {@link RaceDefinition} for the race if it already exists, <code>null</code> otherwise.
     */
    RaceDefinition getExistingRaceDefinitionForRace(UUID raceId);

    /**
     * When a course is changed dynamically, we receive an updated list of control points that now define
     * the new, probably (but not necessarily!) changed course. For updating the course we need an adjusted list
     * of waypoints. The waypoints are created from the control points and represent usages of the control points
     * in a course. A single control point may be used more than once in a course's list of waypoints.
     */
    void updateCourseWaypoints(Course courseToUpdate, Iterable<Util.Pair<TracTracControlPoint, PassingInstruction>> controlPoints) throws PatchFailedException;

    TracTracConfiguration createTracTracConfiguration(String name, String jsonURL, String liveDataURI, String storedDataURI, String courseDesignUpdateURI, String tracTracUsername, String tracTracPassword);

    /**
     * Fetch the race definition for <code>race</code>. If the race definition hasn't been created yet, the call blocks
     * until such a definition is provided by a call to {@link #getOrCreateRaceDefinitionAndTrackedRace}. If
     * <code>timeoutInMilliseconds</code> milliseconds have passed and the race definition is found not to have shown up
     * until then, <code>null</code> is returned. The unblocking may be deferred even beyond
     * <code>timeoutInMilliseconds</code> in case no modifications happen on the set of races cached by this factory.
     * 
     * @param timeoutInMilliseconds
     *            passing -1 means an infinite timeout; 0 means return immediately with <code>null</code> as result if
     *            no race definition is found for <code>race</code>.
     */
    RaceDefinition getAndWaitForRaceDefinition(UUID raceId, long timeoutInMilliseconds);

    Util.Pair<Iterable<com.sap.sailing.domain.base.Competitor>, BoatClass> getCompetitorsAndDominantBoatClass(IRace race);
    
    RaceTrackingConnectivityParameters createTrackingConnectivityParameters(URL paramURL, URI liveURI, URI storedURI,
            URI courseDesignUpdateURI, TimePoint startOfTracking, TimePoint endOfTracking, long delayToLiveInMillis,
            boolean simulateWithStartTimeNow, RaceLogStore raceLogStore, RegattaLogStore regattaLogStore,
            String tracTracUsername, String tracTracPassword, String raceStatus, String raceVisibility);
    /**
     * Removes all knowledge about <code>tractracRace</code> which includes removing it from the race cache, from the
     * {@link com.sap.sailing.domain.base.Regatta} and, if a {@link TrackedRace} for the corresponding
     * {@link RaceDefinition} exists, from the {@link TrackedRegatta}. If removing the race from the event, the event is
     * removed from the event cache such that {@link #getOrCreateDefaultRegatta(IEvent, TrackedRegattaRegistry)} will have to create a new one. Similarly,
     * if the {@link TrackedRace} that was removed from the {@link TrackedRegatta} was the last one, the
     * {@link TrackedRegatta} is removed such that {@link #getOrCreateTrackedRegatta(com.sap.sailing.domain.base.Regatta)}
     * will have to create a new one.
     */
    void removeRace(IEvent tractracEvent, IRace tractracRace, TrackedRegattaRegistry trackedRegattaRegistry);

    /**
     * Computes an ID to use for a {@link RaceDefinition} based on the TracTrac race.
     */
    Serializable getRaceID(IRace tractracRace);

    JSONService parseJSONURLForOneRaceRecord(URL jsonURL, String raceId, boolean loadClientParams) throws IOException, ParseException, org.json.simple.parser.ParseException, URISyntaxException;

    MetadataParser getMetadataParser();

    BoatClass getDominantBoatClass(List<String> competitorClassNames);

    List<Sideline> createSidelines(String raceMetadataString, Iterable<? extends TracTracControlPoint> allEventControlPoints);

    /**
     * When a tracked race has been created for tracking with the TracTrac adapter, change listeners have to be subscribed to
     * the {@link TrackedRace} which will notify certain changes to the race's state to TracTrac. This includes the course layout,
     * the start time and whether a race was aborted.
     */
    void addTracTracUpdateHandlers(URI tracTracUpdateURI, UUID tracTracEventUuid, String tracTracUsername,
            String tracTracPassword, RaceDefinition raceDefinition, DynamicTrackedRace trackedRace);

}
