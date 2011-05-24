package com.sap.sailing.server;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.tractracadapter.DomainFactory;

public interface RacingEventService {
    Iterable<Event> getAllEvents();

    Event getEventByName(String name);

    DomainFactory getDomainFactory();
    
    void addEvent(URL paramURL, URI liveURI, URI storedURI) throws MalformedURLException, FileNotFoundException,
            URISyntaxException;
}
