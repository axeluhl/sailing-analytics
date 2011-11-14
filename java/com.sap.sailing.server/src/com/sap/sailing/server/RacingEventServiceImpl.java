package com.sap.sailing.server;

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
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.impl.LeaderboardImpl;
import com.sap.sailing.domain.leaderboard.impl.ResultDiscardingRuleImpl;
import com.sap.sailing.domain.leaderboard.impl.ScoreCorrectionImpl;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingFactory;
import com.sap.sailing.domain.swisstimingadapter.persistence.SwissTimingAdapterPersistence;
import com.sap.sailing.domain.tracking.DynamicTrackedEvent;
import com.sap.sailing.domain.tracking.RaceHandle;
import com.sap.sailing.domain.tracking.RaceListener;
import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tracking.WindTracker;
import com.sap.sailing.domain.tracking.WindTrackerFactory;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedEventImpl;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.JSONService;
import com.sap.sailing.domain.tractracadapter.RaceRecord;
import com.sap.sailing.domain.tractracadapter.Receiver;
import com.sap.sailing.expeditionconnector.ExpeditionWindTrackerFactory;
import com.sap.sailing.util.Util.Pair;
import com.sap.sailing.util.Util.Triple;

public class RacingEventServiceImpl implements RacingEventService {
    private static final Logger logger = Logger.getLogger(RacingEventServiceImpl.class.getName());

    /**
     * A scheduler for the periodic checks of the paramURL documents for the advent of {@link ControlPoint}s
     * with static position information otherwise not available through <code>MarkPassingReceiver</code>'s events.
     */
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    private final DomainFactory domainFactory;
    
    private final WindTrackerFactory windTrackerFactory;
    
    private final Map<String, Event> eventsByName;
    
    private final Map<Event, Set<RaceTracker>> raceTrackersByEvent;
    
    /**
     * Remembers the wind tracker and the port on which the UDP receiver with which the wind tracker is
     * registers is listening for incoming Expedition messages.
     */
    private final Map<RaceDefinition, WindTracker> windTrackers;
    
    /**
     * Remembers the trackers by paramURL/liveURI/storedURI to avoid duplication
     */
    private final Map<Object, RaceTracker> raceTrackersByID;
    
    /**
     * Leaderboards managed by this racing event service
     */
    private final Map<String, Leaderboard> leaderboardsByName;
    
    private static final String DEFAULT_LEADERBOARD_NAME = "Default Leaderboard";
    
    private Set<DynamicTrackedEvent> eventsObservedForDefaultLeaderboard = new HashSet<DynamicTrackedEvent>();
    
    private final MongoObjectFactory mongoObjectFactory;
    
    private final DomainObjectFactory domainObjectFactory;
    
    private final SwissTimingFactory swissTimingFactory;
    
    private final SwissTimingAdapterPersistence swissTimingAdapterPersistence;

    private final Map<Event, DynamicTrackedEvent> eventTrackingCache;

    public RacingEventServiceImpl() {
        domainFactory = DomainFactory.INSTANCE;
        domainObjectFactory = DomainObjectFactory.INSTANCE;
        mongoObjectFactory = MongoObjectFactory.INSTANCE;
        swissTimingFactory = SwissTimingFactory.INSTANCE;
        swissTimingAdapterPersistence = SwissTimingAdapterPersistence.INSTANCE;
        windTrackerFactory = ExpeditionWindTrackerFactory.getInstance();
        eventsByName = new HashMap<String, Event>();
        eventTrackingCache = new HashMap<Event, DynamicTrackedEvent>();
        raceTrackersByEvent = new HashMap<Event, Set<RaceTracker>>();
        windTrackers = new HashMap<RaceDefinition, WindTracker>();
        raceTrackersByID = new HashMap<Object, RaceTracker>();
        leaderboardsByName = new HashMap<String, Leaderboard>();
        // Add one default leaderboard that aggregates all races currently tracked by this service.
        // This is more for debugging purposes than for anything else.
        addLeaderboard(DEFAULT_LEADERBOARD_NAME, new int[] { 5, 8 });
        loadStoredLeaderboards();
    }
    
    private void loadStoredLeaderboards() {
        for (Leaderboard leaderboard : domainObjectFactory.getAllLeaderboards()) {
            leaderboardsByName.put(leaderboard.getName(), leaderboard);
        }
    }
    
    @Override
    public Leaderboard addLeaderboard(String name, int[] discardThresholds) {
        Leaderboard result = new LeaderboardImpl(name, new ScoreCorrectionImpl(), new ResultDiscardingRuleImpl(
                discardThresholds));
        synchronized (leaderboardsByName) {
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
        // TODO this is very brute force; consider updating more fine-grained
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
        return domainFactory;
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
    public synchronized RaceHandle addSwissTimingRace(String raceID, String hostname, int port, WindStore windStore,
            long timeoutInMilliseconds) throws InterruptedException, UnknownHostException, IOException {
        Triple<String, String, Integer> key = new Triple<String, String, Integer>(raceID, hostname, port);
        RaceTracker tracker = raceTrackersByID.get(key);
        if (tracker == null) {
            tracker = getSwissTimingFactory().createRaceTracker(raceID, hostname, port, windStore, swissTimingAdapterPersistence, this);
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
        ensureEventIsObservedForDefaultLeaderboard(trackedEvent);
        if (timeoutInMilliseconds != -1) {
            scheduleAbortTrackerAfterInitialTimeout(tracker, timeoutInMilliseconds);
        }
        return tracker.getRaceHandle();
    }

    @Override
    public synchronized RaceHandle addTracTracRace(URL paramURL, URI liveURI, URI storedURI, WindStore windStore,
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
        ensureEventIsObservedForDefaultLeaderboard(trackedEvent);
        if (timeoutInMilliseconds != -1) {
            scheduleAbortTrackerAfterInitialTimeout(tracker, timeoutInMilliseconds);
        }
        return tracker.getRaceHandle();
    }

    private void ensureEventIsObservedForDefaultLeaderboard(DynamicTrackedEvent trackedEvent) {
        synchronized (eventsObservedForDefaultLeaderboard) {
            if (!eventsObservedForDefaultLeaderboard.contains(trackedEvent)) {
                trackedEvent.addRaceListener(new RaceListener() {
                    @Override
                    public void raceRemoved(TrackedRace trackedRace) {
                    }

                    @Override
                    public void raceAdded(TrackedRace trackedRace) {
                        leaderboardsByName.get(DEFAULT_LEADERBOARD_NAME).addRace(trackedRace,
                                trackedRace.getRace().getName(), /* medalRace */ false);
                    }
                });
                eventsObservedForDefaultLeaderboard.add(trackedEvent);
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
        if (event != null) {
            if (event.getName() != null) {
                eventsByName.remove(event.getName());
            }
            for (RaceDefinition race : event.getAllRaces()) {
                stopTrackingWind(event, race);
                // remove from default leaderboard
                Leaderboard defaultLeaderboard = getLeaderboardByName(DEFAULT_LEADERBOARD_NAME);
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
                    System.out.println("Found tracker to stop...");
                    raceTracker.stop(); // this also removes the TrackedRace from trackedEvent
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
    public void remove(Event event) {
        eventTrackingCache.remove(event);
    }
    
}
