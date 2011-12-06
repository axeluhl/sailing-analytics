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
import java.util.Set;
import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sap.sailing.domain.base.Bearing;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Speed;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.Tack;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.DegreeBearingImpl;
import com.sap.sailing.domain.base.impl.DegreePosition;
import com.sap.sailing.domain.base.impl.KilometersPerHourSpeedImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MeterDistance;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
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
import com.sap.sailing.domain.tracking.NoWindException;
import com.sap.sailing.domain.tracking.RacesHandle;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindSource;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.domain.tracking.impl.WindTrackImpl;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.RaceRecord;
import com.sap.sailing.domain.tractracadapter.TracTracConfiguration;
import com.sap.sailing.gwt.ui.client.DetailType;
import com.sap.sailing.gwt.ui.client.SailingService;
import com.sap.sailing.gwt.ui.shared.BoatClassDAO;
import com.sap.sailing.gwt.ui.shared.CompetitorDAO;
import com.sap.sailing.gwt.ui.shared.CompetitorAndTimePointsDAO;
import com.sap.sailing.gwt.ui.shared.EventAndRaceIdentifier;
import com.sap.sailing.gwt.ui.shared.EventDAO;
import com.sap.sailing.gwt.ui.shared.EventFetcher;
import com.sap.sailing.gwt.ui.shared.EventIdentifier;
import com.sap.sailing.gwt.ui.shared.EventNameAndRaceName;
import com.sap.sailing.gwt.ui.shared.GPSFixDAO;
import com.sap.sailing.gwt.ui.shared.LeaderboardDAO;
import com.sap.sailing.gwt.ui.shared.LeaderboardEntryDAO;
import com.sap.sailing.gwt.ui.shared.LeaderboardNameAndRaceColumnName;
import com.sap.sailing.gwt.ui.shared.LeaderboardRowDAO;
import com.sap.sailing.gwt.ui.shared.LegEntryDAO;
import com.sap.sailing.gwt.ui.shared.ManeuverDAO;
import com.sap.sailing.gwt.ui.shared.MarkDAO;
import com.sap.sailing.gwt.ui.shared.Pair;
import com.sap.sailing.gwt.ui.shared.PositionDAO;
import com.sap.sailing.gwt.ui.shared.QuickRankDAO;
import com.sap.sailing.gwt.ui.shared.RaceDAO;
import com.sap.sailing.gwt.ui.shared.RaceFetcher;
import com.sap.sailing.gwt.ui.shared.RaceIdentifier;
import com.sap.sailing.gwt.ui.shared.RegattaDAO;
import com.sap.sailing.gwt.ui.shared.SpeedWithBearingDAO;
import com.sap.sailing.gwt.ui.shared.SwissTimingConfigurationDAO;
import com.sap.sailing.gwt.ui.shared.SwissTimingRaceRecordDAO;
import com.sap.sailing.gwt.ui.shared.TracTracConfigurationDAO;
import com.sap.sailing.gwt.ui.shared.TracTracRaceRecordDAO;
import com.sap.sailing.gwt.ui.shared.WindDAO;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDAO;
import com.sap.sailing.gwt.ui.shared.WindTrackInfoDAO;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.util.CountryCode;

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
    
    private final com.sap.sailing.util.CountryCodeFactory countryCodeFactory;

    public SailingServiceImpl() {
        BundleContext context = Activator.getDefault();
        racingEventServiceTracker = createAndOpenRacingEventServiceTracker(context);
        mongoObjectFactory = MongoObjectFactory.INSTANCE;
        domainObjectFactory = DomainObjectFactory.INSTANCE;
        swissTimingAdapterPersistence = SwissTimingAdapterPersistence.INSTANCE;
        tractracDomainObjectFactory = com.sap.sailing.domain.tractracadapter.persistence.DomainObjectFactory.INSTANCE;
        tractracMongoObjectFactory = com.sap.sailing.domain.tractracadapter.persistence.MongoObjectFactory.INSTANCE;
        swissTimingFactory = SwissTimingFactory.INSTANCE;
        countryCodeFactory = com.sap.sailing.util.CountryCodeFactory.INSTANCE;
    }

    protected ServiceTracker<RacingEventService, RacingEventService> createAndOpenRacingEventServiceTracker(
            BundleContext context) {
        ServiceTracker<RacingEventService, RacingEventService> result = new ServiceTracker<RacingEventService, RacingEventService>(
                context, RacingEventService.class.getName(), null);
        result.open();
        return result;
    }
    
    @Override
    public LeaderboardDAO getLeaderboardByName(String leaderboardName, Date date,
            Collection<String> namesOfRacesForWhichToLoadLegDetails) throws Exception {
        long startOfRequestHandling = System.currentTimeMillis();
        LeaderboardDAO result = null;
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard != null) {
            result = new LeaderboardDAO();
            TimePoint timePoint = new MillisecondsTimePoint(date);
            result.competitors = new ArrayList<CompetitorDAO>();
            result.name = leaderboard.getName();
            result.displayNames = new HashMap<CompetitorDAO, String>();
            for (RaceInLeaderboard raceColumn : leaderboard.getRaceColumns()) {
                //result.raceNamesAndMedalRaceAndTracked.put(raceColumn.getName(),
                //        new Pair<Boolean, Boolean>(raceColumn.isMedalRace(), raceColumn.getTrackedRace()!=null));
                result.addRace(raceColumn.getName(), raceColumn.isMedalRace(), raceColumn.getTrackedRace() != null);
            }
            result.rows = new HashMap<CompetitorDAO, LeaderboardRowDAO>();
            result.hasCarriedPoints = leaderboard.hasCarriedPoints();
            result.discardThresholds = leaderboard.getResultDiscardingRule().getDiscardIndexResultsStartingWithHowManyRaces();
            for (Competitor competitor : leaderboard.getCompetitors()) {
                CompetitorDAO competitorDAO = getCompetitorDAO(competitor);
                LeaderboardRowDAO row = new LeaderboardRowDAO();
                row.competitor = competitorDAO;
                row.fieldsByRaceName = new HashMap<String, LeaderboardEntryDAO>();
                row.carriedPoints = leaderboard.hasCarriedPoints(competitor) ? leaderboard.getCarriedPoints(competitor) : null;
                result.competitors.add(competitorDAO);
                for (RaceInLeaderboard raceColumn : leaderboard.getRaceColumns()) {
                    Entry entry = leaderboard.getEntry(competitor, raceColumn, timePoint);
                    LeaderboardEntryDAO entryDAO = getLeaderboardEntryDAO(entry, raceColumn.getTrackedRace(),
                            competitor, timePoint, namesOfRacesForWhichToLoadLegDetails != null
                                    && namesOfRacesForWhichToLoadLegDetails.contains(raceColumn.getName()));
                    row.fieldsByRaceName.put(raceColumn.getName(), entryDAO);
                    result.rows.put(competitorDAO, row);
                }
                String displayName = leaderboard.getDisplayName(competitor);
                if (displayName != null) {
                    result.displayNames.put(competitorDAO, displayName);
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
    
    @Override
    public LeaderboardEntryDAO getLeaderboardEntry(String leaderboardName, String competitorName, String raceName, Date date) throws NoWindException {
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard != null) {
            Competitor competitor = leaderboard.getCompetitorByName(competitorName);
            if (competitor != null) {
                RaceInLeaderboard raceColumn = leaderboard.getRaceColumnByName(raceName);
                if (raceColumn != null) {
                    MillisecondsTimePoint timePoint = new MillisecondsTimePoint(date);
                    return getLeaderboardEntryDAO(leaderboard.getEntry(competitor, raceColumn, timePoint),
                            raceColumn.getTrackedRace(), competitor, timePoint, /* addLegDetails */ false);
                } else {
                    throw new IllegalArgumentException("Didn't find race "+raceName+" in leaderboard "+leaderboardName);
                }
            } else {
                throw new IllegalArgumentException("Didn't find competitor "+competitorName+" in leaderboard "+leaderboardName);
            }
        } else {
            throw new IllegalArgumentException("Didn't find leaderboard "+leaderboardName);
        }
    }
    
    private LeaderboardEntryDAO getLeaderboardEntryDAO(Entry entry, TrackedRace trackedRace, Competitor competitor,
            TimePoint timePoint, boolean addLegDetails) throws NoWindException {
        LeaderboardEntryDAO entryDAO = new LeaderboardEntryDAO();
        entryDAO.netPoints = entry.getNetPoints();
        entryDAO.totalPoints = entry.getTotalPoints();
        entryDAO.reasonForMaxPoints = entry.getMaxPointsReason().name();
        entryDAO.discarded = entry.isDiscarded();
        if (addLegDetails && trackedRace != null) {
            entryDAO.legDetails = new ArrayList<LegEntryDAO>();
            for (Leg leg : trackedRace.getRace().getCourse().getLegs()) {
                TrackedLegOfCompetitor trackedLeg = trackedRace.getTrackedLeg(competitor, leg);
                LegEntryDAO legEntry;
                if (trackedLeg != null && trackedLeg.hasStartedLeg(timePoint)) {
                    legEntry = createLegEntry(trackedLeg, timePoint);
                } else {
                    legEntry = null;
                }
                entryDAO.legDetails.add(legEntry);
            }
        }
        return entryDAO;
    }

    private LegEntryDAO createLegEntry(TrackedLegOfCompetitor trackedLeg, TimePoint timePoint) throws NoWindException {
        LegEntryDAO result;
        if (trackedLeg == null) {
            result = null;
        } else {
            result = new LegEntryDAO();
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

    public List<EventDAO> listEvents() throws IllegalArgumentException {
        List<EventDAO> result = new ArrayList<EventDAO>();
        for (Event event : getService().getAllEvents()) {
            List<CompetitorDAO> competitorList = getCompetitorDAOs(event.getCompetitors());
            List<RegattaDAO> regattasList = getRegattaDAOs(event);
            EventDAO eventDAO = new EventDAO(event.getName(), regattasList, competitorList);
            if (!eventDAO.regattas.isEmpty()) {
                result.add(eventDAO);
            }
        }
        return result;
    }

    private List<RegattaDAO> getRegattaDAOs(Event event) {
        Map<BoatClass, Set<RaceDefinition>> racesByBoatClass = new HashMap<BoatClass, Set<RaceDefinition>>();
        for (RaceDefinition r : event.getAllRaces()) {
            Set<RaceDefinition> racesForBoatClass = racesByBoatClass.get(r.getBoatClass());
            if (racesForBoatClass == null) {
                racesForBoatClass = new HashSet<RaceDefinition>();
                racesByBoatClass.put(r.getBoatClass(), racesForBoatClass);
            }
            racesForBoatClass.add(r);
        }
        List<RegattaDAO> result = new ArrayList<RegattaDAO>();
        for (Map.Entry<BoatClass, Set<RaceDefinition>> e : racesByBoatClass.entrySet()) {
            List<RaceDAO> raceDAOsInBoatClass = getRaceDAOs(event, e.getValue());
            if (!raceDAOsInBoatClass.isEmpty()) {
                RegattaDAO regatta = new RegattaDAO(new BoatClassDAO(e.getKey()==null?"":e.getKey().getName()), raceDAOsInBoatClass);
                result.add(regatta);
            }
        }
        return result;
    }

    private List<RaceDAO> getRaceDAOs(Event event, Set<RaceDefinition> races) {
        List<RaceDAO> result = new ArrayList<RaceDAO>();
        for (RaceDefinition r : races) {
            RaceDAO raceDAO = new RaceDAO(r.getName(), getCompetitorDAOs(r.getCompetitors()), getService().isRaceBeingTracked(r));
            if (raceDAO.currentlyTracked) {
                TrackedRace trackedRace = getService().getTrackedRace(event, r);
                raceDAO.startOfRace = trackedRace.getStart() == null ? null : trackedRace.getStart().asDate();
                raceDAO.startOfTracking = trackedRace.getStartOfTracking() == null ? null : trackedRace.getStartOfTracking().asDate();
                raceDAO.timePointOfLastEvent = trackedRace.getTimePointOfLastEvent() == null ? null : trackedRace.getTimePointOfLastEvent().asDate();
                raceDAO.timePointOfNewestEvent = trackedRace.getTimePointOfNewestEvent() == null ? null : trackedRace.getTimePointOfNewestEvent().asDate();
            }
            result.add(raceDAO);
        }
        return result;
    }

    private List<CompetitorDAO> getCompetitorDAOs(Iterable<Competitor> competitors) {
        List<CompetitorDAO> result = new ArrayList<CompetitorDAO>();
        for (Competitor c : competitors) {
            CompetitorDAO competitorDAO = getCompetitorDAO(c);
            result.add(competitorDAO);
        }
        return result;
    }

    private CompetitorDAO getCompetitorDAO(Competitor c) {
        CountryCode countryCode = c.getTeam().getNationality().getCountryCode();
        CompetitorDAO competitorDAO = new CompetitorDAO(c.getName(), countryCode==null?"":countryCode.getTwoLetterISOCode(),
                countryCode==null?"":countryCode.getThreeLetterIOCCode(), countryCode==null?"":countryCode.getName(), c.getBoat().getSailID(),
                        c.getId().toString());
        return competitorDAO;
    }

    @Override
    public Pair<String, List<TracTracRaceRecordDAO>> listTracTracRacesInEvent(String eventJsonURL) throws MalformedURLException, IOException,
            ParseException, org.json.simple.parser.ParseException, URISyntaxException {
        com.sap.sailing.util.Util.Pair<String,List<RaceRecord>> raceRecords;
        raceRecords = getService().getTracTracRaceRecords(new URL(eventJsonURL));
        List<TracTracRaceRecordDAO> result = new ArrayList<TracTracRaceRecordDAO>();
        for (RaceRecord raceRecord : raceRecords.getB()) {
            result.add(new TracTracRaceRecordDAO(raceRecord.getID(), raceRecord.getEventName(), raceRecord.getName(),
                    raceRecord.getParamURL().toString(), raceRecord.getReplayURL(), raceRecord.getLiveURI().toString(),
                    raceRecord.getStoredURI().toString(), raceRecord.getTrackingStartTime().asDate(), raceRecord
                            .getTrackingEndTime().asDate(), raceRecord.getRaceStartTime().asDate()));
        }
        return new Pair<String, List<TracTracRaceRecordDAO>>(raceRecords.getA(), result);
    }

    @Override
    public void track(TracTracRaceRecordDAO rr, String liveURI, String storedURI, boolean trackWind, final boolean correctWindByDeclination) throws Exception {
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
    public List<TracTracConfigurationDAO> getPreviousTracTracConfigurations() throws Exception {
        Iterable<TracTracConfiguration> configs = tractracDomainObjectFactory.getTracTracConfigurations();
        List<TracTracConfigurationDAO> result = new ArrayList<TracTracConfigurationDAO>();
        for (TracTracConfiguration ttConfig : configs) {
            result.add(new TracTracConfigurationDAO(ttConfig.getName(), ttConfig.getJSONURL().toString(),
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
            for (RaceDefinition r : event.getAllRaces()) {
                if (r.getName().equals(raceName)) {
                    return r;
                }
            }
        }
        return null;
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
    public WindInfoForRaceDAO getWindInfo(RaceIdentifier raceIdentifier, Date fromDate, Date toDate, boolean includeTrackBasedWindEstimation) {
        WindInfoForRaceDAO result = null;
        TrackedRace trackedRace = getTrackedRace(raceIdentifier);
        if (trackedRace != null) {
            MillisecondsTimePoint now = MillisecondsTimePoint.now();
            result = new WindInfoForRaceDAO();
            result.selectedWindSourceName = trackedRace.getWindSource().name();
            TimePoint from = new MillisecondsTimePoint(fromDate);
            TimePoint to = new MillisecondsTimePoint(toDate);
            Map<String, WindTrackInfoDAO> windTrackInfoDAOs = new HashMap<String, WindTrackInfoDAO>();
            WindTrack estimatedTrack = null;
            result.windTrackInfoByWindSourceName = windTrackInfoDAOs;
            for (WindSource windSource : WindSource.values()) {
                WindTrackInfoDAO windTrackInfoDAO = new WindTrackInfoDAO();
                windTrackInfoDAO.windFixes = new ArrayList<WindDAO>();
                WindTrack windTrack = trackedRace.getWindTrack(windSource);
                windTrackInfoDAO.dampeningIntervalInMilliseconds = windTrack.getMillisecondsOverWhichToAverageWind();
                if (includeTrackBasedWindEstimation && windSource == trackedRace.getWindSource()) {
                    estimatedTrack = new WindTrackImpl(windTrack.getMillisecondsOverWhichToAverageWind());
                }
                Iterator<Wind> windIter = windTrack.getFixesIterator(from, /* inclusive */true);
                while (windIter.hasNext()) {
                    Wind wind = windIter.next();
                    if (wind.getTimePoint().compareTo(to) > 0) {
                        break;
                    }
                    WindDAO windDAO = createWindDAO(wind, windTrack);
                    windTrackInfoDAO.windFixes.add(windDAO);
                    if (includeTrackBasedWindEstimation && windSource == trackedRace.getWindSource()) {
                        try {
                            Wind estimatedWindDirection = trackedRace.getEstimatedWindDirection(wind.getPosition(), wind.getTimePoint());
                            estimatedTrack.add(estimatedWindDirection);
                        } catch (NoWindException e) {
                            // no show-stopper; it would just mean that the wind estimation isn't complete which we can tolerate
                            e.printStackTrace();
                        }
                    }
                }
                windTrackInfoDAOs.put(windSource.name(), windTrackInfoDAO);
                // add one "track" with a single wind direction computed from the direction from the start gate to the
                // first mark
                WindTrack windwardMarkWindTrack = new WindTrackImpl(windTrack.getMillisecondsOverWhichToAverageWind());
                windwardMarkWindTrack.add(trackedRace.getDirectionFromStartToNextMark(now));
                windTrackInfoDAOs.put("COURSEBASED", createWindTrackInfoDAO(windwardMarkWindTrack));
            }
            if (includeTrackBasedWindEstimation && estimatedTrack != null) {
                boolean hasNext;
                synchronized (estimatedTrack) {
                    hasNext = estimatedTrack.getFixes().iterator().hasNext();
                }
                if (!hasNext) {
                    // empty wind estimation track; add at least one estimate for the current time for the start gate
                    Wind estimatedWindDirectionForNow;
                    try {
                        estimatedWindDirectionForNow = trackedRace.getEstimatedWindDirection(
                                trackedRace.getApproximatePosition(trackedRace.getRace().getCourse().getWaypoints().iterator().next(), now), now);
                        estimatedTrack.add(estimatedWindDirectionForNow);
                    } catch (NoWindException e) {
                        e.printStackTrace(); // well, then we just can't add a wind estimate
                    }
                }
                WindTrackInfoDAO windEstimations = createWindTrackInfoDAO(estimatedTrack);
                windTrackInfoDAOs.put("ESTIMATION", windEstimations);
            }
        }
        return result;
    }

    private WindTrackInfoDAO createWindTrackInfoDAO(WindTrack estimatedTrack) {
        WindTrackInfoDAO windEstimations = new WindTrackInfoDAO();
        windEstimations.dampeningIntervalInMilliseconds = estimatedTrack.getMillisecondsOverWhichToAverageWind();
        windEstimations.windFixes = new ArrayList<WindDAO>();
        synchronized (estimatedTrack) {
            for (Wind estimatedWind : estimatedTrack.getFixes()) {
                windEstimations.windFixes.add(createWindDAO(estimatedWind, estimatedTrack));
            }
        }
        return windEstimations;
    }

    protected WindDAO createWindDAO(Wind wind, WindTrack windTrack) {
        WindDAO windDAO = new WindDAO();
        windDAO.trueWindBearingDeg = wind.getBearing().getDegrees();
        windDAO.trueWindFromDeg = wind.getBearing().reverse().getDegrees();
        windDAO.trueWindSpeedInKnots = wind.getKnots();
        windDAO.trueWindSpeedInMetersPerSecond = wind.getMetersPerSecond();
        if (wind.getPosition() != null) {
            windDAO.position = new PositionDAO(wind.getPosition().getLatDeg(), wind.getPosition()
                    .getLngDeg());
        }
        if (wind.getTimePoint() != null) {
            windDAO.timepoint = wind.getTimePoint().asMillis();
            Wind estimatedWind = windTrack
                    .getEstimatedWind(wind.getPosition(), wind.getTimePoint());
            windDAO.dampenedTrueWindBearingDeg = estimatedWind.getBearing().getDegrees();
            windDAO.dampenedTrueWindFromDeg = estimatedWind.getBearing().reverse().getDegrees();
            windDAO.dampenedTrueWindSpeedInKnots = estimatedWind.getKnots();
            windDAO.dampenedTrueWindSpeedInMetersPerSecond = estimatedWind.getMetersPerSecond();
        }
        return windDAO;
    }
    
    @Override
    public WindInfoForRaceDAO getWindInfo(RaceIdentifier raceIdentifier, Date from, long millisecondsStepWidth,
            int numberOfFixes, double latDeg, double lngDeg, boolean includeTrackBasedWindEstimation)
            throws NoWindException {
        Position position = new DegreePosition(latDeg, lngDeg);
        WindInfoForRaceDAO result = null;
        TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
        if (trackedRace != null) {
            WindTrack estimatedTrack = null;
            result = new WindInfoForRaceDAO();
            WindSource windSource = trackedRace.getWindSource();
            result.selectedWindSourceName = windSource.name();
            TimePoint fromTimePoint = new MillisecondsTimePoint(from);
            Map<String, WindTrackInfoDAO> windTrackInfoDAOs = new HashMap<String, WindTrackInfoDAO>();
            result.windTrackInfoByWindSourceName = windTrackInfoDAOs;
            WindTrackInfoDAO windTrackInfoDAO = new WindTrackInfoDAO();
            windTrackInfoDAO.windFixes = new ArrayList<WindDAO>();
            WindTrack windTrack = trackedRace.getWindTrack(windSource);
            if (includeTrackBasedWindEstimation && windSource == WindSource.EXPEDITION) {
                estimatedTrack = new WindTrackImpl(windTrack.getMillisecondsOverWhichToAverageWind());
            }
            windTrackInfoDAOs.put(windSource.name(), windTrackInfoDAO);
            windTrackInfoDAO.dampeningIntervalInMilliseconds = windTrack.getMillisecondsOverWhichToAverageWind();
            TimePoint timePoint = fromTimePoint;
            for (int i = 0; i < numberOfFixes; i++) {
                Wind wind = windTrack.getEstimatedWind(position, timePoint);
                if (wind != null) {
                    WindDAO windDAO = createWindDAO(wind, windTrack);
                    windTrackInfoDAO.windFixes.add(windDAO);
                }
                if (includeTrackBasedWindEstimation && windSource == WindSource.EXPEDITION && wind != null) {
                    try {
                        Wind estimatedWindDirection = trackedRace.getEstimatedWindDirection(wind.getPosition(),
                                wind.getTimePoint());
                        estimatedTrack.add(estimatedWindDirection);
                    } catch (NoWindException e) {
                        // no show-stopper; it would just mean that the wind estimation isn't complete which we
                        // can tolerate
                        e.printStackTrace();
                    }
                }
                timePoint = new MillisecondsTimePoint(timePoint.asMillis() + millisecondsStepWidth);
            }
            if (includeTrackBasedWindEstimation && estimatedTrack != null) {
                WindTrackInfoDAO windEstimations = createWindTrackInfoDAO(estimatedTrack);
                windTrackInfoDAOs.put("ESTIMATION", windEstimations);
            }
        }
        return result;
    }
    
    @Override
    public void setWind(RaceIdentifier raceIdentifier, WindDAO windDAO) {
        DynamicTrackedRace trackedRace = (DynamicTrackedRace) getTrackedRace(raceIdentifier);
        if (trackedRace != null) {
            Position p = null;
            if (windDAO.position != null) {
                p = new DegreePosition(windDAO.position.latDeg, windDAO.position.lngDeg);
            }
            TimePoint at = null;
            if (windDAO.timepoint != null) {
                at = new MillisecondsTimePoint(windDAO.timepoint);
            }
            SpeedWithBearing speedWithBearing = null;
            Speed speed = null;
            if (windDAO.trueWindSpeedInKnots != null) {
                speed = new KnotSpeedImpl(windDAO.trueWindSpeedInKnots);
            } else if (windDAO.trueWindSpeedInMetersPerSecond != null) {
                speed = new KilometersPerHourSpeedImpl(windDAO.trueWindSpeedInMetersPerSecond * 3600. / 1000.);
            } else if (windDAO.dampenedTrueWindSpeedInKnots != null) {
                speed = new KnotSpeedImpl(windDAO.dampenedTrueWindSpeedInKnots);
            } else if (windDAO.dampenedTrueWindSpeedInMetersPerSecond != null) {
                speed = new KilometersPerHourSpeedImpl(windDAO.dampenedTrueWindSpeedInMetersPerSecond * 3600. / 1000.);
            }
            if (speed != null) {
                if (windDAO.trueWindBearingDeg != null) {
                    speedWithBearing = new KnotSpeedWithBearingImpl(speed.getKnots(), new DegreeBearingImpl(
                            windDAO.trueWindBearingDeg));
                } else if (windDAO.trueWindFromDeg != null) {
                    speedWithBearing = new KnotSpeedWithBearingImpl(speed.getKnots(), new DegreeBearingImpl(
                            windDAO.trueWindFromDeg).reverse());
                }
            }
            Wind wind = new WindImpl(p, at, speedWithBearing);
            trackedRace.recordWind(wind, WindSource.WEB);
        }
    }
    
    @Override
    public void setWindSource(RaceIdentifier raceIdentifier, String windSourceName) {
        TrackedRace trackedRace = getTrackedRace(raceIdentifier);
        if (trackedRace != null) {
            trackedRace.setWindSource(WindSource.valueOf(windSourceName));
        }
    }

    @Override
    public Map<CompetitorDAO, List<GPSFixDAO>> getBoatPositions(RaceIdentifier raceIdentifier,
            Map<CompetitorDAO, Date> from, Map<CompetitorDAO, Date> to,
            boolean extrapolate) {
        Map<CompetitorDAO, List<GPSFixDAO>> result = new HashMap<CompetitorDAO, List<GPSFixDAO>>();
        TrackedRace trackedRace = getTrackedRace(raceIdentifier);
        if (trackedRace != null) {
            for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
                CompetitorDAO competitorDAO = getCompetitorDAO(competitor);
                if (from.containsKey(competitorDAO)) {
                    List<GPSFixDAO> fixesForCompetitor = new ArrayList<GPSFixDAO>();
                    result.put(competitorDAO, fixesForCompetitor);
                    GPSFixTrack<Competitor, GPSFixMoving> track = trackedRace.getTrack(competitor);
                    TimePoint fromTimePoint = new MillisecondsTimePoint(from.get(competitorDAO));
                    TimePoint toTimePointExcluding = new MillisecondsTimePoint(to.get(competitorDAO));
                    Iterator<GPSFixMoving> fixIter;
                    synchronized (track) {
                        fixIter = track.getFixesIterator(fromTimePoint, /* inclusive */true);
                        if (fixIter.hasNext()) {
                            GPSFixMoving fix = fixIter.next();
                            while (fix != null && fix.getTimePoint().compareTo(toTimePointExcluding) < 0) {
                                Tack tack = trackedRace.getTack(competitor, fix.getTimePoint());
                                GPSFixDAO fixDAO = createGPSFixDAO(fix, fix.getSpeed(), tack, /* extrapolate */false);
                                fixesForCompetitor.add(fixDAO);
                                if (fixIter.hasNext()) {
                                    fix = fixIter.next();
                                } else {
                                    // check if fix was at date and if extrapolation is requested
                                    if (!fix.getTimePoint().equals(toTimePointExcluding) && extrapolate) {
                                        Position position = track.getEstimatedPosition(toTimePointExcluding,
                                                extrapolate);
                                        Tack tack2 = trackedRace.getTack(competitor, toTimePointExcluding);
                                        SpeedWithBearing speedWithBearing = track
                                                .getEstimatedSpeed(toTimePointExcluding);
                                        GPSFixDAO extrapolated = new GPSFixDAO(to.get(competitorDAO), new PositionDAO(
                                                position.getLatDeg(), position.getLngDeg()),
                                                createSpeedWithBearingDAO(speedWithBearing), tack2.name(), /* extrapolated */
                                                true);
                                        fixesForCompetitor.add(extrapolated);
                                    }
                                    fix = null;
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    private SpeedWithBearingDAO createSpeedWithBearingDAO(SpeedWithBearing speedWithBearing) {
        return new SpeedWithBearingDAO(speedWithBearing.getKnots(), speedWithBearing
                .getBearing().getDegrees());
    }

    private GPSFixDAO createGPSFixDAO(GPSFix fix, SpeedWithBearing speedWithBearing, Tack tack, boolean extrapolated) {
        return new GPSFixDAO(fix.getTimePoint().asDate(), new PositionDAO(fix
                .getPosition().getLatDeg(), fix.getPosition().getLngDeg()),
                createSpeedWithBearingDAO(speedWithBearing), tack.name(), extrapolated);
    }

    @Override
    public List<MarkDAO> getMarkPositions(RaceIdentifier raceIdentifier, Date date) {
        List<MarkDAO> result = new ArrayList<MarkDAO>();
        if (date != null) {
            TimePoint dateAsTimePoint = new MillisecondsTimePoint(date);
            TrackedRace trackedRace = getTrackedRace(raceIdentifier);
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
                        MarkDAO markDAO = new MarkDAO(buoy.getName(), positionAtDate.getLatDeg(),
                                positionAtDate.getLngDeg());
                        result.add(markDAO);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public List<QuickRankDAO> getQuickRanks(RaceIdentifier raceIdentifier, Date date) throws NoWindException {
        List<QuickRankDAO> result = new ArrayList<QuickRankDAO>();
        if (date != null) {
            TimePoint dateAsTimePoint = new MillisecondsTimePoint(date);
            TrackedRace trackedRace = getTrackedRace(raceIdentifier);
            if (trackedRace != null) {
                RaceDefinition race = trackedRace.getRace();
                for (Competitor competitor : race.getCompetitors()) {
                    int rank = trackedRace.getRank(competitor, dateAsTimePoint);
                    TrackedLegOfCompetitor trackedLeg = trackedRace.getTrackedLeg(competitor, dateAsTimePoint);
                    if (trackedLeg != null) {
                        int legNumber = race.getCourse().getLegs().indexOf(trackedLeg.getLeg());
                        QuickRankDAO quickRankDAO = new QuickRankDAO(getCompetitorDAO(competitor), rank, legNumber);
                        result.add(quickRankDAO);
                    }
                }
                Collections.sort(result, new Comparator<QuickRankDAO>() {
                    @Override
                    public int compare(QuickRankDAO o1, QuickRankDAO o2) {
                        return o1.rank - o2.rank;
                    }
                });
            }
        }
        return result;
    }

    @Override
    public void removeWind(RaceIdentifier raceIdentifier, WindDAO windDAO) {
        TrackedRace trackedRace = getTrackedRace(raceIdentifier);
        if (trackedRace != null) {
            Position p = null;
            if (windDAO.position != null) {
                p = new DegreePosition(windDAO.position.latDeg, windDAO.position.lngDeg);
            }
            TimePoint at = null;
            if (windDAO.timepoint != null) {
                at = new MillisecondsTimePoint(windDAO.timepoint);
            }
            SpeedWithBearing speedWithBearing = null;
            Speed speed = null;
            if (windDAO.trueWindSpeedInKnots != null) {
                speed = new KnotSpeedImpl(windDAO.trueWindSpeedInKnots);
            } else if (windDAO.trueWindSpeedInMetersPerSecond != null) {
                speed = new KilometersPerHourSpeedImpl(windDAO.trueWindSpeedInMetersPerSecond * 3600. / 1000.);
            } else if (windDAO.dampenedTrueWindSpeedInKnots != null) {
                speed = new KnotSpeedImpl(windDAO.dampenedTrueWindSpeedInKnots);
            } else if (windDAO.dampenedTrueWindSpeedInMetersPerSecond != null) {
                speed = new KilometersPerHourSpeedImpl(windDAO.dampenedTrueWindSpeedInMetersPerSecond * 3600. / 1000.);
            }
            if (speed != null) {
                if (windDAO.trueWindBearingDeg != null) {
                    speedWithBearing = new KnotSpeedWithBearingImpl(speed.getKnots(), new DegreeBearingImpl(
                            windDAO.trueWindBearingDeg));
                } else if (windDAO.trueWindFromDeg != null) {
                    speedWithBearing = new KnotSpeedWithBearingImpl(speed.getKnots(), new DegreeBearingImpl(
                            windDAO.trueWindFromDeg).reverse());
                }
            }
            Wind wind = new WindImpl(p, at, speedWithBearing);
            trackedRace.removeWind(wind, WindSource.WEB);
        }
   }

    protected RacingEventService getService() {
        return (RacingEventService) racingEventServiceTracker.getService(); // grab the service
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
    public void createLeaderboard(String leaderboardName, int[] discardThresholds) {
        getService().addLeaderboard(leaderboardName, discardThresholds);
    }

    @Override
    public List<LeaderboardDAO> getLeaderboards() {
        Map<String, Leaderboard> leaderboards = getService().getLeaderboards();
        List<LeaderboardDAO> results = new ArrayList<LeaderboardDAO>();
        for(Leaderboard leaderboard: leaderboards.values()) {
            LeaderboardDAO dao = new LeaderboardDAO();
            dao.name = leaderboard.getName();
            dao.displayNames = new HashMap<CompetitorDAO, String>();
            for (RaceInLeaderboard raceColumn : leaderboard.getRaceColumns()) {
                dao.addRace(raceColumn.getName(), raceColumn.isMedalRace(), raceColumn.getTrackedRace() != null);
            }
            dao.hasCarriedPoints = leaderboard.hasCarriedPoints();
            dao.discardThresholds = leaderboard.getResultDiscardingRule().getDiscardIndexResultsStartingWithHowManyRaces();
            results.add(dao);
        }
        
        return results;
    }
    
    public LeaderboardDAO getLeaderboardByName(String leaderboardName) {
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        LeaderboardDAO dao = new LeaderboardDAO();
        dao.name = leaderboard.getName();
        dao.displayNames = new HashMap<CompetitorDAO, String>();
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
    public void connectTrackedRaceToLeaderboardColumn(String leaderboardName, String raceColumnName, RaceIdentifier raceIdentifier) {
        TrackedRace trackedRace = getTrackedRace(raceIdentifier);
        if (trackedRace != null) {
            Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
            if (leaderboard != null) {
                RaceInLeaderboard raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
                if (raceColumn != null) {
                    raceColumn.setTrackedRace(trackedRace);
                    getService().updateStoredLeaderboard(leaderboard);
                }
            }
        }
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
    public List<SwissTimingConfigurationDAO> getPreviousSwissTimingConfigurations() {
        Iterable<SwissTimingConfiguration> configs = swissTimingAdapterPersistence.getSwissTimingConfigurations();
        List<SwissTimingConfigurationDAO> result = new ArrayList<SwissTimingConfigurationDAO>();
        for (SwissTimingConfiguration stConfig : configs) {
            result.add(new SwissTimingConfigurationDAO(stConfig.getName(), stConfig.getHostname(), stConfig.getPort(), stConfig.canSendRequests()));
        }
        return result;
   }

    @Override
    public List<SwissTimingRaceRecordDAO> listSwissTimingRaces(String hostname, int port, boolean canSendRequests) 
           throws UnknownHostException, IOException, InterruptedException, ParseException {
        List<SwissTimingRaceRecordDAO> result = new ArrayList<SwissTimingRaceRecordDAO>();
        for (com.sap.sailing.domain.swisstimingadapter.RaceRecord rr : getService().getSwissTimingRaceRecords(hostname, port, canSendRequests)) {
            result.add(new SwissTimingRaceRecordDAO(rr.getRaceID(), rr.getDescription(), rr.getStartTime()));
        }
        return result;
    }

    @Override
    public void storeSwissTimingConfiguration(String configName, String hostname, int port, boolean canSendRequests) {
        swissTimingAdapterPersistence.storeSwissTimingConfiguration(swissTimingFactory.createSwissTimingConfiguration(configName, hostname, port, canSendRequests));
   }

    @Override
    public void trackWithSwissTiming(SwissTimingRaceRecordDAO rr, String hostname, int port, boolean canSendRequests,
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
        for (CountryCode cc : countryCodeFactory.getAll()){
            if (cc.getThreeLetterIOCCode() != null && !cc.getThreeLetterIOCCode().equals("")){
                countryCodes.add(cc.getThreeLetterIOCCode());
            }
        }
        Collections.sort(countryCodes);
        return countryCodes.toArray(new String[0]);
    }

    @Override
    public List<Pair<CompetitorDAO, Double[]>> getCompetitorRaceData(RaceIdentifier race, CompetitorAndTimePointsDAO competitorAndTimePointsDAO, DetailType dataType) throws NoWindException {
        List<Pair<CompetitorDAO, Double[]>> competitorData = new ArrayList<Pair<CompetitorDAO, Double[]>>();
        TrackedRace trackedRace = getTrackedRace(race);
        Iterable<Competitor> competitors = trackedRace.getRace().getCompetitors();
        List<Competitor> selectedCompetitor = new ArrayList<Competitor>();
        for (CompetitorDAO cDAO : competitorAndTimePointsDAO.getCompetitor()){
            for (Competitor c : competitors){
                if (c.getId().toString().equals(cDAO.id)){
                    selectedCompetitor.add(c);
                }
            }
        }
        
        switch (dataType){
        case CURRENT_SPEED_OVER_GROUND_IN_KNOTS:
            for (int c = 0; c < selectedCompetitor.size(); c++){
                Double[] entries = new Double[competitorAndTimePointsDAO.getTimePoints().length];
                for (int i = 0; i < competitorAndTimePointsDAO.getTimePoints().length; i++){
                    MillisecondsTimePoint time = new MillisecondsTimePoint(competitorAndTimePointsDAO.getTimePoints()[i]);
                    TrackedLegOfCompetitor trackedLeg = trackedRace.getTrackedLeg(selectedCompetitor.get(c), time);
                    if (trackedLeg != null){
                        SpeedWithBearing speedOverGround = trackedLeg.getSpeedOverGround(time);
                        entries[i] = (speedOverGround == null) ? null : speedOverGround.getKnots();
                    }
                }
                competitorData.add(new Pair<CompetitorDAO, Double[]>(competitorAndTimePointsDAO.getCompetitor()[c], entries));
            }
            break;
        case VELOCITY_MADE_GOOD_IN_KNOTS:
            for (int c = 0; c < selectedCompetitor.size(); c++){
                Double[] entries = new Double[competitorAndTimePointsDAO.getTimePoints().length];
                for (int i = 0; i < competitorAndTimePointsDAO.getTimePoints().length; i++){
                    MillisecondsTimePoint time = new MillisecondsTimePoint(competitorAndTimePointsDAO.getTimePoints()[i]);
                    TrackedLegOfCompetitor trackedLeg = trackedRace.getTrackedLeg(selectedCompetitor.get(c), time);
                    if (trackedLeg != null){
                        Speed velocityMadeGood = trackedLeg.getVelocityMadeGood(time);
                        entries[i] = (velocityMadeGood == null) ? null : velocityMadeGood.getKnots();
                    }
                }
                competitorData.add(new Pair<CompetitorDAO, Double[]>(competitorAndTimePointsDAO.getCompetitor()[c], entries));
            }
            break;
        case DISTANCE_TRAVELED:
            for (int c = 0; c < selectedCompetitor.size(); c++){
                Double[] entries = new Double[competitorAndTimePointsDAO.getTimePoints().length];
                double distanceOfPreviousLegs = 0;
                double lastTraveledDistance = 0;
                for (int i = 0; i < competitorAndTimePointsDAO.getTimePoints().length; i++){
                    MillisecondsTimePoint time = new MillisecondsTimePoint(competitorAndTimePointsDAO.getTimePoints()[i]);
                    TrackedLegOfCompetitor trackedLeg = trackedRace.getTrackedLeg(selectedCompetitor.get(c), time);
                    if (trackedLeg != null){
                        Distance distanceTraveled = trackedLeg.getDistanceTraveled(time);
                        if (distanceTraveled != null){
                            double d = distanceTraveled.getMeters();
                            if (d < lastTraveledDistance){
                                distanceOfPreviousLegs += lastTraveledDistance;
                            }
                            lastTraveledDistance = d;
                            entries[i] = d + distanceOfPreviousLegs;
                        }
                    }
                }
                competitorData.add(new Pair<CompetitorDAO, Double[]>(competitorAndTimePointsDAO.getCompetitor()[c], entries));
            }
            break;
        case GAP_TO_LEADER_IN_SECONDS:
            for (int c = 0; c < selectedCompetitor.size(); c++){
                Double[] entries = new Double[competitorAndTimePointsDAO.getTimePoints().length];
                for (int i = 0; i < competitorAndTimePointsDAO.getTimePoints().length; i++){
                    MillisecondsTimePoint time = new MillisecondsTimePoint(competitorAndTimePointsDAO.getTimePoints()[i]);
                    TrackedLegOfCompetitor trackedLeg = trackedRace.getTrackedLeg(selectedCompetitor.get(c), time);
                    if (trackedLeg != null){
                        entries[i] = trackedLeg.getGapToLeaderInSeconds(time);
                    }
                }
                competitorData.add(new Pair<CompetitorDAO, Double[]>(competitorAndTimePointsDAO.getCompetitor()[c], entries));
            }
            break;
        case WINDWARD_DISTANCE_TO_OVERALL_LEADER:
            for (int c = 0; c < selectedCompetitor.size(); c++){
                Double[] entries = new Double[competitorAndTimePointsDAO.getTimePoints().length];
                try {
                    for (int i = 0; i < competitorAndTimePointsDAO.getTimePoints().length; i++){
                        MillisecondsTimePoint time = new MillisecondsTimePoint(competitorAndTimePointsDAO.getTimePoints()[i]);
                        TrackedLegOfCompetitor trackedLeg = trackedRace.getTrackedLeg(selectedCompetitor.get(c), time);
                        if (trackedLeg != null){
                            Distance distanceToLeader = trackedLeg.getWindwardDistanceToOverallLeader(time);
                            entries[i] = (distanceToLeader == null) ? null : distanceToLeader.getMeters();
                        }
                    }
                }
                catch (NullPointerException npe){
                    System.out.println(npe.getLocalizedMessage());
                    npe.printStackTrace();
                }
                competitorData.add(new Pair<CompetitorDAO, Double[]>(competitorAndTimePointsDAO.getCompetitor()[c], entries));
            }
            break;
        }
        return competitorData;
    }
    
    public Map<CompetitorDAO, List<GPSFixDAO>> getDouglasPoints(RaceIdentifier raceIdentifier,
            Map<CompetitorDAO, Date> from, Map<CompetitorDAO, Date> to,
            double meters) {
        Map<CompetitorDAO, List<GPSFixDAO>> result = new HashMap<CompetitorDAO, List<GPSFixDAO>>();
        TrackedRace trackedRace = getTrackedRace(raceIdentifier);
        if (trackedRace != null) {
            MeterDistance maxDistance = new MeterDistance(meters);
            for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
                CompetitorDAO competitorDAO = getCompetitorDAO(competitor);
                if (from.containsKey(competitorDAO)) {
                    // get Track of competitor
                    GPSFixTrack<Competitor, GPSFixMoving> gpsFixTrack = trackedRace.getTrack(competitor);
                    // Distance for DouglasPeucker
                    TimePoint timePointFrom = new MillisecondsTimePoint(from.get(competitorDAO));
                    TimePoint timePointTo = new MillisecondsTimePoint(to.get(competitorDAO));
                    List<GPSFixMoving> gpsFixApproximation = trackedRace.approximate(competitor, maxDistance,
                            timePointFrom, timePointTo);
                    List<GPSFixDAO> gpsFixDouglasList = new ArrayList<GPSFixDAO>();
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
                        GPSFixDAO fixDAO = createGPSFixDAO(fix, speedWithBearing, tack, /* extrapolated */false);
                        gpsFixDouglasList.add(fixDAO);
                    }
                    result.put(competitorDAO, gpsFixDouglasList);
                }
            }
        }
        return result;
    }

    @Override
    public Map<CompetitorDAO, List<ManeuverDAO>> getManeuvers(RaceIdentifier raceIdentifier,
            Map<CompetitorDAO, Date> from, Map<CompetitorDAO, Date> to) throws NoWindException {
        Map<CompetitorDAO, List<ManeuverDAO>> result = new HashMap<CompetitorDAO, List<ManeuverDAO>>();
        TrackedRace trackedRace = getTrackedRace(raceIdentifier);
        for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
            CompetitorDAO competitorDAO = getCompetitorDAO(competitor);
            if (from.containsKey(competitorDAO)) {
                TimePoint timePointFrom = new MillisecondsTimePoint(from.get(competitorDAO));
                TimePoint timePointTo = new MillisecondsTimePoint(to.get(competitorDAO));
                List<Maneuver> maneuversForCompetitor = trackedRace.getManeuvers(competitor, timePointFrom, timePointTo);
                List<ManeuverDAO> maneuverDAOs = createManeuverDAOsForCompetitor(maneuversForCompetitor, trackedRace, competitor);
                result.put(competitorDAO, maneuverDAOs);
            }
        }
        return result;
    }

    private List<ManeuverDAO> createManeuverDAOsForCompetitor(List<Maneuver> maneuvers, TrackedRace trackedRace, Competitor competitor) {
        List<ManeuverDAO> result = new ArrayList<ManeuverDAO>();
        for (Maneuver maneuver : maneuvers) {
            ManeuverDAO maneuverDAO = new ManeuverDAO(maneuver.getType().name(), maneuver.getNewTack().name(),
                    new PositionDAO(maneuver.getPosition().getLatDeg(), maneuver.getPosition().getLngDeg()), 
                    maneuver.getTimePoint().asDate(),
                    createSpeedWithBearingDAO(maneuver.getSpeedWithBearingBefore()),
                    createSpeedWithBearingDAO(maneuver.getSpeedWithBearingAfter()),
                    maneuver.getDirectionChangeInDegrees());
            result.add(maneuverDAO);
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
    public Event getEvent(EventNameAndRaceName eventIdentifier) {
        return getService().getEventByName(eventIdentifier.getEventName());
    }

    private RaceDefinition getRace(RaceIdentifier raceIdentifier) {
        return (RaceDefinition) raceIdentifier.getRace(this);
    }

    private TrackedRace getTrackedRace(RaceIdentifier raceIdentifier) {
        return (TrackedRace) raceIdentifier.getTrackedRace(this);
    }
    
    private TrackedRace getExistingTrackedRace(RaceIdentifier raceIdentifier) {
        return (TrackedRace) raceIdentifier.getExistingTrackedRace(this);
    }
    
    private Event getEvent(EventIdentifier eventIdentifier) {
        return (Event) eventIdentifier.getEvent(this);
    }

    @Override
    public CompetitorAndTimePointsDAO getCompetitorAndTimePoints(RaceIdentifier race, int steps) {
        CompetitorAndTimePointsDAO competitorAndTimePointsDAO = new CompetitorAndTimePointsDAO();
        TrackedRace trackedRace = getTrackedRace(race);
        List<CompetitorDAO> competitorDAOs = new ArrayList<CompetitorDAO>();
        for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
            competitorDAOs.add(getCompetitorDAO(competitor));
        }
        competitorAndTimePointsDAO.setCompetitor(competitorDAOs.toArray(new CompetitorDAO[0]));
        competitorAndTimePointsDAO.setStartTime(trackedRace.getStart().asMillis());
        List<Long> timePoints = new ArrayList<Long>();
        long stepsize = (trackedRace.getTimePointOfNewestEvent().asMillis()- trackedRace.getStart().asMillis() - 20000)/steps;
        for (long time = trackedRace.getStart().asMillis() - 20000; time < trackedRace.getTimePointOfNewestEvent().asMillis(); time += stepsize){
            timePoints.add(time);
        }
        competitorAndTimePointsDAO.setTimePoints(timePoints.toArray(new Long[0]));
        return competitorAndTimePointsDAO;
    }
}