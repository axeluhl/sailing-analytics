package com.sap.sailing.gwt.ui.server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
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
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Gate;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnInSeries;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.base.impl.SeriesImpl;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Color;
import com.sap.sailing.domain.common.CountryCode;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.NoWindError;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Placemark;
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
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KilometersPerHourSpeedImpl;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard.Entry;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.MetaLeaderboard;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.MongoWindStoreFactory;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingArchiveConfiguration;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingConfiguration;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingFactory;
import com.sap.sailing.domain.swisstimingadapter.persistence.SwissTimingAdapterPersistence;
import com.sap.sailing.domain.swisstimingreplayadapter.SwissTimingReplayRace;
import com.sap.sailing.domain.swisstimingreplayadapter.SwissTimingReplayService;
import com.sap.sailing.domain.swisstimingreplayadapter.SwissTimingReplayServiceFactory;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.MarkPassingManeuver;
import com.sap.sailing.domain.tracking.RaceChangeListener;
import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sailing.domain.tracking.RacesHandle;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRaceStatus;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.tracking.impl.TrackedRaceImpl;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.RaceRecord;
import com.sap.sailing.domain.tractracadapter.TracTracConfiguration;
import com.sap.sailing.freg.resultimport.FregResultProvider;
import com.sap.sailing.geocoding.ReverseGeocoder;
import com.sap.sailing.gwt.ui.client.SailingService;
import com.sap.sailing.gwt.ui.shared.BoatClassDTO;
import com.sap.sailing.gwt.ui.shared.BulkScoreCorrectionDTO;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;
import com.sap.sailing.gwt.ui.shared.CompetitorRaceDataDTO;
import com.sap.sailing.gwt.ui.shared.CompetitorsRaceDataDTO;
import com.sap.sailing.gwt.ui.shared.ControlPointDTO;
import com.sap.sailing.gwt.ui.shared.CourseDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.FleetDTO;
import com.sap.sailing.gwt.ui.shared.GPSFixDTO;
import com.sap.sailing.gwt.ui.shared.GateDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardEntryDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardRowDTO;
import com.sap.sailing.gwt.ui.shared.LegEntryDTO;
import com.sap.sailing.gwt.ui.shared.LegInfoDTO;
import com.sap.sailing.gwt.ui.shared.ManeuverDTO;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sailing.gwt.ui.shared.MarkPassingTimesDTO;
import com.sap.sailing.gwt.ui.shared.MarkpassingManeuverDTO;
import com.sap.sailing.gwt.ui.shared.PlacemarkDTO;
import com.sap.sailing.gwt.ui.shared.PlacemarkOrderDTO;
import com.sap.sailing.gwt.ui.shared.PositionDTO;
import com.sap.sailing.gwt.ui.shared.QuickRankDTO;
import com.sap.sailing.gwt.ui.shared.RaceColumnDTO;
import com.sap.sailing.gwt.ui.shared.RaceColumnInSeriesDTO;
import com.sap.sailing.gwt.ui.shared.RaceCourseMarksDTO;
import com.sap.sailing.gwt.ui.shared.RaceDTO;
import com.sap.sailing.gwt.ui.shared.RaceMapDataDTO;
import com.sap.sailing.gwt.ui.shared.RaceStatusDTO;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sailing.gwt.ui.shared.RaceWithCompetitorsDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.RegattaScoreCorrectionDTO;
import com.sap.sailing.gwt.ui.shared.RegattaScoreCorrectionDTO.ScoreCorrectionEntryDTO;
import com.sap.sailing.gwt.ui.shared.ReplicaDTO;
import com.sap.sailing.gwt.ui.shared.ReplicationMasterDTO;
import com.sap.sailing.gwt.ui.shared.ReplicationStateDTO;
import com.sap.sailing.gwt.ui.shared.ScoreCorrectionProviderDTO;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;
import com.sap.sailing.gwt.ui.shared.SpeedWithBearingDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.SwissTimingArchiveConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.SwissTimingConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.SwissTimingRaceRecordDTO;
import com.sap.sailing.gwt.ui.shared.SwissTimingReplayRaceDTO;
import com.sap.sailing.gwt.ui.shared.TracTracConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.TracTracRaceRecordDTO;
import com.sap.sailing.gwt.ui.shared.TrackedRaceDTO;
import com.sap.sailing.gwt.ui.shared.VenueDTO;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDTO;
import com.sap.sailing.gwt.ui.shared.WindTrackInfoDTO;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;
import com.sap.sailing.server.operationaltransformation.AddColumnToLeaderboard;
import com.sap.sailing.server.operationaltransformation.AddColumnToSeries;
import com.sap.sailing.server.operationaltransformation.AddSpecificRegatta;
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
import com.sap.sailing.server.replication.ReplicaDescriptor;
import com.sap.sailing.server.replication.ReplicationFactory;
import com.sap.sailing.server.replication.ReplicationMasterDescriptor;
import com.sap.sailing.server.replication.ReplicationService;

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
    
    private final com.sap.sailing.domain.tractracadapter.persistence.MongoObjectFactory tractracMongoObjectFactory;

    private final DomainObjectFactory domainObjectFactory;
    
    private final SwissTimingFactory swissTimingFactory;

    private final com.sap.sailing.domain.tractracadapter.persistence.DomainObjectFactory tractracDomainObjectFactory;
    
    private final com.sap.sailing.domain.common.CountryCodeFactory countryCodeFactory;
    
    private final Executor executor;
    
    /**
     * {@link #getLeaderboardByName(String, Date, Collection)} is the application's bottleneck. When two clients ask the
     * same data for the same leaderboard with their <code>waitForLatestAnalyses</code> parameters set to
     * <code>false</code>, expansion state and (quantized) time stamp, no two computations should be spawned for the two
     * clients. Instead, if the computation is still running, all clients asking the same wait for the single result.
     * Results are cached in this LRU-based evicting cache.
     */
    private final Map<String, LiveLeaderboardUpdater> leaderboardByNameLiveUpdaters;
    
    /**
     * This executor needs to be a different one than {@link #executor} because the tasks run by {@link #executor}
     * can depend on the results of the tasks run by {@link #raceDetailsExecutor}, and an {@link Executor} doesn't
     * move a task that is blocked by waiting for another {@link FutureTask} to the side but blocks permanently,
     * ending in a deadlock (one that cannot easily be detected by the Eclipse debugger either).
     */
    private final Executor raceDetailsExecutor;

    private final WeakHashMap<Competitor, CompetitorDTO> weakCompetitorDTOCache;

    private final Map<Pair<TrackedRace, Competitor>, RunnableFuture<RaceDetails>> raceDetailsAtEndOfTrackingCache;

    /**
     * Used to remove all these listeners from their tracked races when this servlet is {@link #destroy() destroyed}.
     */
    private final Set<CacheInvalidationListener> cacheInvalidationListeners;
    
    private final LeaderboardDTOCache leaderboardDTOCacheWaitingForLatestAnalysis;
    
    private final DomainFactory tractracDomainFactory;
    
    private final com.sap.sailing.domain.base.DomainFactory baseDomainFactory;

    public SailingServiceImpl() {
        BundleContext context = Activator.getDefault();
        tractracDomainFactory = DomainFactory.INSTANCE;
        baseDomainFactory = tractracDomainFactory.getBaseDomainFactory();
        raceDetailsAtEndOfTrackingCache = new HashMap<Pair<TrackedRace, Competitor>, RunnableFuture<RaceDetails>>();
        cacheInvalidationListeners = new HashSet<CacheInvalidationListener>();
        weakCompetitorDTOCache = new WeakHashMap<Competitor, CompetitorDTO>();
        racingEventServiceTracker = createAndOpenRacingEventServiceTracker(context);
        replicationServiceTracker = createAndOpenReplicationServiceTracker(context);
        scoreCorrectionProviderServiceTracker = createAndOpenScoreCorrectionProviderServiceTracker(context);
        mongoObjectFactory = MongoFactory.INSTANCE.getDefaultMongoObjectFactory();
        domainObjectFactory = MongoFactory.INSTANCE.getDefaultDomainObjectFactory();
        swissTimingAdapterPersistence = SwissTimingAdapterPersistence.INSTANCE;
        tractracDomainObjectFactory = com.sap.sailing.domain.tractracadapter.persistence.DomainObjectFactory.INSTANCE;
        tractracMongoObjectFactory = com.sap.sailing.domain.tractracadapter.persistence.MongoObjectFactory.INSTANCE;
        swissTimingFactory = SwissTimingFactory.INSTANCE;
        countryCodeFactory = com.sap.sailing.domain.common.CountryCodeFactory.INSTANCE;
        executor = new ThreadPoolExecutor(/* corePoolSize */ 0,
                /* maximumPoolSize */ Runtime.getRuntime().availableProcessors(),
                /* keepAliveTime */ 60, TimeUnit.SECONDS,
                /* workQueue */ new LinkedBlockingQueue<Runnable>());
        raceDetailsExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        leaderboardByNameLiveUpdaters = new HashMap<String, LiveLeaderboardUpdater>();
        leaderboardDTOCacheWaitingForLatestAnalysis = new LeaderboardDTOCache(this, /* waitForLatestAnalyses */ true);
    }
    
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
    }
    
    public void destroy() {
        super.destroy();
        for (CacheInvalidationListener cacheInvalidationListener : cacheInvalidationListeners) {
            cacheInvalidationListener.removeFromTrackedRace();
        }
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
    private ServiceTracker<ScoreCorrectionProvider, ScoreCorrectionProvider> createAndOpenScoreCorrectionProviderServiceTracker(
            BundleContext bundleContext) {
        ServiceTracker<ScoreCorrectionProvider, ScoreCorrectionProvider> tracker = new ServiceTracker<ScoreCorrectionProvider, ScoreCorrectionProvider>(bundleContext,
                ScoreCorrectionProvider.class.getName(),
                /* customizer */null);
        tracker.open();
        return tracker;
    }
    
    @Override
    public Iterable<ScoreCorrectionProviderDTO> getScoreCorrectionProviderDTOs() throws Exception {
        List<ScoreCorrectionProviderDTO> result = new ArrayList<ScoreCorrectionProviderDTO>();
        final Iterable<ScoreCorrectionProvider> services = getScoreCorrectionProviders();
        for (ScoreCorrectionProvider scoreCorrectionProvider : services) {
            result.add(createScoreCorrectionProviderDTO((ScoreCorrectionProvider) scoreCorrectionProvider));
        }
        return result;
    }

    private Iterable<ScoreCorrectionProvider> getScoreCorrectionProviders() {
        final Object[] services = scoreCorrectionProviderServiceTracker.getServices();
        List<ScoreCorrectionProvider> result = new ArrayList<ScoreCorrectionProvider>();
        if (services != null) {
            for (Object service : services) {
                result.add((ScoreCorrectionProvider) service);
            }
        }
        return result;
    }
    
    private ScoreCorrectionProviderDTO createScoreCorrectionProviderDTO(ScoreCorrectionProvider scoreCorrectionProvider)
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
     * Otherwise, the leaderboard is {@link #computeLeaderboardByName(String, Date, Collection, boolean) computed}
     * synchronously on the fly.
     */
    @Override
    public LeaderboardDTO getLeaderboardByName(final String leaderboardName, final Date date,
            final Collection<String> namesOfRaceColumnsForWhichToLoadLegDetails)
            throws NoWindException, InterruptedException, ExecutionException {
        long startOfRequestHandling = System.currentTimeMillis();
        LeaderboardDTO result = null;
        TimePoint timePoint;
        LiveLeaderboardUpdater liveLeaderboardUpdater;
        final Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard != null) {
            if (date == null) {
                // date==null means live mode; however, if we're after the end of all races and after all score
                // corrections, don't use the live leaderboard updater which would keep re-calculating over and over again, but map
                // this to a usual non-live call which uses the regular LeaderboardDTOCache which is invalidated properly
                // when the tracked race associations or score corrections or tracked race contents changes:
                // TODO see bug 929: use lock instead of synchronized to increase concurrency
                synchronized (leaderboardByNameLiveUpdaters) {
                    liveLeaderboardUpdater = leaderboardByNameLiveUpdaters.get(leaderboardName);
                    if (liveLeaderboardUpdater == null) {
                        liveLeaderboardUpdater = new LiveLeaderboardUpdater(leaderboard, this);
                        leaderboardByNameLiveUpdaters.put(leaderboardName, liveLeaderboardUpdater);
                    }
                }
                final TimePoint nowMinusDelay = leaderboard.getNowMinusDelay();
                final TimePoint timePointOfLatestModification = leaderboard.getTimePointOfLatestModification();
                if (timePointOfLatestModification != null && !nowMinusDelay.before(timePointOfLatestModification)) {
                    timePoint = timePointOfLatestModification;
                } else {
                    timePoint = null;
                    result = liveLeaderboardUpdater.getLiveLeaderboard(namesOfRaceColumnsForWhichToLoadLegDetails,
                            nowMinusDelay);
                }
            } else {
                timePoint = new MillisecondsTimePoint(date);
            }
            if (timePoint != null) {
                // in replay we'd like up-to-date results; they are still cached
                // which is OK because the cache is invalidated whenever any of the tracked races attached to the
                // leaderboard changes.
                result = leaderboardDTOCacheWaitingForLatestAnalysis.getLeaderboardByName(leaderboard, timePoint,
                        namesOfRaceColumnsForWhichToLoadLegDetails);
            }
            logger.fine("getLeaderboardByName(" + leaderboardName + ", " + date + ", "
                    + namesOfRaceColumnsForWhichToLoadLegDetails + ") took "
                    + (System.currentTimeMillis() - startOfRequestHandling) + "ms");
        }
        return result;
    }

    protected LeaderboardDTO computeLeaderboardByName(final Leaderboard leaderboard, final TimePoint timePoint,
            final Collection<String> namesOfRaceColumnsForWhichToLoadLegDetails, final boolean waitForLatestAnalyses)
            throws NoWindException {
        long startOfRequestHandling = System.currentTimeMillis();
        LeaderboardDTO result = null;
        if (leaderboard != null) {
            result = new LeaderboardDTO(leaderboard.getScoreCorrection().getTimePointOfLastCorrectionsValidity()==null ?
                        null : leaderboard.getScoreCorrection().getTimePointOfLastCorrectionsValidity().asDate(),
                    leaderboard.getScoreCorrection()==null?null:leaderboard.getScoreCorrection().getComment(),
                            leaderboard.getScoringScheme().isHigherBetter());
            result.competitors = new ArrayList<CompetitorDTO>();
            result.name = leaderboard.getName();
            result.competitorDisplayNames = new HashMap<CompetitorDTO, String>();
            for (Competitor suppressedCompetitor : leaderboard.getSuppressedCompetitors()) {
                result.setSuppressed(convertToCompetitorDTO(suppressedCompetitor), true);
            }
            for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
                RaceColumnDTO raceColumnDTO = result.createEmptyRaceColumn(raceColumn.getName(), raceColumn.isMedalRace(),
                        leaderboard.getScoringScheme().isValidInTotalScore(leaderboard, raceColumn, timePoint));
                for (Fleet fleet : raceColumn.getFleets()) {
                    TimePoint latestTimePointAfterQueryTimePointWhenATrackedRaceWasLive = null;
                    RegattaAndRaceIdentifier raceIdentifier = null;
                    TrackedRace trackedRace = raceColumn.getTrackedRace(fleet);
                    final FleetDTO fleetDTO = convertToFleetDTO(fleet);
                    if (trackedRace != null) {
                        raceIdentifier = new RegattaNameAndRaceName(trackedRace.getTrackedRegatta().getRegatta()
                                .getName(), trackedRace.getRace().getName());
                        if (trackedRace.hasStarted(timePoint) && trackedRace.hasGPSData() && trackedRace.hasWindData()) {
                            TimePoint liveTimePointForTrackedRace = timePoint;
                            final TimePoint endOfRace = trackedRace.getEndOfRace();
                            if (endOfRace != null) {
                                liveTimePointForTrackedRace = endOfRace;
                            }
                            latestTimePointAfterQueryTimePointWhenATrackedRaceWasLive = liveTimePointForTrackedRace;
                        }
                    }
                    // Note: the RaceColumnDTO won't be created by the following addRace call because it has been created
                    // above by the result.createEmptyRaceColumn call
                    result.addRace(raceColumn.getName(), raceColumn.getExplicitFactor(), fleetDTO,
                            raceColumn.isMedalRace(), raceIdentifier, /* StrippedRaceDTO */ null);
                    if (latestTimePointAfterQueryTimePointWhenATrackedRaceWasLive != null) {
                        raceColumnDTO.setWhenLastTrackedRaceWasLive(fleetDTO, latestTimePointAfterQueryTimePointWhenATrackedRaceWasLive.asDate());
                    }
                }
                result.setCompetitorsFromBestToWorst(raceColumnDTO, getCompetitorDTOList(leaderboard.getCompetitorsFromBestToWorst(raceColumn, timePoint)));
            }
            result.setDelayToLiveInMillisForLatestRace(leaderboard.getDelayToLiveInMillis());
            result.rows = new HashMap<CompetitorDTO, LeaderboardRowDTO>();
            result.hasCarriedPoints = leaderboard.hasCarriedPoints();
            result.discardThresholds = leaderboard.getResultDiscardingRule().getDiscardIndexResultsStartingWithHowManyRaces();
            // Computing the competitor leg ranks is expensive, especially in live mode, in case new events keep invalidating
            // the ranks cache in TrackedLegImpl. Then problem then is that the sorting based on wind data is repeated for each
            // competitor, leading to square effort. We therefore need to compute the leg ranks for those race where leg details
            // are requested only once and pass them into getLeaderboardEntryDTO
            final Map<Leg, LinkedHashMap<Competitor, Integer>> legRanksCache = new HashMap<Leg, LinkedHashMap<Competitor,Integer>>();
            for (final RaceColumn raceColumn : leaderboard.getRaceColumns()) {
                // if details for the column are requested, cache the leg's ranks
                if (namesOfRaceColumnsForWhichToLoadLegDetails != null
                                        && namesOfRaceColumnsForWhichToLoadLegDetails.contains(raceColumn.getName())) {
                    for (Fleet fleet : raceColumn.getFleets()) {
                        TrackedRace trackedRace = raceColumn.getTrackedRace(fleet);
                        if (trackedRace != null) {
                            trackedRace.getRace().getCourse().lockForRead();
                            try {
                                for (TrackedLeg trackedLeg : trackedRace.getTrackedLegs()) {
                                    legRanksCache.put(trackedLeg.getLeg(), trackedLeg.getRanks(timePoint));
                                }
                            } finally {
                                trackedRace.getRace().getCourse().unlockAfterRead();
                            }
                        }
                    }
                }
            }
            for (final Competitor competitor : leaderboard.getCompetitorsFromBestToWorst(timePoint)) {
                CompetitorDTO competitorDTO = convertToCompetitorDTO(competitor);
                LeaderboardRowDTO row = new LeaderboardRowDTO();
                row.competitor = competitorDTO;
                row.fieldsByRaceColumnName = new HashMap<String, LeaderboardEntryDTO>();
                row.carriedPoints = leaderboard.hasCarriedPoints(competitor) ? leaderboard.getCarriedPoints(competitor) : null;
                addOverallDetailsToRow(leaderboard, timePoint, competitor, row);
                result.competitors.add(competitorDTO);
                Map<String, Future<LeaderboardEntryDTO>> futuresForColumnName = new HashMap<String, Future<LeaderboardEntryDTO>>();
                for (final RaceColumn raceColumn : leaderboard.getRaceColumns()) {
                    RunnableFuture<LeaderboardEntryDTO> future = new FutureTask<LeaderboardEntryDTO>(new Callable<LeaderboardEntryDTO>() {
                        @Override
                        public LeaderboardEntryDTO call() {
                            try {
                                Entry entry = leaderboard.getEntry(competitor, raceColumn, timePoint);
                                return getLeaderboardEntryDTO(entry, raceColumn.getTrackedRace(competitor), competitor, timePoint,
                                       namesOfRaceColumnsForWhichToLoadLegDetails != null
                                        && namesOfRaceColumnsForWhichToLoadLegDetails.contains(raceColumn.getName()),
                                        waitForLatestAnalyses, legRanksCache);
                            } catch (NoWindException e) {
                                logger.info("Exception trying to compute leaderboard entry for competitor "+competitor.getName()+
                                        " in race column "+raceColumn.getName()+": "+e.getMessage());
                                logger.throwing(SailingServiceImpl.class.getName(), "computeLeaderboardByName.future.call()", e);
                                throw new NoWindError(e);
                            }
                        }
                    });
                    executor.execute(future);
                    futuresForColumnName.put(raceColumn.getName(), future);
                }
                for (Map.Entry<String, Future<LeaderboardEntryDTO>> raceColumnNameAndFuture : futuresForColumnName.entrySet()) {
                    try {
                        row.fieldsByRaceColumnName.put(raceColumnNameAndFuture.getKey(), raceColumnNameAndFuture.getValue().get());
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                }
                result.rows.put(competitorDTO, row);
                String displayName = leaderboard.getDisplayName(competitor);
                if (displayName != null) {
                    result.competitorDisplayNames.put(competitorDTO, displayName);
                }
            }
        }
        logger.fine("computeLeaderboardByName("+leaderboard.getName()+", "+timePoint+", "+namesOfRaceColumnsForWhichToLoadLegDetails+") took "+
                (System.currentTimeMillis()-startOfRequestHandling)+"ms");
        return result;
    }

    private void addOverallDetailsToRow(final Leaderboard leaderboard, final TimePoint timePoint,
            final Competitor competitor, LeaderboardRowDTO row) throws NoWindException {
        final Pair<GPSFixMoving, Speed> maximumSpeedOverGround = leaderboard.getMaximumSpeedOverGround(competitor, timePoint);
        if (maximumSpeedOverGround != null && maximumSpeedOverGround.getB() != null) {
            row.maximumSpeedOverGroundInKnots = maximumSpeedOverGround.getB().getKnots();
            row.whenMaximumSpeedOverGroundWasAchieved = maximumSpeedOverGround.getA().getTimePoint().asDate();
        }
        final Long totalTimeSailedDownwindInMilliseconds = leaderboard.getTotalTimeSailedInLegTypeInMilliseconds(competitor, LegType.DOWNWIND, timePoint);
        row.totalTimeSailedDownwindInSeconds = totalTimeSailedDownwindInMilliseconds==null?null:1./1000.*totalTimeSailedDownwindInMilliseconds;
        final Long totalTimeSailedUpwindInMilliseconds = leaderboard.getTotalTimeSailedInLegTypeInMilliseconds(competitor, LegType.UPWIND, timePoint);
        row.totalTimeSailedUpwindInSeconds = totalTimeSailedUpwindInMilliseconds==null?null:1./1000.*totalTimeSailedUpwindInMilliseconds;
        final Long totalTimeSailedReachingInMilliseconds = leaderboard.getTotalTimeSailedInLegTypeInMilliseconds(competitor, LegType.REACHING, timePoint);
        row.totalTimeSailedReachingInSeconds = totalTimeSailedReachingInMilliseconds==null?null:1./1000.*totalTimeSailedReachingInMilliseconds;
        final Long totalTimeSailedInMilliseconds = leaderboard.getTotalTimeSailedInMilliseconds(competitor, timePoint);
        row.totalTimeSailedInSeconds = totalTimeSailedInMilliseconds==null?null:1./1000.*totalTimeSailedInMilliseconds;
    }
    
    private List<CompetitorDTO> getCompetitorDTOList(List<Competitor> competitors) {
        List<CompetitorDTO> result = new ArrayList<CompetitorDTO>();
        for (Competitor competitor : competitors) {
            result.add(convertToCompetitorDTO(competitor));
        }
        return result;
    }

    /**
     * @param waitForLatestAnalyses
     *            if <code>false</code>, this method is allowed to read the maneuver analysis results from a cache that
     *            may not reflect all data already received; otherwise, the method will always block for the latest
     *            cache updates to have happened before returning.
     */
    private LeaderboardEntryDTO getLeaderboardEntryDTO(Entry entry, TrackedRace trackedRace, Competitor competitor,
            TimePoint timePoint, boolean addLegDetails, boolean waitForLatestAnalyses,
            Map<Leg, LinkedHashMap<Competitor, Integer>> legRanksCache) throws NoWindException {
        LeaderboardEntryDTO entryDTO = new LeaderboardEntryDTO();
        entryDTO.race = trackedRace == null ? null : trackedRace.getRaceIdentifier();
        entryDTO.netPoints = entry.getNetPoints();
        entryDTO.netPointsCorrected = entry.isNetPointsCorrected();
        entryDTO.totalPoints = entry.getTotalPoints();
        entryDTO.reasonForMaxPoints = entry.getMaxPointsReason();
        entryDTO.discarded = entry.isDiscarded();
        if (addLegDetails && trackedRace != null) {
            try {
                RaceDetails raceDetails = getRaceDetails(trackedRace, competitor, timePoint, waitForLatestAnalyses, legRanksCache);
                entryDTO.legDetails = raceDetails.getLegDetails();
                entryDTO.windwardDistanceToOverallLeaderInMeters = raceDetails.getWindwardDistanceToOverallLeader() == null ? null
                        : raceDetails.getWindwardDistanceToOverallLeader().getMeters();
                entryDTO.averageCrossTrackErrorInMeters = raceDetails.getAverageCrossTrackError() == null ? null
                        : raceDetails.getAverageCrossTrackError().getMeters();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e); // the future used to calculate the leg details was interrupted; escalate as runtime exception
            }
        }
        final Fleet fleet = entry.getFleet();
        entryDTO.fleet = fleet == null ? null : convertToFleetDTO(fleet);
        return entryDTO;
    }
    
    private static class RaceDetails {
        private final List<LegEntryDTO> legDetails;
        private final Distance windwardDistanceToOverallLeader;
        private final Distance averageCrossTrackError;
        public RaceDetails(List<LegEntryDTO> legDetails, Distance windwardDistanceToOverallLeader,
                Distance averageCrossTrackError) {
            super();
            this.legDetails = legDetails;
            this.windwardDistanceToOverallLeader = windwardDistanceToOverallLeader;
            this.averageCrossTrackError = averageCrossTrackError;
        }
        public List<LegEntryDTO> getLegDetails() {
            return legDetails;
        }
        public Distance getWindwardDistanceToOverallLeader() {
            return windwardDistanceToOverallLeader;
        }
        public Distance getAverageCrossTrackError() {
            return averageCrossTrackError;
        }
    }

    /**
     * If <code>timePoint</code> is after the end of the race's tracking the query will be adjusted to obtain the values
     * at the end of the {@link TrackedRace#getEndOfTracking() race's tracking time}. If the time point adjusted this
     * way equals the end of the tracking time, the query results will be looked up in a cache first and if not found,
     * they will be stored to the cache after calculating them. A cache invalidation {@link RaceChangeListener listener}
     * will be registered with the race which will be triggered for any event received by the race.
     * @param waitForLatestAnalyses
     *            if <code>false</code>, this method is allowed to read the maneuver analysis results from a cache that
     *            may not reflect all data already received; otherwise, the method will always block for the latest
     *            cache updates to have happened before returning.
     */
    private RaceDetails getRaceDetails(TrackedRace trackedRace, Competitor competitor, TimePoint timePoint,
            boolean waitForLatestAnalyses, Map<Leg, LinkedHashMap<Competitor, Integer>> legRanksCache) throws NoWindException, InterruptedException, ExecutionException {
        RaceDetails raceDetails;
        if (trackedRace.getEndOfTracking() != null && trackedRace.getEndOfTracking().compareTo(timePoint) < 0) {
            raceDetails = getRaceDetailsForEndOfTrackingFromCacheOrCalculateAndCache(trackedRace, competitor, legRanksCache);
        } else {
            raceDetails = calculateRaceDetails(trackedRace, competitor, timePoint, waitForLatestAnalyses, legRanksCache);
        }
        return raceDetails;
    }

    private RaceDetails getRaceDetailsForEndOfTrackingFromCacheOrCalculateAndCache(final TrackedRace trackedRace,
            final Competitor competitor, final Map<Leg, LinkedHashMap<Competitor, Integer>> legRanksCache)
                    throws NoWindException, InterruptedException, ExecutionException {
        final Pair<TrackedRace, Competitor> key = new Pair<TrackedRace, Competitor>(trackedRace, competitor);
        RunnableFuture<RaceDetails> raceDetails;
        synchronized (raceDetailsAtEndOfTrackingCache) {
            raceDetails = raceDetailsAtEndOfTrackingCache.get(key);
            if (raceDetails == null) {
                raceDetails = new FutureTask<RaceDetails>(new Callable<RaceDetails>() {
                    @Override
                    public RaceDetails call() throws Exception {
                        TimePoint end = trackedRace.getEndOfRace();
                        if (end == null) {
                            end = trackedRace.getEndOfTracking();
                        }
                        return calculateRaceDetails(trackedRace, competitor, end,
                                /* waitForLatestManeuverAnalysis */ true /* because this is done only once after end of tracking */, legRanksCache);
                    }
                });
                raceDetailsExecutor.execute(raceDetails);
                raceDetailsAtEndOfTrackingCache.put(key, raceDetails);
                final CacheInvalidationListener cacheInvalidationListener = new CacheInvalidationListener(trackedRace,
                        competitor);
                trackedRace.addListener(cacheInvalidationListener);
                cacheInvalidationListeners.add(cacheInvalidationListener);
            }
        }
        return raceDetails.get();
    }
    
    /**
     * Handles the invalidation of the {@link SailingServiceImpl#raceDetailsAtEndOfTrackingCache} entries if the tracked race
     * changes in any way.
     * 
     * @author Axel Uhl (D043530)
     *
     */
    private class CacheInvalidationListener implements RaceChangeListener {
        private final TrackedRace trackedRace;
        private final Competitor competitor;
        
        public CacheInvalidationListener(TrackedRace trackedRace, Competitor competitor) {
            this.trackedRace = trackedRace;
            this.competitor = competitor;
        }

        public void removeFromTrackedRace() {
            trackedRace.removeListener(this);
        }

        private void invalidateCacheAndRemoveThisListenerFromTrackedRace() {
            synchronized (raceDetailsAtEndOfTrackingCache) {
                raceDetailsAtEndOfTrackingCache.remove(new Pair<TrackedRace, Competitor>(trackedRace, competitor));
                removeFromTrackedRace();
            }
        }

        @Override
        public void competitorPositionChanged(GPSFixMoving fix, Competitor competitor) {
            invalidateCacheAndRemoveThisListenerFromTrackedRace();
        }

        @Override
        public void statusChanged(TrackedRaceStatus newStatus) {
            // when the status changes away from LOADING, calculations may start or resume, making it necessary to clear the cache
            invalidateCacheAndRemoveThisListenerFromTrackedRace();
        }

        @Override
        public void markPositionChanged(GPSFix fix, Mark mark) {
            invalidateCacheAndRemoveThisListenerFromTrackedRace();
        }

        @Override
        public void markPassingReceived(Competitor competitor, Map<Waypoint, MarkPassing> oldMarkPassings,
                Iterable<MarkPassing> markPassings) {
            invalidateCacheAndRemoveThisListenerFromTrackedRace();
        }

        @Override
        public void speedAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage) {
            invalidateCacheAndRemoveThisListenerFromTrackedRace();
        }

        @Override
        public void windDataReceived(Wind wind, WindSource windSource) {
            invalidateCacheAndRemoveThisListenerFromTrackedRace();
        }

        @Override
        public void windDataRemoved(Wind wind, WindSource windSource) {
            invalidateCacheAndRemoveThisListenerFromTrackedRace();
        }

        @Override
        public void windAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage) {
            invalidateCacheAndRemoveThisListenerFromTrackedRace();
        }

        @Override
        public void raceTimesChanged(TimePoint startOfTracking, TimePoint endOfTracking, TimePoint startTimeReceived) {
            invalidateCacheAndRemoveThisListenerFromTrackedRace();
        }

        @Override
        public void delayToLiveChanged(long delayToLiveInMillis) {
            invalidateCacheAndRemoveThisListenerFromTrackedRace();
        }

        @Override
        public void windSourcesToExcludeChanged(Iterable<? extends WindSource> windSourcesToExclude) {
            invalidateCacheAndRemoveThisListenerFromTrackedRace();
        }
    }

    private RaceDetails calculateRaceDetails(TrackedRace trackedRace, Competitor competitor, TimePoint timePoint,
            boolean waitForLatestAnalyses, Map<Leg, LinkedHashMap<Competitor, Integer>> legRanksCache) throws NoWindException {
        List<LegEntryDTO> legDetails;
        legDetails = new ArrayList<LegEntryDTO>();
        final Course course = trackedRace.getRace().getCourse();
        course.lockForRead(); // hold back any course re-configurations while looping over the legs
        try {
            for (Leg leg : course.getLegs()) {
                LegEntryDTO legEntry;
                // We loop over a copy of the course's legs; during a course change, legs may become "stale," even with
                // regard to the leg/trackedLeg structures inside the tracked race which is updated by the course change
                // immediately. Make sure we're tolerant against disappearing legs! See bug 794.
                TrackedLegOfCompetitor trackedLeg = trackedRace.getTrackedLeg(competitor, leg);
                if (trackedLeg != null && trackedLeg.hasStartedLeg(timePoint)) {
                    legEntry = createLegEntry(trackedLeg, timePoint, waitForLatestAnalyses, legRanksCache);
                } else {
                    legEntry = null;
                }
                legDetails.add(legEntry);
            }
            final Distance windwardDistanceToOverallLeader = trackedRace == null ? null : trackedRace
                    .getWindwardDistanceToOverallLeader(competitor, timePoint);
            final Distance averageCrossTrackError = trackedRace == null ? null : trackedRace.getAverageCrossTrackError(
                    competitor, timePoint, waitForLatestAnalyses);
            return new RaceDetails(legDetails, windwardDistanceToOverallLeader, averageCrossTrackError);
        } finally {
            course.unlockAfterRead();
        }
    }

    private LegEntryDTO createLegEntry(TrackedLegOfCompetitor trackedLeg, TimePoint timePoint,
            boolean waitForLatestAnalyses, Map<Leg, LinkedHashMap<Competitor, Integer>> legRanksCache) throws NoWindException {
        LegEntryDTO result;
        if (trackedLeg == null) {
            result = null;
        } else {
            result = new LegEntryDTO();
            final Speed averageSpeedOverGround = trackedLeg.getAverageSpeedOverGround(timePoint);
            result.averageSpeedOverGroundInKnots = averageSpeedOverGround == null ? null : averageSpeedOverGround.getKnots();
            final Distance averageCrossTrackError = trackedLeg.getAverageCrossTrackError(timePoint, waitForLatestAnalyses);
            result.averageCrossTrackErrorInMeters = averageCrossTrackError == null ? null : averageCrossTrackError.getMeters();
            Double speedOverGroundInKnots;
            if (trackedLeg.hasFinishedLeg(timePoint))  {
                speedOverGroundInKnots = averageSpeedOverGround == null ? null : averageSpeedOverGround.getKnots();
            } else {
                final SpeedWithBearing speedOverGround = trackedLeg.getSpeedOverGround(timePoint);
                speedOverGroundInKnots = speedOverGround == null ? null : speedOverGround.getKnots();
            }
            result.currentSpeedOverGroundInKnots = speedOverGroundInKnots == null ? null : speedOverGroundInKnots;
            Distance distanceTraveled = trackedLeg.getDistanceTraveled(timePoint);
            result.distanceTraveledInMeters = distanceTraveled == null ? null : distanceTraveled.getMeters();
            result.estimatedTimeToNextWaypointInSeconds = trackedLeg.getEstimatedTimeToNextMarkInSeconds(timePoint);
            result.timeInMilliseconds = trackedLeg.getTimeInMilliSeconds(timePoint);
            result.finished = trackedLeg.hasFinishedLeg(timePoint);
            result.gapToLeaderInSeconds = trackedLeg.getGapToLeaderInSeconds(timePoint,
                    legRanksCache.get(trackedLeg.getLeg()).entrySet().iterator().next().getKey());
            if (result.gapToLeaderInSeconds != null) {
                // FIXME problem: asking just after the beginning of the leg yields very different values from asking for the end of the previous leg.
                // This is because for the previous leg it's decided based on the mark passings; for the next (current) leg it's decided based on
                // windward distance and VMG
                Double gapAtEndOfPreviousLegInSeconds = getGapAtEndOfPreviousLegInSeconds(trackedLeg);
                if (gapAtEndOfPreviousLegInSeconds != null) {
                    result.gapChangeSinceLegStartInSeconds = result.gapToLeaderInSeconds - gapAtEndOfPreviousLegInSeconds;
                }
            }
            LinkedHashMap<Competitor, Integer> legRanks = legRanksCache.get(trackedLeg.getLeg());
            if (legRanks != null) {
                result.rank = legRanks.get(trackedLeg.getCompetitor());
            } else {
                result.rank = trackedLeg.getRank(timePoint);
            }
            result.started = trackedLeg.hasStartedLeg(timePoint);
            Speed velocityMadeGood;
            if (trackedLeg.hasFinishedLeg(timePoint)) {
                velocityMadeGood = trackedLeg.getAverageVelocityMadeGood(timePoint);
            } else {
                velocityMadeGood = trackedLeg.getVelocityMadeGood(timePoint);
            }
            result.velocityMadeGoodInKnots = velocityMadeGood == null ? null : velocityMadeGood.getKnots();
            Distance windwardDistanceToGo = trackedLeg.getWindwardDistanceToGo(timePoint);
            result.windwardDistanceToGoInMeters = windwardDistanceToGo == null ? null : windwardDistanceToGo
                    .getMeters();
            final TimePoint startOfRace = trackedLeg.getTrackedLeg().getTrackedRace().getStartOfRace();
            if (startOfRace != null && trackedLeg.hasStartedLeg(timePoint)) {
                // not using trackedLeg.getManeuvers(...) because it may not catch the mark passing maneuver starting this leg
                // because that may have been detected as slightly before the mark passing time, hence associated with the previous leg
                List<Maneuver> maneuvers = trackedLeg.getTrackedLeg().getTrackedRace()
                        .getManeuvers(trackedLeg.getCompetitor(), startOfRace, timePoint, waitForLatestAnalyses);
                if (maneuvers != null) {
                    result.numberOfManeuvers = new HashMap<ManeuverType, Integer>();
                    result.numberOfManeuvers.put(ManeuverType.TACK, 0);
                    result.numberOfManeuvers.put(ManeuverType.JIBE, 0);
                    result.numberOfManeuvers.put(ManeuverType.PENALTY_CIRCLE, 0);
                    Map<ManeuverType, Double> totalManeuverLossInMeters = new HashMap<ManeuverType, Double>();
                    totalManeuverLossInMeters.put(ManeuverType.TACK, 0.0);
                    totalManeuverLossInMeters.put(ManeuverType.JIBE, 0.0);
                    totalManeuverLossInMeters.put(ManeuverType.PENALTY_CIRCLE, 0.0);
                    TimePoint startOfLeg = trackedLeg.getStartTime();
                    for (Maneuver maneuver : maneuvers) {
                        // don't count maneuvers that were in previous legs
                        switch (maneuver.getType()) {
                        case TACK:
                        case JIBE:
                        case PENALTY_CIRCLE:
                            if (!maneuver.getTimePoint().before(startOfLeg) && (!trackedLeg.hasFinishedLeg(timePoint) ||
                                    maneuver.getTimePoint().before(trackedLeg.getFinishTime()))) {
                                if (maneuver.getManeuverLoss() != null) {
                                    result.numberOfManeuvers.put(maneuver.getType(),
                                            result.numberOfManeuvers.get(maneuver.getType()) + 1);
                                    totalManeuverLossInMeters.put(maneuver.getType(),
                                            totalManeuverLossInMeters.get(maneuver.getType())
                                                    + maneuver.getManeuverLoss().getMeters());
                                }
                            }
                            break;
                        case MARK_PASSING:
                            // analyze all mark passings, not only those after this leg's start, to catch the mark passing
                            // maneuver starting this leg, even if its time point is slightly before the mark passing starting this leg
                            MarkPassingManeuver mpm = (MarkPassingManeuver) maneuver;
                            if (mpm.getWaypointPassed() == trackedLeg.getLeg().getFrom()) {
                                result.sideToWhichMarkAtLegStartWasRounded = mpm.getSide();
                            }
                            break;
                		default:
                			/* Do nothing here.
                			 * Throwing an exception destroys the toggling (and maybe other behaviour) of the leaderboard.
                			*/
                        }
                    }
                    result.averageManeuverLossInMeters = new HashMap<ManeuverType, Double>();
                    for (ManeuverType maneuverType : new ManeuverType[] { ManeuverType.TACK, ManeuverType.JIBE,
                            ManeuverType.PENALTY_CIRCLE }) {
                        if (result.numberOfManeuvers.get(maneuverType) != 0) {
                            result.averageManeuverLossInMeters.put(
                                    maneuverType,
                                    totalManeuverLossInMeters.get(maneuverType)
                                            / result.numberOfManeuvers.get(maneuverType));
                        }
                    }
                }
            }
        }
        return result;
    }

    private Double getGapAtEndOfPreviousLegInSeconds(TrackedLegOfCompetitor trackedLeg) throws NoWindException {
        final Double result;
        final Course course = trackedLeg.getTrackedLeg().getTrackedRace().getRace().getCourse();
        course.lockForRead();
        try {
            int indexOfStartWaypoint = course.getIndexOfWaypoint(trackedLeg.getLeg().getFrom());
            if (indexOfStartWaypoint == 0) {
                // trackedLeg was the first leg; gap is determined by gap of start line passing time points
                Iterable<MarkPassing> markPassingsForLegStart = trackedLeg.getTrackedLeg().getTrackedRace().getMarkPassingsInOrder(trackedLeg.getLeg().getFrom());
                trackedLeg.getTrackedLeg().getTrackedRace().lockForRead(markPassingsForLegStart);
                try {
                    final Iterator<MarkPassing> markPassingsIter = markPassingsForLegStart.iterator();
                    if (markPassingsIter.hasNext()) {
                        TimePoint firstStart = markPassingsIter.next().getTimePoint();
                        final MarkPassing markPassingForFrom = trackedLeg.getTrackedLeg().getTrackedRace()
                                .getMarkPassing(trackedLeg.getCompetitor(), trackedLeg.getLeg().getFrom());
                        if (markPassingForFrom != null) {
                            TimePoint competitorStart = markPassingForFrom.getTimePoint();
                            result = (double) (competitorStart.asMillis() - firstStart.asMillis()) / 1000.;
                        } else {
                            result = null;
                        }
                    } else {
                        result = null;
                    }
                } finally {
                    trackedLeg.getTrackedLeg().getTrackedRace().unlockAfterRead(markPassingsForLegStart);
                }
            } else {
                TrackedLeg previousTrackedLeg = trackedLeg.getTrackedLeg().getTrackedRace().getTrackedLeg(course.getLegs().get(indexOfStartWaypoint-1));
                TrackedLegOfCompetitor previousTrackedLegOfCompetitor = previousTrackedLeg.getTrackedLeg(trackedLeg.getCompetitor());
                result = previousTrackedLegOfCompetitor.getGapToLeaderInSeconds(previousTrackedLegOfCompetitor.getFinishTime());
            }
            return result;
        } finally {
            course.unlockAfterRead();
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
        MarkDTO markDTO = new MarkDTO(mark.getName(), position.getLatDeg(), position.getLngDeg());
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
            fleets.add(convertToFleetDTO(fleet));
        }
        List<RaceColumnDTO> raceColumns = new ArrayList<RaceColumnDTO>();
        for (RaceColumnInSeries raceColumn : series.getRaceColumns()) {
            RaceColumnDTO raceColumnDTO = new RaceColumnDTO(/* isValidInTotalScore not relevant here because no scores conveyed */ null);
            raceColumnDTO.name = raceColumn.getName();
            raceColumnDTO.setMedalRace(raceColumn.isMedalRace());
            raceColumnDTO.setExplicitFactor(raceColumn.getExplicitFactor());
            raceColumns.add(raceColumnDTO);
        }
        SeriesDTO result = new SeriesDTO(series.getName(), fleets, raceColumns, series.isMedal());
        return result;
    }

    private FleetDTO convertToFleetDTO(Fleet fleet) {
        return new FleetDTO(fleet.getName(), fleet.getOrdering(), fleet.getColor());
    }

    private List<RaceWithCompetitorsDTO> convertToRaceDTOs(Regatta regatta) {
        List<RaceWithCompetitorsDTO> result = new ArrayList<RaceWithCompetitorsDTO>();
        for (RaceDefinition r : regatta.getAllRaces()) {
            RegattaAndRaceIdentifier raceIdentifier = new RegattaNameAndRaceName(regatta.getName(), r.getName());
            TrackedRace trackedRace = getService().getExistingTrackedRace(raceIdentifier);
            TrackedRaceDTO trackedRaceDTO = null; 
            if (trackedRace != null) {
                trackedRaceDTO = new TrackedRaceDTO();
                trackedRaceDTO.hasWindData = trackedRace.hasWindData();
                trackedRaceDTO.hasGPSData = trackedRace.hasGPSData();
                trackedRaceDTO.startOfTracking = trackedRace.getStartOfTracking() == null ? null : trackedRace.getStartOfTracking().asDate();
                trackedRaceDTO.endOfTracking = trackedRace.getEndOfTracking() == null ? null : trackedRace.getEndOfTracking().asDate();
                trackedRaceDTO.timePointOfNewestEvent = trackedRace.getTimePointOfNewestEvent() == null ? null : trackedRace.getTimePointOfNewestEvent().asDate();
                trackedRaceDTO.delayToLiveInMs = trackedRace.getDelayToLiveInMillis(); 
            }
            RaceWithCompetitorsDTO raceDTO = new RaceWithCompetitorsDTO(raceIdentifier, convertToCompetitorDTOs(r.getCompetitors()),
                    trackedRaceDTO, getService().isRaceBeingTracked(r));
            if (trackedRace != null) {
                raceDTO.startOfRace = trackedRace.getStartOfRace() == null ? null : trackedRace.getStartOfRace().asDate();
                raceDTO.endOfRace = trackedRace.getEndOfRace() == null ? null : trackedRace.getEndOfRace().asDate();
                raceDTO.status = new RaceStatusDTO();
                raceDTO.status.status = trackedRace.getStatus().getStatus();
                raceDTO.status.loadingProgress = trackedRace.getStatus().getLoadingProgress();
            }
            raceDTO.boatClass = regatta.getBoatClass() == null ? null : regatta.getBoatClass().getName(); 
            result.add(raceDTO);
        }
        return result;
    }

    private List<CompetitorDTO> convertToCompetitorDTOs(Iterable<Competitor> competitors) {
        List<CompetitorDTO> result = new ArrayList<CompetitorDTO>();
        for (Competitor c : competitors) {
            CompetitorDTO competitorDTO = convertToCompetitorDTO(c);
            result.add(competitorDTO);
        }
        return result;
    }

    private CompetitorDTO convertToCompetitorDTO(Competitor c) {
        CompetitorDTO competitorDTO = weakCompetitorDTOCache.get(c);
        if (competitorDTO == null) {
            CountryCode countryCode = c.getTeam().getNationality().getCountryCode();
            competitorDTO = new CompetitorDTO(c.getName(), countryCode == null ? ""
                    : countryCode.getTwoLetterISOCode(),
                    countryCode == null ? "" : countryCode.getThreeLetterIOCCode(), countryCode == null ? ""
                            : countryCode.getName(), c.getBoat().getSailID(), c.getId().toString(),
                    new BoatClassDTO(c.getBoat().getBoatClass().getName(), c.getBoat().getBoatClass().getHullLength()
                            .getMeters()));
            weakCompetitorDTOCache.put(c, competitorDTO);
        }
        return competitorDTO;
    }

    @Override
    public Pair<String, List<TracTracRaceRecordDTO>> listTracTracRacesInEvent(String eventJsonURL) throws MalformedURLException, IOException,
            ParseException, org.json.simple.parser.ParseException, URISyntaxException {
        com.sap.sailing.domain.common.impl.Util.Pair<String,List<RaceRecord>> raceRecords;
        raceRecords = getService().getTracTracRaceRecords(new URL(eventJsonURL));
        List<TracTracRaceRecordDTO> result = new ArrayList<TracTracRaceRecordDTO>();
        for (RaceRecord raceRecord : raceRecords.getB()) {
            result.add(new TracTracRaceRecordDTO(raceRecord.getID(), raceRecord.getEventName(), raceRecord.getName(),
                    raceRecord.getParamURL().toString(), raceRecord.getReplayURL(), raceRecord.getLiveURI().toString(),
                    raceRecord.getStoredURI().toString(), raceRecord.getTrackingStartTime().asDate(), raceRecord
                            .getTrackingEndTime().asDate(), raceRecord.getRaceStartTime().asDate(), raceRecord.getBoatClassNames()));
        }
        return new Pair<String, List<TracTracRaceRecordDTO>>(raceRecords.getA(), result);
    }

    @Override
    public void trackWithTracTrac(RegattaIdentifier regattaToAddTo, TracTracRaceRecordDTO rr, String liveURI, String storedURI,
            boolean trackWind, final boolean correctWindByDeclination, final boolean simulateWithStartTimeNow) throws Exception {
        if (liveURI == null || liveURI.trim().length() == 0) {
            liveURI = rr.liveURI;
        }
        if (storedURI == null || storedURI.trim().length() == 0) {
            storedURI = rr.storedURI;
        }
        final RacesHandle raceHandle = getService().addTracTracRace(regattaToAddTo, new URL(rr.paramURL),
                new URI(liveURI), new URI(storedURI), new MillisecondsTimePoint(rr.trackingStartTime),
                new MillisecondsTimePoint(rr.trackingEndTime),
                MongoWindStoreFactory.INSTANCE.getMongoWindStore(mongoObjectFactory, domainObjectFactory),
                RaceTracker.TIMEOUT_FOR_RECEIVING_RACE_DEFINITION_IN_MILLISECONDS, simulateWithStartTimeNow);
        if (trackWind) {
            new Thread("Wind tracking starter for race "+rr.regattaName+"/"+rr.name) {
                public void run() {
                    try {
                        startTrackingWind(raceHandle, correctWindByDeclination, RaceTracker.TIMEOUT_FOR_RECEIVING_RACE_DEFINITION_IN_MILLISECONDS);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }.start();
        }
    }

    @Override
    public List<TracTracConfigurationDTO> getPreviousTracTracConfigurations() throws Exception {
        Iterable<TracTracConfiguration> configs = tractracDomainObjectFactory.getTracTracConfigurations();
        List<TracTracConfigurationDTO> result = new ArrayList<TracTracConfigurationDTO>();
        for (TracTracConfiguration ttConfig : configs) {
            result.add(new TracTracConfigurationDTO(ttConfig.getName(), ttConfig.getJSONURL().toString(),
                    ttConfig.getLiveDataURI().toString(), ttConfig.getStoredDataURI().toString()));
        }
        return result;
    }

    @Override
    public void storeTracTracConfiguration(String name, String jsonURL, String liveDataURI, String storedDataURI) throws Exception {
        tractracMongoObjectFactory.storeTracTracConfiguration(tractracDomainFactory.createTracTracConfiguration(name, jsonURL, liveDataURI, storedDataURI));
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
    public void stopTrackingRace(RegattaAndRaceIdentifier regattaAndRaceIdentifier) throws Exception {
        getService().apply(new StopTrackingRace(regattaAndRaceIdentifier));
    }
    
    @Override
    public void removeAndUntrackRace(RegattaAndRaceIdentifier regattatAndRaceidentifier) {
        getService().apply(new RemoveAndUntrackRace(regattatAndRaceidentifier));
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
    protected WindDTO createWindDTOFromAlreadyAveraged(Wind wind, WindTrack windTrack, TimePoint requestTimepoint) {
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
                            WindDTO windDTO = createWindDTOFromAlreadyAveraged(averagedWindWithConfidence.getObject(), windTrack, timePoint);
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
                            WindDTO windDTO = createWindDTOFromAlreadyAveraged(averagedWindWithConfidence.getObject(), windTrack, timePoint);
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
    public WindInfoForRaceDTO getAveragedWindInfo(RegattaAndRaceIdentifier raceIdentifier, Date from, Date to,
            long resolutionInMilliseconds, Collection<String> windSourceTypeNames) {
        TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
        WindInfoForRaceDTO result = null;
        if (trackedRace != null) {
            TimePoint fromTimePoint = from == null ? trackedRace.getStartOfTracking() : new MillisecondsTimePoint(from);
            TimePoint toTimePoint = to == null ? trackedRace.getEndOfRace() : new MillisecondsTimePoint(to);
            if(fromTimePoint != null && toTimePoint != null) {
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
    public RaceMapDataDTO getRaceMapData(RegattaAndRaceIdentifier raceIdentifier, Date date,
            Map<CompetitorDTO, Date> from, Map<CompetitorDTO, Date> to,
            boolean extrapolate) throws NoWindException {
        RaceMapDataDTO raceMapDataDTO = new RaceMapDataDTO();
        
        raceMapDataDTO.boatPositions = getBoatPositions(raceIdentifier, from, to, extrapolate);
        raceMapDataDTO.coursePositions = getCoursePositions(raceIdentifier, date); 
        raceMapDataDTO.quickRanks = getQuickRanks(raceIdentifier, date);
        raceMapDataDTO.boatClass = getBoatClass(raceIdentifier);
        
        return raceMapDataDTO;
    }    
    
    @Override
    public Map<CompetitorDTO, List<GPSFixDTO>> getBoatPositions(RegattaAndRaceIdentifier raceIdentifier,
            Map<CompetitorDTO, Date> from, Map<CompetitorDTO, Date> to,
            boolean extrapolate) throws NoWindException {
        Map<CompetitorDTO, List<GPSFixDTO>> result = new HashMap<CompetitorDTO, List<GPSFixDTO>>();
        TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
        if (trackedRace != null) {
            for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
                CompetitorDTO competitorDTO = convertToCompetitorDTO(competitor);
                if (from.containsKey(competitorDTO)) {
                    List<GPSFixDTO> fixesForCompetitor = new ArrayList<GPSFixDTO>();
                    result.put(competitorDTO, fixesForCompetitor);
                    GPSFixTrack<Competitor, GPSFixMoving> track = trackedRace.getTrack(competitor);
                    TimePoint fromTimePoint = new MillisecondsTimePoint(from.get(competitorDTO));
                    TimePoint toTimePointExcluding = new MillisecondsTimePoint(to.get(competitorDTO));
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
                        if(estimatedPosition != null && estimatedSpeed != null) {
                            fixes.add(new GPSFixMovingImpl(estimatedPosition, middle, estimatedSpeed));
                        }
                    }
                    Iterator<GPSFixMoving> fixIter = fixes.iterator();
                    if (fixIter.hasNext()) {
                        final WindSource windSource = new WindSourceImpl(WindSourceType.COMBINED);
                        GPSFixMoving fix = fixIter.next();
                        while (fix != null && (fix.getTimePoint().compareTo(toTimePointExcluding) < 0 ||
                                (fix.getTimePoint().equals(toTimePointExcluding) && toTimePointExcluding.equals(fromTimePoint)))) {
                            Tack tack = trackedRace.getTack(competitor, fix.getTimePoint());
                            TrackedLegOfCompetitor trackedLegOfCompetitor = trackedRace.getTrackedLeg(competitor,
                                    fix.getTimePoint());
                            LegType legType = trackedLegOfCompetitor == null ? null : trackedRace.getTrackedLeg(
                                    trackedLegOfCompetitor.getLeg()).getLegType(fix.getTimePoint());
                            Wind wind = trackedRace.getWind(fix.getPosition(),toTimePointExcluding);
                            WindDTO windDTO = createWindDTOFromAlreadyAveraged(wind, trackedRace.getOrCreateWindTrack(windSource), toTimePointExcluding);
                            GPSFixDTO fixDTO = createGPSFixDTO(fix, fix.getSpeed(), windDTO, tack, legType, /* extrapolate */
                                    false);
                            fixesForCompetitor.add(fixDTO);
                            if (fixIter.hasNext()) {
                                fix = fixIter.next();
                            } else {
                                // check if fix was at date and if extrapolation is requested
                                if (!fix.getTimePoint().equals(toTimePointExcluding) && extrapolate) {
                                    Position position = track.getEstimatedPosition(toTimePointExcluding, extrapolate);
                                    Tack tack2 = trackedRace.getTack(competitor, toTimePointExcluding);
                                    LegType legType2 = trackedLegOfCompetitor == null ? null : trackedRace
                                            .getTrackedLeg(trackedLegOfCompetitor.getLeg()).getLegType(
                                                    fix.getTimePoint());
                                    SpeedWithBearing speedWithBearing = track.getEstimatedSpeed(toTimePointExcluding);
                                    Wind wind2 = trackedRace.getWind(position, toTimePointExcluding);
                                    WindDTO windDTO2 = createWindDTOFromAlreadyAveraged(wind2, trackedRace.getOrCreateWindTrack(windSource), toTimePointExcluding);
                                    GPSFixDTO extrapolated = new GPSFixDTO(
                                            to.get(competitorDTO),
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
                    if (wayPointNumber == 1) {
                        name = "S";
                    } else if (wayPointNumber == numberOfWaypoints) {
                        name = "F";
                    }
                    markPassingTimesDTO.name = name;
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
                    legInfoDTO.name = "L" + legNumber;
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

    @Override
    public CourseDTO getCoursePositions(RegattaAndRaceIdentifier raceIdentifier, Date date) {
        CourseDTO result = new CourseDTO();
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
                if (firstWaypoint != null) {
                    result.startMarkPositions = getMarkPositionDTOs(dateAsTimePoint, trackedRace, firstWaypoint);
                }                    
                Waypoint lastWaypoint = course.getLastWaypoint();
                if (lastWaypoint != null) {
                    result.finishMarkPositions = getMarkPositionDTOs(dateAsTimePoint, trackedRace, lastWaypoint);
                }
            }
        }
        return result;
    }

    @Override
    public RaceCourseMarksDTO getRaceCourseMarks(RegattaAndRaceIdentifier raceIdentifier, Date date) {
        RaceCourseMarksDTO raceMarksDTO = new RaceCourseMarksDTO();
        TimePoint dateAsTimePoint = new MillisecondsTimePoint(date);
        TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
        if (trackedRace != null) {
            raceMarksDTO.marks = new HashSet<MarkDTO>();
            Iterable<Mark> marks = trackedRace.getMarks();
            for (Mark mark : marks) {
                GPSFixTrack<Mark, GPSFix> track = trackedRace.getOrCreateTrack(mark);
                Position positionAtDate = track.getEstimatedPosition(dateAsTimePoint, /* extrapolate */false);
                if (positionAtDate != null) {
                    raceMarksDTO.marks.add(convertToMarkDTO(mark, positionAtDate));
                }
            }
        }
        return raceMarksDTO;
    }
    
    @Override
    public List<ControlPointDTO> getRaceCourse(RegattaAndRaceIdentifier raceIdentifier, Date date) {
        List<ControlPointDTO> result = new ArrayList<ControlPointDTO>();
        if (date != null) {
            TimePoint dateAsTimePoint = new MillisecondsTimePoint(date);
            TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
            if (trackedRace != null) {
                Course course = trackedRace.getRace().getCourse();
                for (Waypoint waypoint : course.getWaypoints()) {
                    result.add(createControlPointDTO(waypoint.getControlPoint(), trackedRace, dateAsTimePoint));
                }
            }
        }
        return result;
    }

    private ControlPointDTO createControlPointDTO(ControlPoint controlPoint, TrackedRace trackedRace, TimePoint timePoint) {
        ControlPointDTO result;
        if (controlPoint instanceof Gate) {
            final Mark left = ((Gate) controlPoint).getLeft();
            final Position leftPos = trackedRace.getOrCreateTrack(left).getEstimatedPosition(timePoint, /* extrapolate */ false);
            final Mark right = ((Gate) controlPoint).getRight();
            final Position rightPos = trackedRace.getOrCreateTrack(right).getEstimatedPosition(timePoint, /* extrapolate */ false);
            result = new GateDTO(controlPoint.getName(), convertToMarkDTO(left, leftPos), convertToMarkDTO(right, rightPos)); 
        } else {
            final Position posOfFirst = trackedRace.getOrCreateTrack(controlPoint.getMarks().iterator().next()).
                    getEstimatedPosition(timePoint, /* extrapolate */ false);
            result = new MarkDTO(controlPoint.getName(), posOfFirst.getLatDeg(), posOfFirst.getLngDeg());
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
    public void updateRaceCourse(RegattaAndRaceIdentifier raceIdentifier, List<ControlPointDTO> controlPoints) {
        TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
        if (trackedRace != null) {
            Course course = trackedRace.getRace().getCourse();
            Iterable<Waypoint> waypoints = course.getWaypoints();
            List<ControlPoint> newControlPoints = new ArrayList<ControlPoint>();
            int lastMatchPosition = -1;
            for (ControlPointDTO controlPointDTO : controlPoints) {
                ControlPoint matchFromOldCourse = null;
                for (int i=lastMatchPosition+1; matchFromOldCourse == null && i<Util.size(waypoints); i++) {
                    Waypoint waypointAtI = Util.get(waypoints, i);
                    ControlPoint controlPointAtI = waypointAtI.getControlPoint();
                    if (controlPointAtI.getName().equals(controlPointDTO.name) && markNamesMatch(controlPointAtI.getMarks(), controlPointDTO.getMarks())) {
                        matchFromOldCourse = controlPointAtI;
                        newControlPoints.add(matchFromOldCourse);
                        lastMatchPosition = i;
                    }
                }
                if (matchFromOldCourse == null) {
                    // no match found; create new control point:
                    ControlPoint newControlPoint;
                    if (controlPointDTO instanceof GateDTO) {
                        GateDTO gateDTO = (GateDTO) controlPointDTO;
                        Mark left = baseDomainFactory.getOrCreateMark(gateDTO.getLeft().name);
                        Mark right = baseDomainFactory.getOrCreateMark(gateDTO.getRight().name);
                        newControlPoint = baseDomainFactory.createGate(left, right, gateDTO.name);
                    } else {
                        newControlPoint = baseDomainFactory.getOrCreateMark(controlPointDTO.name);
                    }
                    newControlPoints.add(newControlPoint);
                }
            }
            try {
                course.update(newControlPoints, baseDomainFactory);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean markNamesMatch(Iterable<Mark> marks, Iterable<MarkDTO> marksDTOs) {
        Iterator<Mark> marksIter = marks.iterator();
        Iterator<MarkDTO> markDTOsIter = marksDTOs.iterator();
        while (marksIter.hasNext() && markDTOsIter.hasNext()) {
            Mark nextMark = marksIter.next();
            MarkDTO nextMarkDTO = markDTOsIter.next();
            if (!nextMark.getName().equals(nextMarkDTO.name)) {
                return false;
            }
        }
        return marksIter.hasNext() == markDTOsIter.hasNext();
    }

    private List<PositionDTO> getMarkPositionDTOs(TimePoint timePoint, TrackedRace trackedRace, Waypoint waypoint) {
        List<PositionDTO> startMarkPositions = new ArrayList<PositionDTO>();
        for (Mark startMark : waypoint.getMarks()) {
            final Position estimatedMarkPosition = trackedRace.getOrCreateTrack(startMark)
                    .getEstimatedPosition(timePoint, /* extrapolate */false);
            if (estimatedMarkPosition != null) {
                startMarkPositions.add(new PositionDTO(estimatedMarkPosition.getLatDeg(), estimatedMarkPosition.getLngDeg()));
            }
        }
        return startMarkPositions;
    }

    private BoatClassDTO getBoatClass(RegattaAndRaceIdentifier regattaAndRaceIdentifier) {
        BoatClassDTO result = null;
        Regatta regatta = getService().getRegatta(regattaAndRaceIdentifier);
        BoatClass regattaBoatClass = regatta.getBoatClass();
        if(regattaBoatClass != null) {
            result = new BoatClassDTO(regattaBoatClass.getName(), regattaBoatClass.getHullLength().getMeters());
        }
        return result;
    }
    
    @Override
    public List<QuickRankDTO> getQuickRanks(RegattaAndRaceIdentifier raceIdentifier, Date date) throws NoWindException {
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
                        QuickRankDTO quickRankDTO = new QuickRankDTO(convertToCompetitorDTO(competitor), rank, legNumber);
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
    public List<String> getLeaderboardNames() throws Exception {
        return new ArrayList<String>(getService().getLeaderboards().keySet());
    }

    @Override
    public StrippedLeaderboardDTO createFlexibleLeaderboard(String leaderboardName, int[] discardThresholds, ScoringSchemeType scoringSchemeType) {
        return createStrippedLeaderboardDTO(getService().apply(new CreateFlexibleLeaderboard(leaderboardName, discardThresholds,
                baseDomainFactory.createScoringScheme(scoringSchemeType))), false);
    }

    @Override
    public StrippedLeaderboardDTO createRegattaLeaderboard(RegattaIdentifier regattaIdentifier, int[] discardThresholds) {
        return createStrippedLeaderboardDTO(getService().apply(new CreateRegattaLeaderboard(regattaIdentifier, discardThresholds)), false);
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
    public List<StrippedLeaderboardDTO> getLeaderboardsByEvent(RegattaDTO regatta) {
        List<StrippedLeaderboardDTO> results = new ArrayList<StrippedLeaderboardDTO>();
        for (RaceDTO race : regatta.races) {
            List<StrippedLeaderboardDTO> leaderboard = getLeaderboardsByRace(race);
            if (leaderboard != null && !leaderboard.isEmpty()) {
                results.addAll(leaderboard);
            }
        }
        // Removing duplicates
        HashSet<StrippedLeaderboardDTO> set = new HashSet<StrippedLeaderboardDTO>(results);
        results.clear();
        results.addAll(set);
        return results;
    }
    
    @Override
    public List<StrippedLeaderboardDTO> getLeaderboardsByRace(RaceDTO race) {
        List<StrippedLeaderboardDTO> results = new ArrayList<StrippedLeaderboardDTO>();
        Map<String, Leaderboard> leaderboards = getService().getLeaderboards();
        for (Leaderboard leaderboard : leaderboards.values()) {
            Iterable<RaceColumn> races = leaderboard.getRaceColumns();
            for (RaceColumn raceInLeaderboard : races) {
                for (Fleet fleet : raceInLeaderboard.getFleets()) {
                    TrackedRace trackedRace = raceInLeaderboard.getTrackedRace(fleet);
                    if (trackedRace != null) {
                        RaceDefinition trackedRaceDef = trackedRace.getRace();
                        if (trackedRaceDef.getName().equals(race.name)) {
                            results.add(createStrippedLeaderboardDTO(leaderboard, false));
                            break;
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
        TimePoint now = MillisecondsTimePoint.now();
        Long delayToLiveInMillisForLatestRace = null;
        leaderboardDTO.name = leaderboard.getName();
        leaderboardDTO.competitorDisplayNames = new HashMap<CompetitorDTO, String>();
        leaderboardDTO.isMetaLeaderboard = leaderboard instanceof MetaLeaderboard ? true : false;
        if (leaderboard instanceof RegattaLeaderboard) {
            RegattaLeaderboard regattaLeaderboard = (RegattaLeaderboard) leaderboard;
            Regatta regatta = regattaLeaderboard.getRegatta();
            leaderboardDTO.regattaName = regatta.getName(); 
            leaderboardDTO.isRegattaLeaderboard = true;
            leaderboardDTO.scoringScheme = regatta.getScoringScheme().getType();
        } else {
            leaderboardDTO.isRegattaLeaderboard = false;
            leaderboardDTO.scoringScheme = leaderboard.getScoringScheme().getType();
        }
        leaderboardDTO.setDelayToLiveInMillisForLatestRace(delayToLiveInMillisForLatestRace);
        leaderboardDTO.hasCarriedPoints = leaderboard.hasCarriedPoints();
        leaderboardDTO.discardThresholds = leaderboard.getResultDiscardingRule().getDiscardIndexResultsStartingWithHowManyRaces();
        for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
            for (Fleet fleet : raceColumn.getFleets()) {
                TimePoint latestTimePointAfterQueryTimePointWhenATrackedRaceWasLive = null;
                RaceDTO raceDTO = null;
                RegattaAndRaceIdentifier raceIdentifier = null;
                TrackedRace trackedRace = raceColumn.getTrackedRace(fleet);
                if (trackedRace != null) {
                    if (startOfLatestRace == null || (trackedRace.getStartOfRace() != null && trackedRace.getStartOfRace().compareTo(startOfLatestRace) > 0)) {
                        delayToLiveInMillisForLatestRace = trackedRace.getDelayToLiveInMillis();
                    }
                    raceIdentifier = new RegattaNameAndRaceName(trackedRace.getTrackedRegatta().getRegatta().getName(), trackedRace.getRace().getName());
                    // Optional: Getting the places of the race
                    PlacemarkOrderDTO racePlaces = withGeoLocationData ? getRacePlaces(trackedRace) : null;

                    TrackedRaceDTO trackedRaceDTO = new TrackedRaceDTO();
                    trackedRaceDTO.startOfTracking = trackedRace.getStartOfTracking() == null ? null : trackedRace.getStartOfTracking().asDate();
                    trackedRaceDTO.hasWindData = trackedRace.hasWindData();
                    trackedRaceDTO.hasGPSData = trackedRace.hasGPSData();
                    raceDTO = new RaceDTO(raceIdentifier, trackedRaceDTO, getService().isRaceBeingTracked(trackedRace.getRace()));
                    raceDTO.places = racePlaces;
                    raceDTO.startOfRace = trackedRace.getStartOfRace() == null ? null : trackedRace.getStartOfRace().asDate();
                    raceDTO.endOfRace = trackedRace.getEndOfRace() == null ? null : trackedRace.getEndOfRace().asDate();
                    if (trackedRace.hasStarted(now) && trackedRace.hasGPSData() && trackedRace.hasWindData()) {
                        TimePoint liveTimePointForTrackedRace = now;
                        final TimePoint endOfRace = trackedRace.getEndOfRace();
                        if (endOfRace != null) {
                            liveTimePointForTrackedRace = endOfRace;
                        }
                        latestTimePointAfterQueryTimePointWhenATrackedRaceWasLive = liveTimePointForTrackedRace;
                    }
                }    
                final FleetDTO fleetDTO = convertToFleetDTO(fleet);
                RaceColumnDTO raceColumnDTO = leaderboardDTO.addRace(raceColumn.getName(), raceColumn.getExplicitFactor(), fleetDTO,
                        raceColumn.isMedalRace(), raceIdentifier, raceDTO);
                if (latestTimePointAfterQueryTimePointWhenATrackedRaceWasLive != null) {
                    raceColumnDTO.setWhenLastTrackedRaceWasLive(fleetDTO, latestTimePointAfterQueryTimePointWhenATrackedRaceWasLive.asDate());
                }
            }
        }
        return leaderboardDTO;
    }

    private PlacemarkOrderDTO getRacePlaces(TrackedRace trackedRace) {
        Pair<Placemark, Placemark> startAndFinish = getStartFinishPlacemarksForTrackedRace(trackedRace);
        PlacemarkOrderDTO racePlaces = new PlacemarkOrderDTO();
        if (startAndFinish.getA() != null) {
            racePlaces.getPlacemarks().add(convertToPlacemarkDTO(startAndFinish.getA()));
        }
        if (startAndFinish.getB() != null) {
            racePlaces.getPlacemarks().add(convertToPlacemarkDTO(startAndFinish.getB()));
        }
        if (racePlaces.isEmpty()) {
            racePlaces = null;
        }
        return racePlaces;
    }
    
    private Pair<Placemark, Placemark> getStartFinishPlacemarksForTrackedRace(TrackedRace race) {
        double radiusCalculationFactor = 10.0;
        Placemark startBest = null;
        Placemark finishBest = null;

        // Get start postition
        Iterator<Mark> startMarks = race.getRace().getCourse().getFirstWaypoint().getMarks().iterator();
        GPSFix startMarkFix = startMarks.hasNext() ? race.getOrCreateTrack(startMarks.next()).getLastRawFix() : null;
        Position startPosition = startMarkFix != null ? startMarkFix.getPosition() : null;
        if (startPosition != null) {
            try {
                // Get distance to nearest placemark and calculate the search radius
                Placemark startNearest = ReverseGeocoder.INSTANCE.getPlacemarkNearest(startPosition);
                if (startNearest != null) {
                    Distance startNearestDistance = startNearest.distanceFrom(startPosition);
                    double startRadius = startNearestDistance.getKilometers() * radiusCalculationFactor;

                    // Get the estimated best start place
                    startBest = ReverseGeocoder.INSTANCE.getPlacemarkLast(startPosition, startRadius,
                            new Placemark.ByPopulationDistanceRatio(startPosition));
                }
            } catch (IOException e) {
                logger.throwing(TrackedRaceImpl.class.getName(), "getPlaceOrder()", e);
            } catch (org.json.simple.parser.ParseException e) {
                logger.throwing(TrackedRaceImpl.class.getName(), "getPlaceOrder()", e);
            }
        }

        // Get finish position
        Iterator<Mark> finishMarks = race.getRace().getCourse().getFirstWaypoint().getMarks().iterator();
        GPSFix finishMarkFix = finishMarks.hasNext() ? race.getOrCreateTrack(finishMarks.next()).getLastRawFix() : null;
        Position finishPosition = finishMarkFix != null ? finishMarkFix.getPosition() : null;
        if (startPosition != null && finishPosition != null) {
            if (startPosition.getDistance(finishPosition).getKilometers() <= ReverseGeocoder.POSITION_CACHE_DISTANCE_LIMIT_IN_KM) {
                finishBest = startBest;
            } else {
                try {
                    // Get distance to nearest placemark and calculate the search radius
                    Placemark finishNearest = ReverseGeocoder.INSTANCE.getPlacemarkNearest(finishPosition);
                    Distance finishNearestDistance = finishNearest.distanceFrom(finishPosition);
                    double finishRadius = finishNearestDistance.getKilometers() * radiusCalculationFactor;

                    // Get the estimated best finish place
                    finishBest = ReverseGeocoder.INSTANCE.getPlacemarkLast(finishPosition, finishRadius,
                            new Placemark.ByPopulationDistanceRatio(finishPosition));
                } catch (IOException e) {
                    logger.throwing(TrackedRaceImpl.class.getName(), "getPlaceOrder()", e);
                } catch (org.json.simple.parser.ParseException e) {
                    logger.throwing(TrackedRaceImpl.class.getName(), "getPlaceOrder()", e);
                }
            }
        }
        Pair<Placemark, Placemark> placemarks = new Pair<Placemark, Placemark>(startBest, finishBest);
        return placemarks;
    }
    
    @Override
    public void updateLeaderboard(String leaderboardName, String newLeaderboardName, int[] newDiscardingThresholds) {
        getService().apply(new UpdateLeaderboard(leaderboardName, newLeaderboardName, newDiscardingThresholds));
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
        for (com.sap.sailing.domain.swisstimingadapter.RaceRecord rr : getService().getSwissTimingRaceRecords(hostname, port, canSendRequests)) {
            result.add(new SwissTimingRaceRecordDTO(rr.getRaceID(), rr.getDescription(), rr.getStartTime()));
        }
        return result;
    }

    @Override
    public void storeSwissTimingConfiguration(String configName, String hostname, int port, boolean canSendRequests) {
        swissTimingAdapterPersistence.storeSwissTimingConfiguration(swissTimingFactory.createSwissTimingConfiguration(configName, hostname, port, canSendRequests));
   }

    @Override
    public void trackWithSwissTiming(RegattaIdentifier regattaToAddTo, SwissTimingRaceRecordDTO rr, String hostname, int port,
            boolean canSendRequests, boolean trackWind, final boolean correctWindByDeclination) throws Exception {
        final RacesHandle raceHandle = getService().addSwissTimingRace(regattaToAddTo, rr.ID, hostname, port,
                canSendRequests,
                MongoWindStoreFactory.INSTANCE.getMongoWindStore(mongoObjectFactory, domainObjectFactory),
                RaceTracker.TIMEOUT_FOR_RECEIVING_RACE_DEFINITION_IN_MILLISECONDS);
        if (trackWind) {
            new Thread("Wind tracking starter for race "+rr.ID+"/"+rr.description) {
                public void run() {
                    try {
                        startTrackingWind(raceHandle, correctWindByDeclination, RaceTracker.TIMEOUT_FOR_RECEIVING_RACE_DEFINITION_IN_MILLISECONDS);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }.start();
        }
    }

    @Override
    public void sendSwissTimingDummyRace(String racMessage, String stlMesssage, String ccgMessage) {
        getService().storeSwissTimingDummyRace(racMessage,stlMesssage,ccgMessage);
    }

    @Override
    public List<SwissTimingReplayRaceDTO> listSwissTiminigReplayRaces(String swissTimingUrl) {
        List<SwissTimingReplayRace> replayRaces = SwissTimingReplayServiceFactory.INSTANCE
                .createSwissTimingReplayService().listReplayRaces(swissTimingUrl);
        List<SwissTimingReplayRaceDTO> result = new ArrayList<SwissTimingReplayRaceDTO>(replayRaces.size()); 
        for (SwissTimingReplayRace replayRace : replayRaces) {
            result.add(new SwissTimingReplayRaceDTO(replayRace.getFlightNumber(), replayRace.getRaceId(), replayRace.getRsc(), replayRace.getName(), replayRace.getBoatClass(), replayRace.getStartTime(), replayRace.getLink()));
        }
        return result;
    }
    
    @Override
    public void replaySwissTimingRace(RegattaIdentifier regattaIdentifier, SwissTimingReplayRaceDTO replayRaceDTO,
            boolean trackWind, boolean correctWindByDeclination, boolean simulateWithStartTimeNow) {
        Regatta regatta;
        if (regattaIdentifier == null) {
            String boatClass = replayRaceDTO.boat_class;
            for (String genderIndicator : new String[] { "Man", "Woman", "Men", "Women", "M", "W" }) {
                Pattern p = Pattern.compile("(( - )|-| )"+genderIndicator+"$");
                Matcher m = p.matcher(boatClass.trim());
                if (m.find()) {
                    boatClass = boatClass.trim().substring(0, m.start(1));
                    break;
                }
            }
            regatta = getService().createRegatta(replayRaceDTO.rsc, boatClass.trim(),
                    RegattaImpl.getFullName(replayRaceDTO.rsc, replayRaceDTO.boat_class),
                    Collections.singletonList(new SeriesImpl("Default", /* isMedal */false, Collections
                                    .singletonList(new FleetImpl("Default")), /* race column names */new ArrayList<String>(),
                                    getService())),
                    false, baseDomainFactory.createScoringScheme(ScoringSchemeType.LOW_POINT));
        } else {
            regatta = getService().getRegatta(regattaIdentifier);
        }
        SwissTimingReplayService replayService = SwissTimingReplayServiceFactory.INSTANCE.createSwissTimingReplayService();
        replayService.loadRaceData(replayRaceDTO.link, getService().getSwissTimingDomainFactory(), regatta, getService());
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
    private Competitor getCompetitorById(Iterable<Competitor> competitors, String id) {
        for (Competitor c : competitors) {
            if (c.getId().toString().equals(id)) {
                return c;
            }
        }
        return null;
    }
    
    private Double getCompetitorRaceDataEntry(DetailType dataType, TrackedRace trackedRace, Competitor competitor,
            TimePoint timePoint) throws NoWindException {
        Double result = null;
        Course course = trackedRace.getRace().getCourse();
        course.lockForRead(); // make sure the tracked leg survives this call even if a course update is pending
        try {
            TrackedLegOfCompetitor trackedLeg = trackedRace.getTrackedLeg(competitor, timePoint);
            if (trackedLeg != null) {
                switch (dataType) {
                case CURRENT_SPEED_OVER_GROUND_IN_KNOTS:
                    SpeedWithBearing speedOverGround = trackedLeg.getSpeedOverGround(timePoint);
                    result = (speedOverGround == null) ? null : speedOverGround.getKnots();
                    break;
                case VELOCITY_MADE_GOOD_IN_KNOTS:
                    Speed velocityMadeGood = trackedLeg.getVelocityMadeGood(timePoint);
                    result = (velocityMadeGood == null) ? null : velocityMadeGood.getKnots();
                    break;
                case DISTANCE_TRAVELED:
                    Distance distanceTraveled = trackedRace.getDistanceTraveled(competitor, timePoint);
                    result = distanceTraveled == null ? null : distanceTraveled.getMeters();
                    break;
                case GAP_TO_LEADER_IN_SECONDS:
                    result = trackedLeg.getGapToLeaderInSeconds(timePoint);
                    break;
                case WINDWARD_DISTANCE_TO_OVERALL_LEADER:
                    Distance distanceToLeader = trackedLeg.getWindwardDistanceToOverallLeader(timePoint);
                    result = (distanceToLeader == null) ? null : distanceToLeader.getMeters();
                    break;
                case RACE_RANK:
                    result = (double) trackedLeg.getRank(timePoint);
                    break;
        		default:
        			throw new UnsupportedOperationException("Theres currently no support for the enum value '" + dataType + "' in this method.");
                }
            }
            return result;
        } finally {
            course.unlockAfterRead();
        }
    }
    
    @Override
    public CompetitorsRaceDataDTO getCompetitorsRaceData(RegattaAndRaceIdentifier race, List<CompetitorDTO> competitors, Date from, Date to,
            long stepSizeInMs, DetailType detailType) throws NoWindException {
        CompetitorsRaceDataDTO result = null;
        TrackedRace trackedRace = getExistingTrackedRace(race);
        if (trackedRace != null) {
            TimePoint newestEvent = trackedRace.getTimePointOfNewestEvent();
            TimePoint startTime = from == null ? trackedRace.getStartOfTracking() : new MillisecondsTimePoint(from);
            TimePoint endTime = (to == null || to.after(newestEvent.asDate())) ? newestEvent : new MillisecondsTimePoint(to);
            result = new CompetitorsRaceDataDTO(detailType, startTime==null?null:startTime.asDate(), endTime==null?null:endTime.asDate());

            for (CompetitorDTO competitorDTO : competitors) {
                Competitor competitor = getCompetitorById(trackedRace.getRace().getCompetitors(), competitorDTO.id);
                ArrayList<Triple<String, Date, Double>> markPassingsData = new ArrayList<Triple<String, Date, Double>>();
                ArrayList<Pair<Date, Double>> raceData = new ArrayList<Pair<Date, Double>>();
                // Filling the mark passings
                Set<MarkPassing> competitorMarkPassings = trackedRace.getMarkPassings(competitor);
                if (competitorMarkPassings != null) {
                    trackedRace.lockForRead(competitorMarkPassings);
                    try {
                        for (MarkPassing markPassing : competitorMarkPassings) {
                            MillisecondsTimePoint time = new MillisecondsTimePoint(markPassing.getTimePoint().asMillis());
                            markPassingsData.add(new Triple<String, Date, Double>(markPassing.getWaypoint().getName(),
                                    time.asDate(),
                                    getCompetitorRaceDataEntry(detailType, trackedRace, competitor, time)));
                        }
                    } finally {
                        trackedRace.unlockAfterRead(competitorMarkPassings);
                    }
                }
                if (startTime != null && endTime != null) {
                    for (long i = startTime.asMillis(); i <= endTime.asMillis(); i += stepSizeInMs) {
                        MillisecondsTimePoint time = new MillisecondsTimePoint(i);
                        raceData.add(new Pair<Date, Double>(time.asDate(), getCompetitorRaceDataEntry(detailType, trackedRace,
                                competitor, time)));
                    }
                }
                result.setCompetitorData(competitorDTO, new CompetitorRaceDataDTO(competitorDTO, detailType, markPassingsData, raceData));
            }
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
            final WindSource windSource = new WindSourceImpl(WindSourceType.COMBINED);
            MeterDistance maxDistance = new MeterDistance(meters);
            for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
                CompetitorDTO competitorDTO = convertToCompetitorDTO(competitor);
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
                        WindDTO windDTO = createWindDTOFromAlreadyAveraged(wind, trackedRace.getOrCreateWindTrack(windSource), fix.getTimePoint());
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
                CompetitorDTO competitorDTO = convertToCompetitorDTO(competitor);
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
    public TrackedRace getTrackedRace(RegattaAndRaceIdentifier regattaNameAndRaceName) {
        Regatta regatta = getService().getRegattaByName(regattaNameAndRaceName.getRegattaName());
        RaceDefinition race = getRaceByName(regatta, regattaNameAndRaceName.getRaceName());
        TrackedRace trackedRace = getService().getOrCreateTrackedRegatta(regatta).getTrackedRace(race);
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
        groupDTO.name = leaderboardGroup.getName();
        groupDTO.description = leaderboardGroup.getDescription();
        groupDTO.displayLeaderboardsInReverseOrder = leaderboardGroup.isDisplayGroupsInReverseOrder();
        for (Leaderboard leaderboard : leaderboardGroup.getLeaderboards()) {
            groupDTO.leaderboards.add(createStrippedLeaderboardDTO(leaderboard, withGeoLocationData));
        }
        Leaderboard overallLeaderboard = leaderboardGroup.getOverallLeaderboard();
        if (overallLeaderboard != null) {
            groupDTO.setOverallLeaderboardDiscardThresholds(overallLeaderboard.getResultDiscardingRule().getDiscardIndexResultsStartingWithHowManyRaces());
            groupDTO.setOverallLeaderboardScoringSchemeType(overallLeaderboard.getScoringScheme().getType());
        }
        return groupDTO;
    }
    
    
    private PlacemarkDTO convertToPlacemarkDTO(Placemark placemark) {
        Position position = placemark.getPosition();
        return new PlacemarkDTO(placemark.getName(), placemark.getCountryCode(), new PositionDTO(position.getLatDeg(),
                position.getLngDeg()), placemark.getPopulation());
    }

    @Override
    public void renameLeaderboardGroup(String oldName, String newName) {
        getService().apply(new RenameLeaderboardGroup(oldName, newName));
    }

    @Override
    public void removeLeaderboardGroup(String groupName) {
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
            replicaDTOs.add(new ReplicaDTO(replicaDescriptor.getIpAddress().getHostName(), replicaDescriptor.getRegistrationTime().asDate(),
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
        return new ReplicationStateDTO(master, replicaDTOs);
    }

    @Override
    public void startReplicatingFromMaster(String masterName, String exchangeName, int servletPort, int messagingPort) throws IOException, ClassNotFoundException {
        getReplicationService().startToReplicateFrom(
                ReplicationFactory.INSTANCE.createReplicationMasterDescriptor(masterName, exchangeName, servletPort, messagingPort));
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
    public void updateEvent(String eventName, Serializable id, VenueDTO venue, String publicationUrl, boolean isPublic, List<String> regattaNames) {
        getService().apply(new UpdateEvent(id, eventName, venue.name, publicationUrl, isPublic, regattaNames));
    }

    @Override
    public EventDTO createEvent(String eventName, String venue, String publicationUrl, boolean isPublic) {
        return convertToEventDTO(getService().apply(new CreateEvent(eventName, venue, publicationUrl, isPublic, new ArrayList<String>())));
    }

    @Override
    public void removeEvent(String eventName) {
        getService().apply(new RemoveEvent(eventName));
    }

    @Override
    public void renameEvent(String oldName, String newName) {
        getService().apply(new RenameEvent(oldName, newName));
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
    
    private EventDTO convertToEventDTO(Event event) {
        EventDTO eventDTO = new EventDTO(event.getName());
        eventDTO.venue = new VenueDTO();
        eventDTO.venue.name = event.getVenue() != null ? event.getVenue().getName() : null;
        eventDTO.publicationUrl = event.getPublicationUrl();
        eventDTO.isPublic = event.isPublic();
        eventDTO.id = event.getId();
        eventDTO.regattas = new ArrayList<RegattaDTO>();
        for (Regatta regatta: event.getRegattas()) {
            RegattaDTO regattaDTO = new RegattaDTO();
            regattaDTO.name = regatta.getName();
            eventDTO.regattas.add(regattaDTO);
        }
        return eventDTO;
    }

    @Override
    public void removeRegatta(RegattaIdentifier regattaIdentifier) {
        getService().apply(new RemoveRegatta(regattaIdentifier));
    }

    private RaceColumnInSeriesDTO convertToRaceColumnInSeriesDTO(RaceColumnInSeries raceColumnInSeries) {
        RaceColumnInSeriesDTO raceColumnInSeriesDTO = new RaceColumnInSeriesDTO(raceColumnInSeries.getSeries().getName(),
                raceColumnInSeries.getRegatta().getName());
        raceColumnInSeriesDTO.name = raceColumnInSeries.getName();
        raceColumnInSeriesDTO.setMedalRace(raceColumnInSeries.isMedalRace());
        return raceColumnInSeriesDTO;
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
            LinkedHashMap<String, Pair<List<Triple<String, Integer, Color>>, Boolean>> seriesNamesWithFleetNamesAndFleetOrderingAndMedal,
            boolean persistent, ScoringSchemeType scoringSchemeType) {
        Regatta regatta = getService().apply(
                new AddSpecificRegatta(regattaName, boatClassName, UUID.randomUUID(),
                        seriesNamesWithFleetNamesAndFleetOrderingAndMedal,
                        persistent, baseDomainFactory.createScoringScheme(scoringSchemeType)));
        return convertToRegattaDTO(regatta);
    }

    @Override
    public RegattaScoreCorrectionDTO getScoreCorrections(String scoreCorrectionProviderName, String eventName,
            String boatClassName, Date timePointWhenResultPublished) throws Exception {
        RegattaScoreCorrectionDTO result = null;
        for (ScoreCorrectionProvider scp : getScoreCorrectionProviders()) {
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
    
    private FregResultProvider getFregService() {
        FregResultProvider result = null;
        for (ScoreCorrectionProvider scp : getScoreCorrectionProviders()) {
            if (scp instanceof FregResultProvider) {
                result = (FregResultProvider) scp;
                break;
            }
        }
        return result;
    }

    @Override
    public List<String> getFregResultUrls() {
        List<String> result = new ArrayList<String>();
        final FregResultProvider fregService = getFregService();
        if (fregService != null) {
            Iterable<URL> allUrls = fregService.getAllUrls();
            for (URL url : allUrls) {
                result.add(url.toString());
            }
        }
        return result;
    }

    @Override
    public void removeFregURLs(Set<String> toRemove) throws Exception {
        FregResultProvider fregService = getFregService();
        if (fregService != null) {
            for (String urlToRemove : toRemove) {
                fregService.removeResultUrl(new URL(urlToRemove));
            }
        }
    }

    @Override
    public void addFragUrl(String result) throws Exception {
        FregResultProvider fregService = getFregService();
        if (fregService != null) {
            fregService.registerResultUrl(new URL(result));
        }
    }

    @Override
    public List<Pair<String, List<CompetitorDTO>>> getRankedCompetitorsFromBestToWorstAfterEachRaceColumn(String leaderboardName, Date date) throws NoWindException {
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        final TimePoint timePoint;
        if (date == null) {
            final TimePoint nowMinusDelay = leaderboard.getNowMinusDelay();
            final TimePoint timePointOfLatestModification = leaderboard.getTimePointOfLatestModification();
            if (timePointOfLatestModification != null && !nowMinusDelay.before(timePointOfLatestModification)) {
                timePoint = timePointOfLatestModification;
            } else {
                timePoint = nowMinusDelay;
            }
        } else {
            timePoint = new MillisecondsTimePoint(date);
        }
        Map<RaceColumn, List<Competitor>> preResult = leaderboard
                .getRankedCompetitorsFromBestToWorstAfterEachRaceColumn(timePoint);
        List<Pair<String, List<CompetitorDTO>>> result = new ArrayList<Util.Pair<String,List<CompetitorDTO>>>();
        for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
            List<CompetitorDTO> competitorList = getCompetitorDTOList(preResult.get(raceColumn));
            result.add(new Pair<String, List<CompetitorDTO>>(raceColumn.getName(), competitorList));
        }
        return result;
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
}