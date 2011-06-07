package com.sap.sailing.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.RaceTracker;
import com.sap.sailing.util.Util.Pair;

public interface RacingEventService {
    Iterable<Event> getAllEvents();

    Event getEventByName(String name);

    DomainFactory getDomainFactory();
    
    /**
     * Defines the event and for each race listed in the JSON document creates a {@link RaceTracker} that
     * tracks the respective race. The {@link RaceDefinition}s obtained this way are all grouped into
     * the single {@link Event} produced for the event listed in the JSON response. Note that the
     * many race trackers will have their TracTrac <code>Event</code> each, all with the same name,
     * meaning the same event but being distinct.
     * 
     * @param jsonURL
     *            URL of a JSON response that contains an "event" object telling the event's name and ID, as well as a
     *            JSON array named "races" which tells ID and replay URL for the race. From those replay URLs the
     *            paramURL for the Java client can be derived.
     */
    void addEvent(URL jsonURL, URI liveURI, URI storedURI) throws MalformedURLException, FileNotFoundException,
            URISyntaxException, IOException, ParseException, org.json.simple.parser.ParseException;

    /**
     * Adds a single race tracker, using the race's parameter URL which delivers the single configuration
     * text file for that race. While the result of passing this URL to the TracTrac <code>KeyValue.setup</code>
     * is a TracTrac <code>Event</code>, those events only manage a single race. In our domain model, we group
     * those races into a single instance of our {@link Event} class.<p>
     * 
     * If this is the first race of an event, the {@link Event} is created as well. If the {@link RaceDefinition} for
     * the race already exists, it isn't created again. Also, if a {@link RaceTracker} for the given race already
     * exists, it is not added again.
     */
    void addRace(URL paramURL, URI liveURI, URI storedURI) throws MalformedURLException, FileNotFoundException,
            URISyntaxException;

    /**
     * Stops tracking all races of the event specified. This will also stop tracking wind for all races of this event.
     * See {@link #stopTrackingWind(Event, RaceDefinition)}.
     */
    void stopTracking(Event event) throws MalformedURLException, IOException, InterruptedException;
    
    /**
     * Stops tracking a single race. Other races of the same event that are currently tracked will continue to be
     * tracked.
     */
    void stopTracking(Event event, RaceDefinition race) throws MalformedURLException, IOException, InterruptedException;

    /**
     * @param port
     *            the UDP port on which to listen for incoming messages from Expedition clients
     * @throws SocketException
     *             thrown, e.g., in case there is already another listener on the port requested
     */
    void startTrackingWind(Event event, RaceDefinition race, int port) throws SocketException;

    void stopTrackingWind(Event event, RaceDefinition race) throws SocketException, IOException;

    Iterable<Pair<Event, RaceDefinition>> getWindTrackedRaces();

}
