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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.LogEventAuthorImpl;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.FinishedTimeFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.FinishingTimeFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogRaceStatusEventImpl;
import com.sap.sailing.domain.abstractlog.race.state.RaceState;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.RaceStateImpl;
import com.sap.sailing.domain.abstractlog.race.state.impl.ReadonlyRaceStateImpl;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.shared.analyzing.CompetitorsAndBoatsInLogAnalyzer;
import com.sap.sailing.domain.anniversary.DetailedRaceInfo;
import com.sap.sailing.domain.anniversary.SimpleRaceInfo;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorAndBoatStore;
import com.sap.sailing.domain.base.CompetitorAndBoatStore.BoatUpdateListener;
import com.sap.sailing.domain.base.CompetitorAndBoatStore.CompetitorUpdateListener;
import com.sap.sailing.domain.base.CompetitorWithBoat;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.LeaderboardSearchResult;
import com.sap.sailing.domain.base.LeaderboardSearchResultBase;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnInSeries;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.RegattaListener;
import com.sap.sailing.domain.base.RemoteSailingServerReference;
import com.sap.sailing.domain.base.SailingServerConfiguration;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.Sideline;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.configuration.DeviceConfiguration;
import com.sap.sailing.domain.base.configuration.DeviceConfigurationIdentifier;
import com.sap.sailing.domain.base.configuration.DeviceConfigurationMatcher;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.domain.base.configuration.impl.DeviceConfigurationMapImpl;
import com.sap.sailing.domain.base.impl.DynamicBoat;
import com.sap.sailing.domain.base.impl.DynamicCompetitor;
import com.sap.sailing.domain.base.impl.DynamicCompetitorWithBoat;
import com.sap.sailing.domain.base.impl.DynamicPerson;
import com.sap.sailing.domain.base.impl.DynamicTeam;
import com.sap.sailing.domain.base.impl.EventImpl;
import com.sap.sailing.domain.base.impl.PersonImpl;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.base.impl.RemoteSailingServerReferenceImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.domain.common.CompetitorDescriptor;
import com.sap.sailing.domain.common.CompetitorRegistrationType;
import com.sap.sailing.domain.common.DataImportProgress;
import com.sap.sailing.domain.common.DataImportSubProgress;
import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.MasterDataImportObjectCreationCount;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.TrackedRaceStatusEnum;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.dto.AnniversaryType;
import com.sap.sailing.domain.common.dto.EventType;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RegattaCreationParametersDTO;
import com.sap.sailing.domain.common.dto.SeriesCreationParametersDTO;
import com.sap.sailing.domain.common.impl.DataImportProgressImpl;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.common.tracking.SensorFix;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.FlexibleRaceColumn;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.LeaderboardRegistry;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboardWithEliminations;
import com.sap.sailing.domain.leaderboard.ScoreCorrectionListener;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.leaderboard.SettableScoreCorrection;
import com.sap.sailing.domain.leaderboard.impl.DelegatingRegattaLeaderboardWithCompetitorElimination;
import com.sap.sailing.domain.leaderboard.impl.FlexibleLeaderboardImpl;
import com.sap.sailing.domain.leaderboard.impl.LeaderboardGroupImpl;
import com.sap.sailing.domain.leaderboard.impl.RegattaLeaderboardImpl;
import com.sap.sailing.domain.leaderboard.impl.ThresholdBasedResultDiscardingRuleImpl;
import com.sap.sailing.domain.leaderboard.meta.LeaderboardGroupMetaLeaderboard;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.MongoRaceLogStoreFactory;
import com.sap.sailing.domain.persistence.MongoRegattaLogStoreFactory;
import com.sap.sailing.domain.persistence.MongoWindStore;
import com.sap.sailing.domain.persistence.MongoWindStoreFactory;
import com.sap.sailing.domain.persistence.PersistenceFactory;
import com.sap.sailing.domain.persistence.media.MediaDB;
import com.sap.sailing.domain.persistence.media.MediaDBFactory;
import com.sap.sailing.domain.persistence.racelog.tracking.MongoSensorFixStoreFactory;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.racelog.tracking.SensorFixStore;
import com.sap.sailing.domain.ranking.RankingMetric.CompetitorRankingInfo;
import com.sap.sailing.domain.ranking.RankingMetric.RankingInfo;
import com.sap.sailing.domain.ranking.RankingMetricConstructor;
import com.sap.sailing.domain.regattalike.HasRegattaLike;
import com.sap.sailing.domain.regattalike.IsRegattaLike;
import com.sap.sailing.domain.regattalike.LeaderboardThatHasRegattaLike;
import com.sap.sailing.domain.regattalog.RegattaLogStore;
import com.sap.sailing.domain.statistics.Statistics;
import com.sap.sailing.domain.tracking.DynamicRaceDefinitionSet;
import com.sap.sailing.domain.tracking.DynamicSensorFixTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.RaceChangeListener;
import com.sap.sailing.domain.tracking.RaceHandle;
import com.sap.sailing.domain.tracking.RaceListener;
import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sailing.domain.tracking.RaceTrackingConnectivityParameters;
import com.sap.sailing.domain.tracking.RaceTrackingHandler;
import com.sap.sailing.domain.tracking.RaceTrackingHandler.DefaultRaceTrackingHandler;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRaceStatus;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.TrackedRegattaListener;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tracking.WindTracker;
import com.sap.sailing.domain.tracking.WindTrackerFactory;
import com.sap.sailing.domain.tracking.impl.AbstractRaceChangeListener;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRegattaImpl;
import com.sap.sailing.domain.tracking.impl.TrackedRaceImpl;
import com.sap.sailing.expeditionconnector.ExpeditionTrackerFactory;
import com.sap.sailing.server.Replicator;
import com.sap.sailing.server.anniversary.AnniversaryRaceDeterminatorImpl;
import com.sap.sailing.server.anniversary.RaceChangeObserverForAnniversaryDetection;
import com.sap.sailing.server.anniversary.checker.QuarterChecker;
import com.sap.sailing.server.anniversary.checker.SameDigitChecker;
import com.sap.sailing.server.gateway.deserialization.impl.CourseAreaJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.EventBaseJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.LeaderboardGroupBaseJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.LeaderboardSearchResultBaseJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.VenueJsonDeserializer;
import com.sap.sailing.server.interfaces.DataImportLockWithProgress;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.SimulationService;
import com.sap.sailing.server.interfaces.TaggingService;
import com.sap.sailing.server.notification.EmptySailingNotificationService;
import com.sap.sailing.server.notification.SailingNotificationService;
import com.sap.sailing.server.operationaltransformation.AddCourseAreas;
import com.sap.sailing.server.operationaltransformation.AddDefaultRegatta;
import com.sap.sailing.server.operationaltransformation.AddMediaTrackOperation;
import com.sap.sailing.server.operationaltransformation.AddRaceDefinition;
import com.sap.sailing.server.operationaltransformation.AddSpecificRegatta;
import com.sap.sailing.server.operationaltransformation.ConnectTrackedRaceToLeaderboardColumn;
import com.sap.sailing.server.operationaltransformation.CreateBoat;
import com.sap.sailing.server.operationaltransformation.CreateCompetitor;
import com.sap.sailing.server.operationaltransformation.CreateEvent;
import com.sap.sailing.server.operationaltransformation.CreateOrUpdateDataImportProgress;
import com.sap.sailing.server.operationaltransformation.CreateOrUpdateDeviceConfiguration;
import com.sap.sailing.server.operationaltransformation.CreateTrackedRace;
import com.sap.sailing.server.operationaltransformation.DataImportFailed;
import com.sap.sailing.server.operationaltransformation.RecordCompetitorGPSFix;
import com.sap.sailing.server.operationaltransformation.RecordCompetitorSensorFix;
import com.sap.sailing.server.operationaltransformation.RecordCompetitorSensorFixTrack;
import com.sap.sailing.server.operationaltransformation.RecordMarkGPSFix;
import com.sap.sailing.server.operationaltransformation.RecordMarkGPSFixForExistingTrack;
import com.sap.sailing.server.operationaltransformation.RecordMarkGPSFixForNewMarkTrack;
import com.sap.sailing.server.operationaltransformation.RecordWindFix;
import com.sap.sailing.server.operationaltransformation.RemoveDeviceConfiguration;
import com.sap.sailing.server.operationaltransformation.RemoveEvent;
import com.sap.sailing.server.operationaltransformation.RemoveLeaderboardGroupFromEvent;
import com.sap.sailing.server.operationaltransformation.RemoveMediaTrackOperation;
import com.sap.sailing.server.operationaltransformation.RemoveWindFix;
import com.sap.sailing.server.operationaltransformation.RenameEvent;
import com.sap.sailing.server.operationaltransformation.SetDataImportDeleteProgressFromMapTimer;
import com.sap.sailing.server.operationaltransformation.TrackRegatta;
import com.sap.sailing.server.operationaltransformation.UpdateBoat;
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
import com.sap.sailing.server.security.SailingViewerRole;
import com.sap.sailing.server.simulation.SimulationServiceFactory;
import com.sap.sailing.server.statistics.StatisticsAggregator;
import com.sap.sailing.server.statistics.StatisticsCalculator;
import com.sap.sailing.server.statistics.TrackedRaceStatisticsCache;
import com.sap.sailing.server.tagging.TaggingServiceFactory;
import com.sap.sailing.server.util.EventUtil;
import com.sap.sse.ServerInfo;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.PairingListCreationException;
import com.sap.sse.common.Renamable;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TypeBasedServiceFinderFactory;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.search.KeywordQuery;
import com.sap.sse.common.search.Result;
import com.sap.sse.common.search.ResultImpl;
import com.sap.sse.concurrent.LockUtil;
import com.sap.sse.concurrent.NamedReentrantReadWriteLock;
import com.sap.sse.filestorage.FileStorageManagementService;
import com.sap.sse.pairinglist.CompetitionFormat;
import com.sap.sse.pairinglist.PairingFrameProvider;
import com.sap.sse.pairinglist.PairingList;
import com.sap.sse.pairinglist.PairingListTemplate;
import com.sap.sse.pairinglist.PairingListTemplateFactory;
import com.sap.sse.replication.OperationExecutionListener;
import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.replication.OperationWithResultWithIdWrapper;
import com.sap.sse.replication.OperationsToMasterSender;
import com.sap.sse.replication.ReplicationMasterDescriptor;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.shared.RoleDefinition;
import com.sap.sse.security.shared.impl.Role;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.impl.UserGroup;
import com.sap.sse.replication.ReplicationService;
import com.sap.sse.replication.OperationsToMasterSendingQueue;
import com.sap.sse.shared.media.ImageDescriptor;
import com.sap.sse.shared.media.VideoDescriptor;
import com.sap.sse.util.ClearStateTestSupport;
import com.sap.sse.util.HttpUrlConnectionHelper;
import com.sap.sse.util.JoinedClassLoader;
import com.sap.sse.util.ThreadLocalTransporter;
import com.sap.sse.util.ThreadPoolUtil;

public class RacingEventServiceImpl implements RacingEventService, ClearStateTestSupport, RegattaListener,
        LeaderboardRegistry, Replicator {
    private static final Logger logger = Logger.getLogger(RacingEventServiceImpl.class.getName());

    /**
     * A scheduler for the periodic checks of the paramURL documents for the advent of {@link ControlPoint}s with static
     * position information otherwise not available through <code>MarkPassingReceiver</code>'s events.
     */
    private static final ScheduledExecutorService scheduler = ThreadPoolUtil.INSTANCE.getDefaultForegroundTaskThreadPoolExecutor();

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
     * Using {@link #getRaceTrackerByRegattaAndRaceIdentifier(RegattaAndRaceIdentifier, Consumer)}, one can request a
     * {@link RaceTracker} for a specific race. It can be that the requested {@link RaceTracker} isn't registered in
     * {@link #raceTrackersByRegatta}. To not need a Thread to wait for the RaceTracker to get available, a callback
     * will be registered that fires on registration of the RaceTracker. If the {@link RaceTracker} will not be
     * registered, the callback only leads to a minor leak.
     */
    private volatile transient ConcurrentHashMap<RegattaAndRaceIdentifier, Set<Consumer<RaceTracker>>> raceTrackerCallbacks;

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

    private final CompetitorAndBoatStore competitorAndBoatStore;

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

    private final RaceLogReplicatorAndNotifier raceLogReplicator;
    private final RegattaLogReplicator regattaLogReplicator;

    private final RaceLogScoringReplicator raceLogScoringReplicator;

    private final MediaDB mediaDB;

    private final MediaLibrary mediaLibrary;

    /**
     * Currently valid pairs of {@link DeviceConfigurationMatcher}s and {@link DeviceConfiguration}s. The contents of
     * this map is persisted and replicated. See {@link DeviceConfigurationMapImpl}.
     */
    protected final DeviceConfigurationMapImpl configurationMap;

    private final WindStore windStore;
    private final SensorFixStore sensorFixStore;

    /**
     * This author should be used for server generated race log events
     */
    private final AbstractLogEventAuthor raceLogEventAuthorForServer = new LogEventAuthorImpl(
            RacingEventService.class.getName(), 0);

    private PolarDataService polarDataService;

    private final SimulationService simulationService;

    private final TaggingService taggingService;

    /**
     * A service that, if not {@code null}, must be called when certain events that the service can notify users about
     * have occurred. For example, the
     * {@link SailingNotificationService#notifyUserOnBoatClassWhenScoreCorrectionsAreAvailable(com.sap.sailing.domain.base.BoatClass, Leaderboard)}
     * method must be called whenever a new set of score corrections have been made available.<p>
     * 
     * The field is transient because we don't want the service to be serialized from a master to a replica.
     * Replicas are expected to not notify users about anything, particularly because they usually don't
     * have a valid mail service, either.
     */
    private transient SailingNotificationService notificationService;
    
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
     * The master from which this replicable is currently replicating, or <code>null</code> if this replicable is not
     * currently replicated from any master.
     */
    private ReplicationMasterDescriptor replicatingFromMaster;

    private Set<OperationWithResultWithIdWrapper<?, ?>> operationsSentToMasterForReplication;

    private boolean currentlyFillingFromInitialLoad = false;
    
    private ThreadLocal<Boolean> currentlyApplyingOperationReceivedFromMaster = ThreadLocal.withInitial(() -> false);

    private final Set<ClassLoader> masterDataClassLoaders = new HashSet<ClassLoader>();
    
    private final JoinedClassLoader joinedClassLoader;

    private SailingServerConfiguration sailingServerConfiguration;

    private final TrackedRegattaListenerManager trackedRegattaListener;
    
    private long numberOfTrackedRacesToRestore;
    
    private final AtomicInteger numberOfTrackedRacesRestored;
    
    private transient final ConcurrentHashMap<Leaderboard, ScoreCorrectionListener> scoreCorrectionListenersByLeaderboard;

    private transient final ConcurrentHashMap<RaceDefinition, RaceTrackingConnectivityParameters> connectivityParametersByRace;

    private final TrackedRaceStatisticsCache trackedRaceStatisticsCache;

    private final AnniversaryRaceDeterminatorImpl anniversaryRaceDeterminator;

    /**
     * Observes {@link TrackedRegatta} and {@link TrackedRace} instances known to trigger an update of
     * {@link AnniversaryRaceDeterminatorImpl} if the number of anniversary race candidates changes. To do this, the
     * instance is registered as {@link TrackedRegattaListener} on the {@link TrackedRegattaListenerManager} know by
     * this service.
     */
    private final RaceChangeObserverForAnniversaryDetection raceChangeObserverForAnniversaryDetection;

    private final PairingListTemplateFactory pairingListTemplateFactory = PairingListTemplateFactory.INSTANCE; 
    
    /**
     * This field is expected to be set by the {@link ReplicationService} once it has "adopted" this replicable.
     * The {@link ReplicationService} "injects" this service so it can be used here as a delegate for the
     * {@link OperationsToMasterSendingQueue#scheduleForSending(OperationWithResult, OperationsToMasterSender)}
     * method.
     */
    private OperationsToMasterSendingQueue unsentOperationsToMasterSender;

    private ServiceTracker<SecurityService, SecurityService> securityServiceTracker;

    /**
     * Providing the constructor parameters for a new {@link RacingEventServiceImpl} instance is a bit tricky
     * in some cases because containment and initialization order of some types is fairly tightly coupled.
     * There is a dependency of many such objects on an instance of {@link RaceLogResolver} which is implemented
     * by {@link RacingEventServiceImpl}. However, therefore, this instance only becomes available in the
     * innermost constructor.
     * 
     * @author Axel Uhl (d043530)
     *
     */
    public static interface ConstructorParameters {
        DomainObjectFactory getDomainObjectFactory();
        MongoObjectFactory getMongoObjectFactory();
        com.sap.sailing.domain.base.DomainFactory getBaseDomainFactory();
        CompetitorAndBoatStore getCompetitorAndBoatStore();
    }

    /**
     * Constructs a {@link DomainFactory base domain factory} that uses this object's {@link #competitorAndBoatStore competitor
     * store} for competitor and boat management. This base domain factory is then also used for the construction of the
     * {@link DomainObjectFactory}. This constructor variant initially clears the persistent competitor and boat collections,
     * hence removes all previously persistent competitors and boats. This is the default for testing and for backward
     * compatibility with prior releases that did not support a persistent competitor and boat collection.
     */
    public RacingEventServiceImpl() {
        this(/* clearPersistentCompetitorAndBoatStore */ true, /* serviceFinderFactory */ null, /* restoreTrackedRaces */ false);
    }

    public RacingEventServiceImpl(WindStore windStore, SensorFixStore sensorFixStore,
            TypeBasedServiceFinderFactory serviceFinderFactory) {
        this(/* clearPersistentCompetitorAndBoatStore */ true, windStore, sensorFixStore, serviceFinderFactory,
                /* sailingNotificationService */ null, /* restoreTrackedRaces */ false);
    }

    void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public RacingEventServiceImpl(boolean clearPersistentCompetitorAndBoatStore, final TypeBasedServiceFinderFactory serviceFinderFactory, boolean restoreTrackedRaces) {
        this(clearPersistentCompetitorAndBoatStore, serviceFinderFactory, null, /* sailingNotificationService */ null,
                /* trackedRaceStatisticsCache */ null, restoreTrackedRaces, null);
    }

    /**
     * Like {@link #RacingEventServiceImpl()}, but allows callers to specify that the persistent competitor collection
     * be cleared before the service starts.
     * 
     * @param clearPersistentCompetitorAndBoatStore
     *            if <code>true</code>, the {@link PersistentCompetitorAndBoatStore} is created empty, with the
     *            corresponding database collection cleared as well. Use with caution! When used with
     *            <code>false</code>, competitors and boats created and stored during previous service executions will
     *            initially be loaded.
     * @param sailingNotificationService
     *            a notification service to call upon events worth notifying users about, or {@code null} if no
     *            notification service is available, e.g., in test set-ups
     * @param trackedRaceStatisticsCache
     *            a cache that gives access to detailed statistics about TrackedRaces. If <code>null</code>, no detailed
     *            statistics about TrackedRaces will be calculated.
     * @param securityServiceTracker
     *            will complete as soon as the securityServiceTracker is able to provide a SecurityService, NEVER hold a
     *            reference to the result of this, as it might become invalid if bundles are replaced/ restarted
     */
    public RacingEventServiceImpl(boolean clearPersistentCompetitorAndBoatStore,
            final TypeBasedServiceFinderFactory serviceFinderFactory, TrackedRegattaListenerManager trackedRegattaListener,
            SailingNotificationService sailingNotificationService,
            TrackedRaceStatisticsCache trackedRaceStatisticsCache, boolean restoreTrackedRaces,
            ServiceTracker<SecurityService, SecurityService> securityServiceTracker) {
        this((final RaceLogResolver raceLogResolver) -> {
            return new ConstructorParameters() {
                private final MongoObjectFactory mongoObjectFactory = PersistenceFactory.INSTANCE
                        .getDefaultMongoObjectFactory(serviceFinderFactory);
                private final PersistentCompetitorAndBoatStore competitorStore = new PersistentCompetitorAndBoatStore(
                        PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory(serviceFinderFactory),
                        clearPersistentCompetitorAndBoatStore, serviceFinderFactory, raceLogResolver);

                @Override
                public DomainObjectFactory getDomainObjectFactory() {
                    return competitorStore.getDomainObjectFactory();
                }

                @Override
                public MongoObjectFactory getMongoObjectFactory() {
                    return mongoObjectFactory;
                }

                @Override
                public DomainFactory getBaseDomainFactory() {
                    return competitorStore.getBaseDomainFactory();
                }

                @Override
                public CompetitorAndBoatStore getCompetitorAndBoatStore() {
                    return competitorStore;
                }
            };
        }, MediaDBFactory.INSTANCE.getDefaultMediaDB(), null, null, serviceFinderFactory, trackedRegattaListener,
                sailingNotificationService, trackedRaceStatisticsCache, restoreTrackedRaces,
                securityServiceTracker);
    }

    private RacingEventServiceImpl(final boolean clearPersistentCompetitorStore, WindStore windStore,
            SensorFixStore sensorFixStore, final TypeBasedServiceFinderFactory serviceFinderFactory,
            SailingNotificationService sailingNotificationService, boolean restoreTrackedRaces) {
        this((final RaceLogResolver raceLogResolver) -> {
            return new ConstructorParameters() {
                private final MongoObjectFactory mongoObjectFactory = PersistenceFactory.INSTANCE
                        .getDefaultMongoObjectFactory(serviceFinderFactory);
                private final PersistentCompetitorAndBoatStore competitorStore = new PersistentCompetitorAndBoatStore(
                        mongoObjectFactory, clearPersistentCompetitorStore, serviceFinderFactory, raceLogResolver);

                @Override
                public DomainObjectFactory getDomainObjectFactory() {
                    return competitorStore.getDomainObjectFactory();
                }

                @Override
                public MongoObjectFactory getMongoObjectFactory() {
                    return mongoObjectFactory;
                }

                @Override
                public DomainFactory getBaseDomainFactory() {
                    return competitorStore.getBaseDomainFactory();
                }

                @Override
                public CompetitorAndBoatStore getCompetitorAndBoatStore() {
                    return competitorStore;
                }
            };
        }, MediaDBFactory.INSTANCE.getDefaultMediaDB(), windStore, sensorFixStore, serviceFinderFactory, null,
                sailingNotificationService, /* trackedRaceStatisticsCache */ null, restoreTrackedRaces, null);
    }

    public RacingEventServiceImpl(final DomainObjectFactory domainObjectFactory, MongoObjectFactory mongoObjectFactory,
            MediaDB mediaDB, WindStore windStore, SensorFixStore sensorFixStore, boolean restoreTrackedRaces) {
        this((final RaceLogResolver raceLogResolver) -> {
            return new ConstructorParameters() {
                @Override
                public DomainObjectFactory getDomainObjectFactory() {
                    return domainObjectFactory;
                }

                @Override
                public MongoObjectFactory getMongoObjectFactory() {
                    return mongoObjectFactory;
                }

                @Override
                public DomainFactory getBaseDomainFactory() {
                    return domainObjectFactory.getBaseDomainFactory();
                }

                @Override
                public CompetitorAndBoatStore getCompetitorAndBoatStore() {
                    return getBaseDomainFactory().getCompetitorAndBoatStore();
                }
            };
        }, mediaDB, windStore, sensorFixStore, null, null, /* sailingNotificationService */ null,
                /* trackedRaceStatisticsCache */ null, restoreTrackedRaces, null);
    }

    /**
     * @param windStore
     *            if <code>null</code>, a default {@link MongoWindStore} will be used, based on the persistence set-up
     *            of this service
     * @param serviceFinderFactory
     *            used to find the services handling specific types of tracking devices, such as the persistent storage
     *            of {@link DeviceIdentifier}s of specific device types or the managing of the device-to-competitor
     *            associations per race tracked.
     * @param sailingNotificationService
     *            a notification service to call upon events worth notifying users about, or {@code null} if no
     *            notification service is available, e.g., in test set-ups
     * @param trackedRaceStatisticsCache
     *            a cache that gives access to detailed statistics about TrackedRaces. If <code>null</code>, no detailed
     *            statistics about TrackedRaces will be calculated.
     * @param restoreTrackedRaces
     *            if {@code true}, the tracking connectivity parameters for the races last loaded in the server are
     *            {@link DomainObjectFactory#loadConnectivityParametersForRacesToRestore(Consumer<RaceTrackingConnectivityParameter>)
     *            obtained} from the database, and {@link RaceTracker}s are
     *            {@link #addRace(RegattaIdentifier, RaceTrackingConnectivityParameters, long) created} for those,
     *            effectively restoring the server state to what it was last according to the database. If
     *            {@code false}, all restore information is
     *            {@link MongoObjectFactory#removeAllConnectivityParametersForRacesToRestore() cleared} from the
     *            database, and the server starts out with an empty list of tracked races.
     * @param securityServiceAvailable
     *            will complete as soon as the securityServiceTracker is able to provide a SecurityService, NEVER hold a
     *            reference to the result of this, as it might become invalid if bundles are replaced/ restarted
     */
    public RacingEventServiceImpl(Function<RaceLogResolver, ConstructorParameters> constructorParametersProvider,
            MediaDB mediaDb, final WindStore windStore, final SensorFixStore sensorFixStore,
            TypeBasedServiceFinderFactory serviceFinderFactory, TrackedRegattaListenerManager trackedRegattaListener,
            SailingNotificationService sailingNotificationService,
            TrackedRaceStatisticsCache trackedRaceStatisticsCache, boolean restoreTrackedRaces,
            ServiceTracker<SecurityService, SecurityService> securityServiceTracker) {
        logger.info("Created " + this);
        this.securityServiceTracker = securityServiceTracker;
        this.numberOfTrackedRacesRestored = new AtomicInteger();
        this.scoreCorrectionListenersByLeaderboard = new ConcurrentHashMap<>();
        this.connectivityParametersByRace = new ConcurrentHashMap<>();
        this.notificationService = sailingNotificationService;
        final ConstructorParameters constructorParameters = constructorParametersProvider.apply(this);
        this.domainObjectFactory = constructorParameters.getDomainObjectFactory();
        this.masterDataClassLoaders.add(this.getClass().getClassLoader());
        joinedClassLoader = new JoinedClassLoader(masterDataClassLoaders);
        this.operationsSentToMasterForReplication = new HashSet<>();
        this.baseDomainFactory = constructorParameters.getBaseDomainFactory();
        this.mongoObjectFactory = constructorParameters.getMongoObjectFactory();
        this.mediaDB = mediaDb;
        this.competitorAndBoatStore = constructorParameters.getCompetitorAndBoatStore();
        try {
            this.windStore = windStore == null ? MongoWindStoreFactory.INSTANCE.getMongoWindStore(mongoObjectFactory,
                    domainObjectFactory) : windStore;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.competitorAndBoatStore.addCompetitorUpdateListener(new CompetitorUpdateListener() {
            @Override
            public void competitorUpdated(Competitor competitor) {
                replicate(new UpdateCompetitor(competitor.getId().toString(), competitor.getName(), competitor.getShortName(), competitor
                        .getColor(), competitor.getEmail(), competitor.getTeam().getNationality(),
                        competitor.getTeam().getImage(), competitor.getFlagImage(),
                        competitor.getTimeOnTimeFactor(), competitor.getTimeOnDistanceAllowancePerNauticalMile(),
                        competitor.getSearchTag()));
            }
            @Override
            public void competitorCreated(Competitor competitor) {
                replicate(new CreateCompetitor(competitor.getId(), competitor.getName(), competitor.getShortName(),
                        competitor.getColor(), competitor.getEmail(), competitor.getFlagImage(), 
                        competitor.getTeam()==null?null:competitor.getTeam().getNationality(),
                        competitor.getTimeOnTimeFactor(),
                        competitor.getTimeOnDistanceAllowancePerNauticalMile(), competitor.getSearchTag(),
                        competitor.hasBoat() ? ((CompetitorWithBoat) competitor).getBoat().getId() : null));
            }
        });
        this.competitorAndBoatStore.addBoatUpdateListener(new BoatUpdateListener() {
            @Override
            public void boatUpdated(Boat boat) {
                replicate(new UpdateBoat(boat.getId().toString(), boat.getName(), boat.getColor(), boat.getSailID()));
            }
            @Override
            public void boatCreated(Boat boat) {
                replicate(new CreateBoat(boat.getId(), boat.getName(), 
                        boat.getBoatClass()==null?null:boat.getBoatClass().getName(), 
                        boat.getSailID(), boat.getColor()));
            }
        });
        this.dataImportLock = new DataImportLockWithProgress();

        remoteSailingServerSet = new RemoteSailingServerSet(scheduler, baseDomainFactory);
        regattasByName = new ConcurrentHashMap<String, Regatta>();
        regattasByNameLock = new NamedReentrantReadWriteLock("regattasByName for " + this, /* fair */false);
        eventsById = new ConcurrentHashMap<Serializable, Event>();
        regattaTrackingCache = new ConcurrentHashMap<>();
        regattaTrackingCacheLock = new NamedReentrantReadWriteLock("regattaTrackingCache for " + this, /* fair */false);
        raceTrackersByRegatta = new ConcurrentHashMap<>();
        raceTrackersByRegattaLock = new NamedReentrantReadWriteLock("raceTrackersByRegatta for " + this, /* fair */false);
        raceTrackersByID = new ConcurrentHashMap<>();
        raceTrackersByIDLocks = new ConcurrentHashMap<>();
        raceTrackerCallbacks = new ConcurrentHashMap<>();
        leaderboardGroupsByName = new ConcurrentHashMap<>();
        leaderboardGroupsByID = new ConcurrentHashMap<>();
        leaderboardGroupsByNameLock = new NamedReentrantReadWriteLock("leaderboardGroupsByName for " + this, /* fair */false);
        leaderboardsByName = new ConcurrentHashMap<String, Leaderboard>();
        leaderboardsByNameLock = new NamedReentrantReadWriteLock("leaderboardsByName for " + this, /* fair */false);
        operationExecutionListeners = new ConcurrentHashMap<>();
        courseListeners = new ConcurrentHashMap<>();
        persistentRegattasForRaceIDs = new ConcurrentHashMap<>();
        final ScheduledExecutorService simulatorExecutor = ThreadPoolUtil.INSTANCE.createBackgroundTaskThreadPoolExecutor("Simulator Background Executor");
        simulationService = SimulationServiceFactory.INSTANCE.getService(simulatorExecutor, this);
        taggingService = TaggingServiceFactory.INSTANCE.getService(this);
        this.raceLogReplicator = new RaceLogReplicatorAndNotifier(this);
        this.regattaLogReplicator = new RegattaLogReplicator(this);
        this.raceLogScoringReplicator = new RaceLogScoringReplicator(this);
        this.mediaLibrary = new MediaLibrary();
        try {
            this.sensorFixStore = sensorFixStore == null ? MongoSensorFixStoreFactory.INSTANCE.getMongoGPSFixStore(
                    mongoObjectFactory, domainObjectFactory, serviceFinderFactory) : sensorFixStore;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception trying to obtain MongoDB sensor fix store", e);
            throw new RuntimeException(e);
        }
        this.configurationMap = new DeviceConfigurationMapImpl();
        this.serviceFinderFactory = serviceFinderFactory;
        this.trackedRegattaListener = trackedRegattaListener == null ? EmptyTrackedRegattaListener.INSTANCE : trackedRegattaListener;
        sailingServerConfiguration = domainObjectFactory.loadServerConfiguration();
        final Iterable<Pair<Event, Boolean>> loadedEventsWithRequireStoreFlag = loadStoredEvents();
        loadStoredRegattas();
        loadRaceIDToRegattaAssociations();
        loadStoredLeaderboardsAndGroups();
        loadLinksFromEventsToLeaderboardGroups();
        loadMediaLibary();
        loadStoredDeviceConfigurations();
        loadAllRemoteSailingServersAndSchedulePeriodicEventCacheRefresh();
        // Stores all events which run through a data migration 
        // Remark: must be called after loadLinksFromEventsToLeaderboardGroups(), otherwise would loose the Event -> LeaderboardGroup relation
        for (Pair<Event, Boolean> eventAndRequireStoreFlag : loadedEventsWithRequireStoreFlag) {
            if (eventAndRequireStoreFlag.getB()) {
                mongoObjectFactory.storeEvent(eventAndRequireStoreFlag.getA());
            }
        }
        if (restoreTrackedRaces) {
            restoreTrackedRaces();
        } else {
            getMongoObjectFactory().removeAllConnectivityParametersForRacesToRestore();
        }
        this.trackedRaceStatisticsCache = trackedRaceStatisticsCache;
        anniversaryRaceDeterminator = new AnniversaryRaceDeterminatorImpl(this, remoteSailingServerSet,
                new QuarterChecker(), new SameDigitChecker());
        raceChangeObserverForAnniversaryDetection = new RaceChangeObserverForAnniversaryDetection(anniversaryRaceDeterminator);
        this.trackedRegattaListener.addListener(raceChangeObserverForAnniversaryDetection);
    }

    public void ensureServerIsInitiallyPublic() {
        try {
            final User allUser = getSecurityService().getAllUser();
            String initializedKey = ("serverInitialized " + ServerInfo.getName()).replaceAll("[\\W]|_", "");
            if (!Boolean.TRUE.equals(getSecurityService().getSetting(initializedKey, Boolean.class))) {
                getSecurityService().addSetting(initializedKey, Boolean.class);
                final RoleDefinition viewerRole = getSecurityService()
                        .getRoleDefinition(SailingViewerRole.getInstance().getId());
                final UserGroup defaultServerTenant = getSecurityService().getDefaultTenant();
                // role ownership handling left out on purpose, initially only an admin can set this server to be non
                // public
                final Role publicAccessForServerRole = new Role(viewerRole, defaultServerTenant, null);
                getSecurityService().addRoleForUser(allUser.getName(), publicAccessForServerRole);
                getSecurityService().setSetting(initializedKey, true);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error determining Server initialisation state", e);
        }
    }

    private void restoreTrackedRaces() {
        // restore the races by calling addRace one by one, but in a background thread, therefore concurrent to any remaining
        // server startup activities happening
        numberOfTrackedRacesToRestore = getDomainObjectFactory().loadConnectivityParametersForRacesToRestore(params -> {
            try {
                final RaceHandle handle = addRace(/* addToRegatta==null means "default regatta" */ null, params, /* no timeout during mass loading */ -1,
                        new DefaultRaceTrackingHandler() {
                    @Override
                    public DynamicTrackedRace createTrackedRace(TrackedRegatta trackedRegatta, RaceDefinition raceDefinition,
                            Iterable<Sideline> sidelines, WindStore windStore, long delayToLiveInMillis,
                            long millisecondsOverWhichToAverageWind, long millisecondsOverWhichToAverageSpeed,
                            DynamicRaceDefinitionSet raceDefinitionSetToUpdate, boolean useMarkPassingCalculator,
                            RaceLogResolver raceLogResolver, Optional<ThreadLocalTransporter> threadLocalTransporter) {
                        final DynamicTrackedRace trackedRace = super.createTrackedRace(trackedRegatta, raceDefinition, sidelines, windStore,
                                        delayToLiveInMillis, millisecondsOverWhichToAverageWind,
                                        millisecondsOverWhichToAverageSpeed, raceDefinitionSetToUpdate,
                                        useMarkPassingCalculator, raceLogResolver, threadLocalTransporter);
                        getSecurityService().migrateOwnership(trackedRace);
                        return trackedRace;
                    }
                });
                final RaceDefinition race = handle.getRace(RaceTracker.TIMEOUT_FOR_RECEIVING_RACE_DEFINITION_IN_MILLISECONDS); // try to not flood servers during restore by waiting for race to appear
                if (race == null) {
                    logger.warning("Race for tracker " + handle.getRaceTracker() + " with ID "
                            + handle.getRaceTracker().getID() + " didn't appear within "
                            + RaceTracker.TIMEOUT_FOR_RECEIVING_RACE_DEFINITION_IN_MILLISECONDS
                            + "ms. Maybe it will later...");
                } else {
                    logger.info("Race " + race + " showed up during restoring by tracker " + handle.getRaceTracker()
                            + " with ID " + handle.getRaceTracker().getID());
                }
                int newNumberOfTrackedRacesRestored = numberOfTrackedRacesRestored.incrementAndGet();
                logger.info("Added race to restore #"+newNumberOfTrackedRacesRestored+"/"+numberOfTrackedRacesToRestore);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Exception trying to restore race "+params, e);
            }
        }).getNumberOfParametersToLoad();
    }

    @Override
    public boolean isCurrentlyFillingFromInitialLoad() {
        return currentlyFillingFromInitialLoad;
    }

    @Override
    public void setCurrentlyFillingFromInitialLoad(boolean currentlyFillingFromInitialLoad) {
        this.currentlyFillingFromInitialLoad = currentlyFillingFromInitialLoad;
    }

    @Override
    public boolean isCurrentlyApplyingOperationReceivedFromMaster() {
        return currentlyApplyingOperationReceivedFromMaster.get();
    }

    @Override
    public void setCurrentlyApplyingOperationReceivedFromMaster(boolean currentlyApplyingOperationReceivedFromMaster) {
        this.currentlyApplyingOperationReceivedFromMaster.set(currentlyApplyingOperationReceivedFromMaster);
    }

    @Override
    public PolarDataService getPolarDataService() {
        return polarDataService;
    }

    @Override
    public SimulationService getSimulationService() {
        return simulationService;
    }
    
    @Override
    public TaggingService getTaggingService() {
        return taggingService;
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
            stopTracking(regatta, /* willBeRemoved */ true);
            removeRegatta(regatta);
        }
        for (Event event : new ArrayList<>(this.eventsById.values())) {
            removeEvent(event.getId());
        }
        for (MediaTrack mediaTrack : this.mediaLibrary.allTracks()) {
            mediaTrackDeleted(mediaTrack);
        }
        // TODO clear user store? See bug 2430.
        this.competitorAndBoatStore.clear();
        this.windStore.clear();
        getRaceLogStore().clear();
        getRegattaLogStore().clear();
        anniversaryRaceDeterminator.clear();
        raceChangeObserverForAnniversaryDetection.clear();
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

    public void ensureOwnerships() {
        SecurityService securityService = getSecurityService();
        securityService.assumeOwnershipMigrated(SecuredDomainType.MANAGE_MARK_PASSINGS.getName());
        securityService.assumeOwnershipMigrated(SecuredDomainType.RACE_MANAGER_APP_DEVICE_CONFIGURATION.getName());
        securityService.assumeOwnershipMigrated(SecuredDomainType.RESULT_IMPORT_URL.getName());
        securityService.assumeOwnershipMigrated(SecuredDomainType.MANAGE_MARK_POSITIONS.getName());
        securityService.assumeOwnershipMigrated(SecuredDomainType.CAN_REPLAY_DURING_LIVE_RACES.getName());
        securityService.assumeOwnershipMigrated(SecuredDomainType.DETAIL_TIMER.getName());
        securityService.assumeOwnershipMigrated(SecuredDomainType.DATA_MINING.getName());
        securityService.assumeOwnershipMigrated(SecuredDomainType.REPLICATOR.getName());

        for (Event event : getAllEvents()) {
            securityService.migrateOwnership(event);
        }
        securityService.assumeOwnershipMigrated(SecuredDomainType.EVENT.getName());
        for (Regatta regatta : getAllRegattas()) {
            securityService.migrateOwnership(regatta);
            // FIXME add listener for all TrackedRaces here and migrate them as they become available!
            DynamicTrackedRegatta trackedRegatta = getTrackedRegatta(regatta);
            if (trackedRegatta != null) {
                trackedRegatta.lockTrackedRacesForRead();
                try {
                    for (DynamicTrackedRace trackedRace : trackedRegatta.getTrackedRaces()) {
                        securityService.migrateOwnership(trackedRace);
                    }
                } finally {
                    trackedRegatta.unlockTrackedRacesAfterRead();
                }
            }
        }
        securityService.assumeOwnershipMigrated(SecuredDomainType.TRACKED_RACE.getName());
        securityService.assumeOwnershipMigrated(SecuredDomainType.REGATTA.getName());
        for (Leaderboard leaderboard : getLeaderboards().values()) {
            securityService.migrateOwnership(leaderboard);
        }
        securityService.assumeOwnershipMigrated(SecuredDomainType.LEADERBOARD.getName());
        for (LeaderboardGroup leaderboardGroup : getLeaderboardGroups().values()) {
            securityService.migrateOwnership(leaderboardGroup);
        }
        securityService.assumeOwnershipMigrated(SecuredDomainType.LEADERBOARD_GROUP.getName());
        for (MediaTrack mediaTrack : getAllMediaTracks()) {
            securityService.migrateOwnership(mediaTrack);
        }
        securityService.assumeOwnershipMigrated(SecuredDomainType.MEDIA_TRACK.getName());
        for (Competitor competitor : getCompetitorAndBoatStore().getAllCompetitors()) {
            securityService.migrateOwnership(competitor);
        }
        securityService.assumeOwnershipMigrated(SecuredDomainType.COMPETITOR.getName());
        for (Boat boat : getCompetitorAndBoatStore().getBoats()) {
            securityService.migrateOwnership(boat);
        }
        securityService.assumeOwnershipMigrated(SecuredDomainType.BOAT.getName());
        securityService.checkMigration(SecuredDomainType.getAllInstances());
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
                onRegattaLikeAdded(regatta);
                regatta.addRaceColumnListener(raceLogReplicator);
                regatta.addRaceColumnListener(raceLogScoringReplicator);
            }
        } finally {
            LockUtil.unlockAfterWrite(regattasByNameLock);
        }
    }

    private Iterable<Pair<Event, Boolean>> loadStoredEvents() {
        Iterable<Pair<Event, Boolean>> loadedEventsWithRequireStoreFlag = domainObjectFactory.loadAllEvents(); 
        for (Pair<Event, Boolean> eventAndFlag : loadedEventsWithRequireStoreFlag) {
            Event event = eventAndFlag.getA();
            if (event.getId() != null)
                eventsById.put(event.getId(), event);
        }
        return loadedEventsWithRequireStoreFlag;
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
            onRegattaLikeAdded(((FlexibleLeaderboard) leaderboard).getRegattaLike());
            leaderboard.addRaceColumnListener(raceLogReplicator);
            leaderboard.addRaceColumnListener(raceLogScoringReplicator);
        }
        final LeaderboardScoreCorrectionNotifier scoreCorrectionListener = new LeaderboardScoreCorrectionNotifier(leaderboard);
        scoreCorrectionListenersByLeaderboard.put(leaderboard, scoreCorrectionListener);
        leaderboard.addScoreCorrectionListener(scoreCorrectionListener);
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
        CourseArea courseArea = getCourseArea(courseAreaId);
        FlexibleLeaderboard result = new FlexibleLeaderboardImpl(getRaceLogStore(), getRegattaLogStore(),
                leaderboardName, new ThresholdBasedResultDiscardingRuleImpl(discardThresholds), scoringScheme,
                courseArea);
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
        if (regatta == null) {
            throw new IllegalArgumentException("Cannot find regatta " + regattaIdentifier
                    + ". Hence, cannot create regatta leaderboard for it.");
        }
        final RegattaLeaderboard result = new RegattaLeaderboardImpl(regatta, new ThresholdBasedResultDiscardingRuleImpl(discardThresholds));
        result.setDisplayName(leaderboardDisplayName);
        if (getLeaderboardByName(result.getName()) != null) {
            throw new IllegalArgumentException("Leaderboard with name " + result.getName() + " already exists in "
                    + this);
        }
        logger.info("adding regatta leaderboard for regatta "
                + regatta.getName() + " (" + regatta.hashCode() + ")" + " to " + this);
        addLeaderboard(result);
        mongoObjectFactory.storeLeaderboard(result);
        return result;
    }

    @Override
    public RegattaLeaderboardWithEliminations addRegattaLeaderboardWithEliminations(String leaderboardName,
            String leaderboardDisplayName, RegattaLeaderboard fullRegattaLeaderboard) {
        if (fullRegattaLeaderboard == null) {
            throw new NullPointerException("Must provide a valid regatta leaderboard, not null");
        }
        if (getLeaderboardByName(leaderboardName) != null) {
            throw new IllegalArgumentException("Leaderboard with name "+leaderboardName+" already exists in "+this);
        }
        final RegattaLeaderboardWithEliminations result = new DelegatingRegattaLeaderboardWithCompetitorElimination(
                ()->(RegattaLeaderboard) fullRegattaLeaderboard, leaderboardName);
        result.setDisplayName(leaderboardDisplayName);
        logger.info("adding regatta leaderboard with eliminations for regatta leaderboard "
                + fullRegattaLeaderboard.getName() + " to " + this);
        addLeaderboard(result);
        mongoObjectFactory.storeLeaderboard(result);
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
        }
    }

    @Override
    public void updateStoredLeaderboard(Leaderboard leaderboard) {
        getMongoObjectFactory().storeLeaderboard(leaderboard);
    }

    @Override
    public void updateStoredRegatta(Regatta regatta) {
        if (regatta.isPersistent()) {
            mongoObjectFactory.storeRegatta(regatta);
        }
    }

    @Override
    public void removeLeaderboard(String leaderboardName) {
        Leaderboard leaderboard = removeLeaderboardFromLeaderboardsByName(leaderboardName);
        if (leaderboard != null) {
            leaderboard.removeRaceColumnListener(raceLogReplicator);
            leaderboard.removeRaceColumnListener(raceLogScoringReplicator);
            final ScoreCorrectionListener scoreCorrectionListener = scoreCorrectionListenersByLeaderboard.remove(leaderboard);
            if (scoreCorrectionListener != null) {
                leaderboard.getScoreCorrection().removeScoreCorrectionListener(scoreCorrectionListener);
            }
            mongoObjectFactory.removeLeaderboard(leaderboardName);
            syncGroupsAfterLeaderboardRemove(leaderboardName, true);
            if (leaderboard instanceof FlexibleLeaderboard) {
                onRegattaLikeRemoved(((FlexibleLeaderboard) leaderboard).getRegattaLike());
            }
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
     * Checks all groups, if they contain a leaderboard with the <code>removedLeaderboardName</code> or reference it as their
     * overall leaderboard and removes it from the group or unlinks it as the overall leaderboard, respectively.
     * 
     * @param removedLeaderboardName
     */
    private void syncGroupsAfterLeaderboardRemove(String removedLeaderboardName, boolean doDatabaseUpdate) {
        boolean groupNeedsUpdate = false;
        for (LeaderboardGroup leaderboardGroup : leaderboardGroupsByName.values()) {
            for (final Leaderboard leaderboard : leaderboardGroup.getLeaderboards()) {
                if (leaderboard.getName().equals(removedLeaderboardName)) {
                    leaderboardGroup.removeLeaderboard(leaderboard);
                    groupNeedsUpdate = true;
                    // TODO we assume that the leaderboard names are unique, so we can break the inner loop here
                    break;
                }
            }
            if (leaderboardGroup.getOverallLeaderboard() != null && leaderboardGroup.getOverallLeaderboard().getName().equals(removedLeaderboardName)) {
                leaderboardGroup.setOverallLeaderboard(null);
                groupNeedsUpdate = true;
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
    public Position getMarkPosition(Mark mark, LeaderboardThatHasRegattaLike leaderboard, TimePoint timePoint) {
        GPSFixTrack<Mark, GPSFix> track = null;
        // If no spanning track is found, the fix closest to the time point requested is used instead
        GPSFix nonSpanningFallback = null;
        for (TrackedRace trackedRace : leaderboard.getTrackedRaces()) {
            final GPSFixTrack<Mark, GPSFix> trackCandidate = trackedRace.getTrack(mark);
            if (trackCandidate != null) {
                if (spansTimePoint(trackCandidate, timePoint)) {
                    track = trackCandidate;
                    break;
                } else {
                    nonSpanningFallback = improveTimewiseClosestFix(nonSpanningFallback, trackCandidate, timePoint);
                }
            }
        }
        final Position result; 
        if (track != null) {
            result = track.getEstimatedPosition(timePoint, /* extrapolate */ false);
        } else {
            result = nonSpanningFallback == null ? null : nonSpanningFallback.getPosition();
        }
        return result;
    }

    private GPSFix improveTimewiseClosestFix(GPSFix nonSpanningFallback, GPSFixTrack<Mark, GPSFix> track, final TimePoint timePoint) {
        GPSFix lastAtOrBefore = track.getLastFixAtOrBefore(timePoint);
        GPSFix firstAtOrAfter = track.getFirstFixAtOrAfter(timePoint);
        // find the fix closes to timePoint, sorting null values to the end and fixes near timePoint to the beginning
        final List<GPSFix> list = Arrays.asList(nonSpanningFallback, lastAtOrBefore, firstAtOrAfter);
        list.sort(new Comparator<GPSFix>() {
            @Override
            public int compare(GPSFix o1, GPSFix o2) {
                final int result;
                if (o1 == null) {
                    if (o2 == null) {
                        result = 0;
                    } else {
                        result = 1;
                    }
                } else if (o2 == null) {
                    result = -1;
                } else {
                    result = new Long(Math.abs(o1.getTimePoint().until(timePoint).asMillis())).compareTo(
                            Math.abs(o2.getTimePoint().until(timePoint).asMillis()));
                }
                return result;
            }
        });
        return list.get(0);
    }

    private boolean spansTimePoint(GPSFixTrack<Mark, GPSFix> track, TimePoint timePoint) {
        return track.getLastFixAtOrBefore(timePoint) != null && track.getFirstFixAtOrAfter(timePoint) != null;
    }

    @Override
    public Map<String, Leaderboard> getLeaderboards() {
        return Collections.unmodifiableMap(new HashMap<String, Leaderboard>(leaderboardsByName));
    }

    @Override
    public SailingServerConfiguration getSailingServerConfiguration() {
        return sailingServerConfiguration;
    }
    
    @Override
    public void updateServerConfiguration(SailingServerConfiguration serverConfiguration) {
        this.sailingServerConfiguration = serverConfiguration;
        mongoObjectFactory.storeServerConfiguration(serverConfiguration);
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
                final RaceDefinition race = tracker.getRace();
                if (race == r) {
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
            result = new RegattaImpl(getRaceLogStore(), getRegattaLogStore(), name, getBaseDomainFactory()
                    .getOrCreateBoatClass(boatClassName), /* canBoatsOfCompetitorsChangePerRace*/ false, CompetitorRegistrationType.CLOSED,
                    /* startDate */null, /* endDate */null, this,
                    getBaseDomainFactory().createScoringScheme(ScoringSchemeType.LOW_POINT), id, /* course area */ null);
            logger.info("Created default regatta " + result.getName() + " (" + hashCode() + ") on " + this);
            onRegattaLikeAdded(result);
            cacheAndReplicateDefaultRegatta(result);
        }
        return result;
    }

    private void onRegattaLikeAdded(IsRegattaLike isRegattaLike) {
        isRegattaLike.addListener(regattaLogReplicator);
    }

    private void onRegattaLikeRemoved(IsRegattaLike isRegattaLike) {
        isRegattaLike.removeListener(regattaLogReplicator);
        getRegattaLogStore().removeRegattaLog(isRegattaLike.getRegattaLikeIdentifier());
    }

    @Override
    public Regatta createRegatta(String fullRegattaName, String boatClassName, boolean canBoatsOfCompetitorsChangePerRace,
            CompetitorRegistrationType competitorRegistrationType, String registrationLinkSecret, TimePoint startDate, TimePoint endDate,
            Serializable id, Iterable<? extends Series> series, boolean persistent, ScoringScheme scoringScheme,
            Serializable defaultCourseAreaId, Double buoyZoneRadiusInHullLengths, boolean useStartTimeInference, boolean controlTrackingFromStartAndFinishTimes,
            RankingMetricConstructor rankingMetricConstructor) {
        if (useStartTimeInference && controlTrackingFromStartAndFinishTimes) {
            throw new IllegalArgumentException("Cannot set both of useStartTimeInference and controlTrackingFromStartAndFinishTimes to true");
        }
        com.sap.sse.common.Util.Pair<Regatta, Boolean> regattaWithCreatedFlag = getOrCreateRegattaWithoutReplication(
                fullRegattaName, boatClassName, canBoatsOfCompetitorsChangePerRace, competitorRegistrationType, registrationLinkSecret, startDate, endDate, id, series, persistent, scoringScheme,
                defaultCourseAreaId, buoyZoneRadiusInHullLengths, useStartTimeInference, controlTrackingFromStartAndFinishTimes, rankingMetricConstructor);
        Regatta regatta = regattaWithCreatedFlag.getA();
        if (regattaWithCreatedFlag.getB()) {
            onRegattaLikeAdded(regatta);
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

    private RaceLogStore getRaceLogStore() {
        return MongoRaceLogStoreFactory.INSTANCE.getMongoRaceLogStore(mongoObjectFactory, domainObjectFactory);
    }

    private RegattaLogStore getRegattaLogStore() {
        return MongoRegattaLogStoreFactory.INSTANCE.getMongoRegattaLogStore(mongoObjectFactory, domainObjectFactory);
    }

    @Override
    public com.sap.sse.common.Util.Pair<Regatta, Boolean> getOrCreateRegattaWithoutReplication(String fullRegattaName,
            String boatClassName, boolean canBoatsOfCompetitorsChangePerRace,
            CompetitorRegistrationType competitorRegistrationType, String registrationLinkSecret, TimePoint startDate,
            TimePoint endDate, Serializable id, Iterable<? extends Series> series, boolean persistent,
            ScoringScheme scoringScheme, Serializable defaultCourseAreaId, Double buoyZoneRadiusInHullLengths,
            boolean useStartTimeInference, boolean controlTrackingFromStartAndFinishTimes,
            RankingMetricConstructor rankingMetricConstructor) {
        CourseArea courseArea = getCourseArea(defaultCourseAreaId);
        Regatta regatta = new RegattaImpl(getRaceLogStore(), getRegattaLogStore(), fullRegattaName,
                getBaseDomainFactory().getOrCreateBoatClass(boatClassName), canBoatsOfCompetitorsChangePerRace, competitorRegistrationType, startDate, endDate, series, persistent,
                scoringScheme, id, courseArea, buoyZoneRadiusInHullLengths, useStartTimeInference, controlTrackingFromStartAndFinishTimes, rankingMetricConstructor);
        regatta.setRegistrationLinkSecret(registrationLinkSecret);
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
            long timeoutInMilliseconds, RaceTrackingHandler raceTrackingHandler) throws Exception {
        final Object trackerID = params.getTrackerID();
        NamedReentrantReadWriteLock raceTrackersByIdLock = lockRaceTrackersById(trackerID);
        try {
            RaceTracker tracker = raceTrackersByID.get(trackerID);
            if (tracker == null) {
                Regatta regatta = regattaToAddTo == null ? null : getRegatta(regattaToAddTo);
                if (regatta == null) {
                    // create tracker and use an existing or create a default regatta
                    tracker = params.createRaceTracker(this, windStore, /* raceLogResolver */ this, /* leaderboardGroupResolver */ this, timeoutInMilliseconds,
                            raceTrackingHandler);
                } else {
                    // use the regatta selected by the RaceIdentifier regattaToAddTo
                    tracker = params.createRaceTracker(regatta, this, windStore, /* raceLogResolver */ this, /* leaderboardGroupResolver */ this, timeoutInMilliseconds,
                            raceTrackingHandler);
                    assert tracker.getRegatta() == regatta;
                }
                LockUtil.lockForWrite(raceTrackersByRegattaLock);
                try {
                    raceTrackersByID.put(tracker.getID(), tracker);
                    Set<RaceTracker> trackers = raceTrackersByRegatta.get(tracker.getRegatta());
                    if (trackers == null) {
                        trackers = Collections.newSetFromMap(new ConcurrentHashMap<RaceTracker, Boolean>());
                        raceTrackersByRegatta.put(tracker.getRegatta(), trackers);
                    }
                    trackers.add(tracker);
                    notifyListenersForNewRaceTracker(tracker);
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
                getMongoObjectFactory().addConnectivityParametersForRaceToRestore(params);
                // ensure that as soon as the RaceDefinition becomes available, the connectivity params are linked to it in connectivityParametersByRace
                tracker.add((RaceTracker t) -> rememberConnectivityParametersForRace(t));
                if (params.isTrackWind()) {
                    // start wind tracking if requested, as soon as the RaceDefinition becomes available
                    tracker.add((RaceTracker t) ->
                        new Thread(()->startTrackingWind(regattaWithName, t.getRace(), params.isCorrectWindDirectionByMagneticDeclination()),
                                   "Starting wind trackers for race "+t.getRace()).start());
                }
            } else {
                logger.warning("Race tracker with ID "+trackerID+" already found; not tracking twice to avoid race duplication");
                WindStore existingTrackersWindStore = tracker.getWindStore();
                if (!existingTrackersWindStore.equals(windStore)) {
                    logger.warning("Wind store mismatch. Requested wind store: " + windStore
                            + ". Wind store in use by existing tracker: " + existingTrackersWindStore);
                }
            }
            if (timeoutInMilliseconds != -1) {
                scheduleAbortTrackerAfterInitialTimeout(tracker, timeoutInMilliseconds);
            }
            return tracker.getRaceHandle();
        } finally {
            unlockRaceTrackersById(trackerID, raceTrackersByIdLock);
        }
    }

    /**
     * Remembers the link between the {@link RaceDefinition} that the {@code tracker} just produced and its
     * {@link RaceTracker#getConnectivityParams() connectivity parameters}. This is important for later removing those
     * connectivity parameters from the
     * {@link MongoObjectFactory#removeConnectivityParametersForRaceToRestore(RaceTrackingConnectivityParameters) DB}
     * when the {@link #removeRace(Regatta, RaceDefinition) race is removed}.
     * 
     * @param tracker
     *            must have produced a {@link RaceDefinition} which can be guaranteed by waiting for the callback on a
     *            {@link RaceTracker.RaceCreationListener}
     *            {@link RaceTracker#add(com.sap.sailing.domain.tracking.RaceTracker.RaceCreationListener) registered}
     *            on that tracker and not calling this method before that listener has fired.
     */
    private void rememberConnectivityParametersForRace(RaceTracker tracker) {
        final RaceDefinition race = tracker.getRace(); // guaranteed to be != null by callback
        assert race != null;
        final RaceTrackingConnectivityParameters connectivityParams = tracker.getConnectivityParams();
        connectivityParametersByRace.put(race, connectivityParams);
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
        replicate(new AddSpecificRegatta(regatta.getName(),
                regatta.getBoatClass() == null ? null : regatta.getBoatClass().getName(),
                regatta.canBoatsOfCompetitorsChangePerRace(), regatta.getCompetitorRegistrationType(),
                /* registrationLinkSecret */ null, regatta.getStartDate(), regatta.getEndDate(), regatta.getId(),
                getSeriesWithoutRaceColumnsConstructionParametersAsMap(regatta), regatta.isPersistent(),
                regatta.getScoringScheme(), courseAreaId, regatta.getBuoyZoneRadiusInHullLengths(),
                regatta.useStartTimeInference(), regatta.isControlTrackingFromStartAndFinishTimes(),
                regatta.getRankingMetricType()));
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
                    new SeriesCreationParametersDTO(fleetNamesAndOrdering, s.isMedal(), s.isFleetsCanRunInParallel(), s.isStartsWithZeroScore(), s
                            .isFirstColumnIsNonDiscardableCarryForward(), s.getResultDiscardingRule() == null ? null
                            : s.getResultDiscardingRule().getDiscardIndexResultsStartingWithHowManyRaces(), s
                            .hasSplitFleetContiguousScoring(), s.getMaximumNumberOfDiscards()));
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
            // that name, so we need to check again:
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
                            .getBoatClass().getName(), regatta.getStartDate(), regatta.getEndDate(), regatta.getId()));
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
            long delayToLiveInMillis, long millisecondsOverWhichToAverageWind,
            long millisecondsOverWhichToAverageSpeed, boolean useMarkPassingCalculator) {
        DynamicTrackedRegatta trackedRegatta = getOrCreateTrackedRegatta(getRegatta(raceIdentifier));
        RaceDefinition race = getRace(raceIdentifier);
        return trackedRegatta.createTrackedRace(race, Collections.<Sideline> emptyList(), windStore,
                delayToLiveInMillis, millisecondsOverWhichToAverageWind, millisecondsOverWhichToAverageSpeed,
                /* raceDefinitionSetToUpdate */null, useMarkPassingCalculator, /* raceLogResolver */ this,
                Optional.of(this.getThreadLocalTransporterForCurrentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster()));
    }

    private void ensureRegattaIsObservedForDefaultLeaderboardAndAutoLeaderboardLinking(
            DynamicTrackedRegatta trackedRegatta) {
        if (regattasObservedForDefaultLeaderboard.add(trackedRegatta)) {
            trackedRegatta.addRaceListener(new RaceAdditionListener(),
                    /* ThreadLocalTransporter */ Optional.empty(), // registering for synchronous callbacks; no thread locals need to be transported
                    /* register for synchronous execution in order to ensure that any replication-related effects happen before
                     * any subsequent replication operations referring to a new race hit the outbound replication queue
                     */ true);
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
     * race. When a tracked race is removed, the {@link TrackedRaceReplicatorAndNotifier} that was added as listener to that
     * tracked race is removed again.
     * 
     * A {@link PolarFixCacheUpdater} is added to every race so that polar fixes are aggregated when new GPS fixes
     * arrive.
     * 
     * @author Axel Uhl (d043530)
     * 
     */
    private class RaceAdditionListener implements RaceListener, Serializable {
        private static final long serialVersionUID = 1036955460477000265L;

        private final Map<TrackedRace, TrackedRaceReplicatorAndNotifier> trackedRaceReplicators;

        private final Map<TrackedRace, PolarFixCacheUpdater> polarFixCacheUpdaters;

        public RaceAdditionListener() {
            this.trackedRaceReplicators = new HashMap<TrackedRace, TrackedRaceReplicatorAndNotifier>();
            this.polarFixCacheUpdaters = new HashMap<TrackedRace, PolarFixCacheUpdater>();
        }

        @Override
        public void raceRemoved(TrackedRace trackedRace) {
            TrackedRaceReplicatorAndNotifier trackedRaceReplicator = trackedRaceReplicators.remove(trackedRace);
            if (trackedRaceReplicator != null) {
                trackedRace.removeListener(trackedRaceReplicator);
            }
            PolarFixCacheUpdater polarFixCacheUpdater = polarFixCacheUpdaters.remove(trackedRace);
            if (polarFixCacheUpdater != null) {
                trackedRace.removeListener(polarFixCacheUpdater);
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
            TrackedRaceReplicatorAndNotifier trackedRaceReplicator = new TrackedRaceReplicatorAndNotifier(trackedRace);
            trackedRaceReplicators.put(trackedRace, trackedRaceReplicator);
            trackedRace.addListener(trackedRaceReplicator, /* fire wind already loaded */true, /* notifyAboutGPSFixesAlreadyLoaded */ true);

            PolarFixCacheUpdater polarFixCacheUpdater = new PolarFixCacheUpdater(trackedRace);
            polarFixCacheUpdaters.put(trackedRace, polarFixCacheUpdater);
            trackedRace.addListener(polarFixCacheUpdater);
            
            if (polarDataService != null) {
                trackedRace.setPolarDataService(polarDataService);
            }
        }
    }
    
    /**
     * A score correction listener for a leaderboard that notifies interested users through the
     * {@link RacingEventServiceImpl#notificationService} if one is available.
     * 
     * @author Axel Uhl (d043530)
     *
     */
    private class LeaderboardScoreCorrectionNotifier implements ScoreCorrectionListener {
        /**
         * We don't want to flood the users with notifications about what's basically caused by the
         * same original event. For example, when many single score correction updates are applied to
         * the same leaderboard, we don't want to notify users for each such change if they are generally
         * interested in new results for that leaderboard or this boat class.
         */
        private final Duration HOW_LONG_BETWEEN_TWO_NOTIFICATIONS_FOR_SIMILAR_EVENT = Duration.ONE_MINUTE.times(5);
        
        private TimePoint lastNotificationForLeaderboard;
        
        private final ConcurrentHashMap<Competitor, TimePoint> lastNotificationForCompetitor;
        
        private final Leaderboard leaderboard;

        /**
         * Callers are expected to {@link SettableScoreCorrection#addScoreCorrectionListener(ScoreCorrectionListener)
         * register} this listener as a {@link ScoreCorrectionListener} themselves.
         */
        public LeaderboardScoreCorrectionNotifier(Leaderboard leaderboard) {
            this.leaderboard = leaderboard;
            this.lastNotificationForCompetitor = new ConcurrentHashMap<>();
        }

        @Override
        public void correctedScoreChanged(Competitor competitor, RaceColumn raceColumn, Double oldCorrectedScore,
                Double newCorrectedScore) {
            notifyForCompetitorIfNotAlreadyNotifiedRecently(competitor, raceColumn);
        }

        @Override
        public void maxPointsReasonChanged(Competitor competitor, RaceColumn raceColumn, MaxPointsReason oldMaxPointsReason, MaxPointsReason newMaxPointsReason) {
            notifyForCompetitorIfNotAlreadyNotifiedRecently(competitor, raceColumn);
        }

        @Override
        public void carriedPointsChanged(Competitor competitor, Double oldCarriedPoints, Double newCarriedPoints) {
            notifyForCompetitorIfNotAlreadyNotifiedRecently(competitor, /* no raceColumn in case of carried points */ null);
        }

        @Override
        public void isSuppressedChanged(Competitor competitor, boolean newIsSuppressed) {
            // do nothing
        }

        @Override
        public void timePointOfLastCorrectionsValidityChanged(TimePoint oldTimePointOfLastCorrectionsValidity,
                TimePoint newTimePointOfLastCorrectionsValidity) {
            notifyForLeaderboardIfNotAlreadyNotifiedRecently();
        }

        @Override
        public void commentChanged(String oldComment, String newComment) {
            notifyForLeaderboardIfNotAlreadyNotifiedRecently();
        }

        private void notifyForLeaderboardIfNotAlreadyNotifiedRecently() {
            final TimePoint now = MillisecondsTimePoint.now();
            if (notificationService != null && (lastNotificationForLeaderboard == null ||
                lastNotificationForLeaderboard.until(now).compareTo(HOW_LONG_BETWEEN_TWO_NOTIFICATIONS_FOR_SIMILAR_EVENT) >= 0)) {
                    scheduler.execute(()->notificationService.notifyUserOnBoatClassWhenScoreCorrectionsAreAvailable(
                                        leaderboard.getBoatClass(), leaderboard));
                lastNotificationForLeaderboard = now;
            }
        }
        
        /**
         * @param raceColumn
         *            may be {@code null} which means that something may have changed for the competitor outside of a
         *            specific race column, such as the carried points
         */
        private void notifyForCompetitorIfNotAlreadyNotifiedRecently(Competitor competitor, RaceColumn raceColumn) {
            final TimePoint now = MillisecondsTimePoint.now();
            if (notificationService != null && (!lastNotificationForCompetitor.containsKey(competitor) ||
                    lastNotificationForCompetitor.get(competitor).until(now).compareTo(HOW_LONG_BETWEEN_TWO_NOTIFICATIONS_FOR_SIMILAR_EVENT) >= 0)) {
                scheduler.execute(()->notificationService.notifyUserOnCompetitorScoreCorrections(competitor, leaderboard));
                lastNotificationForCompetitor.put(competitor, now);
            }
            // a change to a single competitor also means a change to the leaderboard
            notifyForLeaderboardIfNotAlreadyNotifiedRecently();
        }

    }

    private class PolarFixCacheUpdater extends AbstractRaceChangeListener {

        private final TrackedRace race;

        public PolarFixCacheUpdater(TrackedRace race) {
            this.race = race;
        }

        @Override
        public void competitorPositionChanged(GPSFixMoving fix, Competitor item) {
            if (polarDataService != null) {
                polarDataService.competitorPositionChanged(fix, item, race);
            }
        }
        
        @Override
        public void statusChanged(TrackedRaceStatus newStatus, TrackedRaceStatus oldStatus) {
            if (oldStatus.getStatus() == TrackedRaceStatusEnum.LOADING
                    && newStatus.getStatus() != TrackedRaceStatusEnum.LOADING && newStatus.getStatus() != TrackedRaceStatusEnum.REMOVED) {
                if (polarDataService != null) {
                    polarDataService.raceFinishedLoading(race);
                }
            }
        }

    }

    /**
     * When changes occur on a {@link TrackedRace}, this object will be notified in its role of being a
     * {@link RaceChangeListener}. It does two things: replicate the changes to replica servers and, potentially, if
     * this is a replica, back to a master; notify users who expressed a corresponding interest about the change if we
     * have a {@link RacingEventServiceImpl##notificationService} available.
     * 
     * @author Axel Uhl (d043530)
     *
     */
    private class TrackedRaceReplicatorAndNotifier implements RaceChangeListener {
        private final TrackedRace trackedRace;

        public TrackedRaceReplicatorAndNotifier(TrackedRace trackedRace) {
            this.trackedRace = trackedRace;
        }

        @Override
        public void windSourcesToExcludeChanged(Iterable<? extends WindSource> windSourcesToExclude) {
            replicate(new UpdateWindSourcesToExclude(getRaceIdentifier(), windSourcesToExclude));
        }

        @Override
        public void startOfTrackingChanged(TimePoint oldStartOfTracking, TimePoint newStartOfTracking) {
            replicate(new UpdateStartOfTracking(getRaceIdentifier(), newStartOfTracking));
        }

        @Override
        public void endOfTrackingChanged(TimePoint oldEndOfTracking, TimePoint newEndOfTracking) {
            replicate(new UpdateEndOfTracking(getRaceIdentifier(), newEndOfTracking));
        }

        @Override
        public void startTimeReceivedChanged(TimePoint startTimeReceived) {
            replicate(new UpdateStartTimeReceived(getRaceIdentifier(), startTimeReceived));
        }

        @Override
        public void startOfRaceChanged(TimePoint oldStartOfRace, TimePoint newStartOfRace) {
            // no replication action required; the update signaled by this call is implicit; for explicit updates
            // see raceTimesChanged(TimePoint, TimePoint, TimePoint).
            
            if (newStartOfRace != null && newStartOfRace.after(MillisecondsTimePoint.now())) {
                scheduler.execute(()->
                    // Notify interested users if the new start time is in the future
                    notificationService.notifyUserOnBoatClassUpcomingRace(trackedRace.getRace().getBoatClass(),
                        getMostAppropriateLeaderboard(), getMostAppropriateRaceColumn(), getMostAppropriateFleet(), newStartOfRace));
            }
        }

        @Override
        public void finishedTimeChanged(TimePoint oldFinishedTime, TimePoint newFinishedTime) {
            // no action required; the update signaled by this call is implicit; the race log
            // updates that led to this change are replicated separately
            if (newFinishedTime != null && newFinishedTime.after(MillisecondsTimePoint.now().minus(Duration.ONE_HOUR))) {
                scheduler.execute(()->
                    // Notify interested users:
                    notificationService.notifyUserOnBoatClassRaceChangesStateToFinished(trackedRace.getRace().getBoatClass(), trackedRace,
                            getMostAppropriateLeaderboard(), getMostAppropriateRaceColumn(), getMostAppropriateFleet()));
            }
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
        public void statusChanged(TrackedRaceStatus newStatus, TrackedRaceStatus oldStatus) {
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
            final MarkPassing last = Util.last(markPassings);
            if (last != null && last.getWaypoint() == trackedRace.getRace().getCourse().getLastWaypoint() &&
                    trackedRace.getStatus().getStatus() != TrackedRaceStatusEnum.LOADING &&
                    last.getTimePoint().after(MillisecondsTimePoint.now().minus(Duration.ONE_HOUR))) {
                scheduler.execute(() ->
                    // Notify interested users:
                    notificationService.notifyUserOnCompetitorPassesFinish(competitor, trackedRace,
                        getMostAppropriateLeaderboard(), getMostAppropriateRaceColumn(), getMostAppropriateFleet()));
            }
        }

        @Override
        public void speedAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage) {
            replicate(new UpdateWindAveragingTime(getRaceIdentifier(), newMillisecondsOverWhichToAverage));
        }
        
        @Override
        public void competitorSensorTrackAdded(DynamicSensorFixTrack<Competitor, ?> track) {
            replicate(new RecordCompetitorSensorFixTrack(getRaceIdentifier(), track));
        }
        
        @Override
        public void competitorSensorFixAdded(Competitor competitor, String trackName, SensorFix fix) {
            replicate(new RecordCompetitorSensorFix(getRaceIdentifier(), competitor, trackName, fix));
        }

        private RegattaAndRaceIdentifier getRaceIdentifier() {
            return trackedRace.getRaceIdentifier();
        }

        @Override
        public void regattaLogAttached(RegattaLog regattaLog) {
            // no action required
        }
        
        @Override
        public void raceLogAttached(RaceLog regattaLog) {
            // no action required
        }
        
        @Override
        public void raceLogDetached(RaceLog raceLog) {
            // no action required
        }

        private Leaderboard getMostAppropriateLeaderboard() {
            final Triple<Leaderboard, RaceColumn, Fleet> slot = findMostAppropriateLeaderboardSlot();
            return slot == null ? null : slot.getA();
        }
        
        private RaceColumn getMostAppropriateRaceColumn() {
            final Triple<Leaderboard, RaceColumn, Fleet> slot = findMostAppropriateLeaderboardSlot();
            return slot == null ? null : slot.getB();
        }

        private Fleet getMostAppropriateFleet() {
            final Triple<Leaderboard, RaceColumn, Fleet> slot = findMostAppropriateLeaderboardSlot();
            return slot == null ? null : slot.getC();
        }

        /**
         * When all we have is a {@link #trackedRace} and we're looking for a slot in a leaderboard, we have to
         * search the enclosing {@link RacingEventService} for a leaderboard that has the {@link #trackedRace}
         * in a {@link RaceColumn}/{@link Fleet} slot.<p>
         * 
         * As a first approximation we'll use the fact that a {@link RegattaLeaderboard}'s name is derived
         * from the {@link Regatta} and as such can be looked up in constant time. Only if such a regatta
         * leaderboard is not found, a search across all leaderboards will need to be carried out.<p>
         * 
         *  Note that this method should be called in a background thread that runs outside of the call stack
         *  of the notification sent to this {@link RaceChangeListener}, ideally as a task in an executor.
         *  This will avoid performance hits due to an attempt to send out notifications.
         */
        private Triple<Leaderboard, RaceColumn, Fleet> findMostAppropriateLeaderboardSlot() {
            final Regatta regatta = trackedRace.getTrackedRegatta().getRegatta();
            final String regattaLeaderboardName = RegattaLeaderboardImpl.getLeaderboardNameForRegatta(regatta);
            final Leaderboard regattaLeaderboard = getLeaderboardByName(regattaLeaderboardName);
            Leaderboard leaderboard = null;
            Pair<RaceColumn, Fleet> raceColumnAndFleet = null;
            if (regattaLeaderboard != null) {
                leaderboard = regattaLeaderboard;
                raceColumnAndFleet = regattaLeaderboard.getRaceColumnAndFleet(trackedRace);
            } else {
                for (final Leaderboard l : getLeaderboards().values()) {
                    final Pair<RaceColumn, Fleet> rcaf = l.getRaceColumnAndFleet(trackedRace);
                    if (rcaf != null) {
                        leaderboard = l;
                        raceColumnAndFleet = rcaf;
                        break;
                    }
                }
            }
            final Triple<Leaderboard, RaceColumn, Fleet> result;
            if (leaderboard != null && raceColumnAndFleet != null) {
                result = new Triple<>(leaderboard, raceColumnAndFleet.getA(), raceColumnAndFleet.getB());
            } else {
                result = null;
            }
            return result;
        }

        @Override
        public void firstGPSFixReceived() {
         // no action required
        }
    }

    /**
     * Based on the <code>trackedRace</code>'s {@link TrackedRace#getRaceIdentifier() race identifier}, the tracked race
     * is (re-)associated to all {@link RaceColumn race columns} that currently have no
     * {@link RaceColumn#getTrackedRace(Fleet) tracked race assigned} and whose
     * {@link RaceColumn#getRaceIdentifier(Fleet) race identifier} equals that of <code>trackedRace</code>.
     */
    private void linkRaceToConfiguredLeaderboardColumns(TrackedRace trackedRace) {
        RegattaAndRaceIdentifier trackedRaceIdentifier = trackedRace.getRaceIdentifier();
        for (Leaderboard leaderboard : getLeaderboards().values()) {
            for (RaceColumn column : leaderboard.getRaceColumns()) {
                for (Fleet fleet : column.getFleets()) {
                    if (trackedRaceIdentifier.equals(column.getRaceIdentifier(fleet))
                            && column.getTrackedRace(fleet) == null) {
                        column.setTrackedRace(fleet, trackedRace);
                        replicate(new ConnectTrackedRaceToLeaderboardColumn(leaderboard.getName(), column.getName(),
                                fleet.getName(), trackedRaceIdentifier));
                    }
                }
            }
        }
    }

    @Override
    public void stopTracking(Regatta regatta, boolean willBeRemoved) throws MalformedURLException, IOException, InterruptedException {
        final Set<RaceTracker> trackersForRegatta = raceTrackersByRegatta.get(regatta);
        if (trackersForRegatta != null) {
            for (RaceTracker raceTracker : trackersForRegatta) {
                final RaceDefinition race = raceTracker.getRace();
                if (race != null) {
                    stopTrackingWind(regatta, race);
                }
                raceTracker.stop(/* preemptive */false, willBeRemoved);
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
        stopTracking(regatta, /* willBeRemoved */ true);
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
            }
        }
    }

    /**
     * The tracker will initially try to connect to the tracking infrastructure to obtain basic race master data. If
     * this fails after some timeout, to avoid garbage and lingering threads, the task scheduled by this method will
     * check after the timeout expires if race master data was successfully received. If so, the tracker continues
     * normally. Otherwise, the tracker is shut down orderly by calling {@link RaceTracker#stop(boolean) stopping}.
     * 
     * @return the scheduled task, in case the caller wants to {@link ScheduledFuture#cancel(boolean) cancel} it, e.g.,
     *         when the tracker is stopped or has successfully received the race
     */
    private ScheduledFuture<?> scheduleAbortTrackerAfterInitialTimeout(final RaceTracker tracker,
            final long timeoutInMilliseconds) {
        ScheduledFuture<?> task = getScheduler().schedule(new Runnable() {
            @Override
            public void run() {
                if (tracker.getRace() == null) {
                    try {
                        Regatta regatta = tracker.getRegatta();
                        logger.log(Level.SEVERE, "RaceDefinition for a race in regatta " + regatta.getName()
                                + " not obtained within " + timeoutInMilliseconds
                                + "ms. Aborting tracker for this race.");
                        Set<RaceTracker> trackersForRegatta = raceTrackersByRegatta.get(regatta);
                        if (trackersForRegatta != null) {
                            trackersForRegatta.remove(tracker);
                        }
                        tracker.stop(/* preemptive */true, /* willBeRemoved */ true);
                        final Object trackerId = tracker.getID();
                        final NamedReentrantReadWriteLock lock = lockRaceTrackersById(trackerId);
                        try {
                            raceTrackersByID.remove(trackerId);
                        } finally {
                            unlockRaceTrackersById(trackerId, lock);
                        }
                        if (trackersForRegatta == null || trackersForRegatta.isEmpty()) {
                            stopTracking(regatta, /* willBeRemoved */ true);
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
        stopTracking(regatta, raceTracker -> raceTracker.getRace() == race, () -> {
            try {
                stopTrackingWind(regatta, race);
                final RaceTrackingConnectivityParameters connectivityParams = connectivityParametersByRace.get(race);
                // update the "restore" handle for race in DB such that when restoring, no wind tracker will be requested for race
                if (connectivityParams != null) {
                    if (connectivityParams.isTrackWind()) {
                        connectivityParams.setTrackWind(false);
                        getMongoObjectFactory().addConnectivityParametersForRaceToRestore(connectivityParams);
                    }
                } else {
                    logger.warning("Would have expected to find connectivity params for race "+race+" but didn't");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    @Override
    public void stopTracker(Regatta regatta, RaceTracker tracker)
            throws MalformedURLException, IOException, InterruptedException {
        stopTracking(regatta, raceTracker -> raceTracker == tracker, () -> {});
    }
    
    private void stopTracking(Regatta regatta, Predicate<RaceTracker> matcher, Runnable actionBeforePotentiallyRemovingTrackedRegatta) throws MalformedURLException, IOException,
    InterruptedException {
        final Set<RaceTracker> trackerSet = raceTrackersByRegatta.get(regatta);
        if (trackerSet != null) {
            Iterator<RaceTracker> trackerIter = trackerSet.iterator();
            while (trackerIter.hasNext()) {
                RaceTracker raceTracker = trackerIter.next();
                if (matcher.test(raceTracker)) {
                    logger.info("Found tracker to stop for races " + raceTracker.getRace());
                    raceTracker.stop(/* preemptive */false);
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
        actionBeforePotentiallyRemovingTrackedRegatta.run();
        // if the last tracked race was removed, confirm that tracking for the entire regatta has stopped
        if (trackerSet == null || trackerSet.isEmpty()) {
            stopTracking(regatta, /* willBeRemoved */ false);
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
        // avoid ConcurrentModificationException by copying the races to remove:
        Set<RaceDefinition> racesToRemove = new HashSet<>();
        Util.addAll(regatta.getAllRaces(), racesToRemove);
        for (RaceDefinition race : racesToRemove) {
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
        onRegattaLikeRemoved(regatta);
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
    public Regatta updateRegatta(RegattaIdentifier regattaIdentifier, TimePoint startDate, TimePoint endDate,
            Serializable newDefaultCourseAreaId, RegattaConfiguration newRegattaConfiguration,
            Iterable<? extends Series> series, Double buoyZoneRadiusInHullLengths, boolean useStartTimeInference, boolean controlTrackingFromStartAndFinishTimes,
            String registrationLinkSecret) {
        if (useStartTimeInference && controlTrackingFromStartAndFinishTimes) {
            throw new IllegalArgumentException("Cannot set both of useStartTimeInference and controlTrackingFromStartAndFinishTimes to true");
        }
        // We're not doing any renaming of the regatta itself, therefore we don't have to sync on the maps.
        Regatta regatta = getRegatta(regattaIdentifier);
        CourseArea newCourseArea = getCourseArea(newDefaultCourseAreaId);
        if (newCourseArea != regatta.getDefaultCourseArea()) {
            regatta.setDefaultCourseArea(newCourseArea);
        }
        regatta.setStartDate(startDate);
        regatta.setEndDate(endDate);
        regatta.setBuoyZoneRadiusInHullLengths(buoyZoneRadiusInHullLengths);
        regatta.setControlTrackingFromStartAndFinishTimes(controlTrackingFromStartAndFinishTimes);
        regatta.setRegistrationLinkSecret(registrationLinkSecret);
        if (regatta.useStartTimeInference() != useStartTimeInference) {
            regatta.setUseStartTimeInference(useStartTimeInference);
            final DynamicTrackedRegatta trackedRegatta = getTrackedRegatta(regatta);
            if (trackedRegatta != null) {
                trackedRegatta.lockTrackedRacesForRead();
                try {
                    for (DynamicTrackedRace trackedRace : trackedRegatta.getTrackedRaces()) {
                        // the start times of the regatta's tracked races now have to be re-evaluated the next time they
                        // are queried
                        trackedRace.invalidateStartTime();
                    }
                } finally {
                    trackedRegatta.unlockTrackedRacesAfterRead();
                }
            }
        }
        regatta.setRegattaConfiguration(newRegattaConfiguration);
        if (series != null) {
            for (Series seriesObj : series) {
                regatta.addSeries(seriesObj);
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
        final RaceTrackingConnectivityParameters connectivityParams = connectivityParametersByRace.remove(race);
        if (connectivityParams != null) {
            getMongoObjectFactory().removeConnectivityParametersForRaceToRestore(connectivityParams);
        }
        stopAllTrackersForWhichRaceIsLastReachable(regatta, race);
        stopTrackingWind(regatta, race);
        TrackedRace trackedRace = getExistingTrackedRace(regatta, race);
        if (trackedRace != null) {
            TrackedRegatta trackedRegatta = getTrackedRegatta(regatta);
            final boolean isTrackedRacesBecameEmpty;
            if (trackedRegatta != null) {
                trackedRegatta.lockTrackedRacesForWrite();
                // The following fixes bug 202: when tracking of multiple races of the same event has been started, this may not
                // remove any race; however, the event may already have been created by another tracker whose race hasn't
                // arrived yet and therefore the races list is still empty; therefore, only remove the event if its
                // race list became empty by the removal performed here.
                final int oldSizeOfTrackedRaces;
                final int newSizeOfTrackedRaces;
                oldSizeOfTrackedRaces = Util.size(trackedRegatta.getTrackedRaces());
                try {
                    trackedRegatta.removeTrackedRace(trackedRace, Optional.of(
                            getThreadLocalTransporterForCurrentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster()));
                    newSizeOfTrackedRaces = Util.size(trackedRegatta.getTrackedRaces());
                    isTrackedRacesBecameEmpty = (oldSizeOfTrackedRaces > 0 && newSizeOfTrackedRaces == 0);
                } finally {
                    trackedRegatta.unlockTrackedRacesAfterWrite();
                }
            } else {
                isTrackedRacesBecameEmpty = false;
            }
            if (isTrackedRacesBecameEmpty) {
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
                if (raceTracker.getRace() == race) {
                    // firstly stop the tracker
                    raceTracker.stop(/* preemptive */true, /* willBeRemoved */ true);
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
                        stopTracking(regatta, /* willBeRemoved */ true);
                    }
                }
            }
        }
    }

    @Override
    public void startTrackingWind(Regatta regatta, RaceDefinition race, boolean correctByDeclination) {
        for (WindTrackerFactory windTrackerFactory : getWindTrackerFactories()) {
            try {
                windTrackerFactory.createWindTracker(getOrCreateTrackedRegatta(regatta), race, correctByDeclination);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error trying to track wind using wind tracker factory "+windTrackerFactory, e);
            }
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
                trackedRegattaListener.regattaAdded(result);
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
        trackedRegattaListener.regattaRemoved(trackedRegatta);
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
    public LeaderboardGroup resolveLeaderboardGroupByRegattaName(String regattaName) {
        for (LeaderboardGroup leaderboardGroup: getLeaderboardGroups().values()) {
            for (Leaderboard leaderboard: leaderboardGroup.getLeaderboards()) {
                if (leaderboard.getName().equals(regattaName)) {
                    return leaderboardGroup;
                }
            }
        }
        return null;
    }
    
    @Override
    public LeaderboardGroup addLeaderboardGroup(UUID leaderboardGroupId, String groupName, String description, String displayName,
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
        LeaderboardGroup result = new LeaderboardGroupImpl(leaderboardGroupId, groupName, description, displayName,
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
                for (final Event event : eventsById.values()) {
                    if (Util.contains(event.getLeaderboardGroups(), leaderboardGroup)) {
                        // unlink the leaderboard group from the event; note that the operation is not "apply"-ed to
                        // this service because it would redundantly replicate; a replica, however, would already have
                        // received the call to this method and should carry out the following statement locally.
                        // As such, using the operation to unlink the leaderboard group from the event is only trying
                        // to avoid duplication of code contained in the operation's internalApplyTo method
                        new RemoveLeaderboardGroupFromEvent(event.getId(), leaderboardGroup.getId()).internalApplyTo(this);
                    }
                }
                leaderboardGroupsByID.remove(leaderboardGroup.getId());
            }
        } finally {
            LockUtil.unlockAfterWrite(leaderboardGroupsByNameLock);
        }
        mongoObjectFactory.removeLeaderboardGroup(groupName);
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

    @Override
    public ObjectInputStream createObjectInputStreamResolvingAgainstCache(InputStream is) throws IOException {
        return getBaseDomainFactory().createObjectInputStreamResolvingAgainstThisFactory(is, null);
    }
    
    @Override
    public ClassLoader getDeserializationClassLoader() {
        return joinedClassLoader;
    }

    @Override
    public Serializable getId() {
        return getClass().getName();
    }

    @Override
    public Iterable<OperationExecutionListener<RacingEventService>> getOperationExecutionListeners() {
        return operationExecutionListeners.keySet();
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

        logger.info("Serializing regattas...");
        oos.writeObject(regattasByName);
        logoutput.append("Serialized " + regattasByName.size() + " regattas\n");
        for (Regatta regatta : regattasByName.values()) {
            logoutput.append(String.format("%3s\n", regatta.toString()));
        }

        logger.info("Serializing events...");
        oos.writeObject(eventsById);
        logoutput.append("\nSerialized " + eventsById.size() + " events\n");
        for (Event event : eventsById.values()) {
            logoutput.append(String.format("%3s\n", event.toString()));
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
        oos.writeObject(competitorAndBoatStore);
        logoutput.append("Serialized " + competitorAndBoatStore.getCompetitorsCount() + " persisted competitors\n");

        logger.info("Serializing configuration map...");
        oos.writeObject(configurationMap);
        logoutput.append("Serialized " + configurationMap.size() + " configuration entries\n");
        for (DeviceConfigurationMatcher matcher : configurationMap.keySet()) {
            logoutput.append(String.format("%3s\n", matcher.toString()));
        }

        logger.info("Serializing anniversary races...");
        final Map<Integer, Pair<DetailedRaceInfo, AnniversaryType>> knownAnniversaries = anniversaryRaceDeterminator
                .getKnownAnniversaries();
        oos.writeObject(knownAnniversaries);
        logoutput.append("Serialized " + knownAnniversaries.size() + " anniversary races\n");

        logger.info("Serializing next anniversary...");
        final Pair<Integer, AnniversaryType> nextAnniversary = anniversaryRaceDeterminator
                .getNextAnniversary();
        oos.writeObject(nextAnniversary);
        logoutput.append("Serialized next anniversary " + nextAnniversary + "\n");

        logger.info("Serializing race count for anniversaries...");
        final int currentRaceCount = anniversaryRaceDeterminator.getCurrentRaceCount();
        oos.writeInt(currentRaceCount);
        logoutput.append("Serialized race count for anniversaries " + currentRaceCount + "\n");

        logger.info("Serializing remote sailing server references...");
        final ArrayList<RemoteSailingServerReference> remoteServerReferences = new ArrayList<>(remoteSailingServerSet
                .getCachedEventsForRemoteSailingServers().keySet());
        oos.writeObject(remoteServerReferences);
        logoutput.append("Serialized " + remoteServerReferences.size() + " remote sailing server references\n");

        logger.info(logoutput.toString());
    }

    @SuppressWarnings("unchecked")
    // all the casts of ois.readObject()'s return value to Map<..., ...>
    // the type-parameters in the casts of the de-serialized collection objects can't be checked
    @Override
    public void initiallyFillFromInternal(ObjectInputStream ois) throws IOException, ClassNotFoundException,
            InterruptedException {
        logger.info("Performing initial replication load on " + this);
        // Use this object's class's class loader as the context class loader which will then be used for
        // de-serialization; this will cause all classes to be visible that this bundle
        // (com.sap.sailing.server) can see
        StringBuffer logoutput = new StringBuffer();
        logger.info("Reading all regattas...");
        regattasByName.putAll((Map<String, Regatta>) ois.readObject());
        logoutput.append("Received " + regattasByName.size() + " NEW regattas\n");
        for (Regatta regatta : regattasByName.values()) {
            regatta.addRegattaListener(this);
            logoutput.append(String.format("%3s\n", regatta.toString()));
        }
        logger.info("Reading all events...");
        eventsById.putAll((Map<Serializable, Event>) ois.readObject());
        logoutput.append("\nReceived " + eventsById.size() + " NEW events\n");
        for (Event event : eventsById.values()) {
            logoutput.append(String.format("%3s\n", event.toString()));
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
            } else if (leaderboard instanceof FlexibleLeaderboard) {
                // and re-establish the RaceLogReplicator as listener on FlexibleLeaderboard objects
                leaderboard.addRaceColumnListener(raceLogReplicator);
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
        for (Competitor competitor : ((CompetitorAndBoatStore) ois.readObject()).getAllCompetitors()) {
            DynamicCompetitor dynamicCompetitor = (DynamicCompetitor) competitor;
            // the following should actually be redundant because during de-serialization the Competitor objects,
            // whose classes implement IsManagedByCache, should already have been got/created from/in the
            // competitor store
            if (dynamicCompetitor.hasBoat()) {
                competitorAndBoatStore.getOrCreateCompetitorWithBoat(dynamicCompetitor.getId(), dynamicCompetitor.getName(), dynamicCompetitor.getShortName(),
                        dynamicCompetitor.getColor(), dynamicCompetitor.getEmail(), dynamicCompetitor.getFlagImage(),
                        dynamicCompetitor.getTeam(), dynamicCompetitor.getTimeOnTimeFactor(),
                        dynamicCompetitor.getTimeOnDistanceAllowancePerNauticalMile(), dynamicCompetitor.getSearchTag(),
                        ((DynamicCompetitorWithBoat) dynamicCompetitor).getBoat());
            } else {
                competitorAndBoatStore.getOrCreateCompetitor(dynamicCompetitor.getId(), dynamicCompetitor.getName(), dynamicCompetitor.getShortName(),
                        dynamicCompetitor.getColor(), dynamicCompetitor.getEmail(), dynamicCompetitor.getFlagImage(),
                        dynamicCompetitor.getTeam(), dynamicCompetitor.getTimeOnTimeFactor(),
                        dynamicCompetitor.getTimeOnDistanceAllowancePerNauticalMile(), dynamicCompetitor.getSearchTag());
            }
        }
        logoutput.append("Received " + competitorAndBoatStore.getCompetitorsCount() + " NEW competitors\n");

        logger.info("Reading device configurations...");
        configurationMap.putAll((DeviceConfigurationMapImpl) ois.readObject());
        logoutput.append("Received " + configurationMap.size() + " NEW configuration entries\n");
        for (DeviceConfigurationMatcher matcher : configurationMap.keySet()) {
            logoutput.append(String.format("%3s\n", matcher.toString()));
        }

        logger.info("Reading anniversary races...");
        final Map<Integer, Pair<DetailedRaceInfo, AnniversaryType>> knownAnniversaries = (Map<Integer, Pair<DetailedRaceInfo, AnniversaryType>>) ois
                .readObject();
        anniversaryRaceDeterminator.setKnownAnniversaries(knownAnniversaries);
        logoutput.append("Received " + knownAnniversaries.size() + " anniversary races\n");

        logger.info("Reading next anniversary...");
        final Pair<Integer, AnniversaryType> nextAnniversary = (Pair<Integer, AnniversaryType>) ois.readObject();
        anniversaryRaceDeterminator.setNextAnniversary(nextAnniversary);
        logoutput.append("Received next anniversary " + nextAnniversary + "\n");

        logger.info("Reading race count for anniversaries...");
        final int currentRaceCount = ois.readInt();
        anniversaryRaceDeterminator.setRaceCount(currentRaceCount);
        logoutput.append("Received race count for anniversaries " + currentRaceCount + "\n");

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
            regattaImpl.addRaceColumnListener(raceLogReplicator);
        }
        // re-establish RaceLogResolver references to this RacingEventService in all TrackedRace instances
        for (DynamicTrackedRegatta trackedRegatta : regattaTrackingCache.values()) {
            trackedRegatta.lockTrackedRacesForRead();
            try {
                for (TrackedRace trackedRace : trackedRegatta.getTrackedRaces()) {
                    ((TrackedRaceImpl) trackedRace).setRaceLogResolver(this);
                }
            } finally {
                trackedRegatta.unlockTrackedRacesAfterRead();
            }
        }
        // The replication added new TrackedRegattas -> inform the respective listeners
        regattaTrackingCache.values().forEach(trackedRegattaListener::regattaAdded);
        logger.info(logoutput.toString());
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
                final Set<RaceTracker> trackers = raceTrackersByRegatta.get(regatta.getRegatta());
                if (trackers != null) {
                    for (RaceTracker tracker : trackers) {
                        tracker.stop(/* preemptive */true);
                    }
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
            scoreCorrectionListenersByLeaderboard.clear();
        } finally {
            LockUtil.unlockAfterWrite(leaderboardsByNameLock);
        }
        connectivityParametersByRace.clear();
        eventsById.clear();
        mediaLibrary.clear();
        competitorAndBoatStore.clearCompetitors();
        remoteSailingServerSet.clear();
        if (notificationService != null) {
            notificationService.stop();
            notificationService = new EmptySailingNotificationService();
        }
        anniversaryRaceDeterminator.clearAndStop();
        this.remoteSailingServerSet.setRetrieveRemoteRaceResult(false);
        this.trackedRegattaListener.removeListener(raceChangeObserverForAnniversaryDetection);
        raceChangeObserverForAnniversaryDetection.stop();
    }

    // Used for TESTING only
    @Override
    public Event addEvent(String eventName, String eventDescription, TimePoint startDate, TimePoint endDate,
            String venue, boolean isPublic, UUID id) {
        Event result = createEventWithoutReplication(eventName, eventDescription, startDate, endDate, venue, isPublic,
                id, /* officialWebsiteURL */null, /* baseURL */null,
                /* sailorsInfoWebsiteURLAsString */null, /* images */Collections.<ImageDescriptor> emptyList(), /* videos */Collections.<VideoDescriptor> emptyList());
        replicate(new CreateEvent(eventName, eventDescription, startDate, endDate, venue, isPublic, id,
                /* officialWebsiteURLAsString */null, /*baseURL*/null,
                /* sailorsInfoWebsiteURLAsString */null, /* images */Collections.<ImageDescriptor> emptyList(),
                /* videos */Collections.<VideoDescriptor> emptyList(), /* leaderboardGroupIds */ Collections.<UUID> emptyList()));
        return result;
    }

    @Override
    public void addEventWithoutReplication(Event event) {
        addEvent(event);
    }

    @Override
    public Event createEventWithoutReplication(String eventName, String eventDescription, TimePoint startDate,
            TimePoint endDate, String venue, boolean isPublic, UUID id, URL officialWebsiteURL, URL baseURL, 
            Map<Locale, URL> sailorsInfoWebsiteURLs, Iterable<ImageDescriptor> images, Iterable<VideoDescriptor> videos) {
        Event result = new EventImpl(eventName, startDate, endDate, venue, isPublic, id);
        addEvent(result);
        result.setDescription(eventDescription);
        result.setOfficialWebsiteURL(officialWebsiteURL);
        result.setBaseURL(baseURL);
        result.setSailorsInfoWebsiteURLs(sailorsInfoWebsiteURLs);
        result.setImages(images);
        result.setVideos(videos);
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
            String venueName, boolean isPublic, Iterable<UUID> leaderboardGroupIds, URL officialWebsiteURL, URL baseURL,
            Map<Locale, URL> sailorsInfoWebsiteURLs, Iterable<ImageDescriptor> images, Iterable<VideoDescriptor> videos,
            Iterable<String> windFinderReviewedSpotCollectionIds) {
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
        event.setBaseURL(baseURL);
        event.setSailorsInfoWebsiteURLs(sailorsInfoWebsiteURLs);
        event.setImages(images);
        event.setVideos(videos);
        event.setWindFinderReviewedSpotsCollection(windFinderReviewedSpotCollectionIds);
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
    public CourseArea[] addCourseAreas(UUID eventId, String[] courseAreaNames, UUID[] courseAreaIds) {
        final CourseArea[] courseAreas = addCourseAreasWithoutReplication(eventId, courseAreaIds, courseAreaNames);
        replicate(new AddCourseAreas(eventId, courseAreaNames, courseAreaIds));
        return courseAreas;
    }

    @Override
    public CourseArea[] addCourseAreasWithoutReplication(UUID eventId, UUID[] courseAreaIds, String[] courseAreaNames) {
        final CourseArea[] result = new CourseArea[courseAreaNames.length];
        for (int i=0; i<courseAreaIds.length; i++) {
            final CourseArea courseArea = getBaseDomainFactory().getOrCreateCourseArea(courseAreaIds[i], courseAreaNames[i]);
            final Event event = eventsById.get(eventId);
            if (event == null) {
                throw new IllegalArgumentException("No sailing event with ID " + eventId + " found.");
            }
            event.getVenue().addCourseArea(courseArea);
            mongoObjectFactory.storeEvent(event);
            result[i] = courseArea;
        }
        return result;
    }

    @Override
    public CourseArea[] removeCourseAreaWithoutReplication(UUID eventId, UUID[] courseAreaIds) {
        final Event event = eventsById.get(eventId);
        if (event == null) {
            throw new IllegalArgumentException("No sailing event with ID " + eventId + " found.");
        }
        final CourseArea[] courseAreasRemoved = new CourseArea[courseAreaIds.length];
        int i=0;
        for (final UUID courseAreaId : courseAreaIds) {
            final CourseArea courseArea = getBaseDomainFactory().getExistingCourseAreaById(courseAreaId);
            if (courseArea == null) {
                throw new IllegalArgumentException("No course area with ID " + courseAreaId + " found.");
            }
            courseAreasRemoved[i++] = courseArea;
            event.getVenue().removeCourseArea(courseArea);
            mongoObjectFactory.storeEvent(event);
        }
        return courseAreasRemoved;
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
    public void mediaTracksAdded(Iterable<MediaTrack> mediaTracks) {
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
    public void mediaTracksImported(Iterable<MediaTrack> mediaTracksToImport, MasterDataImportObjectCreationCount creationCount, boolean override) throws Exception {
    	Exception firstException = null;
        for (MediaTrack trackToImport : mediaTracksToImport) {
        	try {
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
	            creationCount.addOneMediaTrack();
        	} catch (Exception e) {
        		logger.log(Level.SEVERE, "Problem importing media track "+trackToImport+"; continuing with other media tracks.", e);
        		if (firstException == null) {
        			firstException = e;
        		}
        	}
        }
        if (firstException != null) {
        	throw firstException;
        }
    }

    @Override
    public Iterable<MediaTrack> getMediaTracksForRace(RegattaAndRaceIdentifier regattaAndRaceIdentifier) {
        return mediaLibrary.findMediaTracksForRace(regattaAndRaceIdentifier);
    }

    @Override
    public Iterable<MediaTrack> getMediaTracksInTimeRange(RegattaAndRaceIdentifier regattaAndRaceIdentifier) {
        TrackedRace trackedRace = getExistingTrackedRace(regattaAndRaceIdentifier);
        if (trackedRace != null) {
            if (trackedRace.isLive(MillisecondsTimePoint.now())) {
                return mediaLibrary.findLiveMediaTracks();
            } else {
                TimePoint raceStart = trackedRace.getStartOfRace() == null ? trackedRace.getStartOfTracking()
                        : trackedRace.getStartOfRace();
                TimePoint raceEnd = trackedRace.getEndOfRace() == null ? trackedRace.getEndOfTracking() : trackedRace
                        .getEndOfRace();
                return mediaLibrary.findMediaTracksInTimeRange(raceStart, raceEnd);
            }
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public Iterable<MediaTrack> getAllMediaTracks() {
        return mediaLibrary.allTracks();
    }

    public String toString() {
        return "RacingEventService: " + this.hashCode() + " Build: " + ServerInfo.getBuildVersion();
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
                    raceColumn.reloadRaceLog(fleetImpl);
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
        Leaderboard leaderboard = getLeaderboardByName(leaderboardName);
        final TimePoint result;
        if (leaderboard instanceof HasRegattaLike && raceLog != null) {
            RaceState state = RaceStateImpl.create(/* race log resolver */ this, raceLog, new LogEventAuthorImpl(authorName, authorPriority));
            if (passId > raceLog.getCurrentPassId()) {
                state.setAdvancePass(logicalTimePoint);
            }
            state.setRacingProcedure(logicalTimePoint, racingProcedure);
            state.forceNewStartTime(logicalTimePoint, startTime);
            result = state.getStartTime();
        } else {
            result = null;
        }
        return result;
    }
    
    @Override
    public TimePoint setEndTime(String leaderboardName, String raceColumnName, String fleetName,
            String authorName, int authorPriority, int passId, TimePoint logicalTimePoint) {
        RaceLog raceLog = getRaceLog(leaderboardName, raceColumnName, fleetName);
        Leaderboard leaderboard = getLeaderboardByName(leaderboardName);
        final TimePoint result;
        if (leaderboard instanceof HasRegattaLike && raceLog != null) {
            LogEventAuthorImpl author = new LogEventAuthorImpl(authorName, authorPriority);
            raceLog.add(new RaceLogRaceStatusEventImpl(logicalTimePoint, author, raceLog.getCurrentPassId(), RaceLogRaceStatus.FINISHED));
            result = new FinishedTimeFinder(raceLog).analyze();
        } else {
            result = null;
        }
        return result;
    }
    
    @Override
    public TimePoint setFinishingTime(String leaderboardName, String raceColumnName, String fleetName,
            String authorName, Integer authorPriority, int passId, MillisecondsTimePoint logicalTimePoint) {
        RaceLog raceLog = getRaceLog(leaderboardName, raceColumnName, fleetName);
        Leaderboard leaderboard = getLeaderboardByName(leaderboardName);
        final TimePoint result;
        if (leaderboard instanceof HasRegattaLike && raceLog != null) {
            LogEventAuthorImpl author = new LogEventAuthorImpl(authorName, authorPriority);
            raceLog.add(new RaceLogRaceStatusEventImpl(logicalTimePoint, author, raceLog.getCurrentPassId(), RaceLogRaceStatus.FINISHING));
            result = new FinishingTimeFinder(raceLog).analyze();
        } else {
            result = null;
        }
        return result;
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

    public Map<Competitor, Boat> getCompetitorToBoatMappingsForRace(String leaderboardName, String raceColumnName, String fleetName) {
        Map<Competitor, Boat> result = new HashMap<>();
        Leaderboard leaderboard = getLeaderboardByName(leaderboardName);
        if (leaderboard != null) {
            RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
            Fleet fleet = leaderboard.getFleet(fleetName);
            if (raceColumn != null && fleet != null) {
                RaceLog raceLog = raceColumn.getRaceLog(fleet);
                // take the boats first from the racelog
                if (raceLog != null) { 
                    Map<Competitor, Boat> competitorAndBoatsInRacelog = new CompetitorsAndBoatsInLogAnalyzer<>(raceLog).analyze();
                    result.putAll(competitorAndBoatsInRacelog);
                }
                // now look into the tracked race for mappings of competitors without a boat 
                TrackedRace trackedRace = raceColumn.getTrackedRace(fleet);
                if (trackedRace != null) {
                    Map<Competitor, Boat> competitorsAndBoatsFromRaceDef = trackedRace.getRace().getCompetitorsAndTheirBoats();
                    for (Competitor competitor: competitorsAndBoatsFromRaceDef.keySet()) {
                        if (!result.containsKey(competitor)) {
                            result.put(competitor, competitorsAndBoatsFromRaceDef.get(competitor));  
                        }
                    }
                }
            }
        }
        return result;
    }
    

    
    @Override
    public com.sap.sse.common.Util.Triple<TimePoint, Integer, RacingProcedureType> getStartTimeAndProcedure(
            String leaderboardName, String raceColumnName, String fleetName) {
        RaceLog raceLog = getRaceLog(leaderboardName, raceColumnName, fleetName);
        Leaderboard leaderboard = getLeaderboardByName(leaderboardName);
        final Triple<TimePoint, Integer, RacingProcedureType> result;
        if (leaderboard instanceof HasRegattaLike && raceLog != null) {
            ReadonlyRaceState state = ReadonlyRaceStateImpl.getOrCreate(/* race log resolver */ this, raceLog);
            result = new com.sap.sse.common.Util.Triple<TimePoint, Integer, RacingProcedureType>(state.getStartTime(),
                raceLog.getCurrentPassId(), state.getRacingProcedure().getType());
        } else {
            result = null;
        }
        return result;
    }
    
    @Override
    public com.sap.sse.common.Util.Triple<TimePoint, TimePoint, Integer> getFinishingAndFinishTime(
            String leaderboardName, String raceColumnName, String fleetName) {
        RaceLog raceLog = getRaceLog(leaderboardName, raceColumnName, fleetName);
        Leaderboard leaderboard = getLeaderboardByName(leaderboardName);
        final Triple<TimePoint, TimePoint, Integer> result;
        if (leaderboard instanceof HasRegattaLike && raceLog != null) {
            ReadonlyRaceState state = ReadonlyRaceStateImpl.getOrCreate(/* race log resolver */ this, raceLog);
            result = new com.sap.sse.common.Util.Triple<>(state.getFinishingTime(), state.getFinishedTime(),
                raceLog.getCurrentPassId());
        } else {
            result = null;
        }
        return result;
    }

    private Iterable<WindTrackerFactory> getWindTrackerFactories() {
        final Set<WindTrackerFactory> result;
        if (bundleContext == null) { // the non-OSGi case
            result = Collections.singleton((WindTrackerFactory) ExpeditionTrackerFactory.getInstance());
        } else {
            ServiceTracker<WindTrackerFactory, WindTrackerFactory> tracker = new ServiceTracker<WindTrackerFactory, WindTrackerFactory>(
                    bundleContext, WindTrackerFactory.class, null);
            tracker.open();
            result = new HashSet<>();
            for (WindTrackerFactory factory : tracker.getServices(new WindTrackerFactory[0])) {
                result.add(factory);
            }
        }
        return result;
    }

    @Override
    public SensorFixStore getSensorFixStore() {
        return sensorFixStore;
    }

    @Override
    public RaceTracker getRaceTrackerById(Object id) {
        return raceTrackersByID.get(id);
    }

    @Override
    public AbstractLogEventAuthor getServerAuthor() {
        Subject subject  = null;
        try {
            subject = SecurityUtils.getSubject();
        } catch (Exception e) {
            logger.info("Couldn't access security manager's subject; using default server author: "+e.getMessage());
        }
        final AbstractLogEventAuthor result;
        if (subject != null && subject.getPrincipal() != null) {
            result = new LogEventAuthorImpl(subject.getPrincipal().toString(), /* priority */ 0);
        } else {
            result = raceLogEventAuthorForServer;
        }
        return result;
    }

    @Override
    public CompetitorAndBoatStore getCompetitorAndBoatStore() {
        return competitorAndBoatStore;
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
            double overallProgressPct, DataImportSubProgress subProgress, double subProgressPct) {
        // Create/Update locally
        DataImportProgress progress = createOrUpdateDataImportProgressWithoutReplication(importOperationId,
                overallProgressPct, subProgress, subProgressPct);
        // Create/Update on replicas
        replicate(new CreateOrUpdateDataImportProgress(importOperationId, overallProgressPct, subProgress,
                subProgressPct));
        return progress;
    }

    @Override
    public DataImportProgress createOrUpdateDataImportProgressWithoutReplication(UUID importOperationId,
            double overallProgressPct, DataImportSubProgress subProgress, double subProgressPct) {
        DataImportProgress progress = dataImportLock.getProgress(importOperationId);
        boolean newObject = false;
        if (progress == null) {
            progress = new DataImportProgressImpl(importOperationId);
            newObject = true;
        }
        progress.setOverAllProgressPct(overallProgressPct);
        progress.setCurrentSubProgress(subProgress);
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
                    URLConnection urlConnection = HttpUrlConnectionHelper.redirectConnection(eventsURL);
                    bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
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

    @Override
    public ReplicationMasterDescriptor getMasterDescriptor() {
        return replicatingFromMaster;
    }

    @Override
    public void startedReplicatingFrom(ReplicationMasterDescriptor master) {
        this.replicatingFromMaster = master;
    }

    @Override
    public void stoppedReplicatingFrom(ReplicationMasterDescriptor master) {
        this.replicatingFromMaster = null;
        anniversaryRaceDeterminator.start();
        this.remoteSailingServerSet.setRetrieveRemoteRaceResult(true);
    }

    @Override
    public void addOperationSentToMasterForReplication(
            OperationWithResultWithIdWrapper<RacingEventService, ?> operationWithResultWithIdWrapper) {
        this.operationsSentToMasterForReplication.add(operationWithResultWithIdWrapper);
    }

    @Override
    public boolean hasSentOperationToMaster(OperationWithResult<RacingEventService, ?> operation) {
        return this.operationsSentToMasterForReplication.remove(operation);
    }

    @Override
    public FileStorageManagementService getFileStorageManagementService() {
        ServiceReference<FileStorageManagementService> ref = bundleContext
                .getServiceReference(FileStorageManagementService.class);
        if (ref == null) {
            logger.warning("No file storage management service registered");
            return null;
        }
        return bundleContext.getService(ref);
    }

    public void addMasterDataClassLoader(ClassLoader classLoader) {
        masterDataClassLoaders.add(classLoader);
    }

    public void removeMasterDataClassLoader(ClassLoader classLoader) {
        masterDataClassLoaders.remove(classLoader);
    }
    
    @Override
    public ClassLoader getCombinedMasterDataClassLoader() {
        JoinedClassLoader joinedClassLoader = new JoinedClassLoader(masterDataClassLoaders);
        return joinedClassLoader;
    }

    public void setPolarDataService(PolarDataService service) {
        if (this.polarDataService == null && service != null) {
            polarDataService = service;
            polarDataService.registerDomainFactory(baseDomainFactory);
            setPolarDataServiceOnAllTrackedRaces(service);
        }
    }

    private void setPolarDataServiceOnAllTrackedRaces(PolarDataService service) {
        Iterable<Regatta> allRegattas = getAllRegattas();
        for (Regatta regatta : allRegattas) {
            DynamicTrackedRegatta trackedRegatta = getTrackedRegatta(regatta);
            if (trackedRegatta != null) {
                trackedRegatta.lockTrackedRacesForRead();
                try {
                    Iterable<DynamicTrackedRace> trackedRaces = trackedRegatta.getTrackedRaces();
                    for (TrackedRace trackedRace : trackedRaces) {
                        trackedRace.setPolarDataService(service);
                        if (service != null) {
                            service.insertExistingFixes(trackedRace);
                        }
                    }
                } catch (Throwable e) {
                    logger.log(Level.SEVERE, "Error reconstructing the polars for tracked races", e);
                } finally {
                    trackedRegatta.unlockTrackedRacesAfterRead();
                }
            }
        }
    }
    
    public void unsetPolarDataService(PolarDataService service) {
        if (polarDataService == service) {
            polarDataService = null;
            setPolarDataServiceOnAllTrackedRaces(null);
        }
    }

    @Override
    public Iterable<Competitor> getCompetitorInOrderOfWindwardDistanceTraveledFarthestFirst(TrackedRace trackedRace, TimePoint timePoint) {
        final RankingInfo rankingInfo = trackedRace.getRankingMetric().getRankingInfo(timePoint);
        final List<Competitor> result = new ArrayList<>();
        final Map<Competitor, Distance> windwardDistanceSailedPerCompetitor = new HashMap<>();
        for (final Competitor competitor : trackedRace.getRace().getCompetitors()) {
            result.add(competitor);
            final CompetitorRankingInfo competitorRankingInfo = rankingInfo.getCompetitorRankingInfo().apply(competitor);
            windwardDistanceSailedPerCompetitor.put(competitor, competitorRankingInfo == null ? null : competitorRankingInfo.getWindwardDistanceSailed());
        }
        final Comparator<Distance> durationComparatorNullsLast = Comparator.nullsLast(Comparator.naturalOrder());
        result.sort((c1, c2) -> durationComparatorNullsLast.compare(windwardDistanceSailedPerCompetitor.get(c2),
                                windwardDistanceSailedPerCompetitor.get(c1)));
        return result;
    }

    /**
     * A {@link SimpleRaceLogIdentifier} in particular has a {@link SimpleRaceLogIdentifier#getRegattaLikeParentName()}
     * which identifies either a regatta by name or a flexible leaderboard by name. Here is why this can luckily be
     * resolved unanimously: A regatta leaderboard always uses as its name the regatta name (see
     * {@link RegattaImpl#getName()}). Trying to {@link RegattaLeaderboardImpl#setName(String) set} the regatta leaderboard's
     * name can only update its {@link Leaderboard#getDisplayName() display name}. Therefore, regatta leaderboards are always
     * keyed in {@link #leaderboardsByName} by their regatta's name. Thus, no flexible leaderboard can have a regatta's name
     * as its name, and therefore leaderboard names <em>and</em> regatta names are unitedly unique.
     */
    @Override
    public RaceLog resolve(SimpleRaceLogIdentifier identifier) {
        final RaceLog result;
        final IsRegattaLike regattaLike;
        final Regatta regatta = regattasByName.get(identifier.getRegattaLikeParentName());
        if (regatta != null) {
            regattaLike = regatta;
        } else {
            final Leaderboard leaderboard = leaderboardsByName.get(identifier.getRegattaLikeParentName());
            if (leaderboard != null && leaderboard instanceof FlexibleLeaderboard) {
                regattaLike = (FlexibleLeaderboard) leaderboard;
            } else {
                regattaLike = null;
            }
        }
        if (regattaLike != null) {
            final RaceColumn raceColumn = regattaLike.getRaceColumnByName(identifier.getRaceColumnName());
            if (raceColumn != null) {
                final Fleet fleet = raceColumn.getFleetByName(identifier.getFleetName());
                if (fleet != null) {
                    result = raceColumn.getRaceLog(fleet);
                } else {
                    result = null;
                }
            } else {
                result = null;
            }
        } else {
            result = null;
        }
        return result;
    }

    @Override
    public void getRaceTrackerByRegattaAndRaceIdentifier(RegattaAndRaceIdentifier raceIdentifier,
            Consumer<RaceTracker> callback) {
        final Regatta regatta;
        LockUtil.lockForRead(regattasByNameLock);
        try {
            regatta = regattasByName.get(raceIdentifier.getRegattaName());
        } finally {
            LockUtil.unlockAfterRead(regattasByNameLock);
        }
        if (regatta != null) {
            LockUtil.lockForRead(raceTrackersByRegattaLock);
            try {
                Set<RaceTracker> raceTrackersForRegatta = raceTrackersByRegatta.get(regatta);
                if (raceTrackersForRegatta != null) {
                    for (RaceTracker raceTracker : raceTrackersForRegatta) {
                        if (Util.equalsWithNull(raceTracker.getRaceIdentifier(), raceIdentifier)) {
                            callback.accept(raceTracker);
                            return;
                        }
                    }
                }
                Util.addToValueSet(getRaceTrackerCallbacks(), raceIdentifier, callback);
            } finally {
                LockUtil.unlockAfterRead(raceTrackersByRegattaLock);
            }
        }
    }

    private ConcurrentHashMap<RegattaAndRaceIdentifier, Set<Consumer<RaceTracker>>> getRaceTrackerCallbacks() {
        if (raceTrackerCallbacks == null) {
            synchronized (this) {
                if (raceTrackerCallbacks == null) {
                    raceTrackerCallbacks = new ConcurrentHashMap<>();
                }
            }
        }
        return raceTrackerCallbacks;
    }

    private void notifyListenersForNewRaceTracker(RaceTracker tracker) {
        LockUtil.lockForRead(raceTrackersByRegattaLock);
        try {
            final RaceIdentifier raceIdentifier = tracker.getRaceIdentifier();
            if (raceIdentifier != null) {
                Set<Consumer<RaceTracker>> callbacks = getRaceTrackerCallbacks().remove(raceIdentifier);
                if (callbacks != null) {
                    callbacks.forEach((callback) -> callback.accept(tracker));
                }
            };
        } finally {
            LockUtil.unlockAfterRead(raceTrackersByRegattaLock);
        }

    }

    @Override
    public long getNumberOfTrackedRacesToRestore() {
        return numberOfTrackedRacesToRestore;
    }

    @Override
    public int getNumberOfTrackedRacesRestored() {
        return numberOfTrackedRacesRestored.get();
    }

    @Override
    public Map<Integer, Statistics> getLocalStatisticsByYear() {
        final Map<Integer, StatisticsCalculator> calculators = new HashMap<>();
        getAllEvents().forEach((event) -> {
            if (getSecurityService().hasCurrentUserReadPermission(event)) {
                final Integer eventYear = EventUtil.getYearOfEvent(event);
                // The year may be null if the event has no start date set
                // In this case the event is ignored for the yearly
                if (eventYear != null) {
                    final StatisticsCalculator calculator;
                    if (calculators.containsKey(eventYear)) {
                        calculator = calculators.get(eventYear);
                    } else {
                        calculator = new StatisticsCalculator(trackedRaceStatisticsCache);
                        calculators.put(eventYear, calculator);
                    }
                    event.getLeaderboardGroups().forEach((lg) -> {
                        if (getSecurityService().hasCurrentUserReadPermission(lg)) {
                            for (Leaderboard t : lg.getLeaderboards()) {
                                if (t instanceof RegattaLeaderboard) {
                                    if (!getSecurityService()
                                            .hasCurrentUserReadPermission(((RegattaLeaderboard) t).getRegatta())) {
                                        continue;
                                    }
                                }
                                if (getSecurityService().hasCurrentUserReadPermission(t)) {
                                    calculator.addLeaderboard(t);
                                }
                            }
                        }
                    });
                }
            }
        });
        Map<Integer, Statistics> result = new HashMap<>();
        calculators.forEach((year, calculator) -> {
            result.put(year, calculator.getStatistics());
        });
        return result;
    }

    @Override
    public Map<Integer, Statistics> getOverallStatisticsByYear() {
        final Map<Integer, StatisticsAggregator> statisticsAggregators = new HashMap<>();
        final BiConsumer<Integer, Statistics> statisticsConsumer = (year, statistics) -> {
            final StatisticsAggregator statisticsAggregator;
            if (statisticsAggregators.containsKey(year)) {
                statisticsAggregator = statisticsAggregators.get(year);
            } else {
                statisticsAggregator = new StatisticsAggregator();
                statisticsAggregators.put(year, statisticsAggregator);
            }
            statisticsAggregator.addStatistics(statistics);
        };

        Map<Integer, Statistics> localStatistics = getLocalStatisticsByYear();
        localStatistics.forEach(statisticsConsumer);

        Map<RemoteSailingServerReference, Pair<Map<Integer, Statistics>, Exception>> remoteStatistics = remoteSailingServerSet
                .getCachedStatisticsForRemoteSailingServers();
        remoteStatistics.forEach((ref, statisticsOrError) -> {
            Map<Integer, Statistics> remoteStatisticsOrNull = statisticsOrError.getA();
            if (remoteStatisticsOrNull != null) {
                remoteStatisticsOrNull.forEach(statisticsConsumer);
            }
        });

        final Map<Integer, Statistics> result = new HashMap<>();
        statisticsAggregators.forEach((year, aggregator) -> result.put(year, aggregator.getStatistics()));
        return result;
    }

    @Override
    public HashMap<RegattaAndRaceIdentifier, SimpleRaceInfo> getRemoteRaceList() {
        final HashMap<RegattaAndRaceIdentifier, SimpleRaceInfo> store = new HashMap<>();
        for (Entry<RemoteSailingServerReference, Pair<Iterable<SimpleRaceInfo>, Exception>> race : remoteSailingServerSet
                .getCachedRaceList().entrySet()) {
            if (race.getValue().getB() != null) {
                throw new RuntimeException("Some remoteserver did not respond " + race.getKey());
            }
            for (SimpleRaceInfo raceinfo : race.getValue().getA()) {
                store.put(raceinfo.getIdentifier(), raceinfo);
            }
        }
        return store;
    }

    @Override
    public Map<RegattaAndRaceIdentifier, SimpleRaceInfo> getLocalRaceList() {
        final HashMap<RegattaAndRaceIdentifier, SimpleRaceInfo> store = new HashMap<>();
        for (Event event : getAllEvents()) {
            for (LeaderboardGroup group : event.getLeaderboardGroups()) {
                for (Leaderboard leaderboard : group.getLeaderboards()) {
                    for (RaceColumn race : leaderboard.getRaceColumns()) {
                        for (Fleet fleet : race.getFleets()) {
                            TrackedRace trackedRace = race.getTrackedRace(fleet);
                            if (trackedRace != null && trackedRace.hasGPSData()) {
                                RegattaAndRaceIdentifier raceIdentifier = trackedRace.getRaceIdentifier();
                                final TimePoint startOfRace = trackedRace.getStartOfRace();
                                if (startOfRace != null) {
                                    SimpleRaceInfo raceInfo = new SimpleRaceInfo(raceIdentifier, startOfRace, /* remoteURL */ null);
                                    store.put(raceInfo.getIdentifier(), raceInfo);
                                }
                            }
                        }
                    }
                }
            }
        }
        return store;
    }
    
    @Override
    public DetailedRaceInfo getFullDetailsForRaceLocal(RegattaAndRaceIdentifier raceIdentifier) {
        DetailedRaceInfo bestMatch = null;
        boolean matchesName = false;
        boolean matchesCourseArea = false;
        // start from the top; while there are more efficient ways to look up the TrackedRace by its
        // race identifier, this wouldn't tell a valid event and leaderboard combination through which
        // to navigate to it
        for (Event event : this.getAllEvents()) {
            final EventType eventType = EventUtil.getEventType(event);
            for (LeaderboardGroup group : event.getLeaderboardGroups()) {
                for (Leaderboard leaderboard : group.getLeaderboards()) {
                    for (RaceColumn race : leaderboard.getRaceColumns()) {
                        for (Fleet fleet : race.getFleets()) {
                            TrackedRace trackedRace = race.getTrackedRace(fleet);
                            if (trackedRace != null) {
                                RegattaAndRaceIdentifier trackedRaceIdentifier = trackedRace.getRaceIdentifier();
                                // check if the race matches the RegattaAndRaceIdentifier
                                if (trackedRaceIdentifier.equals(raceIdentifier)
                                        && trackedRace.getStartOfRace() != null) {
                                    final CourseArea defaultCourseArea = leaderboard.getDefaultCourseArea();
                                    boolean leaderboardLinkedToEventThroughCourseArea = (defaultCourseArea != null
                                            && Util.contains(event.getVenue().getCourseAreas(), defaultCourseArea));
                                    boolean nameOfRegattaAndLeaderboardMatch = leaderboard.getName().equals(trackedRaceIdentifier.getRegattaName());
                                    // check if the match is a best match -> we keep the previous match otherwise
                                    if (bestMatch == null
                                            || (leaderboardLinkedToEventThroughCourseArea && !matchesCourseArea)
                                            || (leaderboardLinkedToEventThroughCourseArea == matchesCourseArea
                                                    && nameOfRegattaAndLeaderboardMatch && !matchesName)) {
                                        bestMatch = new DetailedRaceInfo(trackedRaceIdentifier, leaderboard.getName(),
                                                leaderboard.getDisplayName(), trackedRace.getStartOfRace(),
                                                event.getId(), event.getName(), eventType, null);
                                        matchesName = nameOfRegattaAndLeaderboardMatch;
                                        matchesCourseArea = leaderboardLinkedToEventThroughCourseArea;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return bestMatch;
    }

    @Override
    public DetailedRaceInfo getFullDetailsForRaceCascading(RegattaAndRaceIdentifier raceIdentifier) {
        DetailedRaceInfo bestMatch = getFullDetailsForRaceLocal(raceIdentifier);
        if (bestMatch == null) {
            // check for stored simpleRaceInfo from remote server
            Map<RemoteSailingServerReference, Pair<Iterable<SimpleRaceInfo>, Exception>> races = remoteSailingServerSet.getCachedRaceList();
            for (Entry<RemoteSailingServerReference, Pair<Iterable<SimpleRaceInfo>, Exception>> cachedInfo : races.entrySet()) {
                Iterable<SimpleRaceInfo> raceList = cachedInfo.getValue().getA();
                if (raceList != null) {
                    for (SimpleRaceInfo race : raceList) {
                        if (race.getIdentifier().equals(raceIdentifier)) {
                            bestMatch = remoteSailingServerSet.getDetailedInfoBlocking(race);
                            break;
                        }
                    }
                }
            }
        }
        return bestMatch;
    }
    
    @Override
    public Pair<Integer, AnniversaryType> getNextAnniversary() {
        return anniversaryRaceDeterminator.getNextAnniversary();
    }

    @Override
    public Triple<Integer, DetailedRaceInfo, AnniversaryType> getLastAnniversary() {
        Map<Integer, Pair<DetailedRaceInfo, AnniversaryType>> allAnniversaries = anniversaryRaceDeterminator
                .getKnownAnniversaries();
        Triple<Integer, DetailedRaceInfo, AnniversaryType> lastAnniversary = null;
        if (!allAnniversaries.isEmpty()) {
            ArrayList<Integer> list = new ArrayList<>(allAnniversaries.keySet());
            list.sort(Integer::compare);
            Integer anniversary = list.get(list.size() - 1);
            Pair<DetailedRaceInfo, AnniversaryType> info = allAnniversaries.get(anniversary);
            lastAnniversary = new Triple<>(anniversary, info.getA(), info.getB());
        }
        return lastAnniversary;
    }

    @Override
    public int getCurrentRaceCount() {
        return anniversaryRaceDeterminator.getCurrentRaceCount();
    }

    @Override
    public Map<Integer, Pair<DetailedRaceInfo, AnniversaryType>> getKnownAnniversaries() {
        return anniversaryRaceDeterminator.getKnownAnniversaries();
    }
    
    @Override
    public AnniversaryRaceDeterminatorImpl getAnniversaryRaceDeterminator() {
        return anniversaryRaceDeterminator;
    }
    
    @Override
    public PairingListTemplate createPairingListTemplate(final int flightsCount, final int groupsCount, 
            final int competitorsCount, final int flightMultiplier) {
        PairingListTemplate template = pairingListTemplateFactory
                .createPairingListTemplate(new PairingFrameProvider() {
                    @Override
                    public int getGroupsCount() {
                        return groupsCount;
                    }

                    @Override
                    public int getFlightsCount() {
                        return flightsCount;
                    }

                    @Override
                    public int getCompetitorsCount() {
                        return competitorsCount;
                    }
                }, flightMultiplier);
        return template;
    }
    
    @Override
    public PairingList<RaceColumn, Fleet, Competitor,Boat> getPairingListFromTemplate(PairingListTemplate pairingListTemplate,
            final String leaderboardName, final Iterable<RaceColumn> selectedRaceColumn) throws PairingListCreationException {
        Leaderboard leaderboard = getLeaderboardByName(leaderboardName);
        List<Competitor> competitors = Util.createList(leaderboard.getAllCompetitors());
        Collections.shuffle(competitors);
        PairingList<RaceColumn, Fleet, Competitor,Boat> pairingList = pairingListTemplate.createPairingList(
                new CompetitionFormat<RaceColumn, Fleet, Competitor, Boat>() {
            @Override
            public Iterable<RaceColumn> getFlights() {
                return selectedRaceColumn;
            }
            @Override
            public Iterable<Competitor> getCompetitors() {
                return competitors;
            }
            @Override
            public Iterable<? extends Fleet> getGroups(RaceColumn flight) {
                return leaderboard.getRaceColumnByName(flight.getName()).getFleets();
            }
            @Override
            public int getGroupsCount() {
                return Util.size(Util.get(leaderboard.getRaceColumns(), 0).getFleets());
            }
            @Override
            public Iterable<Boat> getCompetitorAllocation() {
                return leaderboard.getAllBoats();
            }
        });
        return pairingList;
    }

    @Override
    public Iterable<String> getWindFinderReviewedSpotsCollectionIds() {
        final Set<String> result = new HashSet<>();
        for (final Event event : getAllEvents()) {
            Util.addAll(event.getWindFinderReviewedSpotsCollectionIds(), result);
        }
        return result;
    }
    
    /**
     * Creates a new {@link CompetitorWithBoat} objects from a {@link CompetitorDescriptor}.
     * 
     * @param searchTag
     *            set as the {@link Competitor#getSearchTag() searchTag} property of all new competitors
     */
    @Override
    public DynamicCompetitorWithBoat convertCompetitorDescriptorToCompetitorWithBoat(CompetitorDescriptor competitorDescriptor, String searchTag) {
        Nationality nationality = (competitorDescriptor.getCountryCode() == null
                || competitorDescriptor.getCountryCode().getThreeLetterIOCCode() == null
                || competitorDescriptor.getCountryCode().getThreeLetterIOCCode().isEmpty()) ? null
                        : getBaseDomainFactory().getOrCreateNationality(competitorDescriptor.getCountryCode().getThreeLetterIOCCode());
        UUID competitorUUID = competitorDescriptor.getCompetitorUUID() != null ? competitorDescriptor.getCompetitorUUID() : UUID.randomUUID();
        UUID boatUUID = competitorDescriptor.getBoatUUID() != null ? competitorDescriptor.getBoatUUID() : UUID.randomUUID();
        DynamicPerson sailor = new PersonImpl(competitorDescriptor.getName(), nationality, null, null);
        DynamicTeam team = new TeamImpl(competitorDescriptor.getName(), Collections.singleton(sailor), null);
        BoatClass boatClass = getBaseDomainFactory().getOrCreateBoatClass(competitorDescriptor.getBoatClassName());
        DynamicBoat boat = getCompetitorAndBoatStore().getOrCreateBoat(competitorUUID, competitorDescriptor.getBoatName(), boatClass, competitorDescriptor.getSailNumber(), /* color */ null);
        DynamicCompetitorWithBoat competitorWithBoat = getCompetitorAndBoatStore().getOrCreateCompetitorWithBoat(boatUUID,
                competitorDescriptor.getName(), competitorDescriptor.getShortName(), /* color */ null, /* eMail */ null,
                /* flag image */ null, team, competitorDescriptor.getTimeOnTimeFactor(),
                competitorDescriptor.getTimeOnDistanceAllowancePerNauticalMile(), searchTag, boat);
        return competitorWithBoat;
    }

    @Override
    /**
     * This should only be used for replicable Operations that need access to the SecurityService, all other should
     * obtain the SecurityService in another way.
     */
    public SecurityService getSecurityService() {
        return securityServiceTracker.getService();
    }

    public void setUnsentOperationToMasterSender(OperationsToMasterSendingQueue service) {
        this.unsentOperationsToMasterSender = service;
    }

    @Override
    public <S, O extends OperationWithResult<S, ?>, T> void scheduleForSending(
            OperationWithResult<S, T> operationWithResult, OperationsToMasterSender<S, O> sender) {
        if (unsentOperationsToMasterSender != null) {
            unsentOperationsToMasterSender.scheduleForSending(operationWithResult, sender);
        }
    }
}
