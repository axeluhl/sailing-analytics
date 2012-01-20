package com.sap.sailing.gwt.ui.server;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.KnotSpeedImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MeterDistance;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.CountryCode;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.NoWindError;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KilometersPerHourSpeedImpl;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard.Entry;
import com.sap.sailing.domain.leaderboard.RaceInLeaderboard;
import com.sap.sailing.domain.leaderboard.ScoreCorrection.MaxPointsReason;
import com.sap.sailing.domain.leaderboard.impl.ResultDiscardingRuleImpl;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.MongoWindStoreFactory;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingConfiguration;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingFactory;
import com.sap.sailing.domain.swisstimingadapter.persistence.SwissTimingAdapterPersistence;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.RacesHandle;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.RaceRecord;
import com.sap.sailing.domain.tractracadapter.TracTracConfiguration;
import com.sap.sailing.gwt.ui.client.SailingService;
import com.sap.sailing.gwt.ui.shared.BoatClassDTO;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;
import com.sap.sailing.gwt.ui.shared.CompetitorInRaceDTO;
import com.sap.sailing.gwt.ui.shared.CompetitorsAndTimePointsDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.GPSFixDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardEntryDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardRowDTO;
import com.sap.sailing.gwt.ui.shared.LegEntryDTO;
import com.sap.sailing.gwt.ui.shared.ManeuverDTO;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sailing.gwt.ui.shared.PositionDTO;
import com.sap.sailing.gwt.ui.shared.QuickRankDTO;
import com.sap.sailing.gwt.ui.shared.RaceDTO;
import com.sap.sailing.gwt.ui.shared.RaceInLeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.SpeedWithBearingDTO;
import com.sap.sailing.gwt.ui.shared.SwissTimingConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.SwissTimingRaceRecordDTO;
import com.sap.sailing.gwt.ui.shared.TracTracConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.TracTracRaceRecordDTO;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDTO;
import com.sap.sailing.gwt.ui.shared.WindTrackInfoDTO;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.api.DetailType;
import com.sap.sailing.server.api.EventAndRaceIdentifier;
import com.sap.sailing.server.api.EventFetcher;
import com.sap.sailing.server.api.EventIdentifier;
import com.sap.sailing.server.api.EventName;
import com.sap.sailing.server.api.EventNameAndRaceName;
import com.sap.sailing.server.api.LeaderboardNameAndRaceColumnName;
import com.sap.sailing.server.api.RaceFetcher;
import com.sap.sailing.server.api.RaceIdentifier;

/**
 * The server side implementation of the RPC service.
 */
public class SailingServiceImpl extends RemoteServiceServlet implements SailingService, RaceFetcher, EventFetcher {
    private static final Logger logger = Logger.getLogger(SailingServiceImpl.class.getName());
    
    private static final long serialVersionUID = 9031688830194537489L;

    private static final long TIMEOUT_FOR_RECEIVING_RACE_DEFINITION_IN_MILLISECONDS = 60000;

    private final ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;
    
    private final MongoObjectFactory mongoObjectFactory;
    
    private final SwissTimingAdapterPersistence swissTimingAdapterPersistence;
    
    private final com.sap.sailing.domain.tractracadapter.persistence.MongoObjectFactory tractracMongoObjectFactory;

    private final DomainObjectFactory domainObjectFactory;
    
    private final SwissTimingFactory swissTimingFactory;

    private final com.sap.sailing.domain.tractracadapter.persistence.DomainObjectFactory tractracDomainObjectFactory;
    
    private final com.sap.sailing.domain.common.CountryCodeFactory countryCodeFactory;
    
    private final Executor executor;

    public SailingServiceImpl() {
        BundleContext context = Activator.getDefault();
        racingEventServiceTracker = createAndOpenRacingEventServiceTracker(context);
        mongoObjectFactory = MongoObjectFactory.INSTANCE;
        domainObjectFactory = DomainObjectFactory.INSTANCE;
        swissTimingAdapterPersistence = SwissTimingAdapterPersistence.INSTANCE;
        tractracDomainObjectFactory = com.sap.sailing.domain.tractracadapter.persistence.DomainObjectFactory.INSTANCE;
        tractracMongoObjectFactory = com.sap.sailing.domain.tractracadapter.persistence.MongoObjectFactory.INSTANCE;
        swissTimingFactory = SwissTimingFactory.INSTANCE;
        countryCodeFactory = com.sap.sailing.domain.common.CountryCodeFactory.INSTANCE;
        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    protected ServiceTracker<RacingEventService, RacingEventService> createAndOpenRacingEventServiceTracker(
            BundleContext context) {
        ServiceTracker<RacingEventService, RacingEventService> result = new ServiceTracker<RacingEventService, RacingEventService>(
                context, RacingEventService.class.getName(), null);
        result.open();
        return result;
    }
    
    @Override
    public LeaderboardDTO getLeaderboardByName(String leaderboardName, Date date,
            final Collection<String> namesOfRacesForWhichToLoadLegDetails)
            throws NoWindException {
        long startOfRequestHandling = System.currentTimeMillis();
        LeaderboardDTO result = null;
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard != null) {
            result = new LeaderboardDTO();
            final TimePoint timePoint = new MillisecondsTimePoint(date);
            result.competitors = new ArrayList<CompetitorDTO>();
            result.name = leaderboard.getName();
            result.competitorDisplayNames = new HashMap<CompetitorDTO, String>();
            for (RaceInLeaderboard raceColumn : leaderboard.getRaceColumns()) {
                result.addRace(raceColumn.getName(), raceColumn.isMedalRace(), raceColumn.getTrackedRace() != null);
            }
            result.rows = new HashMap<CompetitorDTO, LeaderboardRowDTO>();
            result.hasCarriedPoints = leaderboard.hasCarriedPoints();
            result.discardThresholds = leaderboard.getResultDiscardingRule().getDiscardIndexResultsStartingWithHowManyRaces();
            for (final Competitor competitor : leaderboard.getCompetitors()) {
                CompetitorDTO competitorDTO = getCompetitorDTO(competitor);
                LeaderboardRowDTO row = new LeaderboardRowDTO();
                row.competitor = competitorDTO;
                row.fieldsByRaceName = new HashMap<String, LeaderboardEntryDTO>();
                row.carriedPoints = leaderboard.hasCarriedPoints(competitor) ? leaderboard.getCarriedPoints(competitor) : null;
                result.competitors.add(competitorDTO);
                Map<String, Future<LeaderboardEntryDTO>> futuresForColumnName = new HashMap<String, Future<LeaderboardEntryDTO>>();
                for (final RaceInLeaderboard raceColumn : leaderboard.getRaceColumns()) {
                    final Entry entry = leaderboard.getEntry(competitor, raceColumn, timePoint);
                    RunnableFuture<LeaderboardEntryDTO> future = new FutureTask<LeaderboardEntryDTO>(new Callable<LeaderboardEntryDTO>() {
                        @Override
                                public LeaderboardEntryDTO call() {
                                    try {
                                        return getLeaderboardEntryDTO(entry, raceColumn.getTrackedRace(), competitor, timePoint,
                                                namesOfRacesForWhichToLoadLegDetails != null
                                                        && namesOfRacesForWhichToLoadLegDetails.contains(raceColumn.getName()));
                                    } catch (NoWindException e) {
                                        throw new NoWindError(e);
                                    }
                                }
                    });
                    executor.execute(future);
                    futuresForColumnName.put(raceColumn.getName(), future);
                }
                for (Map.Entry<String, Future<LeaderboardEntryDTO>> raceColumnNameAndFuture : futuresForColumnName.entrySet()) {
                    try {
                        row.fieldsByRaceName.put(raceColumnNameAndFuture.getKey(), raceColumnNameAndFuture.getValue().get());
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
        logger.fine("getLeaderboardByName("+leaderboardName+", "+date+", "+namesOfRacesForWhichToLoadLegDetails+") took "+
                (System.currentTimeMillis()-startOfRequestHandling)+"ms");
        return result;
    }
    
    @Override
    public void stressTestLeaderboardByName(String leaderboardName, int times) throws Exception {
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard != null) {
            List<String> raceColumnNames = new ArrayList<String>();
            for (RaceInLeaderboard column : leaderboard.getRaceColumns()) {
                raceColumnNames.add(column.getName());
            }
            int i=0;
            for (Date date = new Date(); i<times; date = new Date(date.getTime()-10)) {
                getLeaderboardByName(leaderboardName, date, raceColumnNames);
                i++;
            }
        } else {
            logger.warning("stressTestLeaderboardByName: couldn't find leaderboard "+leaderboardName);
        }
    }
    
    private LeaderboardEntryDTO getLeaderboardEntryDTO(Entry entry, TrackedRace trackedRace, Competitor competitor,
            TimePoint timePoint, boolean addLegDetails) throws NoWindException {
        LeaderboardEntryDTO entryDTO = new LeaderboardEntryDTO();
        entryDTO.netPoints = entry.getNetPoints();
        entryDTO.totalPoints = entry.getTotalPoints();
        entryDTO.reasonForMaxPoints = entry.getMaxPointsReason().name();
        entryDTO.discarded = entry.isDiscarded();
        if (addLegDetails && trackedRace != null) {
            entryDTO.legDetails = new ArrayList<LegEntryDTO>();
            for (Leg leg : trackedRace.getRace().getCourse().getLegs()) {
                TrackedLegOfCompetitor trackedLeg = trackedRace.getTrackedLeg(competitor, leg);
                LegEntryDTO legEntry;
                if (trackedLeg != null && trackedLeg.hasStartedLeg(timePoint)) {
                    legEntry = createLegEntry(trackedLeg, timePoint);
                } else {
                    legEntry = null;
                }
                entryDTO.legDetails.add(legEntry);
            }
        }
        return entryDTO;
    }

    private LegEntryDTO createLegEntry(TrackedLegOfCompetitor trackedLeg, TimePoint timePoint) throws NoWindException {
        LegEntryDTO result;
        if (trackedLeg == null) {
            result = null;
        } else {
            result = new LegEntryDTO();
            Speed averageSpeedOverGround = trackedLeg.getAverageSpeedOverGround(timePoint);
            result.averageSpeedOverGroundInKnots = averageSpeedOverGround == null ? null : averageSpeedOverGround
                    .getKnots();
            SpeedWithBearing speedOverGround = trackedLeg.getSpeedOverGround(timePoint);
            result.currentSpeedOverGroundInKnots = speedOverGround == null ? null : speedOverGround.getKnots();
            Distance distanceTraveled = trackedLeg.getDistanceTraveled(timePoint);
            result.distanceTraveledInMeters = distanceTraveled == null ? null : distanceTraveled.getMeters();
            result.estimatedTimeToNextWaypointInSeconds = trackedLeg.getEstimatedTimeToNextMarkInSeconds(timePoint);
            result.timeInMilliseconds = trackedLeg.getTimeInMilliSeconds(timePoint);
            result.finished = trackedLeg.hasFinishedLeg(timePoint);
            result.gapToLeaderInSeconds = trackedLeg.getGapToLeaderInSeconds(timePoint);
            result.rank = trackedLeg.getRank(timePoint);
            result.started = trackedLeg.hasStartedLeg(timePoint);
            Speed velocityMadeGood = trackedLeg.getVelocityMadeGood(timePoint);
            result.velocityMadeGoodInKnots = velocityMadeGood == null ? null : velocityMadeGood.getKnots();
            Distance windwardDistanceToGo = trackedLeg.getWindwardDistanceToGo(timePoint);
            result.windwardDistanceToGoInMeters = windwardDistanceToGo == null ? null : windwardDistanceToGo
                    .getMeters();
            List<Maneuver> maneuvers = trackedLeg.getManeuvers(timePoint);
            if (maneuvers != null) {
                result.numberOfTacks = 0;
                result.numberOfJibes = 0;
                result.numberOfPenaltyCircles = 0;
                for (Maneuver maneuver : maneuvers) {
                    switch (maneuver.getType()) {
                    case TACK:
                        result.numberOfTacks++;
                        break;
                    case JIBE:
                        result.numberOfJibes++;
                        break;
                    case PENALTY_CIRCLE:
                        result.numberOfPenaltyCircles++;
                        break;
                    }
                }
            }
        }
        return result;
    }

    public List<EventDTO> listEvents(boolean withRacePlaces) throws IllegalArgumentException {
        List<EventDTO> result = new ArrayList<EventDTO>();
        for (Event event : getService().getAllEvents()) {
            List<CompetitorDTO> competitorList = getCompetitorDTOs(event.getCompetitors());
            List<RegattaDTO> regattasList = getRegattaDTOs(event, withRacePlaces);
            EventDTO eventDTO = new EventDTO(event.getName(), regattasList, competitorList);
            for (RegattaDTO regatta : regattasList) {
                regatta.setEvent(eventDTO);
            }
            if (!eventDTO.regattas.isEmpty()) {
                result.add(eventDTO);
            }
        }
        return result;
    }

    private List<RegattaDTO> getRegattaDTOs(Event event, boolean withRacePlaces) {
        Map<BoatClass, Set<RaceDefinition>> racesByBoatClass = new HashMap<BoatClass, Set<RaceDefinition>>();
        for (RaceDefinition r : event.getAllRaces()) {
            Set<RaceDefinition> racesForBoatClass = racesByBoatClass.get(r.getBoatClass());
            if (racesForBoatClass == null) {
                racesForBoatClass = new HashSet<RaceDefinition>();
                racesByBoatClass.put(r.getBoatClass(), racesForBoatClass);
            }
            racesForBoatClass.add(r);
        }
        List<RegattaDTO> result = new ArrayList<RegattaDTO>();
        for (Map.Entry<BoatClass, Set<RaceDefinition>> e : racesByBoatClass.entrySet()) {
            List<RaceDTO> raceDTOsInBoatClass = getRaceDTOs(event, e.getValue(), withRacePlaces);
            if (!raceDTOsInBoatClass.isEmpty()) {
                RegattaDTO regatta = new RegattaDTO(new BoatClassDTO(e.getKey()==null?"":e.getKey().getName()), raceDTOsInBoatClass);
                for (RaceDTO race : raceDTOsInBoatClass) {
                    race.setRegatta(regatta);
                }
                result.add(regatta);
            }
        }
        return result;
    }

    private List<RaceDTO> getRaceDTOs(Event event, Set<RaceDefinition> races, boolean withRacePlaces) {
        List<RaceDTO> result = new ArrayList<RaceDTO>();
        for (RaceDefinition r : races) {
            RaceDTO raceDTO = new RaceDTO(r.getName(), getCompetitorDTOs(r.getCompetitors()), getService().isRaceBeingTracked(r));
            if (raceDTO.currentlyTracked) {
                TrackedRace trackedRace = getService().getTrackedRace(event, r);
                raceDTO.startOfRace = trackedRace.getStart() == null ? null : trackedRace.getStart().asDate();
                raceDTO.startOfTracking = trackedRace.getStartOfTracking() == null ? null : trackedRace.getStartOfTracking().asDate();
                raceDTO.timePointOfLastEvent = trackedRace.getTimePointOfLastEvent() == null ? null : trackedRace.getTimePointOfLastEvent().asDate();
                raceDTO.timePointOfNewestEvent = trackedRace.getTimePointOfNewestEvent() == null ? null : trackedRace.getTimePointOfNewestEvent().asDate();
                if (withRacePlaces) {
                    raceDTO.racePlaces = trackedRace.getPlaceOrder();
                }
            }
            result.add(raceDTO);
        }
        return result;
    }

    private List<CompetitorDTO> getCompetitorDTOs(Iterable<Competitor> competitors) {
        List<CompetitorDTO> result = new ArrayList<CompetitorDTO>();
        for (Competitor c : competitors) {
            CompetitorDTO competitorDTO = getCompetitorDTO(c);
            result.add(competitorDTO);
        }
        return result;
    }

    private CompetitorDTO getCompetitorDTO(Competitor c) {
        CountryCode countryCode = c.getTeam().getNationality().getCountryCode();
        CompetitorDTO competitorDTO = new CompetitorDTO(c.getName(), countryCode==null?"":countryCode.getTwoLetterISOCode(),
                countryCode==null?"":countryCode.getThreeLetterIOCCode(), countryCode==null?"":countryCode.getName(), c.getBoat().getSailID(),
                        c.getId().toString());
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
                            .getTrackingEndTime().asDate(), raceRecord.getRaceStartTime().asDate()));
        }
        return new Pair<String, List<TracTracRaceRecordDTO>>(raceRecords.getA(), result);
    }

    @Override
    public void track(TracTracRaceRecordDTO rr, String liveURI, String storedURI, boolean trackWind, final boolean correctWindByDeclination) throws Exception {
        if (liveURI == null || liveURI.trim().length() == 0) {
            liveURI = rr.liveURI;
        }
        if (storedURI == null || storedURI.trim().length() == 0) {
            storedURI = rr.storedURI;
        }
        final RacesHandle raceHandle = getService().addTracTracRace(new URL(rr.paramURL), new URI(liveURI), new URI(storedURI),
                MongoWindStoreFactory.INSTANCE.getMongoWindStore(mongoObjectFactory, domainObjectFactory), TIMEOUT_FOR_RECEIVING_RACE_DEFINITION_IN_MILLISECONDS);
        if (trackWind) {
            new Thread("Wind tracking starter for race "+rr.eventName+"/"+rr.name) {
                public void run() {
                    try {
                        startTrackingWind(raceHandle, correctWindByDeclination, TIMEOUT_FOR_RECEIVING_RACE_DEFINITION_IN_MILLISECONDS);
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
        DomainFactory domainFactory = DomainFactory.INSTANCE;
        tractracMongoObjectFactory.storeTracTracConfiguration(domainFactory.createTracTracConfiguration(name, jsonURL, liveDataURI, storedDataURI));
    }
    
    @Override
    public void stopTrackingEvent(EventIdentifier eventIdentifier) throws Exception {
        Event event = getEvent(eventIdentifier);
        if (event != null) {
            getService().stopTracking(event);
        }
    }

    private RaceDefinition getRaceByName(Event event, String raceName) {
        if (event != null) {
            return event.getRaceByName(raceName);
        } else {
            return null;
        }
    }
    
    @Override
    public void stopTrackingRace(EventAndRaceIdentifier eventAndRaceIdentifier) throws Exception {
        Event event = getEvent(eventAndRaceIdentifier);
        if (event != null) {
            RaceDefinition r = getRace(eventAndRaceIdentifier);
            if (r != null) {
                getService().stopTracking(event, r);
            }
        }
    }
    
    @Override
    public void removeAndUntrackRace(EventAndRaceIdentifier eventAndRaceidentifier) throws Exception{
        Event event = getEvent(eventAndRaceidentifier);
        if(event!= null) {
            RaceDefinition race = getRace(eventAndRaceidentifier);
            if(race != null) {
                getService().removeRace(event, race);
            }
        }
    }
    
    /**
     * @param timeoutInMilliseconds eventually passed to {@link RacesHandle#getRaces(long)}. If the race definition
     * can be obtained within this timeout, wind for the race will be tracked; otherwise, the method returns without
     * taking any effect.
     */
    private void startTrackingWind(RacesHandle raceHandle, boolean correctByDeclination, long timeoutInMilliseconds) throws Exception {
        Event event = raceHandle.getEvent();
        if (event != null) {
            for (RaceDefinition race : raceHandle.getRaces(timeoutInMilliseconds)) {
                if (race != null) {
                    getService().startTrackingWind(event, race, correctByDeclination);
                } else {
                    log("RaceDefinition wasn't received within " + timeoutInMilliseconds + "ms for a race in event "
                            + event.getName() + ". Aborting wait; no wind tracking for this race.");
                }
            }
        }
    }

    @Override
    public WindInfoForRaceDTO getWindInfo(RaceIdentifier raceIdentifier, Date fromDate, Date toDate, WindSource[] windSources) {
        WindInfoForRaceDTO result = null;
        TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
        if (trackedRace != null) {
            result = new WindInfoForRaceDTO();
            result.raceIsKnownToStartUpwind = trackedRace.raceIsKnownToStartUpwind();
            result.selectedWindSource = trackedRace.getWindSource();
            TimePoint from = fromDate == null ? trackedRace.getStart() : new MillisecondsTimePoint(fromDate);
            TimePoint to = toDate == null ? trackedRace.getTimePointOfNewestEvent() : new MillisecondsTimePoint(toDate);
            Map<WindSource, WindTrackInfoDTO> windTrackInfoDTOs = new HashMap<WindSource, WindTrackInfoDTO>();
            result.windTrackInfoByWindSource = windTrackInfoDTOs;
            if (from != null && to != null) {
                for (WindSource windSource : (windSources == null ? WindSource.values() : windSources)) {
                    WindTrackInfoDTO windTrackInfoDTO = new WindTrackInfoDTO();
                    windTrackInfoDTO.windFixes = new ArrayList<WindDTO>();
                    WindTrack windTrack = trackedRace.getWindTrack(windSource);
                    windTrackInfoDTO.dampeningIntervalInMilliseconds = windTrack
                            .getMillisecondsOverWhichToAverageWind();
                    Iterator<Wind> windIter = windTrack.getFixesIterator(from, /* inclusive */true);
                    while (windIter.hasNext()) {
                        Wind wind = windIter.next();
                        if (wind.getTimePoint().compareTo(to) > 0) {
                            break;
                        }
                        WindDTO windDTO = createWindDTO(wind, windTrack);
                        windTrackInfoDTO.windFixes.add(windDTO);
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
            windDTO.timepoint = wind.getTimePoint().asMillis();
            Wind estimatedWind = windTrack
                    .getEstimatedWind(wind.getPosition(), wind.getTimePoint());
            if (estimatedWind != null) {
                windDTO.dampenedTrueWindBearingDeg = estimatedWind.getBearing().getDegrees();
                windDTO.dampenedTrueWindFromDeg = estimatedWind.getBearing().reverse().getDegrees();
                windDTO.dampenedTrueWindSpeedInKnots = estimatedWind.getKnots();
                windDTO.dampenedTrueWindSpeedInMetersPerSecond = estimatedWind.getMetersPerSecond();
            }
        }
        return windDTO;
    }
    
    @Override
    public WindInfoForRaceDTO getWindInfo(RaceIdentifier raceIdentifier, Date from, long millisecondsStepWidth,
            int numberOfFixes, double latDeg, double lngDeg, Collection<String> windSources)
            throws NoWindException {
        Position position = new DegreePosition(latDeg, lngDeg);
        WindInfoForRaceDTO result = null;
        TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
        if (trackedRace != null) {
            result = new WindInfoForRaceDTO();
            result.raceIsKnownToStartUpwind = trackedRace.raceIsKnownToStartUpwind();
            result.selectedWindSource = trackedRace.getWindSource();
            Map<WindSource, WindTrackInfoDTO> windTrackInfoDTOs = new HashMap<WindSource, WindTrackInfoDTO>();
            result.windTrackInfoByWindSource = windTrackInfoDTOs;
            for (WindSource windSource : WindSource.values()) {
                if (windSources == null || windSources.contains(windSource.name())) {
                    TimePoint fromTimePoint = new MillisecondsTimePoint(from);
                    WindTrackInfoDTO windTrackInfoDTO = new WindTrackInfoDTO();
                    windTrackInfoDTO.windFixes = new ArrayList<WindDTO>();
                    WindTrack windTrack = trackedRace.getWindTrack(windSource);
                    windTrackInfoDTOs.put(windSource, windTrackInfoDTO);
                    windTrackInfoDTO.dampeningIntervalInMilliseconds = windTrack
                            .getMillisecondsOverWhichToAverageWind();
                    TimePoint timePoint = fromTimePoint;
                    for (int i = 0; i < numberOfFixes; i++) {
                        Wind wind = windTrack.getEstimatedWind(position, timePoint);
                        if (wind != null) {
                            WindDTO windDTO = createWindDTO(wind, windTrack);
                            windTrackInfoDTO.windFixes.add(windDTO);
                        }
                        timePoint = new MillisecondsTimePoint(timePoint.asMillis() + millisecondsStepWidth);
                    }
                }
            }
        }
        return result;
    }
    
    @Override
    public void setWind(RaceIdentifier raceIdentifier, WindDTO windDTO) {
        DynamicTrackedRace trackedRace = (DynamicTrackedRace) getExistingTrackedRace(raceIdentifier);
        if (trackedRace != null) {
            Position p = null;
            if (windDTO.position != null) {
                p = new DegreePosition(windDTO.position.latDeg, windDTO.position.lngDeg);
            }
            TimePoint at = null;
            if (windDTO.timepoint != null) {
                at = new MillisecondsTimePoint(windDTO.timepoint);
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
            trackedRace.recordWind(wind, WindSource.WEB);
        }
    }
    
    @Override
    public void setWindSource(RaceIdentifier raceIdentifier, String windSourceName, boolean raceIsKnownToStartUpwind) {
        TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
        if (trackedRace != null && trackedRace instanceof DynamicTrackedRace) {
            DynamicTrackedRace dtr = (DynamicTrackedRace) trackedRace;
            dtr.setWindSource(WindSource.valueOf(windSourceName));
            dtr.setRaceIsKnownToStartUpwind(raceIsKnownToStartUpwind);
        }
    }

    @Override
    public Map<CompetitorDTO, List<GPSFixDTO>> getBoatPositions(RaceIdentifier raceIdentifier,
            Map<CompetitorDTO, Date> from, Map<CompetitorDTO, Date> to,
            boolean extrapolate) throws NoWindException {
        Map<CompetitorDTO, List<GPSFixDTO>> result = new HashMap<CompetitorDTO, List<GPSFixDTO>>();
        TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
        if (trackedRace != null) {
            for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
                CompetitorDTO competitorDTO = getCompetitorDTO(competitor);
                if (from.containsKey(competitorDTO)) {
                    List<GPSFixDTO> fixesForCompetitor = new ArrayList<GPSFixDTO>();
                    result.put(competitorDTO, fixesForCompetitor);
                    GPSFixTrack<Competitor, GPSFixMoving> track = trackedRace.getTrack(competitor);
                    TimePoint fromTimePoint = new MillisecondsTimePoint(from.get(competitorDTO));
                    TimePoint toTimePointExcluding = new MillisecondsTimePoint(to.get(competitorDTO));
                    // copy the fixes into a list while holding the monitor; then release the monitor to avoid deadlocks
                    // during wind estimations required for tack determination
                    List<GPSFixMoving> fixes = new ArrayList<GPSFixMoving>();
                    synchronized (track) {
                        Iterator<GPSFixMoving> fixIter = track.getFixesIterator(fromTimePoint, /* inclusive */true);
                        while (fixIter.hasNext()) {
                            GPSFixMoving fix = fixIter.next();
                            if (fix.getTimePoint().compareTo(toTimePointExcluding) < 0) {
                                fixes.add(fix);
                            } else {
                                break;
                            }
                        }
                    }
                    Iterator<GPSFixMoving> fixIter = fixes.iterator();
                    if (fixIter.hasNext()) {
                        GPSFixMoving fix = fixIter.next();
                        while (fix != null && fix.getTimePoint().compareTo(toTimePointExcluding) < 0) {
                            Tack tack = trackedRace.getTack(competitor, fix.getTimePoint());
                            TrackedLegOfCompetitor trackedLegOfCompetitor = trackedRace.getTrackedLeg(competitor,
                                    fix.getTimePoint());
                            LegType legType = trackedLegOfCompetitor == null ? null : trackedRace.getTrackedLeg(
                                    trackedLegOfCompetitor.getLeg()).getLegType(fix.getTimePoint());
                            GPSFixDTO fixDTO = createGPSFixDTO(fix, fix.getSpeed(), tack, legType, /* extrapolate */
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
                                    GPSFixDTO extrapolated = new GPSFixDTO(to.get(competitorDTO), new PositionDTO(
                                            position.getLatDeg(), position.getLngDeg()),
                                            createSpeedWithBearingDTO(speedWithBearing), tack2, /* extrapolated */
                                            legType2, true);
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

    private GPSFixDTO createGPSFixDTO(GPSFix fix, SpeedWithBearing speedWithBearing, Tack tack, LegType legType, boolean extrapolated) {
        return new GPSFixDTO(fix.getTimePoint().asDate(), new PositionDTO(fix
                .getPosition().getLatDeg(), fix.getPosition().getLngDeg()),
                createSpeedWithBearingDTO(speedWithBearing), tack, legType, extrapolated);
    }

    @Override
    public List<MarkDTO> getMarkPositions(RaceIdentifier raceIdentifier, Date date) {
        List<MarkDTO> result = new ArrayList<MarkDTO>();
        if (date != null) {
            TimePoint dateAsTimePoint = new MillisecondsTimePoint(date);
            TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
            if (trackedRace != null) {
                Set<Buoy> buoys = new HashSet<Buoy>();
                for (Waypoint waypoint : trackedRace.getRace().getCourse().getWaypoints()) {
                    for (Buoy b : waypoint.getBuoys()) {
                        buoys.add(b);
                    }
                }
                for (Buoy buoy : buoys) {
                    GPSFixTrack<Buoy, GPSFix> track = trackedRace.getOrCreateTrack(buoy);
                    Position positionAtDate = track.getEstimatedPosition(dateAsTimePoint, /* extrapolate */false);
                    if (positionAtDate != null) {
                        MarkDTO markDTO = new MarkDTO(buoy.getName(), positionAtDate.getLatDeg(),
                                positionAtDate.getLngDeg());
                        result.add(markDTO);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public List<QuickRankDTO> getQuickRanks(RaceIdentifier raceIdentifier, Date date) throws NoWindException {
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
                        int legNumber = race.getCourse().getLegs().indexOf(trackedLeg.getLeg());
                        QuickRankDTO quickRankDTO = new QuickRankDTO(getCompetitorDTO(competitor), rank, legNumber);
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
    public void removeWind(RaceIdentifier raceIdentifier, WindDTO windDTO) {
        TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
        if (trackedRace != null) {
            Position p = null;
            if (windDTO.position != null) {
                p = new DegreePosition(windDTO.position.latDeg, windDTO.position.lngDeg);
            }
            TimePoint at = null;
            if (windDTO.timepoint != null) {
                at = new MillisecondsTimePoint(windDTO.timepoint);
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
            trackedRace.removeWind(wind, WindSource.WEB);
        }
   }

    protected RacingEventService getService() {
        return racingEventServiceTracker.getService(); // grab the service
    }

    @Override
    public List<String> getLeaderboardNames() throws Exception {
        Map<String, Leaderboard> leaderboards = getService().getLeaderboards();
        List<String> result = new ArrayList<String>(leaderboards.size());
        for (Leaderboard leaderboard : leaderboards.values()) {
            result.add(leaderboard.getName());
        }
        return result;
    }

    @Override
    public LeaderboardDTO createLeaderboard(String leaderboardName, int[] discardThresholds) {
        return createStrippedLeaderboardDTO(getService().addLeaderboard(leaderboardName, discardThresholds));
    }

    @Override
    public List<LeaderboardDTO> getLeaderboards() {
        Map<String, Leaderboard> leaderboards = getService().getLeaderboards();
        List<LeaderboardDTO> results = new ArrayList<LeaderboardDTO>();
        for(Leaderboard leaderboard: leaderboards.values()) {
            LeaderboardDTO dao = createStrippedLeaderboardDTO(leaderboard);
            results.add(dao);
        }
        
        return results;
    }
    
    @Override
    public List<LeaderboardDTO> getLeaderboardsByEvent(EventIdentifier eventIdentifier) {
        Event event = getEvent(eventIdentifier);
        Map<String, Leaderboard> leaderboards = getService().getLeaderboards();
        List<LeaderboardDTO> results = new ArrayList<LeaderboardDTO>();
        
        for (Leaderboard leaderboard : leaderboards.values()) {
            for (RaceInLeaderboard race : leaderboard.getRaceColumns()) {
                if (Util.contains(event.getAllRaces(), race.getTrackedRace().getRace())) {
                    LeaderboardDTO dao = createStrippedLeaderboardDTO(leaderboard);
                    results.add(dao);
                }
            }
        }
        
        return results;
    }

    /**
     * Creates a {@link LeaderboardDTO} for <code>leaderboard</code> and fills in the name, race master data
     * in the form of {@link RaceInLeaderboardDTO}s, whether or not there are {@link LeaderboardDTO#hasCarriedPoints carried points}
     * and the {@link LeaderboardDTO#discardThresholds discarding thresholds} for the leaderboard. No data about the points
     * is filled into the result object. No data about the competitor display names is filled in; instead, an empty map
     * is used for {@link LeaderboardDTO#competitorDisplayNames}.
     */
    private LeaderboardDTO createStrippedLeaderboardDTO(Leaderboard leaderboard) {
        LeaderboardDTO dao = new LeaderboardDTO();
        dao.name = leaderboard.getName();
        dao.competitorDisplayNames = new HashMap<CompetitorDTO, String>();
        for (RaceInLeaderboard raceColumn : leaderboard.getRaceColumns()) {
            dao.addRace(raceColumn.getName(), raceColumn.isMedalRace(), raceColumn.getTrackedRace() != null);
        }
        dao.hasCarriedPoints = leaderboard.hasCarriedPoints();
        dao.discardThresholds = leaderboard.getResultDiscardingRule().getDiscardIndexResultsStartingWithHowManyRaces();
        return dao;
    }
    
    @Override
    public void updateLeaderboard(String leaderboardName, String newLeaderboardName, int[] newDiscardingThreasholds) {
        if (!leaderboardName.equals(newLeaderboardName)) {
            getService().renameLeaderboard(leaderboardName, newLeaderboardName);
        }
        Leaderboard leaderboard = getService().getLeaderboardByName(newLeaderboardName);
        if (!Arrays.equals(leaderboard.getResultDiscardingRule().getDiscardIndexResultsStartingWithHowManyRaces(), newDiscardingThreasholds)) {
            leaderboard.setResultDiscardingRule(new ResultDiscardingRuleImpl(newDiscardingThreasholds));
        }
        getService().updateStoredLeaderboard(leaderboard);
    }

    @Override
    public void removeLeaderboard(String leaderboardName) {
        getService().removeLeaderboard(leaderboardName);
    }

    @Override
    public void renameLeaderboard(String leaderboardName, String newLeaderboardName) {
        getService().renameLeaderboard(leaderboardName, newLeaderboardName);
    }

    @Override
    public void addColumnToLeaderboard(String columnName, String leaderboardName, boolean medalRace) {
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard != null) {
            leaderboard.addRaceColumn(columnName, medalRace);
            getService().updateStoredLeaderboard(leaderboard);
        } else {
            throw new IllegalArgumentException("Leaderboard named "+leaderboardName+" not found");
        }
    }

    @Override
    public void removeLeaderboardColumn(String leaderboardName, String columnName) {
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard != null) {
            leaderboard.removeRaceColumn(columnName);
            getService().updateStoredLeaderboard(leaderboard);
        } else {
            throw new IllegalArgumentException("Leaderboard named "+leaderboardName+" not found");
        }
    }

    @Override
    public void renameLeaderboardColumn(String leaderboardName, String oldColumnName, String newColumnName) {
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard != null) {
            leaderboard.getRaceColumnByName(oldColumnName).setName(newColumnName);
            getService().updateStoredLeaderboard(leaderboard);
        } else {
            throw new IllegalArgumentException("Leaderboard named "+leaderboardName+" not found");
        }
    }

    @Override
    public boolean connectTrackedRaceToLeaderboardColumn(String leaderboardName, String raceColumnName, RaceIdentifier raceIdentifier) {
        boolean success = false;
        TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
        if (trackedRace != null) {
            Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
            if (leaderboard != null) {
                RaceInLeaderboard raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
                if (raceColumn != null) {
                    raceColumn.setTrackedRace(trackedRace);
                    success = true;
                    getService().updateStoredLeaderboard(leaderboard);
                }
            }
        }
        return success;
    }

    @Override
    public Pair<String, String> getEventAndRaceNameOfTrackedRaceConnectedToLeaderboardColumn(String leaderboardName,
            String raceColumnName) {
        Pair<String, String> result = null;
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard != null) {
            RaceInLeaderboard raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
            if (raceColumn != null) {
                TrackedRace trackedRace = raceColumn.getTrackedRace();
                if (trackedRace != null) {
                    result = new Pair<String, String>(trackedRace.getTrackedEvent().getEvent().getName(),
                            trackedRace.getRace().getName());
                }
            }
        }
        return result;
    }

    @Override
    public void disconnectLeaderboardColumnFromTrackedRace(String leaderboardName, String raceColumnName) {
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard != null) {
            RaceInLeaderboard raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
            if (raceColumn != null) {
                raceColumn.setTrackedRace(null);
                getService().updateStoredLeaderboard(leaderboard);
            } else {
                throw new IllegalArgumentException("Didn't find race "+raceColumnName+" in leaderboard "+leaderboardName);
            }
        } else {
            throw new IllegalArgumentException("Didn't find leaderboard "+leaderboardName);
        }
    }

    @Override
    public void updateLeaderboardCarryValue(String leaderboardName, String competitorName, Integer carriedPoints) {
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard != null) {
            Competitor competitor = leaderboard.getCompetitorByName(competitorName);
            if (competitor != null) {
                if (carriedPoints == null) {
                    leaderboard.unsetCarriedPoints(competitor);
                } else {
                    leaderboard.setCarriedPoints(competitor, carriedPoints);
                }
                getService().updateStoredLeaderboard(leaderboard);
            } else {
                throw new IllegalArgumentException("Didn't find competitor "+competitorName+" in leaderboard "+leaderboardName);
            }
        } else {
            throw new IllegalArgumentException("Didn't find leaderboard "+leaderboardName);
        }
    }

    @Override
    public Pair<Integer, Integer> updateLeaderboardMaxPointsReason(String leaderboardName, String competitorName, String raceColumnName,
            String maxPointsReasonAsString, Date date) throws NoWindException {
        TimePoint timePoint = new MillisecondsTimePoint(date);
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard != null) {
            Competitor competitor = leaderboard.getCompetitorByName(competitorName);
            if (competitor != null) {
                RaceInLeaderboard raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
                if (raceColumn == null) {
                    throw new IllegalArgumentException("Didn't find race "+raceColumnName+" in leaderboard "+leaderboardName);
                }
                if (maxPointsReasonAsString == null) {
                    leaderboard.getScoreCorrection().setMaxPointsReason(competitor, raceColumn, null); // null means "unset"
                } else {
                    leaderboard.getScoreCorrection().setMaxPointsReason(competitor, raceColumn, MaxPointsReason.valueOf(maxPointsReasonAsString));
                }
                getService().updateStoredLeaderboard(leaderboard);
                Entry updatedEntry = leaderboard.getEntry(competitor, raceColumn, timePoint);
                return new Pair<Integer, Integer>(updatedEntry.getNetPoints(), updatedEntry.getTotalPoints());
            } else {
                throw new IllegalArgumentException("Didn't find competitor "+competitorName+" in leaderboard "+leaderboardName);
            }
        } else {
            throw new IllegalArgumentException("Didn't find leaderboard "+leaderboardName);
        }
    }

    @Override
    public Pair<Integer, Integer> updateLeaderboardScoreCorrection(String leaderboardName, String competitorName, String raceName,
            Integer correctedScore, Date date) throws NoWindException {
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        int newNetPoints;
        int newTotalPoints;
        if (leaderboard != null) {
            Competitor competitor = leaderboard.getCompetitorByName(competitorName);
            if (competitor != null) {
                MillisecondsTimePoint timePoint = new MillisecondsTimePoint(date);
                RaceInLeaderboard raceColumn = leaderboard.getRaceColumnByName(raceName);
                if (raceColumn == null) {
                    throw new IllegalArgumentException("Didn't find race "+raceName+" in leaderboard "+leaderboardName);
                }
                if (correctedScore == null) {
                    leaderboard.getScoreCorrection().uncorrectScore(competitor, raceColumn);
                    newNetPoints = leaderboard.getNetPoints(competitor, raceColumn, timePoint);
                } else {
                    leaderboard.getScoreCorrection().correctScore(competitor, raceColumn, correctedScore);
                    newNetPoints = correctedScore;
                }
                newTotalPoints = leaderboard.getEntry(competitor, raceColumn, timePoint).getTotalPoints();
            } else {
                throw new IllegalArgumentException("Didn't find competitor "+competitorName+" in leaderboard "+leaderboardName);
            }
        } else {
            throw new IllegalArgumentException("Didn't find leaderboard "+leaderboardName);
        }
        getService().updateStoredLeaderboard(leaderboard);
        return new Pair<Integer, Integer>(newNetPoints, newTotalPoints);
    }
    
    @Override
    public void updateCompetitorDisplayNameInLeaderboard(String leaderboardName, String competitorName, String displayName) {
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        Competitor competitor = leaderboard.getCompetitorByName(competitorName);
        if (competitor != null) {
            leaderboard.setDisplayName(competitor, displayName);
            getService().updateStoredLeaderboard(leaderboard);
        }
    }

    @Override
    public void moveLeaderboardColumnUp(String leaderboardName, String columnName) {
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard != null) {
            leaderboard.moveRaceColumnUp(columnName);
            getService().updateStoredLeaderboard(leaderboard);
        } else {
            throw new IllegalArgumentException("Leaderboard named " + leaderboardName + " not found");
        }

    }

    @Override
    public void moveLeaderboardColumnDown(String leaderboardName, String columnName) {
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard != null) {
            leaderboard.moveRaceColumnDown(columnName);
            getService().updateStoredLeaderboard(leaderboard);
        } else {
            throw new IllegalArgumentException("Leaderboard named " + leaderboardName + " not found");
        }
    }

    @Override
    public void updateIsMedalRace(String leaderboardName, String columnName, boolean isMedalRace) {
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard != null) {
            leaderboard.updateIsMedalRace(columnName, isMedalRace);
            getService().updateStoredLeaderboard(leaderboard);
        } else {
            throw new IllegalArgumentException("Leaderboard named " + leaderboardName + " not found");
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
    public void trackWithSwissTiming(SwissTimingRaceRecordDTO rr, String hostname, int port, boolean canSendRequests,
            boolean trackWind, final boolean correctWindByDeclination) throws Exception {
        final RacesHandle raceHandle = getService().addSwissTimingRace(rr.ID, hostname, port, canSendRequests,
                MongoWindStoreFactory.INSTANCE.getMongoWindStore(mongoObjectFactory, domainObjectFactory),
                TIMEOUT_FOR_RECEIVING_RACE_DEFINITION_IN_MILLISECONDS);
        if (trackWind) {
            new Thread("Wind tracking starter for race "+rr.ID+"/"+rr.description) {
                public void run() {
                    try {
                        startTrackingWind(raceHandle, correctWindByDeclination, TIMEOUT_FOR_RECEIVING_RACE_DEFINITION_IN_MILLISECONDS);
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

    @Override
    public CompetitorInRaceDTO getCompetitorRaceData(RaceIdentifier race,
            CompetitorsAndTimePointsDTO competitorAndTimePointsDTO, DetailType dataType) throws NoWindException {
        CompetitorInRaceDTO competitorData = new CompetitorInRaceDTO();
        TrackedRace trackedRace = getExistingTrackedRace(race);
        if (trackedRace != null) {
            Iterable<Competitor> competitors = trackedRace.getRace().getCompetitors();
            List<Competitor> selectedCompetitor = new ArrayList<Competitor>();
            for (CompetitorDTO cDTO : competitorAndTimePointsDTO.getCompetitors()) {
                for (Competitor c : competitors) {
                    if (c.getId().toString().equals(cDTO.id)) {
                        selectedCompetitor.add(c);
                    }
                }
            }

            switch (dataType) {
            case CURRENT_SPEED_OVER_GROUND_IN_KNOTS:
                for (int c = 0; c < selectedCompetitor.size(); c++) {
                    Double[] entries = new Double[competitorAndTimePointsDTO.getTimePoints().length];
                    for (int i = 0; i < competitorAndTimePointsDTO.getTimePoints().length; i++) {
                        MillisecondsTimePoint time = new MillisecondsTimePoint(
                                competitorAndTimePointsDTO.getTimePoints()[i]);
                        TrackedLegOfCompetitor trackedLeg = trackedRace.getTrackedLeg(selectedCompetitor.get(c), time);
                        if (trackedLeg != null) {
                            SpeedWithBearing speedOverGround = trackedLeg.getSpeedOverGround(time);
                            entries[i] = (speedOverGround == null) ? null : speedOverGround.getKnots();
                        }
                    }
                    CompetitorDTO competitor = competitorAndTimePointsDTO.getCompetitors()[c];
                    competitorData.setRaceData(competitor, entries);
                    entries = new Double[competitorAndTimePointsDTO.getMarkPassings(competitor).length];
                    for (int i = 0; i < competitorAndTimePointsDTO.getMarkPassings(competitor).length; i++) {
                        MillisecondsTimePoint time = new MillisecondsTimePoint(
                                competitorAndTimePointsDTO.getMarkPassings(competitor)[i].getB());
                        TrackedLegOfCompetitor trackedLeg = trackedRace.getTrackedLeg(selectedCompetitor.get(c), time);
                        if (trackedLeg != null) {
                            SpeedWithBearing speedOverGround = trackedLeg.getSpeedOverGround(time);
                            entries[i] = (speedOverGround == null) ? null : speedOverGround.getKnots();
                        }
                    }
                    competitorData.setMarkPassingData(competitor, entries);
                }
                break;
            case VELOCITY_MADE_GOOD_IN_KNOTS:
                for (int c = 0; c < selectedCompetitor.size(); c++) {
                    Double[] entries = new Double[competitorAndTimePointsDTO.getTimePoints().length];
                    for (int i = 0; i < competitorAndTimePointsDTO.getTimePoints().length; i++) {
                        MillisecondsTimePoint time = new MillisecondsTimePoint(
                                competitorAndTimePointsDTO.getTimePoints()[i]);
                        TrackedLegOfCompetitor trackedLeg = trackedRace.getTrackedLeg(selectedCompetitor.get(c), time);
                        if (trackedLeg != null) {
                            Speed velocityMadeGood = trackedLeg.getVelocityMadeGood(time);
                            entries[i] = (velocityMadeGood == null) ? null : velocityMadeGood.getKnots();
                        }
                    }
                    competitorData.setRaceData(competitorAndTimePointsDTO.getCompetitors()[c], entries);
                    CompetitorDTO competitor = competitorAndTimePointsDTO.getCompetitors()[c];
                    entries = new Double[competitorAndTimePointsDTO.getMarkPassings(competitor).length];
                    for (int i = 0; i < competitorAndTimePointsDTO.getMarkPassings(competitor).length; i++) {
                        MillisecondsTimePoint time = new MillisecondsTimePoint(
                                competitorAndTimePointsDTO.getMarkPassings(competitor)[i].getB());
                        TrackedLegOfCompetitor trackedLeg = trackedRace.getTrackedLeg(selectedCompetitor.get(c), time);
                        if (trackedLeg != null) {
                            Speed velocityMadeGood = trackedLeg.getVelocityMadeGood(time);
                            entries[i] = (velocityMadeGood == null) ? null : velocityMadeGood.getKnots();
                        }
                    }
                    competitorData.setMarkPassingData(competitor, entries);
                }
                break;
            case DISTANCE_TRAVELED:
                for (int c = 0; c < selectedCompetitor.size(); c++) {
                    Double[] entries = new Double[competitorAndTimePointsDTO.getTimePoints().length];
                    double distanceOfPreviousLegs = 0;
                    double lastTraveledDistance = 0;
                    for (int i = 0; i < competitorAndTimePointsDTO.getTimePoints().length; i++) {
                        MillisecondsTimePoint time = new MillisecondsTimePoint(
                                competitorAndTimePointsDTO.getTimePoints()[i]);
                        TrackedLegOfCompetitor trackedLeg = trackedRace.getTrackedLeg(selectedCompetitor.get(c), time);
                        if (trackedLeg != null) {
                            Distance distanceTraveled = trackedLeg.getDistanceTraveled(time);
                            if (distanceTraveled != null) {
                                double d = distanceTraveled.getMeters();
                                if (d < lastTraveledDistance) {
                                    distanceOfPreviousLegs += lastTraveledDistance;
                                }
                                lastTraveledDistance = d;
                                entries[i] = d + distanceOfPreviousLegs;
                            }
                        }
                    }
                    competitorData.setRaceData(competitorAndTimePointsDTO.getCompetitors()[c], entries);
                    CompetitorDTO competitor = competitorAndTimePointsDTO.getCompetitors()[c];
                    entries = new Double[competitorAndTimePointsDTO.getMarkPassings(competitor).length];
                    for (int i = 0; i < competitorAndTimePointsDTO.getMarkPassings(competitor).length; i++) {
                        MillisecondsTimePoint time = new MillisecondsTimePoint(
                                competitorAndTimePointsDTO.getMarkPassings(competitor)[i].getB());
                        TrackedLegOfCompetitor trackedLeg = trackedRace.getTrackedLeg(selectedCompetitor.get(c), time);
                        if (trackedLeg != null) {
                            Distance distanceTraveled = trackedLeg.getDistanceTraveled(time);
                            if (distanceTraveled != null) {
                                double d = distanceTraveled.getMeters();
                                if (d < lastTraveledDistance) {
                                    distanceOfPreviousLegs += lastTraveledDistance;
                                }
                                lastTraveledDistance = d;
                                entries[i] = d + distanceOfPreviousLegs;
                            }
                        }
                    }
                    competitorData.setMarkPassingData(competitor, entries);
                }
                break;
            case GAP_TO_LEADER_IN_SECONDS:
                for (int c = 0; c < selectedCompetitor.size(); c++) {
                    Double[] entries = new Double[competitorAndTimePointsDTO.getTimePoints().length];
                    for (int i = 0; i < competitorAndTimePointsDTO.getTimePoints().length; i++) {
                        MillisecondsTimePoint time = new MillisecondsTimePoint(
                                competitorAndTimePointsDTO.getTimePoints()[i]);
                        TrackedLegOfCompetitor trackedLeg = trackedRace.getTrackedLeg(selectedCompetitor.get(c), time);
                        if (trackedLeg != null) {
                            entries[i] = trackedLeg.getGapToLeaderInSeconds(time);
                        }
                    }
                    competitorData.setRaceData(competitorAndTimePointsDTO.getCompetitors()[c], entries);
                    CompetitorDTO competitor = competitorAndTimePointsDTO.getCompetitors()[c];
                    entries = new Double[competitorAndTimePointsDTO.getMarkPassings(competitor).length];
                    for (int i = 0; i < competitorAndTimePointsDTO.getMarkPassings(competitor).length; i++) {
                        MillisecondsTimePoint time = new MillisecondsTimePoint(
                                competitorAndTimePointsDTO.getMarkPassings(competitor)[i].getB());
                        TrackedLegOfCompetitor trackedLeg = trackedRace.getTrackedLeg(selectedCompetitor.get(c), time);
                        if (trackedLeg != null) {
                            entries[i] = trackedLeg.getGapToLeaderInSeconds(time);
                        }
                    }
                    competitorData.setMarkPassingData(competitor, entries);
                }
                break;
            case WINDWARD_DISTANCE_TO_OVERALL_LEADER:
                for (int c = 0; c < selectedCompetitor.size(); c++) {
                    CompetitorDTO competitor = competitorAndTimePointsDTO.getCompetitors()[c];
                    Double[] entries = new Double[competitorAndTimePointsDTO.getTimePoints().length];
                    Double[] markEntries = new Double[competitorAndTimePointsDTO.getMarkPassings(competitor).length];
                    MillisecondsTimePoint markTime = new MillisecondsTimePoint(
                            competitorAndTimePointsDTO.getMarkPassings(competitor)[0].getB());
                    int markEntryCounter = 0;
                    TrackedLegOfCompetitor trackedLegforMark = trackedRace.getTrackedLeg(selectedCompetitor.get(c),
                            markTime);
                    for (int i = 0; i < competitorAndTimePointsDTO.getTimePoints().length; i++) {
                        MillisecondsTimePoint time = new MillisecondsTimePoint(
                                competitorAndTimePointsDTO.getTimePoints()[i]);
                        TrackedLegOfCompetitor trackedLeg = trackedRace.getTrackedLeg(selectedCompetitor.get(c), time);
                        if (trackedLeg != null) {
                            Distance distanceToLeader = trackedLeg.getWindwardDistanceToOverallLeader(time);
                            entries[i] = (distanceToLeader == null) ? null : distanceToLeader.getMeters();
                        }
                        if (trackedLegforMark != null && markTime.asMillis() > time.asMillis()
                                && markEntryCounter < competitorAndTimePointsDTO.getMarkPassings(competitor).length) {
                            Distance distanceToLeader = trackedLegforMark.getWindwardDistanceToOverallLeader(markTime);
                            markEntries[markEntryCounter] = (distanceToLeader == null) ? null : distanceToLeader
                                    .getMeters();
                            markEntryCounter++;
                            if (markEntryCounter < competitorAndTimePointsDTO.getMarkPassings(competitor).length) {
                                markTime = new MillisecondsTimePoint(
                                        competitorAndTimePointsDTO.getMarkPassings(competitor)[markEntryCounter].getB());
                                trackedLegforMark = trackedRace.getTrackedLeg(selectedCompetitor.get(c), markTime);
                            }
                        }
                    }
                    competitorData.setRaceData(competitor, entries);
                    competitorData.setMarkPassingData(competitor, markEntries);
                }
                break;
            }
        }
        return competitorData;
    }
    
    @Override
    public Map<CompetitorDTO, List<GPSFixDTO>> getDouglasPoints(RaceIdentifier raceIdentifier,
            Map<CompetitorDTO, Date> from, Map<CompetitorDTO, Date> to,
            double meters) throws NoWindException {
        Map<CompetitorDTO, List<GPSFixDTO>> result = new HashMap<CompetitorDTO, List<GPSFixDTO>>();
        TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
        if (trackedRace != null) {
            MeterDistance maxDistance = new MeterDistance(meters);
            for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
                CompetitorDTO competitorDTO = getCompetitorDTO(competitor);
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
                        GPSFixDTO fixDTO = createGPSFixDTO(fix, speedWithBearing, tack, legType, /* extrapolated */false);
                        gpsFixDouglasList.add(fixDTO);
                    }
                    result.put(competitorDTO, gpsFixDouglasList);
                }
            }
        }
        return result;
    }

    @Override
    public Map<CompetitorDTO, List<ManeuverDTO>> getManeuvers(RaceIdentifier raceIdentifier,
            Map<CompetitorDTO, Date> from, Map<CompetitorDTO, Date> to) throws NoWindException {
        Map<CompetitorDTO, List<ManeuverDTO>> result = new HashMap<CompetitorDTO, List<ManeuverDTO>>();
        final TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
        if (trackedRace != null) {
            Map<CompetitorDTO, Future<List<ManeuverDTO>>> futures = new HashMap<CompetitorDTO, Future<List<ManeuverDTO>>>();
            for (final Competitor competitor : trackedRace.getRace().getCompetitors()) {
                CompetitorDTO competitorDTO = getCompetitorDTO(competitor);
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
                                                timePointTo);
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
            ManeuverDTO maneuverDTO = new ManeuverDTO(maneuver.getType(), maneuver.getNewTack(),
                    new PositionDTO(maneuver.getPosition().getLatDeg(), maneuver.getPosition().getLngDeg()), 
                    maneuver.getTimePoint().asDate(),
                    createSpeedWithBearingDTO(maneuver.getSpeedWithBearingBefore()),
                    createSpeedWithBearingDTO(maneuver.getSpeedWithBearingAfter()),
                    maneuver.getDirectionChangeInDegrees());
            result.add(maneuverDTO);
        }
        return result;
    }

    @Override
    public RaceDefinition getRace(EventNameAndRaceName eventNameAndRaceName) {
        Event event = getService().getEventByName(eventNameAndRaceName.getEventName());
        RaceDefinition race = getRaceByName(event, eventNameAndRaceName.getRaceName());
        return race;
    }

    @Override
    public TrackedRace getTrackedRace(EventNameAndRaceName eventNameAndRaceName) {
        Event event = getService().getEventByName(eventNameAndRaceName.getEventName());
        RaceDefinition race = getRaceByName(event, eventNameAndRaceName.getRaceName());
        TrackedRace trackedRace = getService().getOrCreateTrackedEvent(event).getTrackedRace(race);
        return trackedRace;
    }

    @Override
    public RaceDefinition getRace(LeaderboardNameAndRaceColumnName leaderboardNameAndRaceColumnName) {
        RaceDefinition result = null;
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardNameAndRaceColumnName.getLeaderboardName());
        if (leaderboard != null) {
            RaceInLeaderboard raceColumn = leaderboard.getRaceColumnByName(leaderboardNameAndRaceColumnName
                    .getRaceColumnName());
            if (raceColumn != null && raceColumn.getTrackedRace() != null) {
                result = raceColumn.getTrackedRace().getRace();
            }
        }
        return result;
    }

    @Override
    public TrackedRace getTrackedRace(LeaderboardNameAndRaceColumnName leaderboardNameAndRaceColumnName) {
        TrackedRace result = null;
        Leaderboard leaderboard = getService().getLeaderboardByName(
                leaderboardNameAndRaceColumnName.getLeaderboardName());
        if (leaderboard != null) {
            RaceInLeaderboard raceColumn = leaderboard.getRaceColumnByName(leaderboardNameAndRaceColumnName
                    .getRaceColumnName());
            if (raceColumn != null) {
                result = raceColumn.getTrackedRace();
            }
        }
        return result;
    }
    
    @Override
    public TrackedRace getExistingTrackedRace(LeaderboardNameAndRaceColumnName leaderboardNameAndRaceColumnName) {
        return getTrackedRace(leaderboardNameAndRaceColumnName);
    }
    
    @Override
    public TrackedRace getExistingTrackedRace(EventNameAndRaceName eventNameAndRaceName) {
        Event event = getService().getEventByName(eventNameAndRaceName.getEventName());
        RaceDefinition race = getRaceByName(event, eventNameAndRaceName.getRaceName());
        TrackedRace trackedRace = getService().getOrCreateTrackedEvent(event).getExistingTrackedRace(race);
        return trackedRace;
    }
    
    @Override
    public Event getEvent(EventName eventIdentifier) {
        return getService().getEventByName(eventIdentifier.getEventName());
    }

    private RaceDefinition getRace(RaceIdentifier raceIdentifier) {
        return (RaceDefinition) raceIdentifier.getRace(this);
    }

    private TrackedRace getExistingTrackedRace(RaceIdentifier raceIdentifier) {
        return (TrackedRace) raceIdentifier.getExistingTrackedRace(this);
    }
    
    private Event getEvent(EventIdentifier eventIdentifier) {
        return (Event) eventIdentifier.getEvent(this);
    }

    @SuppressWarnings("unchecked")
	@Override
    public CompetitorsAndTimePointsDTO getCompetitorsAndTimePoints(RaceIdentifier race, int steps) {
        CompetitorsAndTimePointsDTO competitorAndTimePointsDTO = new CompetitorsAndTimePointsDTO(steps);
        TrackedRace trackedRace = getExistingTrackedRace(race);
        if (trackedRace != null) {
            List<CompetitorDTO> competitors = new ArrayList<CompetitorDTO>();
            for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
                NavigableSet<MarkPassing> markPassings = trackedRace.getMarkPassings(competitor);
                List<Pair<String, Long>> markPassingTimes = new ArrayList<Pair<String, Long>>();
                for (MarkPassing markPassing : markPassings) {
                    markPassingTimes.add(new Pair<String, Long>(markPassing.getWaypoint().getName(), markPassing
                            .getTimePoint().asMillis()));
                }
                competitors.add(getCompetitorDTO(competitor));
                // The following line will create a "Unchecked type safety warning".
                // There is no way to solve this, so it is okay to suppress this warning.
                competitorAndTimePointsDTO.setMarkPassings(getCompetitorDTO(competitor),
                        markPassingTimes.toArray(new Pair[0]));
            }
            competitorAndTimePointsDTO.setCompetitors(competitors.toArray(new CompetitorDTO[0]));
            competitorAndTimePointsDTO.setStartTime(trackedRace.getStart().asMillis());
            competitorAndTimePointsDTO.setTimePointOfNewestEvent(trackedRace.getTimePointOfNewestEvent().asMillis());
        }
        return competitorAndTimePointsDTO;
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
     * Override of funtion to prevent exception "Blocked request without GWT permutation header (XSRF attack?)" when testing the GWT sites
     */
    protected void checkPermutationStrongName() throws SecurityException {
        //Override to prevent exception "Blocked request without GWT permutation header (XSRF attack?)" when testing the GWT sites
        return;
    }

}