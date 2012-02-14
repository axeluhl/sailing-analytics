package com.sap.sailing.server.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.DefaultLeaderboardName;
import com.sap.sailing.domain.common.EventAndRaceIdentifier;
import com.sap.sailing.domain.common.EventFetcher;
import com.sap.sailing.domain.common.EventIdentifier;
import com.sap.sailing.domain.common.EventName;
import com.sap.sailing.domain.common.RaceFetcher;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RaceInLeaderboard;
import com.sap.sailing.domain.leaderboard.impl.LeaderboardGroupImpl;
import com.sap.sailing.domain.leaderboard.impl.LeaderboardImpl;
import com.sap.sailing.domain.leaderboard.impl.ResultDiscardingRuleImpl;
import com.sap.sailing.domain.leaderboard.impl.ScoreCorrectionImpl;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.swisstimingadapter.Race;
import com.sap.sailing.domain.swisstimingadapter.SailMasterConnector;
import com.sap.sailing.domain.swisstimingadapter.SailMasterMessage;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingFactory;
import com.sap.sailing.domain.swisstimingadapter.persistence.SwissTimingAdapterPersistence;
import com.sap.sailing.domain.tracking.DynamicTrackedEvent;
import com.sap.sailing.domain.tracking.RaceListener;
import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sailing.domain.tracking.RacesHandle;
import com.sap.sailing.domain.tracking.TrackedEvent;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tracking.WindTracker;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedEventImpl;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.JSONService;
import com.sap.sailing.domain.tractracadapter.RaceRecord;
import com.sap.sailing.domain.tractracadapter.Receiver;
import com.sap.sailing.expeditionconnector.ExpeditionListener;
import com.sap.sailing.expeditionconnector.ExpeditionWindTrackerFactory;
import com.sap.sailing.expeditionconnector.UDPExpeditionReceiver;
import com.sap.sailing.server.RacingEventService;

public class RacingEventServiceImpl implements RacingEventService, EventFetcher, RaceFetcher {
    private static final Logger logger = Logger.getLogger(RacingEventServiceImpl.class.getName());

    /**
     * A scheduler for the periodic checks of the paramURL documents for the advent of {@link ControlPoint}s
     * with static position information otherwise not available through <code>MarkPassingReceiver</code>'s events.
     */
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    private final DomainFactory tractracDomainFactory;
    
    private final com.sap.sailing.domain.swisstimingadapter.DomainFactory swissTimingDomainFactory;
    
    private final ExpeditionWindTrackerFactory windTrackerFactory;
    
    protected final Map<String, Event> eventsByName;
    
    protected final Map<Event, Set<RaceTracker>> raceTrackersByEvent;
    
    /**
     * Remembers the wind tracker and the port on which the UDP receiver with which the wind tracker is
     * registers is listening for incoming Expedition messages.
     */
    private final Map<RaceDefinition, WindTracker> windTrackers;
    
    /**
     * Remembers the trackers by paramURL/liveURI/storedURI to avoid duplication
     */
    protected final Map<Object, RaceTracker> raceTrackersByID;
    
    /**
     * Leaderboards managed by this racing event service
     */
    private final Map<String, Leaderboard> leaderboardsByName;
    
    private final Map<String, LeaderboardGroup> leaderboardGroupsByName;
    
    private Set<DynamicTrackedEvent> eventsObservedForDefaultLeaderboard = new HashSet<DynamicTrackedEvent>();
    
    private final MongoObjectFactory mongoObjectFactory;
    
    private final DomainObjectFactory domainObjectFactory;
    
    private final SwissTimingFactory swissTimingFactory;
    
    private final SwissTimingAdapterPersistence swissTimingAdapterPersistence;

    private final Map<Event, DynamicTrackedEvent> eventTrackingCache;

    public RacingEventServiceImpl() {
        tractracDomainFactory = DomainFactory.INSTANCE;
        domainObjectFactory = DomainObjectFactory.INSTANCE;
        mongoObjectFactory = MongoObjectFactory.INSTANCE;
        swissTimingFactory = SwissTimingFactory.INSTANCE;
        swissTimingDomainFactory = com.sap.sailing.domain.swisstimingadapter.DomainFactory.INSTANCE;
        swissTimingAdapterPersistence = SwissTimingAdapterPersistence.INSTANCE;
        windTrackerFactory = ExpeditionWindTrackerFactory.getInstance();
        eventsByName = new HashMap<String, Event>();
        eventTrackingCache = new HashMap<Event, DynamicTrackedEvent>();
        raceTrackersByEvent = new HashMap<Event, Set<RaceTracker>>();
        windTrackers = new HashMap<RaceDefinition, WindTracker>();
        raceTrackersByID = new HashMap<Object, RaceTracker>();
        leaderboardGroupsByName = new HashMap<String, LeaderboardGroup>();
        leaderboardsByName = new HashMap<String, Leaderboard>();
        // Add one default leaderboard that aggregates all races currently tracked by this service.
        // This is more for debugging purposes than for anything else.
        addLeaderboard(DefaultLeaderboardName.DEFAULT_LEADERBOARD_NAME, new int[] { 5, 8 });
        loadStoredLeaderboardsAndGroups();
    }
    
    private void loadStoredLeaderboardsAndGroups() {
        //Loading all leaderboard groups and putting the contained leaderboards
        for (LeaderboardGroup leaderboardGroup : domainObjectFactory.getAllLeaderboardGroups()) {
            leaderboardGroupsByName.put(leaderboardGroup.getName(), leaderboardGroup);
            for (Leaderboard leaderboard : leaderboardGroup.getLeaderboards()) {
                leaderboardsByName.put(leaderboard.getName(), leaderboard);
            }
        }
        //Loading the remaining leaderboards
        for (Leaderboard leaderboard : domainObjectFactory.getLeaderboardsNotInGroup()) {
            leaderboardsByName.put(leaderboard.getName(), leaderboard);
        }
    }
    
    @Override
    public Leaderboard addLeaderboard(String name, int[] discardThresholds) {
        Leaderboard result = new LeaderboardImpl(name, new ScoreCorrectionImpl(), new ResultDiscardingRuleImpl(
                discardThresholds));
        synchronized (leaderboardsByName) {
            if (leaderboardsByName.containsKey(name)) {
                throw new IllegalArgumentException("Leaderboard with name "+name+" already exists");
            }
            leaderboardsByName.put(name, result);
        }
        mongoObjectFactory.storeLeaderboard(result);
        return result;
    }
    
    @Override
    public void renameLeaderboard(String oldName, String newName) {
        synchronized (leaderboardsByName) {
            if (!leaderboardsByName.containsKey(oldName)) {
                throw new IllegalArgumentException("No leaderboard with name "+oldName+" found");
            }
            if (leaderboardsByName.containsKey(newName)) {
                throw new IllegalArgumentException("Leaderboard with name "+newName+" already exists");
            }
            Leaderboard toRename = leaderboardsByName.remove(oldName);
            toRename.setName(newName);
            leaderboardsByName.put(newName, toRename);
            mongoObjectFactory.renameLeaderboard(oldName, newName);
        }
    }
    
    @Override
    public void updateStoredLeaderboard(Leaderboard leaderboard) {
        mongoObjectFactory.storeLeaderboard(leaderboard);
    }
    
    @Override
    public void removeLeaderboard(String leaderboardName) {
        synchronized (leaderboardsByName) {
            leaderboardsByName.remove(leaderboardName);
        }
        mongoObjectFactory.removeLeaderboard(leaderboardName);
    }
    
    @Override
    public Leaderboard getLeaderboardByName(String name) {
        synchronized (leaderboardsByName) {
            return leaderboardsByName.get(name);
        }
    }
    
    @Override
    public Map<String, Leaderboard> getLeaderboards() {
        synchronized (leaderboardsByName) {
            return Collections.unmodifiableMap(new HashMap<String, Leaderboard>(leaderboardsByName));
        }
    }

    private DomainFactory getDomainFactory() {
        return tractracDomainFactory;
    }
    
    @Override
    public SwissTimingFactory getSwissTimingFactory() {
        return swissTimingFactory;
    }
    
    @Override
    public Iterable<Event> getAllEvents() {
        return Collections.unmodifiableCollection(eventsByName.values());
    }
    
    @Override
    public boolean isRaceBeingTracked(RaceDefinition r) {
        for (Set<RaceTracker> trackers : raceTrackersByEvent.values()) {
            for (RaceTracker tracker : trackers) {
                if (tracker.getRaces() != null && tracker.getRaces().contains(r)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Event getEventByName(String name) {
        return eventsByName.get(name);
    }

    @Override
    public synchronized Event addEvent(URL jsonURL, URI liveURI, URI storedURI, WindStore windStore, long timeoutInMilliseconds) throws URISyntaxException, IOException, ParseException, org.json.simple.parser.ParseException {
        JSONService jsonService = getDomainFactory().parseJSONURL(jsonURL);
        Event event = null;
        for (RaceRecord rr : jsonService.getRaceRecords()) {
            URL paramURL = rr.getParamURL();
            event = addTracTracRace(paramURL, liveURI, storedURI, windStore, timeoutInMilliseconds).getEvent();
        }
        return event;
    }

    @Override
    public Pair<String, List<RaceRecord>> getTracTracRaceRecords(URL jsonURL) throws IOException, ParseException, org.json.simple.parser.ParseException, URISyntaxException {
        JSONService jsonService = getDomainFactory().parseJSONURL(jsonURL);
        return new Pair<String, List<RaceRecord>>(jsonService.getEventName(), jsonService.getRaceRecords());
    }
    
    @Override
    public List<com.sap.sailing.domain.swisstimingadapter.RaceRecord> getSwissTimingRaceRecords(String hostname,
            int port, boolean canSendRequests) throws InterruptedException, UnknownHostException, IOException, ParseException {
        List<com.sap.sailing.domain.swisstimingadapter.RaceRecord> result = new ArrayList<com.sap.sailing.domain.swisstimingadapter.RaceRecord>();
        SailMasterConnector swissTimingConnector = swissTimingFactory.getOrCreateSailMasterConnector(hostname, port, swissTimingAdapterPersistence,
                canSendRequests);
        for (Race race : swissTimingConnector.getRaces()) {
            TimePoint startTime = swissTimingConnector.getStartTime(race.getRaceID());
            result.add(new com.sap.sailing.domain.swisstimingadapter.RaceRecord(race.getRaceID(), race.getDescription(),
                    startTime==null?null:startTime.asDate()));
        }
        return result;
    }

    @Override
    public synchronized RacesHandle addSwissTimingRace(String raceID, String hostname, int port, boolean canSendRequests,
            WindStore windStore, long timeoutInMilliseconds) throws InterruptedException, UnknownHostException, IOException, ParseException {
        Triple<String, String, Integer> key = new Triple<String, String, Integer>(raceID, hostname, port);
        RaceTracker tracker = raceTrackersByID.get(key);
        if (tracker == null) {
            tracker = getSwissTimingFactory().createRaceTracker(raceID, hostname, port, canSendRequests,
                    windStore, swissTimingAdapterPersistence, swissTimingDomainFactory, this);
            raceTrackersByID.put(tracker.getID(), tracker);
            Set<RaceTracker> trackers = raceTrackersByEvent.get(tracker.getEvent());
            if (trackers == null) {
                trackers = new HashSet<RaceTracker>();
                raceTrackersByEvent.put(tracker.getEvent(), trackers);
            }
            trackers.add(tracker);
            // TODO we assume here that the event name is unique which necessesitates adding the boat class name to it in EventImpl constructor
            String eventName = tracker.getEvent().getName();
            Event eventWithName = eventsByName.get(eventName);
            // TODO we assume here that the event name is unique which necessesitates adding the boat class name to it in EventImpl constructor
            if (eventWithName != null) {
                if (eventWithName != tracker.getEvent()) {
                    throw new RuntimeException("Internal error. Two Event objects with equal name "+eventName);
                }
            } else {
                eventsByName.put(eventName, tracker.getEvent());
            }
        } else {
            WindStore existingTrackersWindStore = tracker.getWindStore();
            if (!existingTrackersWindStore.equals(windStore)) {
                logger.warning("Wind store mismatch. Requested wind store: "+windStore+
                        ". Wind store in use by existing tracker: "+existingTrackersWindStore);
            }
        }
        DynamicTrackedEvent trackedEvent = tracker.getTrackedEvent();
        ensureEventIsObservedForDefaultLeaderboardAndAutoLeaderboardLinking(trackedEvent);
        if (timeoutInMilliseconds != -1) {
            scheduleAbortTrackerAfterInitialTimeout(tracker, timeoutInMilliseconds);
        }
        return tracker.getRacesHandle();
    }

    @Override
    public synchronized RacesHandle addTracTracRace(URL paramURL, URI liveURI, URI storedURI, WindStore windStore,
            long timeoutInMilliseconds) throws MalformedURLException, FileNotFoundException, URISyntaxException {
        Triple<URL, URI, URI> key = new Triple<URL, URI, URI>(paramURL, liveURI, storedURI);
        RaceTracker tracker = raceTrackersByID.get(key);
        if (tracker == null) {
            tracker = getDomainFactory().createRaceTracker(paramURL, liveURI, storedURI, windStore, this);
            raceTrackersByID.put(tracker.getID(), tracker);
            Set<RaceTracker> trackers = raceTrackersByEvent.get(tracker.getEvent());
            if (trackers == null) {
                trackers = new HashSet<RaceTracker>();
                raceTrackersByEvent.put(tracker.getEvent(), trackers);
            }
            trackers.add(tracker);
            // TODO we assume here that the event name is unique which necessesitates adding the boat class name to it in EventImpl constructor
            String eventName = tracker.getEvent().getName();
            Event eventWithName = eventsByName.get(eventName);
            // TODO we assume here that the event name is unique which necessesitates adding the boat class name to it in EventImpl constructor
            if (eventWithName != null) {
                if (eventWithName != tracker.getEvent()) {
                    if (Util.isEmpty(eventWithName.getAllRaces())) {
                        // probably, tracker removed the last races from the old event and created a new one
                        eventsByName.put(eventName, tracker.getEvent());
                    } else {
                        throw new RuntimeException("Internal error. Two Event objects with equal name "+eventName);
                    }
                }
            } else {
                eventsByName.put(eventName, tracker.getEvent());
            }
        } else {
            WindStore existingTrackersWindStore = tracker.getWindStore();
            if (!existingTrackersWindStore.equals(windStore)) {
                logger.warning("Wind store mismatch. Requested wind store: "+windStore+
                        ". Wind store in use by existing tracker: "+existingTrackersWindStore);
            }
        }
        DynamicTrackedEvent trackedEvent = tracker.getTrackedEvent();
        ensureEventIsObservedForDefaultLeaderboardAndAutoLeaderboardLinking(trackedEvent);
        if (timeoutInMilliseconds != -1) {
            scheduleAbortTrackerAfterInitialTimeout(tracker, timeoutInMilliseconds);
        }
        return tracker.getRacesHandle();
    }

    private void ensureEventIsObservedForDefaultLeaderboardAndAutoLeaderboardLinking(DynamicTrackedEvent trackedEvent) {
        synchronized (eventsObservedForDefaultLeaderboard) {
            if (!eventsObservedForDefaultLeaderboard.contains(trackedEvent)) {
                trackedEvent.addRaceListener(new RaceListener() {
                    @Override
                    public void raceRemoved(TrackedRace trackedRace) {
                    }

                    @Override
                    public void raceAdded(TrackedRace trackedRace) {
                        linkRaceToConfiguredLeaderboardColumns(trackedRace);
                        leaderboardsByName.get(DefaultLeaderboardName.DEFAULT_LEADERBOARD_NAME).addRace(trackedRace,
                                trackedRace.getRace().getName(), /* medalRace */ false);
                    }
                });
                eventsObservedForDefaultLeaderboard.add(trackedEvent);
            }
        }
    }

    /**
     * Based on the <code>trackedRace</code>'s {@link TrackedRace#getRaceIdentifier() race identifier}, the tracked race
     * is (re-)associated to all {@link RaceInLeaderboard race columns} that currently have no
     * {@link RaceInLeaderboard#getTrackedRace() tracked race assigned} and whose
     * {@link RaceInLeaderboard#getRaceIdentifier() race identifier} equals that of <code>trackedRace</code>.
     */
    private void linkRaceToConfiguredLeaderboardColumns(TrackedRace trackedRace) {
        RaceIdentifier trackedRaceIdentifier = trackedRace.getRaceIdentifier();
        for (Leaderboard leaderboard : getLeaderboards().values()) {
            for (RaceInLeaderboard column : leaderboard.getRaceColumns()) {
                if (trackedRaceIdentifier.equals(column.getRaceIdentifier()) && column.getTrackedRace() == null) {
                    column.setTrackedRace(trackedRace);
                }
            }
        }
    }

    @Override
    public synchronized void stopTracking(Event event) throws MalformedURLException, IOException, InterruptedException {
        if (raceTrackersByEvent.containsKey(event)) {
            for (RaceTracker raceTracker : raceTrackersByEvent.get(event)) {
                raceTracker.stop(); // this also removes the TrackedRace from trackedEvent
                raceTrackersByID.remove(raceTracker.getID());
            }
            raceTrackersByEvent.remove(event);
        }
    }
    
    @Override
    public synchronized void stopTrackingAndRemove(Event event) throws MalformedURLException, IOException, InterruptedException {
        stopTracking(event);
        if (event != null) {
            if (event.getName() != null) {
                eventsByName.remove(event.getName());
            }
            for (RaceDefinition race : event.getAllRaces()) {
                stopTrackingWind(event, race);
                // remove from default leaderboard
                Leaderboard defaultLeaderboard = getLeaderboardByName(DefaultLeaderboardName.DEFAULT_LEADERBOARD_NAME);
                defaultLeaderboard.removeRaceColumn(race.getName());
            }
        }
    }

    /**
     * The tracker will initially try to connect to the TracTrac infrastructure to obtain basic race master data. If
     * this fails after some timeout, to avoid garbage and lingering threads, the task scheduled by this method will
     * check after the timeout expires if race master data was successfully received. If so, the tracker continues
     * normally. Otherwise, the tracker is shut down orderly by {@link Receiver#stopPreemptively() stopping} all
     * receivers and {@link DataController#stop(boolean) stopping} the TracTrac controller for this tracker.
     * 
     * @return the scheduled task, in case the caller wants to {@link ScheduledFuture#cancel(boolean) cancel} it, e.g.,
     *         when the tracker is stopped or has successfully received the race
     */
    private ScheduledFuture<?> scheduleAbortTrackerAfterInitialTimeout(final RaceTracker tracker, final long timeoutInMilliseconds) {
        ScheduledFuture<?> task = scheduler.schedule(new Runnable() {
            @Override public void run() {
                if (tracker.getRaces() == null || tracker.getRaces().isEmpty()) {
                    try {
                        Event event = tracker.getEvent();
                        logger.log(Level.SEVERE, "RaceDefinition for a race in event "+event.getName()+" not obtained within "+
                                timeoutInMilliseconds+"ms. Aborting tracker for this race.");
                        Set<RaceTracker> trackersForEvent = raceTrackersByEvent.get(event);
                        if (trackersForEvent != null) {
                            trackersForEvent.remove(tracker);
                        }
                        tracker.stop();
                        raceTrackersByID.remove(tracker.getID());
                        if (trackersForEvent == null || trackersForEvent.isEmpty()) {
                            stopTracking(event);
                        }
                    } catch (Exception e) {
                        logger.throwing(RacingEventServiceImpl.class.getName(), "scheduleAbortTrackerAfterInitialTimeout", e);
                        e.printStackTrace();
                    }
                }
            }
        }, /* delay */ timeoutInMilliseconds, /* unit */ TimeUnit.MILLISECONDS);
        return task;
    }

    @Override
    public synchronized void stopTracking(Event event, RaceDefinition race) throws MalformedURLException, IOException, InterruptedException {
        logger.info("Stopping tracking for "+race+"...");
        if (raceTrackersByEvent.containsKey(event)) {
            Iterator<RaceTracker> trackerIter = raceTrackersByEvent.get(event).iterator();
            while (trackerIter.hasNext()) {
                RaceTracker raceTracker = trackerIter.next();
                if (raceTracker.getRaces() != null && raceTracker.getRaces().contains(race)) {
                    logger.info("Found tracker to stop for races "+raceTracker.getRaces());
                    raceTracker.stop(); // this also removes the TrackedRace from trackedEvent
                    // do not remove the tracker from raceTrackersByEvent, because it should still exist there, but with the state "non-tracked"
                    trackerIter.remove();
                    raceTrackersByID.remove(raceTracker.getID());
                }
            }
        } else {
            logger.warning("Didn't find any trackers for event "+event);
        }
        stopTrackingWind(event, race);
        // if the last tracked race was removed, remove the entire event
        if (raceTrackersByEvent.get(event).isEmpty()) {
            stopTracking(event);
        }
    }

    @Override
    public synchronized void removeEvent(Event event) throws MalformedURLException, IOException, InterruptedException {
        for (RaceDefinition race : event.getAllRaces()) {
            removeRace(event, race);
        }
    }
    
    @Override
    public synchronized void removeRace(Event event, RaceDefinition race) throws MalformedURLException,
            IOException, InterruptedException {
        logger.info("Removing the race + " + race + "...");
        stopAllTrackersForWhichRaceIsLastReachable(event, race);
        stopTrackingWind(event, race);
        TrackedRace trackedRace = getExistingTrackedRace(event, race);
        if (trackedRace != null) {
            TrackedEvent trackedEvent = getTrackedEvent(event);
            if (trackedEvent != null) {
                trackedEvent.removeTrackedRace(trackedRace);
            }
            if (Util.isEmpty(trackedEvent.getTrackedRaces())) {
                removeTrackedEvent(event);
            }
            for (Leaderboard leaderboard : getLeaderboards().values()) {
                boolean changed = false;
                for (RaceInLeaderboard raceColumn : leaderboard.getRaceColumns()) {
                    if (raceColumn.getTrackedRace() == trackedRace) {
                        raceColumn.setTrackedRace(null); // but leave the RaceIdentifier on the race column untouched, e.g., for later re-load
                        changed = true;
                    }
                }
                if (changed) {
                    updateStoredLeaderboard(leaderboard);
                }
            }
        }
        // remove the race from the event
        event.removeRace(race);
        if (Util.isEmpty(event.getAllRaces())) {
            eventsByName.remove(event.getName());
        }
    }

    /**
     * Doesn't stop any wind trackers
     */
    private void stopAllTrackersForWhichRaceIsLastReachable(Event event, RaceDefinition race)
            throws MalformedURLException, IOException, InterruptedException {
        if (raceTrackersByEvent.containsKey(event)) {
            Iterator<RaceTracker> trackerIter = raceTrackersByEvent.get(event).iterator();
            while (trackerIter.hasNext()) {
                RaceTracker raceTracker = trackerIter.next();
                if (raceTracker.getRaces() != null && raceTracker.getRaces().contains(race)) {
                    boolean foundReachableRace = false;
                    for (RaceDefinition raceTrackedByTracker : raceTracker.getRaces()) {
                        if (raceTrackedByTracker != race && isReachable(event, raceTrackedByTracker)) {
                            foundReachableRace = true;
                            break;
                        }
                    }
                    if (!foundReachableRace) {
                        // firstly stop the tracker
                        raceTracker.stop();
                        // remove it from the raceTrackers by Event
                        trackerIter.remove();
                        raceTrackersByID.remove(raceTracker.getID());
                        // if the last tracked race was removed, remove the entire event
                        if (raceTrackersByEvent.get(event).isEmpty()) {
                            stopTracking(event);
                        }
                    }
                }
            }
        }
    }

    private boolean isReachable(Event event, RaceDefinition race) {
        return Util.contains(event.getAllRaces(), race);
    }

    @Override
    public void startTrackingWind(Event event, RaceDefinition race,
            boolean correctByDeclination) throws SocketException {
        windTrackerFactory.createWindTracker(getOrCreateTrackedEvent(event), race, correctByDeclination);
    }

    @Override
    public synchronized void stopTrackingWind(Event event, RaceDefinition race) throws SocketException, IOException {
        WindTracker windTracker = windTrackers.get(race);
        if (windTracker != null) {
            windTracker.stop();
            windTrackers.remove(race);
        }
    }

    @Override
    public synchronized Iterable<Triple<Event, RaceDefinition, String>> getWindTrackedRaces() {
        List<Triple<Event, RaceDefinition, String>> result = new ArrayList<Triple<Event, RaceDefinition, String>>();
        for (Event event : eventsByName.values()) {
            for (RaceDefinition race : event.getAllRaces()) {
                if (windTrackers.containsKey(race)) {
                    result.add(new Triple<Event, RaceDefinition, String>(event, race, windTrackers.get(race).toString()));
                }
            }
        }
        return result;
    }

    @Override
    public TrackedRace getTrackedRace(Event e, RaceDefinition r) {
        return getOrCreateTrackedEvent(e).getTrackedRace(r);
    }
    
    private TrackedRace getExistingTrackedRace(Event e, RaceDefinition r) {
        return getOrCreateTrackedEvent(e).getExistingTrackedRace(r);
    }
    
    @Override
    public DynamicTrackedEvent getOrCreateTrackedEvent(Event event) {
        synchronized (eventTrackingCache) {
            DynamicTrackedEvent result = eventTrackingCache.get(event);
            if (result == null) {
                result = new DynamicTrackedEventImpl(event);
                eventTrackingCache.put(event, result);
            }
            return result;
        }
    }

    @Override
    public DynamicTrackedEvent getTrackedEvent(com.sap.sailing.domain.base.Event event) {
        return eventTrackingCache.get(event);
    }

    @Override
    public void removeTrackedEvent(Event event) {
        eventTrackingCache.remove(event);
    }

    @Override
    public void storeSwissTimingDummyRace(String racMessage, String stlMessage, String ccgMessage){
        SailMasterMessage racSMMessage = swissTimingFactory.createMessage(racMessage, null);
        SailMasterMessage stlSMMessage = swissTimingFactory.createMessage(stlMessage, null);
        SailMasterMessage ccgSMMessage = swissTimingFactory.createMessage(ccgMessage, null);
        if (swissTimingAdapterPersistence.getRace(stlSMMessage.getRaceID()) != null) {
            throw new IllegalArgumentException("Race with raceID \"" + stlSMMessage.getRaceID() + "\" already exists.");
        }
        else {
            swissTimingAdapterPersistence.storeSailMasterMessage(racSMMessage);
            swissTimingAdapterPersistence.storeSailMasterMessage(stlSMMessage);
            swissTimingAdapterPersistence.storeSailMasterMessage(ccgSMMessage);
        }
    }

    @Override
    public Event getEvent(EventIdentifier eventIdentifier) {
        return (Event) eventIdentifier.getEvent(this);
    }

    @Override
    public TrackedRace getTrackedRace(RaceIdentifier raceIdentifier) {
        return (TrackedRace) raceIdentifier.getTrackedRace(this);
    }

    @Override
    public TrackedRace getExistingTrackedRace(RaceIdentifier raceIdentifier) {
        return (TrackedRace) raceIdentifier.getExistingTrackedRace(this);
    }

    @Override
    public RaceDefinition getRace(EventAndRaceIdentifier eventNameAndRaceName) {
        RaceDefinition result = null;
        Event event = getEvent(eventNameAndRaceName);
        if (event != null) {
            result = event.getRaceByName(eventNameAndRaceName.getRaceName());
        }
        return result;
    }

    @Override
    public Event getEvent(EventName eventIdentifier) {
        return getEventByName(eventIdentifier.getEventName());
    }

    @Override
    public Map<String, LeaderboardGroup> getLeaderboardGroups() {
        synchronized (leaderboardGroupsByName) {
            return Collections.unmodifiableMap(new HashMap<String, LeaderboardGroup>(leaderboardGroupsByName));
        }
    }

    @Override
    public LeaderboardGroup getLeaderboardGroupByName(String groupName) {
        synchronized (leaderboardGroupsByName) {
            return leaderboardGroupsByName.get(groupName);
        }
    }

    @Override
    public LeaderboardGroup addLeaderboardGroup(String groupName, String description, List<String> leaderboardNames) {
        ArrayList<Leaderboard> leaderboards = new ArrayList<>();
        synchronized (leaderboardsByName) {
            for (String leaderboardName : leaderboardNames) {
                Leaderboard leaderboard = leaderboardsByName.get(leaderboardName);
                if (leaderboard == null) {
                    throw new IllegalArgumentException("No leaderboard with name " + leaderboardName + " found");
                } else {
                    leaderboards.add(leaderboard);
                }
            }
        }
        LeaderboardGroup result = new LeaderboardGroupImpl(groupName, description, leaderboards);
        synchronized (leaderboardGroupsByName) {
            if (leaderboardGroupsByName.containsKey(groupName)) {
                throw new IllegalArgumentException("Leaderboard group with name " + groupName + " already exists");
            }
            leaderboardGroupsByName.put(groupName, result);
        }
        mongoObjectFactory.storeLeaderboardGroup(result);
        return result;
    }

    @Override
    public void removeLeaderboardGroup(String groupName) {
        synchronized (leaderboardGroupsByName) {
            leaderboardGroupsByName.remove(groupName);
        }
        mongoObjectFactory.removeLeaderboardGroup(groupName);
    }

    @Override
    public void renameLeaderboardGroup(String oldName, String newName) {
        synchronized (leaderboardGroupsByName) {
            if (!leaderboardGroupsByName.containsKey(oldName)) {
                throw new IllegalArgumentException("No leaderboard group with name " + oldName + " found");
            }
            if (leaderboardGroupsByName.containsKey(newName)) {
                throw new IllegalArgumentException("Leaderboard group with name " + newName + " already exists");
            }
            LeaderboardGroup toRename = leaderboardGroupsByName.remove(oldName);
            toRename.setName(newName);
            leaderboardGroupsByName.put(newName, toRename);
            mongoObjectFactory.renameLeaderboardGroup(oldName, newName);
        }
    }

    @Override
    public void updateStoredLeaderboardGroup(LeaderboardGroup leaderboardGroup) {
        mongoObjectFactory.storeLeaderboardGroup(leaderboardGroup);
    }
    
    @Override
    public void addExpeditionListener(ExpeditionListener listener, boolean validMessagesOnly) throws SocketException {
        UDPExpeditionReceiver receiver = windTrackerFactory.getOrCreateWindReceiverOnDefaultPort();
        receiver.addListener(listener, validMessagesOnly);
    }

    @Override
    public void removeExpeditionListener(ExpeditionListener listener) throws SocketException {
        UDPExpeditionReceiver receiver = windTrackerFactory.getOrCreateWindReceiverOnDefaultPort();
        receiver.removeListener(listener);
    }

}
