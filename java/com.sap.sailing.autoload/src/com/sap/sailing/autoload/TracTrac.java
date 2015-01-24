package com.sap.sailing.autoload;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.persistence.MongoRaceLogStoreFactory;
import com.sap.sailing.domain.persistence.MongoRegattaLogStoreFactory;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.regattalog.RegattaLogStore;
import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sailing.domain.tracking.TrackerManager;
import com.sap.sailing.domain.tractracadapter.RaceRecord;
import com.sap.sailing.domain.tractracadapter.TracTracAdapter;
import com.sap.sailing.domain.tractracadapter.TracTracAdapterFactory;
import com.sap.sailing.domain.tractracadapter.TracTracConnectionConstants;
import com.sap.sailing.gwt.ui.shared.TracTracRaceRecordDTO;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * Auto load TracTrac races from a parameter called <code>tractrac.autoload.json_urls</code>. This
 * parameter contains a list of all event json urls from which you want to load races from. Only
 * races in state <code>REPLAY</code> will be loaded.
 * 
 * @author Simon Marcel Pamies
 */
public class TracTrac {
    Logger logger = Logger.getLogger(TracTrac.class.getName());
    
    private final TracTracAdapter tracTracAdapter;
    private final RacingEventService racingEventService;

    public TracTrac(BundleContext context, RacingEventService racingEventService) {
        this.racingEventService = racingEventService;
        this.tracTracAdapter = createAndOpenTracTracAdapterTracker(context).getService().getOrCreateTracTracAdapter(this.racingEventService.getBaseDomainFactory());
        try {
            autoLoadTrackTrackRaces();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private TracTracAdapter getTracTracAdapter() {
        return this.tracTracAdapter;
    }
    
    private RacingEventService getService() {
        return this.racingEventService;
    }
    
    private void autoLoadTrackTrackRaces() throws Exception {
        String jsonUrls = System.getProperty("tractrac.autoload.json_urls");
        if (jsonUrls != null) {
            String[] splitJsonUrls = jsonUrls.split(",");
            for (String jsonUrl : splitJsonUrls) {
                logger.info("Loading races from " + jsonUrl);
                Pair<String, List<TracTracRaceRecordDTO>> racesThatCanBeTracked = listTracTracRacesInEvent(jsonUrl, /*listHiddenRaces*/false);
                
                for (TracTracRaceRecordDTO raceRecord : racesThatCanBeTracked.getB()) {
                    RaceRecord record = getTracTracAdapter().getSingleTracTracRaceRecord(new URL(raceRecord.jsonURL), raceRecord.id, /*loadClientParams*/true);
                    if (record.getRaceStatus().equals(TracTracConnectionConstants.REPLAY_STATUS) || record.getRaceVisibility().equals(TracTracConnectionConstants.REPLAY_VISIBILITY)) {
                        RegattaIdentifier regattaForRaceRecord = null;
                        if (raceRecord.hasRememberedRegatta()) {
                            Regatta regatta = getService().getRememberedRegattaForRace(raceRecord.id);
                            if (regatta != null) {
                                regattaForRaceRecord = regatta.getRegattaIdentifier();
                            }
                        }
                        RaceLogStore raceLogStore = MongoRaceLogStoreFactory.INSTANCE.getMongoRaceLogStore(getService()
                                .getMongoObjectFactory(), getService().getDomainObjectFactory());
                        RegattaLogStore regattaLogStore = MongoRegattaLogStoreFactory.INSTANCE.getMongoRegattaLogStore(
                                getService().getMongoObjectFactory(), getService().getDomainObjectFactory());
                        getTracTracAdapter().addTracTracRace(
                                (TrackerManager) getService(),
                                regattaForRaceRecord,
                                record.getParamURL(), /* effectiveLiveURI */
                                null,
                                record.getStoredURI(),
                                new URI(""),
                                new MillisecondsTimePoint(record.getTrackingStartTime().asMillis()),
                                new MillisecondsTimePoint(record.getTrackingEndTime().asMillis()),
                                raceLogStore, regattaLogStore,
                                RaceTracker.TIMEOUT_FOR_RECEIVING_RACE_DEFINITION_IN_MILLISECONDS, /* simulateWithStartTimeNow */
                                false, /*useTracTracMarkPassings*/ false,
                                /* tracTracUsername */"", /* tracTracPassword */"", record.getRaceStatus(),
                                record.getRaceVisibility());
                    } else {
                        logger.info("Ignoring race " + record.getName() + " because it is in status "
                                + record.getRaceStatus() + " and visibility " + record.getRaceVisibility());
                    }
                }
            }
        }
    }
    
    public com.sap.sse.common.Util.Pair<String, List<TracTracRaceRecordDTO>> listTracTracRacesInEvent(String eventJsonURL, boolean listHiddenRaces) throws MalformedURLException, IOException, ParseException, org.json.simple.parser.ParseException, URISyntaxException {
        com.sap.sse.common.Util.Pair<String,List<RaceRecord>> raceRecords;
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
                    raceRecord.getBoatClassNames(), raceRecord.getRaceStatus(), raceRecord.getRaceVisibility(), raceRecord.getJsonURL().toString(),
                    hasRememberedRegatta(raceRecord.getID())));
        }
        return new com.sap.sse.common.Util.Pair<String, List<TracTracRaceRecordDTO>>(raceRecords.getA(), result);
    }
    
    private boolean hasRememberedRegatta(Serializable raceID) {
        return racingEventService.getRememberedRegattaForRace(raceID) != null;
    }
    
    private ServiceTracker<TracTracAdapterFactory, TracTracAdapterFactory> createAndOpenTracTracAdapterTracker(BundleContext context) {
        ServiceTracker<TracTracAdapterFactory, TracTracAdapterFactory> result = new ServiceTracker<TracTracAdapterFactory, TracTracAdapterFactory>(
                context, TracTracAdapterFactory.class.getName(), null);
        result.open();
        result.getService().getOrCreateTracTracAdapter(racingEventService.getBaseDomainFactory());
        return result;
    }

}
