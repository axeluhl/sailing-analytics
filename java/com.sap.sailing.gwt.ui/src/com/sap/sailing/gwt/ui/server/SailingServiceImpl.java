package com.sap.sailing.gwt.ui.server;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sap.sailing.domain.tractracadapter.RaceRecord;
import com.sap.sailing.gwt.ui.client.SailingService;
import com.sap.sailing.gwt.ui.shared.EventDAO;
import com.sap.sailing.gwt.ui.shared.RaceRecordDAO;
import com.sap.sailing.gwt.ui.shared.RegattaDAO;
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
        List<RegattaDAO> regattas = Collections.emptyList();
        return Arrays.asList(new EventDAO[] { new EventDAO("Kiel Week", /* regattas */ regattas), new EventDAO("STG Training", /* regattas */ regattas) });
    }

    @Override
    public List<RaceRecordDAO> listRacesInEvent(String eventJsonURL) throws MalformedURLException, IOException,
            ParseException, org.json.simple.parser.ParseException {
        List<RaceRecord> raceRecords;
        raceRecords = service.getRaceRecords(new URL(eventJsonURL));
        List<RaceRecordDAO> result = new ArrayList<RaceRecordDAO>();
        for (RaceRecord raceRecord : raceRecords) {
            result.add(new RaceRecordDAO(raceRecord.getID(), raceRecord.getName(), raceRecord.getParamURL().toString(),
                    raceRecord.getReplayURL(), raceRecord.getTrackingStartTime().asDate(), raceRecord.getTrackingEndTime().asDate(),
                    raceRecord.getRaceStartTime().asDate()));
        }
        return result;
    }
}
