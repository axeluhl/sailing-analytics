package com.sap.sailing.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.RegattaRegistry;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.common.RaceFetcher;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaFetcher;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.LeaderboardRegistry;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingFactory;
import com.sap.sailing.domain.tracking.RaceListener;
import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sailing.domain.tracking.RaceTrackingConnectivityParameters;
import com.sap.sailing.domain.tracking.RacesHandle;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.RaceRecord;
import com.sap.sailing.domain.tractracadapter.TracTracRaceTracker;
import com.sap.sailing.expeditionconnector.ExpeditionListener;

/**
 * An OSGi service that can be used to track boat races using a TracTrac connector that pushes
 * live GPS boat location, waypoint, coarse and mark passing data.<p>
 * 
 * If a race/regatta is already being tracked, another {@link #addTracTracRace(URL, URI, URI, WindStore, long)} or
 * {@link #addRegatta(URL, URI, URI, WindStore, long)} call will have no effect, even if a different
 * {@link WindStore} is requested.<p>
 * 
 * When the tracking of a race/regatta is {@link #stopTracking(Regatta, RaceDefinition) stopped}, the next
 * time it's started to be tracked, a new {@link TrackedRace} at least will be constructed. This also
 * means that when a {@link TrackedRegatta} exists that still holds other {@link TrackedRace}s, the
 * no longer tracked {@link TrackedRace} will be removed from the {@link TrackedRegatta}.
 * corresponding information is removed also from the {@link DomainFactory}'s caches to ensure that
 * clean, fresh data is received should another tracking request be issued later.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface RacingEventService extends TrackedRegattaRegistry, RegattaFetcher, RegattaRegistry, RaceFetcher, LeaderboardRegistry {
    @Override
    Regatta getRegatta(RegattaName regattaName);
    
    @Override
    RaceDefinition getRace(RegattaAndRaceIdentifier raceIdentifier);

    TrackedRace getTrackedRace(Regatta regatta, RaceDefinition race);
    
    TrackedRace getTrackedRace(RegattaAndRaceIdentifier raceIdentifier);

    /**
     * Obtains an unmodifiable map of the leaderboard configured in this service keyed by their names.
     */
    Map<String, Leaderboard> getLeaderboards();

    /**
     * @return a leaderboard whose {@link Leaderboard#getName()} method returns the value of the <code>name</code>
     *         parameter, or <code>null</code> if no such leaderboard is known to this service
     */
    Leaderboard getLeaderboardByName(String name);

    /**
     * Defines the regatta and for each race listed in the JSON document that is not already being tracked by this service
     * creates a {@link TracTracRaceTracker} that starts tracking the respective race. The {@link RaceDefinition}s obtained this
     * way are all grouped into the single {@link Regatta} produced for the event listed in the JSON response. Note that
     * the many race trackers will have their TracTrac <code>Event</code> each, all with the same name, meaning the same
     * event but being distinct.
     * 
     * @param jsonURL
     *            URL of a JSON response that contains an "event" object telling the event's name and ID, as well as a
     *            JSON array named "races" which tells ID and replay URL for the race. From those replay URLs the
     *            paramURL for the Java client can be derived.
     * @param timeoutInMilliseconds
     *            if a race definition is not received for a race of this event within this time, the race tracker for
     *            that race is stopped; use -1 to wait forever
     */
    Regatta addRegatta(URL jsonURL, URI liveURI, URI storedURI, WindStore windStore, long timeoutInMilliseconds)
            throws MalformedURLException, FileNotFoundException, URISyntaxException, IOException, ParseException,
            org.json.simple.parser.ParseException, Exception;

    /**
     * If not already tracking the URL/URI/URI combination, adds a single race tracker and starts tracking the race,
     * using the race's parameter URL which delivers the single configuration text file for that race. While the result
     * of passing this URL to the TracTrac <code>KeyValue.setup</code> is a TracTrac <code>Event</code>, those events
     * only manage a single race. In our domain model, we group those races into a single instance of our {@link Regatta}
     * class.
     * <p>
     * 
     * If this is the first race of an event, the {@link Regatta} is created as well. If the {@link RaceDefinition} for
     * the race already exists, it isn't created again. Also, if a {@link TracTracRaceTracker} for the given race already
     * exists, it is not added again.
     * <p>
     * 
     * Note that when the race identified by <code>paramURL</code>, <code>liveURI</code> and <code>storedURI</code> is
     * already being tracked, then regardless of the <code>windStore</code> selection the existing tracker will be used
     * and its race handle will be returned. A log message will indicate a potential wind store mismatch (based on
     * {@link WindStore#equals(Object)}).
     * 
     * @param timeoutInMilliseconds
     *            if the race definition is not received for the race within this time, the race tracker for
     *            that race is stopped; use -1 to wait forever
     */
    RacesHandle addTracTracRace(URL paramURL, URI liveURI, URI storedURI, WindStore windStore, long timeoutInMilliseconds)
            throws MalformedURLException, FileNotFoundException, URISyntaxException, Exception;

    /**
     * Same as {@link #addTracTracRace(URL, URI, URI, WindStore, long)}, only that start and end of tracking are
     * specified which may help reducing the amount of stored data (particularly mark positions) that needs to be
     * loaded.
     * 
     * @param regattaToAddTo
     *            if <code>null</code>, an existing regatta by the name of the TracTrac event with the boat class name
     *            appended in parentheses will be looked up; if not found, a default regatta with that name will be
     *            created, with a single default series and a single default fleet. If a valid {@link RegattaIdentifier}
     *            is specified, a regatta lookup is performed with that identifier; if the regatta is found, it is used
     *            to add the races to. Otherwise, a default regatta as described above will be created and used.
     */
    RacesHandle addTracTracRace(RegattaIdentifier regattaToAddTo, URL paramURL, URI liveURI, URI storedURI,
            TimePoint trackingStartTime, TimePoint trackingEndTime, WindStore windStore,
            long timeoutForReceivingRaceDefinitionInMilliseconds, boolean simulateWithStartTimeNow)
            throws MalformedURLException, FileNotFoundException, URISyntaxException, Exception;

    /**
     * Stops tracking all races of the regatta specified. This will also stop tracking wind for all races of this regatta.
     * See {@link #stopTrackingWind(Regatta, RaceDefinition)}. If there were multiple calls to
     * {@link #addTracTracRace(URL, URI, URI, WindStore, long)} with an equal combination of URLs/URIs, the {@link TracTracRaceTracker}
     * already tracking the race was re-used. The trackers will be stopped by this call regardless of how many calls
     * were made that ensured they were tracking.
     */
    void stopTracking(Regatta regatta) throws MalformedURLException, IOException, InterruptedException;
    
    /**
     * Removes <code>race</code> and any corresponding {@link #getTrackedRace(Regatta, RaceDefinition) tracked race}
     * from this service. If it was the last {@link RaceDefinition} in its {@link Regatta} and the regatta
     * {@link Regatta#isPersistent() is not stored persistently}, the <code>regatta</code> is removed as well and will no
     * longer be returned by {@link #getAllRegattas()}. The wind tracking is stopped for <code>race</code>.
     * <p>
     * 
     * Any {@link RaceTracker} for which <code>race</race> is the last race tracked that is still reachable
     * from {@link #getAllRegattas()} will be {@link RaceTracker#stop() stopped}.
     * 
     * The <code>race</code> will be also removed from all leaderboards containing a column that has <code>race</code>'s
     * {@link #getTrackedRace(Regatta, RaceDefinition) corresponding} {@link TrackedRace} as its
     * {@link RaceColumn#getTrackedRace(Fleet)}.
     * 
     * @param regatta
     *            the regatta from which to remove the race
     * @param race
     *            the race to remove
     */
    void removeRace(Regatta regatta, RaceDefinition race) throws MalformedURLException, IOException,InterruptedException;
    
    /**
     * Stops all {@link RaceTracker}s currently tracking <code>race</code>. Note that if the same tracker also may have
     * been tracking other races. Other races of the same event that are currently tracked will continue to be tracked.
     * If wind tracking for the race is currently running, it will be stopped (see also
     * {@link #stopTrackingWind(Regatta, RaceDefinition)}). The <code>race</code> (and the other races tracked by the
     * same tracker) as well as the corresponding {@link TrackedRace}s will continue to exist, e.g., when asking
     * {@link #getTrackedRace(Regatta, RaceDefinition)}.
     */
    void stopTracking(Regatta regatta, RaceDefinition race) throws MalformedURLException, IOException, InterruptedException;

    /**
     * @param port
     *            the UDP port on which to listen for incoming messages from Expedition clients
     * @param correctByDeclination
     *            An optional service to convert the wind bearings (which the receiver may
     *            believe to be true bearings) from magnetic to true bearings.
     * @throws SocketException
     *             thrown, e.g., in case there is already another listener on the port requested
     */
    void startTrackingWind(Regatta regatta, RaceDefinition race, boolean correctByDeclination) throws SocketException;

    void stopTrackingWind(Regatta regatta, RaceDefinition race) throws SocketException, IOException;

    /**
     * The {@link Triple#getC() third component} of the triples returned is a wind tracker-specific
     * comment where a wind tracker may provide information such as its type name or, if applicable,
     * connectivity information such as the network port on which it receives wind information.
     */
    Iterable<Triple<Regatta, RaceDefinition, String>> getWindTrackedRaces();

    /**
     * For the JSON URL of an account / event, lists the paramURLs that can be used for {@link #addTracTracRace(URL, URI, URI, WindStore, long)}
     * calls to individually start tracking races of this event, rather than tracking <em>all</em> races in the event which
     * is hardly ever useful. The returned pair's first component is the event name.
     */
    Pair<String, List<RaceRecord>> getTracTracRaceRecords(URL jsonURL) throws IOException, ParseException,
            org.json.simple.parser.ParseException, URISyntaxException;
    
    List<com.sap.sailing.domain.swisstimingadapter.RaceRecord> getSwissTimingRaceRecords(String hostname, int port, boolean canSendRequests)
            throws InterruptedException, UnknownHostException, IOException, ParseException;

    boolean isRaceBeingTracked(RaceDefinition r);
    
    /**
     * Creates a new leaderboard with the <code>name</code> specified.
     * @param discardThresholds
     *            Tells the thresholds from which on a next higher number of worst races will be discarded per
     *            competitor. Example: <code>[3, 6]</code> means that starting from three races the single worst race
     *            will be discarded; starting from six races, the two worst races per competitor are discarded.
     * 
     * @return the leaderboard created
     */
    FlexibleLeaderboard addFlexibleLeaderboard(String name, int[] discardThresholds, ScoringScheme scoringScheme);
    
    RegattaLeaderboard addRegattaLeaderboard(RegattaIdentifier regattaIdentifier, int[] discardThresholds);

    void removeLeaderboard(String leaderboardName);
    
    /**
     * Renames a leaderboard. If a leaderboard by the name <code>oldName</code> does not exist in {@link #getLeaderboards()},
     * or if a leaderboard with the name <code>newName</code> already exists, an {@link IllegalArgumentException} is thrown.
     * If the method completes normally, the rename has been successful, and the leaderboard previously obtained by calling
     * {@link #getLeaderboardByName(String) getLeaderboardByName(oldName)} can now be obtained by calling
     * {@link #getLeaderboardByName(String) getLeaderboardByName(newName)}.
     */
    void renameLeaderboard(String oldName, String newName);

    RaceColumn addColumnToLeaderboard(String columnName, String leaderboardName, boolean medalRace);

    void moveLeaderboardColumnUp(String leaderboardName, String columnName);

    void moveLeaderboardColumnDown(String leaderboardName, String columnName);

    void removeLeaderboardColumn(String leaderboardName, String columnName);

    void renameLeaderboardColumn(String leaderboardName, String oldColumnName, String newColumnName);

    /**
     * Updates the leaderboard data in the persistent store
     */
    void updateStoredLeaderboard(Leaderboard leaderboard);
    
    void updateStoredRegattaLeaderboard(RegattaLeaderboard leaderboard);

    void updateStoredRegatta(Regatta regatta);

    long getDelayToLiveInMillis();

    void setDelayToLiveInMillis(long delayToLiveInMillis);

    /**
     * @param regattaToAddTo
     *            if <code>null</code>, an existing regatta by the name of the TracTrac event with the boat class name
     *            appended in parentheses will be looked up; if not found, a default regatta with that name will be
     *            created, with a single default series and a single default fleet. If a valid {@link RegattaIdentifier}
     *            is specified, a regatta lookup is performed with that identifier; if the regatta is found, it is used
     *            to add the races to. Otherwise, a default regatta as described above will be created and used.
     */
    RacesHandle addSwissTimingRace(RegattaIdentifier regattaToAddTo, String raceID, String hostname, int port,
            boolean canSendRequests, WindStore windStore, long timeoutInMilliseconds) throws InterruptedException, UnknownHostException,
            IOException, ParseException, Exception;

    SwissTimingFactory getSwissTimingFactory();
    
    void storeSwissTimingDummyRace(String racMessage, String stlMesssage, String ccgMessage) throws IllegalArgumentException;

    void stopTrackingAndRemove(Regatta regatta) throws MalformedURLException, IOException, InterruptedException;

    void removeRegatta(Regatta regatta) throws MalformedURLException, IOException, InterruptedException;

    TrackedRace getExistingTrackedRace(RegattaAndRaceIdentifier raceIdentifier);
    
    /**
     * Obtains an unmodifiable map of the leaderboard groups configured in this service keyed by their names.
     */
    Map<String, LeaderboardGroup> getLeaderboardGroups();
    
    /**
     * @param groupName The name of the requested leaderboard group
     * @return The leaderboard group with the name <code>groupName</code>, or <code>null</code> if theres no such group
     */
    LeaderboardGroup getLeaderboardGroupByName(String groupName);
    
    /**
     * Creates a new group with the name <code>groupName</code>, the description <code>desciption</code> and the
     * leaderboards with the names in <code>leaderboardNames</code> and saves it in the database.
     * 
     * @param groupName
     *            The name of the new group
     * @param description
     *            The description of the new group
     * @param leaderboardNames
     *            The names of the leaderboards, which should be contained by the new group.<br />
     *            If there isn't a leaderboard with one of these names an {@link IllegalArgumentException} is thrown.
     * @return The new leaderboard group
     */
    LeaderboardGroup addLeaderboardGroup(String groupName, String description, List<String> leaderboardNames,
            int[] overallLeaderboardDiscardThresholds, ScoringSchemeType overallLeaderboardScoringSchemeType);

    /**
     * Removes the group with the name <code>groupName</code> from the service and the database.
     * @param groupName The name of the group which shall be removed.
     */
    void removeLeaderboardGroup(String groupName);
    
    /**
     * Renames the group with the name <code>oldName</code> to the <code>newName</code>.<br />
     * If there's no group with the name <code>oldName</code> or there's already a group with the name
     * <code>newName</code> a {@link IllegalArgumentException} is thrown.
     * 
     * @param oldName The old name of the group
     * @param newName The new name of the group
     */
    void renameLeaderboardGroup(String oldName, String newName);
    
    /**
     * Updates the group data in the persistant store.
     */
    void updateStoredLeaderboardGroup(LeaderboardGroup leaderboardGroup);

    void addExpeditionListener(ExpeditionListener listener, boolean validMessagesOnly) throws SocketException;

    void removeExpeditionListener(ExpeditionListener listener);

    /**
     * @param regattaToAddTo
     *            if <code>null</code> or no regatta by that identifier is found, an existing regatta by the name of the
     *            TracTrac event with the boat class name appended in parentheses will be looked up; if not found, a
     *            default regatta with that name will be created, with a single default series and a single default
     *            fleet. If a valid {@link RegattaIdentifier} is specified, a regatta lookup is performed with that
     *            identifier; if the regatta is found, it is used to add the races to, and
     *            {@link #setRegattaForRace(Regatta, RaceDefinition)} is called to remember the association
     *            persistently. Otherwise, a default regatta as described above will be created and used.
     * @param windStore
     *            must not be <code>null</code>, but can, e.g., be an {@link EmptyWindStore}
     */
    RacesHandle addRace(RegattaIdentifier regattaToAddTo, RaceTrackingConnectivityParameters params, WindStore windStore, long timeoutInMilliseconds)
            throws MalformedURLException, FileNotFoundException, URISyntaxException, Exception;

    TrackedRace createTrackedRace(RegattaAndRaceIdentifier raceIdentifier, WindStore windStore,
            long delayToLiveInMillis, long millisecondsOverWhichToAverageWind, long millisecondsOverWhichToAverageSpeed);

    Regatta getOrCreateRegatta(String regattaName, String boatClassName);

    Regatta createRegatta(String baseName, String boatClassName, Iterable<? extends Series> series, boolean persistent, ScoringScheme scoringScheme);

    /**
     * Adds <code>raceDefinition</code> to the {@link Regatta} such that it will appear in {@link Regatta#getAllRaces()}
     * and {@link Regatta#getRaceByName(String)}.
     * 
     * @param addToRegatta identifier of an regatta that must exist already
     */
    void addRace(RegattaIdentifier addToRegatta, RaceDefinition raceDefinition);

    void updateLeaderboardGroup(String oldName, String newName, String description, List<String> leaderboardNames,
            int[] overallLeaderboardDiscardThresholds, ScoringSchemeType overallLeaderboardScoringSchemeType);

    /**
     * Executes an operation whose effects need to be replicated to any replica of this service known and
     * {@link OperationExecutionListener#executed(RacingEventServiceOperation) notifies} all registered
     * operation execution listeners about the execution of the operation.
     */
    <T> T apply(RacingEventServiceOperation<T> operation);

    void addOperationExecutionListener(OperationExecutionListener listener);
    
    void removeOperationExecutionListener(OperationExecutionListener listener);
    
    /**
     * Produces a one-shot serializable copy of those elements required for replication into <code>oos</code> so that
     * afterwards the {@link RacingEventServiceOperation}s can be {@link #apply(RacingEventServiceOperation) applied} to
     * maintain consistency with the master copy of the service. The dual operation is {@link #initiallyFillFrom}.
     */
    void serializeForInitialReplication(ObjectOutputStream oos) throws IOException;
    
    /**
     * Dual, reading operation for {@link #serializeForInitialReplication(ObjectOutputStream)}. In other words, when
     * this operation returns, this service instance is in a state "equivalent" to that of the service instance that
     * produced the stream contents in its {@link #serializeForInitialReplication(ObjectOutputStream)}. "Equivalent"
     * here means that a replica will have equal sets of regattas, tracked regattas, leaderboards and leaderboard groups but
     * will not have any active trackers for wind or positions because it relies on these elements to be sent through
     * the replication channel.
     * <p>
     * 
     * Tracked regattas read from the stream are observed (see {@link RaceListener}) by this object for automatic updates
     * to the default leaderboard and for automatic linking to leaderboard columns. It is assumed that no explicit
     * replication of these operations will happen based on the changes performed on the replication master.<p>
     * 
     * <b>Caution:</b> All relevant contents of this service instance will be replaced by the stream contents.
     */
    void initiallyFillFrom(ObjectInputStream ois) throws IOException, ClassNotFoundException;

    Event getEventByName(String name);

    /**
     * @return a thread-safe copy of the events currently known by the service; it's safe for callers to iterate over
     *         the iterable returned, and no risk of a {@link ConcurrentModificationException} exists
     */
    Iterable<Event> getAllEvents();

    /**
     * Creates a new event with the name <code>eventName</code>, the venue<code>venue</code> and the
     * regattas with the names in <code>regattaNames</code> and saves it in the database.
     * 
     * @param eventName
     *            The name of the new event
     * @param venue
     *            The name of the venue of the new event
     * @param publicationUrl
     *            The publication URL of the new event
     * @param isPublic
     *            Indicates whether the event is public accessible via the publication URL or not
     * @param regattaNames
     *            The names of the regattas contained in the new event.<br />
     * @return The new event
     */
    Event addEvent(String eventName, String venueName, String publicationUrl, boolean isPublic, List<String> regattaNames);

    /**
     * Updates a sailing event with the name <code>eventName</code>, the venue<code>venue</code> and the
     * regattas with the names in <code>regattaNames</code> and updates it in the database.
     * 
     * @param eventName
     *            The name of the event to update
     * @param venueName
     *            The name of the venue of the event
     * @param publicationUrl
     *            The publication URL of the event
     * @param isPublic
     *            Indicates whether the event is public accessible via the publication URL or not
     * @param regattaNames
     *            The names of the regattas contained in the event.<br />
     * @return The new event
     */
    void updateEvent(String eventName, String venueName, String publicationUrl, boolean isPublic, List<String> regattaNames);
    
    /**
     * Renames a sailing event. If a sailing event by the name <code>oldName</code> does not exist in {@link #getEvents()},
     * or if a event with the name <code>newName</code> already exists, an {@link IllegalArgumentException} is thrown.
     * If the method completes normally, the rename has been successful, and the event previously obtained by calling
     * {@link #getEventByName(String) getEventByName(oldName)} can now be obtained by calling
     * {@link #getEventByName(String) getEventByName(newName)}.
     */
    void renameEvent(String oldEventName, String newEventName);
    
    void removeEvent(String eventName);

    com.sap.sailing.domain.base.DomainFactory getBaseDomainFactory();

}
