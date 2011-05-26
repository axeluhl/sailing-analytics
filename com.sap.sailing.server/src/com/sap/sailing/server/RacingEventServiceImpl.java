package com.sap.sailing.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.tracking.DynamicTrackedEvent;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.EventTracker;
import com.sap.sailing.expeditionconnector.UDPExpeditionReceiver;
import com.sap.sailing.expeditionconnector.WindTracker;

public class RacingEventServiceImpl implements RacingEventService {
    private final DomainFactory domainFactory;
    
    private final Map<String, Event> eventsByName;
    
    private final Map<Event, EventTracker> eventTrackers;
    
    private final Map<RaceDefinition, WindTracker> windTrackers;
    
    private final Map<Integer, UDPExpeditionReceiver> windReceivers;

    public RacingEventServiceImpl() {
        domainFactory = DomainFactory.INSTANCE;
        eventsByName = new HashMap<String, Event>();
        eventTrackers = new HashMap<Event, EventTracker>();
        windTrackers = new HashMap<RaceDefinition, WindTracker>();
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
    public void addEvent(URL paramURL, URI liveURI, URI storedURI) throws MalformedURLException, FileNotFoundException,
            URISyntaxException {
        EventTracker tracker = getDomainFactory().createEventTracker(paramURL, liveURI, storedURI);
        eventTrackers.put(tracker.getEvent(), tracker);
        eventsByName.put(tracker.getEvent().getName(), tracker.getEvent());
    }

    @Override
    public void stopTracking(Event event) throws MalformedURLException, IOException, InterruptedException {
        eventTrackers.get(event).stop();
        eventTrackers.remove(event);
        eventsByName.remove(event.getName());
    }

    @Override
    public synchronized void startTrackingWind(Event event, RaceDefinition race, int port) throws SocketException {
        if (!windTrackers.containsKey(race)) {
            DynamicTrackedEvent trackedEvent = getDomainFactory().trackEvent(event);
            DynamicTrackedRace trackedRace = trackedEvent.getTrackedRace(race);
            WindTracker windTracker = new WindTracker(trackedRace);
            UDPExpeditionReceiver receiver = getOrCreateWindReceiverForPort(port);
            windTrackers.put(race, windTracker);
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
        WindTracker windTracker = windTrackers.get(race);
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
