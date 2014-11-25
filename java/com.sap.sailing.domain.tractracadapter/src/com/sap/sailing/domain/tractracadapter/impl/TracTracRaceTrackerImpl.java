package com.sap.sailing.domain.tractracadapter.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.common.TrackedRaceStatusEnum;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.racelog.tracking.GPSFixStore;
import com.sap.sailing.domain.tracking.AbstractRaceTrackerImpl;
import com.sap.sailing.domain.tracking.DynamicRaceDefinitionSet;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.RaceHandle;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRaceStatus;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.domain.tracking.impl.TrackedRaceStatusImpl;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.Receiver;
import com.sap.sailing.domain.tractracadapter.TracTracConnectionConstants;
import com.sap.sailing.domain.tractracadapter.TracTracRaceTracker;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.tractrac.model.lib.api.ModelLocator;
import com.tractrac.model.lib.api.event.CreateModelException;
import com.tractrac.model.lib.api.event.ICompetitor;
import com.tractrac.model.lib.api.event.IEvent;
import com.tractrac.model.lib.api.event.IRace;
import com.tractrac.model.lib.api.route.IControl;
import com.tractrac.subscription.lib.api.IEventSubscriber;
import com.tractrac.subscription.lib.api.IRaceSubscriber;
import com.tractrac.subscription.lib.api.ISubscriberFactory;
import com.tractrac.subscription.lib.api.SubscriberInitializationException;
import com.tractrac.subscription.lib.api.SubscriptionLocator;
import com.tractrac.subscription.lib.api.competitor.ICompetitorsListener;
import com.tractrac.subscription.lib.api.event.IConnectionStatusListener;
import com.tractrac.subscription.lib.api.event.ILiveDataEvent;
import com.tractrac.subscription.lib.api.event.IStoredDataEvent;
import com.tractrac.subscription.lib.api.race.IRacesListener;

public class TracTracRaceTrackerImpl extends AbstractRaceTrackerImpl implements IConnectionStatusListener, TracTracRaceTracker, DynamicRaceDefinitionSet {
    private static final Logger logger = Logger.getLogger(TracTracRaceTrackerImpl.class.getName());
    
    /**
     * A scheduler for the periodic checks of the paramURL documents for the advent of {@link IControl control points}
     * with static position information otherwise not available through {@link MarkPassingReceiver}'s events.
     */
    static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * This value indicated how many stored data packets we allow that are not in the right sequence Background: It can
     * happen that the progress for storedData hops around and delivers a progress that is lower than one received
     * before. This can happen but only at a maximum of times this constant describes. To provide some background, here
     * is an excerpt of a description received from Jorge Piera Llodra from TracTrac on 2014-02-05:
     * 
     * <i>
     * <p>
     * "Our library creates a new thread per data type where a data type is associated with a subscription. e.g: there
     * is a data type for the competitor positions, other for the mark positions, other for the course update, other for
     * the start/stop times... Every thread calculates its individual progress and its weight and the progress that you
     * receive in the "storedDataProgress" method is a function of all the individual progresses and all the weights
     * reported by the threads. The function that calculates the total progress is:
     * 
     * <pre>
     * total_progress = sum(progress(thread_i)) / sum(weight(thread_i))
     * </pre>
     * 
     * The weight is also the maximum individual progress that a thread can send: if a thread has a weight = 10 its
     * progress only can be between 0 and 10, e.g,:
     * <p>
     * 
     * You are subscribed to receive competitor positions and the current course. All the threads have a default weight
     * and the beginning the send the values:
     * <ul>
     * <li>Competitor positions thread -> weight = 10, progress = 0</li>
     * <li>Course thread -> weight = 1, progress = 0</li>
     * </ul>
     * The total progress that you receive is:
     * 
     * <pre>
     *   total_progress = 0 + 0 / 10 + 1 = 0 / 11 = 0
     * </pre>
     * 
     * Then, the "Course thread" retrieves the course from the server and it sends a new progress message to the system:
     * 
     * <pre>
     *   Course thread -&gt; weight = 1, progress = 1  ---&gt total_progress = 0 + 1 /  10 + 1 = 1 / 11 = 0.090909091
     * </pre>
     * 
     * Then, the "Competitor positions thread" goes to the server and it checks that there is a high number of positions
     * for the competitors. It decides to change its weight:
     * 
     * <pre>
     *   Competitor positions thread -&gt weight = 50, progress = 0 --&gt total_progress = 0 + 1 / 50 + 1 = 1 / 51 = 0.019607843
     * </pre>
     * 
     * Then, the "Competitor positions thread" starts to retrieve positions and it sends several messages updating the
     * progress:
     * <ul>
     * <li>
     * 
     * <pre>
     * Competitor positions thread -&gt weight = 50, progress = 1 --&gt; total_progress = 1 + 1 / 50 + 1 = 2 / 51 = 0.039215686
     * </pre>
     * 
     * </li>
     * <li>
     * 
     * <pre>
     * Competitor positions thread -&gt weight = 50, progress = 2 --&gt total_progress = 2 + 1 / 50 + 1 = 3 / 51 = 0.058823529
     * </pre>
     * 
     * </li>
     * <li>
     * 
     * <pre>
     * Competitor positions thread -&gt weight = 50, progress = 3 --&gt total_progress = 3 + 1 / 50 + 1 = 4 / 51 = 0.078431373
     * </pre>
     * 
     * </li>
     * <li>
     * 
     * <pre>
     * ...
     * </pre>
     * 
     * </li>
     * <li>
     * 
     * <pre>
     * Competitor positions thread -&gt weight = 50, progress = 50 --&gt total_progress = 50 + 1 / 50 + 1 = 51 / 51 = 1.0
     * </pre>
     * 
     * </li>
     * </ul>
     * 
     * This example shows that is possible to receive more that 3 values of the progress lower than one already
     * received. It happens because the weight of the threads changes."</i>
     * <p>
     * 
     * We assume that there won't be more than eight threads in TTCM receiving data for the same race, based on Jorge's
     * statement from 2014-02-06: "One thread per subscription where the subscriptions are:
     * <ul>
     * <li>Competitor positions</li>
     * <li>Mark positions</li>
     * <li>Mark passings</li>
     * <li>Route</li>
     * <li>Start/Stop times for race</li>
     * <li>Start/Stop times for event</li>
     * <li>Messages for race</li>
     * <li>Messages for event</li>
     * </ul>
     * Potentially, you can create 8 threads per TTCM (connecting only with one single race)."
     */
    static final Integer MAX_STORED_PACKET_HOP_ALLOWANCE = 1000;
    
    private final IEvent tractracEvent;
    private final IRace tractracRace;
    private final com.sap.sailing.domain.base.Regatta regatta;
    private final IEventSubscriber eventSubscriber;
    private final IRaceSubscriber raceSubscriber;
    private final Set<Receiver> receivers;
    private final DomainFactory domainFactory;
    private final WindStore windStore;
    private final GPSFixStore gpsFixStore;
    private final Set<RaceDefinition> races;
    private final DynamicTrackedRegatta trackedRegatta;
    private TrackedRaceStatus lastStatus;
    private HashMap<Util.Triple<URL, URI, URI>, Util.Pair<Integer, Float>> lastProgressPerID;

    /**
     * paramURL, liveURI and storedURI for TracTrac connection
     */
    private final Util.Triple<URL, URI, URI> urls;

    /**
     * Tells if this tracker was created with a valid live URI. If not, the tracker will stop and unregister itself
     * from the {@link RacingEventService} after having received all stored data.
     */
    private final boolean isLiveTracking;

    /**
     * Tells whether the {@link #stop(boolean)} method has been called. This prevents further calls to that method
     * from having any effects.
     */
    private boolean stopped;

    private final TrackedRegattaRegistry trackedRegattaRegistry;

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
     * respond with the {@link RaceDefinition} when its {@link DomainFactory#getRaceID(com.tractrac.model.lib.api.event.IRace)} is called with the
     * TracTrac {@link IEvent} as argument that is used for its tracking.
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
    protected TracTracRaceTrackerImpl(DomainFactory domainFactory, URL paramURL, URI liveURI, URI storedURI, URI courseDesignUpdateURI,
            TimePoint startOfTracking, TimePoint endOfTracking, long delayToLiveInMillis,
            boolean simulateWithStartTimeNow, RaceLogStore raceLogStore, WindStore windStore, GPSFixStore gpsFixStore, String tracTracUsername, String tracTracPassword, String raceStatus, String raceVisibility, TrackedRegattaRegistry trackedRegattaRegistry)
            throws URISyntaxException, MalformedURLException, FileNotFoundException, CreateModelException, SubscriberInitializationException {
        this(ModelLocator.getEventFactory().createRace(new URI(paramURL.toString())), domainFactory,
                paramURL, liveURI, storedURI, courseDesignUpdateURI, startOfTracking, endOfTracking,
                delayToLiveInMillis, simulateWithStartTimeNow, raceLogStore, windStore, gpsFixStore, tracTracUsername,
                tracTracPassword, raceStatus, raceVisibility, trackedRegattaRegistry);
    }
    
    private TracTracRaceTrackerImpl(IRace tractracRace, DomainFactory domainFactory, URL paramURL, URI liveURI, URI storedURI, URI courseDesignUpdateURI,
            TimePoint startOfTracking, TimePoint endOfTracking, long delayToLiveInMillis, boolean simulateWithStartTimeNow,
            RaceLogStore raceLogStore, WindStore windStore, GPSFixStore gpsFixStore, String tracTracUsername, String tracTracPassword, String raceStatus, String raceVisibility, TrackedRegattaRegistry trackedRegattaRegistry) 
                throws URISyntaxException, MalformedURLException, FileNotFoundException, SubscriberInitializationException {
        this(tractracRace, null, domainFactory, paramURL, liveURI, storedURI, courseDesignUpdateURI,
                startOfTracking, endOfTracking, delayToLiveInMillis, simulateWithStartTimeNow, raceLogStore, windStore, gpsFixStore, 
                tracTracUsername, tracTracPassword, raceStatus, raceVisibility, trackedRegattaRegistry);
    }
    
    /**
     * Use this constructor if the {@link Regatta} in which to arrange the {@link RaceDefinition}s created by this
     * tracker is already known up-front, particularly if it has a specific configuration to use. Other constructors
     * may create a default {@link Regatta} with only a single default {@link Series} and {@link Fleet} which may not
     * always be what you want.
     */
    protected TracTracRaceTrackerImpl(Regatta regatta, DomainFactory domainFactory, URL paramURL, URI liveURI, URI storedURI, URI courseDesignUpdateURI,
            TimePoint startOfTracking, TimePoint endOfTracking, long delayToLiveInMillis, boolean simulateWithStartTimeNow,
            RaceLogStore raceLogStore, WindStore windStore, GPSFixStore gpsFixStore, String tracTracUsername, String tracTracPassword, String raceStatus, String raceVisibility, TrackedRegattaRegistry trackedRegattaRegistry) 
                throws URISyntaxException, MalformedURLException, FileNotFoundException, CreateModelException, SubscriberInitializationException {
        this(ModelLocator.getEventFactory().createRace(new URI(paramURL.toString())), regatta,
                domainFactory, paramURL, liveURI, storedURI, courseDesignUpdateURI, startOfTracking, endOfTracking,
                delayToLiveInMillis, simulateWithStartTimeNow, raceLogStore, windStore, gpsFixStore, tracTracUsername,
                tracTracPassword, raceStatus, raceVisibility, trackedRegattaRegistry);
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
    private TracTracRaceTrackerImpl(IRace tractracRace, final Regatta regatta, DomainFactory domainFactory,
            URL paramURL, URI liveURI, URI storedURI, URI tracTracUpdateURI, TimePoint startOfTracking, TimePoint endOfTracking,
            long delayToLiveInMillis, boolean simulateWithStartTimeNow, RaceLogStore raceLogStore, 
            WindStore windStore, GPSFixStore gpsFixStore, String tracTracUsername, String tracTracPassword, String raceStatus, String raceVisibility, TrackedRegattaRegistry trackedRegattaRegistry)
            throws URISyntaxException, MalformedURLException, FileNotFoundException, SubscriberInitializationException {
        super();
        this.trackedRegattaRegistry = trackedRegattaRegistry;
        this.tractracRace = tractracRace;
        this.tractracEvent = tractracRace.getEvent();
        urls = createID(paramURL, liveURI, storedURI);
        isLiveTracking = liveURI != null;
        this.races = new HashSet<RaceDefinition>();
        this.gpsFixStore = gpsFixStore;
        this.domainFactory = domainFactory;
        this.lastProgressPerID = new HashMap<Util.Triple<URL, URI, URI>, Util.Pair<Integer, Float>>();
        final Simulator simulator;
        if (simulateWithStartTimeNow) {
            simulator = new Simulator(windStore);
            // don't write the transformed wind fixes into the DB again... see also bug 1974 
            this.windStore = EmptyWindStore.INSTANCE;
        } else {
            simulator = null;
            this.windStore = windStore;
        }
        // check if there is a directory configured where stored data files can be cached
        // only cache files for races in REPLAY state
        if ( (raceStatus != null && raceStatus.equals(TracTracConnectionConstants.REPLAY_STATUS)) || 
                (raceVisibility != null && raceVisibility.equals(TracTracConnectionConstants.REPLAY_VISIBILITY)) ) {
            storedURI = checkForCachedStoredData(storedURI);
        }
        
        logger.info("Starting race tracker: " + tractracRace.getName() + " " + paramURL + " " + liveURI + " "
                + storedURI + " startOfTracking:" + (startOfTracking != null ? startOfTracking.asMillis() : "n/a") + " endOfTracking:" + (endOfTracking != null ? endOfTracking.asMillis() : "n/a"));
        
        // Initialize data controller using live and stored data sources
        ISubscriberFactory subscriberFactory = SubscriptionLocator.getSusbcriberFactory();
        eventSubscriber = subscriberFactory.createEventSubscriber(tractracEvent, liveURI, storedURI);
        eventSubscriber.subscribeCompetitors(new ICompetitorsListener() {
            @Override
            public void updateCompetitor(ICompetitor competitor) {
                final Competitor domainCompetitor = TracTracRaceTrackerImpl.this.domainFactory.getOrCreateCompetitor(competitor);
                logger.info("Competitor "+competitor+" was updated on TracTrac side. Maybe consider updating in competitor store as well. "+
                            "TracTrac competitor maps to "+domainCompetitor.getName()+" with sail ID "+domainCompetitor.getBoat().getSailID()+
                            " and boat class "+domainCompetitor.getBoat().getBoatClass().getName());
            }
            
            @Override
            public void deleteCompetitor(UUID competitorId) {
            }
            
            @Override
            public void addCompetitor(ICompetitor competitor) {
            }
        });
        eventSubscriber.subscribeRaces(new IRacesListener() {
            @Override public void abandonRace(UUID raceId) {}
            @Override public void addRace(IRace race) {}
            @Override public void deleteRace(UUID raceId) {}
            @Override public void reloadRace(UUID raceId) {}
            @Override public void startTracking(UUID raceId) {}
            @Override
            public void updateRace(IRace race) {
                if (Util.equalsWithNull(race, TracTracRaceTrackerImpl.this.tractracRace)) {
                    int delayToLiveInMillis = race.getLiveDelay()*1000;
                    for (RaceDefinition raceDefinition : getRaces()) {
                        DynamicTrackedRace trackedRace = getTrackedRegatta().getExistingTrackedRace(raceDefinition);
                        if (trackedRace != null) {
                            trackedRace.setDelayToLiveInMillis(delayToLiveInMillis);
                        }
                    }
                }
            }
        });
        // Start live and stored data streams
        Regatta effectiveRegatta = regatta;
        raceSubscriber = subscriberFactory.createRaceSubscriber(tractracRace, liveURI, storedURI);
        raceSubscriber.subscribeConnectionStatus(this);
        // Try to find a pre-associated event based on the Race ID
        if (effectiveRegatta == null) {
            Serializable raceID = domainFactory.getRaceID(tractracRace);
            effectiveRegatta = trackedRegattaRegistry.getRememberedRegattaForRace(raceID);
        }
        // removeRace may detach the domain regatta from the domain factory if that
        // removed the last race; therefore, it's important to getOrCreate the
        // domain regatta *after* calling removeRace
        domainFactory.removeRace(tractracRace.getEvent(), tractracRace, trackedRegattaRegistry);
        // if regatta is still null, no previous assignment of any of the races in this TracTrac event to a Regatta was
        // found;
        // in this case, create a default regatta based on the TracTrac event data
        this.regatta = effectiveRegatta == null ? domainFactory.getOrCreateDefaultRegatta(raceLogStore, tractracRace, trackedRegattaRegistry) : effectiveRegatta;
        trackedRegatta = trackedRegattaRegistry.getOrCreateTrackedRegatta(this.regatta);
        receivers = new HashSet<Receiver>();
        for (Receiver receiver : domainFactory.getUpdateReceivers(getTrackedRegatta(), delayToLiveInMillis,
                simulator, windStore, this, trackedRegattaRegistry, tractracRace, tracTracUpdateURI,
                tracTracUsername, tracTracPassword, eventSubscriber, raceSubscriber)) {
            receivers.add(receiver);
        }
        addListenersForStoredDataAndStartController(receivers);
    }

    private URI checkForCachedStoredData(URI storedURI){
        final String CACHE_DIR_PROPERTY = "tractrac.mtb.cache.dir";
        if (System.getProperty(CACHE_DIR_PROPERTY) != null) {
            final String directory = System.getProperty(CACHE_DIR_PROPERTY);
            if (new File(directory).exists()) {
                final String[] pathFragments = storedURI.getPath().split("\\/");
                final String mtbFileName = pathFragments[pathFragments.length-1];
                final String directoryAndFileName = directory+"/"+mtbFileName;
                final File f = new File(directoryAndFileName);
                if (!f.exists()) {
                    FileOutputStream mtbOutStream = null;
                    try {
                        logger.info("Starting to download " + storedURI + " to cache dir " + directoryAndFileName);
                        InputStream in = storedURI.toURL().openStream();
                        mtbOutStream = new FileOutputStream(f);
                        byte data[] = new byte[1024];
                        int count;
                        while ((count = in.read(data, 0, 1024)) != -1)
                        {
                            mtbOutStream.write(data, 0, count);
                        }
                        logger.info("Finished downloading file to cache!");
                    } catch (Exception ex) {
                        // never throw but display
                        ex.printStackTrace();
                    } finally {
                        if (mtbOutStream != null) {
                            try {
                                mtbOutStream.close();
                            } catch (IOException e) {
                                // ignore
                            }   
                        }
                    }
                } else {
                    logger.info("Found file " + directoryAndFileName + "! Reusing it for this race!");
                }
                
                try {
                    // notice us using three slashes here - this is because of a bug in the TracAPI
                    return new URI("file:///" + directoryAndFileName);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return storedURI;
    }

    @Override
    public DynamicTrackedRegatta getTrackedRegatta() {
        return trackedRegatta;
    }

    static Util.Triple<URL, URI, URI> createID(URL paramURL, URI liveURI, URI storedURI) {
        return new Util.Triple<URL, URI, URI>(paramURL, liveURI, storedURI);
    }
    
    @Override
    public Util.Triple<URL, URI, URI> getID() {
        return urls;
    }

    @Override
    public WindStore getWindStore() {
        return windStore;
    }

    @Override
    public RaceHandle getRacesHandle() {
        return new RaceHandleImpl(domainFactory, tractracRace, getTrackedRegatta(), this);
    }
    
    @Override
    public Set<RaceDefinition> getRaces() {
        return races;
    }
    
    protected void addListenersForStoredDataAndStartController(Iterable<Receiver> listenersForStoredData) {
        for (Receiver receiver : listenersForStoredData) {
            receiver.subscribe();
        }
        eventSubscriber.start();
        raceSubscriber.start();
    }
    
    @Override
    public com.sap.sailing.domain.base.Regatta getRegatta() {
        return regatta;
    }
    
    @Override
    public void stop() throws InterruptedException {
        if (!stopped) {
            stop(/* stop receivers preemtively */false);
        }
    }

    private void stop(boolean stopReceiversPreemtively) throws InterruptedException {
        if (!stopped) {
            stopped = true;
            raceSubscriber.stop();
            eventSubscriber.stop();
            for (Receiver receiver : receivers) {
                if (stopReceiversPreemtively) {
                    receiver.stopPreemptively();
                } else {
                    receiver.stopAfterProcessingQueuedEvents();
                }
            }
            lastStatus = new TrackedRaceStatusImpl(TrackedRaceStatusEnum.FINISHED, /* will be ignored */1.0);
            updateStatusOfTrackedRaces();
        }
    }

    /**
     * Propagates {@link #lastStatus} to all tracked races to which this tracker writes.
     * 
     * @see #updateStatusOfTrackedRace(DynamicTrackedRace)
     */
    private void updateStatusOfTrackedRaces() {
        for (RaceDefinition race : getRaces()) {
            DynamicTrackedRace trackedRace = getTrackedRegatta().getExistingTrackedRace(race);
            if (trackedRace != null) {
                updateStatusOfTrackedRace(trackedRace);
            }
        }
    }

    /**
     * Propagates {@link #lastStatus} to <code>trackedRace</code>'s {@link TrackedRace#getStatus() status}. If
     * {@link #lastStatus} is a {@link TrackedRaceStatusEnum#FINISHED FINISHED} status, the progress value is taken from
     * the tracked race's current status instead of overwriting it with the progress indicated by
     * {@link #lastStatus}.
     */
    private void updateStatusOfTrackedRace(DynamicTrackedRace trackedRace) {
        // can't update a race status once it has been set to FINISHED
        if (lastStatus != null && trackedRace.getStatus() != null && trackedRace.getStatus().getStatus() != TrackedRaceStatusEnum.FINISHED) {
            final TrackedRaceStatus status;
            if (lastStatus.getStatus() == TrackedRaceStatusEnum.FINISHED) {
                // in this case use the tracked race's progress value:
                status = new TrackedRaceStatusImpl(lastStatus.getStatus(), trackedRace.getStatus() == null ? 0.0
                        : trackedRace.getStatus().getLoadingProgress());
            } else {
                status = lastStatus;
            }
            trackedRace.setStatus(status);
        }
    }

    private void storedDataProgress(float progress) {
        if (lastStatus.getStatus().equals(TrackedRaceStatusEnum.ERROR)) {
            return;
        }
        Integer counter = 0;
        final Util.Pair<Integer, Float> lastProgressPair = lastProgressPerID.get(getID());
        if (lastProgressPair != null) {
            Float lastProgress = lastProgressPair.getB();
            counter = lastProgressPair.getA();
            if (progress < lastProgress.floatValue()) {
                if (counter.intValue() > MAX_STORED_PACKET_HOP_ALLOWANCE) {
                    try {
                        logger.severe("Got " + MAX_STORED_PACKET_HOP_ALLOWANCE + " times a value for progress " + progress + " that is lower than one already received " + lastProgress + "! This is a severe error - stopping receivers for " + getID() + " now!");
                        stop(/* stopReceiversPreemptively */ true);
                        /* make sure to indicate that this race is erroneous */
                        lastStatus = new TrackedRaceStatusImpl(TrackedRaceStatusEnum.ERROR, 0.0);
                        updateStatusOfTrackedRaces();
                        return;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    counter += 1;
                }
            } 
        }
        logger.info("Stored data progress in tracker "+getID()+" for race(s) "+getRaces()+": "+progress);
        lastStatus = new TrackedRaceStatusImpl(progress==1.0 ? TrackedRaceStatusEnum.TRACKING : TrackedRaceStatusEnum.LOADING, progress);
        lastProgressPerID.put(getID(), new Util.Pair<Integer, Float>(counter, progress));
        updateStatusOfTrackedRaces();
    }

    @Override
    public void addRaceDefinition(RaceDefinition race, DynamicTrackedRace trackedRace) {
        races.add(race);
        updateStatusOfTrackedRace(trackedRace);
    }

    @Override
    public void gotLiveDataEvent(ILiveDataEvent liveDataEvent) {
        logger.info("Status change in tracker "+getID()+" for race(s) "+getRaces()+": "+liveDataEvent);
    }

    @Override
    public void gotStoredDataEvent(IStoredDataEvent storedDataEvent) {
        logger.info("Status change in tracker "+getID()+" for race(s) "+getRaces()+": "+storedDataEvent);
        switch (storedDataEvent.getType()) {
        case Begin:
            logger.info("Stored data begin in tracker "+getID()+" for race(s) "+getRaces());
            lastStatus = new TrackedRaceStatusImpl(TrackedRaceStatusEnum.LOADING, 0);
            updateStatusOfTrackedRaces();
            break;
        case End:
            logger.info("Stored data end in tracker "+getID()+" for race(s) "+getRaces());
            if (isLiveTracking) {
                lastStatus = new TrackedRaceStatusImpl(TrackedRaceStatusEnum.TRACKING, 1);
                updateStatusOfTrackedRaces();
            }
            break;
        case Progress:
            storedDataProgress(storedDataEvent.getProgress());
            break;
        case Error:
            logger.warning("Error with stored data in tracker "+getID()+" for race(s) "+getRaces()+": "+storedDataEvent.getError());
            break;
        }
    }

    @Override
    public void stopped(Object o) {
        logger.info("stopped TracTrac tracking in tracker "+getID()+" for "+getRaces()+" while in status "+lastStatus);
        lastStatus = new TrackedRaceStatusImpl(TrackedRaceStatusEnum.FINISHED, 1.0);
        updateStatusOfTrackedRaces();
        if (!stopped) {
            try {
                for (RaceDefinition race : getRaces()) {
                    // See also bug 1517; with TracAPI we assume that when stopped(IEvent) is called by the TracAPI then
                    // all subscriptions have received all their data and it's therefore safe to stop all subscriptions
                    // at this point without missing any data.
                    trackedRegattaRegistry.stopTracking(regatta, race);
                }
            } catch (InterruptedException | IOException e) {
                logger.log(Level.INFO, "Interrupted while trying to stop tracker "+this, e);
            }
        }
    }

    @Override
    public GPSFixStore getGPSFixStore() {
        return gpsFixStore;
    }
}
