package com.sap.sailing.gwt.ui.server;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.servlet.ServletContext;

import org.apache.http.client.ClientProtocolException;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.datamining.DataMiningFactory;
import com.sap.sailing.datamining.Query;
import com.sap.sailing.datamining.shared.DataMiningSerializationDummy;
import com.sap.sailing.datamining.shared.QueryDefinition;
import com.sap.sailing.datamining.shared.QueryResult;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.ControlPointWithTwoMarks;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnInSeries;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.Sideline;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.configuration.DeviceConfiguration;
import com.sap.sailing.domain.base.configuration.DeviceConfigurationMatcher;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.domain.base.configuration.impl.DeviceConfigurationImpl;
import com.sap.sailing.domain.base.configuration.impl.DeviceConfigurationMatcherMulti;
import com.sap.sailing.domain.base.configuration.impl.DeviceConfigurationMatcherSingle;
import com.sap.sailing.domain.base.configuration.impl.ESSConfigurationImpl;
import com.sap.sailing.domain.base.configuration.impl.GateStartConfigurationImpl;
import com.sap.sailing.domain.base.configuration.impl.RRS26ConfigurationImpl;
import com.sap.sailing.domain.base.configuration.impl.RacingProcedureConfigurationImpl;
import com.sap.sailing.domain.base.configuration.impl.RegattaConfigurationImpl;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.base.impl.SeriesImpl;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.CountryCode;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.domain.common.LeaderboardType;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.MasterDataImportObjectCreationCount;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.NoWindError;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.PolarSheetGenerationResponse;
import com.sap.sailing.domain.common.PolarSheetGenerationSettings;
import com.sap.sailing.domain.common.PolarSheetsData;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.RaceFetcher;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaFetcher;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.RegattaScoreCorrections;
import com.sap.sailing.domain.common.RegattaScoreCorrections.ScoreCorrectionForCompetitorInRace;
import com.sap.sailing.domain.common.RegattaScoreCorrections.ScoreCorrectionsForRace;
import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.configuration.DeviceConfigurationMatcherType;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.FullLeaderboardDTO;
import com.sap.sailing.domain.common.dto.IncrementalLeaderboardDTO;
import com.sap.sailing.domain.common.dto.IncrementalOrFullLeaderboardDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.LeaderboardEntryDTO;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;
import com.sap.sailing.domain.common.dto.PositionDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.domain.common.dto.RaceColumnInSeriesDTO;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.domain.common.dto.RegattaCreationParametersDTO;
import com.sap.sailing.domain.common.dto.TrackedRaceDTO;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KilometersPerHourSpeedImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.impl.PolarSheetGenerationResponseImpl;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.common.racelog.FlagPole;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.igtimiadapter.Account;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnection;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnectionFactory;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.MetaLeaderboard;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.domain.leaderboard.caching.LiveLeaderboardUpdater;
import com.sap.sailing.domain.masterdataimport.TopLevelMasterData;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.MongoRaceLogStoreFactory;
import com.sap.sailing.domain.polarsheets.PolarSheetGenerationWorker;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;
import com.sap.sailing.domain.racelog.RaceStateOfSameDayHelper;
import com.sap.sailing.domain.racelog.analyzing.impl.AbortingFlagFinder;
import com.sap.sailing.domain.racelog.state.ReadonlyRaceState;
import com.sap.sailing.domain.racelog.state.impl.ReadonlyRaceStateImpl;
import com.sap.sailing.domain.racelog.state.racingprocedure.FlagPoleState;
import com.sap.sailing.domain.racelog.state.racingprocedure.gate.ReadonlyGateStartRacingProcedure;
import com.sap.sailing.domain.racelog.state.racingprocedure.rrs26.ReadonlyRRS26RacingProcedure;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingAdapter;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingAdapterFactory;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingArchiveConfiguration;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingConfiguration;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingFactory;
import com.sap.sailing.domain.swisstimingadapter.persistence.SwissTimingAdapterPersistence;
import com.sap.sailing.domain.swisstimingreplayadapter.SwissTimingReplayRace;
import com.sap.sailing.domain.swisstimingreplayadapter.SwissTimingReplayService;
import com.sap.sailing.domain.swisstimingreplayadapter.SwissTimingReplayServiceFactory;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.LineDetails;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.MarkPassingManeuver;
import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sailing.domain.tracking.RacesHandle;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.domain.tractracadapter.RaceRecord;
import com.sap.sailing.domain.tractracadapter.TracTracAdapter;
import com.sap.sailing.domain.tractracadapter.TracTracAdapterFactory;
import com.sap.sailing.domain.tractracadapter.TracTracConfiguration;
import com.sap.sailing.domain.tractracadapter.TracTracConnectionConstants;
import com.sap.sailing.gwt.ui.client.SailingService;
import com.sap.sailing.gwt.ui.shared.BulkScoreCorrectionDTO;
import com.sap.sailing.gwt.ui.shared.CompactRaceMapDataDTO;
import com.sap.sailing.gwt.ui.shared.CompetitorRaceDataDTO;
import com.sap.sailing.gwt.ui.shared.CompetitorsRaceDataDTO;
import com.sap.sailing.gwt.ui.shared.ControlPointDTO;
import com.sap.sailing.gwt.ui.shared.CourseAreaDTO;
import com.sap.sailing.gwt.ui.shared.CoursePositionsDTO;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationDTO.RegattaConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationMatcherDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.GPSFixDTO;
import com.sap.sailing.gwt.ui.shared.GateDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.LegInfoDTO;
import com.sap.sailing.gwt.ui.shared.ManeuverDTO;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sailing.gwt.ui.shared.MarkPassingTimesDTO;
import com.sap.sailing.gwt.ui.shared.MarkpassingManeuverDTO;
import com.sap.sailing.gwt.ui.shared.QuickRankDTO;
import com.sap.sailing.gwt.ui.shared.RaceCourseDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupSeriesDTO;
import com.sap.sailing.gwt.ui.shared.RaceInfoDTO;
import com.sap.sailing.gwt.ui.shared.RaceInfoDTO.GateStartInfoDTO;
import com.sap.sailing.gwt.ui.shared.RaceInfoDTO.RRS26InfoDTO;
import com.sap.sailing.gwt.ui.shared.RaceInfoDTO.RaceInfoExtensionDTO;
import com.sap.sailing.gwt.ui.shared.RaceLogDTO;
import com.sap.sailing.gwt.ui.shared.RaceLogEventDTO;
import com.sap.sailing.gwt.ui.shared.RaceLogSetStartTimeDTO;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sailing.gwt.ui.shared.RaceWithCompetitorsDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.RegattaOverviewEntryDTO;
import com.sap.sailing.gwt.ui.shared.RegattaScoreCorrectionDTO;
import com.sap.sailing.gwt.ui.shared.RegattaScoreCorrectionDTO.ScoreCorrectionEntryDTO;
import com.sap.sailing.gwt.ui.shared.ReplicaDTO;
import com.sap.sailing.gwt.ui.shared.ReplicationMasterDTO;
import com.sap.sailing.gwt.ui.shared.ReplicationStateDTO;
import com.sap.sailing.gwt.ui.shared.ScoreCorrectionProviderDTO;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;
import com.sap.sailing.gwt.ui.shared.SidelineDTO;
import com.sap.sailing.gwt.ui.shared.SpeedWithBearingDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.SwissTimingArchiveConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.SwissTimingConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.SwissTimingRaceRecordDTO;
import com.sap.sailing.gwt.ui.shared.SwissTimingReplayRaceDTO;
import com.sap.sailing.gwt.ui.shared.TracTracConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.TracTracRaceRecordDTO;
import com.sap.sailing.gwt.ui.shared.VenueDTO;
import com.sap.sailing.gwt.ui.shared.WaypointDTO;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDTO;
import com.sap.sailing.gwt.ui.shared.WindTrackInfoDTO;
import com.sap.sailing.resultimport.ResultUrlProvider;
import com.sap.sailing.resultimport.ResultUrlRegistry;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;
import com.sap.sailing.server.masterdata.MasterDataImporter;
import com.sap.sailing.server.operationaltransformation.AddColumnToLeaderboard;
import com.sap.sailing.server.operationaltransformation.AddColumnToSeries;
import com.sap.sailing.server.operationaltransformation.AddCourseArea;
import com.sap.sailing.server.operationaltransformation.AddSpecificRegatta;
import com.sap.sailing.server.operationaltransformation.AllowCompetitorResetToDefaults;
import com.sap.sailing.server.operationaltransformation.ConnectTrackedRaceToLeaderboardColumn;
import com.sap.sailing.server.operationaltransformation.CreateEvent;
import com.sap.sailing.server.operationaltransformation.CreateFlexibleLeaderboard;
import com.sap.sailing.server.operationaltransformation.CreateLeaderboardGroup;
import com.sap.sailing.server.operationaltransformation.CreateRegattaLeaderboard;
import com.sap.sailing.server.operationaltransformation.DisconnectLeaderboardColumnFromTrackedRace;
import com.sap.sailing.server.operationaltransformation.MoveColumnInSeriesDown;
import com.sap.sailing.server.operationaltransformation.MoveColumnInSeriesUp;
import com.sap.sailing.server.operationaltransformation.MoveLeaderboardColumnDown;
import com.sap.sailing.server.operationaltransformation.MoveLeaderboardColumnUp;
import com.sap.sailing.server.operationaltransformation.RemoveAndUntrackRace;
import com.sap.sailing.server.operationaltransformation.RemoveColumnFromSeries;
import com.sap.sailing.server.operationaltransformation.RemoveEvent;
import com.sap.sailing.server.operationaltransformation.RemoveLeaderboard;
import com.sap.sailing.server.operationaltransformation.RemoveLeaderboardColumn;
import com.sap.sailing.server.operationaltransformation.RemoveLeaderboardGroup;
import com.sap.sailing.server.operationaltransformation.RemoveRegatta;
import com.sap.sailing.server.operationaltransformation.RenameEvent;
import com.sap.sailing.server.operationaltransformation.RenameLeaderboard;
import com.sap.sailing.server.operationaltransformation.RenameLeaderboardColumn;
import com.sap.sailing.server.operationaltransformation.RenameLeaderboardGroup;
import com.sap.sailing.server.operationaltransformation.SetRaceIsKnownToStartUpwind;
import com.sap.sailing.server.operationaltransformation.SetSuppressedFlagForCompetitorInLeaderboard;
import com.sap.sailing.server.operationaltransformation.SetWindSourcesToExclude;
import com.sap.sailing.server.operationaltransformation.StopTrackingRace;
import com.sap.sailing.server.operationaltransformation.StopTrackingRegatta;
import com.sap.sailing.server.operationaltransformation.UpdateCompetitor;
import com.sap.sailing.server.operationaltransformation.UpdateCompetitorDisplayNameInLeaderboard;
import com.sap.sailing.server.operationaltransformation.UpdateEvent;
import com.sap.sailing.server.operationaltransformation.UpdateIsMedalRace;
import com.sap.sailing.server.operationaltransformation.UpdateLeaderboard;
import com.sap.sailing.server.operationaltransformation.UpdateLeaderboardCarryValue;
import com.sap.sailing.server.operationaltransformation.UpdateLeaderboardColumnFactor;
import com.sap.sailing.server.operationaltransformation.UpdateLeaderboardGroup;
import com.sap.sailing.server.operationaltransformation.UpdateLeaderboardMaxPointsReason;
import com.sap.sailing.server.operationaltransformation.UpdateLeaderboardScoreCorrection;
import com.sap.sailing.server.operationaltransformation.UpdateLeaderboardScoreCorrectionMetadata;
import com.sap.sailing.server.operationaltransformation.UpdateRaceDelayToLive;
import com.sap.sailing.server.operationaltransformation.UpdateSeries;
import com.sap.sailing.server.operationaltransformation.UpdateSpecificRegatta;
import com.sap.sailing.server.replication.ReplicationFactory;
import com.sap.sailing.server.replication.ReplicationMasterDescriptor;
import com.sap.sailing.server.replication.ReplicationService;
import com.sap.sailing.server.replication.impl.ReplicaDescriptor;
import com.sap.sailing.util.BuildVersion;

/**
 * The server side implementation of the RPC service.
 */
public class SailingServiceImpl extends ProxiedRemoteServiceServlet implements SailingService, RaceFetcher, RegattaFetcher {
    private static final Logger logger = Logger.getLogger(SailingServiceImpl.class.getName());

    private static final long serialVersionUID = 9031688830194537489L;

    private final ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;

    private final ServiceTracker<ReplicationService, ReplicationService> replicationServiceTracker;

    private final ServiceTracker<ScoreCorrectionProvider, ScoreCorrectionProvider> scoreCorrectionProviderServiceTracker;

    private final MongoObjectFactory mongoObjectFactory;

    private final SwissTimingAdapterPersistence swissTimingAdapterPersistence;
    
    private final ServiceTracker<SwissTimingAdapterFactory, SwissTimingAdapterFactory> swissTimingAdapterTracker;

    private final ServiceTracker<TracTracAdapterFactory, TracTracAdapterFactory> tractracAdapterTracker;

    private final ServiceTracker<IgtimiConnectionFactory, IgtimiConnectionFactory> igtimiAdapterTracker;

    private final com.sap.sailing.domain.tractracadapter.persistence.MongoObjectFactory tractracMongoObjectFactory;

    private final DomainObjectFactory domainObjectFactory;

    private final SwissTimingFactory swissTimingFactory;

    private final com.sap.sailing.domain.tractracadapter.persistence.DomainObjectFactory tractracDomainObjectFactory;

    private final com.sap.sailing.domain.common.CountryCodeFactory countryCodeFactory;

    private final Executor executor;
    
    private final com.sap.sailing.domain.base.DomainFactory baseDomainFactory;
    
    private static final int LEADERBOARD_BY_NAME_RESULTS_CACHE_BY_ID_SIZE = 100;
    
    private static final int LEADERBOARD_DIFFERENCE_CACHE_SIZE = 50;

    private final LinkedHashMap<String, LeaderboardDTO> leaderboardByNameResultsCacheById;

    private int leaderboardDifferenceCacheByIdPairHits;
    private int leaderboardDifferenceCacheByIdPairMisses;
    /**
     * Caches some results of the hard to compute difference between two {@link LeaderboardDTO}s. The objects contained as values
     * have been obtained by {@link IncrementalLeaderboardDTO#strip(LeaderboardDTO)}. The cache size is limited to
     * {@link #LEADERBOARD_DIFFERENCE_CACHE_SIZE}.
     */
    private final LinkedHashMap<Pair<String, String>, IncrementalLeaderboardDTO> leaderboardDifferenceCacheByIdPair;

    private final SwissTimingReplayService swissTimingReplayService;

    private final BundleContext context;

    public SailingServiceImpl() {
        context = Activator.getDefault();
        racingEventServiceTracker = createAndOpenRacingEventServiceTracker(context);
        replicationServiceTracker = createAndOpenReplicationServiceTracker(context);
        swissTimingAdapterTracker = createAndOpenSwissTimingAdapterTracker(context);
        tractracAdapterTracker = createAndOpenTracTracAdapterTracker(context);
        igtimiAdapterTracker = createAndOpenIgtimiTracker(context);
        baseDomainFactory = getService().getBaseDomainFactory();
        mongoObjectFactory = getService().getMongoObjectFactory();
        domainObjectFactory = getService().getDomainObjectFactory();
        // TODO what about passing on the mongo/domain object factory to obtain an according SwissTimingAdapterPersistence instance similar to how the tractracDomainObjectFactory etc. are created below?
        swissTimingAdapterPersistence = SwissTimingAdapterPersistence.INSTANCE;
        swissTimingReplayService = getSwissTimingReplayService(context);
        scoreCorrectionProviderServiceTracker = createAndOpenScoreCorrectionProviderServiceTracker(context);
        tractracDomainObjectFactory = com.sap.sailing.domain.tractracadapter.persistence.PersistenceFactory.INSTANCE
                .createDomainObjectFactory(mongoObjectFactory.getDatabase(), getTracTracAdapter()
                        .getTracTracDomainFactory());
        tractracMongoObjectFactory = com.sap.sailing.domain.tractracadapter.persistence.MongoObjectFactory.INSTANCE;
        swissTimingFactory = SwissTimingFactory.INSTANCE;
        countryCodeFactory = com.sap.sailing.domain.common.CountryCodeFactory.INSTANCE;
        leaderboardDifferenceCacheByIdPair = new LinkedHashMap<Pair<String, String>, IncrementalLeaderboardDTO>(LEADERBOARD_DIFFERENCE_CACHE_SIZE, 0.75f, /* accessOrder */ true) {
            private static final long serialVersionUID = 3775119859130148488L;
            @Override
            protected boolean removeEldestEntry(Entry<Pair<String, String>, IncrementalLeaderboardDTO> eldest) {
                return this.size() > LEADERBOARD_DIFFERENCE_CACHE_SIZE;
            }
        };
        leaderboardByNameResultsCacheById = new LinkedHashMap<String, LeaderboardDTO>(LEADERBOARD_BY_NAME_RESULTS_CACHE_BY_ID_SIZE, 0.75f, /* accessOrder */ true) {
            private static final long serialVersionUID = 3775119859130148488L;
            @Override
            protected boolean removeEldestEntry(Entry<String, LeaderboardDTO> eldest) {
                return this.size() > LEADERBOARD_BY_NAME_RESULTS_CACHE_BY_ID_SIZE;
            }
        };
        // When many updates are triggered in a short period of time by a single thread, ensure that the single thread
        // providing the updates is not outperformed by all the re-calculations happening here. Leave at least one
        // core to other things, but by using at least three threads ensure that no simplistic deadlocks may occur.
        final int THREAD_POOL_SIZE = Math.max(Runtime.getRuntime().availableProcessors(), 3);
        executor = new ThreadPoolExecutor(/* corePoolSize */ THREAD_POOL_SIZE,
                /* maximumPoolSize */ THREAD_POOL_SIZE,
                /* keepAliveTime */ 60, TimeUnit.SECONDS,
                /* workQueue */ new LinkedBlockingQueue<Runnable>());
    }

    protected SwissTimingReplayService getSwissTimingReplayService(BundleContext context) {
        return createAndOpenSwissTimingReplayServiceTracker(context).getService().createSwissTimingReplayService(getSwissTimingAdapter().getSwissTimingDomainFactory());
    }

    protected SwissTimingAdapter getSwissTimingAdapter() {
        return swissTimingAdapterTracker.getService().getOrCreateSwissTimingAdapter(baseDomainFactory, swissTimingAdapterPersistence);
    }

    protected TracTracAdapter getTracTracAdapter() {
        return tractracAdapterTracker.getService().getOrCreateTracTracAdapter(baseDomainFactory);
    }

    protected ServiceTracker<TracTracAdapterFactory, TracTracAdapterFactory> createAndOpenTracTracAdapterTracker(BundleContext context) {
        ServiceTracker<TracTracAdapterFactory, TracTracAdapterFactory> result = new ServiceTracker<TracTracAdapterFactory, TracTracAdapterFactory>(
                context, TracTracAdapterFactory.class.getName(), null);
        result.open();
        return result;
    }

    protected ServiceTracker<IgtimiConnectionFactory, IgtimiConnectionFactory> createAndOpenIgtimiTracker(BundleContext context) {
        ServiceTracker<IgtimiConnectionFactory, IgtimiConnectionFactory> result = new ServiceTracker<IgtimiConnectionFactory, IgtimiConnectionFactory>(
                context, IgtimiConnectionFactory.class.getName(), null);
        result.open();
        return result;
    }

    protected ServiceTracker<SwissTimingAdapterFactory, SwissTimingAdapterFactory> createAndOpenSwissTimingAdapterTracker(
            BundleContext context) {
        ServiceTracker<SwissTimingAdapterFactory, SwissTimingAdapterFactory> result = new ServiceTracker<SwissTimingAdapterFactory, SwissTimingAdapterFactory>(
                context, SwissTimingAdapterFactory.class.getName(), null);
        result.open();
        return result;
    }

    protected ServiceTracker<SwissTimingReplayServiceFactory, SwissTimingReplayServiceFactory> createAndOpenSwissTimingReplayServiceTracker(
            BundleContext context) {
        ServiceTracker<SwissTimingReplayServiceFactory, SwissTimingReplayServiceFactory> result = new ServiceTracker<SwissTimingReplayServiceFactory, SwissTimingReplayServiceFactory>(
                context, SwissTimingReplayServiceFactory.class.getName(), null);
        result.open();
        return result;
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
    }

    protected ServiceTracker<RacingEventService, RacingEventService> createAndOpenRacingEventServiceTracker(
            BundleContext context) {
        ServiceTracker<RacingEventService, RacingEventService> result = new ServiceTracker<RacingEventService, RacingEventService>(
                context, RacingEventService.class.getName(), null);
        result.open();
        return result;
    }

    /**
     * Asks the OSGi system for registered score correction provider services
     */
    protected ServiceTracker<ScoreCorrectionProvider, ScoreCorrectionProvider> createAndOpenScoreCorrectionProviderServiceTracker(
            BundleContext bundleContext) {
        ServiceTracker<ScoreCorrectionProvider, ScoreCorrectionProvider> tracker = new ServiceTracker<ScoreCorrectionProvider, ScoreCorrectionProvider>(bundleContext,
                ScoreCorrectionProvider.class.getName(),
                /* customizer */null);
        tracker.open();
        return tracker;
    }

    @Override
    public Iterable<String> getScoreCorrectionProviderNames() {
        List<String> result = new ArrayList<String>();
        for (ScoreCorrectionProvider scoreCorrectionProvider : getAllScoreCorrectionProviders()) {
            result.add(scoreCorrectionProvider.getName());
        }
        return result;
    }

    @Override
    public ScoreCorrectionProviderDTO getScoreCorrectionsOfProvider(String providerName) throws Exception {
        ScoreCorrectionProviderDTO result = null;
        for (ScoreCorrectionProvider scoreCorrectionProvider : getAllScoreCorrectionProviders()) {
            if(scoreCorrectionProvider.getName().equals(providerName)) {
                result = convertScoreCorrectionProviderDTO(scoreCorrectionProvider);
                break;
            }
        }
        return result;
    }

    private Iterable<ScoreCorrectionProvider> getAllScoreCorrectionProviders() {
        final Object[] services = scoreCorrectionProviderServiceTracker.getServices();
        List<ScoreCorrectionProvider> result = new ArrayList<ScoreCorrectionProvider>();
        if (services != null) {
            for (Object service : services) {
                result.add((ScoreCorrectionProvider) service);
            }
        }
        return result;
    }

    private ScoreCorrectionProviderDTO convertScoreCorrectionProviderDTO(ScoreCorrectionProvider scoreCorrectionProvider)
            throws Exception {
        Map<String, Set<Pair<String, Date>>> hasResultsForBoatClassFromDateByEventName = new HashMap<String, Set<Pair<String,Date>>>();
        for (Map.Entry<String, Set<Pair<String, TimePoint>>> e : scoreCorrectionProvider
                .getHasResultsForBoatClassFromDateByEventName().entrySet()) {
            Set<Pair<String, Date>> set = new HashSet<Pair<String, Date>>();
            for (Pair<String, TimePoint> p : e.getValue()) {
                set.add(new Pair<String, Date>(p.getA(), p.getB().asDate()));
            }
            hasResultsForBoatClassFromDateByEventName.put(e.getKey(), set);
        }
        return new ScoreCorrectionProviderDTO(scoreCorrectionProvider.getName(), hasResultsForBoatClassFromDateByEventName);
    }

    protected ServiceTracker<ReplicationService, ReplicationService> createAndOpenReplicationServiceTracker(
            BundleContext context) {
        ServiceTracker<ReplicationService, ReplicationService> result = new ServiceTracker<ReplicationService, ReplicationService>(
                context, ReplicationService.class.getName(), null);
        result.open();
        return result;
    }

    /**
     * If <code>date</code> is <code>null</code>, the {@link LiveLeaderboardUpdater} for the
     * <code>leaderboardName</code> requested is obtained or created if it doesn't exist yet. The request is then passed
     * on to the live leaderboard updater which will respond with its live {@link LeaderboardDTO} if it has at least the
     * columns requested as per <code>namesOfRaceColumnsForWhichToLoadLegDetails</code>. Otherwise, the updater will add
     * the missing columns to its profile and start a synchronous computation for the requesting client, the result of
     * which will be used as live leaderboard cache update.
     * <p>
     * 
     * Otherwise, the leaderboard is computed synchronously on the fly.
     * 
     * @param previousLeaderboardId
     *            if <code>null</code> or no leaderboard with that {@link LeaderboardDTO#getId() ID} is known, a
     *            {@link FullLeaderboardDTO} will be computed; otherwise, an {@link IncrementalLeaderboardDTO} will be
     *            computed as the difference between the new, resulting leaderboard and the previous leaderboard.
     */
    @Override
    public IncrementalOrFullLeaderboardDTO getLeaderboardByName(final String leaderboardName, final Date date,
            final Collection<String> namesOfRaceColumnsForWhichToLoadLegDetails, String previousLeaderboardId)
                    throws NoWindException, InterruptedException, ExecutionException, IllegalArgumentException {
        try {
            long startOfRequestHandling = System.currentTimeMillis();
            IncrementalOrFullLeaderboardDTO result = null;
            final Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
            if (leaderboard != null) {
                TimePoint timePoint;
                if (date == null) {
                    timePoint = null;
                } else {
                    timePoint = new MillisecondsTimePoint(date);
                }
                LeaderboardDTO leaderboardDTO = leaderboard.getLeaderboardDTO(timePoint,
                        namesOfRaceColumnsForWhichToLoadLegDetails, getService(), baseDomainFactory);
                LeaderboardDTO previousLeaderboardDTO = null;
                synchronized (leaderboardByNameResultsCacheById) {
                    leaderboardByNameResultsCacheById.put(leaderboardDTO.getId(), leaderboardDTO);
                    if (previousLeaderboardId != null) {
                        previousLeaderboardDTO = leaderboardByNameResultsCacheById.get(previousLeaderboardId);
                    }
                }
                // Un-comment the following lines if you need to update the file used by LeaderboardDTODiffingTest, set a breakpoint
                // and toggle the storeLeaderboardForTesting flag if you found a good version. See also bug 1417.
//                boolean storeLeaderboardForTesting = false;
//                if (storeLeaderboardForTesting) {
//                    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File("c:/data/SAP/sailing/workspace/java/com.sap.sailing.domain.test/resources/IncrementalLeaderboardDTO.ser")));
//                    oos.writeObject(leaderboardDTO);
//                    oos.close();
//                }
                final IncrementalLeaderboardDTO cachedDiff;
                if (previousLeaderboardId != null) {
                    synchronized (leaderboardDifferenceCacheByIdPair) {
                        cachedDiff = leaderboardDifferenceCacheByIdPair.get(new Pair<String, String>(previousLeaderboardId, leaderboardDTO.getId()));
                    }
                    if (cachedDiff == null) {
                        leaderboardDifferenceCacheByIdPairMisses++;
                    } else {
                        leaderboardDifferenceCacheByIdPairHits++;
                    }
                } else {
                    cachedDiff = null;
                }
                if (previousLeaderboardDTO == null) {
                    result = new FullLeaderboardDTO(leaderboardDTO);
                } else {
                    final IncrementalLeaderboardDTO incrementalResult;
                    if (cachedDiff == null) {
                        IncrementalLeaderboardDTO preResult = new IncrementalLeaderboardDTOCloner().clone(leaderboardDTO).strip(previousLeaderboardDTO);
                        synchronized (leaderboardDifferenceCacheByIdPair) {
                            leaderboardDifferenceCacheByIdPair.put(new Pair<String, String>(previousLeaderboardId, leaderboardDTO.getId()), preResult);
                        }
                        incrementalResult = preResult;
                    } else {
                        incrementalResult = cachedDiff;
                    }
                    incrementalResult.setCurrentServerTime(new Date()); // may update a cached object, but we consider a reference update atomic
                    result = incrementalResult;
                }
                logger.fine("getLeaderboardByName(" + leaderboardName + ", " + date + ", "
                        + namesOfRaceColumnsForWhichToLoadLegDetails + ") took "
                        + (System.currentTimeMillis() - startOfRequestHandling) + "ms; diff cache hits/misses "
                        + leaderboardDifferenceCacheByIdPairHits+"/"+leaderboardDifferenceCacheByIdPairMisses);
            }
            return result;
        } catch (NoWindException e) {
            throw e;
        } catch (InterruptedException e) {
            throw e;
        } catch (ExecutionException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Exception during SailingService.getLeaderboardByName", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<RegattaDTO> getRegattas() throws IllegalArgumentException {
        List<RegattaDTO> result = new ArrayList<RegattaDTO>();
        for (Regatta regatta : getService().getAllRegattas()) {
            result.add(convertToRegattaDTO(regatta));
        }
        return result;
    }

    private MarkDTO convertToMarkDTO(Mark mark, Position position) {
        MarkDTO markDTO;
        if(position != null) {
            markDTO = new MarkDTO(mark.getId().toString(), mark.getName(), position.getLatDeg(), position.getLngDeg());
        } else {
            markDTO = new MarkDTO(mark.getId().toString(), mark.getName());
        }
        markDTO.color = mark.getColor();
        markDTO.shape = mark.getShape();
        markDTO.pattern = mark.getPattern();
        markDTO.type = mark.getType();
        return markDTO;
    }

    private RegattaDTO convertToRegattaDTO(Regatta regatta) {
        RegattaDTO regattaDTO = new RegattaDTO(regatta.getName(), regatta.getScoringScheme().getType());
        regattaDTO.races = convertToRaceDTOs(regatta);
        regattaDTO.series = convertToSeriesDTOs(regatta);
        BoatClass boatClass = regatta.getBoatClass();
        if (boatClass != null) {
            regattaDTO.boatClass = new BoatClassDTO(boatClass.getName(), boatClass.getHullLength().getMeters());
        }
        if (regatta.getDefaultCourseArea() != null) {
            regattaDTO.defaultCourseAreaUuid = regatta.getDefaultCourseArea().getId();
            regattaDTO.defaultCourseAreaName = regatta.getDefaultCourseArea().getName();
        }
        regattaDTO.configuration = convertToRegattaConfigurationDTO(regatta.getRegattaConfiguration());
        return regattaDTO;
    }

    private List<SeriesDTO> convertToSeriesDTOs(Regatta regatta) {
        List<SeriesDTO> result = new ArrayList<SeriesDTO>();
        for (Series series : regatta.getSeries()) {
            SeriesDTO seriesDTO = convertToSeriesDTO(series);
            result.add(seriesDTO);
        }
        return result;
    }

    private SeriesDTO convertToSeriesDTO(Series series) {
        List<FleetDTO> fleets = new ArrayList<FleetDTO>();
        for (Fleet fleet : series.getFleets()) {
            fleets.add(baseDomainFactory.convertToFleetDTO(fleet));
        }
        List<RaceColumnDTO> raceColumns = new ArrayList<RaceColumnDTO>();
        for (RaceColumnInSeries raceColumn : series.getRaceColumns()) {
            RaceColumnDTO raceColumnDTO = new RaceColumnDTO(/* isValidInTotalScore not relevant here because no scores conveyed */ null);
            raceColumnDTO.setName(raceColumn.getName());
            raceColumnDTO.setMedalRace(raceColumn.isMedalRace());
            raceColumnDTO.setExplicitFactor(raceColumn.getExplicitFactor());
            raceColumns.add(raceColumnDTO);
        }
        SeriesDTO result = new SeriesDTO(series.getName(), fleets, raceColumns, series.isMedal(),
                series.getResultDiscardingRule() == null ? null : series.getResultDiscardingRule().getDiscardIndexResultsStartingWithHowManyRaces(),
                        series.isStartsWithZeroScore(), series.isFirstColumnIsNonDiscardableCarryForward(), series.hasSplitFleetContiguousScoring());
        return result;
    }
    
    private RaceInfoDTO createRaceInfoDTO(String seriesName, RaceColumn raceColumn, Fleet fleet) {
        RaceInfoDTO raceInfoDTO = new RaceInfoDTO();
        RaceLog raceLog = raceColumn.getRaceLog(fleet);
        if (raceLog != null) {
            
            ReadonlyRaceState state = ReadonlyRaceStateImpl.create(raceLog);
            
            TimePoint startTime = state.getStartTime();
            if (startTime != null) {
                raceInfoDTO.startTime = startTime.asDate();
            }

            raceInfoDTO.lastStatus = state.getStatus();
            
            if (raceLog.getLastRawFix() != null) {
                raceInfoDTO.lastUpdateTime = raceLog.getLastRawFix().getCreatedAt().asDate();
            }
            
            TimePoint finishedTime = state.getFinishedTime();
            if (finishedTime != null) {
                raceInfoDTO.finishedTime = finishedTime.asDate();
            }

            if (startTime != null) {
                FlagPoleState activeFlagState = state.getRacingProcedure().getActiveFlags(startTime, MillisecondsTimePoint.now());
                List<FlagPole> activeFlags = activeFlagState.getCurrentState();
                // TODO: adapt the LastFlagFinder#getMostRecent method!
                if (!activeFlags.isEmpty()) {
                    raceInfoDTO.lastUpperFlag = activeFlags.get(0).getUpperFlag();
                    raceInfoDTO.lastLowerFlag = activeFlags.get(0).getLowerFlag();
                    raceInfoDTO.isLastFlagDisplayed = activeFlags.get(0).isDisplayed();
                }
            }
            
            AbortingFlagFinder abortingFlagFinder = new AbortingFlagFinder(raceLog);
            
            RaceLogFlagEvent abortingFlagEvent = abortingFlagFinder.analyze();
            if (abortingFlagEvent != null) {
                raceInfoDTO.isRaceAbortedInPassBefore = true;
                
                if (raceInfoDTO.lastStatus.equals(RaceLogRaceStatus.UNSCHEDULED)) {
                    raceInfoDTO.lastUpperFlag = abortingFlagEvent.getUpperFlag();
                    raceInfoDTO.lastLowerFlag = abortingFlagEvent.getLowerFlag();
                    raceInfoDTO.isLastFlagDisplayed = abortingFlagEvent.isDisplayed();
                }
            }
            
            CourseBase lastCourse = state.getCourseDesign();
            if (lastCourse != null) {
                raceInfoDTO.lastCourseDesign = convertCourseDesignToRaceCourseDTO(lastCourse);
                raceInfoDTO.lastCourseName = lastCourse.getName();
            }
            
            if (raceInfoDTO.lastStatus.equals(RaceLogRaceStatus.FINISHED)) {
                TimePoint protestStartTime = state.getProtestTime();
                if (protestStartTime != null) {
                    long protestDuration = 90 * 60 * 1000; // 90 min protest duration
                    raceInfoDTO.protestFinishTime = protestStartTime.plus(protestDuration).asDate();
                    raceInfoDTO.lastUpperFlag = Flags.BRAVO;
                    raceInfoDTO.lastLowerFlag = Flags.NONE;
                    raceInfoDTO.isLastFlagDisplayed = true;
                }
            }
            
            Wind wind = state.getWindFix();
            if (wind != null) {
                raceInfoDTO.lastWind = createWindDTOFromAlreadyAveraged(wind, MillisecondsTimePoint.now());
            }

            fillStartProcedureSpecifics(raceInfoDTO, state);
        }
        raceInfoDTO.seriesName = seriesName;
        raceInfoDTO.raceName = raceColumn.getName();
        raceInfoDTO.fleetName = fleet.getName();
        raceInfoDTO.fleetOrdering = fleet.getOrdering();
        raceInfoDTO.raceIdentifier = raceColumn.getRaceIdentifier(fleet);
        raceInfoDTO.isTracked = raceColumn.getTrackedRace(fleet) != null ? true : false;

        return raceInfoDTO;
    }    
    
    private void fillStartProcedureSpecifics(RaceInfoDTO raceInfoDTO, ReadonlyRaceState state) {
        RaceInfoExtensionDTO info = null;
        raceInfoDTO.startProcedure = state.getRacingProcedure().getType();
        switch (raceInfoDTO.startProcedure) {
        case GateStart:
            ReadonlyGateStartRacingProcedure gateStart = state.getTypedReadonlyRacingProcedure();
            info = new GateStartInfoDTO(gateStart.getPathfinder(), gateStart.getGateLaunchStopTime());
            break;
        case RRS26:
            ReadonlyRRS26RacingProcedure rrs26 = state.getTypedReadonlyRacingProcedure();
            info = new RRS26InfoDTO(rrs26.getStartModeFlag());
        case UNKNOWN:
        default:
            break;
        }
        raceInfoDTO.startProcedureDTO = info;
    }

    private RaceCourseDTO convertCourseDesignToRaceCourseDTO(CourseBase lastCourseDesign) {
        RaceCourseDTO result = new RaceCourseDTO(Collections.<WaypointDTO> emptyList());
        if (lastCourseDesign != null) {
            List<WaypointDTO> waypointDTOs = new ArrayList<WaypointDTO>();
            for (Waypoint waypoint : lastCourseDesign.getWaypoints()) {
                ControlPointDTO controlPointDTO = convertToControlPointDTO(waypoint.getControlPoint());
                List<MarkDTO> marks = new ArrayList<MarkDTO>();
                for (MarkDTO markDTO : controlPointDTO.getMarks()) {
                    marks.add(markDTO);
                }
                WaypointDTO waypointDTO = new WaypointDTO(waypoint.getName(), controlPointDTO, marks, waypoint.getPassingInstructions());
                waypointDTOs.add(waypointDTO);
            }
            result = new RaceCourseDTO(waypointDTOs);
        }
        return result;
    }

    private List<RaceWithCompetitorsDTO> convertToRaceDTOs(Regatta regatta) {
        List<RaceWithCompetitorsDTO> result = new ArrayList<RaceWithCompetitorsDTO>();
        for (RaceDefinition r : regatta.getAllRaces()) {
            RegattaAndRaceIdentifier raceIdentifier = new RegattaNameAndRaceName(regatta.getName(), r.getName());
            TrackedRace trackedRace = getService().getExistingTrackedRace(raceIdentifier);
            TrackedRaceDTO trackedRaceDTO = null; 
            if (trackedRace != null) {
                trackedRaceDTO = getBaseDomainFactory().createTrackedRaceDTO(trackedRace);
            }
            RaceWithCompetitorsDTO raceDTO = new RaceWithCompetitorsDTO(raceIdentifier, convertToCompetitorDTOs(r.getCompetitors()),
                    trackedRaceDTO, getService().isRaceBeingTracked(r));
            if (trackedRace != null) {
                getBaseDomainFactory().updateRaceDTOWithTrackedRaceData(trackedRace, raceDTO);
            }
            raceDTO.boatClass = regatta.getBoatClass() == null ? null : regatta.getBoatClass().getName(); 
            result.add(raceDTO);
        }
        return result;
    }

    private List<CompetitorDTO> convertToCompetitorDTOs(Iterable<? extends Competitor> iterable) {
        List<CompetitorDTO> result = new ArrayList<CompetitorDTO>();
        for (Competitor c : iterable) {
            CompetitorDTO competitorDTO = baseDomainFactory.convertToCompetitorDTO(c);
            result.add(competitorDTO);
        }
        return result;
    }

    @Override
    public Pair<String, List<TracTracRaceRecordDTO>> listTracTracRacesInEvent(String eventJsonURL, boolean listHiddenRaces) throws MalformedURLException, IOException, ParseException, org.json.simple.parser.ParseException, URISyntaxException {
        com.sap.sailing.domain.common.impl.Util.Pair<String,List<RaceRecord>> raceRecords;
        raceRecords = getTracTracAdapter().getTracTracRaceRecords(new URL(eventJsonURL), /*loadClientParam*/ false);
        List<TracTracRaceRecordDTO> result = new ArrayList<TracTracRaceRecordDTO>();
        for (RaceRecord raceRecord : raceRecords.getB()) {
            if (listHiddenRaces == false && raceRecord.getRaceStatus().equals(TracTracConnectionConstants.HIDDEN_STATUS)) {
                continue;
            }
            
            result.add(new TracTracRaceRecordDTO(raceRecord.getID(), raceRecord.getEventName(), raceRecord.getName(),
                    raceRecord.getTrackingStartTime().asDate(), 
                    raceRecord
                    .getTrackingEndTime().asDate(), raceRecord.getRaceStartTime().asDate(),
                    raceRecord.getBoatClassNames(), raceRecord.getRaceStatus(), raceRecord.getJsonURL().toString()));
        }
        return new Pair<String, List<TracTracRaceRecordDTO>>(raceRecords.getA(), result);
    }

    @Override
    public void trackWithTracTrac(RegattaIdentifier regattaToAddTo, Iterable<TracTracRaceRecordDTO> rrs, String liveURI, String storedURI, 
            String courseDesignUpdateURI, boolean trackWind, final boolean correctWindByDeclination, final boolean simulateWithStartTimeNow, 
            String tracTracUsername, String tracTracPassword) throws Exception {
        logger.info("tracWithTracTrac for regatta "+regattaToAddTo+" for race records "+rrs+" with liveURI "+liveURI+" and storedURI "+storedURI);
        for (TracTracRaceRecordDTO rr : rrs) {
            // reload JSON and load clientparams.php
            RaceRecord record = getTracTracAdapter().getSingleTracTracRaceRecord(new URL(rr.jsonURL), rr.id, /*loadClientParams*/true);
            logger.info("Loaded race " + record.getName() + " in " + record.getEventName() + " start:" + record.getRaceStartTime() + " trackingStart:" + record.getTrackingStartTime() + " trackingEnd:" + record.getTrackingEndTime());
            // note that the live URI may be null for races that were put into replay mode
            final String effectiveLiveURI;
            if (!record.getRaceStatus().equals(TracTracConnectionConstants.REPLAY_STATUS)) {
                if (liveURI == null || liveURI.trim().length() == 0) {
                    effectiveLiveURI = record.getLiveURI() == null ? null : record.getLiveURI().toString();
                } else {
                    effectiveLiveURI = liveURI;
                }
            } else {
                effectiveLiveURI = null;
            }
            final String effectiveStoredURI;
            if (storedURI == null || storedURI.trim().length() == 0) {
                effectiveStoredURI = record.getStoredURI().toString();
            } else {
                effectiveStoredURI = storedURI;
            }
            final RacesHandle raceHandle = getTracTracAdapter().addTracTracRace(getService(), regattaToAddTo,
                    record.getParamURL(), effectiveLiveURI == null ? null : new URI(effectiveLiveURI),
                    new URI(effectiveStoredURI), new URI(courseDesignUpdateURI),
                    new MillisecondsTimePoint(record.getTrackingStartTime().asMillis()),
                    new MillisecondsTimePoint(record.getTrackingEndTime().asMillis()),
                    MongoRaceLogStoreFactory.INSTANCE.getMongoRaceLogStore(mongoObjectFactory, domainObjectFactory),
                    RaceTracker.TIMEOUT_FOR_RECEIVING_RACE_DEFINITION_IN_MILLISECONDS, simulateWithStartTimeNow, 
                    tracTracUsername, tracTracPassword,
                    record.getRaceStatus());
            if (trackWind) {
                new Thread("Wind tracking starter for race " + record.getEventName() + "/" + record.getName()) {
                    public void run() {
                        try {
                            startTrackingWind(raceHandle, correctWindByDeclination,
                                    RaceTracker.TIMEOUT_FOR_RECEIVING_RACE_DEFINITION_IN_MILLISECONDS);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }.start();
            }
        }
    }

    private SwissTimingReplayService getSwissTimingReplayService() {
        return swissTimingReplayService;
    }

    @Override
    public List<TracTracConfigurationDTO> getPreviousTracTracConfigurations() throws Exception {
        Iterable<TracTracConfiguration> configs = tractracDomainObjectFactory.getTracTracConfigurations();
        List<TracTracConfigurationDTO> result = new ArrayList<TracTracConfigurationDTO>();
        for (TracTracConfiguration ttConfig : configs) {
            result.add(new TracTracConfigurationDTO(ttConfig.getName(), ttConfig.getJSONURL().toString(),
                    ttConfig.getLiveDataURI().toString(), ttConfig.getStoredDataURI().toString(), ttConfig.getCourseDesignUpdateURI().toString(),
                    ttConfig.getTracTracUsername().toString(), ttConfig.getTracTracPassword().toString()));
        }
        return result;
    }

    @Override
    public void storeTracTracConfiguration(String name, String jsonURL, String liveDataURI, String storedDataURI, String courseDesignUpdateURI, String tracTracUsername, String tracTracPassword) throws Exception {
        tractracMongoObjectFactory.storeTracTracConfiguration(getTracTracAdapter().createTracTracConfiguration(name, jsonURL, liveDataURI, storedDataURI, 
                courseDesignUpdateURI, tracTracUsername, tracTracPassword));
    }

    @Override
    public void stopTrackingEvent(RegattaIdentifier regattaIdentifier) throws Exception {
        getService().apply(new StopTrackingRegatta(regattaIdentifier));
    }

    private RaceDefinition getRaceByName(Regatta regatta, String raceName) {
        if (regatta != null) {
            return regatta.getRaceByName(raceName);
        } else {
            return null;
        }
    }

    @Override
    public void stopTrackingRaces(Iterable<RegattaAndRaceIdentifier> regattaAndRaceIdentifiers) throws Exception {
        for (RegattaAndRaceIdentifier regattaAndRaceIdentifier : regattaAndRaceIdentifiers) {
            getService().apply(new StopTrackingRace(regattaAndRaceIdentifier));
        }
    }

    @Override
    public void removeAndUntrackRaces(Iterable<RegattaAndRaceIdentifier> regattaAndRaceIdentifiers) {
        for (RegattaAndRaceIdentifier regattaAndRaceIdentifier : regattaAndRaceIdentifiers) {
            getService().apply(new RemoveAndUntrackRace(regattaAndRaceIdentifier));
        }
    }

    /**
     * @param timeoutInMilliseconds eventually passed to {@link RacesHandle#getRaces(long)}. If the race definition
     * can be obtained within this timeout, wind for the race will be tracked; otherwise, the method returns without
     * taking any effect.
     */
    private void startTrackingWind(RacesHandle raceHandle, boolean correctByDeclination, long timeoutInMilliseconds) throws Exception {
        Regatta regatta = raceHandle.getRegatta();
        if (regatta != null) {
            for (RaceDefinition race : raceHandle.getRaces(timeoutInMilliseconds)) {
                if (race != null) {
                    getService().startTrackingWind(regatta, race, correctByDeclination);
                } else {
                    log("RaceDefinition wasn't received within " + timeoutInMilliseconds + "ms for a race in regatta "
                            + regatta.getName() + ". Aborting wait; no wind tracking for this race.");
                }
            }
        }
    }

    @Override
    public WindInfoForRaceDTO getRawWindFixes(RegattaAndRaceIdentifier raceIdentifier, Collection<WindSource> windSources) {
        WindInfoForRaceDTO result = null;
        TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
        if (trackedRace != null) {
            result = new WindInfoForRaceDTO();
            result.raceIsKnownToStartUpwind = trackedRace.raceIsKnownToStartUpwind();
            Map<WindSource, WindTrackInfoDTO> windTrackInfoDTOs = new HashMap<WindSource, WindTrackInfoDTO>();
            result.windTrackInfoByWindSource = windTrackInfoDTOs;

            List<WindSource> windSourcesToDeliver = new ArrayList<WindSource>();
            if (windSources != null) {
                windSourcesToDeliver.addAll(windSources);
            } else {
                windSourcesToDeliver.add(new WindSourceImpl(WindSourceType.EXPEDITION));
                windSourcesToDeliver.add(new WindSourceImpl(WindSourceType.WEB));
            }
            for (WindSource windSource : windSourcesToDeliver) {
                if(windSource.getType() == WindSourceType.WEB) {
                    WindTrackInfoDTO windTrackInfoDTO = new WindTrackInfoDTO();
                    windTrackInfoDTO.windFixes = new ArrayList<WindDTO>();
                    WindTrack windTrack = trackedRace.getOrCreateWindTrack(windSource);

                    windTrack.lockForRead();
                    try {
                        Iterator<Wind> windIter = windTrack.getRawFixes().iterator();
                        while (windIter.hasNext()) {
                            Wind wind = windIter.next();
                            if(wind != null) {
                                WindDTO windDTO = createWindDTO(wind, windTrack);
                                windTrackInfoDTO.windFixes.add(windDTO);
                            }
                        }
                    } finally {
                        windTrack.unlockAfterRead();
                    }

                    windTrackInfoDTOs.put(windSource, windTrackInfoDTO);
                }
            }
        }
        return result;
    }

    protected WindDTO createWindDTO(Wind wind, WindTrack windTrack) {
        WindDTO windDTO = new WindDTO();
        windDTO.trueWindBearingDeg = wind.getBearing().getDegrees();
        windDTO.trueWindFromDeg = wind.getBearing().reverse().getDegrees();
        windDTO.trueWindSpeedInKnots = wind.getKnots();
        windDTO.trueWindSpeedInMetersPerSecond = wind.getMetersPerSecond();
        if (wind.getPosition() != null) {
            windDTO.position = new PositionDTO(wind.getPosition().getLatDeg(), wind.getPosition()
                    .getLngDeg());
        }
        if (wind.getTimePoint() != null) {
            windDTO.measureTimepoint = wind.getTimePoint().asMillis();
            Wind estimatedWind = windTrack
                    .getAveragedWind(wind.getPosition(), wind.getTimePoint());
            if (estimatedWind != null) {
                windDTO.dampenedTrueWindBearingDeg = estimatedWind.getBearing().getDegrees();
                windDTO.dampenedTrueWindFromDeg = estimatedWind.getBearing().reverse().getDegrees();
                windDTO.dampenedTrueWindSpeedInKnots = estimatedWind.getKnots();
                windDTO.dampenedTrueWindSpeedInMetersPerSecond = estimatedWind.getMetersPerSecond();
            }
        }
        return windDTO;
    }

    /**
     * Uses <code>wind</code> for both, the non-dampened and dampened fields of the {@link WindDTO} object returned
     */
    protected WindDTO createWindDTOFromAlreadyAveraged(Wind wind, TimePoint requestTimepoint) {
        WindDTO windDTO = new WindDTO();
        windDTO.requestTimepoint = requestTimepoint.asMillis();
        windDTO.trueWindBearingDeg = wind.getBearing().getDegrees();
        windDTO.trueWindFromDeg = wind.getBearing().reverse().getDegrees();
        windDTO.trueWindSpeedInKnots = wind.getKnots();
        windDTO.trueWindSpeedInMetersPerSecond = wind.getMetersPerSecond();
        windDTO.dampenedTrueWindBearingDeg = wind.getBearing().getDegrees();
        windDTO.dampenedTrueWindFromDeg = wind.getBearing().reverse().getDegrees();
        windDTO.dampenedTrueWindSpeedInKnots = wind.getKnots();
        windDTO.dampenedTrueWindSpeedInMetersPerSecond = wind.getMetersPerSecond();
        if (wind.getPosition() != null) {
            windDTO.position = new PositionDTO(wind.getPosition().getLatDeg(), wind.getPosition()
                    .getLngDeg());
        }
        if (wind.getTimePoint() != null) {
            windDTO.measureTimepoint = wind.getTimePoint().asMillis();
        }
        return windDTO;
    }

    /**
     * Fetches the {@link WindTrack#getAveragedWind(Position, TimePoint) average wind} from all wind tracks or those identified
     * by <code>windSourceTypeNames</code>
     */
    //@Override
    public WindInfoForRaceDTO getAveragedWindInfo(RegattaAndRaceIdentifier raceIdentifier, Date from, long millisecondsStepWidth,
            int numberOfFixes, double latDeg, double lngDeg, Collection<String> windSourceTypeNames)
                    throws NoWindException {
        Position position = new DegreePosition(latDeg, lngDeg);
        WindInfoForRaceDTO result = null;
        TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
        if (trackedRace != null) {
            result = new WindInfoForRaceDTO();
            result.raceIsKnownToStartUpwind = trackedRace.raceIsKnownToStartUpwind();
            List<WindSource> windSourcesToExclude = new ArrayList<WindSource>();
            for (WindSource windSourceToExclude : trackedRace.getWindSourcesToExclude()) {
                windSourcesToExclude.add(windSourceToExclude);
            }
            result.windSourcesToExclude = windSourcesToExclude;
            Map<WindSource, WindTrackInfoDTO> windTrackInfoDTOs = new HashMap<WindSource, WindTrackInfoDTO>();
            result.windTrackInfoByWindSource = windTrackInfoDTOs;
            List<WindSource> windSourcesToDeliver = new ArrayList<WindSource>();
            Util.addAll(trackedRace.getWindSources(), windSourcesToDeliver);
            // TODO bug #375: add the combined wind; currently, CombinedWindTrackImpl just takes too long to return results...
            windSourcesToDeliver.add(new WindSourceImpl(WindSourceType.COMBINED));
            for (WindSource windSource : windSourcesToDeliver) {
                if (windSourceTypeNames == null || windSourceTypeNames.contains(windSource.getType().name())) {
                    TimePoint fromTimePoint = new MillisecondsTimePoint(from);
                    WindTrackInfoDTO windTrackInfoDTO = new WindTrackInfoDTO();
                    windTrackInfoDTO.windFixes = new ArrayList<WindDTO>();
                    WindTrack windTrack = trackedRace.getOrCreateWindTrack(windSource);
                    windTrackInfoDTOs.put(windSource, windTrackInfoDTO);
                    windTrackInfoDTO.dampeningIntervalInMilliseconds = windTrack
                            .getMillisecondsOverWhichToAverageWind();
                    TimePoint timePoint = fromTimePoint;
                    Double minWindConfidence = 2.0;
                    Double maxWindConfidence = -1.0;
                    for (int i = 0; i < numberOfFixes; i++) {
                        WindWithConfidence<Pair<Position, TimePoint>> averagedWindWithConfidence = windTrack.getAveragedWindWithConfidence(position, timePoint);

                        if (averagedWindWithConfidence != null) {
                            WindDTO windDTO = createWindDTOFromAlreadyAveraged(averagedWindWithConfidence.getObject(), timePoint);
                            double confidence = averagedWindWithConfidence.getConfidence();
                            windDTO.confidence = confidence;
                            windTrackInfoDTO.windFixes.add(windDTO);
                            if(confidence < minWindConfidence) {
                                minWindConfidence = confidence;
                            }
                            if(confidence > maxWindConfidence) {
                                maxWindConfidence = confidence;
                            }
                        }
                        timePoint = new MillisecondsTimePoint(timePoint.asMillis() + millisecondsStepWidth);
                    }
                    windTrackInfoDTO.minWindConfidence = minWindConfidence; 
                    windTrackInfoDTO.maxWindConfidence = maxWindConfidence; 
                }
            }
        }
        return result;
    }

    @Override
    public WindInfoForRaceDTO getAveragedWindInfo(RegattaAndRaceIdentifier raceIdentifier, Date from, long millisecondsStepWidth,
            int numberOfFixes, Collection<String> windSourceTypeNames)
                    throws NoWindException {
        assert from != null;
        TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);

        WindInfoForRaceDTO result = getAveragedWindInfo(new MillisecondsTimePoint(from), millisecondsStepWidth, numberOfFixes,
                windSourceTypeNames, trackedRace);
        return result;
    }

    private WindInfoForRaceDTO getAveragedWindInfo(TimePoint from, long millisecondsStepWidth, int numberOfFixes,
            Collection<String> windSourceTypeNames, TrackedRace trackedRace) {
        WindInfoForRaceDTO result = null;
        if (trackedRace != null) {
            TimePoint newestEvent = trackedRace.getTimePointOfNewestEvent();
            result = new WindInfoForRaceDTO();
            result.raceIsKnownToStartUpwind = trackedRace.raceIsKnownToStartUpwind();
            List<WindSource> windSourcesToExclude = new ArrayList<WindSource>();
            for (WindSource windSourceToExclude : trackedRace.getWindSourcesToExclude()) {
                windSourcesToExclude.add(windSourceToExclude);
            }
            result.windSourcesToExclude = windSourcesToExclude;
            Map<WindSource, WindTrackInfoDTO> windTrackInfoDTOs = new HashMap<WindSource, WindTrackInfoDTO>();
            result.windTrackInfoByWindSource = windTrackInfoDTOs;
            List<WindSource> windSourcesToDeliver = new ArrayList<WindSource>();
            Util.addAll(trackedRace.getWindSources(), windSourcesToDeliver);
            windSourcesToDeliver.add(new WindSourceImpl(WindSourceType.COMBINED));
            for (WindSource windSource : windSourcesToDeliver) {
                // TODO consider parallelizing
                if (windSourceTypeNames == null || windSourceTypeNames.contains(windSource.getType().name())) {
                    WindTrackInfoDTO windTrackInfoDTO = new WindTrackInfoDTO();
                    windTrackInfoDTOs.put(windSource, windTrackInfoDTO);
                    windTrackInfoDTO.windFixes = new ArrayList<WindDTO>();
                    WindTrack windTrack = trackedRace.getOrCreateWindTrack(windSource);
                    windTrackInfoDTO.dampeningIntervalInMilliseconds = windTrack
                            .getMillisecondsOverWhichToAverageWind();
                    TimePoint timePoint = from;
                    Double minWindConfidence = 2.0;
                    Double maxWindConfidence = -1.0;
                    for (int i = 0; i < numberOfFixes && newestEvent != null && timePoint.compareTo(newestEvent) < 0; i++) {
                        WindWithConfidence<Pair<Position, TimePoint>> averagedWindWithConfidence = windTrack.getAveragedWindWithConfidence(null, timePoint);
                        if (averagedWindWithConfidence != null) {
                            double confidence = averagedWindWithConfidence.getConfidence();
                            WindDTO windDTO = createWindDTOFromAlreadyAveraged(averagedWindWithConfidence.getObject(), timePoint);
                            windDTO.confidence = confidence;
                            windTrackInfoDTO.windFixes.add(windDTO);
                            if(confidence < minWindConfidence) {
                                minWindConfidence = confidence;
                            }
                            if (confidence > maxWindConfidence) {
                                maxWindConfidence = confidence;
                            }
                        }
                        timePoint = new MillisecondsTimePoint(timePoint.asMillis() + millisecondsStepWidth);
                    }
                    windTrackInfoDTO.minWindConfidence = minWindConfidence; 
                    windTrackInfoDTO.maxWindConfidence = maxWindConfidence; 
                }
            }
        }
        return result;
    }

    @Override
    public WindInfoForRaceDTO getAveragedWindInfo(RegattaAndRaceIdentifier raceIdentifier, Date from, Date to,
            long resolutionInMilliseconds, Collection<String> windSourceTypeNames) {
        TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
        WindInfoForRaceDTO result = null;
        if (trackedRace != null) {
            TimePoint fromTimePoint = from == null ? trackedRace.getStartOfTracking() : new MillisecondsTimePoint(from);
            TimePoint toTimePoint = to == null ? trackedRace.getEndOfRace() : new MillisecondsTimePoint(to);
            if (fromTimePoint != null && toTimePoint != null) {
                int numberOfFixes = (int) ((toTimePoint.asMillis() - fromTimePoint.asMillis())/resolutionInMilliseconds);
                result = getAveragedWindInfo(fromTimePoint, resolutionInMilliseconds, numberOfFixes, windSourceTypeNames, trackedRace);
            }
        }
        return result;
    }

    @Override
    public void setWind(RegattaAndRaceIdentifier raceIdentifier, WindDTO windDTO) {
        DynamicTrackedRace trackedRace = (DynamicTrackedRace) getExistingTrackedRace(raceIdentifier);
        if (trackedRace != null) {
            Position p = null;
            if (windDTO.position != null) {
                p = new DegreePosition(windDTO.position.latDeg, windDTO.position.lngDeg);
            }
            TimePoint at = null;
            if (windDTO.measureTimepoint != null) {
                at = new MillisecondsTimePoint(windDTO.measureTimepoint);
            }
            SpeedWithBearing speedWithBearing = null;
            Speed speed = null;
            if (windDTO.trueWindSpeedInKnots != null) {
                speed = new KnotSpeedImpl(windDTO.trueWindSpeedInKnots);
            } else if (windDTO.trueWindSpeedInMetersPerSecond != null) {
                speed = new KilometersPerHourSpeedImpl(windDTO.trueWindSpeedInMetersPerSecond * 3600. / 1000.);
            } else if (windDTO.dampenedTrueWindSpeedInKnots != null) {
                speed = new KnotSpeedImpl(windDTO.dampenedTrueWindSpeedInKnots);
            } else if (windDTO.dampenedTrueWindSpeedInMetersPerSecond != null) {
                speed = new KilometersPerHourSpeedImpl(windDTO.dampenedTrueWindSpeedInMetersPerSecond * 3600. / 1000.);
            }
            if (speed != null) {
                if (windDTO.trueWindBearingDeg != null) {
                    speedWithBearing = new KnotSpeedWithBearingImpl(speed.getKnots(), new DegreeBearingImpl(
                            windDTO.trueWindBearingDeg));
                } else if (windDTO.trueWindFromDeg != null) {
                    speedWithBearing = new KnotSpeedWithBearingImpl(speed.getKnots(), new DegreeBearingImpl(
                            windDTO.trueWindFromDeg).reverse());
                }
            }
            Wind wind = new WindImpl(p, at, speedWithBearing);
            Iterable<WindSource> webWindSources = trackedRace.getWindSources(WindSourceType.WEB);
            if(Util.size(webWindSources) == 0) {
                // create a new WEB wind source if not available
                trackedRace.recordWind(wind, new WindSourceImpl(WindSourceType.WEB));
            } else {
                trackedRace.recordWind(wind, webWindSources.iterator().next());
            }
        }
    }

    @Override
    public CompactRaceMapDataDTO getRaceMapData(RegattaAndRaceIdentifier raceIdentifier, Date date,
            Map<String, Date> fromPerCompetitorIdAsString, Map<String, Date> toPerCompetitorIdAsString,
            boolean extrapolate) throws NoWindException {
        return new CompactRaceMapDataDTO(getBoatPositions(raceIdentifier, fromPerCompetitorIdAsString,
                toPerCompetitorIdAsString, extrapolate), getCoursePositions(raceIdentifier, date), getCourseSidelines(raceIdentifier, date), getQuickRanks(
                raceIdentifier, date));
    }    

    /**
     * @param from
     *            for the list of competitors provided as keys of this map, requests the GPS fixes starting with the
     *            date provided as value
     * @param to
     *            for the list of competitors provided as keys (expected to be equal to the set of competitors used as
     *            keys in the <code>from</code> parameter, requests the GPS fixes up to but excluding the date provided
     *            as value
     * @param extrapolate
     *            if <code>true</code> and no (exact or interpolated) position is known for <code>date</code>, the last
     *            entry returned in the list of GPS fixes will be obtained by extrapolating from the competitors last
     *            known position before <code>date</code> and the estimated speed.
     * @return a map where for each competitor participating in the race the list of GPS fixes in increasing
     *         chronological order is provided. The last one is the last position at or before <code>date</code>.
     */
    private Map<CompetitorDTO, List<GPSFixDTO>> getBoatPositions(RegattaAndRaceIdentifier raceIdentifier,
            Map<String, Date> fromPerCompetitorIdAsString, Map<String, Date> toPerCompetitorIdAsString,
            boolean extrapolate) throws NoWindException {
        Map<CompetitorDTO, List<GPSFixDTO>> result = new HashMap<CompetitorDTO, List<GPSFixDTO>>();
        TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
        if (trackedRace != null) {
            for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
                if (fromPerCompetitorIdAsString.containsKey(competitor.getId().toString())) {
                    CompetitorDTO competitorDTO = baseDomainFactory.convertToCompetitorDTO(competitor);
                    List<GPSFixDTO> fixesForCompetitor = new ArrayList<GPSFixDTO>();
                    result.put(competitorDTO, fixesForCompetitor);
                    GPSFixTrack<Competitor, GPSFixMoving> track = trackedRace.getTrack(competitor);
                    TimePoint fromTimePoint = new MillisecondsTimePoint(fromPerCompetitorIdAsString.get(competitorDTO.getIdAsString()));
                    TimePoint toTimePointExcluding = new MillisecondsTimePoint(toPerCompetitorIdAsString.get(competitorDTO.getIdAsString()));
                    // copy the fixes into a list while holding the monitor; then release the monitor to avoid deadlocks
                    // during wind estimations required for tack determination
                    List<GPSFixMoving> fixes = new ArrayList<GPSFixMoving>();
                    track.lockForRead();
                    try {
                        Iterator<GPSFixMoving> fixIter = track.getFixesIterator(fromTimePoint, /* inclusive */true);
                        while (fixIter.hasNext()) {
                            GPSFixMoving fix = fixIter.next();
                            if (fix.getTimePoint().compareTo(toTimePointExcluding) < 0) {
                                fixes.add(fix);
                            } else {
                                break;
                            }
                        }
                    } finally {
                        track.unlockAfterRead();
                    }
                    if (fixes.isEmpty()) {
                        // then there was no (smoothened) fix between fromTimePoint and toTimePointExcluding; estimate...
                        TimePoint middle = new MillisecondsTimePoint((toTimePointExcluding.asMillis()+fromTimePoint.asMillis())/2);
                        Position estimatedPosition = track.getEstimatedPosition(middle, extrapolate);
                        SpeedWithBearing estimatedSpeed = track.getEstimatedSpeed(middle);
                        if (estimatedPosition != null && estimatedSpeed != null) {
                            fixes.add(new GPSFixMovingImpl(estimatedPosition, middle, estimatedSpeed));
                        }
                    }
                    Iterator<GPSFixMoving> fixIter = fixes.iterator();
                    if (fixIter.hasNext()) {
                        GPSFixMoving fix = fixIter.next();
                        while (fix != null && (fix.getTimePoint().compareTo(toTimePointExcluding) < 0 ||
                                (fix.getTimePoint().equals(toTimePointExcluding) && toTimePointExcluding.equals(fromTimePoint)))) {
                            Tack tack;
                            try {
                                tack = trackedRace.getTack(competitor, fix.getTimePoint());
                            } catch (NoWindException nwe) {
                                tack = null;
                            }
                            TrackedLegOfCompetitor trackedLegOfCompetitor = trackedRace.getTrackedLeg(competitor,
                                    fix.getTimePoint());
                            LegType legType = trackedLegOfCompetitor == null ? null : trackedRace.getTrackedLeg(
                                    trackedLegOfCompetitor.getLeg()).getLegType(fix.getTimePoint());
                            Wind wind = trackedRace.getWind(fix.getPosition(),toTimePointExcluding);
                            WindDTO windDTO = wind == null ? null : createWindDTOFromAlreadyAveraged(wind, toTimePointExcluding);
                            GPSFixDTO fixDTO = createGPSFixDTO(fix, fix.getSpeed(), windDTO, tack, legType, /* extrapolate */
                                    false);
                            fixesForCompetitor.add(fixDTO);
                            if (fixIter.hasNext()) {
                                fix = fixIter.next();
                            } else {
                                // check if fix was at date and if extrapolation is requested
                                if (!fix.getTimePoint().equals(toTimePointExcluding) && extrapolate) {
                                    Position position = track.getEstimatedPosition(toTimePointExcluding, extrapolate);
                                    Tack tack2;
                                    try {
                                        tack2 = trackedRace.getTack(competitor, toTimePointExcluding);
                                    } catch (NoWindException nwe) {
                                        tack2 = null;
                                    }
                                    LegType legType2 = trackedLegOfCompetitor == null ? null : trackedRace
                                            .getTrackedLeg(trackedLegOfCompetitor.getLeg()).getLegType(
                                                    fix.getTimePoint());
                                    SpeedWithBearing speedWithBearing = track.getEstimatedSpeed(toTimePointExcluding);
                                    Wind wind2 = trackedRace.getWind(position, toTimePointExcluding);
                                    WindDTO windDTO2 = wind2 == null ? null : createWindDTOFromAlreadyAveraged(wind2, toTimePointExcluding);
                                    GPSFixDTO extrapolated = new GPSFixDTO(
                                            toPerCompetitorIdAsString.get(competitorDTO.getIdAsString()),
                                            position==null?null:new PositionDTO(position.getLatDeg(), position.getLngDeg()),
                                                    speedWithBearing==null?null:createSpeedWithBearingDTO(speedWithBearing), windDTO2,
                                                            tack2, legType2, /* extrapolated */ true);
                                    fixesForCompetitor.add(extrapolated);
                                }
                                fix = null;
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    private SpeedWithBearingDTO createSpeedWithBearingDTO(SpeedWithBearing speedWithBearing) {
        return new SpeedWithBearingDTO(speedWithBearing.getKnots(), speedWithBearing
                .getBearing().getDegrees());
    }

    private GPSFixDTO createGPSFixDTO(GPSFix fix, SpeedWithBearing speedWithBearing, WindDTO windDTO, Tack tack, LegType legType, boolean extrapolated) {
        return new GPSFixDTO(fix.getTimePoint().asDate(), fix.getPosition()==null?null:new PositionDTO(fix
                .getPosition().getLatDeg(), fix.getPosition().getLngDeg()),
                speedWithBearing==null?null:createSpeedWithBearingDTO(speedWithBearing), windDTO, tack, legType, extrapolated);
    }

    @Override
    public RaceTimesInfoDTO getRaceTimesInfo(RegattaAndRaceIdentifier raceIdentifier) {
        RaceTimesInfoDTO raceTimesInfo = null;
        TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);

        if (trackedRace != null) {
            raceTimesInfo = new RaceTimesInfoDTO(raceIdentifier);
            List<LegInfoDTO> legInfos = new ArrayList<LegInfoDTO>();
            raceTimesInfo.setLegInfos(legInfos);
            List<MarkPassingTimesDTO> markPassingTimesDTOs = new ArrayList<MarkPassingTimesDTO>();
            raceTimesInfo.setMarkPassingTimes(markPassingTimesDTOs);

            raceTimesInfo.startOfRace = trackedRace.getStartOfRace() == null ? null : trackedRace.getStartOfRace().asDate();
            raceTimesInfo.startOfTracking = trackedRace.getStartOfTracking() == null ? null : trackedRace.getStartOfTracking().asDate();
            raceTimesInfo.newestTrackingEvent = trackedRace.getTimePointOfNewestEvent() == null ? null : trackedRace.getTimePointOfNewestEvent().asDate();
            raceTimesInfo.endOfTracking = trackedRace.getEndOfTracking() == null ? null : trackedRace.getEndOfTracking().asDate();
            raceTimesInfo.endOfRace = trackedRace.getEndOfRace() == null ? null : trackedRace.getEndOfRace().asDate();
            raceTimesInfo.delayToLiveInMs = trackedRace.getDelayToLiveInMillis();

            Iterable<Pair<Waypoint, Pair<TimePoint, TimePoint>>> markPassingsTimes = trackedRace.getMarkPassingsTimes();
            synchronized (markPassingsTimes) {
                int numberOfWaypoints = Util.size(markPassingsTimes);
                int wayPointNumber = 1;
                for (Pair<Waypoint, Pair<TimePoint, TimePoint>> markPassingTimes : markPassingsTimes) {
                    MarkPassingTimesDTO markPassingTimesDTO = new MarkPassingTimesDTO();
                    String name = "M" + (wayPointNumber - 1);
                    if (wayPointNumber == numberOfWaypoints) {
                        name = "F";
                    }
                    markPassingTimesDTO.setName(name);
                    Pair<TimePoint, TimePoint> timesPair = markPassingTimes.getB();
                    TimePoint firstPassingTime = timesPair.getA();
                    TimePoint lastPassingTime = timesPair.getB();
                    markPassingTimesDTO.firstPassingDate = firstPassingTime == null ? null : firstPassingTime.asDate();
                    markPassingTimesDTO.lastPassingDate = lastPassingTime == null ? null : lastPassingTime.asDate();
                    markPassingTimesDTOs.add(markPassingTimesDTO);
                    wayPointNumber++;
                }
            }
            trackedRace.getRace().getCourse().lockForRead();
            try {
                Iterable<TrackedLeg> trackedLegs = trackedRace.getTrackedLegs();
                int legNumber = 1;
                for (TrackedLeg trackedLeg : trackedLegs) {
                    LegInfoDTO legInfoDTO = new LegInfoDTO(legNumber);
                    legInfoDTO.setName("L" + legNumber);
                    try {
                        MarkPassingTimesDTO markPassingTimesDTO = markPassingTimesDTOs.get(legNumber - 1);
                        if (markPassingTimesDTO.firstPassingDate != null) {
                            TimePoint p = new MillisecondsTimePoint(markPassingTimesDTO.firstPassingDate);
                            legInfoDTO.legType = trackedLeg.getLegType(p);
                            legInfoDTO.legBearingInDegrees = trackedLeg.getLegBearing(p).getDegrees();
                        }
                    } catch (NoWindException e) {
                        // do nothing
                    }
                    legInfos.add(legInfoDTO);
                    legNumber++;
                }
            } finally {
                trackedRace.getRace().getCourse().unlockAfterRead();
            }
        }   
        if (raceTimesInfo != null) {
            raceTimesInfo.currentServerTime = new Date();
        }
        return raceTimesInfo;
    }

    @Override
    public List<RaceTimesInfoDTO> getRaceTimesInfos(Collection<RegattaAndRaceIdentifier> raceIdentifiers) {
        List<RaceTimesInfoDTO> raceTimesInfos = new ArrayList<RaceTimesInfoDTO>();
        for (RegattaAndRaceIdentifier raceIdentifier : raceIdentifiers) {
            RaceTimesInfoDTO raceTimesInfo = getRaceTimesInfo(raceIdentifier);
            if (raceTimesInfo != null) {
                raceTimesInfos.add(raceTimesInfo);
            }
        }
        return raceTimesInfos;
    }

    private List<SidelineDTO> getCourseSidelines(RegattaAndRaceIdentifier raceIdentifier, Date date) {
        List<SidelineDTO> result = new ArrayList<SidelineDTO>();
        if (date != null) {
            TimePoint dateAsTimePoint = new MillisecondsTimePoint(date);
            TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
            if (trackedRace != null) {
                for (Sideline sideline : trackedRace.getCourseSidelines()) {
                    List<MarkDTO> markDTOs = new ArrayList<MarkDTO>();
                    for (Mark mark : sideline.getMarks()) {
                        GPSFixTrack<Mark, GPSFix> track = trackedRace.getOrCreateTrack(mark);
                        Position positionAtDate = track.getEstimatedPosition(dateAsTimePoint, /* extrapolate */false);
                        if (positionAtDate != null) {
                            markDTOs.add(convertToMarkDTO(mark, positionAtDate));
                        }
                    }
                    result.add(new SidelineDTO(sideline.getName(), markDTOs));
                }
            }
        }
        return result;
    }
        
    @Override
    public CoursePositionsDTO getCoursePositions(RegattaAndRaceIdentifier raceIdentifier, Date date) {
        CoursePositionsDTO result = new CoursePositionsDTO();
        if (date != null) {
            TimePoint dateAsTimePoint = new MillisecondsTimePoint(date);
            TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
            if (trackedRace != null) {
                result.marks = new HashSet<MarkDTO>();
                result.waypointPositions = new ArrayList<PositionDTO>();
                Set<Mark> marks = new HashSet<Mark>();
                Course course = trackedRace.getRace().getCourse();
                for (Waypoint waypoint : course.getWaypoints()) {
                    Position waypointPosition = trackedRace.getApproximatePosition(waypoint, dateAsTimePoint);
                    if (waypointPosition != null) {
                        result.waypointPositions.add(new PositionDTO(waypointPosition.getLatDeg(), waypointPosition.getLngDeg()));
                    }
                    for (Mark b : waypoint.getMarks()) {
                        marks.add(b);
                    }
                }
                for (Mark mark : marks) {
                    GPSFixTrack<Mark, GPSFix> track = trackedRace.getOrCreateTrack(mark);
                    Position positionAtDate = track.getEstimatedPosition(dateAsTimePoint, /* extrapolate */false);
                    if (positionAtDate != null) {
                        result.marks.add(convertToMarkDTO(mark, positionAtDate));
                    }
                }

                // set the positions of start and finish
                Waypoint firstWaypoint = course.getFirstWaypoint();
                if (firstWaypoint != null && Util.size(firstWaypoint.getMarks())==2) {
                    final LineDetails markPositionDTOsAndLineAdvantage = trackedRace.getStartLine(dateAsTimePoint);
                    if (markPositionDTOsAndLineAdvantage != null) {
                        final List<PositionDTO> startMarkPositionDTOs = getMarkPositionDTOs(dateAsTimePoint, trackedRace, firstWaypoint);
                        result.startMarkPositions = startMarkPositionDTOs;
                        result.startLineLengthInMeters = markPositionDTOsAndLineAdvantage.getLength().getMeters();
                        result.startLineAngleToCombinedWind = markPositionDTOsAndLineAdvantage.getAbsoluteAngleDifferenceToTrueWind().getDegrees();
                        result.startLineAdvantageousSide = markPositionDTOsAndLineAdvantage.getAdvantageousSideWhileApproachingLine();
                        result.startLineAdvantageInMeters = markPositionDTOsAndLineAdvantage.getAdvantage().getMeters();
                    }
                }                    
                Waypoint lastWaypoint = course.getLastWaypoint();
                if (lastWaypoint != null && Util.size(lastWaypoint.getMarks())==2) {
                    final LineDetails markPositionDTOsAndLineAdvantage = trackedRace.getFinishLine(dateAsTimePoint);
                    if (markPositionDTOsAndLineAdvantage != null) {
                        final List<PositionDTO> finishMarkPositionDTOs = getMarkPositionDTOs(dateAsTimePoint, trackedRace, lastWaypoint);
                        result.finishMarkPositions = finishMarkPositionDTOs;
                        result.finishLineLengthInMeters = markPositionDTOsAndLineAdvantage.getLength().getMeters();
                        result.finishLineAngleToCombinedWind = markPositionDTOsAndLineAdvantage.getAbsoluteAngleDifferenceToTrueWind().getDegrees();
                        result.finishLineAdvantageousSide = markPositionDTOsAndLineAdvantage.getAdvantageousSideWhileApproachingLine();
                        result.finishLineAdvantageInMeters = markPositionDTOsAndLineAdvantage.getAdvantage().getMeters();
                    }
                }
            }
        }
        return result;
    }
      
    @Override
    public RaceCourseDTO getRaceCourse(RegattaAndRaceIdentifier raceIdentifier, Date date) {
        List<WaypointDTO> waypointDTOs = new ArrayList<WaypointDTO>();
        RaceCourseDTO result = new RaceCourseDTO(waypointDTOs);
        if (date != null) {
            TimePoint dateAsTimePoint = new MillisecondsTimePoint(date);
            TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
            if (trackedRace != null) {
                Course course = trackedRace.getRace().getCourse();
                for (Waypoint waypoint : course.getWaypoints()) {
                    List<MarkDTO> markDTOs = new ArrayList<MarkDTO>();
                    for (Mark mark : trackedRace.getMarks()) {
                        GPSFixTrack<Mark, GPSFix> track = trackedRace.getOrCreateTrack(mark);
                        Position positionAtDate = track.getEstimatedPosition(dateAsTimePoint, /* extrapolate */false);
                        markDTOs.add(convertToMarkDTO(mark, positionAtDate));
                    }
                    ControlPointDTO controlPointDTO = convertToControlPointDTO(waypoint.getControlPoint(), trackedRace, dateAsTimePoint);
                    WaypointDTO waypointDTO = new WaypointDTO(waypoint.getName(), controlPointDTO, markDTOs, waypoint.getPassingInstructions());
                    waypointDTOs.add(waypointDTO);
                }
            }
        }
        return result;
    }
    
    private ControlPointDTO convertToControlPointDTO(ControlPoint controlPoint, TrackedRace trackedRace, TimePoint timePoint) {
        ControlPointDTO result;
        if (controlPoint instanceof ControlPointWithTwoMarks) {
            final Mark left = ((ControlPointWithTwoMarks) controlPoint).getLeft();
            final Position leftPos = trackedRace.getOrCreateTrack(left).getEstimatedPosition(timePoint, /* extrapolate */ false);
            final Mark right = ((ControlPointWithTwoMarks) controlPoint).getRight();
            final Position rightPos = trackedRace.getOrCreateTrack(right).getEstimatedPosition(timePoint, /* extrapolate */ false);
            result = new GateDTO(controlPoint.getId().toString(), controlPoint.getName(), convertToMarkDTO(left, leftPos), convertToMarkDTO(right, rightPos)); 
        } else {
            final Position posOfFirst = trackedRace.getOrCreateTrack(controlPoint.getMarks().iterator().next()).
                    getEstimatedPosition(timePoint, /* extrapolate */ false);
            result = new MarkDTO(controlPoint.getId().toString(), controlPoint.getName(), posOfFirst.getLatDeg(), posOfFirst.getLngDeg());
        }
        return result;
    }
    
    private ControlPointDTO convertToControlPointDTO(ControlPoint controlPoint) {
        ControlPointDTO result;
        if (controlPoint instanceof ControlPointWithTwoMarks) {
            final Mark left = ((ControlPointWithTwoMarks) controlPoint).getLeft();
            final Mark right = ((ControlPointWithTwoMarks) controlPoint).getRight();
            result = new GateDTO(controlPoint.getId().toString(), controlPoint.getName(), convertToMarkDTO(left, null), convertToMarkDTO(right, null)); 
        } else {
            result = new MarkDTO(controlPoint.getId().toString(), controlPoint.getName());
        }
        return result;
    }

    /**
     * For each {@link ControlPointDTO} in <code>controlPoints</code> tries to find the best-matching waypoint
     * from the course that belongs to the race identified by <code>raceIdentifier</code>. If such a waypoint is
     * found, its control point is added to the control point list for the new course. Otherwise, a new control
     * point is created using the default {@link com.sap.sailing.domain.base.DomainFactory} instance. The resulting
     * list of control points is then passed to {@link Course#update(List, com.sap.sailing.domain.base.DomainFactory)} for
     * the course of the race identified by <code>raceIdentifier</code>.
     */
    @Override
    public void updateRaceCourse(RegattaAndRaceIdentifier raceIdentifier, List<Pair<ControlPointDTO, PassingInstruction>> controlPoints) {
        TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
        if (trackedRace != null) {
            Course course = trackedRace.getRace().getCourse();
            Iterable<Waypoint> waypoints = course.getWaypoints();
            List<Pair<ControlPoint, PassingInstruction>> newControlPoints = new ArrayList<Pair<ControlPoint, PassingInstruction>>();
            int lastMatchPosition = -1;
            for (Pair<ControlPointDTO, PassingInstruction> controlPointAndPassingInstruction : controlPoints) {
                ControlPointDTO controlPointDTO = controlPointAndPassingInstruction.getA();
                ControlPoint matchFromOldCourse = null;
                for (int i=lastMatchPosition+1; matchFromOldCourse == null && i<Util.size(waypoints); i++) {
                    Waypoint waypointAtI = Util.get(waypoints, i);
                    ControlPoint controlPointAtI = waypointAtI.getControlPoint();
                    if (controlPointAtI.getId().toString().equals(controlPointDTO.getIdAsString()) && markIDsMatch(controlPointAtI.getMarks(), controlPointDTO.getMarks())) {
                        matchFromOldCourse = controlPointAtI;
                        newControlPoints.add(new Pair<ControlPoint, PassingInstruction>(matchFromOldCourse, null));
                        lastMatchPosition = i;
                    }
                }
                if (matchFromOldCourse == null) {
                    // no match found; create new control point:
                    ControlPoint newControlPoint;
                    if (controlPointDTO instanceof GateDTO) {
                        GateDTO gateDTO = (GateDTO) controlPointDTO;
                        final Serializable id;
                        if (gateDTO.getIdAsString() == null) {
                            id = UUID.randomUUID();
                        } else {
                            id = gateDTO.getIdAsString();
                        }
                        Mark left = baseDomainFactory.getOrCreateMark(gateDTO.getLeft().getIdAsString(), gateDTO.getLeft().getName());
                        Mark right = baseDomainFactory.getOrCreateMark(gateDTO.getRight().getIdAsString(), gateDTO.getRight().getName());
                        newControlPoint = baseDomainFactory.createControlPointWithTwoMarks(id, left, right, gateDTO.getName());
                        newControlPoints.add(new Pair<ControlPoint, PassingInstruction>(newControlPoint, null));
                    } else {
                        newControlPoint = baseDomainFactory.getOrCreateMark(controlPointDTO.getIdAsString(), controlPointDTO.getName());
                        PassingInstruction passingInstructions = controlPointAndPassingInstruction.getB();
                        newControlPoints.add(new Pair<ControlPoint, PassingInstruction>(newControlPoint, passingInstructions));
                    }
                }
            }
            try {
                course.update(newControlPoints, baseDomainFactory);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean markIDsMatch(Iterable<Mark> marks, Iterable<MarkDTO> marksDTOs) {
        Iterator<Mark> marksIter = marks.iterator();
        Iterator<MarkDTO> markDTOsIter = marksDTOs.iterator();
        while (marksIter.hasNext() && markDTOsIter.hasNext()) {
            Mark nextMark = marksIter.next();
            MarkDTO nextMarkDTO = markDTOsIter.next();
            if (!nextMark.getId().toString().equals(nextMarkDTO.getIdAsString())) {
                return false;
            }
        }
        return marksIter.hasNext() == markDTOsIter.hasNext();
    }

    private List<PositionDTO> getMarkPositionDTOs(
            TimePoint timePoint, TrackedRace trackedRace, Waypoint waypoint) {
        List<PositionDTO> markPositionDTOs = new ArrayList<PositionDTO>();
        for (Mark startMark : waypoint.getMarks()) {
            final Position estimatedMarkPosition = trackedRace.getOrCreateTrack(startMark)
                    .getEstimatedPosition(timePoint, /* extrapolate */false);
            if (estimatedMarkPosition != null) {
                markPositionDTOs.add(new PositionDTO(estimatedMarkPosition.getLatDeg(), estimatedMarkPosition.getLngDeg()));
            }
        }
        return markPositionDTOs;
    }

    private List<QuickRankDTO> getQuickRanks(RegattaAndRaceIdentifier raceIdentifier, Date date) throws NoWindException {
        List<QuickRankDTO> result = new ArrayList<QuickRankDTO>();
        if (date != null) {
            TimePoint dateAsTimePoint = new MillisecondsTimePoint(date);
            TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
            if (trackedRace != null) {
                RaceDefinition race = trackedRace.getRace();
                for (Competitor competitor : race.getCompetitors()) {
                    int rank = trackedRace.getRank(competitor, dateAsTimePoint);
                    TrackedLegOfCompetitor trackedLeg = trackedRace.getTrackedLeg(competitor, dateAsTimePoint);
                    if (trackedLeg != null) {
                        int legNumber = race.getCourse().getLegs().indexOf(trackedLeg.getLeg()) + 1;
                        QuickRankDTO quickRankDTO = new QuickRankDTO(baseDomainFactory.convertToCompetitorDTO(competitor), rank, legNumber);
                        result.add(quickRankDTO);
                    }
                }
                Collections.sort(result, new Comparator<QuickRankDTO>() {
                    @Override
                    public int compare(QuickRankDTO o1, QuickRankDTO o2) {
                        return o1.rank - o2.rank;
                    }
                });
            }
        }
        return result;
    }

    @Override
    public void setRaceIsKnownToStartUpwind(RegattaAndRaceIdentifier raceIdentifier, boolean raceIsKnownToStartUpwind) {
        getService().apply(new SetRaceIsKnownToStartUpwind(raceIdentifier, raceIsKnownToStartUpwind));
    }

    @Override
    public void setWindSourcesToExclude(RegattaAndRaceIdentifier raceIdentifier, Iterable<WindSource> windSourcesToExclude) {
        getService().apply(new SetWindSourcesToExclude(raceIdentifier, windSourcesToExclude));
    }

    @Override
    public WindInfoForRaceDTO getWindSourcesInfo(RegattaAndRaceIdentifier raceIdentifier) {
        WindInfoForRaceDTO result = null;
        TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
        if (trackedRace != null) {
            result = new WindInfoForRaceDTO();
            result.raceIsKnownToStartUpwind = trackedRace.raceIsKnownToStartUpwind();
            List<WindSource> windSourcesToExclude = new ArrayList<WindSource>();
            for (WindSource windSourceToExclude : trackedRace.getWindSourcesToExclude()) {
                windSourcesToExclude.add(windSourceToExclude);
            }
            result.windSourcesToExclude = windSourcesToExclude;
            Map<WindSource, WindTrackInfoDTO> windTrackInfoDTOs = new HashMap<WindSource, WindTrackInfoDTO>();
            result.windTrackInfoByWindSource = windTrackInfoDTOs;

            for(WindSource windSource: trackedRace.getWindSources()) {
                windTrackInfoDTOs.put(windSource, new WindTrackInfoDTO());
            }
            windTrackInfoDTOs.put(new WindSourceImpl(WindSourceType.COMBINED), new WindTrackInfoDTO());
        }
        return result;
    }

    @Override
    public void removeWind(RegattaAndRaceIdentifier raceIdentifier, WindDTO windDTO) {
        DynamicTrackedRace trackedRace = (DynamicTrackedRace) getExistingTrackedRace(raceIdentifier);
        if (trackedRace != null) {
            Position p = null;
            if (windDTO.position != null) {
                p = new DegreePosition(windDTO.position.latDeg, windDTO.position.lngDeg);
            }
            TimePoint at = null;
            if (windDTO.measureTimepoint != null) {
                at = new MillisecondsTimePoint(windDTO.measureTimepoint);
            }
            SpeedWithBearing speedWithBearing = null;
            Speed speed = null;
            if (windDTO.trueWindSpeedInKnots != null) {
                speed = new KnotSpeedImpl(windDTO.trueWindSpeedInKnots);
            } else if (windDTO.trueWindSpeedInMetersPerSecond != null) {
                speed = new KilometersPerHourSpeedImpl(windDTO.trueWindSpeedInMetersPerSecond * 3600. / 1000.);
            } else if (windDTO.dampenedTrueWindSpeedInKnots != null) {
                speed = new KnotSpeedImpl(windDTO.dampenedTrueWindSpeedInKnots);
            } else if (windDTO.dampenedTrueWindSpeedInMetersPerSecond != null) {
                speed = new KilometersPerHourSpeedImpl(windDTO.dampenedTrueWindSpeedInMetersPerSecond * 3600. / 1000.);
            }
            if (speed != null) {
                if (windDTO.trueWindBearingDeg != null) {
                    speedWithBearing = new KnotSpeedWithBearingImpl(speed.getKnots(), new DegreeBearingImpl(
                            windDTO.trueWindBearingDeg));
                } else if (windDTO.trueWindFromDeg != null) {
                    speedWithBearing = new KnotSpeedWithBearingImpl(speed.getKnots(), new DegreeBearingImpl(
                            windDTO.trueWindFromDeg).reverse());
                }
            }
            Wind wind = new WindImpl(p, at, speedWithBearing);
            trackedRace.removeWind(wind, trackedRace.getWindSources(WindSourceType.WEB).iterator().next());
        }
    }

    protected RacingEventService getService() {
        return racingEventServiceTracker.getService(); // grab the service
    }

    private ReplicationService getReplicationService() {
        return replicationServiceTracker.getService();
    }
    
    @Override
    public List<String> getLeaderboardNames() {
        return new ArrayList<String>(getService().getLeaderboards().keySet());
    }

    @Override
    public Pair<String, LeaderboardType> checkLeaderboardName(String leaderboardName) {
        Pair<String, LeaderboardType> result = null;

        if(getService().getLeaderboards().containsKey(leaderboardName)) {
            Leaderboard leaderboard = getService().getLeaderboards().get(leaderboardName);
            boolean isMetaLeaderboard = leaderboard instanceof MetaLeaderboard ? true : false;
            boolean isRegattaLeaderboard = leaderboard instanceof RegattaLeaderboard ? true : false;
            LeaderboardType type;
            if(isMetaLeaderboard) {
                type = isRegattaLeaderboard ? LeaderboardType.RegattaMetaLeaderboard : LeaderboardType.FlexibleMetaLeaderboard;
            } else {
                type = isRegattaLeaderboard ? LeaderboardType.RegattaLeaderboard : LeaderboardType.FlexibleLeaderboard;
            }
            result = new Pair<String, LeaderboardType>(leaderboard.getName(), type);
        }
        
        return result;
    }

    @Override
    public StrippedLeaderboardDTO createFlexibleLeaderboard(String leaderboardName, String leaderboardDisplayName, int[] discardThresholds, ScoringSchemeType scoringSchemeType,
            UUID courseAreaId) {
        return createStrippedLeaderboardDTO(getService().apply(new CreateFlexibleLeaderboard(leaderboardName, leaderboardDisplayName, discardThresholds,
                baseDomainFactory.createScoringScheme(scoringSchemeType), courseAreaId)), false);
    }

    public StrippedLeaderboardDTO createRegattaLeaderboard(RegattaIdentifier regattaIdentifier, String leaderboardDisplayName, int[] discardThresholds) {
        return createStrippedLeaderboardDTO(getService().apply(new CreateRegattaLeaderboard(regattaIdentifier, leaderboardDisplayName, discardThresholds)), false);
    }

    @Override
    public List<StrippedLeaderboardDTO> getLeaderboards() {
        Map<String, Leaderboard> leaderboards = getService().getLeaderboards();
        List<StrippedLeaderboardDTO> results = new ArrayList<StrippedLeaderboardDTO>();
        for(Leaderboard leaderboard: leaderboards.values()) {
            StrippedLeaderboardDTO dao = createStrippedLeaderboardDTO(leaderboard, false);
            results.add(dao);
        }
        return results;
    }

    @Override
    public StrippedLeaderboardDTO getLeaderboard(String leaderboardName) {
        Map<String, Leaderboard> leaderboards = getService().getLeaderboards();
        StrippedLeaderboardDTO result = null;
        Leaderboard leaderboard = leaderboards.get(leaderboardName);
        if (leaderboard != null) {
            result = createStrippedLeaderboardDTO(leaderboard, false);
        }
        return result;
    }

    @Override
    public List<StrippedLeaderboardDTO> getLeaderboardsByEvent(EventDTO event) {
        List<StrippedLeaderboardDTO> results = new ArrayList<StrippedLeaderboardDTO>();
        if (event != null) {
            for (RegattaDTO regatta : event.regattas) {
                results.addAll(getLeaderboardsByRegatta(regatta));
            }
            HashSet<StrippedLeaderboardDTO> set = new HashSet<StrippedLeaderboardDTO>(results);
            results.clear();
            results.addAll(set);
        }
        return results;
    }

    @Override
    public List<StrippedLeaderboardDTO> getLeaderboardsByRegatta(RegattaDTO regatta) {
        List<StrippedLeaderboardDTO> results = new ArrayList<StrippedLeaderboardDTO>();
        if (regatta != null && regatta.races != null) {
            for (RaceDTO race : regatta.races) {
                List<StrippedLeaderboardDTO> leaderboard = getLeaderboardsByRaceAndRegatta(race, regatta.getRegattaIdentifier());
                if (leaderboard != null && !leaderboard.isEmpty()) {
                    results.addAll(leaderboard);
                }
            }
        }
        // Removing duplicates
        HashSet<StrippedLeaderboardDTO> set = new HashSet<StrippedLeaderboardDTO>(results);
        results.clear();
        results.addAll(set);
        return results;
    }

    @Override
    public List<StrippedLeaderboardDTO> getLeaderboardsByRaceAndRegatta(RaceDTO race, RegattaIdentifier regattaIdentifier) {
        List<StrippedLeaderboardDTO> results = new ArrayList<StrippedLeaderboardDTO>();
        Map<String, Leaderboard> leaderboards = getService().getLeaderboards();
        for (Leaderboard leaderboard : leaderboards.values()) {
            if (leaderboard instanceof RegattaLeaderboard && ((RegattaLeaderboard) leaderboard).getRegatta().getRegattaIdentifier().equals(regattaIdentifier)) {
                Iterable<RaceColumn> races = leaderboard.getRaceColumns();
                for (RaceColumn raceInLeaderboard : races) {
                    for (Fleet fleet : raceInLeaderboard.getFleets()) {
                        TrackedRace trackedRace = raceInLeaderboard.getTrackedRace(fleet);
                        if (trackedRace != null) {
                            RaceDefinition trackedRaceDef = trackedRace.getRace();
                            if (trackedRaceDef.getName().equals(race.getName())) {
                                results.add(createStrippedLeaderboardDTO(leaderboard, false));
                                break;
                            }
                        }
                    }
                }
            }
        }
        return results;
    }

    /**
     * Creates a {@link LeaderboardDTO} for <code>leaderboard</code> and fills in the name, race master data
     * in the form of {@link RaceColumnDTO}s, whether or not there are {@link LeaderboardDTO#hasCarriedPoints carried points}
     * and the {@link LeaderboardDTO#discardThresholds discarding thresholds} for the leaderboard. No data about the points
     * is filled into the result object. No data about the competitor display names is filled in; instead, an empty map
     * is used for {@link LeaderboardDTO#competitorDisplayNames}.<br />
     * If <code>withGeoLocationData</code> is <code>true</code> the geographical location of all races will be determined.
     */
    private StrippedLeaderboardDTO createStrippedLeaderboardDTO(Leaderboard leaderboard, boolean withGeoLocationData) {
        StrippedLeaderboardDTO leaderboardDTO = new StrippedLeaderboardDTO();
        TimePoint startOfLatestRace = null;
        Long delayToLiveInMillisForLatestRace = null;
        leaderboardDTO.name = leaderboard.getName();
        leaderboardDTO.displayName = leaderboard.getDisplayName();
        leaderboardDTO.competitorDisplayNames = new HashMap<CompetitorDTO, String>();
        if (leaderboard instanceof RegattaLeaderboard) {
            RegattaLeaderboard regattaLeaderboard = (RegattaLeaderboard) leaderboard;
            Regatta regatta = regattaLeaderboard.getRegatta();
            leaderboardDTO.regattaName = regatta.getName(); 
            leaderboardDTO.type = leaderboard instanceof MetaLeaderboard ? LeaderboardType.RegattaMetaLeaderboard : LeaderboardType.RegattaLeaderboard;
            leaderboardDTO.scoringScheme = regatta.getScoringScheme().getType();
        } else {
            leaderboardDTO.type = leaderboard instanceof MetaLeaderboard ? LeaderboardType.FlexibleMetaLeaderboard : LeaderboardType.FlexibleLeaderboard;
            leaderboardDTO.scoringScheme = leaderboard.getScoringScheme().getType();
        }
        if (leaderboard.getDefaultCourseArea() != null) {
            leaderboardDTO.defaultCourseAreaId = leaderboard.getDefaultCourseArea().getId();
            leaderboardDTO.defaultCourseAreaName = leaderboard.getDefaultCourseArea().getName();
        }
        leaderboardDTO.setDelayToLiveInMillisForLatestRace(delayToLiveInMillisForLatestRace);
        leaderboardDTO.hasCarriedPoints = leaderboard.hasCarriedPoints();
        if (leaderboard.getResultDiscardingRule() instanceof ThresholdBasedResultDiscardingRule) {
            leaderboardDTO.discardThresholds = ((ThresholdBasedResultDiscardingRule) leaderboard.getResultDiscardingRule()).getDiscardIndexResultsStartingWithHowManyRaces();
        } else {
            leaderboardDTO.discardThresholds = null;
        }
        for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
            for (Fleet fleet : raceColumn.getFleets()) {
                RaceDTO raceDTO = null;
                RegattaAndRaceIdentifier raceIdentifier = null;
                TrackedRace trackedRace = raceColumn.getTrackedRace(fleet);
                if (trackedRace != null) {
                    if (startOfLatestRace == null || (trackedRace.getStartOfRace() != null && trackedRace.getStartOfRace().compareTo(startOfLatestRace) > 0)) {
                        delayToLiveInMillisForLatestRace = trackedRace.getDelayToLiveInMillis();
                    }
                    raceIdentifier = new RegattaNameAndRaceName(trackedRace.getTrackedRegatta().getRegatta().getName(), trackedRace.getRace().getName());
                    raceDTO = baseDomainFactory.createRaceDTO(getService(), withGeoLocationData, raceIdentifier, trackedRace);
                }    
                final FleetDTO fleetDTO = baseDomainFactory.convertToFleetDTO(fleet);
                leaderboardDTO.addRace(raceColumn.getName(), raceColumn.getExplicitFactor(), raceColumn.getFactor(),
                        fleetDTO, raceColumn.isMedalRace(), raceIdentifier, raceDTO);
            }
        }
        return leaderboardDTO;
    }

    @Override
    public StrippedLeaderboardDTO updateLeaderboard(String leaderboardName, String newLeaderboardName, String newLeaderboardDisplayName, int[] newDiscardingThresholds, UUID newCourseAreaId) {
        Leaderboard updatedLeaderboard = getService().apply(new UpdateLeaderboard(leaderboardName, newLeaderboardName, newLeaderboardDisplayName, newDiscardingThresholds, newCourseAreaId));
        return createStrippedLeaderboardDTO(updatedLeaderboard, false);
    }
    
    @Override
    public void removeLeaderboards(Collection<String> leaderboardNames) {
        for (String leaderoardName : leaderboardNames) {
            removeLeaderboard(leaderoardName);
        }
    }

    @Override
    public void removeLeaderboard(String leaderboardName) {
        getService().apply(new RemoveLeaderboard(leaderboardName));
    }

    @Override
    public void renameLeaderboard(String leaderboardName, String newLeaderboardName) {
        getService().apply(new RenameLeaderboard(leaderboardName, newLeaderboardName));
    }

    @Override
    public void addColumnToLeaderboard(String columnName, String leaderboardName, boolean medalRace) {
        getService().apply(new AddColumnToLeaderboard(columnName, leaderboardName, medalRace));
    }

    @Override
    public void addColumnsToLeaderboard(String leaderboardName, List<Pair<String, Boolean>> columnsToAdd) {
        for(Pair<String, Boolean> columnToAdd: columnsToAdd) {
            getService().apply(new AddColumnToLeaderboard(columnToAdd.getA(), leaderboardName, columnToAdd.getB()));
        }
    }

    @Override
    public void removeLeaderboardColumns(String leaderboardName, List<String> columnsToRemove) {
        for (String columnToRemove : columnsToRemove) {
            getService().apply(new RemoveLeaderboardColumn(columnToRemove, leaderboardName));
        }
    }

    @Override
    public void removeLeaderboardColumn(String leaderboardName, String columnName) {
        getService().apply(new RemoveLeaderboardColumn(columnName, leaderboardName));
    }

    @Override
    public void renameLeaderboardColumn(String leaderboardName, String oldColumnName, String newColumnName) {
        getService().apply(new RenameLeaderboardColumn(leaderboardName, oldColumnName, newColumnName));
    }

    @Override
    public void updateLeaderboardColumnFactor(String leaderboardName, String columnName, Double newFactor) {
        getService().apply(new UpdateLeaderboardColumnFactor(leaderboardName, columnName, newFactor));
    }

    @Override
    public void suppressCompetitorInLeaderboard(String leaderboardName, String competitorIdAsString, boolean suppressed) {
        getService().apply(new SetSuppressedFlagForCompetitorInLeaderboard(leaderboardName, competitorIdAsString, suppressed));
    }

    @Override
    public boolean connectTrackedRaceToLeaderboardColumn(String leaderboardName, String raceColumnName,
            String fleetName, RegattaAndRaceIdentifier raceIdentifier) {
        return getService().apply(new ConnectTrackedRaceToLeaderboardColumn(leaderboardName, raceColumnName, fleetName, raceIdentifier));
    }

    @Override
    public Map<String, RegattaAndRaceIdentifier> getRegattaAndRaceNameOfTrackedRaceConnectedToLeaderboardColumn(String leaderboardName, String raceColumnName) {
        Map<String, RegattaAndRaceIdentifier> result = new HashMap<String, RegattaAndRaceIdentifier>();
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard != null) {
            RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
            if (raceColumn != null) {
                for (Fleet fleet : raceColumn.getFleets()) {
                    TrackedRace trackedRace = raceColumn.getTrackedRace(fleet);
                    if (trackedRace != null) {
                        result.put(fleet.getName(), trackedRace.getRaceIdentifier());
                    } else {
                        result.put(fleet.getName(), null);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public void disconnectLeaderboardColumnFromTrackedRace(String leaderboardName, String raceColumnName, String fleetName) {
        getService().apply(new DisconnectLeaderboardColumnFromTrackedRace(leaderboardName, raceColumnName, fleetName));
    }

    @Override
    public void updateLeaderboardCarryValue(String leaderboardName, String competitorIdAsString, Double carriedPoints) {
        getService().apply(new UpdateLeaderboardCarryValue(leaderboardName, competitorIdAsString, carriedPoints));
    }

    @Override
    public Triple<Double, Double, Boolean> updateLeaderboardMaxPointsReason(String leaderboardName, String competitorIdAsString, String raceColumnName,
            MaxPointsReason maxPointsReason, Date date) throws NoWindException {
        return getService().apply(
                new UpdateLeaderboardMaxPointsReason(leaderboardName, raceColumnName, competitorIdAsString,
                        maxPointsReason, new MillisecondsTimePoint(date)));
    }

    @Override
    public Triple<Double, Double, Boolean> updateLeaderboardScoreCorrection(String leaderboardName,
            String competitorIdAsString, String columnName, Double correctedScore, Date date) throws NoWindException {
        return getService().apply(
                new UpdateLeaderboardScoreCorrection(leaderboardName, columnName, competitorIdAsString, correctedScore,
                        new MillisecondsTimePoint(date)));
    }

    @Override
    public Void updateLeaderboardScoreCorrectionMetadata(String leaderboardName, Date timePointOfLastCorrectionValidity, String comment) {
        return getService().apply(
                new UpdateLeaderboardScoreCorrectionMetadata(leaderboardName,
                        timePointOfLastCorrectionValidity == null ? null : new MillisecondsTimePoint(timePointOfLastCorrectionValidity),
                                comment));
    }

    @Override
    public void updateLeaderboardScoreCorrectionsAndMaxPointsReasons(BulkScoreCorrectionDTO updates) throws NoWindException {
        Date dateForResults = new Date(); // we don't care about the result date/time here; use current date as default
        for (Map.Entry<String, Map<String, Double>> e : updates.getScoreUpdatesForRaceColumnByCompetitorIdAsString().entrySet()) {
            for (Map.Entry<String, Double> raceColumnNameAndCorrectedScore : e.getValue().entrySet()) {
                updateLeaderboardScoreCorrection(updates.getLeaderboardName(), e.getKey(),
                        raceColumnNameAndCorrectedScore.getKey(), raceColumnNameAndCorrectedScore.getValue(), dateForResults);
            }
        }
        for (Map.Entry<String, Map<String, MaxPointsReason>> e : updates.getMaxPointsUpdatesForRaceColumnByCompetitorIdAsString().entrySet()) {
            for (Map.Entry<String, MaxPointsReason> raceColumnNameAndNewMaxPointsReason : e.getValue().entrySet()) {
                updateLeaderboardMaxPointsReason(updates.getLeaderboardName(), e.getKey(),
                        raceColumnNameAndNewMaxPointsReason.getKey(), raceColumnNameAndNewMaxPointsReason.getValue(), dateForResults);
            }
        }
    }

    @Override
    public void updateCompetitorDisplayNameInLeaderboard(String leaderboardName, String competitorIdAsString, String displayName) {
        getService().apply(new UpdateCompetitorDisplayNameInLeaderboard(leaderboardName, competitorIdAsString, displayName));
    }

    @Override
    public void moveLeaderboardColumnUp(String leaderboardName, String columnName) {
        getService().apply(new MoveLeaderboardColumnUp(leaderboardName, columnName));
    }

    @Override
    public void moveLeaderboardColumnDown(String leaderboardName, String columnName) {
        getService().apply(new MoveLeaderboardColumnDown(leaderboardName, columnName));
    }

    @Override
    public void updateIsMedalRace(String leaderboardName, String columnName, boolean isMedalRace) {
        getService().apply(new UpdateIsMedalRace(leaderboardName, columnName, isMedalRace));
    }

    @Override
    public void updateRaceDelayToLive(RegattaAndRaceIdentifier regattaAndRaceIdentifier, long delayToLiveInMs) {
        getService().apply(new UpdateRaceDelayToLive(regattaAndRaceIdentifier, delayToLiveInMs));
    }

    @Override
    public void updateRacesDelayToLive(List<RegattaAndRaceIdentifier> regattaAndRaceIdentifiers, long delayToLiveInMs) {
        for (RegattaAndRaceIdentifier regattaAndRaceIdentifier : regattaAndRaceIdentifiers) {
            getService().apply(new UpdateRaceDelayToLive(regattaAndRaceIdentifier, delayToLiveInMs));
        }
    }

    @Override
    public List<SwissTimingConfigurationDTO> getPreviousSwissTimingConfigurations() {
        Iterable<SwissTimingConfiguration> configs = swissTimingAdapterPersistence.getSwissTimingConfigurations();
        List<SwissTimingConfigurationDTO> result = new ArrayList<SwissTimingConfigurationDTO>();
        for (SwissTimingConfiguration stConfig : configs) {
            result.add(new SwissTimingConfigurationDTO(stConfig.getName(), stConfig.getHostname(), stConfig.getPort(), stConfig.canSendRequests()));
        }
        return result;
    }

    @Override
    public List<SwissTimingRaceRecordDTO> listSwissTimingRaces(String hostname, int port, boolean canSendRequests) 
            throws UnknownHostException, IOException, InterruptedException, ParseException {
        List<SwissTimingRaceRecordDTO> result = new ArrayList<SwissTimingRaceRecordDTO>();
        for (com.sap.sailing.domain.swisstimingadapter.RaceRecord rr : getSwissTimingAdapter().getSwissTimingRaceRecords(hostname, port, canSendRequests)) {
            SwissTimingRaceRecordDTO swissTimingRaceRecordDTO = new SwissTimingRaceRecordDTO(rr.getRaceID(), rr.getDescription(), rr.getStartTime());
            BoatClass boatClass = getSwissTimingAdapter().getSwissTimingDomainFactory().getRaceTypeFromRaceID(rr.getRaceID()).getBoatClass();
            swissTimingRaceRecordDTO.boatClass = boatClass != null ? boatClass.getName() : null;
            swissTimingRaceRecordDTO.discipline = rr.getRaceID().length() >= 3 ? rr.getRaceID().substring(2, 3) : null;
            swissTimingRaceRecordDTO.hasCourse = rr.hasCourse();
            swissTimingRaceRecordDTO.hasStartlist = rr.hasStartlist();
            result.add(swissTimingRaceRecordDTO);
        }
        return result;
    }

    @Override
    public void storeSwissTimingConfiguration(String configName, String hostname, int port, boolean canSendRequests) {
        swissTimingAdapterPersistence.storeSwissTimingConfiguration(swissTimingFactory.createSwissTimingConfiguration(configName, hostname, port, canSendRequests));
    }

    @Override
    public void trackWithSwissTiming(RegattaIdentifier regattaToAddTo, Iterable<SwissTimingRaceRecordDTO> rrs, String hostname, int port,
            boolean canSendRequests, boolean trackWind, final boolean correctWindByDeclination) throws Exception {
        logger.info("tracWithSwissTiming for regatta " + regattaToAddTo + " for race records " + rrs
                + " with hostname " + hostname + " and port " + port + " and canSendRequests=" + canSendRequests);
        for (SwissTimingRaceRecordDTO rr : rrs) {
            final RacesHandle raceHandle = getSwissTimingAdapter().addSwissTimingRace(getService(), regattaToAddTo, rr.ID, hostname,
                    port,
                    canSendRequests,
                    MongoRaceLogStoreFactory.INSTANCE.getMongoRaceLogStore(mongoObjectFactory, domainObjectFactory), RaceTracker.TIMEOUT_FOR_RECEIVING_RACE_DEFINITION_IN_MILLISECONDS);
            if (trackWind) {
                new Thread("Wind tracking starter for race " + rr.ID + "/" + rr.description) {
                    public void run() {
                        try {
                            startTrackingWind(raceHandle, correctWindByDeclination,
                                    RaceTracker.TIMEOUT_FOR_RECEIVING_RACE_DEFINITION_IN_MILLISECONDS);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }.start();
            }
        }
    }

    @Override
    public void sendSwissTimingDummyRace(String racMessage, String stlMesssage, String ccgMessage) {
        getSwissTimingAdapter().storeSwissTimingDummyRace(racMessage,stlMesssage,ccgMessage);
    }

    @Override
    public List<SwissTimingReplayRaceDTO> listSwissTiminigReplayRaces(String swissTimingUrl) {
        List<SwissTimingReplayRace> replayRaces = getSwissTimingReplayService().listReplayRaces(swissTimingUrl);
        List<SwissTimingReplayRaceDTO> result = new ArrayList<SwissTimingReplayRaceDTO>(replayRaces.size()); 
        for (SwissTimingReplayRace replayRace : replayRaces) {
            result.add(new SwissTimingReplayRaceDTO(replayRace.getFlightNumber(), replayRace.getRaceId(), replayRace.getRsc(), replayRace.getName(), replayRace.getBoatClass(), replayRace.getStartTime(), replayRace.getLink()));
        }
        return result;
    }

    @Override
    public void replaySwissTimingRace(RegattaIdentifier regattaIdentifier, Iterable<SwissTimingReplayRaceDTO> replayRaceDTOs,
            boolean trackWind, boolean correctWindByDeclination, boolean simulateWithStartTimeNow) {
        logger.info("replaySwissTimingRace for regatta "+regattaIdentifier+" for races "+replayRaceDTOs);
        Regatta regatta;
        for (SwissTimingReplayRaceDTO replayRaceDTO : replayRaceDTOs) {
            try {
                if (regattaIdentifier == null) {
                    String boatClass = replayRaceDTO.boat_class;
                    for (String genderIndicator : new String[] { "Man", "Woman", "Men", "Women", "M", "W" }) {
                        Pattern p = Pattern.compile("(( - )|-| )" + genderIndicator + "$");
                        Matcher m = p.matcher(boatClass.trim());
                        if (m.find()) {
                            boatClass = boatClass.trim().substring(0, m.start(1));
                            break;
                        }
                    }
                    regatta = getService().createRegatta(
                            replayRaceDTO.rsc,
                            boatClass.trim(),
                            RegattaImpl.getDefaultName(replayRaceDTO.rsc, replayRaceDTO.boat_class),
                            Collections.singletonList(new SeriesImpl(LeaderboardNameConstants.DEFAULT_SERIES_NAME,
                            /* isMedal */false, Collections.singletonList(new FleetImpl(
                                    LeaderboardNameConstants.DEFAULT_FLEET_NAME)),
                            /* race column names */new ArrayList<String>(), getService())), false,
                            baseDomainFactory.createScoringScheme(ScoringSchemeType.LOW_POINT), null);
                    // TODO: is course area relevant for swiss timing replay?
                } else {
                    regatta = getService().getRegatta(regattaIdentifier);
                }
                getSwissTimingReplayService().loadRaceData(replayRaceDTO.link, regatta, getService());
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error trying to load SwissTimingReplay race " + replayRaceDTO, e);
            }
        }
    }

    @Override
    public String[] getCountryCodes() {
        List<String> countryCodes = new ArrayList<String>();
        for (CountryCode cc : countryCodeFactory.getAll()) {
            if (cc.getThreeLetterIOCCode() != null && !cc.getThreeLetterIOCCode().equals("")) {
                countryCodes.add(cc.getThreeLetterIOCCode());
            }
        }
        Collections.sort(countryCodes);
        return countryCodes.toArray(new String[0]);
    }

    /**
     * Finds a competitor in a sequence of competitors that has an {@link Competitor#getId()} equal to <code>id</code>. 
     */
    private Competitor getCompetitorByIdAsString(Iterable<Competitor> competitors, String idAsString) {
        for (Competitor c : competitors) {
            if (c.getId().toString().equals(idAsString)) {
                return c;
            }
        }
        return null;
    }

    private Double getCompetitorRaceDataEntry(DetailType dataType, TrackedRace trackedRace, Competitor competitor,
            TimePoint timePoint, String leaderboardGroupName, String leaderboardName) throws NoWindException {
        Double result = null;
        Course course = trackedRace.getRace().getCourse();
        course.lockForRead(); // make sure the tracked leg survives this call even if a course update is pending
        try {
            TrackedLegOfCompetitor trackedLeg = trackedRace.getTrackedLeg(competitor, timePoint);
            switch (dataType) {
            case CURRENT_SPEED_OVER_GROUND_IN_KNOTS:
                final GPSFixTrack<Competitor, GPSFixMoving> track = trackedRace.getTrack(competitor);
                if (track != null) {
                    SpeedWithBearing speedOverGround = track.getEstimatedSpeed(timePoint);
                    result = (speedOverGround == null) ? null : speedOverGround.getKnots();
                }
                break;
            case VELOCITY_MADE_GOOD_IN_KNOTS:
                if (trackedLeg != null) {
                    Speed velocityMadeGood = trackedLeg.getVelocityMadeGood(timePoint);
                    result = (velocityMadeGood == null) ? null : velocityMadeGood.getKnots();
                }
                break;
            case DISTANCE_TRAVELED:
                if (trackedLeg != null) {
                    Distance distanceTraveled = trackedRace.getDistanceTraveled(competitor, timePoint);
                    result = distanceTraveled == null ? null : distanceTraveled.getMeters();
                }
                break;
            case GAP_TO_LEADER_IN_SECONDS:
                if (trackedLeg != null) {
                    result = trackedLeg.getGapToLeaderInSeconds(timePoint);
                }
                break;
            case WINDWARD_DISTANCE_TO_OVERALL_LEADER:
                if (trackedLeg != null) {
                    Distance distanceToLeader = trackedLeg.getWindwardDistanceToOverallLeader(timePoint);
                    result = (distanceToLeader == null) ? null : distanceToLeader.getMeters();
                }
                break;
            case RACE_RANK:
                if (trackedLeg != null) {
                    result = (double) trackedLeg.getRank(timePoint);
                }
                break;
            case REGATTA_RANK:
                if (leaderboardName == null || leaderboardName.isEmpty()) {
                    break;
                }
                Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
                result = leaderboard == null ? null : (double) leaderboard.getTotalRankOfCompetitor(competitor,
                        timePoint);
                break;
            case OVERALL_RANK:
                if (leaderboardGroupName == null || leaderboardGroupName.isEmpty()) {
                    break;
                }
                LeaderboardGroup group = getService().getLeaderboardGroupByName(leaderboardGroupName);
                Leaderboard overall = group.getOverallLeaderboard();
                result = overall == null ? null : (double) overall.getTotalRankOfCompetitor(competitor, timePoint);
                break;
            case DISTANCE_TO_START_LINE:
                TimePoint startOfRace = trackedRace.getStartOfRace();
                if (startOfRace == null || timePoint.before(startOfRace) || timePoint.equals(startOfRace)) {
                    Distance distanceToStartLine = trackedRace.getDistanceToStartLine(competitor, timePoint);
                    result = distanceToStartLine == null ? null : distanceToStartLine.getMeters();
                }
                break;
            case BEAT_ANGLE:
                if (trackedLeg != null) {
                    Bearing beatAngle = trackedLeg.getBeatAngle(timePoint);
                    result = beatAngle == null ? null : Math.abs(beatAngle.getDegrees());
                }
                break;
            default:
                throw new UnsupportedOperationException("Theres currently no support for the enum value '" + dataType
                        + "' in this method.");
            }
            return result;
        } finally {
            course.unlockAfterRead();
        }
    }

    @Override
    public CompetitorsRaceDataDTO getCompetitorsRaceData(RegattaAndRaceIdentifier race, List<CompetitorDTO> competitors, Date from, Date to,
            final long stepSizeInMs, final DetailType detailType, final String leaderboardGroupName, final String leaderboardName) throws NoWindException {
        CompetitorsRaceDataDTO result = null;
        final TrackedRace trackedRace = getExistingTrackedRace(race);
        if (trackedRace != null) {
            TimePoint newestEvent = trackedRace.getTimePointOfNewestEvent();
            final TimePoint startTime = from == null ? trackedRace.getStartOfTracking() : new MillisecondsTimePoint(from);
            final TimePoint endTime = (to == null || to.after(newestEvent.asDate())) ? newestEvent : new MillisecondsTimePoint(to);
            result = new CompetitorsRaceDataDTO(detailType, startTime==null?null:startTime.asDate(), endTime==null?null:endTime.asDate());

            Map<CompetitorDTO, FutureTask<CompetitorRaceDataDTO>> resultFutures = new HashMap<CompetitorDTO, FutureTask<CompetitorRaceDataDTO>>();
            for (final CompetitorDTO competitorDTO : competitors) {
                FutureTask<CompetitorRaceDataDTO> future = new FutureTask<CompetitorRaceDataDTO>(new Callable<CompetitorRaceDataDTO>() {
                    @Override
                            public CompetitorRaceDataDTO call() throws NoWindException {
                                Competitor competitor = getCompetitorByIdAsString(trackedRace.getRace().getCompetitors(),
                                        competitorDTO.getIdAsString());
                                ArrayList<Triple<String, Date, Double>> markPassingsData = new ArrayList<Triple<String, Date, Double>>();
                                ArrayList<Pair<Date, Double>> raceData = new ArrayList<Pair<Date, Double>>();
                                // Filling the mark passings
                                Set<MarkPassing> competitorMarkPassings = trackedRace.getMarkPassings(competitor);
                                if (competitorMarkPassings != null) {
                                    trackedRace.lockForRead(competitorMarkPassings);
                                    try {
                                        for (MarkPassing markPassing : competitorMarkPassings) {
                                            MillisecondsTimePoint time = new MillisecondsTimePoint(markPassing.getTimePoint().asMillis());
                                            Double competitorMarkPassingsData = getCompetitorRaceDataEntry(detailType,
                                                    trackedRace, competitor, time, leaderboardGroupName, leaderboardName);
                                            if (competitorMarkPassingsData != null) {
                                                markPassingsData.add(new Triple<String, Date, Double>(markPassing
                                                        .getWaypoint().getName(), time.asDate(), competitorMarkPassingsData));
                                            }
                                        }
                                    } finally {
                                        trackedRace.unlockAfterRead(competitorMarkPassings);
                                    }
                                }
                                if (startTime != null && endTime != null) {
                                    for (long i = startTime.asMillis(); i <= endTime.asMillis(); i += stepSizeInMs) {
                                        MillisecondsTimePoint time = new MillisecondsTimePoint(i);
                                        Double competitorRaceData = getCompetitorRaceDataEntry(detailType, trackedRace,
                                                competitor, time, leaderboardGroupName, leaderboardName);
                                        if (competitorRaceData != null) {
                                            raceData.add(new Pair<Date, Double>(time.asDate(), competitorRaceData));
                                        }
                                    }
                                }
                                return new CompetitorRaceDataDTO(competitorDTO, detailType, markPassingsData, raceData);
                            }
                        });
                resultFutures.put(competitorDTO, future);
                executor.execute(future);
            }
            for (Map.Entry<CompetitorDTO, FutureTask<CompetitorRaceDataDTO>> e : resultFutures.entrySet()) {
                CompetitorRaceDataDTO competitorData;
                try {
                    competitorData = e.getValue().get();
                } catch (InterruptedException e1) {
                    competitorData = null;
                    logger.log(Level.SEVERE, "Exception while trying to compute competitor data "+detailType+" for competitor "+e.getKey().getName(), e1);
                } catch (ExecutionException e1) {
                    competitorData = null;
                    logger.log(Level.SEVERE, "Exception while trying to compute competitor data "+detailType+" for competitor "+e.getKey().getName(), e1);
                }
                result.setCompetitorData(e.getKey(), competitorData);
            }
        }
        return result;
    }

    private Triple<String, List<CompetitorDTO>, List<Double>> getLeaderboardDataEntriesForRaceColumn(DetailType detailType,
            LeaderboardDTO leaderboard, RaceColumnDTO raceColumn) throws NoWindException {
        List<Double> values = new ArrayList<Double>();
        List<CompetitorDTO> competitorDTOs = new ArrayList<CompetitorDTO>();
        if(detailType != null) {
            switch (detailType) {
            case REGATTA_TOTAL_POINTS:
                for (Entry<CompetitorDTO, LeaderboardRowDTO> entry : leaderboard.rows.entrySet()) {
                    LeaderboardEntryDTO leaderboardEntryDTO = entry.getValue().fieldsByRaceColumnName.get(raceColumn.getName());
                    values.add(leaderboardEntryDTO != null ? leaderboardEntryDTO.totalPoints : null);
                    competitorDTOs.add(entry.getKey());
                }
                
                break;
            case REGATTA_RANK:
            case OVERALL_RANK:
                List<CompetitorDTO> competitorsFromBestToWorst = leaderboard.getCompetitorsFromBestToWorst(raceColumn);
                int rank = 1;
                for(CompetitorDTO competitor: competitorsFromBestToWorst) {
                    values.add(new Double(rank));
                    competitorDTOs.add(competitor);
                    rank++;
                }
                break;
            default:
                break;
            }
        }
        return new Triple<String, List<CompetitorDTO>, List<Double>>(raceColumn.getName(), competitorDTOs, values);
    }

    @Override
    public List<Triple<String, List<CompetitorDTO>, List<Double>>> getLeaderboardDataEntriesForAllRaceColumns(String leaderboardName, 
            Date date, DetailType detailType) throws Exception {
        List<Triple<String, List<CompetitorDTO>, List<Double>>> result = new ArrayList<Util.Triple<String,List<CompetitorDTO>,List<Double>>>();

        // Attention: The reason why we read the data from the LeaderboardDTO and not from the leaderboard directly is to ensure
        // the use of the leaderboard cache and the reuse of all the logic in the getLeaderboardByName method
        final Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard != null) {
            TimePoint timePoint;
            if (date == null) {
                timePoint = null;
            } else {
                timePoint = new MillisecondsTimePoint(date);
            }
            LeaderboardDTO leaderboardDTO = leaderboard.getLeaderboardDTO(timePoint, Collections.<String> emptyList(),
                    getService(), baseDomainFactory);
            for (RaceColumnDTO raceColumnDTO : leaderboardDTO.getRaceList()) {
                result.add(getLeaderboardDataEntriesForRaceColumn(detailType, leaderboardDTO, raceColumnDTO));
            }
        }
        return result;
    }

    @Override
    public List<Pair<String, String>> getLeaderboardsNamesOfMetaleaderboard(String metaLeaderboardName) {
        Leaderboard leaderboard = getService().getLeaderboardByName(metaLeaderboardName);
        if (leaderboard == null) {
            throw new IllegalArgumentException("Couldn't find leaderboard named "+metaLeaderboardName);
        }
        if(!(leaderboard instanceof MetaLeaderboard)) {
            throw new IllegalArgumentException("The leaderboard "+metaLeaderboardName + " is not a metaleaderboard");
        }
        List<Pair<String, String>> result = new ArrayList<Pair<String, String>>();
        MetaLeaderboard metaLeaderboard = (MetaLeaderboard) leaderboard;
        for(Leaderboard containedLeaderboard: metaLeaderboard.getLeaderboards()) {
            result.add(new Pair<String, String>(containedLeaderboard.getName(),
                    containedLeaderboard.getDisplayName() != null ? containedLeaderboard.getDisplayName() : containedLeaderboard.getName()));
        }
        return result;
    }

    @Override
    public Map<CompetitorDTO, List<GPSFixDTO>> getDouglasPoints(RegattaAndRaceIdentifier raceIdentifier,
            Map<CompetitorDTO, Date> from, Map<CompetitorDTO, Date> to,
            double meters) throws NoWindException {
        Map<CompetitorDTO, List<GPSFixDTO>> result = new HashMap<CompetitorDTO, List<GPSFixDTO>>();
        TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
        if (trackedRace != null) {
            MeterDistance maxDistance = new MeterDistance(meters);
            for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
                CompetitorDTO competitorDTO = baseDomainFactory.convertToCompetitorDTO(competitor);
                if (from.containsKey(competitorDTO)) {
                    // get Track of competitor
                    GPSFixTrack<Competitor, GPSFixMoving> gpsFixTrack = trackedRace.getTrack(competitor);
                    // Distance for DouglasPeucker
                    TimePoint timePointFrom = new MillisecondsTimePoint(from.get(competitorDTO));
                    TimePoint timePointTo = new MillisecondsTimePoint(to.get(competitorDTO));
                    List<GPSFixMoving> gpsFixApproximation = trackedRace.approximate(competitor, maxDistance,
                            timePointFrom, timePointTo);
                    List<GPSFixDTO> gpsFixDouglasList = new ArrayList<GPSFixDTO>();
                    for (int i = 0; i < gpsFixApproximation.size(); i++) {
                        GPSFix fix = gpsFixApproximation.get(i);
                        SpeedWithBearing speedWithBearing;
                        if (i < gpsFixApproximation.size() - 1) {
                            GPSFix next = gpsFixApproximation.get(i + 1);
                            Bearing bearing = fix.getPosition().getBearingGreatCircle(next.getPosition());
                            Speed speed = fix.getPosition().getDistance(next.getPosition())
                                    .inTime(next.getTimePoint().asMillis() - fix.getTimePoint().asMillis());
                            speedWithBearing = new KnotSpeedWithBearingImpl(speed.getKnots(), bearing);
                        } else {
                            speedWithBearing = gpsFixTrack.getEstimatedSpeed(fix.getTimePoint());
                        }
                        Tack tack = trackedRace.getTack(competitor, fix.getTimePoint());
                        TrackedLegOfCompetitor trackedLegOfCompetitor = trackedRace.getTrackedLeg(competitor, fix.getTimePoint());
                        LegType legType = trackedLegOfCompetitor == null ? null : trackedRace.getTrackedLeg(
                                trackedLegOfCompetitor.getLeg()).getLegType(fix.getTimePoint());
                        Wind wind = trackedRace.getWind(fix.getPosition(), fix.getTimePoint());
                        WindDTO windDTO = createWindDTOFromAlreadyAveraged(wind, fix.getTimePoint());
                        GPSFixDTO fixDTO = createGPSFixDTO(fix, speedWithBearing,  windDTO, tack, legType, /* extrapolated */false);
                        gpsFixDouglasList.add(fixDTO);
                    }
                    result.put(competitorDTO, gpsFixDouglasList);
                }
            }
        }
        return result;
    }

    @Override
    public Map<CompetitorDTO, List<ManeuverDTO>> getManeuvers(RegattaAndRaceIdentifier raceIdentifier,
            Map<CompetitorDTO, Date> from, Map<CompetitorDTO, Date> to) throws NoWindException {
        Map<CompetitorDTO, List<ManeuverDTO>> result = new HashMap<CompetitorDTO, List<ManeuverDTO>>();
        final TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
        if (trackedRace != null) {
            Map<CompetitorDTO, Future<List<ManeuverDTO>>> futures = new HashMap<CompetitorDTO, Future<List<ManeuverDTO>>>();
            for (final Competitor competitor : trackedRace.getRace().getCompetitors()) {
                CompetitorDTO competitorDTO = baseDomainFactory.convertToCompetitorDTO(competitor);
                if (from.containsKey(competitorDTO)) {
                    final TimePoint timePointFrom = new MillisecondsTimePoint(from.get(competitorDTO));
                    final TimePoint timePointTo = new MillisecondsTimePoint(to.get(competitorDTO));
                    RunnableFuture<List<ManeuverDTO>> future = new FutureTask<List<ManeuverDTO>>(
                            new Callable<List<ManeuverDTO>>() {
                                @Override
                                public List<ManeuverDTO> call() {
                                    List<Maneuver> maneuversForCompetitor;
                                    try {
                                        maneuversForCompetitor = trackedRace.getManeuvers(competitor, timePointFrom,
                                                timePointTo, /* waitForLatest */ true);
                                    } catch (NoWindException e) {
                                        throw new NoWindError(e);
                                    }
                                    return createManeuverDTOsForCompetitor(maneuversForCompetitor, trackedRace,
                                            competitor);
                                }
                            });
                    executor.execute(future);
                    futures.put(competitorDTO, future);
                }
            }
            for (Map.Entry<CompetitorDTO, Future<List<ManeuverDTO>>> competitorAndFuture : futures.entrySet()) {
                try {
                    result.put(competitorAndFuture.getKey(), competitorAndFuture.getValue().get());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return result;
    }

    private List<ManeuverDTO> createManeuverDTOsForCompetitor(List<Maneuver> maneuvers, TrackedRace trackedRace, Competitor competitor) {
        List<ManeuverDTO> result = new ArrayList<ManeuverDTO>();
        for (Maneuver maneuver : maneuvers) {
            final ManeuverDTO maneuverDTO;
            if (maneuver.getType() == ManeuverType.MARK_PASSING) {
                maneuverDTO = new MarkpassingManeuverDTO(maneuver.getType(), maneuver.getNewTack(),
                        new PositionDTO(maneuver.getPosition().getLatDeg(), maneuver.getPosition().getLngDeg()), 
                        maneuver.getTimePoint().asDate(),
                        createSpeedWithBearingDTO(maneuver.getSpeedWithBearingBefore()),
                        createSpeedWithBearingDTO(maneuver.getSpeedWithBearingAfter()),
                        maneuver.getDirectionChangeInDegrees(), maneuver.getManeuverLoss()==null?null:maneuver.getManeuverLoss().getMeters(),
                                ((MarkPassingManeuver) maneuver).getSide());
            } else  {
                maneuverDTO = new ManeuverDTO(maneuver.getType(), maneuver.getNewTack(),
                        new PositionDTO(maneuver.getPosition().getLatDeg(), maneuver.getPosition().getLngDeg()), 
                        maneuver.getTimePoint().asDate(),
                        createSpeedWithBearingDTO(maneuver.getSpeedWithBearingBefore()),
                        createSpeedWithBearingDTO(maneuver.getSpeedWithBearingAfter()),
                        maneuver.getDirectionChangeInDegrees(), maneuver.getManeuverLoss()==null?null:maneuver.getManeuverLoss().getMeters());
            }
            result.add(maneuverDTO);
        }
        return result;
    }

    @Override
    public RaceDefinition getRace(RegattaAndRaceIdentifier raceIdentifier) {
        Regatta regatta = getService().getRegattaByName(raceIdentifier.getRegattaName());
        RaceDefinition race = getRaceByName(regatta, raceIdentifier.getRaceName());
        return race;
    }

    @Override
    public DynamicTrackedRace getTrackedRace(RegattaAndRaceIdentifier regattaNameAndRaceName) {
        Regatta regatta = getService().getRegattaByName(regattaNameAndRaceName.getRegattaName());
        RaceDefinition race = getRaceByName(regatta, regattaNameAndRaceName.getRaceName());
        DynamicTrackedRace trackedRace = getService().getOrCreateTrackedRegatta(regatta).getTrackedRace(race);
        return trackedRace;
    }

    @Override
    public TrackedRace getExistingTrackedRace(RegattaAndRaceIdentifier regattaNameAndRaceName) {
        return getService().getExistingTrackedRace(regattaNameAndRaceName);
    }

    @Override
    public Regatta getRegatta(RegattaName regattaIdentifier) {
        return getService().getRegattaByName(regattaIdentifier.getRegattaName());
    }

    /**
     * Returns a servlet context that, when asked for a resource, first tries the original servlet context's implementation. If that
     * fails, it prepends "war/" to the request because the war/ folder contains all the resources exposed externally
     * through the HTTP server.
     */
    @Override
    public ServletContext getServletContext() {
        return new DelegatingServletContext(super.getServletContext());
    }

    @Override
    /**
     * Override of function to prevent exception "Blocked request without GWT permutation header (XSRF attack?)" when testing the GWT sites
     */
    protected void checkPermutationStrongName() throws SecurityException {
        //Override to prevent exception "Blocked request without GWT permutation header (XSRF attack?)" when testing the GWT sites
        return;
    }

    @Override
    public List<LeaderboardGroupDTO> getLeaderboardGroups(boolean withGeoLocationData) {
        ArrayList<LeaderboardGroupDTO> leaderboardGroupDTOs = new ArrayList<LeaderboardGroupDTO>();
        Map<String, LeaderboardGroup> leaderboardGroups = getService().getLeaderboardGroups();

        for (LeaderboardGroup leaderboardGroup : leaderboardGroups.values()) {
            leaderboardGroupDTOs.add(convertToLeaderboardGroupDTO(leaderboardGroup, withGeoLocationData));
        }

        return leaderboardGroupDTOs;
    }

    @Override
    public LeaderboardGroupDTO getLeaderboardGroupByName(String groupName, boolean withGeoLocationData) {
        return convertToLeaderboardGroupDTO(getService().getLeaderboardGroupByName(groupName), withGeoLocationData);
    }

    private LeaderboardGroupDTO convertToLeaderboardGroupDTO(LeaderboardGroup leaderboardGroup, boolean withGeoLocationData) {
        LeaderboardGroupDTO groupDTO = new LeaderboardGroupDTO();
        groupDTO.setName(leaderboardGroup.getName());
        groupDTO.description = leaderboardGroup.getDescription();
        groupDTO.displayLeaderboardsInReverseOrder = leaderboardGroup.isDisplayGroupsInReverseOrder();
        for (Leaderboard leaderboard : leaderboardGroup.getLeaderboards()) {
            groupDTO.leaderboards.add(createStrippedLeaderboardDTO(leaderboard, withGeoLocationData));
        }
        Leaderboard overallLeaderboard = leaderboardGroup.getOverallLeaderboard();
        if (overallLeaderboard != null) {
            if (overallLeaderboard.getResultDiscardingRule() instanceof ThresholdBasedResultDiscardingRule) {
                groupDTO.setOverallLeaderboardDiscardThresholds(((ThresholdBasedResultDiscardingRule) overallLeaderboard
                        .getResultDiscardingRule()).getDiscardIndexResultsStartingWithHowManyRaces());
            }
            groupDTO.setOverallLeaderboardScoringSchemeType(overallLeaderboard.getScoringScheme().getType());
        }
        return groupDTO;
    }


    @Override
    public void renameLeaderboardGroup(String oldName, String newName) {
        getService().apply(new RenameLeaderboardGroup(oldName, newName));
    }

    @Override
    public void removeLeaderboardGroups(Set<String> groupNames) {
        for (String groupName : groupNames) {
            removeLeaderboardGroup(groupName);
        }
    }

    private void removeLeaderboardGroup(String groupName) {
        getService().apply(new RemoveLeaderboardGroup(groupName));
    }

    @Override
    public LeaderboardGroupDTO createLeaderboardGroup(String groupName, String description, boolean displayGroupsInReverseOrder,
            int[] overallLeaderboardDiscardThresholds, ScoringSchemeType overallLeaderboardScoringSchemeType) {
        CreateLeaderboardGroup createLeaderboardGroupOp = new CreateLeaderboardGroup(groupName, description, displayGroupsInReverseOrder,
                new ArrayList<String>(), overallLeaderboardDiscardThresholds, overallLeaderboardScoringSchemeType);
        return convertToLeaderboardGroupDTO(getService().apply(createLeaderboardGroupOp), false);
    }

    @Override
    public void updateLeaderboardGroup(String oldName, String newName, String description, List<String> leaderboardNames,
            int[] overallLeaderboardDiscardThresholds, ScoringSchemeType overallLeaderboardScoringSchemeType) {
        getService().apply(
                new UpdateLeaderboardGroup(oldName, newName, description, leaderboardNames,
                        overallLeaderboardDiscardThresholds, overallLeaderboardScoringSchemeType));
    }

    @Override
    public ReplicationStateDTO getReplicaInfo() {
        ReplicationService service = getReplicationService();
        Set<ReplicaDTO> replicaDTOs = new HashSet<ReplicaDTO>();
        for (ReplicaDescriptor replicaDescriptor : service.getReplicaInfo()) {
            final Map<Class<? extends RacingEventServiceOperation<?>>, Integer> statistics = service.getStatistics(replicaDescriptor);
            Map<String, Integer> replicationCountByOperationClassName = new HashMap<String, Integer>();
            for (Map.Entry<Class<? extends RacingEventServiceOperation<?>>, Integer> e : statistics.entrySet()) {
                replicationCountByOperationClassName.put(e.getKey().getName(), e.getValue());
            }
            replicaDTOs.add(new ReplicaDTO(replicaDescriptor.getIpAddress().getHostName(), replicaDescriptor.getRegistrationTime().asDate(), replicaDescriptor.getUuid().toString(),
                    replicationCountByOperationClassName));
        }
        ReplicationMasterDTO master;
        ReplicationMasterDescriptor replicatingFromMaster = service.isReplicatingFromMaster();
        if (replicatingFromMaster == null) {
            master = null;
        } else {
            master = new ReplicationMasterDTO(replicatingFromMaster.getHostname(), replicatingFromMaster.getMessagingPort(),
                    replicatingFromMaster.getServletPort());
        }
        return new ReplicationStateDTO(master, replicaDTOs, service.getServerIdentifier().toString());
    }

    @Override
    public void startReplicatingFromMaster(String messagingHost, String masterHost, String exchangeName, int servletPort, int messagingPort) throws IOException, ClassNotFoundException, InterruptedException {
        // the queue name must be always the same for this server. in order to achieve
        // this we're using the unique server identifier
        getReplicationService().startToReplicateFrom(
                ReplicationFactory.INSTANCE.createReplicationMasterDescriptor(messagingHost, masterHost, exchangeName, servletPort, messagingPort, 
                        getReplicationService().getServerIdentifier().toString()));
    }

    @Override
    public List<EventDTO> getEvents() {
        List<EventDTO> result = new ArrayList<EventDTO>();
        for (Event event : getService().getAllEvents()) {
            EventDTO eventDTO = convertToEventDTO(event);
            result.add(eventDTO);
        }
        return result;
    }

    @Override
    public void updateEvent(String eventName, UUID eventId, VenueDTO venue, String publicationUrl, boolean isPublic, List<String> regattaNames) {
        getService().apply(new UpdateEvent(eventId, eventName, venue.getName(), publicationUrl, isPublic, regattaNames));
    }

    @Override
    public EventDTO createEvent(String eventName, String venue, String publicationUrl, boolean isPublic, List<String> courseAreaNames) {
        UUID eventUuid = UUID.randomUUID();
        getService().apply(new CreateEvent(eventName, venue, publicationUrl, isPublic, eventUuid));
        for (String courseAreaName : courseAreaNames) {
            createCourseArea(eventUuid, courseAreaName);
        }
        return getEventById(eventUuid);
    }

    @Override
    public CourseAreaDTO createCourseArea(UUID eventId, String courseAreaName) {
        CourseArea courseArea = getService().apply(new AddCourseArea(eventId, courseAreaName, UUID.randomUUID()));
        return convertToCourseAreaDTO(courseArea);
    }

    @Override
    public void removeEvents(Collection<UUID> eventIds) {
        for (UUID eventId : eventIds) {
            removeEvent(eventId);
        }
    }

    @Override
    public void removeEvent(UUID eventId) {
        getService().apply(new RemoveEvent(eventId));
    }

    @Override
    public void renameEvent(UUID eventId, String newName) {
        getService().apply(new RenameEvent(eventId, newName));
    }

    @Override
    public EventDTO getEventByName(String eventName) {
        EventDTO result = null;
        for (Event event : getService().getAllEvents()) {
            if(event.getName().equals(eventName)) {
                result = convertToEventDTO(event);
                break;
            }
        }
        return result;
    }

    @Override
    public EventDTO getEventById(UUID id) {
        EventDTO result = null;
        Event event = getService().getEvent(id);
        if (event != null) {
            result = convertToEventDTO(event);
        }
        return result;
    }

    private EventDTO convertToEventDTO(Event event) {
        EventDTO eventDTO = new EventDTO(event.getName());
        eventDTO.venue = new VenueDTO();
        eventDTO.venue.setName(event.getVenue() != null ? event.getVenue().getName() : null);
        eventDTO.publicationUrl = event.getPublicationUrl();
        eventDTO.isPublic = event.isPublic();
        eventDTO.id = event.getId();
        eventDTO.regattas = new ArrayList<RegattaDTO>();
        for (Regatta regatta: event.getRegattas()) {
            RegattaDTO regattaDTO = new RegattaDTO();
            regattaDTO.setName(regatta.getName());
            regattaDTO.races = convertToRaceDTOs(regatta);
            eventDTO.regattas.add(regattaDTO);
        }
        eventDTO.venue.setCourseAreas(new ArrayList<CourseAreaDTO>());
        for (CourseArea courseArea : event.getVenue().getCourseAreas()) {
            CourseAreaDTO courseAreaDTO = convertToCourseAreaDTO(courseArea);
            eventDTO.venue.getCourseAreas().add(courseAreaDTO);
        }
        return eventDTO;
    }

    private CourseAreaDTO convertToCourseAreaDTO(CourseArea courseArea) {
        CourseAreaDTO courseAreaDTO = new CourseAreaDTO(courseArea.getName());
        courseAreaDTO.id = courseArea.getId();
        return courseAreaDTO;
    }
    
    @Override
    public List<RaceGroupDTO> getRegattaStructureForEvent(UUID eventId) {
        List<RaceGroupDTO> raceGroups = new ArrayList<RaceGroupDTO>();
        Event event = getService().getEvent(eventId);
        if (event != null) {
            for (CourseArea courseArea : event.getVenue().getCourseAreas()) {
                for (Leaderboard leaderboard : getService().getLeaderboards().values()) {
                    if (leaderboard.getDefaultCourseArea() != null && leaderboard.getDefaultCourseArea() == courseArea) {
                        RaceGroupDTO raceGroup = new RaceGroupDTO(leaderboard.getName());
                        raceGroup.courseAreaIdAsString = courseArea.getId().toString();
                        raceGroup.displayName = getRegattaNameFromLeaderboard(leaderboard);
                        if (leaderboard instanceof RegattaLeaderboard) {
                            RegattaLeaderboard regattaLeaderboard = (RegattaLeaderboard) leaderboard;
                            for (Series series : regattaLeaderboard.getRegatta().getSeries()) {
                                RaceGroupSeriesDTO seriesDTO = new RaceGroupSeriesDTO(series.getName());
                                raceGroup.getSeries().add(seriesDTO);
                                for (Fleet fleet : series.getFleets()) {
                                    FleetDTO fleetDTO = new FleetDTO(fleet.getName(), fleet.getOrdering(), fleet.getColor());
                                    seriesDTO.getFleets().add(fleetDTO);
                                }
                            }
                        } else {
                            RaceGroupSeriesDTO seriesDTO = new RaceGroupSeriesDTO(LeaderboardNameConstants.DEFAULT_SERIES_NAME);
                            raceGroup.getSeries().add(seriesDTO);
                            FleetDTO fleetDTO = new FleetDTO(LeaderboardNameConstants.DEFAULT_FLEET_NAME, 0, null);
                            seriesDTO.getFleets().add(fleetDTO);
                        }
                        raceGroups.add(raceGroup);
                    }
                }
            }
        }
        return raceGroups;
    }

    /**
     * The name of the regatta to be shown on the regatta overview webpage is retrieved from the name of the {@link Leaderboard}. Since regattas are
     * not always represented by a {@link Regatta} object in the Sailing Suite but need to be shown on the regatta overview page, the leaderboard is
     * used as the representative of the sailing regatta. When a display name is set for a leaderboard, this name is favored against the (mostly technical)
     * regatta name as the display name represents the publicly visible name of the regatta. 
     * <br>
     * When the leaderboard is a {@link RegattaLeaderboard} the name of the {@link Regatta} is used, otherwise the leaderboard 
     * is a {@link FlexibleLeaderboard} and it's name is used as the last option.
     * @param leaderboard The {@link Leaderboard} from which the name is be retrieved
     * @return the name of the regatta to be shown on the regatta overview page
     */
    private String getRegattaNameFromLeaderboard(Leaderboard leaderboard) {
        String regattaName;
        if (leaderboard.getDisplayName() != null && !leaderboard.getDisplayName().isEmpty()) {
            regattaName = leaderboard.getDisplayName();
        } else {
            if (leaderboard instanceof RegattaLeaderboard) {
                RegattaLeaderboard regattaLeaderboard = (RegattaLeaderboard) leaderboard;
                regattaName = regattaLeaderboard.getRegatta().getName();
            } else {
                regattaName = leaderboard.getName();
            }
        }
        return regattaName;
    }

    @Override
    public void removeRegattas(Collection<RegattaIdentifier> selectedRegattas) {
        for (RegattaIdentifier regatta : selectedRegattas) {
            removeRegatta(regatta);
        }
    }
    
    @Override
    public void removeRegatta(RegattaIdentifier regattaIdentifier) {
        getService().apply(new RemoveRegatta(regattaIdentifier));
    }

    private RaceColumnInSeriesDTO convertToRaceColumnInSeriesDTO(RaceColumnInSeries raceColumnInSeries) {
        RaceColumnInSeriesDTO raceColumnInSeriesDTO = new RaceColumnInSeriesDTO(raceColumnInSeries.getSeries().getName(),
                raceColumnInSeries.getRegatta().getName());
        raceColumnInSeriesDTO.setName(raceColumnInSeries.getName());
        raceColumnInSeriesDTO.setMedalRace(raceColumnInSeries.isMedalRace());
        return raceColumnInSeriesDTO;
    }

    @Override
    public void updateRegatta(RegattaIdentifier regattaName, UUID defaultCourseAreaUuid, 
            RegattaConfigurationDTO configurationDTO) {
        getService().apply(new UpdateSpecificRegatta(regattaName, defaultCourseAreaUuid, convertToRegattaConfiguration(configurationDTO)));
    }

    @Override
    public List<RaceColumnInSeriesDTO> addRaceColumnsToSeries(RegattaIdentifier regattaIdentifier, String seriesName, List<String> columnNames) {
        List<RaceColumnInSeriesDTO> result = new ArrayList<RaceColumnInSeriesDTO>();
        for(String columnName: columnNames) {
            RaceColumnInSeries raceColumnInSeries = getService().apply(new AddColumnToSeries(regattaIdentifier, seriesName, columnName));
            if(raceColumnInSeries != null) {
                result.add(convertToRaceColumnInSeriesDTO(raceColumnInSeries));
            }
        }
        return result;
    }
    
    @Override
    public void updateSeries(RegattaIdentifier regattaIdentifier, String seriesName, String newSeriesName, boolean isMedal,
            int[] resultDiscardingThresholds, boolean startsWithZeroScore,
            boolean firstColumnIsNonDiscardableCarryForward, boolean hasSplitFleetContiguousScoring,
            List<FleetDTO> fleets) {
        getService().apply(
                new UpdateSeries(regattaIdentifier, seriesName, newSeriesName, isMedal, resultDiscardingThresholds,
                        startsWithZeroScore, firstColumnIsNonDiscardableCarryForward, hasSplitFleetContiguousScoring,
                        fleets));
    }

    @Override
    public RaceColumnInSeriesDTO addRaceColumnToSeries(RegattaIdentifier regattaIdentifier, String seriesName, String columnName) {
        RaceColumnInSeriesDTO result = null;
        RaceColumnInSeries raceColumnInSeries = getService().apply(new AddColumnToSeries(regattaIdentifier, seriesName, columnName));
        if(raceColumnInSeries != null) {
            result = convertToRaceColumnInSeriesDTO(raceColumnInSeries);
        }
        return result;
    }

    @Override
    public void removeRaceColumnsFromSeries(RegattaIdentifier regattaIdentifier, String seriesName, List<String> columnNames) {
        for(String columnName: columnNames) {
            getService().apply(new RemoveColumnFromSeries(regattaIdentifier, seriesName, columnName));
        }
    }

    @Override
    public void removeRaceColumnFromSeries(RegattaIdentifier regattaIdentifier, String seriesName, String columnName) {
        getService().apply(new RemoveColumnFromSeries(regattaIdentifier, seriesName, columnName));
    }

    @Override
    public void moveRaceColumnInSeriesUp(RegattaIdentifier regattaIdentifier, String seriesName, String columnName) {
        getService().apply(new MoveColumnInSeriesUp(regattaIdentifier, seriesName, columnName));
    }

    @Override
    public void moveRaceColumnInSeriesDown(RegattaIdentifier regattaIdentifier, String seriesName, String columnName) {
        getService().apply(new MoveColumnInSeriesDown(regattaIdentifier, seriesName, columnName));
    }

    @Override
    public RegattaDTO createRegatta(String regattaName, String boatClassName,
            RegattaCreationParametersDTO seriesNamesWithFleetNamesAndFleetOrderingAndMedal,
            boolean persistent, ScoringSchemeType scoringSchemeType, UUID defaultCourseAreaId) {
        Regatta regatta = getService().apply(
                new AddSpecificRegatta(
                        regattaName, boatClassName, UUID.randomUUID(),
                        seriesNamesWithFleetNamesAndFleetOrderingAndMedal,
                        persistent, baseDomainFactory.createScoringScheme(scoringSchemeType), defaultCourseAreaId));
        return convertToRegattaDTO(regatta);
    }

    @Override
    public RegattaScoreCorrectionDTO getScoreCorrections(String scoreCorrectionProviderName, String eventName,
            String boatClassName, Date timePointWhenResultPublished) throws Exception {
        RegattaScoreCorrectionDTO result = null;
        for (ScoreCorrectionProvider scp : getAllScoreCorrectionProviders()) {
            if (scp.getName().equals(scoreCorrectionProviderName)) {
                result = createScoreCorrection(scp.getScoreCorrections(eventName, boatClassName,
                        new MillisecondsTimePoint(timePointWhenResultPublished)));
                break;
            }
        }
        return result;
    }

    private RegattaScoreCorrectionDTO createScoreCorrection(RegattaScoreCorrections scoreCorrections) {
        // Key is the race name or number as String; values are maps whose key is the sailID.
        LinkedHashMap<String, Map<String, ScoreCorrectionEntryDTO>> map = new LinkedHashMap<String, Map<String, ScoreCorrectionEntryDTO>>();
        for (ScoreCorrectionsForRace sc4r : scoreCorrections.getScoreCorrectionsForRaces()) {
            Map<String, ScoreCorrectionEntryDTO> entryMap = new HashMap<String, RegattaScoreCorrectionDTO.ScoreCorrectionEntryDTO>();
            for (String sailID : sc4r.getSailIDs()) {
                entryMap.put(sailID, createScoreCorrectionEntryDTO(sc4r.getScoreCorrectionForCompetitor(sailID)));
            }
            map.put(sc4r.getRaceNameOrNumber(), entryMap);
        }
        return new RegattaScoreCorrectionDTO(scoreCorrections.getProvider().getName(), map);
    }

    private ScoreCorrectionEntryDTO createScoreCorrectionEntryDTO(
            ScoreCorrectionForCompetitorInRace scoreCorrectionForCompetitor) {
        return new ScoreCorrectionEntryDTO(scoreCorrectionForCompetitor.getPoints(),
                scoreCorrectionForCompetitor.isDiscarded(), scoreCorrectionForCompetitor.getMaxPointsReason());
    }
    
    @Override
    public List<String> getUrlResultProviderNames() {
        List<String> result = new ArrayList<String>();
        for (ScoreCorrectionProvider scp : getAllScoreCorrectionProviders()) {
            if (scp instanceof ResultUrlProvider) {
                result.add(scp.getName());
            }
        }
        return result;
    }

    private ResultUrlProvider getUrlBasedScoreCorrectionProvider(String resultProviderName) {
        ResultUrlProvider result = null;
        for (ScoreCorrectionProvider scp : getAllScoreCorrectionProviders()) {
            if (scp instanceof ResultUrlProvider && scp.getName().equals(resultProviderName)) {
                result = (ResultUrlProvider) scp;
                break;
            }
        }
        return result;
    }

    @Override
    public List<String> getResultImportUrls(String resultProviderName) {
        List<String> result = new ArrayList<String>();
        ResultUrlProvider urlBasedScoreCorrectionProvider = getUrlBasedScoreCorrectionProvider(resultProviderName);
        if (urlBasedScoreCorrectionProvider != null) {
            Iterable<URL> allUrls = ResultUrlRegistry.INSTANCE.getResultUrls(resultProviderName);
            for (URL url : allUrls) {
                result.add(url.toString());
            }
        }
        return result;
    }

    @Override
    public void removeResultImportURLs(String resultProviderName, Set<String> toRemove) throws Exception {
        ResultUrlProvider urlBasedScoreCorrectionProvider = getUrlBasedScoreCorrectionProvider(resultProviderName);
        if (urlBasedScoreCorrectionProvider != null) {
            for (String urlToRemove : toRemove) {
                ResultUrlRegistry.INSTANCE.unregisterResultUrl(resultProviderName, new URL(urlToRemove));
            }
        }
    }

    @Override
    public void addResultImportUrl(String resultProviderName, String url) throws Exception {
        ResultUrlProvider urlBasedScoreCorrectionProvider = getUrlBasedScoreCorrectionProvider(resultProviderName);
        if (urlBasedScoreCorrectionProvider != null) {
            ResultUrlRegistry.INSTANCE.registerResultUrl(resultProviderName, new URL(url));
        }
    }    

    @Override
    public List<String> getOverallLeaderboardNamesContaining(String leaderboardName) {
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard == null) {
            throw new IllegalArgumentException("Couldn't find leaderboard named "+leaderboardName);
        }
        List<String> result = new ArrayList<String>();
        for (Map.Entry<String, Leaderboard> leaderboardEntry : getService().getLeaderboards().entrySet()) {
            if (leaderboardEntry.getValue() instanceof MetaLeaderboard) {
                MetaLeaderboard metaLeaderboard = (MetaLeaderboard) leaderboardEntry.getValue();
                if (Util.contains(metaLeaderboard.getLeaderboards(), leaderboard)) {
                    result.add(leaderboardEntry.getKey());
                }
            }
        }
        return result;
    }

    @Override
    public List<SwissTimingArchiveConfigurationDTO> getPreviousSwissTimingArchiveConfigurations() {
        Iterable<SwissTimingArchiveConfiguration> configs = swissTimingAdapterPersistence.getSwissTimingArchiveConfigurations();
        List<SwissTimingArchiveConfigurationDTO> result = new ArrayList<SwissTimingArchiveConfigurationDTO>();
        for (SwissTimingArchiveConfiguration stArchiveConfig : configs) {
            result.add(new SwissTimingArchiveConfigurationDTO(stArchiveConfig.getJsonUrl()));
        }
        return result;
    }

    @Override
    public void storeSwissTimingArchiveConfiguration(String swissTimingJsonUrl) {
        swissTimingAdapterPersistence.storeSwissTimingArchiveConfiguration(swissTimingFactory.createSwissTimingArchiveConfiguration(
                swissTimingJsonUrl));
    }

    @Override
    public PolarSheetGenerationResponse generatePolarSheetForRaces(List<RegattaAndRaceIdentifier> selectedRaces,
            PolarSheetGenerationSettings settings, String name) throws Exception {
        String id = UUID.randomUUID().toString();
        RacingEventService service = getService();
        Set<TrackedRace> trackedRaces = new HashSet<TrackedRace>();
        for (RegattaAndRaceIdentifier race : selectedRaces) {
            trackedRaces.add(service.getTrackedRace(race));
        }
        PolarSheetGenerationWorker genWorker = new PolarSheetGenerationWorker(trackedRaces, settings, executor);
        genWorker.startPolarSheetGeneration();
        if (name == null || name.isEmpty()) {
            name = getCommonBoatClass(trackedRaces);
        }
        PolarSheetsData result = genWorker.get();
        return new PolarSheetGenerationResponseImpl(id, name, result);
    }

    private String getCommonBoatClass(Set<TrackedRace> trackedRaces) {
        BoatClass boatClass = null;
        for (TrackedRace race : trackedRaces) {
            if (boatClass == null) {
                boatClass = race.getRace().getBoatClass();
            }
            if (!boatClass.getName().toLowerCase().matches(race.getRace().getBoatClass().getName().toLowerCase())) {
                return "Mixed";
            }
        }

        return boatClass.getName();
    }

    protected com.sap.sailing.domain.base.DomainFactory getBaseDomainFactory() {
        return baseDomainFactory;
    }

    @Override
    public List<RegattaOverviewEntryDTO> getRaceStateEntriesForRaceGroup(UUID eventId, List<UUID> visibleCourseAreaIds, 
            List<String> visibleRegattas, boolean showOnlyCurrentlyRunningRaces, boolean showOnlyRacesOfSameDay) {
        List<RegattaOverviewEntryDTO> result = new ArrayList<RegattaOverviewEntryDTO>();
        
        Calendar dayToCheck = Calendar.getInstance();
        dayToCheck.setTime(new Date());
        
        Event event = getService().getEvent(eventId);
        if (event != null) {
            for (CourseArea courseArea : event.getVenue().getCourseAreas()) {
                if (!visibleCourseAreaIds.contains(courseArea.getId())) {
                    continue;
                }
                for (Leaderboard leaderboard : getService().getLeaderboards().values()) {
                    if (leaderboard.getDefaultCourseArea() != null && leaderboard.getDefaultCourseArea().equals(courseArea)) {
                        if (!visibleRegattas.contains(leaderboard.getName())) {
                            continue;
                        }
                        String regattaName = getRegattaNameFromLeaderboard(leaderboard);
                        if (leaderboard instanceof RegattaLeaderboard) {
                            RegattaLeaderboard regattaLeaderboard = (RegattaLeaderboard) leaderboard;
                            for (Series series : regattaLeaderboard.getRegatta().getSeries()) {
                                Map<String, List<RegattaOverviewEntryDTO>> entriesPerFleet = new HashMap<String, List<RegattaOverviewEntryDTO>>();
                                for (RaceColumn raceColumn : series.getRaceColumns()) {
                                    getRegattaOverviewEntries(showOnlyRacesOfSameDay, dayToCheck,
                                            courseArea, leaderboard, regattaName, series.getName(), raceColumn, entriesPerFleet);
                                }
                                result.addAll(getRegattaOverviewEntriesToBeShown(showOnlyCurrentlyRunningRaces, entriesPerFleet));
                            }

                        } else {
                            Map<String, List<RegattaOverviewEntryDTO>> entriesPerFleet = new HashMap<String, List<RegattaOverviewEntryDTO>>();
                            for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
                                getRegattaOverviewEntries(showOnlyRacesOfSameDay, dayToCheck, courseArea,
                                        leaderboard, regattaName, LeaderboardNameConstants.DEFAULT_SERIES_NAME, raceColumn, entriesPerFleet);
                            }
                            result.addAll(getRegattaOverviewEntriesToBeShown(showOnlyCurrentlyRunningRaces, entriesPerFleet));
                        }
                    }
                }
            }
        }
        return result;
    }

    private void getRegattaOverviewEntries(boolean showOnlyRacesOfSameDay, Calendar dayToCheck,
            CourseArea courseArea, Leaderboard leaderboard, String regattaName, String seriesName, RaceColumn raceColumn,
            Map<String, List<RegattaOverviewEntryDTO>> entriesPerFleet) {

        for (Fleet fleet : raceColumn.getFleets()) {
            RegattaOverviewEntryDTO entry = createRegattaOverviewEntryDTO(courseArea,
                    leaderboard, regattaName, seriesName, raceColumn, fleet, 
                    showOnlyRacesOfSameDay, dayToCheck);
            if (entry != null) {
                addRegattaOverviewEntryToEntriesPerFleet(entriesPerFleet, fleet, entry);
            }
        }
    }

    private List<RegattaOverviewEntryDTO> getRegattaOverviewEntriesToBeShown(boolean showOnlyCurrentlyRunningRaces,
            Map<String, List<RegattaOverviewEntryDTO>> entriesPerFleet) {
        List<RegattaOverviewEntryDTO> result = new ArrayList<RegattaOverviewEntryDTO>();
        for (List<RegattaOverviewEntryDTO> entryList : entriesPerFleet.values()) {
            result.addAll(entryList);
            if (showOnlyCurrentlyRunningRaces) {
                List<RegattaOverviewEntryDTO> finishedEntries = new ArrayList<RegattaOverviewEntryDTO>();
                for (RegattaOverviewEntryDTO entry : entryList) {
                    if (!RaceLogRaceStatus.isActive(entry.raceInfo.lastStatus)) {
                        if (entry.raceInfo.lastStatus.equals(RaceLogRaceStatus.FINISHED)) {
                            finishedEntries.add(entry);
                        } else if (entry.raceInfo.lastStatus.equals(RaceLogRaceStatus.UNSCHEDULED)) {
                            //don't filter when the race is unscheduled and aborted before
                            if (!entry.raceInfo.isRaceAbortedInPassBefore) {
                                result.remove(entry);
                            }
                            
                        }
                    }
                }
                if (!finishedEntries.isEmpty()) {
                    //keep the last finished race in the list to be shown
                    int indexOfLastElement = finishedEntries.size() - 1;
                    finishedEntries.remove(indexOfLastElement);
                    
                    //... and remove all other finished races
                    result.removeAll(finishedEntries);
                }
            }
        }
        return result;
    }

    private void addRegattaOverviewEntryToEntriesPerFleet(Map<String, List<RegattaOverviewEntryDTO>> entriesPerFleet,
            Fleet fleet, RegattaOverviewEntryDTO entry) {
        if (!entriesPerFleet.containsKey(fleet.getName())) {
           entriesPerFleet.put(fleet.getName(), new ArrayList<RegattaOverviewEntryDTO>()); 
        }
        entriesPerFleet.get(fleet.getName()).add(entry);
    }
    
    private RegattaOverviewEntryDTO createRegattaOverviewEntryDTO(CourseArea courseArea, Leaderboard leaderboard,
            String regattaName, String seriesName, RaceColumn raceColumn, Fleet fleet, boolean showOnlyRacesOfSameDay, Calendar dayToCheck) {
        RegattaOverviewEntryDTO entry = new RegattaOverviewEntryDTO();
        entry.courseAreaName = courseArea.getName();
        entry.courseAreaIdAsString = courseArea.getId().toString();
        entry.regattaDisplayName = regattaName;
        entry.regattaName = leaderboard.getName();
        entry.raceInfo = createRaceInfoDTO(seriesName, raceColumn, fleet);
        entry.currentServerTime = new Date();
        
        if (showOnlyRacesOfSameDay) {
            if (!RaceStateOfSameDayHelper.isRaceStateOfSameDay(entry.raceInfo.startTime, entry.raceInfo.finishedTime, dayToCheck)) {
                entry = null;
            }
        }
        return entry;
    }
    
    @Override
    public String getBuildVersion() {
        return BuildVersion.getBuildVersion();
    }

    @Override
    public void stopReplicatingFromMaster() {
        try {
            getReplicationService().stopToReplicateFromMaster();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stopAllReplicas() {
        try {
            getReplicationService().stopAllReplica();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stopSingleReplicaInstance(String identifier) {
        UUID uuid = UUID.fromString(identifier);
        ReplicaDescriptor replicaDescriptor = new ReplicaDescriptor(null, uuid, "");
        try {
            getReplicationService().unregisterReplica(replicaDescriptor);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void reloadRaceLog(String leaderboardName, RaceColumnDTO raceColumnDTO, FleetDTO fleet) {
        getService().reloadRaceLog(leaderboardName, raceColumnDTO.getName(), fleet.getName());
    }

    @Override
    public RaceLogDTO getRaceLog(String leaderboardName, RaceColumnDTO raceColumnDTO, FleetDTO fleet) {
        RaceLogDTO result = null;
        RaceLog raceLog = getService().getRaceLog(leaderboardName, raceColumnDTO.getName(), fleet.getName());
        if(raceLog != null) {
            List<RaceLogEventDTO> entries = new ArrayList<RaceLogEventDTO>();
            result = new RaceLogDTO(leaderboardName, raceColumnDTO.getName(), fleet.getName(), raceLog.getCurrentPassId(), entries);
            raceLog.lockForRead();
            try {
                for(RaceLogEvent raceLogEvent: raceLog.getRawFixes()) {
                    RaceLogEventDTO entry = new RaceLogEventDTO(raceLogEvent.getPassId(), 
                            raceLogEvent.getAuthor().getName(), raceLogEvent.getAuthor().getPriority(), 
                            raceLogEvent.getCreatedAt() != null ? raceLogEvent.getCreatedAt().asDate() : null,
                            raceLogEvent.getLogicalTimePoint() != null ? raceLogEvent.getLogicalTimePoint().asDate() : null,
                            raceLogEvent.getClass().getSimpleName(), raceLogEvent.getShortInfo());
                    entries.add(entry);
                }
            } finally {
                raceLog.unlockAfterRead();
            }
        }
        return result;
    }

    @Override
    public MasterDataImportObjectCreationCount importMasterData(String urlAsString, String[] groupNames, boolean override, boolean compress) {
        long startTime = System.currentTimeMillis();
        String hostname;
        Integer port = 80;
        try {
            URL url = new URL(urlAsString);
            hostname = url.getHost();
            port = url.getPort();
        } catch (MalformedURLException e1) {
            hostname = urlAsString;
            if (urlAsString.contains("://")) {
                hostname = hostname.split("://")[1];
            }
            if (hostname.contains("/")) {
                hostname = hostname.split("/")[0]; // also eliminate a trailing slash
            }
            if (hostname.contains(":")) {
                String[] split = hostname.split(":");
                hostname = split[0];
                port = Integer.parseInt(split[1]);
            }
        }
        String query;
        try {
            query = createLeaderboardQuery(groupNames, compress);
        } catch (UnsupportedEncodingException e1) {
            throw new RuntimeException(e1);
        }
        HttpURLConnection connection = null;

        URL serverAddress = null;
        InputStream inputStream = null;
        ObjectInputStream objectInputStream = null;
        try {
            serverAddress = createUrl(hostname, port, query);
            //set up out communications stuff
            connection = null;
            //Set up the initial connection
            connection = (HttpURLConnection)serverAddress.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            // Initial timeout needs to be big enough to allow the first parts of the response to reach this server
            connection.setReadTimeout(60000);
            connection.connect();

            
            if (compress) {
                InputStream timeoutExtendingInputStream = new TimeoutExtendingInputStream(connection.getInputStream(),
                        connection);
                inputStream = new GZIPInputStream(timeoutExtendingInputStream);
            } else {
                inputStream = new TimeoutExtendingInputStream(connection.getInputStream(), connection);
            }

            objectInputStream = getService().getBaseDomainFactory().createObjectInputStreamResolvingAgainstThisFactory(
                    inputStream);
            TopLevelMasterData topLevelMasterData = (TopLevelMasterData) objectInputStream.readObject();
            return importFromHttpResponse(topLevelMasterData, override);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            // close the connection, set all objects to null
            connection.disconnect();
            connection = null;
            long timeToImport = System.currentTimeMillis() - startTime;
            logger.info(String.format("Took %s ms overall to import master data.", timeToImport));
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (objectInputStream != null) {
                    objectInputStream.close();
                }
            } catch (IOException e) {
            }
        }
    }
    
    private URL createUrl(String host, Integer port, String query) throws Exception {
        return new URL("http://" + host + ":" + port + "/sailingserver/spi/v1/masterdata/leaderboardgroups?" + query);
    }
    
    protected MasterDataImportObjectCreationCount importFromHttpResponse(TopLevelMasterData topLevelMasterData,
            boolean override) {
        MasterDataImporter importer = new MasterDataImporter(baseDomainFactory, getService());       
        return importer.importMasterData(topLevelMasterData, override);
    }

    private String createLeaderboardQuery(String[] groupNames, boolean compress) throws UnsupportedEncodingException {
        StringBuffer queryStringBuffer = new StringBuffer("");
        for (int i = 0; i < groupNames.length; i++) {
            String encodedGroupName = URLEncoder.encode(groupNames[i], "UTF-8");
            queryStringBuffer.append("names[]=" + encodedGroupName + "&");
        }
        if (compress) {
            queryStringBuffer.append("compress=true");
        } else {
            queryStringBuffer.deleteCharAt(queryStringBuffer.length() - 1);
        }
        return queryStringBuffer.toString();
    }

    @Override
    public Iterable<CompetitorDTO> getCompetitors() {
        return convertToCompetitorDTOs(getService().getBaseDomainFactory().getCompetitorStore().getCompetitors());
    }

    @Override
    public Iterable<CompetitorDTO> getCompetitorsOfLeaderboard(String leaderboardName) {
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        return convertToCompetitorDTOs(leaderboard.getAllCompetitors());
    }

    @Override
    public CompetitorDTO updateCompetitor(CompetitorDTO competitor) {
        return getBaseDomainFactory().convertToCompetitorDTO(
                getService().apply(new UpdateCompetitor(competitor.getIdAsString(), competitor.getName(),
                competitor.getColor(), competitor.getSailID(),
                (competitor.getThreeLetterIocCountryCode() == null || competitor.getThreeLetterIocCountryCode().isEmpty()) ? null :
                    getBaseDomainFactory().getOrCreateNationality(competitor.getThreeLetterIocCountryCode()))));
    }

    @Override
    public void allowCompetitorResetToDefaults(Iterable<CompetitorDTO> competitors) {
        List<String> competitorIdsAsStrings = new ArrayList<String>();
        for (CompetitorDTO competitor : competitors) {
            competitorIdsAsStrings.add(competitor.getIdAsString());
        }
        getService().apply(new AllowCompetitorResetToDefaults(competitorIdsAsStrings));
    }

    
    @Override
    public <ResultType extends Number> QueryResult<ResultType> runQuery(QueryDefinition queryDefinition) throws Exception {
        Query<?, ResultType> query = DataMiningFactory.createQuery(queryDefinition, getService()); 
        return query.run();
    }
    
    @Override
    public DataMiningSerializationDummy pseudoMethodSoThatSomeDataMiningClassesAreAddedToTheGWTSerializationPolicy() {
        return null;
    }
    
    @Override
    public List<DeviceConfigurationMatcherDTO> getDeviceConfigurationMatchers() {
        List<DeviceConfigurationMatcherDTO> configs = new ArrayList<DeviceConfigurationMatcherDTO>();
        for (Entry<DeviceConfigurationMatcher, DeviceConfiguration> entry : 
            getService().getAllDeviceConfigurations().entrySet()) {
            DeviceConfigurationMatcher matcher = entry.getKey();
            configs.add(convertToDeviceConfigurationMatcherDTO(matcher));
        }
        return configs;
    }

    @Override
    public DeviceConfigurationDTO getDeviceConfiguration(DeviceConfigurationMatcherDTO matcherDto) {
        DeviceConfigurationMatcher matcher = convertToDeviceConfigurationMatcher(matcherDto.type, matcherDto.clients);
        DeviceConfiguration configuration = getService().getAllDeviceConfigurations().get(matcher);
        if (configuration == null) {
            return null;
        } else {
            return convertToDeviceConfigurationDTO(configuration);
        }
    }

    @Override
    public DeviceConfigurationMatcherDTO createOrUpdateDeviceConfiguration(DeviceConfigurationMatcherDTO matcherDTO, DeviceConfigurationDTO configurationDTO) {
        DeviceConfigurationMatcher matcher = convertToDeviceConfigurationMatcher(matcherDTO.type, matcherDTO.clients);
        DeviceConfiguration configuration = convertToDeviceConfiguration(configurationDTO);
        getService().createOrUpdateDeviceConfiguration(matcher, configuration);
        return convertToDeviceConfigurationMatcherDTO(matcher);
    }

    @Override
    public boolean removeDeviceConfiguration(DeviceConfigurationMatcherType type, List<String> clientIds) {
        DeviceConfigurationMatcher matcher = convertToDeviceConfigurationMatcher(type, clientIds);
        getService().removeDeviceConfiguration(matcher);
        return true;
    }

    private DeviceConfigurationMatcherDTO convertToDeviceConfigurationMatcherDTO(DeviceConfigurationMatcher matcher) {
        List<String> clients = new ArrayList<String>();
        
        if (matcher instanceof DeviceConfigurationMatcherSingle) {
            clients.add(((DeviceConfigurationMatcherSingle)matcher).getClientIdentifier());
        } else if (matcher instanceof DeviceConfigurationMatcherMulti) {
            Util.addAll(((DeviceConfigurationMatcherMulti)matcher).getClientIdentifiers(), clients);
        }
        
        DeviceConfigurationMatcherDTO dto = new DeviceConfigurationMatcherDTO(
                matcher.getMatcherType(),
                clients,  
                matcher.getMatchingRank());
        return dto;
    }

    private DeviceConfigurationMatcher convertToDeviceConfigurationMatcher(DeviceConfigurationMatcherType type, List<String> clientIds) {
        return baseDomainFactory.getOrCreateDeviceConfigurationMatcher(type, clientIds);
    }

    private DeviceConfigurationDTO convertToDeviceConfigurationDTO(DeviceConfiguration configuration) {
        DeviceConfigurationDTO dto = new DeviceConfigurationDTO();
        dto.allowedCourseAreaNames = configuration.getAllowedCourseAreaNames();
        dto.resultsMailRecipient = configuration.getResultsMailRecipient();
        dto.byNameDesignerCourseNames = configuration.getByNameCourseDesignerCourseNames();
        if (configuration.getRegattaConfiguration() != null) {
            dto.regattaConfiguration = convertToRegattaConfigurationDTO(configuration.getRegattaConfiguration());
        }
        return dto;
    }

    private DeviceConfigurationDTO.RegattaConfigurationDTO convertToRegattaConfigurationDTO(
            RegattaConfiguration configuration) {
        if (configuration == null) {
            return null;
        }
        DeviceConfigurationDTO.RegattaConfigurationDTO dto = new DeviceConfigurationDTO.RegattaConfigurationDTO();
        
        dto.defaultRacingProcedureType = configuration.getDefaultRacingProcedureType();
        dto.defaultCourseDesignerMode = configuration.getDefaultCourseDesignerMode();
        
        if (configuration.getRRS26Configuration() != null) {
            dto.rrs26Configuration = new DeviceConfigurationDTO.RegattaConfigurationDTO.RRS26ConfigurationDTO();
            dto.rrs26Configuration.classFlag = configuration.getRRS26Configuration().getClassFlag();
            dto.rrs26Configuration.hasInidividualRecall = configuration.getRRS26Configuration().hasInidividualRecall();
            dto.rrs26Configuration.startModeFlags = configuration.getRRS26Configuration().getStartModeFlags();
        }
        if (configuration.getGateStartConfiguration() != null) {
            dto.gateStartConfiguration = new DeviceConfigurationDTO.RegattaConfigurationDTO.GateStartConfigurationDTO();
            dto.gateStartConfiguration.classFlag = configuration.getGateStartConfiguration().getClassFlag();
            dto.gateStartConfiguration.hasInidividualRecall = configuration.getGateStartConfiguration().hasInidividualRecall();
            dto.gateStartConfiguration.hasPathfinder = configuration.getGateStartConfiguration().hasPathfinder();
            dto.gateStartConfiguration.hasAdditionalGolfDownTime = configuration.getGateStartConfiguration().hasAdditionalGolfDownTime();
        }
        if (configuration.getESSConfiguration() != null) {
            dto.essConfiguration = new DeviceConfigurationDTO.RegattaConfigurationDTO.ESSConfigurationDTO();
            dto.essConfiguration.classFlag = configuration.getESSConfiguration().getClassFlag();
            dto.essConfiguration.hasInidividualRecall = configuration.getESSConfiguration().hasInidividualRecall();
        }
        if (configuration.getBasicConfiguration() != null) {
            dto.basicConfiguration = new DeviceConfigurationDTO.RegattaConfigurationDTO.RacingProcedureConfigurationDTO();
            dto.basicConfiguration.classFlag = configuration.getBasicConfiguration().getClassFlag();
            dto.basicConfiguration.hasInidividualRecall = configuration.getBasicConfiguration().hasInidividualRecall();
        }
        return dto;
    }

    private DeviceConfigurationImpl convertToDeviceConfiguration(DeviceConfigurationDTO dto) {
        DeviceConfigurationImpl configuration = new DeviceConfigurationImpl(convertToRegattaConfiguration(dto.regattaConfiguration));
        configuration.setAllowedCourseAreaNames(dto.allowedCourseAreaNames);
        configuration.setResultsMailRecipient(dto.resultsMailRecipient);
        configuration.setByNameDesignerCourseNames(dto.byNameDesignerCourseNames);
        return configuration;
    }

    private RegattaConfiguration convertToRegattaConfiguration(RegattaConfigurationDTO dto) {
        if (dto == null) {
            return null;
        }
        RegattaConfigurationImpl configuration = new RegattaConfigurationImpl();
        configuration.setDefaultRacingProcedureType(dto.defaultRacingProcedureType);
        configuration.setDefaultCourseDesignerMode(dto.defaultCourseDesignerMode);
        if (dto.rrs26Configuration != null) {
            RRS26ConfigurationImpl config = new RRS26ConfigurationImpl();
            config.setClassFlag(dto.rrs26Configuration.classFlag);
            config.setHasInidividualRecall(dto.rrs26Configuration.hasInidividualRecall);
            config.setStartModeFlags(dto.rrs26Configuration.startModeFlags);
            configuration.setRRS26Configuration(config);
        }
        if (dto.gateStartConfiguration != null) {
            GateStartConfigurationImpl config = new GateStartConfigurationImpl();
            config.setClassFlag(dto.gateStartConfiguration.classFlag);
            config.setHasInidividualRecall(dto.gateStartConfiguration.hasInidividualRecall);
            config.setHasPathfinder(dto.gateStartConfiguration.hasPathfinder);
            config.setHasAdditionalGolfDownTime(dto.gateStartConfiguration.hasAdditionalGolfDownTime);
            configuration.setGateStartConfiguration(config);
        }
        if (dto.essConfiguration != null) {
            ESSConfigurationImpl config = new ESSConfigurationImpl();
            config.setClassFlag(dto.essConfiguration.classFlag);
            config.setHasInidividualRecall(dto.essConfiguration.hasInidividualRecall);
            configuration.setESSConfiguration(config);
        }
        if (dto.basicConfiguration != null) {
            RacingProcedureConfigurationImpl config = new RacingProcedureConfigurationImpl();
            config.setClassFlag(dto.basicConfiguration.classFlag);
            config.setHasInidividualRecall(dto.basicConfiguration.hasInidividualRecall);
            configuration.setBasicConfiguration(config);
        }
        return configuration;
    }

    @Override
    public boolean setStartTime(RaceLogSetStartTimeDTO dto) {
        TimePoint newStartTime = getService().setStartTime(dto.leaderboardName, dto.raceColumnName, 
                dto.fleetName, dto.authorName, dto.authorPriority,
                dto.passId, new MillisecondsTimePoint(dto.logicalTimePoint), new MillisecondsTimePoint(dto.startTime));
        return new MillisecondsTimePoint(dto.startTime).equals(newStartTime);
    }

    @Override
    public Pair<Date, Integer> getStartTime(String leaderboardName, String raceColumnName, String fleetName) {
        Pair<TimePoint, Integer> result = getService().getStartTime(leaderboardName, raceColumnName, fleetName);
        if (result == null || result.getA() == null) {
            return null;
        }
        return new Pair<Date, Integer>(result.getA() == null ? null : result.getA().asDate(), result.getB());
    }

    @Override
    public Iterable<String> getAllIgtimiAccountEmailAddresses() {
        List<String> result = new ArrayList<String>();
        for (Account account : getIgtimiConnectionFactory().getAllAccounts()) {
            result.add(account.getUser().getEmail());
        }
        return result;
    }

    private IgtimiConnectionFactory getIgtimiConnectionFactory() {
        return igtimiAdapterTracker.getService();
    }

    @Override
    public String getIgtimiAuthorizationUrl() {
        return getIgtimiConnectionFactory().getAuthorizationUrl();
    }

    @Override
    public boolean authorizeAccessToIgtimiUser(String eMailAddress, String password) throws Exception {
        Account account = getIgtimiConnectionFactory().createAccountToAccessUserData(eMailAddress, password);
        return account != null;
    }

    @Override
    public void removeIgtimiAccount(String eMailOfAccountToRemove) {
        getIgtimiConnectionFactory().removeAccount(eMailOfAccountToRemove);
    }

    @Override
    public Map<RegattaAndRaceIdentifier, Integer> importWindFromIgtimi(List<RaceDTO> selectedRaces) throws IllegalStateException,
            ClientProtocolException, IOException, org.json.simple.parser.ParseException {
        final IgtimiConnectionFactory igtimiConnectionFactory = getIgtimiConnectionFactory();
        final Iterable<DynamicTrackedRace> trackedRaces;
        if (selectedRaces != null && !selectedRaces.isEmpty()) {
            List<DynamicTrackedRace> myTrackedRaces = new ArrayList<DynamicTrackedRace>();
            trackedRaces = myTrackedRaces;
            for (RaceDTO raceDTO : selectedRaces) {
                DynamicTrackedRace trackedRace = getTrackedRace(raceDTO.getRaceIdentifier());
                myTrackedRaces.add(trackedRace);
            }
        } else {
            trackedRaces = getAllTrackedRaces();
        }
        Map<RegattaAndRaceIdentifier, Integer> numberOfWindFixesImportedPerRace = new HashMap<RegattaAndRaceIdentifier, Integer>();
        for (Account account : igtimiConnectionFactory.getAllAccounts()) {
            IgtimiConnection conn = igtimiConnectionFactory.connect(account);
            Map<TrackedRace, Integer> resultsForAccounts = conn.importWindIntoRace(trackedRaces);
            for (Entry<TrackedRace, Integer> resultForAccount : resultsForAccounts.entrySet()) {
                RegattaAndRaceIdentifier key = resultForAccount.getKey().getRaceIdentifier();
                Integer i = numberOfWindFixesImportedPerRace.get(key);
                if (i == null) {
                    i = 0;
                }
                numberOfWindFixesImportedPerRace.put(key, i+resultForAccount.getValue());
            }
        }
        return numberOfWindFixesImportedPerRace;
    }

    private Set<DynamicTrackedRace> getAllTrackedRaces() {
        Set<DynamicTrackedRace> result = new HashSet<DynamicTrackedRace>();
        Iterable<Regatta> allRegattas = getService().getAllRegattas();
        for (Regatta regatta : allRegattas) {
            DynamicTrackedRegatta trackedRegatta = getService().getTrackedRegatta(regatta);
            if (trackedRegatta != null) {
                Iterable<TrackedRace> trackedRaces = trackedRegatta.getTrackedRaces();
                for (TrackedRace trackedRace : trackedRaces) {
                    result.add((DynamicTrackedRace) trackedRace);
                }
            }
        }
        return result;
    }

    private class TimeoutExtendingInputStream extends FilterInputStream {

        private final HttpURLConnection connection;

        protected TimeoutExtendingInputStream(InputStream in, HttpURLConnection connection) {
            super(in);
            this.connection = connection;
        }

        @Override
        public int read() throws IOException {
            connection.setReadTimeout(10000);
            return super.read();
        }

        @Override
        public int read(byte[] b) throws IOException {
            connection.setReadTimeout(10000);
            return super.read(b);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            connection.setReadTimeout(10000);
            return super.read(b, off, len);
        }

    }

}