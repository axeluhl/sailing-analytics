package com.sap.sailing.domain.yellowbrickadapter.impl;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorAndBoatStore;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Sideline;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.base.impl.DynamicBoat;
import com.sap.sailing.domain.base.impl.DynamicCompetitorWithBoat;
import com.sap.sailing.domain.base.impl.DynamicPerson;
import com.sap.sailing.domain.base.impl.DynamicTeam;
import com.sap.sailing.domain.base.impl.PersonImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.domain.common.TrackedRaceStatusEnum;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.common.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.leaderboard.LeaderboardGroupResolver;
import com.sap.sailing.domain.racelog.RaceLogAndTrackedRaceResolver;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.regattalog.RegattaLogStore;
import com.sap.sailing.domain.tracking.AbstractRaceTrackerImpl;
import com.sap.sailing.domain.tracking.DynamicRaceDefinitionSet;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.RaceHandle;
import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sailing.domain.tracking.RaceTrackingHandler;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;
import com.sap.sailing.domain.tracking.TrackingDataLoader;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.impl.TrackedRaceStatusImpl;
import com.sap.sailing.domain.tracking.impl.TrackingConnectorInfoImpl;
import com.sap.sailing.domain.yellowbrickadapter.YellowBrickRace;
import com.sap.sailing.domain.yellowbrickadapter.YellowBrickRaceTrackingConnectivityParams;
import com.sap.sailing.domain.yellowbrickadapter.YellowBrickTrackingAdapter;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.util.ThreadPoolUtil;

public class YellowBrickRaceTrackerImpl extends AbstractRaceTrackerImpl<YellowBrickRaceTrackingConnectivityParams>
implements TrackingDataLoader {
    private static final Logger logger = Logger.getLogger(YellowBrickRaceTrackerImpl.class.getName());
    private final String DEFAULT_REGATTA_NAME_PREFIX = "YellowBrick ";
    private final Regatta regatta;
    private final RaceDefinition race;
    private final WindStore windStore;
    private final TrackedRegattaRegistry trackedRegattaRegistry;
    private final YellowBrickTrackingAdapter trackingAdapter;
    private final DynamicTrackedRace trackedRace;
    
    /**
     * Whether the tracking process shall stop; this will terminate the periodic polling task by letting it throw an
     * exception.
     */
    private volatile boolean stop;
    
    /**
     * Keys are from {@link TeamPositions#getDeviceSerialNumber()}.
     */
    private final Map<Integer, TimePoint> timePointOfLastFixPerDeviceSerialNumber;
    
    private final Map<Integer, Competitor> competitorByDeviceSerialNumber;
    
    public YellowBrickRaceTrackerImpl(YellowBrickRaceTrackingConnectivityParams connectivityParams, Regatta regatta,
            TrackedRegattaRegistry trackedRegattaRegistry, WindStore windStore,
            RaceLogAndTrackedRaceResolver raceLogResolver, LeaderboardGroupResolver leaderboardGroupResolver,
            long timeoutInMilliseconds, RaceTrackingHandler raceTrackingHandler, RaceLogStore raceLogStore,
            RegattaLogStore regattaLogStore, DomainFactory baseDomainFactory,
            YellowBrickTrackingAdapter yellowBrickTrackingAdapter) throws IOException, ParseException {
        super(connectivityParams);
        this.timePointOfLastFixPerDeviceSerialNumber = new HashMap<>();
        this.competitorByDeviceSerialNumber = new HashMap<>();
        this.trackingAdapter = yellowBrickTrackingAdapter;
        this.windStore = windStore;
        this.trackedRegattaRegistry = trackedRegattaRegistry;
        this.regatta = getOrCreateEffectiveRegatta(DEFAULT_REGATTA_NAME_PREFIX+connectivityParams.getRaceUrl(), trackedRegattaRegistry, regatta);
        this.race = createRaceDefinition(this.regatta, yellowBrickTrackingAdapter, raceTrackingHandler,
                baseDomainFactory.getCompetitorAndBoatStore());
        this.trackedRace = raceTrackingHandler.createTrackedRace(getTrackedRegatta(), race, Collections.<Sideline> emptyList(), windStore,
                getConnectivityParams().getDelayToLiveInMillis(),
                WindTrack.DEFAULT_MILLISECONDS_OVER_WHICH_TO_AVERAGE_WIND,
                /* time over which to average speed */ race.getBoatClass().getApproximateManeuverDurationInMilliseconds(),
                new DynamicRaceDefinitionSet() {
                    @Override
                    public void addRaceDefinition(RaceDefinition race, DynamicTrackedRace trackedRace) {
                        // we already know our single RaceDefinition
                        assert YellowBrickRaceTrackerImpl.this.race == race;
                    }
                }, /* useInternalMarkPassingAlgorithm */ true, raceLogResolver,
                /* Not needed because the RaceTracker is not active on a replica */ Optional.empty(),
                new TrackingConnectorInfoImpl(YellowBrickTrackingAdapter.NAME, "https://www.ybtracking.com/", /* TODO any default YB tracker URL? */ null));
        loadStoredData();
        schedulePeriodicPollingTask();
    }
    
    private void loadStoredData() throws MalformedURLException, IOException, ParseException {
        trackedRace.onStatusChanged(this, new TrackedRaceStatusImpl(TrackedRaceStatusEnum.LOADING, 0.0));
        final PositionsDocument storedData = trackingAdapter.getStoredData(getConnectivityParams().getRaceUrl(),
                Optional.ofNullable(getConnectivityParams().getUsername()),
                Optional.ofNullable(getConnectivityParams().getPassword()));
        insertFixesIntoTrackedRace(storedData, Optional.of(progress->
                trackedRace.onStatusChanged(this, new TrackedRaceStatusImpl(TrackedRaceStatusEnum.LOADING, progress))));
        trackedRace.onStatusChanged(this, new TrackedRaceStatusImpl(TrackedRaceStatusEnum.TRACKING, 1.0));
    }

    private void insertFixesIntoTrackedRace(final PositionsDocument storedData, Optional<Consumer<Double>> progressCallback) {
        final int numberOfTeams = Util.size(storedData.getTeams());
        final int[] i = new int[1];
        for (final TeamPositions teamPositions : storedData.getTeams()) {
            progressCallback.ifPresent(cb->cb.accept((double) i[0]++ / numberOfTeams));
            for (final TeamPosition position : teamPositions.getPositions()) {
                final GPSFixMoving fix = new GPSFixMovingImpl(position.getPosition(), position.getTimePoint(), position.getMotionVector());
                final int deviceSerialNumber = teamPositions.getDeviceSerialNumber();
                trackedRace.getTrack(competitorByDeviceSerialNumber.get(deviceSerialNumber)).add(fix);
                updateTimePointOfLastFixPerDeviceSerialNumber(deviceSerialNumber, position.getTimePoint());
                updateStartOfTrackingToEarliestTimePoint(position.getTimePoint());
            }
        }
    }
    
    private void updateStartOfTrackingToEarliestTimePoint(TimePoint timePoint) {
        if (trackedRace.getStartOfTracking() == null || trackedRace.getStartOfTracking().after(timePoint)) {
            trackedRace.setStartOfTrackingReceived(timePoint);
        }
    }

    private void updateTimePointOfLastFixPerDeviceSerialNumber(int deviceSerialNumber, TimePoint timePoint) {
        timePointOfLastFixPerDeviceSerialNumber.compute(deviceSerialNumber, (serial, oldLastFixTimePoint)->oldLastFixTimePoint==null?timePoint:
            timePoint.after(oldLastFixTimePoint)?timePoint:oldLastFixTimePoint);
    }

    private void schedulePeriodicPollingTask() {
        logger.info("Scheduling regular polling at interval "+YellowBrickTrackingAdapterImpl.DEFAULT_POLLING_INTERVAL+
                " for YB race "+getConnectivityParams().getRaceUrl());
        ThreadPoolUtil.INSTANCE.getDefaultBackgroundTaskThreadPoolExecutor().scheduleAtFixedRate(this::pollNewPositions,
                YellowBrickTrackingAdapterImpl.DEFAULT_POLLING_INTERVAL.asMillis(),
                YellowBrickTrackingAdapterImpl.DEFAULT_POLLING_INTERVAL.asMillis(), TimeUnit.MILLISECONDS);
    }
    
    /**
     * Throws a {@link RuntimeException} if polling shall stop, after the race has been 
     */
    private void pollNewPositions() {
        if (stop) {
            logger.info("Terminating polling for YB race "+getConnectivityParams().getRaceUrl());
            throw new RuntimeException("Terminated");
        }
        // TODO be smart and find drop-outs, then ask since oldes time point excluding the drop-outs and only once in a while ask since including the drop-outs
        final TimePoint timePointStartingFromWhichToPoll = Collections.min(timePointOfLastFixPerDeviceSerialNumber.values());
        logger.info("Polling YB fixes for race "+getConnectivityParams().getRaceUrl()+" since "+timePointStartingFromWhichToPoll);
        try {
            final PositionsDocument storedData = trackingAdapter.getPositionsSince(getConnectivityParams().getRaceUrl(),
                    timePointStartingFromWhichToPoll,
                    Optional.ofNullable(getConnectivityParams().getUsername()),
                    Optional.ofNullable(getConnectivityParams().getPassword()));
            logger.fine(()->"Obtained "+storedData.getNumberOfFixes()+" fixes for "+Util.size(storedData.getTeams())+" teams");
            insertFixesIntoTrackedRace(storedData, /* no progress update */ Optional.empty());
        } catch (IOException | ParseException e) {
            logger.log(Level.SEVERE, "Fetching YB positions for race "+getConnectivityParams().getRaceUrl()+" failed. Keeping trying.", e);
        }
    }
    
    @Override
    protected void onStop(boolean preemptive, boolean willBeRemoved)
            throws MalformedURLException, IOException, InterruptedException {
        logger.info("Stopping tracking for YB race "+getConnectivityParams().getRaceUrl());
        stop = true;
        final double oldLoadingProgress = trackedRace.getStatus().getLoadingProgress();
        trackedRace.onStatusChanged(this, new TrackedRaceStatusImpl(TrackedRaceStatusEnum.FINISHED, oldLoadingProgress));
    }

    private RaceDefinition createRaceDefinition(Regatta regatta, YellowBrickTrackingAdapter yellowBrickTrackingAdapter,
            RaceTrackingHandler raceTrackingHandler, CompetitorAndBoatStore competitorAndBoatStore) throws IOException, ParseException {
        com.sap.sailing.domain.base.Course domainCourse = new CourseImpl("Course for "+getRegatta().getName(), Collections.emptySet());
        Map<Competitor, Boat> competitorsAndBoats = createCompetitorsAndBoats(yellowBrickTrackingAdapter,
                regatta.getBoatClass(), raceTrackingHandler, competitorAndBoatStore);
        logger.info("Creating RaceDefinitionImpl for YellowBrick race "+getConnectivityParams().getRaceUrl());
        RaceDefinition result = raceTrackingHandler.createRaceDefinition(regatta, getConnectivityParams().getRaceUrl(),
                domainCourse, regatta.getBoatClass(), competitorsAndBoats, getRaceId());
        regatta.addRace(result);
        return result;
    }

    /**
     * As a side effect, fills {@link #competitorByDeviceSerialNumber}
     */
    private Map<Competitor, Boat> createCompetitorsAndBoats(YellowBrickTrackingAdapter yellowBrickTrackingAdapter,
            BoatClass boatClass, RaceTrackingHandler raceTrackingHandler, CompetitorAndBoatStore competitorAndBoatStore)
                    throws IOException, ParseException {
        final Map<Competitor, Boat> result = new HashMap<>();
        final YellowBrickRace raceMetadata = yellowBrickTrackingAdapter.getRaceMetadata(getConnectivityParams().getRaceUrl(),
                Optional.ofNullable(getConnectivityParams().getUsername()),
                Optional.ofNullable(getConnectivityParams().getPassword()));
        for (final TeamPositions teamPositions : raceMetadata.getTeamsPositions()) {
            final String competitorId = YellowBrickTrackingAdapter.getCompetitorId(teamPositions.getCompetitorName(), getConnectivityParams().getRaceUrl());
            final String boatId = YellowBrickTrackingAdapter.getBoatId(teamPositions.getCompetitorName());
            final List<DynamicPerson> teamMembers = new ArrayList<DynamicPerson>();
            for (String teamMemberName : teamPositions.getCompetitorName().split("[-+&]")) {
                teamMembers.add(new PersonImpl(teamMemberName.trim(), /* nationality */ null,
                        /* dateOfBirth */ null, teamMemberName.trim()));
            }
            final DynamicTeam team = new TeamImpl(teamPositions.getCompetitorName(), teamMembers, /* coach */ null);
            final DynamicBoat boat = raceTrackingHandler.getOrCreateBoat(competitorAndBoatStore, boatId,
                    teamPositions.getCompetitorName(), boatClass, competitorId, /* color */ null);
            final DynamicCompetitorWithBoat competitor = raceTrackingHandler.getOrCreateCompetitorWithBoat(competitorAndBoatStore,
                    competitorId, teamPositions.getCompetitorName(), /* shortName */ null,
                    /* displayColor */ null, /* email */ null, /* flagImageURL */ null,
                    team, /* timeOnTimeFactor */ 1.0, /* timeOnDistanceAllowancePerNauticalMile */ Duration.NULL,
                    /* searchTag */ null, boat);
            result.put(competitor, competitor.getBoat());
            competitorByDeviceSerialNumber.put(teamPositions.getDeviceSerialNumber(), competitor);
        }
        return result;
    }
    
    private Serializable getRaceId() {
        return YellowBrickTrackingAdapter.YELLOWBRICK_PREFIX+getConnectivityParams().getRaceUrl();
    }

    /**
     * If {@code regatta} is set to a valid {@link Regatta}, it is returned unchanged. Otherwise, a default
     * regatta is looked up in the {@link TrackedRegattaRegistry} passed, and if not found, it is created
     * as a default regatta with a Time-on-Time/Time-on-Distance ranking metric.
     */
    private Regatta getOrCreateEffectiveRegatta(String name, TrackedRegattaRegistry trackedRegattaRegistry, Regatta regatta) {
        final Regatta result;
        if (regatta != null) {
            result = regatta;
        } else {
            final Regatta rememberedRegattaForRaceId = trackedRegattaRegistry.getRememberedRegattaForRace(getRaceId());
            result = rememberedRegattaForRaceId == null
                    ? trackedRegattaRegistry.getOrCreateDefaultRegatta(name, BoatClassMasterdata.IRC.name(), UUID.randomUUID())
                    : rememberedRegattaForRaceId;
        }
        return result;
    }

    @Override
    public Regatta getRegatta() {
        return regatta;
    }

    @Override
    public RaceDefinition getRace() {
        return race;
    }

    @Override
    public RaceHandle getRaceHandle() {
        return new RaceHandle() {
            @Override
            public DynamicTrackedRegatta getTrackedRegatta() {
                return YellowBrickRaceTrackerImpl.this.getTrackedRegatta();
            }
            
            @Override
            public Regatta getRegatta() {
                return regatta;
            }
            
            @Override
            public RaceTracker getRaceTracker() {
                return YellowBrickRaceTrackerImpl.this;
            }
            
            @Override
            public RaceDefinition getRace(long timeoutInMilliseconds) {
                return race;
            }
            
            @Override
            public RaceDefinition getRace() {
                return race;
            }
        };
    }

    @Override
    public DynamicTrackedRegatta getTrackedRegatta() {
        return trackedRegattaRegistry.getOrCreateTrackedRegatta(regatta);
    }

    @Override
    public WindStore getWindStore() {
        return windStore;
    }

    @Override
    public Object getID() {
        return new Pair<>(YellowBrickTrackingAdapter.NAME, getConnectivityParams().getRaceUrl());
    }
}
