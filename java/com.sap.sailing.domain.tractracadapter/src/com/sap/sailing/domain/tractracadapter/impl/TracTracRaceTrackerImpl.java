package com.sap.sailing.domain.tractracadapter.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.maptrack.client.io.TypeController;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.TrackedRaceStatusEnum;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.tracking.AbstractRaceTrackerImpl;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.DynamicRaceDefinitionSet;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.RacesHandle;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRaceStatus;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.domain.tracking.impl.GPSFixImpl;
import com.sap.sailing.domain.tracking.impl.TrackedRaceStatusImpl;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.Receiver;
import com.sap.sailing.domain.tractracadapter.TracTracControlPoint;
import com.sap.sailing.domain.tractracadapter.TracTracRaceTracker;
import com.tractrac.clientmodule.ControlPoint;
import com.tractrac.clientmodule.Event;
import com.tractrac.clientmodule.Race;
import com.tractrac.clientmodule.data.DataController;
import com.tractrac.clientmodule.data.DataController.Listener;
import com.tractrac.clientmodule.setup.KeyValue;

import difflib.PatchFailedException;

public class TracTracRaceTrackerImpl extends AbstractRaceTrackerImpl implements Listener, TracTracRaceTracker, DynamicRaceDefinitionSet {
    private static final Logger logger = Logger.getLogger(TracTracRaceTrackerImpl.class.getName());
    
    /**
     * A scheduler for the periodic checks of the paramURL documents for the advent of {@link ControlPoint}s
     * with static position information otherwise not available through {@link MarkPassingReceiver}'s events.
     */
    static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    private final Event tractracEvent;
    private final com.sap.sailing.domain.base.Regatta regatta;
    private final Thread ioThread;
    private final DataController controller;
    private final Set<Receiver> receivers;
    private final DomainFactory domainFactory;
    private final WindStore windStore;
    private final Set<RaceDefinition> races;
    private final DynamicTrackedRegatta trackedRegatta;
    private TrackedRaceStatus lastStatus;

    /**
     * paramURL, liveURI and storedURI for TracTrac connection
     */
    private final Triple<URL, URI, URI> urls;
    private final ScheduledFuture<?> controlPointPositionPoller;

    /**
     * Creates a race tracked for the specified URL/URIs and starts receiving all available existing and future push
     * data from there. Receiving continues until {@link #stop()} is called.
     * <p>
     * 
     * A race tracker uses the <code>paramURL</code> for the TracTrac Java client to register for push data about one
     * race. The {@link DomainFactory} is asked to retrieve an existing or create a new
     * {@link com.sap.sailing.domain.base.Regatta} based on the TracTrac event. The {@link RaceDefinition} for the race,
     * however, isn't created until the {@link Course} has been received. Therefore, the {@link RaceCourseReceiver} will
     * create the {@link RaceDefinition} and will add it to the {@link com.sap.sailing.domain.base.Regatta}.
     * <p>
     * 
     * The link to the {@link RaceDefinition} is created in the {@link DomainFactory} when the
     * {@link RaceCourseReceiver} creates the {@link TrackedRace} object. Starting then, the {@link DomainFactory} will
     * respond with the {@link RaceDefinition} when its {@link DomainFactory#getRaces(Event)} is called with the
     * TracTrac {@link Event} as argument that is used for its tracking.
     * <p>
     * @param startOfTracking
     *            if <code>null</code>, all stored data from the "beginning of time" will be loaded that the event has
     *            to provide, particularly for the mark positions which are stored per event, not per race; otherwise,
     *            particularly the mark position loading will be constrained to this start time.
     * @param endOfTracking
     *            if <code>null</code>, all stored data until the "end of time" will be loaded that the event has to
     *            provide, particularly for the mark positions which are stored per event, not per race; otherwise,
     *            particularly the mark position loading will be constrained to this end time.
     * @param windStore
     *            Provides the capability to obtain the {@link WindTrack}s for the different wind sources. A trivial
     *            implementation is {@link EmptyWindStore} which simply provides new, empty tracks. This is always
     *            available but loses track of the wind, e.g., during server restarts.
     * @param trackedRegattaRegistry
     *            used to create the {@link TrackedRegatta} for the domain event
     */
    protected TracTracRaceTrackerImpl(DomainFactory domainFactory, URL paramURL, URI liveURI, URI storedURI,
            TimePoint startOfTracking, TimePoint endOfTracking, long delayToLiveInMillis,
            boolean simulateWithStartTimeNow, WindStore windStore, TrackedRegattaRegistry trackedRegattaRegistry)
            throws URISyntaxException, MalformedURLException, FileNotFoundException {
        this(KeyValue.setup(paramURL), domainFactory, paramURL, liveURI, storedURI, startOfTracking, endOfTracking,
                delayToLiveInMillis, simulateWithStartTimeNow, windStore, trackedRegattaRegistry);
    }
    
    private TracTracRaceTrackerImpl(Event tractracEvent, DomainFactory domainFactory, URL paramURL, URI liveURI, URI storedURI,
            TimePoint startOfTracking, TimePoint endOfTracking, long delayToLiveInMillis, boolean simulateWithStartTimeNow,
            WindStore windStore, TrackedRegattaRegistry trackedRegattaRegistry) throws URISyntaxException, MalformedURLException,
            FileNotFoundException {
        this(tractracEvent, null, domainFactory, paramURL, liveURI, storedURI,
                startOfTracking, endOfTracking, delayToLiveInMillis, simulateWithStartTimeNow, windStore, trackedRegattaRegistry);
    }
    
    /**
     * Use this constructor if the {@link Regatta} in which to arrange the {@link RaceDefinition}s created by this
     * tracker is already known up-front, particularly if it has a specific configuration to use. Other constructors
     * may create a default {@link Regatta} with only a single default {@link Series} and {@link Fleet} which may not
     * always be what you want.
     */
    protected TracTracRaceTrackerImpl(Regatta regatta, DomainFactory domainFactory, URL paramURL, URI liveURI, URI storedURI,
            TimePoint startOfTracking, TimePoint endOfTracking, long delayToLiveInMillis, boolean simulateWithStartTimeNow,
            WindStore windStore, TrackedRegattaRegistry trackedRegattaRegistry) throws URISyntaxException, MalformedURLException,
            FileNotFoundException {
        this(KeyValue.setup(paramURL), regatta, domainFactory, paramURL, liveURI, storedURI, startOfTracking,
                endOfTracking, delayToLiveInMillis, simulateWithStartTimeNow, windStore, trackedRegattaRegistry);
    }
    
    /**
     * 
     * @param regatta
     *            if <code>null</code>, then <code>domainFactory.getOrCreateRegatta(tractracEvent)</code> will be used
     *            to obtain a default regatta
     * @param simulateWithStartTimeNow
     *            if <code>true</code>, the connector will adjust the time stamps of all events received such that the
     *            first mark passing for the first waypoint will be set to "now." It will delay the forwarding of all
     *            events received such that they seem to be sent in "real-time." So, more or less the time points
     *            attached to the events sent to the receivers will again approximate the wall time.
     */
    private TracTracRaceTrackerImpl(Event tractracEvent, final Regatta regatta, DomainFactory domainFactory,
            URL paramURL, URI liveURI, URI storedURI, TimePoint startOfTracking, TimePoint endOfTracking,
            long delayToLiveInMillis, boolean simulateWithStartTimeNow, WindStore windStore, TrackedRegattaRegistry trackedRegattaRegistry)
            throws URISyntaxException, MalformedURLException, FileNotFoundException {
        super();
        this.tractracEvent = tractracEvent;
        urls = createID(paramURL, liveURI, storedURI);
        this.races = new HashSet<RaceDefinition>();
        this.windStore = windStore;
        this.domainFactory = domainFactory;
        final Simulator simulator;
        if (simulateWithStartTimeNow) {
            simulator = new Simulator();
        } else {
            simulator = null;
        }
        // Read event data from configuration file
        controlPointPositionPoller = scheduleClientParamsPHPPoller(paramURL, simulator);
        // can happen that TracTrac event is null (occurs when there is no Internet connection)
        // so lets raise some meaningful exception
        if (tractracEvent == null) {
            throw new RuntimeException("Connection failed. Could not connect to " + paramURL);
        }
        
        // Initialize data controller using live and stored data sources
        controller = new DataController(liveURI, storedURI, this);
        // Start live and stored data streams
        ioThread = new Thread(controller, "I/O for event "+tractracEvent.getName()+", race URL "+paramURL);
        Regatta effectiveRegatta = regatta;
        for (Race tractracRace : tractracEvent.getRaceList()) {
            // Try to find a pre-associated event based on the Race ID
            if (effectiveRegatta == null) {
                Serializable raceID = domainFactory.getRaceID(tractracRace);
                effectiveRegatta = trackedRegattaRegistry.getRememberedRegattaForRace(raceID);
            }
            // removeRace may detach the domain regatta from the domain factory if that
            // removed the last race; therefore, it's important to getOrCreate the
            // domain regatta *after* calling removeRace
            domainFactory.removeRace(tractracEvent, tractracRace, trackedRegattaRegistry);
        }
        // if regatta is still null, no previous assignment of any of the races in this TracTrac event to a Regatta was found;
        // in this case, create a default regatta based on the TracTrac event data
        this.regatta = effectiveRegatta == null ? domainFactory.getOrCreateDefaultRegatta(tractracEvent, trackedRegattaRegistry) : effectiveRegatta;
        trackedRegatta = trackedRegattaRegistry.getOrCreateTrackedRegatta(this.regatta);
        receivers = new HashSet<Receiver>();
        Set<TypeController> typeControllers = new HashSet<TypeController>();
        for (Receiver receiver : domainFactory.getUpdateReceivers(getTrackedRegatta(), tractracEvent, startOfTracking,
                endOfTracking, delayToLiveInMillis, simulator, windStore, this, trackedRegattaRegistry)) {
            receivers.add(receiver);
            for (TypeController typeController : receiver.getTypeControllersAndStart()) {
                typeControllers.add(typeController);
            }
        }
        addListenersForStoredDataAndStartController(typeControllers);
    }

    @Override
    public DynamicTrackedRegatta getTrackedRegatta() {
        return trackedRegatta;
    }

    static Triple<URL, URI, URI> createID(URL paramURL, URI liveURI, URI storedURI) {
        return new Triple<URL, URI, URI>(paramURL, liveURI, storedURI);
    }
    
    /**
     * Control points may get added late in the race. If they don't have a tracker installed, their position will be
     * static. This position can be retrieved from {@link ControlPoint#getLat1()} etc. This method registers a task with
     * {@link #scheduler} that regularly polls the <code>paramURL</code> to see if any new control points have arrived
     * or positions for existing control points have been received. Any new information in this direction will be
     * entered into the {@link TrackedRace} for the {@link #getRaces() race} tracked by this tracker.
     * 
     * @param paramURL
     *            points to the document describing the race's metadata which will periodically be downloaded
     * @param simulator
     *            if not <code>null</code>, use this simulator to translate start/stop tracking times received through
     *            clientparams document
     * @return the task to cancel in case the tracker wants to terminate the poller
     */
    private ScheduledFuture<?> scheduleClientParamsPHPPoller(final URL paramURL, final Simulator simulator) {
        ScheduledFuture<?> task = scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override public void run() {
                pollAndParseClientParamsPHP(paramURL, simulator);
            }
        }, /* initialDelay */ 30000, /* delay */ 15000, /* unit */ TimeUnit.MILLISECONDS);
        return task;
    }


    private void pollAndParseClientParamsPHP(final URL paramURL, final Simulator simulator) {
        Set<RaceDefinition> raceDefinitions = getRaces();
        if (raceDefinitions != null && !raceDefinitions.isEmpty()) {
            logger.info("fetching paramURL "+paramURL+" to check for updates for race(s) "+getRaces());
            final ClientParamsPHP clientParams;
            try {
                clientParams = new ClientParamsPHP(new InputStreamReader(paramURL.openStream()));
                List<Pair<com.sap.sailing.domain.base.ControlPoint, NauticalSide>> newCourseControlPoints = new ArrayList<>();
                final List<? extends TracTracControlPoint> newTracTracControlPoints = clientParams.getRaceDefaultRoute().getControlPoints();
                Map<Integer, NauticalSide> passingSideData = parsePassingSideData(clientParams.getRaceDefaultRoute(), newTracTracControlPoints);
                int i = 1;
                for (TracTracControlPoint newTracTracControlPoint : newTracTracControlPoints) {
                    NauticalSide nauticalSide = passingSideData.containsKey(i) ? passingSideData.get(i) : null;
                    newCourseControlPoints.add(new Pair<com.sap.sailing.domain.base.ControlPoint, 
                            NauticalSide>(domainFactory.getOrCreateControlPoint(newTracTracControlPoint), nauticalSide));
                    i++;
                }
                List<com.sap.sailing.domain.base.ControlPoint> currentCourseControlPoints = new ArrayList<>();
                final Course course = getRaces().iterator().next().getCourse();
                for (Waypoint waypoint : course.getWaypoints()) {
                    currentCourseControlPoints.add(waypoint.getControlPoint());
                }
                if (!newCourseControlPoints.equals(currentCourseControlPoints)) {
                    logger.info("Detected course change based on clientparams.php contents for races "+getRaces());
                    try {
                        course.update(newCourseControlPoints, domainFactory.getBaseDomainFactory());
                    } catch (PatchFailedException pfe) {
                        logger.severe("Failed to apply course update "+newTracTracControlPoints+" to course "+course);
                        logger.throwing(TracTracRaceTrackerImpl.class.getName(), "scheduleClientParamsPHPPoller.run", pfe);
                    }
                }
                updateStartStopTimesAndLiveDelay(clientParams, simulator);
                for (TracTracControlPoint controlPoint : clientParams.getControlPointList()) {
                    com.sap.sailing.domain.base.ControlPoint domainControlPoint = domainFactory.getOrCreateControlPoint(controlPoint);
                    boolean first = true;
                    for (Mark mark : domainControlPoint.getMarks()) {
                        for (RaceDefinition raceDefinition : raceDefinitions) {
                            DynamicTrackedRace trackedRace = getTrackedRegatta().getExistingTrackedRace(raceDefinition);
                            if (trackedRace != null) {
                                DynamicGPSFixTrack<Mark, GPSFix> markTrack = trackedRace.getOrCreateTrack(mark);
                                if (markTrack.getFirstRawFix() == null) {
                                    final Position position = first ? controlPoint.getMark1Position() : controlPoint.getMark2Position();
                                    if (position != null) {
                                        markTrack.addGPSFix(new GPSFixImpl(position, MillisecondsTimePoint.now()));
                                    }
                                }
                            }
                        }
                        first = false;
                    }
                }
            } catch (IOException e) {
                logger.info("Exception "+e.getMessage()+" while trying to read clientparams.php for races "+getRaces());
                logger.throwing(TracTracRaceTracker.class.getName(), "scheduleClientParamsPHPPoller.run", e);
            }
        }
    }

    /**
     * Parses the route metadata for additional course information
     * The 'passing side' for each course waypoint is encoded like this...
     * Seq.1=GATE
     * Seq.2=PORT
     * Seq.3=GATE
     * Seq.4=STARBOARD
     */
    private Map<Integer, NauticalSide> parsePassingSideData(ClientParamsPHP.Route route, List<? extends TracTracControlPoint> controlPoints) {
        Map<Integer, NauticalSide> result = new HashMap<Integer, NauticalSide>();
        int controlPointsCount = controlPoints.size();
        String routeMetadataString = route.getMetadata();
        if(routeMetadataString != null) {
            Map<String, String> routeMetadata = parseRouteMetadata(routeMetadataString);
            for(int i = 1; i <= controlPointsCount; i++) {
                String seqValue = routeMetadata.get("Seq." + i);
                TracTracControlPoint controlPoint = controlPoints.get(i-1);
                if(!controlPoint.getHasTwoPoints() && seqValue != null) {
                    if("PORT".equalsIgnoreCase(seqValue)) {
                        result.put(i, NauticalSide.PORT);
                    } else if("STARBOARD".equalsIgnoreCase(seqValue)) {
                        result.put(i, NauticalSide.STARBOARD);
                    }
                }
            }
        }
        return result;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Map<String, String> parseRouteMetadata(String routeMetadata) {
        Map<String, String> metadataMap = new HashMap<String, String>();
        try {
            Properties p = new Properties();
            p.load(new StringReader(routeMetadata));
            metadataMap = new HashMap<String, String>((Map) p);
        } catch (IOException e) {
            // do nothing
        }
        return metadataMap;
    }
    private void updateStartStopTimesAndLiveDelay(ClientParamsPHP clientParams, Simulator simulator) {
        RaceDefinition currentRace = null;
        long delayInMillis = clientParams.getLiveDelayInMillis();
        RaceDefinition race = getRegatta().getRaceByName(clientParams.getRaceName());
        if (race != null) {
            currentRace = race;
            final DynamicTrackedRace trackedRace = getTrackedRegatta().getExistingTrackedRace(currentRace);
            if (trackedRace != null) {
                trackedRace.setDelayToLiveInMillis(delayInMillis);
            }
        }
        if (currentRace != null) {
            final DynamicTrackedRace trackedRace = getTrackedRegatta().getExistingTrackedRace(currentRace);
            if (trackedRace != null) {
                TimePoint startOfTracking = clientParams.getRaceTrackingStartTime();
                if (startOfTracking != null) {
                    trackedRace.setStartOfTrackingReceived(simulator == null ? startOfTracking : simulator
                            .advance(startOfTracking));
                }
                TimePoint endOfTracking = clientParams.getRaceTrackingEndTime();
                if (endOfTracking != null) {
                    trackedRace.setEndOfTrackingReceived(simulator == null ? endOfTracking : simulator
                            .advance(endOfTracking));
                }
            }
        }
    }

    @Override
    public Triple<URL, URI, URI> getID() {
        return urls;
    }

    @Override
    public WindStore getWindStore() {
        return windStore;
    }

    @Override
    public RacesHandle getRacesHandle() {
        return new RaceHandleImpl(domainFactory, tractracEvent, getTrackedRegatta(), this);
    }
    
    @Override
    public Set<RaceDefinition> getRaces() {
        return races;
    }
    
    protected void addListenersForStoredDataAndStartController(Iterable<TypeController> listenersForStoredData) {
        for (TypeController listener : listenersForStoredData) {
            getController().add(listener);
        }
        startController();
    }
    
    @Override
    public com.sap.sailing.domain.base.Regatta getRegatta() {
        return regatta;
    }
    
    /**
     * Called when the {@link #storedDataEnd()} event was received. Adds the listeners
     * returned to the {@link #getController() controller}, presumably for live data.
     * This default implementation returns an empty iterable. Subclasses may override
     * to return more.
     */
    protected Iterable<TypeController> getListenersForLiveData() {
        return Collections.emptySet();
    }

    protected void startController() {
        ioThread.start();
    }
    
    @SuppressWarnings("deprecation") // explicitly calling Thread.stop in case IO thread didn't join in three seconds time
    @Override
    public void stop() throws MalformedURLException, IOException, InterruptedException {
        controlPointPositionPoller.cancel(/* mayInterruptIfRunning */ false);
        controller.stop(/* abortStored */ true);
        for (Receiver receiver : receivers) {
            receiver.stopPreemptively();
        }
        ioThread.join(3000); // wait no more than three seconds
        if (ioThread.isAlive()) {
            ioThread.stop();
            logger.warning("Tractrac IO thread for race(s) "+getRaces()+" didn't join in 3s. Stopped forcefully.");
        } else {
            logger.info("Joined TracTrac IO thread for race(s) "+getRaces());
        }
    }

    protected DataController getController() {
        return controller;
    }

    @Override
    public void liveDataConnected() {
        logger.info("Live data connected for race(s) "+getRaces());
    }

    @Override
    public void liveDataDisconnected() {
        logger.info("Live data disconnected for race(s) "+getRaces());
    }

    @Override
    public void stopped() {
        logger.info("stopped TracTrac tracking for "+getRaces());
        lastStatus = new TrackedRaceStatusImpl(TrackedRaceStatusEnum.FINISHED, 1.0);
        updateStatusOfTrackedRaces();
    }

    private void updateStatusOfTrackedRaces() {
        for (RaceDefinition race : getRaces()) {
            DynamicTrackedRace trackedRace = getTrackedRegatta().getExistingTrackedRace(race);
            if (trackedRace != null) {
                updateStatusOfTrackedRace(trackedRace);
            }
        }
    }

    private void updateStatusOfTrackedRace(DynamicTrackedRace trackedRace) {
        trackedRace.setStatus(lastStatus);
    }

    @Override
    public void storedDataBegin() {
        logger.info("Stored data begin for race(s) "+getRaces());
        lastStatus = new TrackedRaceStatusImpl(TrackedRaceStatusEnum.LOADING, 0);
        updateStatusOfTrackedRaces();
    }

    @Override
    public void storedDataEnd() {
        logger.info("Stored data end for race(s) "+getRaces());
        lastStatus = new TrackedRaceStatusImpl(TrackedRaceStatusEnum.TRACKING, 1);
        updateStatusOfTrackedRaces();
    }

    @Override
    public void storedDataProgress(float progress) {
        logger.info("Stored data progress for race(s) "+getRaces()+": "+progress);
        lastStatus = new TrackedRaceStatusImpl(TrackedRaceStatusEnum.LOADING, progress);
        updateStatusOfTrackedRaces();
    }

    @Override
    public void storedDataError(String arg0) {
        logger.warning("Error with stored data for race(s) "+getRaces()+": "+arg0);
    }

    @Override
    public void liveDataConnectError(String arg0) {
        logger.warning("Error with live data for race(s) "+getRaces()+": "+arg0);
    }

    @Override
    public void addRaceDefinition(RaceDefinition race, DynamicTrackedRace trackedRace) {
        races.add(race);
        updateStatusOfTrackedRace(trackedRace);
    }

}
