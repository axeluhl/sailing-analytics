package com.sap.sailing.domain.yellowbrickadapter.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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
import com.sap.sailing.domain.base.impl.DynamicPerson;
import com.sap.sailing.domain.base.impl.DynamicTeam;
import com.sap.sailing.domain.base.impl.PersonImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.domain.common.BoatClassMasterdata;
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
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.impl.TrackingConnectorInfoImpl;
import com.sap.sailing.domain.yellowbrickadapter.YellowBrickRace;
import com.sap.sailing.domain.yellowbrickadapter.YellowBrickRaceTrackingConnectivityParams;
import com.sap.sailing.domain.yellowbrickadapter.YellowBrickTrackingAdapter;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Util.Pair;

public class YellowBrickRaceTrackerImpl extends AbstractRaceTrackerImpl<YellowBrickRaceTrackingConnectivityParams> {
    private static final Logger logger = Logger.getLogger(YellowBrickRaceTrackerImpl.class.getName());
    private final String DEFAULT_REGATTA_NAME_PREFIX = "YellowBrick ";
    private final Regatta regatta;
    private final RaceDefinition race;
    private final WindStore windStore;
    private final TrackedRegattaRegistry trackedRegattaRegistry;
    private final DynamicTrackedRace trackedRace; // TODO will be used once we start filling in the tracks
    
    public YellowBrickRaceTrackerImpl(YellowBrickRaceTrackingConnectivityParams connectivityParams, Regatta regatta,
            TrackedRegattaRegistry trackedRegattaRegistry, WindStore windStore,
            RaceLogAndTrackedRaceResolver raceLogResolver, LeaderboardGroupResolver leaderboardGroupResolver,
            long timeoutInMilliseconds, RaceTrackingHandler raceTrackingHandler, RaceLogStore raceLogStore,
            RegattaLogStore regattaLogStore, DomainFactory baseDomainFactory,
            YellowBrickTrackingAdapter yellowBrickTrackingAdapter) throws IOException, ParseException {
        super(connectivityParams);
        this.windStore = windStore;
        this.trackedRegattaRegistry = trackedRegattaRegistry;
        this.regatta = getOrCreateEffectiveRegatta(DEFAULT_REGATTA_NAME_PREFIX+connectivityParams.getRaceUrl(), trackedRegattaRegistry, regatta);
        this.race = createRaceDefinition(regatta, yellowBrickTrackingAdapter, raceTrackingHandler,
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
        // TODO fetch positions document ("stored data") up to now (starting at the beginning of time) and add positions to TrackedRace
        // TODO schedule recurring task that keeps fetching based on the last fix known for each competitor, with a smart strategy for trackers dropping out...
    }
    
    private RaceDefinition createRaceDefinition(Regatta regatta, YellowBrickTrackingAdapter yellowBrickTrackingAdapter,
            RaceTrackingHandler raceTrackingHandler, CompetitorAndBoatStore competitorAndBoatStore) throws IOException, ParseException {
        com.sap.sailing.domain.base.Course domainCourse = new CourseImpl("Course for "+getRegatta().getName(), Collections.emptySet());
        Map<Competitor, Boat> competitorsAndBoats = createCompetitorsAndBoats(yellowBrickTrackingAdapter,
                regatta.getBoatClass(), raceTrackingHandler, competitorAndBoatStore);
        logger.info("Creating RaceDefinitionImpl for YellowBrick race "+getConnectivityParams().getRaceUrl());
        RaceDefinition result = raceTrackingHandler.createRaceDefinition(regatta, getConnectivityParams().getRaceUrl(),
                domainCourse, regatta.getBoatClass(), competitorsAndBoats, getConnectivityParams().getRaceUrl());
        regatta.addRace(result);
        return result;
    }

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
            raceTrackingHandler.getOrCreateCompetitorWithBoat(competitorAndBoatStore,
                    competitorId, teamPositions.getCompetitorName(), /* shortName */ null,
                    /* displayColor */ null, /* email */ null, /* flagImageURL */ null,
                    team, /* timeOnTimeFactor */ 1.0, /* timeOnDistanceAllowancePerNauticalMile */ Duration.NULL,
                    /* searchTag */ null, boat);
        }
        return result;
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
            result = trackedRegattaRegistry.getOrCreateDefaultRegatta(name, BoatClassMasterdata.IRC.name(), UUID.randomUUID());
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
