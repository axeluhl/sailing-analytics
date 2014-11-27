package com.sap.sailing.server.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.LogEventAuthorImpl;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.state.RaceState;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.RaceStateImpl;
import com.sap.sailing.domain.abstractlog.race.state.impl.ReadonlyRaceStateImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorStore;
import com.sap.sailing.domain.base.CompetitorStore.CompetitorUpdateListener;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.LeaderboardSearchResult;
import com.sap.sailing.domain.base.LeaderboardSearchResultBase;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnInSeries;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.RegattaListener;
import com.sap.sailing.domain.base.RemoteSailingServerReference;
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
import com.sap.sailing.domain.base.impl.RemoteSailingServerReferenceImpl;
import com.sap.sailing.domain.common.DataImportProgress;
import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.common.Renamable;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RegattaCreationParametersDTO;
import com.sap.sailing.domain.common.dto.SeriesCreationParametersDTO;
import com.sap.sailing.domain.common.impl.DataImportProgressImpl;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.common.racelog.tracking.TypeBasedServiceFinderFactory;
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
import com.sap.sailing.domain.persistence.MongoWindStore;
import com.sap.sailing.domain.persistence.MongoWindStoreFactory;
import com.sap.sailing.domain.persistence.PersistenceFactory;
import com.sap.sailing.domain.persistence.media.MediaDB;
import com.sap.sailing.domain.persistence.media.MediaDBFactory;
import com.sap.sailing.domain.persistence.racelog.tracking.MongoGPSFixStoreFactory;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.racelog.tracking.GPSFixStore;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.RaceChangeListener;
import com.sap.sailing.domain.tracking.RaceHandle;
import com.sap.sailing.domain.tracking.RaceListener;
import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sailing.domain.tracking.RaceTrackingConnectivityParameters;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRaceStatus;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tracking.WindTracker;
import com.sap.sailing.domain.tracking.WindTrackerFactory;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRegattaImpl;
import com.sap.sailing.expeditionconnector.ExpeditionWindTrackerFactory;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;
import com.sap.sailing.server.Replicator;
import com.sap.sailing.server.gateway.deserialization.impl.CourseAreaJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.EventBaseJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.LeaderboardGroupBaseJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.LeaderboardSearchResultBaseJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.VenueJsonDeserializer;
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
import com.sap.sailing.server.operationaltransformation.RecordMarkGPSFixForExistingTrack;
import com.sap.sailing.server.operationaltransformation.RecordMarkGPSFixForNewMarkTrack;
import com.sap.sailing.server.operationaltransformation.RecordWindFix;
import com.sap.sailing.server.operationaltransformation.RemoveDeviceConfiguration;
import com.sap.sailing.server.operationaltransformation.RemoveEvent;
import com.sap.sailing.server.operationaltransformation.RemoveMediaTrackOperation;
import com.sap.sailing.server.operationaltransformation.RemoveWindFix;
import com.sap.sailing.server.operationaltransformation.RenameEvent;
import com.sap.sailing.server.operationaltransformation.SetDataImportDeleteProgressFromMapTimer;
import com.sap.sailing.server.operationaltransformation.TrackRegatta;
import com.sap.sailing.server.operationaltransformation.UpdateCompetitor;
import com.sap.sailing.server.operationaltransformation.UpdateEndOfTracking;
import com.sap.sailing.server.operationaltransformation.UpdateMarkPassings;
import com.sap.sailing.server.operationaltransformation.UpdateMediaTrackDurationOperation;
import com.sap.sailing.server.operationaltransformation.UpdateMediaTrackRacesOperation;
import com.sap.sailing.server.operationaltransformation.UpdateMediaTrackStartTimeOperation;
import com.sap.sailing.server.operationaltransformation.UpdateMediaTrackTitleOperation;
import com.sap.sailing.server.operationaltransformation.UpdateMediaTrackUrlOperation;
import com.sap.sailing.server.operationaltransformation.UpdateRaceDelayToLive;
import com.sap.sailing.server.operationaltransformation.UpdateStartOfTracking;
import com.sap.sailing.server.operationaltransformation.UpdateStartTimeReceived;
import com.sap.sailing.server.operationaltransformation.UpdateTrackedRaceStatus;
import com.sap.sailing.server.operationaltransformation.UpdateWindAveragingTime;
import com.sap.sailing.server.operationaltransformation.UpdateWindSourcesToExclude;
import com.sap.sailing.server.test.support.RacingEventServiceWithTestSupport;
import com.sap.sailing.util.impl.LockUtil;
import com.sap.sailing.util.impl.NamedReentrantReadWriteLock;
import com.sap.sse.BuildVersion;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.search.KeywordQuery;
import com.sap.sse.common.search.Result;
import com.sap.sse.common.search.ResultImpl;
import com.sap.sse.operationaltransformation.Operation;
import com.sap.sse.replication.OperationExecutionListener;
import com.sap.sse.replication.OperationWithResult;

public class RacingEventServiceImpl implements RacingEventServiceWithTestSupport, RegattaListener, LeaderboardRegistry,
        Replicator {
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
    private final ConcurrentHashMap<Serializable, Event> eventsById;

    private final RemoteSailingServerSet remoteSailingServerSet;

    /**
     * Holds the {@link Regatta} objects for those races registered with this service. Note that there may be
     * {@link Regatta} objects that exist outside this service for regattas not (yet) registered here.
     */
    protected final ConcurrentHashMap<String, Regatta> regattasByName;

    private final NamedReentrantReadWriteLock regattasByNameLock;

    private final ConcurrentHashMap<RaceDefinition, CourseChangeReplicator> courseListeners;

    protected final ConcurrentHashMap<Regatta, Set<RaceTracker>> raceTrackersByRegatta;

    /**
     * Although {@link #raceTrackersByRegatta} is a concurrent hash map, entering sets as values needs to be
     * synchronized using this lock's write lock to avoid two value sets overwriting each other.
     */
    private final NamedReentrantReadWriteLock raceTrackersByRegattaLock;

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
     * Fetching or creating and putting a lock to this map happens in ({@link #getOrCreateRaceTrackersByIdLock}) which
     * takes care of managing concurrent access to this concurrent map. When done, the
     * {@link #addRace(RegattaIdentifier, RaceTrackingConnectivityParameters, long)} method cleans up by removing the
     * lock again from this map, again using a synchronized method (
     * {@link #unlockRaceTrackersById(Object, NamedReentrantReadWriteLock)}).
     */
    private final ConcurrentHashMap<Object, NamedReentrantReadWriteLock> raceTrackersByIDLocks;

    /**
     * Leaderboards managed by this racing event service
     */
    private final ConcurrentHashMap<String, Leaderboard> leaderboardsByName;

    /**
     * {@link #leaderboardsByName} is already a concurrent hash map; however, when renaming a leaderboard, this shall
     * happen as an atomic transaction, not interruptible by other write accesses on the same map because otherwise
     * assumptions made during the rename process wouldn't hold. See, in particular,
     * {@link #renameLeaderboard(String, String)}.
     */
    private final NamedReentrantReadWriteLock leaderboardsByNameLock;

    private final ConcurrentHashMap<String, LeaderboardGroup> leaderboardGroupsByName;

    private final ConcurrentHashMap<UUID, LeaderboardGroup> leaderboardGroupsByID;

    /**
     * See {@link #leaderboardsByNameLock}
     */
    private final NamedReentrantReadWriteLock leaderboardGroupsByNameLock;

    private final CompetitorStore competitorStore;

    /**
     * A set based on a concurrent hash map, therefore being thread safe
     */
    private Set<DynamicTrackedRegatta> regattasObservedForDefaultLeaderboard = Collections
            .newSetFromMap(new ConcurrentHashMap<DynamicTrackedRegatta, Boolean>());

    private final MongoObjectFactory mongoObjectFactory;

    private final DomainObjectFactory domainObjectFactory;

    private final ConcurrentHashMap<Regatta, DynamicTrackedRegatta> regattaTrackingCache;

    /**
     * Protects write access transactions that do a previous read to {@link #regattaTrackingCache}; read-only access is
     * already synchronized by using a concurrent hash map for {@link #regattaTrackingCache}.
     */
    private final NamedReentrantReadWriteLock regattaTrackingCacheLock;

    private final ConcurrentHashMap<OperationExecutionListener<RacingEventService>, OperationExecutionListener<RacingEventService>> operationExecutionListeners;

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
     * Currently valid pairs of {@link DeviceConfigurationMatcher}s and {@link DeviceConfiguration}s. The contents of
     * this map is persisted and replicated. See {@link DeviceConfigurationMapImpl}.
     */
    protected final DeviceConfigurationMapImpl configurationMap;

    private final WindStore windStore;
    private final GPSFixStore gpsFixStore;

    /**
     * This author should be used for server generated race log events
     */
    private final AbstractLogEventAuthor raceLogEventAuthorForServer = new LogEventAuthorImpl(
            RacingEventService.class.getName(), 0);

    /**
     * Allow only one master data import at a time to avoid situation where multiple Imports override each other in
     * unpredictable fashion
     */
    private final DataImportLockWithProgress dataImportLock;

    /**
     * If this service runs in the context of an OSGi environment, the activator should {@link #setBundleContext set the
     * bundle context} on this object so that service lookups become possible.
     */
    private BundleContext bundleContext;

    private TypeBasedServiceFinderFactory serviceFinderFactory;

    /**
     * Constructs a {@link DomainFactory base domain factory} that uses this object's {@link #competitorStore competitor
     * store} for competitor management. This base domain factory is then also used for the construction of the
     * {@link DomainObjectFactory}. This constructor variant initially clears the persistent competitor collection,
     * hence removes all previously persistent competitors. This is the default for testing and for backward
     * compatibility with prior releases that did not support a persistent competitor collection.
     */
    public RacingEventServiceImpl() {
        this(true, null);
    }

    public RacingEventServiceImpl(WindStore windStore, GPSFixStore gpsFixStore,
            TypeBasedServiceFinderFactory serviceFinderFactory) {
        this(true, windStore, gpsFixStore, serviceFinderFactory);
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
    public RacingEventServiceImpl(boolean clearPersistentCompetitorStore,
            TypeBasedServiceFinderFactory serviceFinderFactory) {
        this(new PersistentCompetitorStore(
                PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory(serviceFinderFactory),
                clearPersistentCompetitorStore, serviceFinderFactory), null, null, serviceFinderFactory);
    }

    private RacingEventServiceImpl(boolean clearPersistentCompetitorStore, WindStore windStore,
            GPSFixStore gpsFixStore, TypeBasedServiceFinderFactory serviceFinderFactory) {
        this(new PersistentCompetitorStore(
                PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory(serviceFinderFactory),
                clearPersistentCompetitorStore, serviceFinderFactory), windStore, gpsFixStore, serviceFinderFactory);
    }

    private RacingEventServiceImpl(PersistentCompetitorStore persistentCompetitorStore, WindStore windStore,
            GPSFixStore gpsFixStore, TypeBasedServiceFinderFactory serviceFinderFactory) {
        this(persistentCompetitorStore.getDomainObjectFactory(), persistentCompetitorStore.getMongoObjectFactory(),
                persistentCompetitorStore.getBaseDomainFactory(), MediaDBFactory.INSTANCE.getDefaultMediaDB(),
                persistentCompetitorStore, windStore, gpsFixStore, serviceFinderFactory);
    }

    /**
     * Uses the default factories for the tracking adapters and the {@link DomainFactory base domain factory} of the
     * {@link PersistenceFactory#getDefaultDomainObjectFactory() default domain object factory}. This constructor should
     * be used for testing because it provides a transient {@link CompetitorStore} as required for competitor
     * persistence.
     */
    public RacingEventServiceImpl(DomainObjectFactory domainObjectFactory, MongoObjectFactory mongoObjectFactory,
            MediaDB mediaDB, WindStore windStore, GPSFixStore gpsFixStore) {
        this(domainObjectFactory, mongoObjectFactory, domainObjectFactory.getBaseDomainFactory(), mediaDB,
                domainObjectFactory.getBaseDomainFactory().getCompetitorStore(), windStore, gpsFixStore, null);
    }

    /**
     * @param windStore
     *            if <code>null</code>, a default {@link MongoWindStore} will be used, based on the persistence set-up
     *            of this service
     * @param serviceFinderFactory
     *            used to find the services handling specific types of tracking devices, such as the persistent storage
     *            of {@link DeviceIdentifier}s of specific device types or the managing of the device-to-competitor
     *            associations per race tracked.
     */
    private RacingEventServiceImpl(DomainObjectFactory domainObjectFactory, MongoObjectFactory mongoObjectFactory,
            com.sap.sailing.domain.base.DomainFactory baseDomainFactory, MediaDB mediaDb,
            CompetitorStore competitorStore, WindStore windStore, GPSFixStore gpsFixStore,
            TypeBasedServiceFinderFactory serviceFinderFactory) {
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
        this.competitorStore.addCompetitorUpdateListener(new CompetitorUpdateListener() {
            @Override
            public void competitorUpdated(Competitor competitor) {
                replicate(new UpdateCompetitor(competitor.getId().toString(), competitor.getName(), competitor
                        .getColor(), competitor.getBoat().getSailID(), competitor.getTeam().getNationality()));
            }
        });
        this.windStore = windStore;
        this.dataImportLock = new DataImportLockWithProgress();

        remoteSailingServerSet = new RemoteSailingServerSet(scheduler);
        regattasByName = new ConcurrentHashMap<String, Regatta>();
        regattasByNameLock = new NamedReentrantReadWriteLock("regattasByName for " + this, /* fair */false);
        eventsById = new ConcurrentHashMap<Serializable, Event>();
        regattaTrackingCache = new ConcurrentHashMap<>();
        regattaTrackingCacheLock = new NamedReentrantReadWriteLock("regattaTrackingCache for " + this, /* fair */false);
        raceTrackersByRegatta = new ConcurrentHashMap<>();
        raceTrackersByRegattaLock = new NamedReentrantReadWriteLock("raceTrackersByRegatta for " + this, /* fair */
        false);
        raceTrackersByID = new ConcurrentHashMap<>();
        raceTrackersByIDLocks = new ConcurrentHashMap<>();
        leaderboardGroupsByName = new ConcurrentHashMap<>();
        leaderboardGroupsByID = new ConcurrentHashMap<>();
        leaderboardGroupsByNameLock = new NamedReentrantReadWriteLock("leaderboardGroupsByName for " + this, /* fair */
        false);
        leaderboardsByName = new ConcurrentHashMap<String, Leaderboard>();
        leaderboardsByNameLock = new NamedReentrantReadWriteLock("leaderboardsByName for " + this, /* fair */false);
        operationExecutionListeners = new ConcurrentHashMap<>();
        courseListeners = new ConcurrentHashMap<>();
        persistentRegattasForRaceIDs = new ConcurrentHashMap<>();
        this.raceLogReplicator = new RaceLogReplicator(this);
        this.raceLogScoringReplicator = new RaceLogScoringReplicator(this);
        this.mediaLibrary = new MediaLibrary();
        if (gpsFixStore == null) {
            try {
                gpsFixStore = MongoGPSFixStoreFactory.INSTANCE.getMongoGPSFixStore(mongoObjectFactory,
                        domainObjectFactory, serviceFinderFactory);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        this.gpsFixStore = gpsFixStore;
        this.configurationMap = new DeviceConfigurationMapImpl();
        this.serviceFinderFactory = serviceFinderFactory;

        // Add one default leaderboard that aggregates all races currently tracked by this service.
        // This is more for debugging purposes than for anything else.
        addFlexibleLeaderboard(LeaderboardNameConstants.DEFAULT_LEADERBOARD_NAME, null, new int[] { 5, 8 },
                getBaseDomainFactory().createScoringScheme(ScoringSchemeType.LOW_POINT), null);
        loadStoredEvents();
        loadStoredRegattas();
        loadRaceIDToRegattaAssociations();
        loadStoredLeaderboardsAndGroups();
        loadLinksFromEventsToLeaderboardGroups();
        loadMediaLibary();
        loadStoredDeviceConfigurations();
        loadAllRemoteSailingServersAndSchedulePeriodicEventCacheRefresh();
    }

    @Override
    public void clearState() throws Exception {
        for (String leaderboardGroupName : new ArrayList<>(this.leaderboardGroupsByName.keySet())) {
            removeLeaderboardGroup(leaderboardGroupName);
        }
        for (String leaderboardName : new ArrayList<>(this.leaderboardsByName.keySet())) {
            removeLeaderboard(leaderboardName);
        }
        for (Regatta regatta : new ArrayList<>(this.regattasByName.values())) {
            stopTracking(regatta);
            removeRegatta(regatta);
        }
        for (Event event : new ArrayList<>(this.eventsById.values())) {
            removeEvent(event.getId());
        }
        for (MediaTrack mediaTrack : this.mediaLibrary.allTracks()) {
            mediaTrackDeleted(mediaTrack);
        }
        // TODO clear user store? See bug 2430.
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
        LockUtil.lockForWrite(regattasByNameLock);
        try {
            for (Regatta regatta : domainObjectFactory.loadAllRegattas(this)) {
                logger.info("putting regatta " + regatta.getName() + " (" + regatta.hashCode()
                        + ") into regattasByName");
                regattasByName.put(regatta.getName(), regatta);
                regatta.addRegattaListener(this);
                regatta.addRaceColumnListener(raceLogReplicator);
                regatta.addRaceColumnListener(raceLogScoringReplicator);
            }
        } finally {
            LockUtil.unlockAfterWrite(regattasByNameLock);
        }
    }

    private void loadStoredEvents() {
        for (Event event : domainObjectFactory.loadAllEvents()) {
            if (event.getId() != null)
                eventsById.put(event.getId(), event);
        }
    }

    private void loadLinksFromEventsToLeaderboardGroups() {
        domainObjectFactory.loadLeaderboardGroupLinksForEvents(/* eventResolver */this, /* leaderboardGroupResolver */
                this);
    }

    private void loadAllRemoteSailingServersAndSchedulePeriodicEventCacheRefresh() {
        for (RemoteSailingServerReference sailingServer : domainObjectFactory.loadAllRemoteSailingServerReferences()) {
            remoteSailingServerSet.add(sailingServer);
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
        Collection<MediaTrack> allDbMediaTracks = mediaDB.loadAllMediaTracks();
        mediaTracksAdded(allDbMediaTracks);
    }

    private void loadStoredDeviceConfigurations() {
        for (Entry<DeviceConfigurationMatcher, DeviceConfiguration> entry : domainObjectFactory
                .loadAllDeviceConfigurations()) {
            configurationMap.put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void addLeaderboard(Leaderboard leaderboard) {
        LockUtil.lockForWrite(leaderboardsByNameLock);
        try {
            leaderboardsByName.put(leaderboard.getName(), leaderboard);
        } finally {
            LockUtil.unlockAfterWrite(leaderboardsByNameLock);
        }
        // RaceColumns of RegattaLeaderboards are tracked via its Regatta!
        if (leaderboard instanceof FlexibleLeaderboard) {
            leaderboard.addRaceColumnListener(raceLogReplicator);
            leaderboard.addRaceColumnListener(raceLogScoringReplicator);
        }
    }

    private void loadStoredLeaderboardsAndGroups() {
        logger.info("loading stored leaderboards and groups");
        // Loading all leaderboard groups and the contained leaderboards
        for (LeaderboardGroup leaderboardGroup : domainObjectFactory.getAllLeaderboardGroups(this, this)) {
            logger.info("loaded leaderboard group " + leaderboardGroup.getName() + " into " + this);
            LockUtil.lockForWrite(leaderboardGroupsByNameLock);
            try {
                leaderboardGroupsByName.put(leaderboardGroup.getName(), leaderboardGroup);
                leaderboardGroupsByID.put(leaderboardGroup.getId(), leaderboardGroup);
            } finally {
                LockUtil.unlockAfterWrite(leaderboardGroupsByNameLock);
            }
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
                new ThresholdBasedResultDiscardingRuleImpl(discardThresholds), scoringScheme, courseArea);
        result.setDisplayName(leaderboardDisplayName);
        if (getLeaderboardByName(leaderboardName) != null) {
            throw new IllegalArgumentException("Leaderboard with name " + leaderboardName + " already exists");
        }
        addLeaderboard(result);
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
            if (getLeaderboardByName(result.getName()) != null) {
                throw new IllegalArgumentException("Leaderboard with name " + result.getName() + " already exists in "
                        + this);
            }
            addLeaderboard(result);
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
                RaceColumn result = ((FlexibleLeaderboard) leaderboard).addRaceColumn(columnName, medalRace);
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
     * When a race column is renamed, its race log identifiers change. Therefore, the race logs need to be stored under
     * the new identifier again to be consistent with the in-memory image again.
     */
    private void storeRaceLogs(RaceColumn raceColumn) {
        for (Fleet fleet : raceColumn.getFleets()) {
            RaceLogIdentifier identifier = raceColumn.getRaceLogIdentifier(fleet);
            RaceLogEventVisitor storeVisitor = MongoRaceLogStoreFactory.INSTANCE.getMongoRaceLogStoreVisitor(
                    identifier, getMongoObjectFactory());
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
        final Leaderboard toRename = leaderboardsByName.get(oldName);
        LockUtil.lockForWrite(leaderboardsByNameLock);
        try {
            if (toRename == null) {
                throw new IllegalArgumentException("No leaderboard with name " + oldName + " found");
            }
            if (leaderboardsByName.containsKey(newName)) {
                throw new IllegalArgumentException("Leaderboard with name " + newName + " already exists");
            }
            if (toRename instanceof Renamable) {
                ((Renamable) toRename).setName(newName);
                leaderboardsByName.remove(oldName);
                leaderboardsByName.put(newName, toRename);
            } else {
                throw new IllegalArgumentException("Leaderboard with name " + newName + " is of type "
                        + toRename.getClass().getSimpleName() + " and therefore cannot be renamed");
            }
        } finally {
            LockUtil.unlockAfterWrite(leaderboardsByNameLock);
        }
        // don't need the lock anymore to update DB
        if (toRename instanceof Renamable) {
            mongoObjectFactory.renameLeaderboard(oldName, newName);
            syncGroupsAfterLeaderboardChange(toRename, true);
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
        LockUtil.lockForWrite(leaderboardsByNameLock);
        try {
            return leaderboardsByName.remove(leaderboardName);
        } finally {
            LockUtil.unlockAfterWrite(leaderboardsByNameLock);
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

    @Override
    public Leaderboard getLeaderboardByName(String name) {
        return leaderboardsByName.get(name);
    }

    @Override
    public Map<String, Leaderboard> getLeaderboards() {
        return Collections.unmodifiableMap(new HashMap<String, Leaderboard>(leaderboardsByName));
    }

    @Override
    public Map<RemoteSailingServerReference, com.sap.sse.common.Util.Pair<Iterable<EventBase>, Exception>> getPublicEventsOfAllSailingServers() {
        return remoteSailingServerSet.getCachedEventsForRemoteSailingServers(); // FIXME should probably add our own
                                                                                // stuff here... Is it enough to pass on
                                                                                // the remote reference URL to the
                                                                                // client for leaderboard group URL
                                                                                // construction?
    }

    @Override
    public RemoteSailingServerReference addRemoteSailingServerReference(String name, URL url) {
        RemoteSailingServerReference result = new RemoteSailingServerReferenceImpl(name, url);
        remoteSailingServerSet.add(result);
        mongoObjectFactory.storeSailingServer(result);
        return result;
    }

    @Override
    public Iterable<RemoteSailingServerReference> getLiveRemoteServerReferences() {
        return remoteSailingServerSet.getLiveRemoteServerReferences();
    }

    @Override
    public RemoteSailingServerReference getRemoteServerReferenceByName(String remoteServerReferenceName) {
        return remoteSailingServerSet.getServerReferenceByName(remoteServerReferenceName);
    }

    @Override
    public com.sap.sse.common.Util.Pair<Iterable<EventBase>, Exception> updateRemoteServerEventCacheSynchronously(
            RemoteSailingServerReference ref) {
        return remoteSailingServerSet.getEventsOrException(ref);
    }

    @Override
    public void removeRemoteSailingServerReference(String name) {
        remoteSailingServerSet.remove(name);
        mongoObjectFactory.removeSailingServer(name);
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
    public boolean isRaceBeingTracked(Regatta regattaContext, RaceDefinition r) {
        Set<RaceTracker> trackers = raceTrackersByRegatta.get(regattaContext);
        if (trackers != null) {
            for (RaceTracker tracker : trackers) {
                final Set<RaceDefinition> races = tracker.getRaces();
                if (races != null && races.contains(r)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Regatta getRegattaByName(String name) {
        return name == null ? null : regattasByName.get(name);
    }

    @Override
    public Regatta getOrCreateDefaultRegatta(String name, String boatClassName, Serializable id) {
        Regatta result = regattasByName.get(name);
        if (result == null) {
            RaceLogStore raceLogStore = MongoRaceLogStoreFactory.INSTANCE.getMongoRaceLogStore(mongoObjectFactory,
                    domainObjectFactory);
            result = new RegattaImpl(raceLogStore, name, getBaseDomainFactory().getOrCreateBoatClass(boatClassName),
                    this, getBaseDomainFactory().createScoringScheme(ScoringSchemeType.LOW_POINT), id, null);
            logger.info("Created default regatta " + result.getName() + " (" + hashCode() + ") on " + this);
            cacheAndReplicateDefaultRegatta(result);
        }
        return result;
    }

    @Override
    public Regatta createRegatta(String fullRegattaName, String boatClassName, Serializable id,
            Iterable<? extends Series> series, boolean persistent, ScoringScheme scoringScheme,
            Serializable defaultCourseAreaId, boolean useStartTimeInference) {
        com.sap.sse.common.Util.Pair<Regatta, Boolean> regattaWithCreatedFlag = getOrCreateRegattaWithoutReplication(
                fullRegattaName, boatClassName, id, series, persistent, scoringScheme, defaultCourseAreaId,
                useStartTimeInference);
        Regatta regatta = regattaWithCreatedFlag.getA();
        if (regattaWithCreatedFlag.getB()) {
            replicateSpecificRegattaWithoutRaceColumns(regatta);
        }
        return regatta;
    }

    @Override
    public void addRegattaWithoutReplication(Regatta regatta) {
        UUID defaultCourseAreaId = null;
        if (regatta.getDefaultCourseArea() != null) {
            defaultCourseAreaId = regatta.getDefaultCourseArea().getId();
        }
        boolean wasAdded = addAndConnectRegatta(regatta.isPersistent(), defaultCourseAreaId, regatta);
        if (!wasAdded) {
            logger.info("Regatta with name " + regatta.getName() + " already existed, so it hasn't been added.");
        }
    }

    @Override
    public com.sap.sse.common.Util.Pair<Regatta, Boolean> getOrCreateRegattaWithoutReplication(String fullRegattaName,
            String boatClassName, Serializable id, Iterable<? extends Series> series, boolean persistent,
            ScoringScheme scoringScheme, Serializable defaultCourseAreaId, boolean useStartTimeInference) {
        RaceLogStore raceLogStore = MongoRaceLogStoreFactory.INSTANCE.getMongoRaceLogStore(mongoObjectFactory,
                domainObjectFactory);
        CourseArea courseArea = getCourseArea(defaultCourseAreaId);
        Regatta regatta = new RegattaImpl(raceLogStore, fullRegattaName, getBaseDomainFactory().getOrCreateBoatClass(
                boatClassName), series, persistent, scoringScheme, id, courseArea, useStartTimeInference);
        boolean wasCreated = addAndConnectRegatta(persistent, defaultCourseAreaId, regatta);
        if (wasCreated) {
            logger.info("Created regatta " + regatta.getName() + " (" + hashCode() + ") on " + this);
        }
        return new com.sap.sse.common.Util.Pair<Regatta, Boolean>(regatta, wasCreated);
    }

    private boolean addAndConnectRegatta(boolean persistent, Serializable defaultCourseAreaId, Regatta regatta) {
        boolean wasCreated = false;
        // try a quick read protected by the concurrent hash map implementation
        if (!regattasByName.containsKey(regatta.getName())) {
            LockUtil.lockForWrite(regattasByNameLock);
            try {
                // check again, now that we hold the exclusive write lock
                if (!regattasByName.containsKey(regatta.getName())) {
                    wasCreated = true;
                    logger.info("putting regatta " + regatta.getName() + " (" + regatta.hashCode()
                            + ") into regattasByName of " + this);
                    regattasByName.put(regatta.getName(), regatta);
                    regatta.addRegattaListener(this);
                    regatta.addRaceColumnListener(raceLogReplicator);
                    regatta.addRaceColumnListener(raceLogScoringReplicator);
                }
            } finally {
                LockUtil.unlockAfterWrite(regattasByNameLock);
            }
        }
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
        return wasCreated;
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
                lock = new NamedReentrantReadWriteLock("raceTrackersByIDLock for " + trackerId, /* fair */false);
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
    private void unlockRaceTrackersById(Object trackerId, NamedReentrantReadWriteLock lock) {
        LockUtil.unlockAfterWrite(lock);
        synchronized (raceTrackersByIDLocks) {
            raceTrackersByIDLocks.remove(trackerId);
        }
    }

    @Override
    public RaceHandle addRace(RegattaIdentifier regattaToAddTo, RaceTrackingConnectivityParameters params,
            long timeoutInMilliseconds) throws Exception {
        final Object trackerID = params.getTrackerID();
        NamedReentrantReadWriteLock raceTrackersByIdLock = lockRaceTrackersById(trackerID);
        try {
            RaceTracker tracker = raceTrackersByID.get(trackerID);
            if (tracker == null) {
                Regatta regatta = regattaToAddTo == null ? null : getRegatta(regattaToAddTo);
                if (regatta == null) {
                    // create tracker and use an existing or create a default regatta
                    tracker = params.createRaceTracker(this, windStore, gpsFixStore);
                } else {
                    // use the regatta selected by the RaceIdentifier regattaToAddTo
                    tracker = params.createRaceTracker(regatta, this, windStore, gpsFixStore);
                    assert tracker.getRegatta() == regatta;
                }
                LockUtil.lockForWrite(raceTrackersByRegattaLock);
                try {
                    raceTrackersByID.put(trackerID, tracker);
                    Set<RaceTracker> trackers = raceTrackersByRegatta.get(tracker.getRegatta());
                    if (trackers == null) {
                        trackers = Collections.newSetFromMap(new ConcurrentHashMap<RaceTracker, Boolean>());
                        raceTrackersByRegatta.put(tracker.getRegatta(), trackers);
                    }
                    trackers.add(tracker);
                } finally {
                    LockUtil.unlockAfterWrite(raceTrackersByRegattaLock);
                }
                // TODO we assume here that the regatta name is unique which necessitates adding the boat class name to
                // it in RegattaImpl constructor
                String regattaName = tracker.getRegatta().getName();
                Regatta regattaWithName = regattasByName.get(regattaName);
                // TODO we assume here that the regatta name is unique which necessitates adding the boat class name to
                // it in RegattaImpl constructor
                if (regattaWithName != null) {
                    if (regattaWithName != tracker.getRegatta()) {
                        if (Util.isEmpty(regattaWithName.getAllRaces())) {
                            // probably, tracker removed the last races from the old regatta and created a new one
                            LockUtil.lockForWrite(regattasByNameLock);
                            try {
                                regattasByName.remove(regattaName);
                                cacheAndReplicateDefaultRegatta(tracker.getRegatta());
                            } finally {
                                LockUtil.unlockAfterWrite(regattasByNameLock);
                            }
                        } else {
                            throw new RuntimeException("Internal error. Two regatta objects with equal name "
                                    + regattaName);
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
                GPSFixStore existingTrackersGPSFixStore = tracker.getGPSFixStore();
                if (!existingTrackersGPSFixStore.equals(gpsFixStore)) {
                    logger.warning("GPSFix store mismatch. Requested GPSFix store: " + gpsFixStore
                            + ". GPSFix store in use by existing tracker: " + existingTrackersGPSFixStore);
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
     * The regatta and all its contained {@link Regatta#getAllRaces() races} are replicated to all replicas.
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
        replicate(new AddSpecificRegatta(regatta.getName(), regatta.getBoatClass() == null ? null : regatta
                .getBoatClass().getName(), regatta.getId(),
                getSeriesWithoutRaceColumnsConstructionParametersAsMap(regatta), regatta.isPersistent(),
                regatta.getScoringScheme(), courseAreaId, regatta.useStartTimeInference()));
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
                            : s.getResultDiscardingRule().getDiscardIndexResultsStartingWithHowManyRaces(), s
                            .hasSplitFleetContiguousScoring()));
        }
        return new RegattaCreationParametersDTO(result);
    }

    /**
     * If <code>regatta</code> is not yet in {@link #regattasByName}, it is added, this service is
     * {@link Regatta#addRegattaListener(RegattaListener) added} as regatta listener, and the regatta and all its
     * contained {@link Regatta#getAllRaces() races} are replicated to all replica.
     */
    private void cacheAndReplicateDefaultRegatta(Regatta regatta) {
        // try a quick read first, protected by regattasByName being a concurrent hash set
        if (!regattasByName.containsKey(regatta.getName())) {
            // now we need to obtain exclusive write access; in between, some other thread may have added a regatta by
            // that
            // name, so we need to check again:
            LockUtil.lockForWrite(regattasByNameLock);
            try {
                if (!regattasByName.containsKey(regatta.getName())) {
                    logger.info("putting regatta " + regatta.getName() + " (" + regatta.hashCode()
                            + ") into regattasByName of " + this);
                    regattasByName.put(regatta.getName(), regatta);
                    regatta.addRegattaListener(this);
                    regatta.addRaceColumnListener(raceLogReplicator);
                    regatta.addRaceColumnListener(raceLogScoringReplicator);

                    replicate(new AddDefaultRegatta(regatta.getName(), regatta.getBoatClass() == null ? null : regatta
                            .getBoatClass().getName(), regatta.getId()));
                    RegattaIdentifier regattaIdentifier = regatta.getRegattaIdentifier();
                    for (RaceDefinition race : regatta.getAllRaces()) {
                        replicate(new AddRaceDefinition(regattaIdentifier, race));
                    }
                }
            } finally {
                LockUtil.unlockAfterWrite(regattasByNameLock);
            }
        }
    }

    @Override
    public DynamicTrackedRace createTrackedRace(RegattaAndRaceIdentifier raceIdentifier, WindStore windStore,
            GPSFixStore gpsFixStore, long delayToLiveInMillis, long millisecondsOverWhichToAverageWind,
            long millisecondsOverWhichToAverageSpeed) {
        DynamicTrackedRegatta trackedRegatta = getOrCreateTrackedRegatta(getRegatta(raceIdentifier));
        RaceDefinition race = getRace(raceIdentifier);
        return trackedRegatta.createTrackedRace(race, Collections.<Sideline> emptyList(), windStore, gpsFixStore,
                delayToLiveInMillis, millisecondsOverWhichToAverageWind, millisecondsOverWhichToAverageSpeed,
                /* raceDefinitionSetToUpdate */null);
    }

    private void ensureRegattaIsObservedForDefaultLeaderboardAndAutoLeaderboardLinking(
            DynamicTrackedRegatta trackedRegatta) {
        if (regattasObservedForDefaultLeaderboard.add(trackedRegatta)) {
            trackedRegatta.addRaceListener(new RaceAdditionListener());
        }
    }

    private void stopObservingRegattaForRedaultLeaderboardAndAutoLeaderboardLinking(DynamicTrackedRegatta trackedRegatta) {
        regattasObservedForDefaultLeaderboard.remove(trackedRegatta);
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
                    trackedRace.getGPSFixStore(), trackedRace.getDelayToLiveInMillis(),
                    trackedRace.getMillisecondsOverWhichToAverageWind(),
                    trackedRace.getMillisecondsOverWhichToAverageSpeed());
            replicate(op);
            linkRaceToConfiguredLeaderboardColumns(trackedRace);
            final FlexibleLeaderboard defaultLeaderboard = (FlexibleLeaderboard) leaderboardsByName
                    .get(LeaderboardNameConstants.DEFAULT_LEADERBOARD_NAME);
            if (defaultLeaderboard != null) {
                String columnName = trackedRace.getRace().getName();
                defaultLeaderboard.addRace(trackedRace, columnName, /* medalRace */false);
            }
            TrackedRaceReplicator trackedRaceReplicator = new TrackedRaceReplicator(trackedRace);
            trackedRaceReplicators.put(trackedRace, trackedRaceReplicator);
            trackedRace.addListener(trackedRaceReplicator, /* fire wind already loaded */true, true);
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
        public void startOfTrackingChanged(TimePoint startOfTracking) {
            replicate(new UpdateStartOfTracking(getRaceIdentifier(), startOfTracking));            
        }

        @Override
        public void endOfTrackingChanged(TimePoint endOfTracking) {
            replicate(new UpdateEndOfTracking(getRaceIdentifier(), endOfTracking));            
        }

        @Override
        public void startTimeReceivedChanged(TimePoint startTimeReceived) {
            replicate(new UpdateStartTimeReceived(getRaceIdentifier(), startTimeReceived));            
        }

        @Override
        public void startOfRaceChanged(TimePoint oldStartOfRace, TimePoint newStartOfRace) {
            // no action required; the update signaled by this call is implicit; for explicit updates
            // see raceTimesChanged(TimePoint, TimePoint, TimePoint).
        }

        @Override
        public void waypointAdded(int zeroBasedIndex, Waypoint waypointThatGotAdded) {
            // no-op; the course change is replicated by the separate CourseChangeReplicator
        }

        @Override
        public void waypointRemoved(int zeroBasedIndex, Waypoint waypointThatGotRemoved) {
            // no-op; the course change is replicated by the separate CourseChangeReplicator
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
        public void markPositionChanged(GPSFix fix, Mark mark, boolean firstInTrack) {
            final RecordMarkGPSFix operation;
            if (firstInTrack) {
                operation = new RecordMarkGPSFixForNewMarkTrack(getRaceIdentifier(), mark, fix);
            } else {
                operation = new RecordMarkGPSFixForExistingTrack(getRaceIdentifier(), mark, fix);
            }
            replicate(operation);
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
        final Set<RaceTracker> trackersForRegatta = raceTrackersByRegatta.get(regatta);
        if (trackersForRegatta != null) {
            for (RaceTracker raceTracker : trackersForRegatta) {
                final Set<RaceDefinition> races = raceTracker.getRaces();
                if (races != null) {
                    for (RaceDefinition race : races) {
                        stopTrackingWind(regatta, race);
                    }
                }
                raceTracker.stop();
                final Object trackerId = raceTracker.getID();
                final NamedReentrantReadWriteLock lock = lockRaceTrackersById(trackerId);
                try {
                    raceTrackersByID.remove(trackerId);
                } finally {
                    unlockRaceTrackersById(trackerId, lock);
                }
                raceTrackersByID.remove(trackerId);
            }
            LockUtil.lockForWrite(raceTrackersByRegattaLock);
            try {
                raceTrackersByRegatta.remove(regatta);
            } finally {
                LockUtil.unlockAfterWrite(raceTrackersByRegattaLock);
            }
        }
    }

    @Override
    public void stopTrackingAndRemove(Regatta regatta) throws MalformedURLException, IOException, InterruptedException {
        stopTracking(regatta);
        if (regatta != null) {
            if (regatta.getName() != null) {
                logger.info("Removing regatta " + regatta.getName() + " (" + regatta.hashCode() + ") from " + this);
                LockUtil.lockForWrite(regattasByNameLock);
                try {
                    regattasByName.remove(regatta.getName());
                } finally {
                    LockUtil.unlockAfterWrite(regattasByNameLock);
                }
                LockUtil.lockForWrite(regattaTrackingCacheLock);
                try {
                    regattaTrackingCache.remove(regatta);
                } finally {
                    LockUtil.unlockAfterWrite(regattaTrackingCacheLock);
                }
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
                        Set<RaceTracker> trackersForRegatta = raceTrackersByRegatta.get(regatta);
                        if (trackersForRegatta != null) {
                            trackersForRegatta.remove(tracker);
                        }
                        tracker.stop();
                        final Object trackerId = tracker.getID();
                        final NamedReentrantReadWriteLock lock = lockRaceTrackersById(trackerId);
                        try {
                            raceTrackersByID.remove(trackerId);
                        } finally {
                            unlockRaceTrackersById(trackerId, lock);
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
        final Set<RaceTracker> trackerSet = raceTrackersByRegatta.get(regatta);
        if (trackerSet != null) {
            Iterator<RaceTracker> trackerIter = trackerSet.iterator();
            while (trackerIter.hasNext()) {
                RaceTracker raceTracker = trackerIter.next();
                if (raceTracker.getRaces() != null && raceTracker.getRaces().contains(race)) {
                    logger.info("Found tracker to stop for races " + raceTracker.getRaces());
                    raceTracker.stop();
                    trackerIter.remove();
                    final Object trackerId = raceTracker.getID();
                    final NamedReentrantReadWriteLock lock = lockRaceTrackersById(trackerId);
                    try {
                        raceTrackersByID.remove(trackerId);
                    } finally {
                        unlockRaceTrackersById(trackerId, lock);
                    }
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
        LockUtil.lockForWrite(regattasByNameLock);
        try {
            regattasByName.remove(regatta.getName());
        } finally {
            LockUtil.unlockAfterWrite(regattasByNameLock);
        }
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
            RegattaConfiguration newRegattaConfiguration, Iterable<? extends Series> series,
            boolean useStartTimeInference) {
        // We're not doing any renaming of the regatta itself, therefore we don't have to sync on the maps.
        Regatta regatta = getRegatta(regattaIdentifier);
        CourseArea newCourseArea = getCourseArea(newDefaultCourseAreaId);
        if (newCourseArea != regatta.getDefaultCourseArea()) {
            regatta.setDefaultCourseArea(newCourseArea);
        }
        if (regatta.useStartTimeInference() != useStartTimeInference) {
            regatta.setUseStartTimeInference(useStartTimeInference);
            if (getTrackedRegatta(regatta) != null) {
                for (DynamicTrackedRace trackedRace : getTrackedRegatta(regatta).getTrackedRaces()) {
                    // the start times of the regatta's tracked races now have to be re-evaluated the next time they are
                    // queried
                    trackedRace.invalidateStartTime();
                }
            }
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
        if (regatta.isPersistent()) {
            mongoObjectFactory.storeRegatta(regatta);
        }
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
                if (leaderboard instanceof FlexibleLeaderboard) { // RegattaLeaderboards have implicitly been updated by
                                                                  // the code above
                    for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
                        for (Fleet fleet : raceColumn.getFleets()) {
                            if (raceColumn.getTrackedRace(fleet) == trackedRace) {
                                raceColumn.releaseTrackedRace(fleet); // but leave the RaceIdentifier on the race column
                                                                      // untouched, e.g., for later re-load
                            }
                        }
                    }
                }
            }
        }
        // remove the race from the (default) regatta if the regatta is not persistently stored
        regatta.removeRace(race);
        if (!regatta.isPersistent() && Util.isEmpty(regatta.getAllRaces())) {
            logger.info("Removing regatta " + regatta.getName() + " (" + regatta.hashCode() + ") from service " + this);
            LockUtil.lockForWrite(regattasByNameLock);
            try {
                regattasByName.remove(regatta.getName());
            } finally {
                LockUtil.unlockAfterWrite(regattasByNameLock);
            }
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
                        final Object trackerId = raceTracker.getID();
                        final NamedReentrantReadWriteLock lock = lockRaceTrackersById(trackerId);
                        try {
                            raceTrackersByID.remove(trackerId);
                        } finally {
                            unlockRaceTrackersById(trackerId, lock);
                        }
                        // if the last tracked race was removed, remove the entire regatta
                        if (raceTrackersByRegatta.get(regatta).isEmpty()) {
                            stopTracking(regatta);
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
    public Iterable<com.sap.sse.common.Util.Triple<Regatta, RaceDefinition, String>> getWindTrackedRaces() {
        List<com.sap.sse.common.Util.Triple<Regatta, RaceDefinition, String>> result = new ArrayList<com.sap.sse.common.Util.Triple<Regatta, RaceDefinition, String>>();
        for (Regatta regatta : getAllRegattas()) {
            for (RaceDefinition race : regatta.getAllRaces()) {
                for (WindTrackerFactory windTrackerFactory : getWindTrackerFactories()) {
                    WindTracker windTracker = windTrackerFactory.getExistingWindTracker(race);
                    if (windTracker != null) {
                        result.add(new com.sap.sse.common.Util.Triple<Regatta, RaceDefinition, String>(regatta, race,
                                windTracker.toString()));
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
        LockUtil.lockForWrite(regattaTrackingCacheLock);
        try {
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
        } finally {
            LockUtil.unlockAfterWrite(regattaTrackingCacheLock);
        }
    }

    @Override
    public DynamicTrackedRegatta getTrackedRegatta(com.sap.sailing.domain.base.Regatta regatta) {
        return regattaTrackingCache.get(regatta);
    }

    @Override
    public void removeTrackedRegatta(Regatta regatta) {
        logger.info("Removing regatta " + regatta.getName() + " from regattaTrackingCache");
        final DynamicTrackedRegatta trackedRegatta;
        LockUtil.lockForWrite(regattaTrackingCacheLock);
        try {
            trackedRegatta = regattaTrackingCache.remove(regatta);
        } finally {
            LockUtil.unlockAfterWrite(regattaTrackingCacheLock);
        }
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
        return Collections.unmodifiableMap(new HashMap<String, LeaderboardGroup>(leaderboardGroupsByName));
    }

    @Override
    public LeaderboardGroup getLeaderboardGroupByName(String groupName) {
        return leaderboardGroupsByName.get(groupName);
    }

    @Override
    public LeaderboardGroup getLeaderboardGroupByID(UUID leaderboardGroupID) {
        return leaderboardGroupsByID.get(leaderboardGroupID);
    }

    @Override
    public LeaderboardGroup addLeaderboardGroup(UUID id, String groupName, String description, String displayName,
            boolean displayGroupsInReverseOrder, List<String> leaderboardNames,
            int[] overallLeaderboardDiscardThresholds, ScoringSchemeType overallLeaderboardScoringSchemeType) {
        ArrayList<Leaderboard> leaderboards = new ArrayList<>();
        for (String leaderboardName : leaderboardNames) {
            Leaderboard leaderboard = leaderboardsByName.get(leaderboardName);
            if (leaderboard == null) {
                throw new IllegalArgumentException("No leaderboard with name " + leaderboardName + " found");
            } else {
                leaderboards.add(leaderboard);
            }
        }
        LeaderboardGroup result = new LeaderboardGroupImpl(id, groupName, description, displayName,
                displayGroupsInReverseOrder, leaderboards);
        if (overallLeaderboardScoringSchemeType != null) {
            // create overall leaderboard and its discards settings
            addOverallLeaderboardToLeaderboardGroup(result,
                    getBaseDomainFactory().createScoringScheme(overallLeaderboardScoringSchemeType),
                    overallLeaderboardDiscardThresholds);
        }
        LockUtil.lockForWrite(leaderboardGroupsByNameLock);
        try {
            if (leaderboardGroupsByName.containsKey(groupName)) {
                throw new IllegalArgumentException("Leaderboard group with name " + groupName + " already exists");
            }
            leaderboardGroupsByName.put(groupName, result);
            leaderboardGroupsByID.put(result.getId(), result);
        } finally {
            LockUtil.unlockAfterWrite(leaderboardGroupsByNameLock);
        }
        mongoObjectFactory.storeLeaderboardGroup(result);
        return result;
    }

    @Override
    public void addLeaderboardGroupWithoutReplication(LeaderboardGroup leaderboardGroup) {
        LockUtil.lockForWrite(leaderboardGroupsByNameLock);
        try {
            String groupName = leaderboardGroup.getName();
            if (leaderboardGroupsByName.containsKey(groupName)) {
                throw new IllegalArgumentException("Leaderboard group with name " + groupName + " already exists");
            }
            leaderboardGroupsByName.put(groupName, leaderboardGroup);
            leaderboardGroupsByID.put(leaderboardGroup.getId(), leaderboardGroup);
        } finally {
            LockUtil.unlockAfterWrite(leaderboardGroupsByNameLock);
        }
        if (leaderboardGroup.hasOverallLeaderboard()) {
            addLeaderboard(leaderboardGroup.getOverallLeaderboard());
        }
        mongoObjectFactory.storeLeaderboardGroup(leaderboardGroup);
    }

    @Override
    public void removeLeaderboardGroup(String groupName) {
        final LeaderboardGroup leaderboardGroup;
        LockUtil.lockForWrite(leaderboardGroupsByNameLock);
        try {
            leaderboardGroup = leaderboardGroupsByName.remove(groupName);
            if (leaderboardGroup != null) {
                leaderboardGroupsByID.remove(leaderboardGroup.getId());
            }
        } finally {
            LockUtil.unlockAfterWrite(leaderboardGroupsByNameLock);
        }
        mongoObjectFactory.removeLeaderboardGroup(groupName);
        if (leaderboardGroup != null && leaderboardGroup.getOverallLeaderboard() != null) {
            removeLeaderboard(leaderboardGroup.getOverallLeaderboard().getName());
        }
    }

    @Override
    public void renameLeaderboardGroup(String oldName, String newName) {
        LockUtil.lockForWrite(leaderboardGroupsByNameLock);
        try {
            final LeaderboardGroup toRename = leaderboardGroupsByName.get(oldName);
            if (toRename == null) {
                throw new IllegalArgumentException("No leaderboard group with name " + oldName + " found");
            }
            if (leaderboardGroupsByName.containsKey(newName)) {
                throw new IllegalArgumentException("Leaderboard group with name " + newName + " already exists");
            }
            leaderboardGroupsByName.remove(oldName);
            toRename.setName(newName);
            leaderboardGroupsByName.put(newName, toRename);
        } finally {
            LockUtil.unlockAfterWrite(leaderboardGroupsByNameLock);
        }
        mongoObjectFactory.renameLeaderboardGroup(oldName, newName);
    }

    @Override
    public void updateLeaderboardGroup(String oldName, String newName, String description, String displayName,
            List<String> leaderboardNames, int[] overallLeaderboardDiscardThresholds,
            ScoringSchemeType overallLeaderboardScoringSchemeType) {
        if (!oldName.equals(newName)) {
            renameLeaderboardGroup(oldName, newName);
        }
        LeaderboardGroup group = getLeaderboardGroupByName(newName);
        if (!description.equals(group.getDescription())) {
            group.setDescriptiom(description);
        }
        if (!Util.equalsWithNull(displayName, group.getDisplayName())) {
            group.setDisplayName(displayName);
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
     * The operation is executed by immediately {@link Operation#internalApplyTo(Object) applying} it to this
     * service object. It is then replicated to all replicas.
     * 
     * @see {@link #replicate(RacingEventServiceOperation)}
     */
    @Override
    public <T> T apply(OperationWithResult<RacingEventService, T> operation) {
        RacingEventServiceOperation<T> reso = (RacingEventServiceOperation<T>) operation;
        try {
            T result = reso.internalApplyTo(this);
            replicate(reso);
            return result;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "apply", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public ObjectInputStream createObjectInputStreamResolvingAgainstCache(InputStream is) throws IOException {
        return getBaseDomainFactory().createObjectInputStreamResolvingAgainstThisFactory(is);
    }

    @Override
    public Serializable getId() {
        return getClass().getName();
    }

    @Override
    public <T> void replicate(RacingEventServiceOperation<T> operation) {
        for (OperationExecutionListener<RacingEventService> listener : operationExecutionListeners.keySet()) {
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
    public void addOperationExecutionListener(OperationExecutionListener<RacingEventService> listener) {
        operationExecutionListeners.put(listener, listener);
    }

    @Override
    public void removeOperationExecutionListener(OperationExecutionListener<RacingEventService> listener) {
        operationExecutionListeners.remove(listener);
    }

    @Override
    public void serializeForInitialReplicationInternal(ObjectOutputStream oos) throws IOException {
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

        logger.info("Serializing configuration map...");
        oos.writeObject(configurationMap);
        logoutput.append("Serialized " + configurationMap.size() + " configuration entries\n");
        for (DeviceConfigurationMatcher matcher : configurationMap.keySet()) {
            logoutput.append(String.format("%3s\n", matcher.toString()));
        }

        logger.info("Serializing remote sailing server references...");
        final ArrayList<RemoteSailingServerReference> remoteServerReferences = new ArrayList<>(remoteSailingServerSet
                .getCachedEventsForRemoteSailingServers().keySet());
        oos.writeObject(remoteServerReferences);
        logoutput.append("Serialized " + remoteServerReferences.size() + " remote sailing server references\n");

        logger.info(logoutput.toString());
    }

    @SuppressWarnings("unchecked") // all the casts of ois.readObject()'s return value to Map<..., ...>
    // the type-parameters in the casts of the de-serialized collection objects can't be checked
    @Override
    public void initiallyFillFromInternal(ObjectInputStream ois) throws IOException, ClassNotFoundException, InterruptedException {
        logger.info("Performing initial replication load on " + this);
        ClassLoader oldContextClassloader = Thread.currentThread().getContextClassLoader();
        try {
            // Use this object's class's class loader as the context class loader which will then be used for
            // de-serialization; this will cause all classes to be visible that this bundle
            // (com.sap.sailing.server) can see
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
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
                leaderboardGroupsByID.put(lg.getId(), lg);
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
                competitorStore.getOrCreateCompetitor(dynamicCompetitor.getId(), dynamicCompetitor.getName(),
                        dynamicCompetitor.getColor(), dynamicCompetitor.getTeam(), dynamicCompetitor.getBoat());
            }
            logoutput.append("Received " + competitorStore.size() + " NEW competitors\n");

            logger.info("Reading device configurations...");
            configurationMap.putAll((DeviceConfigurationMapImpl) ois.readObject());
            logoutput.append("Received " + configurationMap.size() + " NEW configuration entries\n");
            for (DeviceConfigurationMatcher matcher : configurationMap.keySet()) {
                logoutput.append(String.format("%3s\n", matcher.toString()));
            }

            logger.info("Reading remote sailing server references...");
            for (RemoteSailingServerReference remoteSailingServerReference : (Iterable<RemoteSailingServerReference>) ois
                    .readObject()) {
                remoteSailingServerSet.add(remoteSailingServerReference);
                logoutput.append("Received remote sailing server reference " + remoteSailingServerReference);
            }

            // make sure to initialize listeners correctly
            for (Regatta regatta : regattasByName.values()) {
                RegattaImpl regattaImpl = (RegattaImpl) regatta;
                regattaImpl.initializeSeriesAfterDeserialize();
            }

            logger.info(logoutput.toString());
        } finally {
            Thread.currentThread().setContextClassLoader(oldContextClassloader);
        }
    }

    @Override
    public void clearReplicaState() throws MalformedURLException, IOException, InterruptedException {
        logger.info("Clearing all data structures...");
        LockUtil.lockForWrite(regattasByNameLock);
        try {
            regattasByName.clear();
        } finally {
            LockUtil.unlockAfterWrite(regattasByNameLock);
        }
        regattasObservedForDefaultLeaderboard.clear();

        if (raceTrackersByRegatta != null && !raceTrackersByRegatta.isEmpty()) {
            for (DynamicTrackedRegatta regatta : regattaTrackingCache.values()) {
                for (RaceTracker tracker : raceTrackersByRegatta.get(regatta)) {
                    tracker.stop();
                }
            }
        }
        LockUtil.lockForWrite(regattaTrackingCacheLock);
        try {
            regattaTrackingCache.clear();
        } finally {
            LockUtil.unlockAfterWrite(regattaTrackingCacheLock);
        }
        LockUtil.lockForWrite(raceTrackersByRegattaLock);
        try {
            raceTrackersByRegatta.clear();
        } finally {
            LockUtil.unlockAfterWrite(raceTrackersByRegattaLock);
        }
        LockUtil.lockForWrite(leaderboardGroupsByNameLock);
        try {
            leaderboardGroupsByName.clear();
            leaderboardGroupsByID.clear();
        } finally {
            LockUtil.unlockAfterWrite(leaderboardGroupsByNameLock);
        }
        LockUtil.lockForWrite(leaderboardsByNameLock);
        try {
            leaderboardsByName.clear();
        } finally {
            LockUtil.unlockAfterWrite(leaderboardsByNameLock);
        }
        eventsById.clear();
        mediaLibrary.clear();
        competitorStore.clear();
        remoteSailingServerSet.clear();
    }

    // Used for TESTING only
    @Override
    public Event addEvent(String eventName, String eventDescription, TimePoint startDate, TimePoint endDate,
            String venue, boolean isPublic, UUID id) {
        Event result = createEventWithoutReplication(eventName, eventDescription, startDate, endDate, venue, isPublic,
                id, /* imageURLs */Collections.<URL> emptyList(),
                /* videoURLs */Collections.<URL> emptyList(), /* sponsorImageURLs */Collections.<URL> emptyList(), /* logoImageURL */
                null, /* officialWebsiteURL */null);
        replicate(new CreateEvent(eventName, eventDescription, startDate, endDate, venue, isPublic, /* imageURLs */
        id, Collections.<URL> emptyList(),
        /* videoURLs */Collections.<URL> emptyList(), /* sponsorImageURLs */Collections.<URL> emptyList(),
        /* logoimageURL */null, /* officialWebsiteURLAsString */null));
        return result;
    }

    @Override
    public void addEventWithoutReplication(Event event) {
        addEvent(event);
    }

    @Override
    public Event createEventWithoutReplication(String eventName, String eventDescription, TimePoint startDate,
            TimePoint endDate, String venue, boolean isPublic, UUID id, Iterable<URL> imageURLs,
            Iterable<URL> videoURLs, Iterable<URL> sponsorImageURLs, URL logoImageURL, URL officialWebsiteURL) {
        Event result = new EventImpl(eventName, startDate, endDate, venue, isPublic, id);
        addEvent(result);
        result.setDescription(eventDescription);
        result.setImageURLs(imageURLs);
        result.setVideoURLs(videoURLs);
        result.setSponsorImageURLs(sponsorImageURLs);
        result.setLogoImageURL(logoImageURL);
        result.setOfficialWebsiteURL(officialWebsiteURL);
        return result;
    }

    private void addEvent(Event result) {
        if (eventsById.containsKey(result.getId())) {
            throw new IllegalArgumentException("Event with ID " + result.getId()
                    + " already exists which is pretty surprising...");
        }
        eventsById.put(result.getId(), result);
        mongoObjectFactory.storeEvent(result);
    }

    @Override
    public void updateEvent(UUID id, String eventName, String eventDescription, TimePoint startDate, TimePoint endDate,
            String venueName, boolean isPublic, Iterable<UUID> leaderboardGroupIds, URL officialWebsiteURL,
            URL logoImageURL, Iterable<URL> imageURLs, Iterable<URL> videoURLs, Iterable<URL> sponsorImageURLs) {
        final Event event = eventsById.get(id);
        if (event == null) {
            throw new IllegalArgumentException("Sailing event with ID " + id + " does not exist.");
        }
        event.setName(eventName);
        event.setDescription(eventDescription);
        event.setStartDate(startDate);
        event.setEndDate(endDate);
        event.setPublic(isPublic);
        event.getVenue().setName(venueName);
        List<LeaderboardGroup> leaderboardGroups = new ArrayList<>();
        for (UUID lgid : leaderboardGroupIds) {
            LeaderboardGroup lg = getLeaderboardGroupByID(lgid);
            if (lg != null) {
                leaderboardGroups.add(lg);
            } else {
                logger.info("Couldn't find leaderboard group with ID " + lgid + " while updating event "
                        + event.getName());
            }
        }
        event.setLeaderboardGroups(leaderboardGroups);
        event.setOfficialWebsiteURL(officialWebsiteURL);
        event.setLogoImageURL(logoImageURL);
        event.setImageURLs(imageURLs);
        event.setVideoURLs(videoURLs);
        event.setSponsorImageURLs(sponsorImageURLs);
        // TODO consider use diffutils to compute diff between old and new leaderboard groups list and apply the patch
        // to keep changes minimial
        mongoObjectFactory.storeEvent(event);
    }

    @Override
    public void renameEvent(UUID id, String newName) {
        final Event toRename = eventsById.get(id);
        if (toRename == null) {
            throw new IllegalArgumentException("No sailing event with ID " + id + " found.");
        }
        toRename.setName(newName);
        mongoObjectFactory.renameEvent(id, newName);
        replicate(new RenameEvent(id, newName));
    }

    @Override
    public void removeEvent(UUID id) {
        removeEventFromEventsById(id);
        mongoObjectFactory.removeEvent(id);
        replicate(new RemoveEvent(id));
    }

    protected void removeEventFromEventsById(Serializable id) {
        eventsById.remove(id);
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
        addCourseAreaWithoutReplication(eventId, courseAreaId, courseAreaName);
        replicate(new AddCourseArea(eventId, courseAreaName, courseAreaId));
        return courseArea;
    }

    @Override
    public CourseArea addCourseAreaWithoutReplication(UUID eventId, UUID courseAreaId, String courseAreaName) {
        final CourseArea courseArea = getBaseDomainFactory().getOrCreateCourseArea(courseAreaId, courseAreaName);
        final Event event = eventsById.get(eventId);
        if (event == null) {
            throw new IllegalArgumentException("No sailing event with ID " + eventId + " found.");
        }
        event.getVenue().addCourseArea(courseArea);
        mongoObjectFactory.storeEvent(event);
        return courseArea;
    }

    @Override
    public CourseArea removeCourseAreaWithoutReplication(UUID eventId, UUID courseAreaId) {
        final CourseArea courseArea = getBaseDomainFactory().getExistingCourseAreaById(courseAreaId);
        if (courseArea == null) {
            throw new IllegalArgumentException("No course area with ID "+courseAreaId+" found.");
        }
        final Event event = eventsById.get(eventId);
        if (event == null) {
            throw new IllegalArgumentException("No sailing event with ID " + eventId + " found.");
        }
        event.getVenue().removeCourseArea(courseArea);
        mongoObjectFactory.storeEvent(event);
        return courseArea;
    }

    @Override
    public void mediaTrackAdded(MediaTrack mediaTrack) {
        if (mediaTrack.dbId == null) {
            mediaTrack.dbId = mediaDB.insertMediaTrack(mediaTrack.title, mediaTrack.url, mediaTrack.startTime,
                    mediaTrack.duration, mediaTrack.mimeType, mediaTrack.assignedRaces);
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
        mediaDB.updateDuration(mediaTrack.dbId, mediaTrack.duration);
        mediaLibrary.durationChanged(mediaTrack);
        replicate(new UpdateMediaTrackDurationOperation(mediaTrack));
    }

    @Override
    public void mediaTrackAssignedRacesChanged(MediaTrack mediaTrack) {
        mediaDB.updateRace(mediaTrack.dbId, mediaTrack.assignedRaces);
        mediaLibrary.assignedRacesChanged(mediaTrack);
        replicate(new UpdateMediaTrackRacesOperation(mediaTrack));

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
                mediaDB.insertMediaTrackWithId(trackToImport.dbId, trackToImport.title, trackToImport.url,
                        trackToImport.startTime, trackToImport.duration, trackToImport.mimeType,
                        trackToImport.assignedRaces);
                mediaTrackAdded(trackToImport);
            } else if (override) {

                // Using fine-grained update methods.
                // Rationale: Changes on more than one track property are rare
                // and don't justify the introduction of a new set
                // of methods (including replication).
                if (!Util.equalsWithNull(existingTrack.title, trackToImport.title)) {
                    mediaTrackTitleChanged(trackToImport);
                }
                if (!Util.equalsWithNull(existingTrack.url, trackToImport.url)) {
                    mediaTrackUrlChanged(trackToImport);
                }
                if (!Util.equalsWithNull(existingTrack.startTime, trackToImport.startTime)) {
                    mediaTrackStartTimeChanged(trackToImport);
                }
                if (!Util.equalsWithNull(existingTrack.duration, trackToImport.duration)) {
                    mediaTrackDurationChanged(trackToImport);
                }
                if (!Util.equalsWithNull(existingTrack.assignedRaces, trackToImport.assignedRaces)) {
                    mediaTrackAssignedRacesChanged(trackToImport);
                }
            }
        }
    }

    @Override
    public Collection<MediaTrack> getMediaTracksForRace(RegattaAndRaceIdentifier regattaAndRaceIdentifier) {
        return mediaLibrary.findMediaTracksForRace(regattaAndRaceIdentifier);
    }
    
    @Override
    public Collection<MediaTrack> getMediaTracksInTimeRange(RegattaAndRaceIdentifier regattaAndRaceIdentifier) {
        TrackedRace trackedRace = getExistingTrackedRace(regattaAndRaceIdentifier);
        if (trackedRace != null) {
            if (trackedRace.isLive(MillisecondsTimePoint.now())) {
                return mediaLibrary.findLiveMediaTracks();
            } else {
                TimePoint raceStart = trackedRace.getStartOfRace() == null ? trackedRace.getStartOfTracking() : trackedRace.getStartOfRace();
                TimePoint raceEnd = trackedRace.getEndOfRace() == null ? trackedRace.getEndOfTracking() : trackedRace.getEndOfRace();
                return mediaLibrary.findMediaTracksInTimeRange(raceStart, raceEnd);
            }
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
        configurationMap.put(matcher, configuration);
        mongoObjectFactory.storeDeviceConfiguration(matcher, configuration);
        replicate(new CreateOrUpdateDeviceConfiguration(matcher, configuration));
    }

    @Override
    public void removeDeviceConfiguration(DeviceConfigurationMatcher matcher) {
        configurationMap.remove(matcher);
        mongoObjectFactory.removeDeviceConfiguration(matcher);
        replicate(new RemoveDeviceConfiguration(matcher));
    }

    @Override
    public Map<DeviceConfigurationMatcher, DeviceConfiguration> getAllDeviceConfigurations() {
        return new HashMap<DeviceConfigurationMatcher, DeviceConfiguration>(configurationMap);
    }

    @Override
    public TimePoint setStartTimeAndProcedure(String leaderboardName, String raceColumnName, String fleetName,
            String authorName, int authorPriority, int passId, TimePoint logicalTimePoint, TimePoint startTime,
            RacingProcedureType racingProcedure) {
        RaceLog raceLog = getRaceLog(leaderboardName, raceColumnName, fleetName);
        if (raceLog != null) {
            RaceState state = RaceStateImpl.create(raceLog, new LogEventAuthorImpl(authorName, authorPriority));
            if (passId > raceLog.getCurrentPassId()) {
                state.setAdvancePass(logicalTimePoint);
            }
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
    public com.sap.sse.common.Util.Triple<TimePoint, Integer, RacingProcedureType> getStartTimeAndProcedure(
            String leaderboardName, String raceColumnName, String fleetName) {
        RaceLog raceLog = getRaceLog(leaderboardName, raceColumnName, fleetName);
        if (raceLog != null) {
            ReadonlyRaceState state = ReadonlyRaceStateImpl.create(raceLog);
            return new com.sap.sse.common.Util.Triple<TimePoint, Integer, RacingProcedureType>(state.getStartTime(),
                    raceLog.getCurrentPassId(), state.getRacingProcedure().getType());
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
    public GPSFixStore getGPSFixStore() {
        return gpsFixStore;
    }

    @Override
    public RaceTracker getRaceTrackerById(Object id) {
        return raceTrackersByID.get(id);
    }

    @Override
    public AbstractLogEventAuthor getServerAuthor() {
        return raceLogEventAuthorForServer;
    }

    @Override
    public CompetitorStore getCompetitorStore() {
        return competitorStore;
    }

    @Override
    public TypeBasedServiceFinderFactory getTypeBasedServiceFinderFactory() {
        return serviceFinderFactory;
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

    @Override
    public Result<LeaderboardSearchResult> search(KeywordQuery query) {
        long start = System.currentTimeMillis();
        logger.info("Searching local server for " + query);
        Result<LeaderboardSearchResult> result = new RegattaByKeywordSearchService().search(this, query);
        logger.fine("Search for " + query + " took " + (System.currentTimeMillis() - start) + "ms");
        return result;
    }

    @Override
    public Result<LeaderboardSearchResultBase> searchRemotely(String remoteServerReferenceName, KeywordQuery query) {
        long start = System.currentTimeMillis();
        ResultImpl<LeaderboardSearchResultBase> result = null;
        RemoteSailingServerReference remoteRef = remoteSailingServerSet
                .getServerReferenceByName(remoteServerReferenceName);
        if (remoteRef == null) {
            result = null;
        } else {
            BufferedReader bufferedReader = null;
            try {
                try {
                    final URL eventsURL = new URL(remoteRef.getURL(), "sailingserver/api/v1/search?q="
                            + URLEncoder.encode(query.toString(), "UTF-8"));
                    logger.info("Searching remote server " + remoteRef + " for " + query);
                    URLConnection urlConnection = eventsURL.openConnection();
                    urlConnection.connect();
                    bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    JSONParser parser = new JSONParser();
                    Object eventsAsObject = parser.parse(bufferedReader);
                    final LeaderboardGroupBaseJsonDeserializer leaderboardGroupBaseJsonDeserializer = new LeaderboardGroupBaseJsonDeserializer();
                    LeaderboardSearchResultBaseJsonDeserializer deserializer = new LeaderboardSearchResultBaseJsonDeserializer(
                            new EventBaseJsonDeserializer(new VenueJsonDeserializer(new CourseAreaJsonDeserializer(
                                    DomainFactory.INSTANCE)), leaderboardGroupBaseJsonDeserializer),
                            leaderboardGroupBaseJsonDeserializer);
                    result = new ResultImpl<LeaderboardSearchResultBase>(query,
                            new LeaderboardSearchResultBaseRanker<LeaderboardSearchResultBase>());
                    JSONArray hitsAsJsonArray = (JSONArray) eventsAsObject;
                    for (Object hitAsObject : hitsAsJsonArray) {
                        JSONObject hitAsJson = (JSONObject) hitAsObject;
                        LeaderboardSearchResultBase hit = deserializer.deserialize(hitAsJson);
                        result.addHit(hit);
                    }
                } finally {
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                }
            } catch (IOException | ParseException e) {
                logger.log(Level.INFO,
                        "Exception trying to fetch events from remote server " + remoteRef + ": " + e.getMessage(), e);
            }
        }
        logger.fine("Remote search on " + remoteRef + " for " + query + " took " + (System.currentTimeMillis() - start)
                + "ms");
        return result;
    }

}
