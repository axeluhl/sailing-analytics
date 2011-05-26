package com.sap.sailing.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.tractracadapter.DomainFactory;

public interface RacingEventService {
    Iterable<Event> getAllEvents();

    Event getEventByName(String name);

    DomainFactory getDomainFactory();
    
    void addEvent(URL paramURL, URI liveURI, URI storedURI) throws MalformedURLException, FileNotFoundException,
            URISyntaxException;

    void stopTracking(Event event) throws MalformedURLException, IOException, InterruptedException;
    
    /**
     * @param port
     *            the UDP port on which to listen for incoming messages from Expedition clients
     * @throws SocketException
     *             thrown, e.g., in case there is already another listener on the port requested
     */
    void startTrackingWind(Event event, RaceDefinition race, int port) throws SocketException;

    void stopTrackingWind(Event event, RaceDefinition race) throws SocketException, IOException;

}
