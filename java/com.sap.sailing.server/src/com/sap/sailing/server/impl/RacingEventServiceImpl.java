package com.sap.sailing.server.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorStore;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnInSeries;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.RegattaListener;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.Sideline;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.configuration.DeviceConfiguration;
import com.sap.sailing.domain.base.configuration.DeviceConfigurationIdentifier;
import com.sap.sailing.domain.base.configuration.DeviceConfigurationMatcher;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.domain.base.configuration.impl.DeviceConfigurationMapImpl;
import com.sap.sailing.domain.base.impl.DynamicCompetitor;
import com.sap.sailing.domain.base.impl.EventImpl;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.common.DataImportProgress;
import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.common.Renamable;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RegattaCreationParametersDTO;
import com.sap.sailing.domain.common.dto.SeriesCreationParametersDTO;
import com.sap.sailing.domain.common.impl.DataImportProgressImpl;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.domain.common.media.MediaTrack.MimeType;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.FlexibleRaceColumn;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.LeaderboardRegistry;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.leaderboard.impl.FlexibleLeaderboardImpl;
import com.sap.sailing.domain.leaderboard.impl.LeaderboardGroupImpl;
import com.sap.sailing.domain.leaderboard.impl.RegattaLeaderboardImpl;
import com.sap.sailing.domain.leaderboard.impl.ThresholdBasedResultDiscardingRuleImpl;
import com.sap.sailing.domain.leaderboard.meta.LeaderboardGroupMetaLeaderboard;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.MongoRaceLogStoreFactory;
import com.sap.sailing.domain.persistence.MongoWindStoreFactory;
import com.sap.sailing.domain.persistence.PersistenceFactory;
import com.sap.sailing.domain.persistence.media.DBMediaTrack;
import com.sap.sailing.domain.persistence.media.MediaDB;
import com.sap.sailing.domain.persistence.media.MediaDBFactory;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventVisitor;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.racelog.impl.RaceLogEventAuthorImpl;
import com.sap.sailing.domain.racelog.state.RaceState;
import com.sap.sailing.domain.racelog.state.ReadonlyRaceState;
import com.sap.sailing.domain.racelog.state.impl.RaceStateImpl;
import com.sap.sailing.domain.racelog.state.impl.ReadonlyRaceStateImpl;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.RaceChangeListener;
import com.sap.sailing.domain.tracking.RaceListener;
import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sailing.domain.tracking.RaceTrackingConnectivityParameters;
import com.sap.sailing.domain.tracking.RacesHandle;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRaceStatus;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tracking.WindTracker;
import com.sap.sailing.domain.tracking.WindTrackerFactory;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRegattaImpl;
import com.sap.sailing.expeditionconnector.ExpeditionWindTrackerFactory;
import com.sap.sailing.operationaltransformation.Operation;
import com.sap.sailing.server.OperationExecutionListener;
import com.sap.sailing.server.RacingEventServiceOperation;
import com.sap.sailing.server.Replicator;
import com.sap.sailing.server.masterdata.DataImportLockWithProgress;
import com.sap.sailing.server.operationaltransformation.AddCourseArea;
import com.sap.sailing.server.operationaltransformation.AddDefaultRegatta;
import com.sap.sailing.server.operationaltransformation.AddMediaTrackOperation;
import com.sap.sailing.server.operationaltransformation.AddRaceDefinition;
import com.sap.sailing.server.operationaltransformation.AddSpecificRegatta;
import com.sap.sailing.server.operationaltransformation.ConnectTrackedRaceToLeaderboardColumn;
import com.sap.sailing.server.operationaltransformation.CreateEvent;
import com.sap.sailing.server.operationaltransformation.CreateOrUpdateDataImportProgress;
import com.sap.sailing.server.operationaltransformation.CreateOrUpdateDeviceConfiguration;
import com.sap.sailing.server.operationaltransformation.CreateTrackedRace;
import com.sap.sailing.server.operationaltransformation.DataImportFailed;
import com.sap.sailing.server.operationaltransformation.RecordCompetitorGPSFix;
import com.sap.sailing.server.operationaltransformation.RecordMarkGPSFix;
import com.sap.sailing.server.operationaltransformation.RecordWindFix;
import com.sap.sailing.server.operationaltransformation.RemoveDeviceConfiguration;
import com.sap.sailing.server.operationaltransformation.RemoveEvent;
import com.sap.sailing.server.operationaltransformation.RemoveMediaTrackOperation;
import com.sap.sailing.server.operationaltransformation.RemoveWindFix;
import com.sap.sailing.server.operationaltransformation.RenameEvent;
import com.sap.sailing.server.operationaltransformation.SetDataImportDeleteProgressFromMapTimer;
import com.sap.sailing.server.operationaltransformation.TrackRegatta;
import com.sap.sailing.server.operationaltransformation.UpdateEvent;
import com.sap.sailing.server.operationaltransformation.UpdateMarkPassings;
import com.sap.sailing.server.operationaltransformation.UpdateMediaTrackDurationOperation;
import com.sap.sailing.server.operationaltransformation.UpdateMediaTrackStartTimeOperation;
import com.sap.sailing.server.operationaltransformation.UpdateMediaTrackTitleOperation;
import com.sap.sailing.server.operationaltransformation.UpdateMediaTrackUrlOperation;
import com.sap.sailing.server.operationaltransformation.UpdateRaceDelayToLive;
import com.sap.sailing.server.operationaltransformation.UpdateRaceTimes;
import com.sap.sailing.server.operationaltransformation.UpdateTrackedRaceStatus;
import com.sap.sailing.server.operationaltransformation.UpdateWindAveragingTime;
import com.sap.sailing.server.operationaltransformation.UpdateWindSourcesToExclude;
import com.sap.sailing.server.test.support.RacingEventServiceWithTestSupport;
import com.sap.sailing.util.BuildVersion;
import com.sap.sailing.util.impl.LockUtil;
import com.sap.sailing.util.impl.NamedReentrantReadWriteLock;

public class RacingEventServiceImpl implements RacingEventServiceWithTestSupport, RegattaListener, LeaderboardRegistry, Replicator {
    private static final Logger logger = Logger.getLogger(RacingEventServiceImpl.class.getName());

    /**
     * A scheduler for the periodic checks of the paramURL documents for the advent of {@link ControlPoint}s with static
     * position information otherwise not available through <code>MarkPassingReceiver</code>'s events.
     */
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final com.sap.sailing.domain.base.DomainFactory baseDomainFactory;

    /**
     * Holds the {@link Event} objects for those event registered with this service. Note that there may be
     * {@link Event} objects that exist outside this service for events not (yet) registered here.
     */
    protected final ConcurrentHashMap<Serializable, Event> eventsById;

    /**
     * Holds the {@link Regatta} objects for those races registered with this service. Note that there may be
     * {@link Regatta} objects that exist outside this service for regattas not (yet) registered here.
     */
    protected final ConcurrentHashMap<String, Regatta> regattasByName;

    private final ConcurrentHashMap<RaceDefinition, CourseChangeReplicator> courseListeners;

    protected final ConcurrentHashMap<Regatta, Set<RaceTracker>> raceTrackersByRegatta;

    /**
     * Remembers the trackers by paramURL/liveURI/storedURI to avoid duplication
     */
    protected final ConcurrentHashMap<Object, RaceTracker> raceTrackersByID;
    
    /**
     * {@link #addRace(RegattaIdentifier, RaceTrackingConnectivityParameters, long)} will check
     * {@link #raceTrackersByID} for the presence of a tracker and won't create a new tracker if one for the
     * connectivity parameters' ID already exists. This check and creation and addition to {@link #raceTrackersByID}
     * requires locking in the face of concurrent calls to
     * {@link #addRace(RegattaIdentifier, RaceTrackingConnectivityParameters, long)}. Using <code>synchronized</code> is
     * not ideal due to its coarse-grained locking style which allows for little concurrency. Instead, this map is used
     * to keep locks for any ID that any invocation of the
     * {@link #addRace(RegattaIdentifier, RaceTrackingConnectivityParameters, long)} method is currently working on.
     * Fetching or creating and putting a lock to this map happens in a synchronized method ({@link #getOrCreateRaceTrackersByIdLock}).
     * When done, the {@link #addRace(RegattaIdentifier, RaceTrackingConnectivityParameters, long)} method cleans up by
     * removing the lock again from this map, again using a synchronized method ({@link #removeRaceTrackersByIdLock}).
     */
    private final ConcurrentHashMap<Object, NamedReentrantReadWriteLock> raceTrackersByIDLocks;

    /**
     * Leaderboards managed by this racing event service
    */
    private final ConcurrentHashMap<String, Leaderboard> leaderboardsByName;

    private final ConcurrentHashMap<String, LeaderboardGroup> leaderboardGroupsByName;
    
    private final CompetitorStore competitorStore;

    private Set<DynamicTrackedRegatta> regattasObservedForDefaultLeaderboard = new HashSet<DynamicTrackedRegatta>();

    private final MongoObjectFactory mongoObjectFactory;

    private final DomainObjectFactory domainObjectFactory;

    private final ConcurrentHashMap<Regatta, DynamicTrackedRegatta> regattaTrackingCache;

    private final ConcurrentHashMap<OperationExecutionListener, OperationExecutionListener> operationExecutionListeners;

    /**
     * Keys are the toString() representation of the {@link RaceDefinition#getId() IDs} of races passed to
     * {@link #setRegattaForRace(Regatta, RaceDefinition)}.
     */
    private final ConcurrentHashMap<String, Regatta> persistentRegattasForRaceIDs;

    private final RaceLogReplicator raceLogReplicator;

    private final RaceLogScoringReplicator raceLogScoringReplicator;

    private final MediaDB mediaDB;

    private final MediaLibrary mediaLibrary;
    
    /**
     * Currently valid pairs of {@link DeviceConfigurationMatcher}s and {@link DeviceConfiguration}s. 
     * The contents of this map is persisted and replicated. See {@link DeviceConfigurationMapImpl}.
     */
    protected final DeviceConfigurationMapImpl configurationMap;

    private final WindStore windStore;

    /**
     * Allow only one master data import at a time to avoid situation where multiple Imports override each other in
     * unpredictable fashion
     */
    private final DataImportLockWithProgress dataImportLock;

    /**
     * If this service runs in the context of an OSGi environment, the activator should {@link #setBundleContext set the bundle context} on this
     * object so that service lookups become possible.
     */
    private BundleContext bundleContext;

    /**
     * Constructs a {@link DomainFactory base domain factory} that uses this object's {@link #competitorStore
     * competitor store} for competitor management. This base domain factory is then also used for the construction of
     * the {@link DomainObjectFactory}. This constructor variant initially clears the persistent competitor collection, hence
     * removes all previously persistent competitors. This is the default for testing and for backward compatibility with
     * prior releases that did not support a persistent competitor collection.
     */
    public RacingEventServiceImpl() {
        this(true, null);
    }
    
    public RacingEventServiceImpl(WindStore windStore) {
        this(true, windStore);
    }
    
    void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }
    
    /**
     * Like {@link #RacingEventServiceImpl()}, but allows callers to specify that the persistent competitor collection
     * be cleared before the service starts.
     * 
     * @param clearPersistentCompetitorStore
     *            if <code>true</code>, the {@link PersistentCompetitorStore} is created empty, with the correcponding
     *            database collection cleared as well. Use with caution! When used with <code>false</code>, competitors
     *            created and stored during previous service executions will initially be loaded.
     */
    public RacingEventServiceImpl(boolean clearPersistentCompetitorStore) {
        this(new PersistentCompetitorStore(PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory(), clearPersistentCompetitorStore), null);
    }
    
    private RacingEventServiceImpl(boolean clearPersistentCompetitorStore, WindStore windStore) {
        this(new PersistentCompetitorStore(PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory(), clearPersistentCompetitorStore), windStore);
    }
    
    private RacingEventServiceImpl(PersistentCompetitorStore persistentCompetitorStore, WindStore windStore) {
        this(persistentCompetitorStore.getDomainObjectFactory(), persistentCompetitorStore.getMongoObjectFactory(), persistentCompetitorStore.getBaseDomainFactory(), MediaDBFactory.INSTANCE.getDefaultMediaDB(), persistentCompetitorStore, windStore);
    }

    /**
     * Uses the default factories for the tracking adapters and the {@link DomainFactory base domain factory} of the
     * {@link PersistenceFactory#getDefaultDomainObjectFactory() default domain object factory}. This constructor should
     * be used for testing because it provides a transient {@link CompetitorStore} as required for competitor persistence.
     */
    public RacingEventServiceImpl(DomainObjectFactory domainObjectFactory, MongoObjectFactory mongoObjectFactory, MediaDB mediaDB, WindStore windStore) {
        this(domainObjectFactory, mongoObjectFactory, domainObjectFactory.getBaseDomainFactory(), mediaDB, domainObjectFactory.getBaseDomainFactory().getCompetitorStore(), windStore);
    }

    private RacingEventServiceImpl(DomainObjectFactory domainObjectFactory, MongoObjectFactory mongoObjectFactory,
            com.sap.sailing.domain.base.DomainFactory baseDomainFactory, MediaDB mediaDb, CompetitorStore competitorStore, WindStore windStore) {
        logger.info("Created " + this);
        if (windStore == null) {
            try {
                windStore = MongoWindStoreFactory.INSTANCE.getMongoWindStore(mongoObjectFactory, domainObjectFactory);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } 
        }
        this.baseDomainFactory = baseDomainFactory;
        this.domainObjectFactory = domainObjectFactory;
        this.mongoObjectFactory = mongoObjectFactory;
        this.mediaDB = mediaDb;
        this.competitorStore = competitorStore;
        this.windStore = windStore;
        this.dataImportLock = new DataImportLockWithProgress();
        
        regattasByName = new ConcurrentHashMap<String, Regatta>();
        eventsById = new ConcurrentHashMap<Serializable, Event>();
        regattaTrackingCache = new ConcurrentHashMap<>();
        raceTrackersByRegatta = new ConcurrentHashMap<>();
        raceTrackersByID = new ConcurrentHashMap<>();
        raceTrackersByIDLocks = new ConcurrentHashMap<>();
        leaderboardGroupsByName = new ConcurrentHashMap<>();
        leaderboardsByName = new ConcurrentHashMap<String, Leaderboard>();
        operationExecutionListeners = new ConcurrentHashMap<>();
        courseListeners = new ConcurrentHashMap<>();
        persistentRegattasForRaceIDs = new ConcurrentHashMap<>();
        this.raceLogReplicator = new RaceLogReplicator(this);
        this.raceLogScoringReplicator = new RaceLogScoringReplicator(this);
        this.mediaLibrary = new MediaLibrary();
        this.configurationMap = new DeviceConfigurationMapImpl();

        // Add one default leaderboard that aggregates all races currently tracked by this service.
        // This is more for debugging purposes than for anything else.
        addFlexibleLeaderboard(LeaderboardNameConstants.DEFAULT_LEADERBOARD_NAME, null, new int[] { 5, 8 },
                getBaseDomainFactory().createScoringScheme(ScoringSchemeType.LOW_POINT), null);
        loadStoredEvents();
        loadStoredRegattas();
        loadRaceIDToRegattaAssociations();
        loadStoredLeaderboardsAndGroups();
        loadMediaLibary();
        loadStoredDeviceConfigurations();
    }

    @Override
    public void clearState() throws Exception {
        for(String leaderboardGroupName : this.leaderboardGroupsByName.keySet()) {
            removeLeaderboardGroup(leaderboardGroupName);
        }
        
        for(String leaderboardName : this.leaderboardsByName.keySet()) {
            removeLeaderboard(leaderboardName);
        }
        
        for(Regatta regatta : this.regattasByName.values()) {
            stopTracking(regatta);
            removeRegatta(regatta);
        }
        
        for(Event event : this.eventsById.values()) {
            removeEvent(event.getId());
        }
        
        for(MediaTrack mediaTrack : this.mediaLibrary.allTracks()) {
            mediaTrackDeleted(mediaTrack);
        }
        
        this.competitorStore.clear();
        
        // Add one default leaderboard that aggregates all races currently tracked by this service.
        // This is more for debugging purposes than for anything else.
        addFlexibleLeaderboard(LeaderboardNameConstants.DEFAULT_LEADERBOARD_NAME, null, new int[] { 5, 8 },
                getBaseDomainFactory().createScoringScheme(ScoringSchemeType.LOW_POINT), null);
    }
    
    @Override
    public com.sap.sailing.domain.base.DomainFactory getBaseDomainFactory() {
        return baseDomainFactory;
    }
    
    @Override
    public MongoObjectFactory getMongoObjectFactory() {
        return mongoObjectFactory;
    }
    
    @Override
    public DomainObjectFactory getDomainObjectFactory() {
        return domainObjectFactory;
    }

    private void loadRaceIDToRegattaAssociations() {
        persistentRegattasForRaceIDs.putAll(domainObjectFactory.loadRaceIDToRegattaAssociations(this));
    }

    private void loadStoredRegattas() {
        for (Regatta regatta : domainObjectFactory.loadAllRegattas(this)) {
            logger.info("Putting regatta " + regatta.getName() + " (" + regatta.hashCode() + ") into regattasByName");
            regattasByName.put(regatta.getName(), regatta);
            regatta.addRegattaListener(this);
            regatta.addRaceColumnListener(raceLogReplicator);
            regatta.addRaceColumnListener(raceLogScoringReplicator);
            
            for (Event event : this.getAllEvents()) {
                for (CourseArea courseArea : event.getVenue().getCourseAreas()) {
                    if (regatta.getDefaultCourseArea() != null && courseArea.getId().equals(regatta.getDefaultCourseArea().getId())) {
                        logger.info("Associating loaded regatta " + regatta.getName() + " to course " + courseArea.getName() + " of event " + event.getName());
                        event.addRegatta(regatta);
                    }
                }
            }
        }
    }

    private void loadStoredEvents() {
        for (Event event : domainObjectFactory.loadAllEvents()) {
            synchronized (eventsById) {
                if (event.getId() != null)
                    eventsById.put(event.getId(), event);
            }
        }
    }

    /**
     * Collects media track references from the configured sources (mongo DB by default, ftp folder yet to be
     * implemented). The method is expected to be called initially blocking the API until finished.
     * 
     * Subsequent calls (assumed to be triggered from the admin console or in scheduled intervals) don't need to block.
     * In that case, the API will simply serve the current state.
     * 
     */
    private void loadMediaLibary() {
        Collection<DBMediaTrack> allDBMediaTracks = mediaDB.loadAllMediaTracks();
        for (DBMediaTrack dbMediaTrack : allDBMediaTracks) {
            MimeType mimeType = dbMediaTrack.mimeType != null ? MimeType.valueOf(dbMediaTrack.mimeType) : null;
            MediaTrack mediaTrack = new MediaTrack(dbMediaTrack.dbId, dbMediaTrack.title, dbMediaTrack.url,
                    dbMediaTrack.startTime, dbMediaTrack.durationInMillis, mimeType);
            mediaTrackAdded(mediaTrack);
        }
    }
    
    private void loadStoredDeviceConfigurations() {
        for (Entry<DeviceConfigurationMatcher, DeviceConfiguration> entry : domainObjectFactory.loadAllDeviceConfigurations()) {
            synchronized (configurationMap) {
                configurationMap.put(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public void addLeaderboard(Leaderboard leaderboard) {
        synchronized (leaderboardsByName) {
            leaderboardsByName.put(leaderboard.getName(), leaderboard);
            // RaceColumns of RegattaLeaderboards are tracked via its Regatta!
            if (leaderboard instanceof FlexibleLeaderboard) {
                leaderboard.addRaceColumnListener(raceLogReplicator);
                leaderboard.addRaceColumnListener(raceLogScoringReplicator);
            }
        }
    }

    private void loadStoredLeaderboardsAndGroups() {
        logger.info("loading stored leaderboards and groups");
        // Loading all leaderboard groups and the contained leaderboards
        for (LeaderboardGroup leaderboardGroup : domainObjectFactory.getAllLeaderboardGroups(this, this)) {
            logger.info("loaded leaderboard group " + leaderboardGroup.getName() + " into " + this);
            leaderboardGroupsByName.put(leaderboardGroup.getName(), leaderboardGroup);
        }
        // Loading the remaining leaderboards
        domainObjectFactory.getLeaderboardsNotInGroup(this, this);
        logger.info("done with loading stored leaderboards and groups");
    }

    @Override
    public FlexibleLeaderboard addFlexibleLeaderboard(String leaderboardName, String leaderboardDisplayName,
            int[] discardThresholds, ScoringScheme scoringScheme, Serializable courseAreaId) {
        logger.info("adding flexible leaderboard " + leaderboardName);
        RaceLogStore raceLogStore = MongoRaceLogStoreFactory.INSTANCE.getMongoRaceLogStore(mongoObjectFactory,
                domainObjectFactory);
        CourseArea courseArea = getCourseArea(courseAreaId);
        FlexibleLeaderboard result = new FlexibleLeaderboardImpl(raceLogStore, leaderboardName,
                new ThresholdBasedResultDiscardingRuleImpl(discardThresholds), scoringScheme,
                courseArea);
        result.setDisplayName(leaderboardDisplayName);
        synchronized (leaderboardsByName) {
            if (getLeaderboardByName(leaderboardName) != null) {
                throw new IllegalArgumentException("Leaderboard with name " + leaderboardName + " already exists");
            }
            addLeaderboard(result);
        }
        mongoObjectFactory.storeLeaderboard(result);
        return result;
    }

    @Override
    public CourseArea getCourseArea(Serializable courseAreaId) {
        for (Event event : getAllEvents()) {
            for (CourseArea courseArea : event.getVenue().getCourseAreas()) {
                if (courseArea.getId().equals(courseAreaId)) {
                    return courseArea;
                }
            }
        }
        return null;
    }

    @Override
    public RegattaLeaderboard addRegattaLeaderboard(RegattaIdentifier regattaIdentifier, String leaderboardDisplayName,
            int[] discardThresholds) {
        Regatta regatta = getRegatta(regattaIdentifier);
        logger.info("adding regatta leaderboard for regatta "
                + (regatta == null ? "null" : (regatta.getName() + " (" + regatta.hashCode() + ")")) + " to " + this);
        RegattaLeaderboard result = null;
        if (regatta != null) {
            result = new RegattaLeaderboardImpl(regatta, new ThresholdBasedResultDiscardingRuleImpl(discardThresholds));
            result.setDisplayName(leaderboardDisplayName);
            synchronized (leaderboardsByName) {
                if (getLeaderboardByName(result.getName()) != null) {
                    throw new IllegalArgumentException("Leaderboard with name " + result.getName()
                            + " already exists in " + this);
                }
                addLeaderboard(result);
            }
            mongoObjectFactory.storeLeaderboard(result);
        } else {
            logger.warning("Cannot find regatta " + regattaIdentifier
                    + ". Hence, cannot create regatta leaderboard for it.");
        }
        return result;
    }

    @Override
    public RaceColumn addColumnToLeaderboard(String columnName, String leaderboardName, boolean medalRace) {
        Leaderboard leaderboard = getLeaderboardByName(leaderboardName);
        if (leaderboard != null) {
            if (leaderboard instanceof FlexibleLeaderboard) {
                // uses the default fleet as the single fleet for the new column
                RaceColumn result = ((FlexibleLeaderboard) leaderboard).addRaceColumn(columnName, medalRace,
                        leaderboard.getFleet(null));
                updateStoredLeaderboard((FlexibleLeaderboard) leaderboard);
                return result;
            } else {
                throw new IllegalArgumentException("Leaderboard named " + leaderboardName
                        + " is not a FlexibleLeaderboard");
            }
        } else {
            throw new IllegalArgumentException("Leaderboard named " + leaderboardName + " not found");
        }
    }

    @Override
    public void moveLeaderboardColumnUp(String leaderboardName, String columnName) {
        Leaderboard leaderboard = getLeaderboardByName(leaderboardName);
        if (leaderboard != null && leaderboard instanceof FlexibleLeaderboard) {
            ((FlexibleLeaderboard) leaderboard).moveRaceColumnUp(columnName);
            updateStoredLeaderboard((FlexibleLeaderboard) leaderboard);
        } else {
            throw new IllegalArgumentException("Leaderboard named " + leaderboardName + " not found");
        }
    }

    @Override
    public void moveLeaderboardColumnDown(String leaderboardName, String columnName) {
        Leaderboard leaderboard = getLeaderboardByName(leaderboardName);
        if (leaderboard != null && leaderboard instanceof FlexibleLeaderboard) {
            ((FlexibleLeaderboard) leaderboard).moveRaceColumnDown(columnName);
            updateStoredLeaderboard((FlexibleLeaderboard) leaderboard);
        } else {
            throw new IllegalArgumentException("Leaderboard named " + leaderboardName + " not found");
        }
    }

    @Override
    public void removeLeaderboardColumn(String leaderboardName, String columnName) {
        Leaderboard leaderboard = getLeaderboardByName(leaderboardName);
        if (leaderboard == null) {
            throw new IllegalArgumentException("Leaderboard named " + leaderboardName + " not found");
        } else if (!(leaderboard instanceof FlexibleLeaderboard)) {
            throw new IllegalArgumentException("Columns cannot be removed from Leaderboard named " + leaderboardName);
        } else {
            ((FlexibleLeaderboard) leaderboard).removeRaceColumn(columnName);
            updateStoredLeaderboard((FlexibleLeaderboard) leaderboard);
        }
    }

    @Override
    public void renameLeaderboardColumn(String leaderboardName, String oldColumnName, String newColumnName) {
        Leaderboard leaderboard = getLeaderboardByName(leaderboardName);
        if (leaderboard != null) {
            final RaceColumn raceColumn = leaderboard.getRaceColumnByName(oldColumnName);
            if (raceColumn instanceof FlexibleRaceColumn) {
                // remove race log under old identifier; the race log identifier changes
                for (Fleet fleet : raceColumn.getFleets()) {
                    getMongoObjectFactory().removeRaceLog(raceColumn.getRaceLogIdentifier(fleet));
                }
                ((FlexibleRaceColumn) raceColumn).setName(newColumnName);
                // store the race logs again under the new identifiers
                storeRaceLogs(raceColumn);
                updateStoredLeaderboard(leaderboard);
            } else {
                throw new IllegalArgumentException("Race column " + oldColumnName + " cannot be renamed");
            }
        } else {
            throw new IllegalArgumentException("Leaderboard named " + leaderboardName + " not found");
        }
    }

    /**
     * When a race column is renamed, its race log identifiers change. Therefore, the race logs need to be stored
     * under the new identifier again to be consistent with the in-memory image again.
     */
    private void storeRaceLogs(RaceColumn raceColumn) {
        for (Fleet fleet : raceColumn.getFleets()) {
            RaceLogIdentifier identifier = raceColumn.getRaceLogIdentifier(fleet);
            RaceLogEventVisitor storeVisitor = MongoRaceLogStoreFactory.INSTANCE.getMongoRaceLogStoreVisitor(identifier, getMongoObjectFactory());
            RaceLog raceLog = raceColumn.getRaceLog(fleet);
            raceLog.lockForRead();
            try {
                for (RaceLogEvent e : raceLog.getRawFixes()) {
                    e.accept(storeVisitor);
                }
            } finally {
                raceLog.unlockAfterRead();
            }
        }
    }

    @Override
    public void updateLeaderboardColumnFactor(String leaderboardName, String columnName, Double factor) {
        Leaderboard leaderboard = getLeaderboardByName(leaderboardName);
        if (leaderboard != null) {
            final RaceColumn raceColumn = leaderboard.getRaceColumnByName(columnName);
            if (raceColumn != null) {
                raceColumn.setFactor(factor);
                updateStoredLeaderboard(leaderboard);
            } else {
                throw new IllegalArgumentException("Race column " + columnName + " not found in leaderboard "
                        + leaderboardName);
            }
        } else {
            throw new IllegalArgumentException("Leaderboard named " + leaderboardName + " not found");
        }
    }

    @Override
    public void renameLeaderboard(String oldName, String newName) {
        synchronized (leaderboardsByName) {
            if (!leaderboardsByName.containsKey(oldName)) {
                throw new IllegalArgumentException("No leaderboard with name " + oldName + " found");
            }
            if (leaderboardsByName.containsKey(newName)) {
                throw new IllegalArgumentException("Leaderboard with name " + newName + " already exists");
            }
            Leaderboard toRename = leaderboardsByName.get(oldName);
            if (toRename instanceof Renamable) {
                ((Renamable) toRename).setName(newName);
                leaderboardsByName.remove(oldName);
                leaderboardsByName.put(newName, toRename);
                mongoObjectFactory.renameLeaderboard(oldName, newName);
                syncGroupsAfterLeaderboardChange(toRename, true);
            } else {
                throw new IllegalArgumentException("Leaderboard with name " + newName + " is of type "
                        + toRename.getClass().getSimpleName() + " and therefore cannot be renamed");
            }
        }
    }

    @Override
    public void updateStoredLeaderboard(Leaderboard leaderboard) {
        getMongoObjectFactory().storeLeaderboard(leaderboard);
        syncGroupsAfterLeaderboardChange(leaderboard, true);
    }

    @Override
    public void updateStoredRegatta(Regatta regatta) {
        if (regatta.isPersistent()) {
            mongoObjectFactory.storeRegatta(regatta);
        }
    }

    /**
     * Checks all groups, if they contain a leaderboard with the name of the <code>updatedLeaderboard</code> and
     * replaces the one in the group with the updated one.<br />
     * This synchronizes things like the RaceIdentifier in the leaderboard columns.
     */
    private void syncGroupsAfterLeaderboardChange(Leaderboard updatedLeaderboard, boolean doDatabaseUpdate) {
        boolean groupNeedsUpdate = false;
        synchronized (leaderboardGroupsByName) {
            for (LeaderboardGroup leaderboardGroup : leaderboardGroupsByName.values()) {
                for (Leaderboard leaderboard : leaderboardGroup.getLeaderboards()) {
                    if (leaderboard == updatedLeaderboard) {
                        int index = leaderboardGroup.getIndexOf(leaderboard);
                        leaderboardGroup.removeLeaderboard(leaderboard);
                        leaderboardGroup.addLeaderboardAt(updatedLeaderboard, index);
                        groupNeedsUpdate = true;
                        // TODO we assume that the leaderboard names are unique, so we can break the inner loop here
                        break;
                    }
                }

                if (doDatabaseUpdate && groupNeedsUpdate) {
                    mongoObjectFactory.storeLeaderboardGroup(leaderboardGroup);
                }
                groupNeedsUpdate = false;
            }
        }
    }

    @Override
    public void removeLeaderboard(String leaderboardName) {
        Leaderboard leaderboard = removeLeaderboardFromLeaderboardsByName(leaderboardName);
        if (leaderboard != null) {
            leaderboard.removeRaceColumnListener(raceLogReplicator);
            leaderboard.removeRaceColumnListener(raceLogScoringReplicator);
            mongoObjectFactory.removeLeaderboard(leaderboardName);
            syncGroupsAfterLeaderboardRemove(leaderboardName, true);
            leaderboard.destroy();
        }
    }

    private Leaderboard removeLeaderboardFromLeaderboardsByName(String leaderboardName) {
        synchronized (leaderboardsByName) {
            return leaderboardsByName.remove(leaderboardName);
        }
    }

    /**
     * Checks all groups, if they contain a leaderboard with the <code>removedLeaderboardName</code> and removes it from
     * the group.
     * 
     * @param removedLeaderboardName
     */
    private void syncGroupsAfterLeaderboardRemove(String removedLeaderboardName, boolean doDatabaseUpdate) {
        boolean groupNeedsUpdate = false;
        synchronized (leaderboardGroupsByName) {
            for (LeaderboardGroup leaderboardGroup : leaderboardGroupsByName.values()) {
                for (Leaderboard leaderboard : leaderboardGroup.getLeaderboards()) {
                    if (leaderboard.getName().equals(removedLeaderboardName)) {
                        leaderboardGroup.removeLeaderboard(leaderboard);
                        groupNeedsUpdate = true;
                        // TODO we assume that the leaderboard names are unique, so we can break the inner loop here
                        break;
                    }
                }

                if (doDatabaseUpdate && groupNeedsUpdate) {
                    mongoObjectFactory.storeLeaderboardGroup(leaderboardGroup);
                }
                groupNeedsUpdate = false;
            }
        }
    }

    @Override
    public Leaderboard getLeaderboardByName(String name) {
        synchronized (leaderboardsByName) {
            return leaderboardsByName.get(name);
        }
    }

    @Override
    public Map<String, Leaderboard> getLeaderboards() {
        synchronized (leaderboardsByName) {
            return Collections.unmodifiableMap(new HashMap<String, Leaderboard>(leaderboardsByName));
        }
    }

    @Override
    public Iterable<Event> getAllEvents() {
        return Collections.unmodifiableCollection(new ArrayList<Event>(eventsById.values()));
    }

    @Override
    public Event getEvent(Serializable id) {
        return id == null ? null : eventsById.get(id);
    }

    @Override
    public Iterable<Regatta> getAllRegattas() {
        return Collections.unmodifiableCollection(new ArrayList<Regatta>(regattasByName.values()));
    }

    @Override
    public boolean isRaceBeingTracked(RaceDefinition r) {
        synchronized (raceTrackersByRegatta) {
            for (Set<RaceTracker> trackers : raceTrackersByRegatta.values()) {
                for (RaceTracker tracker : trackers) {
                    if (tracker.getRaces() != null && tracker.getRaces().contains(r)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    @Override
    public Regatta getRegattaByName(String name) {
        return name == null ? null : regattasByName.get(name);
    }

    @Override
    public Regatta getOrCreateDefaultRegatta(String baseRegattaName, String boatClassName, Serializable id) {
        String defaultRegattaName = RegattaImpl.getDefaultName(baseRegattaName, boatClassName);
        Regatta result = regattasByName.get(defaultRegattaName);
        if (result == null) {
            RaceLogStore raceLogStore = MongoRaceLogStoreFactory.INSTANCE.getMongoRaceLogStore(mongoObjectFactory,
                    domainObjectFactory);
            result = new RegattaImpl(
                    raceLogStore,
                    baseRegattaName,
                    getBaseDomainFactory().getOrCreateBoatClass(boatClassName),
                    this,
                    getBaseDomainFactory().createScoringScheme(ScoringSchemeType.LOW_POINT),
                    id, null);
            logger.info("Created default regatta " + result.getName() + " (" + hashCode() + ") on " + this);
            cacheAndReplicateDefaultRegatta(result);
        }
        return result;
    }

    @Override
    public Regatta createRegatta(String baseRegattaName, String boatClassName, Serializable id,
            Iterable<? extends Series> series, boolean persistent, ScoringScheme scoringScheme,
            Serializable defaultCourseAreaId) {
        Pair<Regatta, Boolean> regattaWithCreatedFlag = getOrCreateRegattaWithoutReplication(baseRegattaName,
                boatClassName, id, series, persistent, scoringScheme, defaultCourseAreaId);
        Regatta regatta = regattaWithCreatedFlag.getA();
        if (regattaWithCreatedFlag.getB()) {
            replicateSpecificRegattaWithoutRaceColumns(regatta);
        }
        return regatta;
    }

    @Override
    public Pair<Regatta, Boolean> getOrCreateRegattaWithoutReplication(String baseRegattaName, String boatClassName,
            Serializable id, Iterable<? extends Series> series, boolean persistent, ScoringScheme scoringScheme,
            Serializable defaultCourseAreaId) {
        RaceLogStore raceLogStore = MongoRaceLogStoreFactory.INSTANCE.getMongoRaceLogStore(mongoObjectFactory,
                domainObjectFactory);
        CourseArea courseArea = getCourseArea(defaultCourseAreaId);
        Regatta regatta = new RegattaImpl(raceLogStore, baseRegattaName, getBaseDomainFactory().getOrCreateBoatClass(
                boatClassName), series, persistent, scoringScheme, id, courseArea);
        boolean wasCreated = false;
        if (!regattasByName.containsKey(regatta.getName())) {
            wasCreated = true;
            logger.info("putting regatta " + regatta.getName() + " (" + regatta.hashCode()
                    + ") into regattasByName of " + this);
            regattasByName.put(regatta.getName(), regatta);
            regatta.addRegattaListener(this);
            regatta.addRaceColumnListener(raceLogReplicator);
            regatta.addRaceColumnListener(raceLogScoringReplicator);
        }
        logger.info("Created regatta " + regatta.getName() + " (" + hashCode() + ") on " + this);
        if (persistent) {
            updateStoredRegatta(regatta);
        }
        for (Event event : getAllEvents()) {
            for (CourseArea eventCourseArea : event.getVenue().getCourseAreas()) {
                if (defaultCourseAreaId != null && eventCourseArea.getId().equals(defaultCourseAreaId)) {
                    event.addRegatta(regatta);
                }
            }
        }
        return new Pair<Regatta, Boolean>(regatta, wasCreated);
    }

    @Override
    public void addRace(RegattaIdentifier addToRegatta, RaceDefinition raceDefinition) {
        Regatta regatta = getRegatta(addToRegatta);
        regatta.addRace(raceDefinition); // will trigger the raceAdded operation because this service is listening on
                                         // all its regattas
    }

    /**
     * If the <code>regatta</code> {@link Regatta#isPersistent() is a persistent one}, the association of the race with
     * the regatta is remembered persistently so that {@link #getRememberedRegattaForRace(Serializable)} will provide
     * it.
     */
    @Override
    public void raceAdded(Regatta regatta, RaceDefinition raceDefinition) {
        if (regatta.isPersistent()) {
            setRegattaForRace(regatta, raceDefinition);
        }
        final CourseChangeReplicator listener = new CourseChangeReplicator(this, regatta, raceDefinition);
        courseListeners.put(raceDefinition, listener);
        raceDefinition.getCourse().addCourseListener(listener);
        replicate(new AddRaceDefinition(regatta.getRegattaIdentifier(), raceDefinition));
    }

    @Override
    public void raceRemoved(Regatta regatta, RaceDefinition raceDefinition) {
        raceDefinition.getCourse().removeCourseListener(courseListeners.remove(raceDefinition));
    }
    
    private NamedReentrantReadWriteLock lockRaceTrackersById(Object trackerId) {
        NamedReentrantReadWriteLock lock;
        synchronized (raceTrackersByIDLocks) {
            lock = raceTrackersByIDLocks.get(trackerId);
            if (lock == null) {
                lock = new NamedReentrantReadWriteLock("raceTrackersByIDLock for "+trackerId, /* fair */ false);
                raceTrackersByIDLocks.put(trackerId, lock);
            }
        }
        LockUtil.lockForWrite(lock);
        return lock;
    }
    
    /**
     * @param lock
     *            need to pass the lock obtained from {@link #lockRaceTrackersById(Object)} because a competing thread
     *            may already have removed the lock from the {@link #raceTrackersByIDLocks} map
     */
    private synchronized void unlockRaceTrackersById(Object trackerId, NamedReentrantReadWriteLock lock) {
        LockUtil.unlockAfterWrite(lock);
        synchronized (raceTrackersByIDLocks) {
            raceTrackersByIDLocks.remove(trackerId);
        }
    }

    @Override
    public RacesHandle addRace(RegattaIdentifier regattaToAddTo, RaceTrackingConnectivityParameters params,
            long timeoutInMilliseconds) throws Exception {
        final Object trackerID = params.getTrackerID();
        NamedReentrantReadWriteLock raceTrackersByIdLock = lockRaceTrackersById(trackerID);
        try {
            RaceTracker tracker = raceTrackersByID.get(trackerID);
            if (tracker == null) {
                Regatta regatta = regattaToAddTo == null ? null : getRegatta(regattaToAddTo);
                if (regatta == null) {
                    // create tracker and use an existing or create a default regatta
                    tracker = params.createRaceTracker(this, windStore);
                } else {
                    // use the regatta selected by the RaceIdentifier regattaToAddTo
                    tracker = params.createRaceTracker(regatta, this, windStore);
                    assert tracker.getRegatta() == regatta;
                }
                synchronized (raceTrackersByRegatta) {
                    raceTrackersByID.put(trackerID, tracker);
                    Set<RaceTracker> trackers = raceTrackersByRegatta.get(tracker.getRegatta());
                    if (trackers == null) {
                        trackers = new HashSet<RaceTracker>();
                        raceTrackersByRegatta.put(tracker.getRegatta(), trackers);
                    }
                    trackers.add(tracker);
                }
                // TODO we assume here that the regatta name is unique which necessitates adding the boat class name to it in RegattaImpl constructor
                String regattaName = tracker.getRegatta().getName();
                Regatta regattaWithName = regattasByName.get(regattaName);
                // TODO we assume here that the regatta name is unique which necessitates adding the boat class name to it in RegattaImpl constructor
                if (regattaWithName != null) {
                    if (regattaWithName != tracker.getRegatta()) {
                        if (Util.isEmpty(regattaWithName.getAllRaces())) {
                            // probably, tracker removed the last races from the old regatta and created a new one
                            regattasByName.remove(regattaName);
                            cacheAndReplicateDefaultRegatta(tracker.getRegatta());
                        } else {
                            throw new RuntimeException("Internal error. Two regatta objects with equal name " + regattaName);
                        }
                    }
                } else {
                    cacheAndReplicateDefaultRegatta(tracker.getRegatta());
                }
            } else {
                WindStore existingTrackersWindStore = tracker.getWindStore();
                if (!existingTrackersWindStore.equals(windStore)) {
                    logger.warning("Wind store mismatch. Requested wind store: " + windStore
                            + ". Wind store in use by existing tracker: " + existingTrackersWindStore);
                }
            }
            if (timeoutInMilliseconds != -1) {
                scheduleAbortTrackerAfterInitialTimeout(tracker, timeoutInMilliseconds);
            }
            return tracker.getRacesHandle();
        } finally {
            unlockRaceTrackersById(trackerID, raceTrackersByIdLock);
        }
    }

    /**
     * The regatta and all its contained {@link Regatta#getAllRaces() races} are replicated to all replica.
     * 
     * @param regatta
     *            the series of this regatta must not have any {@link Series#getRaceColumns() race columns associated
     *            (yet)}.
     */
    private void replicateSpecificRegattaWithoutRaceColumns(Regatta regatta) {
        Serializable courseAreaId = null;
        if (regatta.getDefaultCourseArea() != null) {
            courseAreaId = regatta.getDefaultCourseArea().getId();
        }
        replicate(new AddSpecificRegatta(regatta.getBaseName(), regatta.getBoatClass() == null ? null : regatta
                .getBoatClass().getName(), regatta.getId(),
                getSeriesWithoutRaceColumnsConstructionParametersAsMap(regatta), regatta.isPersistent(),
                regatta.getScoringScheme(), courseAreaId));
        RegattaIdentifier regattaIdentifier = regatta.getRegattaIdentifier();
        for (RaceDefinition race : regatta.getAllRaces()) {
            replicate(new AddRaceDefinition(regattaIdentifier, race));
        }
    }

    private RegattaCreationParametersDTO getSeriesWithoutRaceColumnsConstructionParametersAsMap(Regatta regatta) {
        LinkedHashMap<String, SeriesCreationParametersDTO> result = new LinkedHashMap<String, SeriesCreationParametersDTO>();
        for (Series s : regatta.getSeries()) {
            assert Util.isEmpty(s.getRaceColumns());
            List<FleetDTO> fleetNamesAndOrdering = new ArrayList<FleetDTO>();
            for (Fleet f : s.getFleets()) {
                fleetNamesAndOrdering.add(getBaseDomainFactory().convertToFleetDTO(f));
            }
            result.put(
                    s.getName(),
                    new SeriesCreationParametersDTO(fleetNamesAndOrdering, s.isMedal(), s.isStartsWithZeroScore(), s
                            .isFirstColumnIsNonDiscardableCarryForward(), s.getResultDiscardingRule() == null ? null
                            : s.getResultDiscardingRule().getDiscardIndexResultsStartingWithHowManyRaces(), s.hasSplitFleetContiguousScoring()));
        }
        return new RegattaCreationParametersDTO(result);
    }

    /**
     * If <code>regatta</code> is not yet in {@link #regattasByName}, it is added, this service is
     * {@link Regatta#addRegattaListener(RegattaListener) added} as regatta listener, and the regatta and all its
     * contained {@link Regatta#getAllRaces() races} are replicated to all replica.
     */
    private void cacheAndReplicateDefaultRegatta(Regatta regatta) {
        if (!regattasByName.containsKey(regatta.getName())) {
            logger.info("putting regatta " + regatta.getName() + " (" + regatta.hashCode()
                    + ") into regattasByName of " + this);
            regattasByName.put(regatta.getName(), regatta);
            regatta.addRegattaListener(this);
            regatta.addRaceColumnListener(raceLogReplicator);
            regatta.addRaceColumnListener(raceLogScoringReplicator);

            replicate(new AddDefaultRegatta(regatta.getBaseName(), regatta.getBoatClass() == null ? null : regatta
                    .getBoatClass().getName(), regatta.getId()));
            RegattaIdentifier regattaIdentifier = regatta.getRegattaIdentifier();
            for (RaceDefinition race : regatta.getAllRaces()) {
                replicate(new AddRaceDefinition(regattaIdentifier, race));
            }
        }
    }

    @Override
    public DynamicTrackedRace createTrackedRace(RegattaAndRaceIdentifier raceIdentifier, WindStore windStore,
            long delayToLiveInMillis, long millisecondsOverWhichToAverageWind, long millisecondsOverWhichToAverageSpeed) {
        DynamicTrackedRegatta trackedRegatta = getOrCreateTrackedRegatta(getRegatta(raceIdentifier));
        RaceDefinition race = getRace(raceIdentifier);
        return trackedRegatta.createTrackedRace(race, Collections.<Sideline> emptyList(), windStore, delayToLiveInMillis,
                millisecondsOverWhichToAverageWind, millisecondsOverWhichToAverageSpeed,
                /* raceDefinitionSetToUpdate */null);
    }

    private void ensureRegattaIsObservedForDefaultLeaderboardAndAutoLeaderboardLinking(
            DynamicTrackedRegatta trackedRegatta) {
        synchronized (regattasObservedForDefaultLeaderboard) {
            if (!regattasObservedForDefaultLeaderboard.contains(trackedRegatta)) {
                trackedRegatta.addRaceListener(new RaceAdditionListener());
                regattasObservedForDefaultLeaderboard.add(trackedRegatta);
            }
        }
    }

    private void stopObservingRegattaForRedaultLeaderboardAndAutoLeaderboardLinking(DynamicTrackedRegatta trackedRegatta) {
        synchronized (regattasObservedForDefaultLeaderboard) {
            regattasObservedForDefaultLeaderboard.remove(trackedRegatta);
        }
    }

    /**
     * A listener class used to ensure that when a tracked race is added to any {@link TrackedRegatta} managed by this
     * service, the service adds the tracked race to the default leaderboard and links it to the leaderboard columns
     * that were previously connected to it. Additionally, a {@link RaceChangeListener} is added to the
     * {@link TrackedRace} which is responsible for triggering the replication of all relevant changes to the tracked
     * race. When a tracked race is removed, the {@link TrackedRaceReplicator} that was added as listener to that
     * tracked race is removed again.
     * 
     * @author Axel Uhl (d043530)
     * 
     */
    private class RaceAdditionListener implements RaceListener, Serializable {
        private static final long serialVersionUID = 1036955460477000265L;

        private final Map<TrackedRace, TrackedRaceReplicator> trackedRaceReplicators;

        public RaceAdditionListener() {
            this.trackedRaceReplicators = new HashMap<TrackedRace, TrackedRaceReplicator>();
        }

        @Override
        public void raceRemoved(TrackedRace trackedRace) {
            TrackedRaceReplicator trackedRaceReplicator = trackedRaceReplicators.remove(trackedRace);
            if (trackedRaceReplicator != null) {
                trackedRace.removeListener(trackedRaceReplicator);
            }
        }

        @Override
        public void raceAdded(TrackedRace trackedRace) {
            // replicate the addition of the tracked race:
            CreateTrackedRace op = new CreateTrackedRace(trackedRace.getRaceIdentifier(), trackedRace.getWindStore(),
                    trackedRace.getDelayToLiveInMillis(), trackedRace.getMillisecondsOverWhichToAverageWind(),
                    trackedRace.getMillisecondsOverWhichToAverageSpeed());
            replicate(op);
            linkRaceToConfiguredLeaderboardColumns(trackedRace);
            final FlexibleLeaderboard defaultLeaderboard = (FlexibleLeaderboard) leaderboardsByName
                    .get(LeaderboardNameConstants.DEFAULT_LEADERBOARD_NAME);
            if (defaultLeaderboard != null) {
                String columnName = trackedRace.getRace().getName();
                defaultLeaderboard.addRace(trackedRace, columnName, /* medalRace */false,
                        defaultLeaderboard.getFleet(null));
            }
            TrackedRaceReplicator trackedRaceReplicator = new TrackedRaceReplicator(trackedRace);
            trackedRaceReplicators.put(trackedRace, trackedRaceReplicator);
            trackedRace.addListener(trackedRaceReplicator, /* fire wind already loaded */ true);
        }
    }

    private class TrackedRaceReplicator implements RaceChangeListener {
        private final TrackedRace trackedRace;

        public TrackedRaceReplicator(TrackedRace trackedRace) {
            this.trackedRace = trackedRace;
        }

        @Override
        public void windSourcesToExcludeChanged(Iterable<? extends WindSource> windSourcesToExclude) {
            replicate(new UpdateWindSourcesToExclude(getRaceIdentifier(), windSourcesToExclude));
        }

        @Override
        public void raceTimesChanged(TimePoint startOfTracking, TimePoint endOfTracking, TimePoint startTimeReceived) {
            replicate(new UpdateRaceTimes(getRaceIdentifier(), startOfTracking, endOfTracking, startTimeReceived));
        }

        @Override
        public void delayToLiveChanged(long delayToLiveInMillis) {
            replicate(new UpdateRaceDelayToLive(getRaceIdentifier(), delayToLiveInMillis));
        }

        @Override
        public void windDataReceived(Wind wind, WindSource windSource) {
            replicate(new RecordWindFix(getRaceIdentifier(), windSource, wind));
        }

        @Override
        public void windDataRemoved(Wind wind, WindSource windSource) {
            replicate(new RemoveWindFix(getRaceIdentifier(), windSource, wind));
        }

        @Override
        public void windAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage) {
            replicate(new UpdateWindAveragingTime(getRaceIdentifier(), newMillisecondsOverWhichToAverage));
        }

        @Override
        public void competitorPositionChanged(GPSFixMoving fix, Competitor competitor) {
            replicate(new RecordCompetitorGPSFix(getRaceIdentifier(), competitor, fix));
        }

        @Override
        public void statusChanged(TrackedRaceStatus newStatus) {
            replicate(new UpdateTrackedRaceStatus(getRaceIdentifier(), newStatus));
        }

        @Override
        public void markPositionChanged(GPSFix fix, Mark mark) {
            replicate(new RecordMarkGPSFix(getRaceIdentifier(), mark, fix));
        }

        @Override
        public void markPassingReceived(Competitor competitor, Map<Waypoint, MarkPassing> oldMarkPassings,
                Iterable<MarkPassing> markPassings) {
            replicate(new UpdateMarkPassings(getRaceIdentifier(), competitor, markPassings));
        }

        @Override
        public void speedAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage) {
            replicate(new UpdateWindAveragingTime(getRaceIdentifier(), newMillisecondsOverWhichToAverage));
        }

        private RegattaAndRaceIdentifier getRaceIdentifier() {
            return trackedRace.getRaceIdentifier();
        }
    }

    /**
     * Based on the <code>trackedRace</code>'s {@link TrackedRace#getRaceIdentifier() race identifier}, the tracked race
     * is (re-)associated to all {@link RaceColumn race columns} that currently have no
     * {@link RaceColumn#getTrackedRace(Fleet) tracked race assigned} and whose
     * {@link RaceColumn#getRaceIdentifier(Fleet) race identifier} equals that of <code>trackedRace</code>.
     */
    private void linkRaceToConfiguredLeaderboardColumns(TrackedRace trackedRace) {
        boolean leaderboardHasChanged = false;
        RegattaAndRaceIdentifier trackedRaceIdentifier = trackedRace.getRaceIdentifier();
        for (Leaderboard leaderboard : getLeaderboards().values()) {
            for (RaceColumn column : leaderboard.getRaceColumns()) {
                for (Fleet fleet : column.getFleets()) {
                    if (trackedRaceIdentifier.equals(column.getRaceIdentifier(fleet))
                            && column.getTrackedRace(fleet) == null) {
                        column.setTrackedRace(fleet, trackedRace);
                        leaderboardHasChanged = true;
                        replicate(new ConnectTrackedRaceToLeaderboardColumn(leaderboard.getName(), column.getName(),
                                fleet.getName(), trackedRaceIdentifier));
                    }
                }
            }
            if (leaderboardHasChanged) {
                // Update the corresponding groups, to keep them in sync
                syncGroupsAfterLeaderboardChange(leaderboard, /* doDatabaseUpdate */false);
            }
        }
    }

    @Override
    public void stopTracking(Regatta regatta) throws MalformedURLException, IOException, InterruptedException {
        synchronized (raceTrackersByRegatta) {
            if (raceTrackersByRegatta.containsKey(regatta)) {
                for (RaceTracker raceTracker : raceTrackersByRegatta.get(regatta)) {
                    final Set<RaceDefinition> races = raceTracker.getRaces();
                    if (races != null) {
                        for (RaceDefinition race : races) {
                            stopTrackingWind(regatta, race);
                        }
                    }
                    raceTracker.stop();
                    raceTrackersByID.remove(raceTracker.getID());
                }
                raceTrackersByRegatta.remove(regatta);
            }
        }
    }

    @Override
    public void stopTrackingAndRemove(Regatta regatta) throws MalformedURLException, IOException, InterruptedException {
        stopTracking(regatta);
        if (regatta != null) {
            if (regatta.getName() != null) {
                logger.info("Removing regatta " + regatta.getName() + " (" + regatta.hashCode() + ") from " + this);
                regattasByName.remove(regatta.getName());
                regattaTrackingCache.remove(regatta);
                regatta.removeRegattaListener(this);
                regatta.removeRaceColumnListener(raceLogReplicator);
                regatta.removeRaceColumnListener(raceLogScoringReplicator);
            }
            for (RaceDefinition race : regatta.getAllRaces()) {
                stopTrackingWind(regatta, race);
                // remove from default leaderboard
                FlexibleLeaderboard defaultLeaderboard = (FlexibleLeaderboard) getLeaderboardByName(LeaderboardNameConstants.DEFAULT_LEADERBOARD_NAME);
                defaultLeaderboard.removeRaceColumn(race.getName());
            }
        }
    }

    /**
     * The tracker will initially try to connect to the tracking infrastructure to obtain basic race master data. If
     * this fails after some timeout, to avoid garbage and lingering threads, the task scheduled by this method will
     * check after the timeout expires if race master data was successfully received. If so, the tracker continues
     * normally. Otherwise, the tracker is shut down orderly by calling {@link RaceTracker#stop() stopping}.
     * 
     * @return the scheduled task, in case the caller wants to {@link ScheduledFuture#cancel(boolean) cancel} it, e.g.,
     *         when the tracker is stopped or has successfully received the race
     */
    private ScheduledFuture<?> scheduleAbortTrackerAfterInitialTimeout(final RaceTracker tracker,
            final long timeoutInMilliseconds) {
        ScheduledFuture<?> task = getScheduler().schedule(new Runnable() {
            @Override
            public void run() {
                if (tracker.getRaces() == null || tracker.getRaces().isEmpty()) {
                    try {
                        Regatta regatta = tracker.getRegatta();
                        logger.log(Level.SEVERE, "RaceDefinition for a race in regatta " + regatta.getName()
                                + " not obtained within " + timeoutInMilliseconds
                                + "ms. Aborting tracker for this race.");
                        Set<RaceTracker> trackersForRegatta;
                        synchronized (raceTrackersByRegatta) {
                            trackersForRegatta = raceTrackersByRegatta.get(regatta);
                            if (trackersForRegatta != null) {
                                trackersForRegatta.remove(tracker);
                            }
                            tracker.stop();
                            raceTrackersByID.remove(tracker.getID());
                        }
                        if (trackersForRegatta == null || trackersForRegatta.isEmpty()) {
                            stopTracking(regatta);
                        }
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "scheduleAbortTrackerAfterInitialTimeout", e);
                        e.printStackTrace();
                    }
                }
            }
        }, /* delay */timeoutInMilliseconds, /* unit */TimeUnit.MILLISECONDS);
        return task;
    }

    @Override
    public void stopTracking(Regatta regatta, RaceDefinition race) throws MalformedURLException, IOException,
            InterruptedException {
        logger.info("Stopping tracking for " + race + "...");
        synchronized (raceTrackersByRegatta) {
            final Set<RaceTracker> trackerSet = raceTrackersByRegatta.get(regatta);
            if (raceTrackersByRegatta.containsKey(regatta)) {
                Iterator<RaceTracker> trackerIter = trackerSet.iterator();
                while (trackerIter.hasNext()) {
                    RaceTracker raceTracker = trackerIter.next();
                    if (raceTracker.getRaces() != null && raceTracker.getRaces().contains(race)) {
                        logger.info("Found tracker to stop for races " + raceTracker.getRaces());
                        raceTracker.stop();
                        trackerIter.remove();
                        raceTrackersByID.remove(raceTracker.getID());
                    }
                }
            } else {
                logger.warning("Didn't find any trackers for regatta " + regatta);
            }
            stopTrackingWind(regatta, race);
            // if the last tracked race was removed, confirm that tracking for the entire regatta has stopped
            if (trackerSet == null || trackerSet.isEmpty()) {
                stopTracking(regatta);
            }
        }
    }

    @Override
    public void removeRegatta(Regatta regatta) throws MalformedURLException, IOException, InterruptedException {
        Set<RegattaLeaderboard> leaderboardsToRemove = new HashSet<>();
        for (Leaderboard leaderboard : getLeaderboards().values()) {
            if (leaderboard instanceof RegattaLeaderboard) {
                RegattaLeaderboard regattaLeaderboard = (RegattaLeaderboard) leaderboard;
                if (regattaLeaderboard.getRegatta() == regatta) {
                    leaderboardsToRemove.add(regattaLeaderboard);
                }
            }
        }
        for (RegattaLeaderboard regattaLeaderboardToRemove : leaderboardsToRemove) {
            removeLeaderboard(regattaLeaderboardToRemove.getName());
        }
        for (RaceDefinition race : regatta.getAllRaces()) {
            removeRace(regatta, race);
            mongoObjectFactory.removeRegattaForRaceID(race.getName(), regatta);
            persistentRegattasForRaceIDs.remove(race.getId().toString());
        }
        if (regatta.isPersistent()) {
            mongoObjectFactory.removeRegatta(regatta);
        }
        regattasByName.remove(regatta.getName());
        regatta.removeRegattaListener(this);
        regatta.removeRaceColumnListener(raceLogReplicator);
        regatta.removeRaceColumnListener(raceLogScoringReplicator);
    }
    
    @Override
    public void removeSeries(Series series) throws MalformedURLException, IOException, InterruptedException {
        Regatta regatta = series.getRegatta();
        regatta.removeSeries(series);
        if (regatta.isPersistent()) {
            mongoObjectFactory.storeRegatta(regatta);
        }
    }

    @Override
    public Regatta updateRegatta(RegattaIdentifier regattaIdentifier, Serializable newDefaultCourseAreaId, 
           RegattaConfiguration newRegattaConfiguration, Iterable<? extends Series> series) {
        // We're not doing any renaming of the regatta itself, therefore we don't have to sync on the maps.
        Regatta regatta = getRegatta(regattaIdentifier);
        CourseArea newCourseArea = getCourseArea(newDefaultCourseAreaId);
        if (newCourseArea != regatta.getDefaultCourseArea()) {
            regatta.setDefaultCourseArea(newCourseArea);
        }
        regatta.setRegattaConfiguration(newRegattaConfiguration);
        if (series != null) {
            for (Series seriesObj : series) {
                regatta.addSeries(seriesObj);
            }
        }
        for (Event event : getAllEvents()) {
            event.removeRegatta(regatta);
            for (CourseArea courseArea : event.getVenue().getCourseAreas()) {
                if (newDefaultCourseAreaId != null && courseArea.getId().equals(newDefaultCourseAreaId)) {
                    event.addRegatta(regatta);
                }
            }
        }
        mongoObjectFactory.storeRegatta(regatta);
        return regatta;
    }

    @Override
    public void removeRace(Regatta regatta, RaceDefinition race) throws MalformedURLException, IOException,
            InterruptedException {
        logger.info("Removing the race " + race + "...");
        stopAllTrackersForWhichRaceIsLastReachable(regatta, race);
        stopTrackingWind(regatta, race);
        TrackedRace trackedRace = getExistingTrackedRace(regatta, race);
        if (trackedRace != null) {
            TrackedRegatta trackedRegatta = getTrackedRegatta(regatta);
            if (trackedRegatta != null) {
                trackedRegatta.removeTrackedRace(trackedRace);
            }
            if (Util.isEmpty(trackedRegatta.getTrackedRaces())) {
                removeTrackedRegatta(regatta);
            }
            // remove tracked race from RaceColumns of regatta
            for (Series series : regatta.getSeries()) {
                for (RaceColumnInSeries raceColumn : series.getRaceColumns()) {
                    for (Fleet fleet : series.getFleets()) {
                        if (raceColumn.getTrackedRace(fleet) == trackedRace) {
                            raceColumn.releaseTrackedRace(fleet);
                        }
                    }
                }
            }
            for (Leaderboard leaderboard : getLeaderboards().values()) {
                if (leaderboard instanceof FlexibleLeaderboard) { // RegattaLeaderboards have implicitly been updated by the code above
                    for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
                        for (Fleet fleet : raceColumn.getFleets()) {
                            if (raceColumn.getTrackedRace(fleet) == trackedRace) {
                                raceColumn.releaseTrackedRace(fleet); // but leave the RaceIdentifier on the race column untouched, e.g., for later re-load
                            }
                        }
                    }
                }
            }
        }
        // remove the race from the regatta if the regatta is not persistently stored
        regatta.removeRace(race);
        if (!regatta.isPersistent() && Util.isEmpty(regatta.getAllRaces())) {
            logger.info("Removing regatta " + regatta.getName() + " (" + regatta.hashCode() + ") from service " + this);
            regattasByName.remove(regatta.getName());
            regatta.removeRegattaListener(this);
            regatta.removeRaceColumnListener(raceLogReplicator);
            regatta.removeRaceColumnListener(raceLogScoringReplicator);
        }
    }

    /**
     * Doesn't stop any wind trackers
     */
    private void stopAllTrackersForWhichRaceIsLastReachable(Regatta regatta, RaceDefinition race)
            throws MalformedURLException, IOException, InterruptedException {
        synchronized (raceTrackersByRegatta) {
            if (raceTrackersByRegatta.containsKey(regatta)) {
                Iterator<RaceTracker> trackerIter = raceTrackersByRegatta.get(regatta).iterator();
                while (trackerIter.hasNext()) {
                    RaceTracker raceTracker = trackerIter.next();
                    if (raceTracker.getRaces() != null && raceTracker.getRaces().contains(race)) {
                        boolean foundReachableRace = false;
                        for (RaceDefinition raceTrackedByTracker : raceTracker.getRaces()) {
                            if (raceTrackedByTracker != race && isReachable(regatta, raceTrackedByTracker)) {
                                foundReachableRace = true;
                                break;
                            }
                        }
                        if (!foundReachableRace) {
                            // firstly stop the tracker
                            raceTracker.stop();
                            // remove it from the raceTrackers by Regatta
                            trackerIter.remove();
                            raceTrackersByID.remove(raceTracker.getID());
                            // if the last tracked race was removed, remove the entire regatta
                            if (raceTrackersByRegatta.get(regatta).isEmpty()) {
                                stopTracking(regatta);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isReachable(Regatta regatta, RaceDefinition race) {
        return Util.contains(regatta.getAllRaces(), race);
    }

    @Override
    public void startTrackingWind(Regatta regatta, RaceDefinition race, boolean correctByDeclination) throws Exception {
        for (WindTrackerFactory windTrackerFactory : getWindTrackerFactories()) {
            windTrackerFactory.createWindTracker(getOrCreateTrackedRegatta(regatta), race, correctByDeclination);
        }
    }

    @Override
    public void stopTrackingWind(Regatta regatta, RaceDefinition race) throws SocketException, IOException {
        for (WindTrackerFactory windTrackerFactory : getWindTrackerFactories()) {
            WindTracker windTracker = windTrackerFactory.getExistingWindTracker(race);
            if (windTracker != null) {
                windTracker.stop();
            }
        }
    }

    @Override
    public Iterable<Triple<Regatta, RaceDefinition, String>> getWindTrackedRaces() {
        List<Triple<Regatta, RaceDefinition, String>> result = new ArrayList<Triple<Regatta, RaceDefinition, String>>();
        for (Regatta regatta : getAllRegattas()) {
            for (RaceDefinition race : regatta.getAllRaces()) {
                for (WindTrackerFactory windTrackerFactory : getWindTrackerFactories()) {
                    WindTracker windTracker = windTrackerFactory.getExistingWindTracker(race);
                    if (windTracker != null) {
                        result.add(new Triple<Regatta, RaceDefinition, String>(regatta, race, windTracker.toString()));
                    }
                }
            }
        }
        return result;
    }

    @Override
    public DynamicTrackedRace getTrackedRace(Regatta regatta, RaceDefinition race) {
        return getOrCreateTrackedRegatta(regatta).getTrackedRace(race);
    }

    private DynamicTrackedRace getExistingTrackedRace(Regatta regatta, RaceDefinition race) {
        return getOrCreateTrackedRegatta(regatta).getExistingTrackedRace(race);
    }

    @Override
    public DynamicTrackedRegatta getOrCreateTrackedRegatta(Regatta regatta) {
        cacheAndReplicateDefaultRegatta(regatta);
        synchronized (regattaTrackingCache) {
            DynamicTrackedRegatta result = regattaTrackingCache.get(regatta);
            if (result == null) {
                logger.info("Creating DynamicTrackedRegattaImpl for regatta " + regatta.getName() + " with hashCode "
                        + regatta.hashCode());
                result = new DynamicTrackedRegattaImpl(regatta);
                replicate(new TrackRegatta(regatta.getRegattaIdentifier()));
                regattaTrackingCache.put(regatta, result);
                ensureRegattaIsObservedForDefaultLeaderboardAndAutoLeaderboardLinking(result);
            }
            return result;
        }
    }

    @Override
    public DynamicTrackedRegatta getTrackedRegatta(com.sap.sailing.domain.base.Regatta regatta) {
        return regattaTrackingCache.get(regatta);
    }

    @Override
    public void removeTrackedRegatta(Regatta regatta) {
        logger.info("Removing regatta " + regatta.getName() + " from regattaTrackingCache");
        DynamicTrackedRegatta trackedRegatta = regattaTrackingCache.remove(regatta);
        stopObservingRegattaForRedaultLeaderboardAndAutoLeaderboardLinking(trackedRegatta);
    }

    @Override
    public Regatta getRegatta(RegattaName regattaName) {
        return (Regatta) regattasByName.get(regattaName.getRegattaName());
    }

    @Override
    public Regatta getRegatta(RegattaIdentifier regattaIdentifier) {
        return (Regatta) regattaIdentifier.getRegatta(this);
    }

    @Override
    public DynamicTrackedRace getTrackedRace(RegattaAndRaceIdentifier raceIdentifier) {
    	DynamicTrackedRace result = null;
        Regatta regatta = regattasByName.get(raceIdentifier.getRegattaName());
        if (regatta != null) {
            DynamicTrackedRegatta trackedRegatta = regattaTrackingCache.get(regatta);
            if (trackedRegatta != null) {
                RaceDefinition race = getRace(raceIdentifier);
                if (race != null) {
                    result = trackedRegatta.getTrackedRace(race);
                }
            }
        }
        return result;
    }

    @Override
    public DynamicTrackedRace getExistingTrackedRace(RegattaAndRaceIdentifier raceIdentifier) {
        Regatta regatta = getRegattaByName(raceIdentifier.getRegattaName());
        DynamicTrackedRace trackedRace = null;
        if (regatta != null) {
            RaceDefinition race = regatta.getRaceByName(raceIdentifier.getRaceName());
            trackedRace = getOrCreateTrackedRegatta(regatta).getExistingTrackedRace(race);
        }
        return trackedRace;
    }

    @Override
    public RaceDefinition getRace(RegattaAndRaceIdentifier regattaNameAndRaceName) {
        RaceDefinition result = null;
        Regatta regatta = getRegatta(regattaNameAndRaceName);
        if (regatta != null) {
            result = regatta.getRaceByName(regattaNameAndRaceName.getRaceName());
        }
        return result;
    }

    @Override
    public Map<String, LeaderboardGroup> getLeaderboardGroups() {
        synchronized (leaderboardGroupsByName) {
            return Collections.unmodifiableMap(new HashMap<String, LeaderboardGroup>(leaderboardGroupsByName));
        }
    }

    @Override
    public LeaderboardGroup getLeaderboardGroupByName(String groupName) {
        synchronized (leaderboardGroupsByName) {
            return leaderboardGroupsByName.get(groupName);
        }
    }

    @Override
    public LeaderboardGroup addLeaderboardGroup(String groupName, String description,
            boolean displayGroupsInReverseOrder, List<String> leaderboardNames,
            int[] overallLeaderboardDiscardThresholds, ScoringSchemeType overallLeaderboardScoringSchemeType) {
        ArrayList<Leaderboard> leaderboards = new ArrayList<>();
        synchronized (leaderboardsByName) {
            for (String leaderboardName : leaderboardNames) {
                Leaderboard leaderboard = leaderboardsByName.get(leaderboardName);
                if (leaderboard == null) {
                    throw new IllegalArgumentException("No leaderboard with name " + leaderboardName + " found");
                } else {
                    leaderboards.add(leaderboard);
                }
            }
        }
        LeaderboardGroup result = new LeaderboardGroupImpl(groupName, description, displayGroupsInReverseOrder,
                leaderboards);
        if (overallLeaderboardScoringSchemeType != null) {
            // create overall leaderboard and its discards settings
            addOverallLeaderboardToLeaderboardGroup(result,
                    getBaseDomainFactory().createScoringScheme(overallLeaderboardScoringSchemeType),
                    overallLeaderboardDiscardThresholds);
        }
        synchronized (leaderboardGroupsByName) {
            if (leaderboardGroupsByName.containsKey(groupName)) {
                throw new IllegalArgumentException("Leaderboard group with name " + groupName + " already exists");
            }
            leaderboardGroupsByName.put(groupName, result);
        }
        mongoObjectFactory.storeLeaderboardGroup(result);
        return result;
    }

    @Override
    public void removeLeaderboardGroup(String groupName) {
        final LeaderboardGroup leaderboardGroup;
        synchronized (leaderboardGroupsByName) {
            leaderboardGroup = leaderboardGroupsByName.remove(groupName);
        }
        mongoObjectFactory.removeLeaderboardGroup(groupName);
        if (leaderboardGroup != null && leaderboardGroup.getOverallLeaderboard() != null) {
            removeLeaderboard(leaderboardGroup.getOverallLeaderboard().getName());
        }
    }

    @Override
    public void renameLeaderboardGroup(String oldName, String newName) {
        synchronized (leaderboardGroupsByName) {
            if (!leaderboardGroupsByName.containsKey(oldName)) {
                throw new IllegalArgumentException("No leaderboard group with name " + oldName + " found");
            }
            if (leaderboardGroupsByName.containsKey(newName)) {
                throw new IllegalArgumentException("Leaderboard group with name " + newName + " already exists");
            }
            LeaderboardGroup toRename = leaderboardGroupsByName.remove(oldName);
            toRename.setName(newName);
            leaderboardGroupsByName.put(newName, toRename);
            mongoObjectFactory.renameLeaderboardGroup(oldName, newName);
        }
    }

    @Override
    public void updateLeaderboardGroup(String oldName, String newName, String description,
            List<String> leaderboardNames, int[] overallLeaderboardDiscardThresholds,
            ScoringSchemeType overallLeaderboardScoringSchemeType) {
        if (!oldName.equals(newName)) {
            renameLeaderboardGroup(oldName, newName);
        }
        LeaderboardGroup group = getLeaderboardGroupByName(newName);
        if (!description.equals(group.getDescription())) {
            group.setDescriptiom(description);
        }
        group.clearLeaderboards();
        for (String leaderboardName : leaderboardNames) {
            Leaderboard leaderboard = getLeaderboardByName(leaderboardName);
            if (leaderboard != null) {
                group.addLeaderboard(leaderboard);
            }
        }
        Leaderboard overallLeaderboard = group.getOverallLeaderboard();
        if (overallLeaderboard != null) {
            if (overallLeaderboardScoringSchemeType == null) {
                group.setOverallLeaderboard(null);
                removeLeaderboard(overallLeaderboard.getName());
            } else {
                // update existing overall leaderboard's discards settings; scoring scheme cannot be updated in-place
                overallLeaderboard.setCrossLeaderboardResultDiscardingRule(new ThresholdBasedResultDiscardingRuleImpl(
                        overallLeaderboardDiscardThresholds));
                updateStoredLeaderboard(overallLeaderboard);
            }
        } else if (overallLeaderboard == null && overallLeaderboardScoringSchemeType != null) {
            addOverallLeaderboardToLeaderboardGroup(group,
                    getBaseDomainFactory().createScoringScheme(overallLeaderboardScoringSchemeType),
                    overallLeaderboardDiscardThresholds);
        }
        updateStoredLeaderboardGroup(group);
    }

    private void addOverallLeaderboardToLeaderboardGroup(LeaderboardGroup leaderboardGroup,
            ScoringScheme scoringScheme, int[] discardThresholds) {
        Leaderboard overallLeaderboard = new LeaderboardGroupMetaLeaderboard(leaderboardGroup, scoringScheme,
                new ThresholdBasedResultDiscardingRuleImpl(discardThresholds));
        leaderboardGroup.setOverallLeaderboard(overallLeaderboard);
        addLeaderboard(overallLeaderboard);
        updateStoredLeaderboard(overallLeaderboard);
    }

    @Override
    public void updateStoredLeaderboardGroup(LeaderboardGroup leaderboardGroup) {
        mongoObjectFactory.storeLeaderboardGroup(leaderboardGroup);
    }

    private ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    /**
     * Currently, the operation is executed by immediately {@link Operation#internalApplyTo(Object) applying} it to this
     * service object.
     * <p>
     * 
     * Future implementations of this method will need to also replicate the effects of the operation to all replica of
     * this service known.
     */
    @Override
    public <T> T apply(RacingEventServiceOperation<T> operation) {
        try {
            T result = operation.internalApplyTo(this);
            replicate(operation);
            return result;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "apply", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> void replicate(RacingEventServiceOperation<T> operation) {
        for (OperationExecutionListener listener : operationExecutionListeners.keySet()) {
            try {
                listener.executed(operation);
            } catch (Exception e) {
                // don't risk the master's operation only because replication to a listener/replica doesn't work
                logger.severe("Error replicating operation " + operation + " to replication listener " + listener);
                logger.log(Level.SEVERE, "replicate", e);
            }
        }
    }

    @Override
    public void addOperationExecutionListener(OperationExecutionListener listener) {
        operationExecutionListeners.put(listener, listener);
    }

    @Override
    public void removeOperationExecutionListener(OperationExecutionListener listener) {
        operationExecutionListeners.remove(listener);
    }

    @Override
    public void serializeForInitialReplication(ObjectOutputStream oos) throws IOException {
        StringBuffer logoutput = new StringBuffer();

        logger.info("Serializing events...");
        oos.writeObject(eventsById);
        logoutput.append("\nSerialized " + eventsById.size() + " events\n");
        for (Event event : eventsById.values()) {
            logoutput.append(String.format("%3s\n", event.toString()));
        }

        logger.info("Serializing regattas...");
        oos.writeObject(regattasByName);
        logoutput.append("Serialized " + regattasByName.size() + " regattas\n");
        for (Regatta regatta : regattasByName.values()) {
            logoutput.append(String.format("%3s\n", regatta.toString()));
        }

        logger.info("Serializing regattas observed...");
        oos.writeObject(regattasObservedForDefaultLeaderboard);
        logger.info("Serializing regatta tracking cache...");
        oos.writeObject(regattaTrackingCache);
        logger.info("Serializing leaderboard groups...");
        oos.writeObject(leaderboardGroupsByName);
        logoutput.append("Serialized " + leaderboardGroupsByName.size() + " leaderboard groups\n");
        for (LeaderboardGroup lg : leaderboardGroupsByName.values()) {
            logoutput.append(String.format("%3s\n", lg.toString()));
        }
        logger.info("Serializing leaderboards...");
        oos.writeObject(leaderboardsByName);
        logoutput.append("Serialized " + leaderboardsByName.size() + " leaderboards\n");
        for (Leaderboard lg : leaderboardsByName.values()) {
            logoutput.append(String.format("%3s\n", lg.toString()));
        }
        logger.info("Serializing media library...");
        mediaLibrary.serialize(oos);
        logoutput.append("Serialized " + mediaLibrary.allTracks().size() + " media tracks\n");
        for (MediaTrack lg : mediaLibrary.allTracks()) {
            logoutput.append(String.format("%3s\n", lg.toString()));
        }
        logger.info("Serializing persisted competitors...");
        oos.writeObject(competitorStore);
        logoutput.append("Serialized " + competitorStore.size() + " persisted competitors\n");
        logger.info(logoutput.toString());
        
        logger.info("Serializing configuration map...");
        oos.writeObject(configurationMap);
        logoutput.append("Serialized " + configurationMap.size() + " configuration entries\n");
        for (DeviceConfigurationMatcher matcher : configurationMap.keySet()) {
            logoutput.append(String.format("%3s\n", matcher.toString()));
        }
        
        logger.info(logoutput.toString());    }

    @SuppressWarnings("unchecked")
    // the type-parameters in the casts of the de-serialized collection objects can't be checked
    @Override
    public void initiallyFillFrom(ObjectInputStream ois) throws IOException, ClassNotFoundException,
            InterruptedException {
        logger.info("Performing initial replication load on " + this);
        ClassLoader oldContextClassloader = Thread.currentThread().getContextClassLoader();
        try {
            // Use this object's class's class loader as the context class loader which will then be used for
            // de-serialization; this will cause all classes to be visible that this bundle
            // (com.sap.sailing.server) can see
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            regattasByName.clear();
            regattasObservedForDefaultLeaderboard.clear();

            if (raceTrackersByRegatta != null && !raceTrackersByRegatta.isEmpty()) {
                for (DynamicTrackedRegatta regatta : regattaTrackingCache.values()) {
                    for (RaceTracker tracker : raceTrackersByRegatta.get(regatta)) {
                        tracker.stop();
                    }
                }
            }

            logger.info("Clearing all data structures...");
            regattaTrackingCache.clear();
            leaderboardGroupsByName.clear();
            leaderboardsByName.clear();
            eventsById.clear();
            mediaLibrary.clear();
            competitorStore.clear();

            StringBuffer logoutput = new StringBuffer();

            logger.info("Reading all events...");
            eventsById.putAll((Map<Serializable, Event>) ois.readObject());
            logoutput.append("\nReceived " + eventsById.size() + " NEW events\n");
            for (Event event : eventsById.values()) {
                logoutput.append(String.format("%3s\n", event.toString()));
            }

            logger.info("Reading all regattas...");
            regattasByName.putAll((Map<String, Regatta>) ois.readObject());
            logoutput.append("Received " + regattasByName.size() + " NEW regattas\n");
            for (Regatta regatta : regattasByName.values()) {
                logoutput.append(String.format("%3s\n", regatta.toString()));
            }

            // it is important that the leaderboards and tracked regattas are cleared before auto-linking to
            // old leaderboards takes place which then don't match the new ones
            logger.info("Reading all dynamic tracked regattas...");
            for (DynamicTrackedRegatta trackedRegattaToObserve : (Set<DynamicTrackedRegatta>) ois.readObject()) {
                ensureRegattaIsObservedForDefaultLeaderboardAndAutoLeaderboardLinking(trackedRegattaToObserve);
            }

            logger.info("Reading all of the regatta tracking cache...");
            regattaTrackingCache.putAll((Map<Regatta, DynamicTrackedRegatta>) ois.readObject());
            logoutput.append("Received " + regattaTrackingCache.size() + " NEW regatta tracking cache entries\n");

            logger.info("Reading leaderboard groups...");
            leaderboardGroupsByName.putAll((Map<String, LeaderboardGroup>) ois.readObject());
            logoutput.append("Received " + leaderboardGroupsByName.size() + " NEW leaderboard groups\n");
            for (LeaderboardGroup lg : leaderboardGroupsByName.values()) {
                logoutput.append(String.format("%3s\n", lg.toString()));
            }

            logger.info("Reading leaderboards by name...");
            leaderboardsByName.putAll((Map<String, Leaderboard>) ois.readObject());
            logoutput.append("Received " + leaderboardsByName.size() + " NEW leaderboards\n");
            for (Leaderboard leaderboard : leaderboardsByName.values()) {
                logoutput.append(String.format("%3s\n", leaderboard.toString()));
            }

            // now fix ScoreCorrectionListener setup for LeaderboardGroupMetaLeaderboard instances:
            for (Leaderboard leaderboard : leaderboardsByName.values()) {
                if (leaderboard instanceof LeaderboardGroupMetaLeaderboard) {
                    ((LeaderboardGroupMetaLeaderboard) leaderboard)
                            .registerAsScoreCorrectionChangeForwarderAndRaceColumnListenerOnAllLeaderboards();
                }
            }

            logger.info("Reading media library...");
            mediaLibrary.deserialize(ois);
            logoutput.append("Received " + mediaLibrary.allTracks().size() + " NEW media tracks\n");
            for (MediaTrack mediatrack : mediaLibrary.allTracks()) {
                logoutput.append(String.format("%3s\n", mediatrack.toString()));
            }
            

            // only copy the competitors from the deserialized competitor store; don't use it because it will have set
            // a default Mongo object factory
            logger.info("Reading competitors...");
            for (Competitor competitor : ((CompetitorStore) ois.readObject()).getCompetitors()) {
                DynamicCompetitor dynamicCompetitor = (DynamicCompetitor) competitor;
                competitorStore.getOrCreateCompetitor(dynamicCompetitor.getId(), dynamicCompetitor.getName(), dynamicCompetitor.getColor(), dynamicCompetitor.getTeam(), dynamicCompetitor.getBoat());
            }
            logoutput.append("Received " + competitorStore.size() + " NEW competitors\n");

            logger.info("Reading device configurations...");
            configurationMap.putAll((DeviceConfigurationMapImpl) ois.readObject());
            logoutput.append("Received " + configurationMap.size() + " NEW configuration entries\n");
            for (DeviceConfigurationMatcher matcher : configurationMap.keySet()) {
                logoutput.append(String.format("%3s\n", matcher.toString()));
            }
            logger.info(logoutput.toString());
        } finally {
            Thread.currentThread().setContextClassLoader(oldContextClassloader);
        }
    }

    @Override
    public Event addEvent(String eventName, TimePoint startDate, TimePoint endDate, String venue, boolean isPublic, UUID id) {
        synchronized (eventsById) {
            Event result = createEventWithoutReplication(eventName, startDate, endDate, venue, isPublic, id);
            replicate(new CreateEvent(eventName, startDate, endDate, venue, isPublic, id));
            return result;
        }
    }

    @Override
    public Event createEventWithoutReplication(String eventName, TimePoint startDate, TimePoint endDate, String venue, boolean isPublic, UUID id) {
        Event result = new EventImpl(eventName, startDate, endDate, venue, isPublic, id);
        synchronized (eventsById) {
            if (eventsById.containsKey(result.getId())) {
                throw new IllegalArgumentException("Event with ID " + result.getId()
                        + " already exists which is pretty surprising...");
            }
            eventsById.put(result.getId(), result);
        }
        mongoObjectFactory.storeEvent(result);
        return result;
    }

    @Override
    public void updateEvent(UUID id, String eventName, TimePoint startDate, TimePoint endDate, String venueName,
            boolean isPublic, List<String> regattaNames) {
        synchronized (eventsById) {
            if (!eventsById.containsKey(id)) {
                throw new IllegalArgumentException("Sailing event with ID " + id + " does not exist.");
            }
            Event event = eventsById.get(id);
            event.setName(eventName);
            event.setStartDate(startDate);
            event.setEndDate(endDate);
            event.setPublic(isPublic);
            event.getVenue().setName(venueName);

            // TODO need to update regattas if they are once linked to event objects
            mongoObjectFactory.storeEvent(event);

            replicate(new UpdateEvent(id, eventName, startDate, endDate, venueName, isPublic, regattaNames));
        }
    }

    @Override
    public void renameEvent(UUID id, String newName) {
        synchronized (eventsById) {
            if (!eventsById.containsKey(id)) {
                throw new IllegalArgumentException("No sailing event with ID " + id + " found.");
            }
            Event toRename = eventsById.get(id);
            toRename.setName(newName);
            mongoObjectFactory.renameEvent(id, newName);

            replicate(new RenameEvent(id, newName));
        }
    }

    @Override
    public void removeEvent(UUID id) {
        removeEventFromEventsById(id);
        mongoObjectFactory.removeEvent(id);
        replicate(new RemoveEvent(id));
    }

    protected void removeEventFromEventsById(Serializable id) {
        synchronized (eventsById) {
            eventsById.remove(id);
        }
    }

    @Override
    public Regatta getRememberedRegattaForRace(Serializable raceID) {
        return persistentRegattasForRaceIDs.get(raceID.toString());
    }

    /**
     * Persistently remembers the association of the race with its {@link RaceDefinition#getId()} to the
     * <code>regatta</code> with its {@link Regatta#getRegattaIdentifier() identifier} so that the next time
     * {@link #getRememberedRegattaForRace(RaceDefinition)} is called with <code>race</code> as argument,
     * <code>regatta</code> will be returned.
     */
    private void setRegattaForRace(Regatta regatta, RaceDefinition race) {
        setRegattaForRace(regatta, race.getId().toString());
    }

    @Override
    public void setRegattaForRace(Regatta regatta, String raceIdAsString) {
        persistentRegattasForRaceIDs.put(raceIdAsString, regatta);
        mongoObjectFactory.storeRegattaForRaceID(raceIdAsString, regatta);
    }

    @Override
    public CourseArea addCourseArea(UUID eventId, String courseAreaName, UUID courseAreaId) {
        CourseArea courseArea = getBaseDomainFactory().getOrCreateCourseArea(courseAreaId, courseAreaName);
        synchronized (eventsById) {
            addCourseAreaWithoutReplication(eventId, courseAreaId, courseAreaName);
            replicate(new AddCourseArea(eventId, courseAreaName, courseAreaId));
        }
        return courseArea;
    }

    @Override
    public CourseArea addCourseAreaWithoutReplication(UUID eventId, UUID courseAreaId, String courseAreaName) {
        final CourseArea courseArea = getBaseDomainFactory().getOrCreateCourseArea(courseAreaId, courseAreaName);
        synchronized (eventsById) {
            if (!eventsById.containsKey(eventId)) {
                throw new IllegalArgumentException("No sailing event with ID " + eventId + " found.");
            }
            Event event = eventsById.get(eventId);
            event.getVenue().addCourseArea(courseArea);
            mongoObjectFactory.storeEvent(event);
            return courseArea;
        }
    }

    @Override
    public void mediaTrackAdded(MediaTrack mediaTrack) {
        String mimeType = mediaTrack.mimeType != null ? mediaTrack.mimeType.name() : null;
        if (mediaTrack.dbId == null) {
            mediaTrack.dbId = mediaDB.insertMediaTrack(mediaTrack.title, mediaTrack.url, mediaTrack.startTime,
                    mediaTrack.durationInMillis, mimeType);
        }
        mediaLibrary.addMediaTrack(mediaTrack);
        replicate(new AddMediaTrackOperation(mediaTrack));
    }

    @Override
    public void mediaTracksAdded(Collection<MediaTrack> mediaTracks) {
        mediaLibrary.addMediaTracks(mediaTracks);
    }

    @Override
    public void mediaTrackTitleChanged(MediaTrack mediaTrack) {
        mediaDB.updateTitle(mediaTrack.dbId, mediaTrack.title);
        mediaLibrary.titleChanged(mediaTrack);
        replicate(new UpdateMediaTrackTitleOperation(mediaTrack));
    }

    @Override
    public void mediaTrackUrlChanged(MediaTrack mediaTrack) {
        mediaDB.updateUrl(mediaTrack.dbId, mediaTrack.url);
        mediaLibrary.urlChanged(mediaTrack);
        replicate(new UpdateMediaTrackUrlOperation(mediaTrack));
    }

    @Override
    public void mediaTrackStartTimeChanged(MediaTrack mediaTrack) {
        mediaDB.updateStartTime(mediaTrack.dbId, mediaTrack.startTime);
        mediaLibrary.startTimeChanged(mediaTrack);
        replicate(new UpdateMediaTrackStartTimeOperation(mediaTrack));
    }

    @Override
    public void mediaTrackDurationChanged(MediaTrack mediaTrack) {
        mediaDB.updateDuration(mediaTrack.dbId, mediaTrack.durationInMillis);
        mediaLibrary.durationChanged(mediaTrack);
        replicate(new UpdateMediaTrackDurationOperation(mediaTrack));
    }

    @Override
    public void mediaTrackDeleted(MediaTrack mediaTrack) {
        mediaDB.deleteMediaTrack(mediaTrack.dbId);
        mediaLibrary.deleteMediaTrack(mediaTrack);
        replicate(new RemoveMediaTrackOperation(mediaTrack));
    }

    @Override
    public void mediaTracksImported(Collection<MediaTrack> mediaTracksToImport, boolean override) {
        for (MediaTrack trackToImport : mediaTracksToImport) {
            MediaTrack existingTrack = mediaLibrary.lookupMediaTrack(trackToImport);
            if (existingTrack == null) {
                mediaDB.insertMediaTrackWithId(trackToImport.dbId, trackToImport.title, trackToImport.url, trackToImport.startTime, trackToImport.durationInMillis, trackToImport.mimeType.name());
                mediaTrackAdded(trackToImport);
            } else if (override) {
                    
                // Using fine-grained update methods.
                // Rationale: Changes on more than one track property are rare 
                //            and don't justify the introduction of a new set 
                //            of methods (including replication).
                if (!Util.equalsWithNull(existingTrack.title, trackToImport.title)) {
                    existingTrack.title = trackToImport.title;
                    mediaTrackTitleChanged(existingTrack);
                }
                if (!Util.equalsWithNull(existingTrack.url, trackToImport.url)) {
                    existingTrack.url = trackToImport.url;
                    mediaTrackUrlChanged(existingTrack);
                }
                if (!Util.equalsWithNull(existingTrack.startTime, trackToImport.startTime)) {
                    existingTrack.startTime = trackToImport.startTime;
                    mediaTrackStartTimeChanged(existingTrack);
                }
                if (existingTrack.durationInMillis != trackToImport.durationInMillis) {
                    existingTrack.durationInMillis = trackToImport.durationInMillis;
                    mediaTrackDurationChanged(existingTrack);
                }
            }
        }
    }
    
    @Override
    public Collection<MediaTrack> getMediaTracksForRace(RegattaAndRaceIdentifier regattaAndRaceIdentifier) {
        TrackedRace trackedRace = getExistingTrackedRace(regattaAndRaceIdentifier);
        if (trackedRace != null) {
            Date raceStart = trackedRace.getStartOfRace() == null ? null : trackedRace.getStartOfRace().asDate();
            Date raceEnd = trackedRace.getEndOfRace() == null ? null : trackedRace.getEndOfRace().asDate();
            return mediaLibrary.findMediaTracksInTimeRange(raceStart, raceEnd);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public Collection<MediaTrack> getAllMediaTracks() {
        return mediaLibrary.allTracks();
    }

    public String toString() {
        return "RacingEventService: " + this.hashCode() + " Build: " + BuildVersion.getBuildVersion();
    }

    @Override
    public void reloadRaceLog(String leaderboardName, String raceColumnName, String fleetName) {
        Leaderboard leaderboard = getLeaderboardByName(leaderboardName);
        if (leaderboard != null) {
            RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
            if (raceColumn != null) {
                Fleet fleetImpl = raceColumn.getFleetByName(fleetName);
                RaceLog racelog = raceColumn.getRaceLog(fleetImpl);
                if (racelog != null) {
                    raceColumn.setOrReloadRaceLogInformation(raceColumn.getRaceLogInformation(), fleetImpl);
                    logger.info("Reloaded race log for fleet " + fleetImpl + " for race column " + raceColumn.getName()
                            + " for leaderboard " + leaderboard.getName());
                }
            }
        }
    }

    @Override
    public ConcurrentHashMap<String, Regatta> getPersistentRegattasForRaceIDs() {
        return persistentRegattasForRaceIDs;
    }


    @Override
    public WindStore getWindStore() {
        return windStore;
    }
    @Override
    public DeviceConfiguration getDeviceConfiguration(DeviceConfigurationIdentifier identifier) {
        return configurationMap.getByMatch(identifier);
    }

    @Override
    public void createOrUpdateDeviceConfiguration(DeviceConfigurationMatcher matcher, DeviceConfiguration configuration) {
        synchronized (configurationMap) {
            configurationMap.put(matcher, configuration);
        }
        mongoObjectFactory.storeDeviceConfiguration(matcher, configuration);
        replicate(new CreateOrUpdateDeviceConfiguration(matcher, configuration));
    }

    @Override
    public void removeDeviceConfiguration(DeviceConfigurationMatcher matcher) {
        synchronized (configurationMap) {
            configurationMap.remove(matcher);
        }
        mongoObjectFactory.removeDeviceConfiguration(matcher);
        replicate(new RemoveDeviceConfiguration(matcher));
    }

    @Override
    public Map<DeviceConfigurationMatcher, DeviceConfiguration> getAllDeviceConfigurations() {
        return new HashMap<DeviceConfigurationMatcher, DeviceConfiguration>(configurationMap);
    }
    
    @Override
    public TimePoint setStartTimeAndProcedure(String leaderboardName, String raceColumnName, String fleetName, String authorName,
            int authorPriority, int passId, TimePoint logicalTimePoint, TimePoint startTime, RacingProcedureType racingProcedure) {
        RaceLog raceLog = getRaceLog(leaderboardName, raceColumnName, fleetName);
        if (raceLog != null) {
            RaceState state = RaceStateImpl.create(raceLog, new RaceLogEventAuthorImpl(authorName, authorPriority));
            state.setRacingProcedure(logicalTimePoint, racingProcedure);
            state.forceNewStartTime(logicalTimePoint, startTime);
            return state.getStartTime();
        }
        return null;
    }
    
    public RaceLog getRaceLog(String leaderboardName, String raceColumnName, String fleetName) {
        Leaderboard leaderboard = getLeaderboardByName(leaderboardName);
        if (leaderboard != null) {
            RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
            if (raceColumn != null) {
                Fleet fleetImpl = raceColumn.getFleetByName(fleetName);
                return raceColumn.getRaceLog(fleetImpl);
            }
        }
        return null;
    }

    @Override
    public Triple<TimePoint, Integer, RacingProcedureType> getStartTimeAndProcedure(String leaderboardName, String raceColumnName, String fleetName) {
        RaceLog raceLog = getRaceLog(leaderboardName, raceColumnName, fleetName);
        if (raceLog != null) {
            ReadonlyRaceState state = ReadonlyRaceStateImpl.create(raceLog);
            return new Triple<TimePoint, Integer, RacingProcedureType>(state.getStartTime(), raceLog.getCurrentPassId(), state.getRacingProcedure().getType());
        }
        return null;
    }

    private Iterable<WindTrackerFactory> getWindTrackerFactories() {
        final Set<WindTrackerFactory> result;
        if (bundleContext == null) {
            result = Collections.singleton((WindTrackerFactory) ExpeditionWindTrackerFactory.getInstance());
        } else {
            ServiceTracker<WindTrackerFactory, WindTrackerFactory> tracker = new ServiceTracker<WindTrackerFactory, WindTrackerFactory>(
                    bundleContext, WindTrackerFactory.class.getName(), null);
            tracker.open();
            result = new HashSet<>();
            for (WindTrackerFactory factory : tracker.getServices(new WindTrackerFactory[0])) {
                result.add(factory);
            }
        }
        return result;
    }

    @Override
    public DataImportLockWithProgress getDataImportLock() {
        return dataImportLock;
    }

    @Override
    public DataImportProgress createOrUpdateDataImportProgressWithReplication(UUID importOperationId,
            double overallProgressPct, String subProgressName, double subProgressPct) {
        // Create/Update locally
        DataImportProgress progress = createOrUpdateDataImportProgressWithoutReplication(importOperationId,
                overallProgressPct, subProgressName, subProgressPct);
        // Create/Update on replicas
        replicate(new CreateOrUpdateDataImportProgress(importOperationId, overallProgressPct, subProgressName,
                subProgressPct));
        return progress;
    }

    @Override
    public DataImportProgress createOrUpdateDataImportProgressWithoutReplication(UUID importOperationId,
            double overallProgressPct, String subProgressName, double subProgressPct) {
        DataImportProgress progress = dataImportLock.getProgress(importOperationId);
        boolean newObject = false;
        if (progress == null) {
            progress = new DataImportProgressImpl(importOperationId);
            newObject = true;
        }
        progress.setOverAllProgressPct(overallProgressPct);
        progress.setNameOfCurrentSubProgress(subProgressName);
        progress.setCurrentSubProgressPct(subProgressPct);
        if (newObject) {
            dataImportLock.addProgress(importOperationId, progress);
        }
        return progress;
    }

    @Override
    public void setDataImportFailedWithoutReplication(UUID importOperationId, String errorMessage) {
        DataImportProgress progress = dataImportLock.getProgress(importOperationId);
        if (progress != null) {
            progress.setFailed();
            progress.setErrorMessage(errorMessage);
        }
    }

    @Override
    public void setDataImportFailedWithReplication(UUID importOperationId, String errorMessage) {
        setDataImportFailedWithoutReplication(importOperationId, errorMessage);
        replicate(new DataImportFailed(importOperationId, errorMessage));
    }

    @Override
    public void setDataImportDeleteProgressFromMapTimerWithReplication(UUID importOperationId) {
        setDataImportDeleteProgressFromMapTimerWithoutReplication(importOperationId);
        replicate(new SetDataImportDeleteProgressFromMapTimer(importOperationId));
    }

    @Override
    public void setDataImportDeleteProgressFromMapTimerWithoutReplication(UUID importOperationId) {
        dataImportLock.setDeleteFromMapTimer(importOperationId);
    }
}
