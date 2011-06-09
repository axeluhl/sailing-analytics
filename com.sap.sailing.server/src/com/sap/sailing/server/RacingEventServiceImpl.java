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

import com.sap.sailing.declination.DeclinationService;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.tracking.DynamicTrackedEvent;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.JSONService;
import com.sap.sailing.domain.tractracadapter.RaceRecord;
import com.sap.sailing.domain.tractracadapter.RaceTracker;
import com.sap.sailing.expeditionconnector.UDPExpeditionReceiver;
import com.sap.sailing.expeditionconnector.WindTracker;
import com.sap.sailing.util.Util.Pair;
import com.sap.sailing.util.Util.Triple;

public class RacingEventServiceImpl implements RacingEventService {
    private final DomainFactory domainFactory;
    
    private final Map<String, Event> eventsByName;
    
    private final Map<Event, Set<RaceTracker>> raceTrackers;
    
    /**
     * Remembers the wind tracker and the port on which the UDP receiver with which the wind tracker is
     * registers is listening for incoming Expedition messages.
     */
    private final Map<RaceDefinition, Pair<WindTracker, Integer>> windTrackers;
    
    private final Map<Integer, UDPExpeditionReceiver> windReceivers;

    public RacingEventServiceImpl() {
        domainFactory = DomainFactory.INSTANCE;
        eventsByName = new HashMap<String, Event>();
        raceTrackers = new HashMap<Event, Set<RaceTracker>>();
        windTrackers = new HashMap<RaceDefinition, Pair<WindTracker, Integer>>();
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
    public void addEvent(URL jsonURL, URI liveURI, URI storedURI) throws URISyntaxException, IOException, ParseException, org.json.simple.parser.ParseException {
        JSONService jsonService = DomainFactory.INSTANCE.parseJSONURL(jsonURL);
        for (RaceRecord rr : jsonService.getRaceRecords()) {
            URL paramURL = rr.getParamURL();
            addRace(paramURL, liveURI, storedURI);
        }
    }

    @Override
    public void addRace(URL paramURL, URI liveURI, URI storedURI) throws MalformedURLException, FileNotFoundException,
            URISyntaxException {
        RaceTracker tracker = getDomainFactory().createRaceTracker(paramURL, liveURI, storedURI);
        Set<RaceTracker> trackers = raceTrackers.get(tracker.getEvent());
        if (trackers == null) {
            trackers = new HashSet<RaceTracker>();
            raceTrackers.put(tracker.getEvent(), trackers);
        }
        trackers.add(tracker);
        String eventName = tracker.getEvent().getName();
        Event eventWithName = eventsByName.get(eventName);
        if (eventWithName != null) {
            if (eventWithName != tracker.getEvent()) {
                throw new RuntimeException("Internal error. Two Event objects with equal name "+eventName);
            }
        } else {
            eventsByName.put(eventName, tracker.getEvent());
        }
    }

    @Override
    public void stopTracking(Event event) throws MalformedURLException, IOException, InterruptedException {
        if (raceTrackers.containsKey(event)) {
            for (RaceTracker raceTracker : raceTrackers.get(event)) {
                raceTracker.stop();
            }
            raceTrackers.remove(event);
        }
        if (event != null && event.getName() != null) {
            eventsByName.remove(event.getName());
        }
        for (RaceDefinition race : event.getAllRaces()) {
            stopTrackingWind(event, race);
        }
    }

    @Override
    public void stopTracking(Event event, RaceDefinition race) throws MalformedURLException, IOException, InterruptedException {
        if (raceTrackers.containsKey(event)) {
            Iterator<RaceTracker> trackerIter = raceTrackers.get(event).iterator();
            while (trackerIter.hasNext()) {
                RaceTracker raceTracker = trackerIter.next();
                if (raceTracker.getRace() == race) {
                    raceTracker.stop();
                    trackerIter.remove();
                }
            }
        }
        // if the last tracked race was removed, remove the entire event
        if (raceTrackers.get(event).isEmpty()) {
            stopTracking(event);
        }
    }

    @Override
    public synchronized void startTrackingWind(Event event, RaceDefinition race, int port,
            DeclinationService declinationService) throws SocketException {
        if (!windTrackers.containsKey(race)) {
            DynamicTrackedEvent trackedEvent = getDomainFactory().trackEvent(event);
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
        WindTracker windTracker = windTrackers.get(race).getA();
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

    @Override
    public Iterable<Triple<Event, RaceDefinition, Integer>> getWindTrackedRaces() {
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
