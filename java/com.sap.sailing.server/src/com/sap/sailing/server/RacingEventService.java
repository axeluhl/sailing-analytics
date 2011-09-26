package com.sap.sailing.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.tracking.TrackedEvent;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.RaceHandle;
import com.sap.sailing.domain.tractracadapter.RaceRecord;
import com.sap.sailing.domain.tractracadapter.RaceTracker;
import com.sap.sailing.util.Util.Triple;

/**
 * An OSGi service that can be used to track boat races using a TracTrac connector that pushes
 * live GPS boat location, waypoint, coarse and mark passing data.<p>
 * 
 * If a race/event is already being tracked, another {@link #addRace(URL, URI, URI, WindStore)} or
 * {@link #addEvent(URL, URI, URI, WindStore)} call will have no effect, even if a different
 * {@link WindStore} is requested.<p>
 * 
 * TODO When the tracking of a race/event is {@link #stopTracking(Event, RaceDefinition) stopped}, the next
 * time it's started to be tracked, a new {@link TrackedRace} at least will be constructed. This also
 * means that when a {@link TrackedEvent} exists that still holds other {@link TrackedRace}s, the
 * no longer tracked {@link TrackedRace} will be removed from the {@link TrackedEvent}.
 * corresponding information is removed also from the {@link DomainFactory}'s caches to ensure that
 * clean, fresh data is received should another tracking request be issued later.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface RacingEventService {
    Iterable<Event> getAllEvents();

    Event getEventByName(String name);

    DomainFactory getDomainFactory();
    
    /**
     * Defines the event and for each race listed in the JSON document that is not already being tracked by this service
     * creates a {@link RaceTracker} that starts tracking the respective race. The {@link RaceDefinition}s obtained this
     * way are all grouped into the single {@link Event} produced for the event listed in the JSON response. Note that
     * the many race trackers will have their TracTrac <code>Event</code> each, all with the same name, meaning the same
     * event but being distinct.
     * 
     * @param jsonURL
     *            URL of a JSON response that contains an "event" object telling the event's name and ID, as well as a
     *            JSON array named "races" which tells ID and replay URL for the race. From those replay URLs the
     *            paramURL for the Java client can be derived.
     */
    Event addEvent(URL jsonURL, URI liveURI, URI storedURI, WindStore windStore) throws MalformedURLException, FileNotFoundException,
            URISyntaxException, IOException, ParseException, org.json.simple.parser.ParseException;

    /**
     * If not already tracking the URL/URI/URI combination, adds a single race tracker and starts tracking the race,
     * using the race's parameter URL which delivers the single configuration text file for that race. While the result
     * of passing this URL to the TracTrac <code>KeyValue.setup</code> is a TracTrac <code>Event</code>, those events
     * only manage a single race. In our domain model, we group those races into a single instance of our {@link Event}
     * class.
     * <p>
     * 
     * If this is the first race of an event, the {@link Event} is created as well. If the {@link RaceDefinition} for
     * the race already exists, it isn't created again. Also, if a {@link RaceTracker} for the given race already
     * exists, it is not added again.<p>
     * 
     * Note that when the race identified by <code>paramURL</code>, <code>liveURI</code> and <code>storedURI</code> is
     * already being tracked, then regardless of the <code>windStore</code> selection the existing tracker will be used
     * and its race handle will be returned. A log message will indicate a potential wind store mismatch (based on
     * {@link WindStore#equals(Object)}).
     */
    RaceHandle addRace(URL paramURL, URI liveURI, URI storedURI, WindStore windStore) throws MalformedURLException, FileNotFoundException,
            URISyntaxException;

    /**
     * Stops tracking all races of the event specified. This will also stop tracking wind for all races of this event.
     * See {@link #stopTrackingWind(Event, RaceDefinition)}. If there were multiple calls to
     * {@link #addRace(URL, URI, URI, WindStore)} with an equal combination of URLs/URIs, the {@link RaceTracker}
     * already tracking the race was re-used. The trackers will be stopped by this call regardless of how many calls
     * were made that ensured they were tracking.
     */
    void stopTracking(Event event) throws MalformedURLException, IOException, InterruptedException;
    
    /**
     * Stops tracking a single race. Other races of the same event that are currently tracked will continue to be
     * tracked. If wind tracking for the race is currently running, it will be stopped (see also
     * {@link #stopTrackingWind(Event, RaceDefinition)}).
     */
    void stopTracking(Event event, RaceDefinition race) throws MalformedURLException, IOException, InterruptedException;

    /**
     * @param port
     *            the UDP port on which to listen for incoming messages from Expedition clients
     * @param correctByDeclination
     *            An optional service to convert the wind bearings (which the receiver may
     *            believe to be true bearings) from magnetic to true bearings.
     * @throws SocketException
     *             thrown, e.g., in case there is already another listener on the port requested
     */
    void startTrackingWind(Event event, RaceDefinition race, boolean correctByDeclination) throws SocketException;

    void stopTrackingWind(Event event, RaceDefinition race) throws SocketException, IOException;

    /**
     * The {@link Triple#getC() third component} of the triples returned is a wind tracker-specific
     * comment where a wind tracker may provide information such as its type name or, if applicable,
     * connectivity information such as the network port on which it receives wind information.
     */
    Iterable<Triple<Event, RaceDefinition, String>> getWindTrackedRaces();

    /**
     * For the JSON URL of an account / event, lists the paramURLs that can be used for {@link #addRace(URL, URI, URI, WindStore)}
     * calls to individually start tracking races of this event, rather than tracking <em>all</em> races in the event which
     * is hardly ever useful.
     */
    List<RaceRecord> getRaceRecords(URL jsonURL) throws IOException, ParseException, org.json.simple.parser.ParseException, URISyntaxException;

    boolean isRaceBeingTracked(RaceDefinition r);

    TrackedRace getTrackedRace(Event event, RaceDefinition r);

    /**
     * Creates a new leaderboard with the <code>name</code> specified.
     * 
     * @param discardThresholds
     *            Tells the thresholds from which on a next higher number of worst races will be discarded per
     *            competitor. Example: <code>[3, 6]</code> means that starting from three races the single worst race
     *            will be discarded; starting from six races, the two worst races per competitor are discarded.
     * 
     * @return the leaderboard created
     */
    Leaderboard addLeaderboard(String name, int[] discardThresholds);

    void removeLeaderboard(String leaderboardName);
    
    Leaderboard getLeaderboardByName(String name);

    /**
     * Obtains an unmodifiable map of the leaderboard configured in this service keyed by their names.
     */
    Map<String, Leaderboard> getLeaderboards();

    /**
     * Renames a leaderboard. If a leaderboard by the name <code>oldName</code> does not exist in {@link #getLeaderboards()},
     * or if a leaderboard with the name <code>newName</code> already exists, an {@link IllegalArgumentException} is thrown.
     * If the method completes normally, the rename has been successful, and the leaderboard previously obtained by calling
     * {@link #getLeaderboardByName(String) getLeaderboardByName(oldName)} can now be obtained by calling
     * {@link #getLeaderboardByName(String) getLeaderboardByName(newName)}.
     */
    void renameLeaderboard(String oldName, String newName);

    void updateStoredLeaderboard(Leaderboard leaderboard);

}
