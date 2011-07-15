package com.sap.sailing.gwt.ui.server;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Event;
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
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
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
import com.sap.sailing.gwt.ui.shared.MarkDAO;
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

    private RacingEventService service;

    public SailingServiceImpl() {
        BundleContext context = Activator.getDefault();
        ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker = new ServiceTracker<RacingEventService, RacingEventService>(
                context, RacingEventService.class.getName(), null);
        racingEventServiceTracker.open();
        // grab the service
        service = (RacingEventService) racingEventServiceTracker.getService();
    }

    public List<EventDAO> listEvents() throws IllegalArgumentException {
        List<EventDAO> result = new ArrayList<EventDAO>();
        for (Event event : service.getAllEvents()) {
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
            RaceDAO raceDAO = new RaceDAO(r.getName(), getCompetitorDAOs(r.getCompetitors()), service.isRaceBeingTracked(r));
            if (raceDAO.currentlyTracked) {
                TrackedRace trackedRace = service.getTrackedRace(event, r);
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
                countryCode==null?"":countryCode.getThreeLetterIOCCode());
        return competitorDAO;
    }

    @Override
    public List<RaceRecordDAO> listRacesInEvent(String eventJsonURL) throws MalformedURLException, IOException,
            ParseException, org.json.simple.parser.ParseException {
        List<RaceRecord> raceRecords;
        raceRecords = service.getRaceRecords(new URL(eventJsonURL));
        List<RaceRecordDAO> result = new ArrayList<RaceRecordDAO>();
        for (RaceRecord raceRecord : raceRecords) {
            result.add(new RaceRecordDAO(raceRecord.getID(), raceRecord.getEventName(), raceRecord.getName(),
                    raceRecord.getParamURL().toString(), raceRecord.getReplayURL(), raceRecord.getTrackingStartTime().asDate(),
                    raceRecord.getTrackingEndTime().asDate(), raceRecord.getRaceStartTime().asDate()));
        }
        return result;
    }

    @Override
    public void track(RaceRecordDAO rr, String liveURI, String storedURI, boolean trackWind, boolean correctWindByDeclination) throws Exception {
        RaceHandle raceHandle = service.addRace(new URL(rr.paramURL), new URI(liveURI), new URI(storedURI),
                MongoWindStoreFactory.INSTANCE.getMongoWindStore(MongoObjectFactory.INSTANCE));
        if (trackWind) {
            startTrackingWind(raceHandle.getEvent().getName(), raceHandle.getRace().getName(), correctWindByDeclination);
        }
    }

    @Override
    public List<TracTracConfigurationDAO> getPreviousConfigurations() throws Exception {
        DomainObjectFactory domainObjectFactory = DomainObjectFactory.INSTANCE;
        Iterable<TracTracConfiguration> configs = domainObjectFactory.getTracTracConfigurations(domainObjectFactory.getDefaultDatabase());
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
        MongoObjectFactory mongoObjectFactory = MongoObjectFactory.INSTANCE;
        mongoObjectFactory.storeTracTracConfiguration(DomainObjectFactory.INSTANCE.getDefaultDatabase(),
                domainFactory.createTracTracConfiguration(name, jsonURL, liveDataURI, storedDataURI));
    }
    
    @Override
    public void stopTrackingEvent(String eventName) throws Exception {
        service.stopTracking(service.getEventByName(eventName));
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
        Event event = service.getEventByName(eventName);
        RaceDefinition r = getRaceByName(event, raceName);
        if (r != null) {
            service.stopTracking(event, r);
        }
    }
    
    private void startTrackingWind(String eventName, String raceName, boolean correctByDeclination) throws Exception {
        Event event = service.getEventByName(eventName);
        if (event != null) {
            RaceDefinition race = getRaceByName(event, raceName);
            service.startTrackingWind(event, race, correctByDeclination);
        }
    }
    
    @Override
    public WindInfoForRaceDAO getWindInfo(String eventName, String raceName, Date fromDate, Date toDate) {
        Event event = service.getEventByName(eventName);
        RaceDefinition race = getRaceByName(event, raceName);
        WindInfoForRaceDAO result = new WindInfoForRaceDAO();
        TrackedRace trackedRace = service.getDomainFactory().getTrackedEvent(event).getExistingTrackedRace(race);
        TimePoint from = new MillisecondsTimePoint(fromDate);
        TimePoint to = new MillisecondsTimePoint(toDate);
        Map<String, WindTrackInfoDAO> windTrackInfoDAOs = new HashMap<String, WindTrackInfoDAO>();
        result.windTrackInfoByWindSourceName = windTrackInfoDAOs;
        for (WindSource windSource : WindSource.values()) {
            WindTrackInfoDAO windTrackInfoDAO = new WindTrackInfoDAO();
            windTrackInfoDAO.windFixes = new ArrayList<WindDAO>();
            windTrackInfoDAOs.put(windSource.name(), windTrackInfoDAO);
            WindTrack windTrack = trackedRace.getWindTrack(windSource);
            windTrackInfoDAO.dampeningIntervalInMilliseconds = windTrack.getMillisecondsOverWhichToAverageWind();
            Iterator<Wind> windIter = windTrack.getFixesIterator(from, /* inclusive */true);
            while (windIter.hasNext()) {
                Wind wind = windIter.next();
                if (wind.getTimePoint().compareTo(to) > 0) {
                    break;
                }
                WindDAO windDAO = new WindDAO();
                windDAO.trueWindBearingDeg = wind.getBearing().getDegrees();
                windDAO.trueWindFromDeg = wind.getBearing().reverse().getDegrees();
                windDAO.trueWindSpeedInKnots = wind.getKnots();
                windDAO.trueWindSpeedInMetersPerSecond = wind.getMetersPerSecond();
                if (wind.getPosition() != null) {
                    windDAO.position = new PositionDAO(wind.getPosition().getLatDeg(), wind.getPosition().getLngDeg());
                }
                windTrackInfoDAO.windFixes.add(windDAO);
                if (wind.getTimePoint() != null) {
                    windDAO.timepoint = wind.getTimePoint().asMillis();
                    Wind estimatedWind = windTrack.getEstimatedWind(wind.getPosition(), wind.getTimePoint());
                    windDAO.dampenedTrueWindBearingDeg = estimatedWind.getBearing().getDegrees();
                    windDAO.dampenedTrueWindFromDeg = estimatedWind.getBearing().reverse().getDegrees();
                    windDAO.dampenedTrueWindSpeedInKnots = estimatedWind.getKnots();
                    windDAO.dampenedTrueWindSpeedInMetersPerSecond = estimatedWind.getMetersPerSecond();
                }
            }
        }
        return result;
    }
    
    @Override
    public void setWind(String eventName, String raceName, WindDAO windDAO) {
        Event event = service.getEventByName(eventName);
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
        service.getDomainFactory().getTrackedEvent(event).getTrackedRace(race).recordWind(wind, WindSource.WEB);
    }

    @Override
    public Map<CompetitorDAO, List<GPSFixDAO>> getBoatPositions(String eventName, String raceName, Date date,
            long tailLengthInMilliseconds, boolean extrapolate) {
        Map<CompetitorDAO, List<GPSFixDAO>> result = new HashMap<CompetitorDAO, List<GPSFixDAO>>();
        if (date != null) {
            Event event = service.getEventByName(eventName);
            if (event != null) {
                RaceDefinition race = getRaceByName(event, raceName);
                TimePoint end = new MillisecondsTimePoint(date);
                TrackedRace trackedRace = service.getDomainFactory().getTrackedEvent(event).getTrackedRace(race);
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
            Event event = service.getEventByName(eventName);
            if (event != null) {
                RaceDefinition race = getRaceByName(event, raceName);
                TimePoint dateAsTimePoint = new MillisecondsTimePoint(date);
                TrackedRace trackedRace = service.getDomainFactory().getTrackedEvent(event).getTrackedRace(race);
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
        Event event = service.getEventByName(eventName);
        List<QuickRankDAO> result = new ArrayList<QuickRankDAO>();
        if (event != null) {
            RaceDefinition race = getRaceByName(event, raceName);
            if (race != null) {
                TimePoint dateAsTimePoint = new MillisecondsTimePoint(date);
                TrackedRace trackedRace = service.getDomainFactory().getTrackedEvent(event).getTrackedRace(race);
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
    
}