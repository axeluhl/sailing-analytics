package com.sap.sailing.server;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartTimeEvent;
import com.sap.sailing.domain.base.CompetitorStore;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.LeaderboardSearchResult;
import com.sap.sailing.domain.base.LeaderboardSearchResultBase;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.RegattaRegistry;
import com.sap.sailing.domain.base.RemoteSailingServerReference;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.configuration.DeviceConfiguration;
import com.sap.sailing.domain.base.configuration.DeviceConfigurationIdentifier;
import com.sap.sailing.domain.base.configuration.DeviceConfigurationMatcher;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.domain.common.DataImportProgress;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.RaceFetcher;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaFetcher;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.leaderboard.EventResolver;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.LeaderboardGroupResolver;
import com.sap.sailing.domain.leaderboard.LeaderboardRegistry;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.racelog.tracking.GPSFixStore;
import com.sap.sailing.domain.regattalike.LeaderboardThatHasRegattaLike;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.RaceListener;
import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;
import com.sap.sailing.domain.tracking.TrackerManager;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.polars.PolarDataService;
import com.sap.sailing.server.masterdata.DataImportLockWithProgress;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TypeBasedServiceFinderFactory;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.common.search.KeywordQuery;
import com.sap.sse.common.search.Result;
import com.sap.sse.common.search.Searchable;
import com.sap.sse.filestorage.FileStorageManagementService;
import com.sap.sse.replication.impl.ReplicableWithObjectInputStream;

/**
 * An OSGi service that can be used to track boat races using a TracTrac connector that pushes live GPS boat location,
 * waypoint, coarse and mark passing data.
 * <p>
 * 
 * If a race/regatta is already being tracked, another {@link #addTracTracRace(URL, URI, URI, WindStore, long)} or
 * {@link #addRegatta(URL, URI, URI, WindStore, long)} call will have no effect, even if a different {@link WindStore}
 * is requested.
 * <p>
 * 
 * When the tracking of a race/regatta is {@link #stopTracking(Regatta, RaceDefinition) stopped}, the next time it's
 * started to be tracked, a new {@link TrackedRace} at least will be constructed. This also means that when a
 * {@link TrackedRegatta} exists that still holds other {@link TrackedRace}s, the no longer tracked {@link TrackedRace}
 * will be removed from the {@link TrackedRegatta}. corresponding information is removed also from the
 * {@link DomainFactory}'s caches to ensure that clean, fresh data is received should another tracking request be issued
 * later.
 * <p>
 * 
 * During receiving the initial load for a replication in {@link #initiallyFillFromInternal(java.io.ObjectInputStream)},
 * tracked regattas read from the stream are observed (see {@link RaceListener}) by this object for automatic updates to
 * the default leaderboard and for automatic linking to leaderboard columns. It is assumed that no explicit replication
 * of these operations will happen based on the changes performed on the replication master.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface RacingEventService extends TrackedRegattaRegistry, RegattaFetcher, RegattaRegistry, RaceFetcher,
        LeaderboardRegistry, EventResolver, LeaderboardGroupResolver, TrackerManager, Searchable<LeaderboardSearchResult, KeywordQuery>,
        ReplicableWithObjectInputStream<RacingEventService, RacingEventServiceOperation<?>> {
    @Override
    Regatta getRegatta(RegattaName regattaName);

    @Override
    RaceDefinition getRace(RegattaAndRaceIdentifier raceIdentifier);

    DynamicTrackedRace getTrackedRace(Regatta regatta, RaceDefinition race);

    DynamicTrackedRace getTrackedRace(RegattaAndRaceIdentifier raceIdentifier);

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
     * Looks at the mark tracks in the tracked races attached to the <code>leaderboard</code>. If it doesn't find a
     * track for the <code>mark</code> requested there which has fixes before and after <code>timePoint</code> (to
     * ensure that no track cropping has taken place, removing the fixes for the interesting time period), looks in the
     * leaderboard's regatta log and the specific <code>raceLog</code> (if provided) or all race logs attached to the
     * leaderboard (if not provided) for device mappings for the mark and tries to load fixes from the
     * {@link GPSFixStore}. The latter is only necessary if the mark isn't found in any tracked race with fixes
     * surrounding <code>timePoint</code> because should there be a tracked race in the leaderboard that has the mark
     * then it will also have received the fixes from the {@link GPSFixStore} through the regatta log mapping.
     * <p>
     * 
     * @return the position obtained by interpolation but never extrapolation from the track identified as described
     *         above
     */
    Position getMarkPosition(Mark mark, LeaderboardThatHasRegattaLike leaderboard, TimePoint timePoint, RaceLog raceLog);

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
     * from {@link #getAllRegattas()} will be {@link RaceTracker#stop(boolean) stopped}.
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
     * @param port
     *            the UDP port on which to listen for incoming messages from Expedition clients
     * @param correctByDeclination
     *            An optional service to convert the wind bearings (which the receiver may
     *            believe to be true bearings) from magnetic to true bearings.
     * @throws SocketException
     *             thrown, e.g., in case there is already another listener on the port requested
     */
    void startTrackingWind(Regatta regatta, RaceDefinition race, boolean correctByDeclination) throws Exception;

    void stopTrackingWind(Regatta regatta, RaceDefinition race) throws SocketException, IOException;

    /**
     * The {@link Triple#getC() third component} of the triples returned is a wind tracker-specific
     * comment where a wind tracker may provide information such as its type name or, if applicable,
     * connectivity information such as the network port on which it receives wind information.
     */
    Iterable<Util.Triple<Regatta, RaceDefinition, String>> getWindTrackedRaces();

    /**
     * Creates a new leaderboard with the <code>name</code> specified.
     * @param discardThresholds
     *            Tells the thresholds from which on a next higher number of worst races will be discarded per
     *            competitor. Example: <code>[3, 6]</code> means that starting from three races the single worst race
     *            will be discarded; starting from six races, the two worst races per competitor are discarded.
     * 
     * @return the leaderboard created
     */
    FlexibleLeaderboard addFlexibleLeaderboard(String leaderboardName, String leaderboardDisplayName, int[] discardThresholds, ScoringScheme scoringScheme, Serializable courseAreaId);

    RegattaLeaderboard addRegattaLeaderboard(RegattaIdentifier regattaIdentifier, String leaderboardDisplayName, int[] discardThresholds);

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
     * @see RaceColumn#setFactor(Double)
     */
    void updateLeaderboardColumnFactor(String leaderboardName, String columnName, Double factor);

    /**
     * Updates the leaderboard data in the persistent store
     */
    void updateStoredLeaderboard(Leaderboard leaderboard);

    void updateStoredRegatta(Regatta regatta);

    void stopTrackingAndRemove(Regatta regatta) throws MalformedURLException, IOException, InterruptedException;

    /**
     * Removes the regatta as well as all regatta leaderboards for that regatta
     */
    void removeRegatta(Regatta regatta) throws MalformedURLException, IOException, InterruptedException;
    
    /**
     * Removes the given series
     */
    void removeSeries(Series series) throws MalformedURLException, IOException, InterruptedException;

    DynamicTrackedRace getExistingTrackedRace(RegattaAndRaceIdentifier raceIdentifier);

    /**
     * Obtains an unmodifiable map of the leaderboard groups configured in this service keyed by their names.
     */
    Map<String, LeaderboardGroup> getLeaderboardGroups();

    /**
     * Creates a new group with the name <code>groupName</code>, the description <code>desciption</code> and the
     * leaderboards with the names in <code>leaderboardNames</code> and saves it in the database.
     * @param id TODO
     * @param groupName
     *            The name of the new group
     * @param description
     *            The description of the new group
     * @param displayName TODO
     * @param displayGroupsInReverseOrder TODO
     * @param leaderboardNames
     *            The names of the leaderboards, which should be contained by the new group.<br />
     *            If there isn't a leaderboard with one of these names an {@link IllegalArgumentException} is thrown.
     * @return The new leaderboard group
     */
    LeaderboardGroup addLeaderboardGroup(UUID id, String groupName, String description,
            String displayName, boolean displayGroupsInReverseOrder, List<String> leaderboardNames, int[] overallLeaderboardDiscardThresholds, ScoringSchemeType overallLeaderboardScoringSchemeType);

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

    DynamicTrackedRace createTrackedRace(RegattaAndRaceIdentifier raceIdentifier, WindStore windStore, GPSFixStore gpsFixStore,
            long delayToLiveInMillis, long millisecondsOverWhichToAverageWind, long millisecondsOverWhichToAverageSpeed, boolean useMarkPassingCalculator);

    Regatta getOrCreateDefaultRegatta(String name, String boatClassName, Serializable id);

    /**
     * @param series the series must not have any {@link RaceColumn}s yet
     */
    Regatta createRegatta(String regattaName, String boatClassName, TimePoint startDate, TimePoint endDate, Serializable id, Iterable<? extends Series> series,
            boolean persistent, ScoringScheme scoringScheme, Serializable defaultCourseAreaId,
            boolean useStartTimeInference);
    
    Regatta updateRegatta(RegattaIdentifier regattaIdentifier, TimePoint startDate, TimePoint endDate, Serializable newDefaultCourseAreaId, RegattaConfiguration regattaConfiguration, Iterable<? extends Series> series, boolean useStartTimeInference);

    /**
     * Adds <code>raceDefinition</code> to the {@link Regatta} such that it will appear in {@link Regatta#getAllRaces()}
     * and {@link Regatta#getRaceByName(String)}.
     * 
     * @param addToRegatta identifier of an regatta that must exist already
     */
    void addRace(RegattaIdentifier addToRegatta, RaceDefinition raceDefinition);

    void updateLeaderboardGroup(String oldName, String newName, String description, String displayName,
            List<String> leaderboardNames, int[] overallLeaderboardDiscardThresholds, ScoringSchemeType overallLeaderboardScoringSchemeType);

    /**
     * @return a thread-safe copy of the events currently known by the service; it's safe for callers to iterate over
     *         the iterable returned, and no risk of a {@link ConcurrentModificationException} exists
     */
    Iterable<Event> getAllEvents();

    /**
     * Creates a new event with the name <code>eventName</code>, the venue <code>venue</code> and the regattas with the
     * names in <code>regattaNames</code>, saves it in the database and replicates it. Use for TESTING only!
     * 
     * @param eventName
     *            The name of the new event
     * @param eventDescription TODO
     * @param startDate
     *            The start date of the event
     * @param endDate
     *            The end date of the event
     * @param isPublic
     *            Indicates whether the event is public accessible via the publication URL or not
     * @param id
     *            The id of the new event
     * @param venue
     *            The name of the venue of the new event
     * @return The new event
     */
    Event addEvent(String eventName, String eventDescription, TimePoint startDate, TimePoint endDate, String venueName, boolean isPublic, UUID id);

    /**
     * Updates a sailing event with the name <code>eventName</code>, the venue<code>venue</code> and the regattas with
     * the names in <code>regattaNames</code> and updates it in the database.
     * @param eventName
     *            The name of the event to update
     * @param startDate
     *            The start date of the event
     * @param endDate
     *            The end date of the event
     * @param venueName
     *            The name of the venue of the event
     * @param isPublic
     *            Indicates whether the event is public accessible via the publication URL or not
     * @return The new event
     */
    void updateEvent(UUID id, String eventName, String eventDescription, TimePoint startDate, TimePoint endDate,
            String venueName, boolean isPublic, Iterable<UUID> leaderboardGroupIds, URL officialWebsiteURL,
            URL logoImageURL, Iterable<URL> imageURLs, Iterable<URL> videoURLs, Iterable<URL> sponsorImageURLs);

    /**
     * Renames a sailing event. If a sailing event by the name <code>oldName</code> does not exist in {@link #getEvents()},
     * or if a event with the name <code>newName</code> already exists, an {@link IllegalArgumentException} is thrown.
     * If the method completes normally, the rename has been successful, and the event previously obtained by calling
     * {@link #getEventByName(String) getEventByName(oldName)} can now be obtained by calling
     * {@link #getEventByName(String) getEventByName(newName)}.
     */
    void renameEvent(UUID id, String newEventName);

    void removeEvent(UUID id);

    
    /**
     * @return a thread-safe copy of the events (or the exception that occurred trying to obtain the events; arranged in
     *         a {@link Util.Pair}) of from all sailing server instances currently known by the service; it's safe for
     *         callers to iterate over the iterable returned, and no risk of a {@link ConcurrentModificationException}
     *         exists
     */
    Map<RemoteSailingServerReference, Util.Pair<Iterable<EventBase>, Exception>> getPublicEventsOfAllSailingServers();

    RemoteSailingServerReference addRemoteSailingServerReference(String name, URL url);

    void removeRemoteSailingServerReference(String name);

    
    CourseArea addCourseArea(UUID eventId, String courseAreaName, UUID courseAreaId);

    com.sap.sailing.domain.base.DomainFactory getBaseDomainFactory();

    CourseArea getCourseArea(Serializable courseAreaId);

    /**
     * Adds the specified mediaTrack to the in-memory media library.
     * Important note: Only if mediaTrack.dbId != null the mediaTrack will be persisted in the the database.
     * @param mediaTrack
     */
    void mediaTrackAdded(MediaTrack mediaTrack);

    /**
     * Calling mediaTrackAdded for every entry in the specified collection. 
     * @param mediaTracks
     */
    void mediaTracksAdded(Collection<MediaTrack> mediaTracks);
    
    void mediaTrackTitleChanged(MediaTrack mediaTrack);

    void mediaTrackUrlChanged(MediaTrack mediaTrack);

    void mediaTrackStartTimeChanged(MediaTrack mediaTrack);

    void mediaTrackDurationChanged(MediaTrack mediaTrack);

    void mediaTrackAssignedRacesChanged(MediaTrack mediaTrack);
    
    void mediaTrackDeleted(MediaTrack mediaTrack);

    /**
     * In contrast to mediaTracksAdded, this method takes mediaTracks with a given dbId.
     * Checks if the track already exists in the library and the database and adds/stores it
     * accordingly. If a track already exists and override, its properties are checked for changes 
     * @param mediaTrack
     * @param override If true, track properties (title, url, start time, duration, not mime type!) will be 
     * overwritten with the values from the track to be imported.
     */
    void mediaTracksImported(Collection<MediaTrack> mediaTracksToImport, boolean override);
    
    Collection<MediaTrack> getMediaTracksForRace(RegattaAndRaceIdentifier regattaAndRaceIdentifier);
    
    Collection<MediaTrack> getMediaTracksInTimeRange(RegattaAndRaceIdentifier regattaAndRaceIdentifier);

    Collection<MediaTrack> getAllMediaTracks();

    void reloadRaceLog(String leaderboardName, String raceColumnName, String fleetName);

    RaceLog getRaceLog(String leaderboardName, String raceColumnName, String fleetName);

    /**
     * @return a pair with the found or created regatta, and a boolean that tells whether the regatta was created during
     *         the call
     */
    Util.Pair<Regatta, Boolean> getOrCreateRegattaWithoutReplication(String fullRegattaName, String boatClassName, 
            TimePoint startDate, TimePoint endDate, Serializable id, 
            Iterable<? extends Series> series, boolean persistent, ScoringScheme scoringScheme,
            Serializable defaultCourseAreaId, boolean useStartTimeInference);

    /**
     * @return map where keys are the toString() representation of the {@link RaceDefinition#getId() IDs} of races passed to
     * {@link #setRegattaForRace(Regatta, RaceDefinition)}. It helps remember the connection between races and regattas.
     */
    ConcurrentHashMap<String, Regatta> getPersistentRegattasForRaceIDs();
    
    Event createEventWithoutReplication(String eventName, String eventDescription, TimePoint startDate, TimePoint endDate, String venue,
            boolean isPublic, UUID id, Iterable<URL> imageURLs, Iterable<URL> videoURLs, Iterable<URL> sponsorImageURLs, URL logoImageURL, URL officialWebsiteURL);

    void setRegattaForRace(Regatta regatta, String raceIdAsString);

    CourseArea addCourseAreaWithoutReplication(UUID eventId, UUID courseAreaId, String courseAreaName);

    CourseArea removeCourseAreaWithoutReplication(UUID eventId, UUID courseAreaId);

    /**
     * Returns a mobile device's configuration.
     * @param identifier of the client (may include event)
     * @return the {@link DeviceConfiguration}
     */
    DeviceConfiguration getDeviceConfiguration(DeviceConfigurationIdentifier identifier);
    
    /**
     * Adds a device configuration.
     * @param matcher defining for which the configuration applies.
     * @param configuration of the device.
     */
    void createOrUpdateDeviceConfiguration(DeviceConfigurationMatcher matcher, DeviceConfiguration configuration);
    
    /**
     * Removes a configuration by its matching object.
     * @param matcher
     */
    void removeDeviceConfiguration(DeviceConfigurationMatcher matcher);

    /**
     * Returns all configurations and their matching objects. 
     * @return the {@link DeviceConfiguration}s.
     */
    Map<DeviceConfigurationMatcher, DeviceConfiguration> getAllDeviceConfigurations();

    /**
     * Forces a new start time on the RaceLog identified by the passed parameters.
     * @param leaderboardName name of the RaceLog's leaderboard.
     * @param raceColumnName name of the RaceLog's column
     * @param fleetName name of the RaceLog's fleet
     * @param authorName name of the {@link AbstractLogEventAuthor} the {@link RaceLogStartTimeEvent} will be created with
     * @param authorPriority priority of the author.
     * @param passId Pass identifier of the new start time event.
     * @param logicalTimePoint logical {@link TimePoint} of the new event.
     * @param startTime the new Start-Time
     * @return
     */
    TimePoint setStartTimeAndProcedure(String leaderboardName, String raceColumnName, String fleetName, String authorName,
            int authorPriority, int passId, TimePoint logicalTimePoint, TimePoint startTime, RacingProcedureType racingProcedure);

    /**
     * Gets the start time, pass identifier and racing procedure for the queried race. Start time might be <code>null</code>.
     */
    Util.Triple<TimePoint, Integer, RacingProcedureType> getStartTimeAndProcedure(String leaderboardName, String raceColumnName, String fleetName);

    MongoObjectFactory getMongoObjectFactory();
    
    DomainObjectFactory getDomainObjectFactory();
    
    WindStore getWindStore();

    PolarDataService getPolarDataService();
    
    GPSFixStore getGPSFixStore();
    
    RaceTracker getRaceTrackerById(Object id);
    
    AbstractLogEventAuthor getServerAuthor();
    
    CompetitorStore getCompetitorStore();
    
    TypeBasedServiceFinderFactory getTypeBasedServiceFinderFactory();

    /**
     * This lock exists to allow only one master data import at a time to avoid situation where multiple Imports
     * override each other in unpredictable fashion
     */
    DataImportLockWithProgress getDataImportLock();

    DataImportProgress createOrUpdateDataImportProgressWithReplication(UUID importOperationId,
            double overallProgressPct,
            String subProgressName, double subProgressPct);

    DataImportProgress createOrUpdateDataImportProgressWithoutReplication(UUID importOperationId,
            double overallProgressPct,
            String subProgressName, double subProgressPct);

    void setDataImportFailedWithReplication(UUID importOperationId, String errorMessage);

    void setDataImportFailedWithoutReplication(UUID importOperationId, String errorMessage);

    void setDataImportDeleteProgressFromMapTimerWithReplication(UUID importOperationId);

    void setDataImportDeleteProgressFromMapTimerWithoutReplication(UUID importOperationId);

    /**
     * For the reference to a remote sailing server, updates its events cache and returns the event list
     * or, if fetching the event list from the remote server did fail, the exception for which it failed.
     */
    Util.Pair<Iterable<EventBase>, Exception> updateRemoteServerEventCacheSynchronously(RemoteSailingServerReference ref);

    /**
     * Searches the content of this server, not that of any remote servers referenced by any {@link RemoteSailingServerReference}s.
     */
    @Override
    Result<LeaderboardSearchResult> search(KeywordQuery query);

    /**
     * Searches a specific remote server whose reference has the {@link RemoteSailingServerReference#getName() name}
     * <code>remoteServerReferenceName</code>. If a remote server reference with that name is not known,
     * <code>null</code> is returned. Otherwise, a non-<code>null</code> and possibly empty search result set is
     * returned.
     */
    Result<LeaderboardSearchResultBase> searchRemotely(String remoteServerReferenceName, KeywordQuery query);

    /**
     * References to remote servers may be dead or alive. This is internally determined by regularly polling those
     * servers for their events list. If the events list cannot be successfully retrieved, the server is considered "dead."
     * This method returns the "live" server references.
     */
    Iterable<RemoteSailingServerReference> getLiveRemoteServerReferences();

    RemoteSailingServerReference getRemoteServerReferenceByName(String remoteServerReferenceName);

    void addRegattaWithoutReplication(Regatta regatta);

    void addEventWithoutReplication(Event event);

    /**
     * Adds the leaderboard group to this service; if the group has an overall leaderboard, the overall leaderboard
     * is added to this service as well. For both, the group and the overall leaderboard, any previously existing
     * objects by the same name of that type will be replaced.
     */
    void addLeaderboardGroupWithoutReplication(LeaderboardGroup leaderboardGroup);

    /**
     * @return {@code null} if no service can be found in the OSGi registry
     */
    FileStorageManagementService getFileStorageManagementService();

    ClassLoader getCombinedMasterDataClassLoader();

}
