package com.sap.sailing.server;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.EventTracker;

public class RacingEventServiceImpl implements RacingEventService {
    private final DomainFactory domainFactory;
    
    private final Map<String, Event> eventsByName;
    
    private final Map<Event, EventTracker> eventTrackers;

    public RacingEventServiceImpl() {
        domainFactory = DomainFactory.INSTANCE;
        eventsByName = new HashMap<String, Event>();
        eventTrackers = new HashMap<Event, EventTracker>();
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
    
}
