package com.sap.sailing.gwt.ui.server;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
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
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.DegreeBearingImpl;
import com.sap.sailing.domain.base.impl.DegreePosition;
import com.sap.sailing.domain.base.impl.KilometersPerHourSpeedImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard.Entry;
import com.sap.sailing.domain.leaderboard.RaceInLeaderboard;
import com.sap.sailing.domain.leaderboard.ScoreCorrection.MaxPointsReason;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.NoWindException;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindSource;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.RaceHandle;
import com.sap.sailing.domain.tractracadapter.RaceRecord;
import com.sap.sailing.domain.tractracadapter.TracTracConfiguration;
import com.sap.sailing.gwt.ui.client.SailingService;
import com.sap.sailing.gwt.ui.shared.BoatClassDAO;
import com.sap.sailing.gwt.ui.shared.CompetitorDAO;
import com.sap.sailing.gwt.ui.shared.EventDAO;
import com.sap.sailing.gwt.ui.shared.GPSFixDAO;
import com.sap.sailing.gwt.ui.shared.LeaderboardDAO;
import com.sap.sailing.gwt.ui.shared.LeaderboardEntryDAO;
import com.sap.sailing.gwt.ui.shared.LeaderboardRowDAO;
import com.sap.sailing.gwt.ui.shared.LegEntryDAO;
import com.sap.sailing.gwt.ui.shared.MarkDAO;
import com.sap.sailing.gwt.ui.shared.Pair;
import com.sap.sailing.gwt.ui.shared.PositionDAO;
import com.sap.sailing.gwt.ui.shared.QuickRankDAO;
import com.sap.sailing.gwt.ui.shared.RaceDAO;
import com.sap.sailing.gwt.ui.shared.RaceRecordDAO;
import com.sap.sailing.gwt.ui.shared.RegattaDAO;
import com.sap.sailing.gwt.ui.shared.TracTracConfigurationDAO;
import com.sap.sailing.gwt.ui.shared.WindDAO;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDAO;
import com.sap.sailing.gwt.ui.shared.WindTrackInfoDAO;
import com.sap.sailing.mongodb.DomainObjectFactory;
import com.sap.sailing.mongodb.MongoObjectFactory;
import com.sap.sailing.mongodb.MongoWindStoreFactory;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.util.CountryCode;

/**
 * The server side implementation of the RPC service.
 */
public class SailingServiceImpl extends RemoteServiceServlet implements SailingService {
    private static final long serialVersionUID = 9031688830194537489L;

    private final ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;
    
    private final MongoObjectFactory mongoObjectFactory;

    public SailingServiceImpl() {
        BundleContext context = Activator.getDefault();
        racingEventServiceTracker = new ServiceTracker<RacingEventService, RacingEventService>(
                context, RacingEventService.class.getName(), null);
        racingEventServiceTracker.open();
        mongoObjectFactory = MongoObjectFactory.INSTANCE;
    }
    
    public LeaderboardDAO getLeaderboardByName(String leaderboardName, Date date, Collection<String> namesOfRacesForWhichToLoadLegDetails) throws Exception {
        LeaderboardDAO result = null;
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard != null) {
            result = new LeaderboardDAO();
            TimePoint timePoint = new MillisecondsTimePoint(date);
            result.competitors = new ArrayList<CompetitorDAO>();
            result.name = leaderboard.getName();
            result.raceNamesAndMedalRaceAndTracked = new LinkedHashMap<String, Pair<Boolean, Boolean>>();
            for (RaceInLeaderboard raceColumn : leaderboard.getRaceColumns()) {
                result.raceNamesAndMedalRaceAndTracked.put(raceColumn.getName(),
                        new Pair<Boolean, Boolean>(raceColumn.isMedalRace(), raceColumn.getTrackedRace()!=null));
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
            }
        }
        return result;
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
        if (addLegDetails) {
            entryDAO.legDetails = new ArrayList<LegEntryDAO>();
            for (Leg leg : trackedRace.getRace().getCourse().getLegs()) {
                TrackedLegOfCompetitor trackedLeg = trackedRace.getTrackedLeg(competitor, leg);
                LegEntryDAO legEntry = createLegEntry(trackedLeg, timePoint);
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
            result.finished = trackedLeg.hasFinishedLeg(timePoint);
            result.gapToLeaderInSeconds = trackedLeg.getGapToLeaderInSeconds(timePoint);
            result.rank = trackedLeg.getRank(timePoint);
            result.started = trackedLeg.hasStartedLeg(timePoint);
            Speed velocityMadeGood = trackedLeg.getVelocityMadeGood(timePoint);
            result.velocityMadeGoodInKnots = velocityMadeGood == null ? null : velocityMadeGood.getKnots();
            Distance windwardDistanceToGo = trackedLeg.getWindwardDistanceToGo(timePoint);
            result.windwardDistanceToGoInMeters = windwardDistanceToGo == null ? null : windwardDistanceToGo
                    .getMeters();
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
                RegattaDAO regatta = new RegattaDAO(new BoatClassDAO(e.getKey().getName()), raceDAOsInBoatClass);
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
                countryCode==null?"":countryCode.getThreeLetterIOCCode(), countryCode==null?"":countryCode.getName(), c.getBoat().getSailID());
        return competitorDAO;
    }

    @Override
    public List<RaceRecordDAO> listRacesInEvent(String eventJsonURL) throws MalformedURLException, IOException,
            ParseException, org.json.simple.parser.ParseException, URISyntaxException {
        List<RaceRecord> raceRecords;
        raceRecords = getService().getRaceRecords(new URL(eventJsonURL));
        List<RaceRecordDAO> result = new ArrayList<RaceRecordDAO>();
        for (RaceRecord raceRecord : raceRecords) {
            result.add(new RaceRecordDAO(raceRecord.getID(), raceRecord.getEventName(), raceRecord.getName(),
                    raceRecord.getParamURL().toString(), raceRecord.getReplayURL(), raceRecord.getLiveURI().toString(),
                    raceRecord.getStoredURI().toString(), raceRecord.getTrackingStartTime().asDate(), raceRecord
                            .getTrackingEndTime().asDate(), raceRecord.getRaceStartTime().asDate()));
        }
        return result;
    }

    @Override
    public void track(RaceRecordDAO rr, String liveURI, String storedURI, boolean trackWind, boolean correctWindByDeclination) throws Exception {
        if (liveURI == null || liveURI.trim().length() == 0) {
            liveURI = rr.liveURI;
        }
        if (storedURI == null || storedURI.trim().length() == 0) {
            storedURI = rr.storedURI;
        }
        RaceHandle raceHandle = getService().addRace(new URL(rr.paramURL), new URI(liveURI), new URI(storedURI),
                MongoWindStoreFactory.INSTANCE.getMongoWindStore(mongoObjectFactory));
        if (trackWind) {
            startTrackingWind(raceHandle.getEvent().getName(), raceHandle.getRace().getName(), correctWindByDeclination);
        }
    }

    @Override
    public List<TracTracConfigurationDAO> getPreviousConfigurations() throws Exception {
        DomainObjectFactory domainObjectFactory = DomainObjectFactory.INSTANCE;
        Iterable<TracTracConfiguration> configs = domainObjectFactory.getTracTracConfigurations();
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
        mongoObjectFactory.storeTracTracConfiguration(domainFactory.createTracTracConfiguration(name, jsonURL, liveDataURI, storedDataURI));
    }
    
    @Override
    public void stopTrackingEvent(String eventName) throws Exception {
        getService().stopTracking(getService().getEventByName(eventName));
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
    public void stopTrackingRace(String eventName, String raceName) throws Exception {
        Event event = getService().getEventByName(eventName);
        RaceDefinition r = getRaceByName(event, raceName);
        if (r != null) {
            getService().stopTracking(event, r);
        }
    }
    
    private void startTrackingWind(String eventName, String raceName, boolean correctByDeclination) throws Exception {
        Event event = getService().getEventByName(eventName);
        if (event != null) {
            RaceDefinition race = getRaceByName(event, raceName);
            getService().startTrackingWind(event, race, correctByDeclination);
        }
    }

    private TrackedRace getTrackedRace(String eventName, String raceName) {
        TrackedRace result = null;
        Event event = getService().getEventByName(eventName);
        if (event != null) {
            RaceDefinition race = getRaceByName(event, raceName);
            if (race != null) {
                result = getService().getDomainFactory().getTrackedEvent(event)
                        .getExistingTrackedRace(race);
            }
        }
        return result;       
    }
    @Override
    public WindInfoForRaceDAO getWindInfo(String eventName, String raceName, Date fromDate, Date toDate) {
        WindInfoForRaceDAO result = null;
        TrackedRace trackedRace = getTrackedRace(eventName, raceName);
        if (trackedRace != null) {
            result = new WindInfoForRaceDAO();
            result.selectedWindSourceName = trackedRace.getWindSource().name();
            TimePoint from = new MillisecondsTimePoint(fromDate);
            TimePoint to = new MillisecondsTimePoint(toDate);
            Map<String, WindTrackInfoDAO> windTrackInfoDAOs = new HashMap<String, WindTrackInfoDAO>();
            result.windTrackInfoByWindSourceName = windTrackInfoDAOs;
            for (WindSource windSource : WindSource.values()) {
                WindTrackInfoDAO windTrackInfoDAO = new WindTrackInfoDAO();
                windTrackInfoDAO.windFixes = new ArrayList<WindDAO>();
                WindTrack windTrack = trackedRace.getWindTrack(windSource);
                windTrackInfoDAO.dampeningIntervalInMilliseconds = windTrack.getMillisecondsOverWhichToAverageWind();
                Iterator<Wind> windIter = windTrack.getFixesIterator(from, /* inclusive */true);
                while (windIter.hasNext()) {
                    Wind wind = windIter.next();
                    if (wind.getTimePoint().compareTo(to) > 0) {
                        break;
                    }
                    WindDAO windDAO = createWindDAO(wind, windTrack);
                    windTrackInfoDAO.windFixes.add(windDAO);
                }
                windTrackInfoDAOs.put(windSource.name(), windTrackInfoDAO);
            }
        }
        return result;
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
    public WindInfoForRaceDAO getWindInfo(String eventName, String raceName, Date from,
            long millisecondsStepWidth, int numberOfFixes, double latDeg, double lngDeg) throws NoWindException {
        Position position = new DegreePosition(latDeg, lngDeg);
        WindInfoForRaceDAO result = null;
        Event event = getService().getEventByName(eventName);
        if (event != null) {
            RaceDefinition race = getRaceByName(event, raceName);
            if (race != null) {
                TrackedRace trackedRace = getService().getDomainFactory().getTrackedEvent(event)
                        .getExistingTrackedRace(race);
                if (trackedRace != null) {
                    result = new WindInfoForRaceDAO();
                    WindSource windSource = trackedRace.getWindSource();
                    result.selectedWindSourceName = windSource.name();
                    TimePoint fromTimePoint = new MillisecondsTimePoint(from);
                    Map<String, WindTrackInfoDAO> windTrackInfoDAOs = new HashMap<String, WindTrackInfoDAO>();
                    result.windTrackInfoByWindSourceName = windTrackInfoDAOs;
                    WindTrackInfoDAO windTrackInfoDAO = new WindTrackInfoDAO();
                    windTrackInfoDAO.windFixes = new ArrayList<WindDAO>();
                    WindTrack windTrack = trackedRace.getWindTrack(windSource);
                    windTrackInfoDAOs.put(windSource.name(), windTrackInfoDAO);
                    windTrackInfoDAO.dampeningIntervalInMilliseconds = windTrack
                            .getMillisecondsOverWhichToAverageWind();
                    TimePoint timePoint = fromTimePoint;
                    for (int i=0; i<numberOfFixes; i++) {
                        Wind wind = windTrack.getEstimatedWind(position, timePoint);
                        if (wind != null) {
                            WindDAO windDAO = createWindDAO(wind, windTrack);
                            windTrackInfoDAO.windFixes.add(windDAO);
                        }
                        timePoint = new MillisecondsTimePoint(timePoint.asMillis() + millisecondsStepWidth);
                    }
                }
            }
        }
        return result;
    }
    
    @Override
    public void setWind(String eventName, String raceName, WindDAO windDAO) {
        Event event = getService().getEventByName(eventName);
        RaceDefinition race = getRaceByName(event, raceName);
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
                speedWithBearing = new KnotSpeedWithBearingImpl(speed.getKnots(), new DegreeBearingImpl(windDAO.trueWindBearingDeg));
            } else if (windDAO.trueWindFromDeg != null) {
                speedWithBearing = new KnotSpeedWithBearingImpl(speed.getKnots(), new DegreeBearingImpl(windDAO.trueWindFromDeg).reverse());
            }
        }
        Wind wind = new WindImpl(p, at, speedWithBearing);
        getService().getDomainFactory().getTrackedEvent(event).getTrackedRace(race).recordWind(wind, WindSource.WEB);
    }
    
    @Override
    public void setWindSource(String eventName, String raceName, String windSourceName) {
        Event event = getService().getEventByName(eventName);
        if (event != null) {
            RaceDefinition race = getRaceByName(event, raceName);
            if (race != null) {
                TrackedRace trackedRace = getService().getDomainFactory().getTrackedEvent(event).getTrackedRace(race);
                if (trackedRace != null) {
                    trackedRace.setWindSource(WindSource.valueOf(windSourceName));
                }
            }
        }
    }

    @Override
    public Map<CompetitorDAO, List<GPSFixDAO>> getBoatPositions(String eventName, String raceName, Date date,
            long tailLengthInMilliseconds, boolean extrapolate) {
        Map<CompetitorDAO, List<GPSFixDAO>> result = new HashMap<CompetitorDAO, List<GPSFixDAO>>();
        if (date != null) {
            Event event = getService().getEventByName(eventName);
            if (event != null) {
                RaceDefinition race = getRaceByName(event, raceName);
                TimePoint end = new MillisecondsTimePoint(date);
                TrackedRace trackedRace = getService().getDomainFactory().getTrackedEvent(event).getTrackedRace(race);
                for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
                    CompetitorDAO competitorDAO = getCompetitorDAO(competitor);
                    List<GPSFixDAO> fixesForCompetitor = new ArrayList<GPSFixDAO>();
                    result.put(competitorDAO, fixesForCompetitor);
                    GPSFixTrack<Competitor, GPSFixMoving> track = trackedRace.getTrack(competitor);
                    Iterator<GPSFixMoving> fixIter = track.getFixesIterator(new MillisecondsTimePoint(date.getTime()
                            - tailLengthInMilliseconds), /* inclusive */true);
                    if (fixIter.hasNext()) {
                        GPSFixMoving fix = fixIter.next();
                        while (fix != null && fix.getTimePoint().compareTo(end) < 0) {
                            GPSFixDAO fixDAO = new GPSFixDAO(fix.getTimePoint().asDate(), new PositionDAO(fix
                                    .getPosition().getLatDeg(), fix.getPosition().getLngDeg()));
                            fixesForCompetitor.add(fixDAO);
                            if (fixIter.hasNext()) {
                                fix = fixIter.next();
                            } else {
                                // check if fix was at date and if extrapolation is requested
                                if (!fix.getTimePoint().equals(end) && extrapolate) {
                                    Position position = track.getEstimatedPosition(end, extrapolate);
                                    GPSFixDAO extrapolated = new GPSFixDAO(date, new PositionDAO(position.getLatDeg(),
                                            position.getLngDeg()));
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

    @Override
    public List<MarkDAO> getMarkPositions(String eventName, String raceName, Date date) {
        List<MarkDAO> result = new ArrayList<MarkDAO>();
        if (date != null) {
            Event event = getService().getEventByName(eventName);
            if (event != null) {
                RaceDefinition race = getRaceByName(event, raceName);
                TimePoint dateAsTimePoint = new MillisecondsTimePoint(date);
                TrackedRace trackedRace = getService().getDomainFactory().getTrackedEvent(event).getTrackedRace(race);
                Set<Buoy> buoys = new HashSet<Buoy>();
                for (Waypoint waypoint : trackedRace.getRace().getCourse().getWaypoints()) {
                    for (Buoy b : waypoint.getBuoys()) {
                        buoys.add(b);
                    }
                }
                for (Buoy buoy : buoys) {
                    GPSFixTrack<Buoy, GPSFix> track = trackedRace.getTrack(buoy);
                    Position positionAtDate = track.getEstimatedPosition(dateAsTimePoint, /* extrapolate */false);
                    MarkDAO markDAO = new MarkDAO(buoy.getName(), positionAtDate.getLatDeg(),
                            positionAtDate.getLngDeg());
                    result.add(markDAO);
                }
            }
        }
        return result;
    }

    @Override
    public List<QuickRankDAO> getQuickRanks(String eventName, String raceName, Date date) throws Exception {
        Event event = getService().getEventByName(eventName);
        List<QuickRankDAO> result = new ArrayList<QuickRankDAO>();
        if (date != null && event != null) {
            RaceDefinition race = getRaceByName(event, raceName);
            if (race != null) {
                TimePoint dateAsTimePoint = new MillisecondsTimePoint(date);
                TrackedRace trackedRace = getService().getDomainFactory().getTrackedEvent(event).getTrackedRace(race);
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
    public void removeWind(String eventName, String raceName, WindDAO windDAO) {
        Event event = getService().getEventByName(eventName);
        RaceDefinition race = getRaceByName(event, raceName);
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
                speedWithBearing = new KnotSpeedWithBearingImpl(speed.getKnots(), new DegreeBearingImpl(windDAO.trueWindBearingDeg));
            } else if (windDAO.trueWindFromDeg != null) {
                speedWithBearing = new KnotSpeedWithBearingImpl(speed.getKnots(), new DegreeBearingImpl(windDAO.trueWindFromDeg).reverse());
            }
        }
        Wind wind = new WindImpl(p, at, speedWithBearing);
        getService().getDomainFactory().getTrackedEvent(event).getTrackedRace(race).removeWind(wind, WindSource.WEB);
   }

    private RacingEventService getService() {
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
    public void connectTrackedRaceToLeaderboardColumn(String leaderboardName, String raceColumnName, String eventName,
            String raceName) {
        TrackedRace trackedRace = getTrackedRace(eventName, raceName);
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
}