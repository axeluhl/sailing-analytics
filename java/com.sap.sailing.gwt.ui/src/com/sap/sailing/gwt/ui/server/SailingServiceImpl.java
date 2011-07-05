package com.sap.sailing.gwt.ui.server;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.RaceRecord;
import com.sap.sailing.domain.tractracadapter.TracTracConfiguration;
import com.sap.sailing.gwt.ui.client.SailingService;
import com.sap.sailing.gwt.ui.shared.BoatClassDAO;
import com.sap.sailing.gwt.ui.shared.CompetitorDAO;
import com.sap.sailing.gwt.ui.shared.EventDAO;
import com.sap.sailing.gwt.ui.shared.RaceDAO;
import com.sap.sailing.gwt.ui.shared.RaceRecordDAO;
import com.sap.sailing.gwt.ui.shared.RegattaDAO;
import com.sap.sailing.gwt.ui.shared.TracTracConfigurationDAO;
import com.sap.sailing.mongodb.DomainObjectFactory;
import com.sap.sailing.mongodb.MongoObjectFactory;
import com.sap.sailing.mongodb.MongoWindStoreFactory;
import com.sap.sailing.server.RacingEventService;

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
            result.add(eventDAO);
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
            List<RaceDAO> raceDAOsInBoatClass = getRaceDAOs(e.getValue());
            RegattaDAO regatta = new RegattaDAO(new BoatClassDAO(e.getKey().getName()), raceDAOsInBoatClass);
            result.add(regatta);
        }
        return result;
    }

    private List<RaceDAO> getRaceDAOs(Set<RaceDefinition> races) {
        List<RaceDAO> result = new ArrayList<RaceDAO>();
        for (RaceDefinition r : races) {
            result.add(new RaceDAO(r.getName(), getCompetitorDAOs(r.getCompetitors())));
        }
        return result;
    }

    private List<CompetitorDAO> getCompetitorDAOs(Iterable<Competitor> competitors) {
        List<CompetitorDAO> result = new ArrayList<CompetitorDAO>();
        for (Competitor c : competitors) {
            result.add(new CompetitorDAO(c.getName(), c.getTeam().getNationality().getCountryCode().getTwoLetterISOCode(),
                    c.getTeam().getNationality().getCountryCode().getThreeLetterIOCCode()));
        }
        return result;
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
    public void track(RaceRecordDAO rr, String liveURI, String storedURI) throws Exception {
        service.addRace(new URL(rr.paramURL), new URI(liveURI), new URI(storedURI),
                MongoWindStoreFactory.INSTANCE.getMongoWindStore(MongoObjectFactory.INSTANCE));
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
}