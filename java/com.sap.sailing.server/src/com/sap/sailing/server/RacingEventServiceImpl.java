package com.sap.sailing.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.sap.sailing.declination.DeclinationService;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.tracking.DynamicTrackedEvent;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.JSONService;
import com.sap.sailing.domain.tractracadapter.RaceHandle;
import com.sap.sailing.domain.tractracadapter.RaceRecord;
import com.sap.sailing.domain.tractracadapter.RaceTracker;
import com.sap.sailing.expeditionconnector.UDPExpeditionReceiver;
import com.sap.sailing.expeditionconnector.WindTracker;
import com.sap.sailing.util.Util.Pair;
import com.sap.sailing.util.Util.Triple;

public class RacingEventServiceImpl implements RacingEventService {
    private static final Logger logger = Logger.getLogger(RacingEventServiceImpl.class.getName());
    
    private final DomainFactory domainFactory;
    
    private final Map<String, Event> eventsByName;
    
    private final Map<Event, Set<RaceTracker>> raceTrackersByEvent;
    
    /**
     * Remembers the wind tracker and the port on which the UDP receiver with which the wind tracker is
     * registers is listening for incoming Expedition messages.
     */
    private final Map<RaceDefinition, Pair<WindTracker, Integer>> windTrackers;
    
    /**
     * Remembers the trackers by paramURL/liveURI/storedURI to avoid duplication
     */
    private final Map<Triple<URL, URI, URI>, RaceTracker> raceTrackersByURLs;
    
    private final Map<Integer, UDPExpeditionReceiver> windReceivers;

    public RacingEventServiceImpl() {
        domainFactory = DomainFactory.INSTANCE;
        eventsByName = new HashMap<String, Event>();
        raceTrackersByEvent = new HashMap<Event, Set<RaceTracker>>();
        windTrackers = new HashMap<RaceDefinition, Pair<WindTracker, Integer>>();
        raceTrackersByURLs = new HashMap<Triple<URL, URI, URI>, RaceTracker>();
        windReceivers = new HashMap<Integer, UDPExpeditionReceiver>();
    }
    
    @Override
    public DomainFactory getDomainFactory() {
        return domainFactory;
    }

    @Override
    public Iterable<Event> getAllEvents() {
        return Collections.unmodifiableCollection(eventsByName.values());
    }

    @Override
    public Event getEventByName(String name) {
        return eventsByName.get(name);
    }

    @Override
    public synchronized Event addEvent(URL jsonURL, URI liveURI, URI storedURI, WindStore windStore) throws URISyntaxException, IOException, ParseException, org.json.simple.parser.ParseException {
        JSONService jsonService = getDomainFactory().parseJSONURL(jsonURL);
        Event event = null;
        for (RaceRecord rr : jsonService.getRaceRecords()) {
            URL paramURL = rr.getParamURL();
            event = addRace(paramURL, liveURI, storedURI, windStore).getEvent();
        }
        return event;
    }

    @Override
    public List<RaceRecord> getRaceRecords(URL jsonURL) throws IOException, ParseException, org.json.simple.parser.ParseException {
        JSONService jsonService = getDomainFactory().parseJSONURL(jsonURL);
        return jsonService.getRaceRecords();
    }

    @Override
    public synchronized RaceHandle addRace(URL paramURL, URI liveURI, URI storedURI, WindStore windStore) throws MalformedURLException, FileNotFoundException,
            URISyntaxException {
        Triple<URL, URI, URI> key = new Triple<URL, URI, URI>(paramURL, liveURI, storedURI);
        RaceTracker tracker = raceTrackersByURLs.get(key);
        if (tracker == null) {
            tracker = getDomainFactory().createRaceTracker(paramURL, liveURI, storedURI, windStore);
            raceTrackersByURLs.put(key, tracker);
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
        return tracker.getRaceHandle();
    }

    @Override
    public synchronized void stopTracking(Event event) throws MalformedURLException, IOException, InterruptedException {
        if (raceTrackersByEvent.containsKey(event)) {
            for (RaceTracker raceTracker : raceTrackersByEvent.get(event)) {
                raceTracker.stop(); // this also removes the TrackedRace from trackedEvent
                raceTrackersByURLs.remove(raceTracker.getURLs());
            }
            raceTrackersByEvent.remove(event);
        }
        if (event != null) {
            if (event.getName() != null) {
                eventsByName.remove(event.getName());
            }
            for (RaceDefinition race : event.getAllRaces()) {
                stopTrackingWind(event, race);
            }
        }
    }

    @Override
    public synchronized void stopTracking(Event event, RaceDefinition race) throws MalformedURLException, IOException, InterruptedException {
        logger.info("Stopping tracking for "+race+"...");
        if (raceTrackersByEvent.containsKey(event)) {
            Iterator<RaceTracker> trackerIter = raceTrackersByEvent.get(event).iterator();
            while (trackerIter.hasNext()) {
                RaceTracker raceTracker = trackerIter.next();
                if (raceTracker.getRace() == race) {
                    System.out.println("Found tracker to stop...");
                    raceTracker.stop(); // this also removes the TrackedRace from trackedEvent
                    trackerIter.remove();
                    raceTrackersByURLs.remove(raceTracker.getURLs());
                }
            }
        } else {
            logger.warning("Didn't find any trackers for event "+event);
        }
        // if the last tracked race was removed, remove the entire event
        if (raceTrackersByEvent.get(event).isEmpty()) {
            stopTracking(event);
        }
    }

    @Override
    public synchronized void startTrackingWind(Event event, RaceDefinition race, int port,
            DeclinationService declinationService) throws SocketException {
        if (!windTrackers.containsKey(race)) {
            DynamicTrackedEvent trackedEvent = getDomainFactory().getOrCreateTrackedEvent(event);
            DynamicTrackedRace trackedRace = trackedEvent.getTrackedRace(race);
            WindTracker windTracker = new WindTracker(trackedRace, declinationService);
            UDPExpeditionReceiver receiver = getOrCreateWindReceiverForPort(port);
            windTrackers.put(race, new Pair<WindTracker, Integer>(windTracker, port));
            receiver.addListener(windTracker, /* validMessagesOnly */ true);
        }
    }

    private synchronized UDPExpeditionReceiver getOrCreateWindReceiverForPort(int port) throws SocketException {
        UDPExpeditionReceiver receiver = windReceivers.get(port);
        if (receiver == null) {
            receiver = new UDPExpeditionReceiver(port);
            windReceivers.put(port, receiver);
            new Thread(receiver, "Expedition Wind Receiver on port "+port).start();
        }
        return receiver;
    }

    @Override
    public synchronized void stopTrackingWind(Event event, RaceDefinition race) throws SocketException, IOException {
        Pair<WindTracker, Integer> windTrackerPair = windTrackers.get(race);
        if (windTrackerPair != null) {
            WindTracker windTracker = windTrackerPair.getA();
            if (windTracker != null) {
                for (UDPExpeditionReceiver receiver : windReceivers.values()) {
                    receiver.removeListener(windTracker);
                }
            }
            windTrackers.remove(race);
            // if there is no more tracker we can also stop and remove the receiver(s) we created
            if (windTrackers.isEmpty()) {
                for (UDPExpeditionReceiver receiver : windReceivers.values()) {
                    receiver.stop();
                }
                windReceivers.clear();
            }
        }
    }

    @Override
    public synchronized Iterable<Triple<Event, RaceDefinition, Integer>> getWindTrackedRaces() {
        List<Triple<Event, RaceDefinition, Integer>> result = new ArrayList<Triple<Event, RaceDefinition, Integer>>();
        for (Event event : eventsByName.values()) {
            for (RaceDefinition race : event.getAllRaces()) {
                if (windTrackers.containsKey(race)) {
                    result.add(new Triple<Event, RaceDefinition, Integer>(event, race, windTrackers.get(race).getB()));
                }
            }
        }
        return result;
    }
    
}
