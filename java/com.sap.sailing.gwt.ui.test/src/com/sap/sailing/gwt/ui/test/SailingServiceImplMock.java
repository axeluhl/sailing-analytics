package com.sap.sailing.gwt.ui.test;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBException;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.racelog.tracking.test.mock.MockSmartphoneImeiServiceFinderFactory;
import com.sap.sailing.domain.racelogtracking.RaceLogTrackingAdapterFactory;
import com.sap.sailing.domain.regattalog.RegattaLogStore;
import com.sap.sailing.domain.swisstimingadapter.StartList;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingAdapter;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingAdapterFactory;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingFactory;
import com.sap.sailing.domain.swisstimingreplayadapter.SwissTimingReplayService;
import com.sap.sailing.domain.tracking.RaceHandle;
import com.sap.sailing.domain.tracking.RaceTrackingHandler;
import com.sap.sailing.domain.tracking.TrackerManager;
import com.sap.sailing.domain.tractracadapter.TracTracAdapterFactory;
import com.sap.sailing.gwt.ui.server.SailingServiceImpl;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.impl.RacingEventServiceImpl;
import com.sap.sailing.xrr.schema.RegattaResults;
import com.sap.sse.replication.Replicable;
import com.sap.sse.replication.ReplicationService;
import com.sap.sse.replication.impl.AbstractReplicablesProvider;
import com.sap.sse.replication.impl.ReplicationInstancesManager;
import com.sap.sse.replication.impl.ReplicationServiceImpl;

public class SailingServiceImplMock extends SailingServiceImpl {
    private static final long serialVersionUID = 8564037671550730455L;

    private final RacingEventService service;
    private ReplicationService replicationService;
    private final ScoreCorrectionProvider scoreCorrectionProvider = null;
    private final TracTracAdapterFactory tracTracAdapterFactory = TracTracAdapterFactory.INSTANCE;
    private final SwissTimingAdapterFactory swissTimingAdapterFactory;
    private final SwissTimingReplayService swissTimingReplayService = null;
    private final RaceLogTrackingAdapterFactory raceLogTrackingAdapterFactory = RaceLogTrackingAdapterFactory.INSTANCE;

    public SailingServiceImplMock() {
        super();
        service = new RacingEventServiceImpl(/* clearPersistentCompetitorStore */ true, new MockSmartphoneImeiServiceFinderFactory(), /* restoreTrackedRaces */ false);
        try {
            replicationService = new ReplicationServiceImpl("test exchange", "localhost", 0,
                    new ReplicationInstancesManager(), new AbstractReplicablesProvider() {
                        @Override
                        public Iterable<Replicable<?, ?>> getReplicables() {
                            return Collections.<Replicable<?, ?>> singleton(service);
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
        swissTimingAdapterFactory = new SwissTimingAdapterFactory() {
            @Override
            public SwissTimingAdapter getOrCreateSwissTimingAdapter(DomainFactory baseDomainFactory) {
                return new SwissTimingAdapter() {
                    @Override
                    public List<com.sap.sailing.domain.swisstimingadapter.RaceRecord> getSwissTimingRaceRecords(
                            String hostname, int port) throws InterruptedException, UnknownHostException, IOException,
                            ParseException {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public RaceHandle addSwissTimingRace(TrackerManager trackerManager,
                            RegattaIdentifier regattaToAddTo, String raceID, String raceName, String raceDescription,
                            BoatClass boatClass, String hostname, int port, StartList startList, RaceLogStore logStore,
                            RegattaLogStore regattaLogStore, long timeoutInMilliseconds, boolean useInternalMarkPassingAlgorithm, boolean trackWind, boolean correctWindDirectionByMagneticDeclination, String updateURL, String updateUsername, String updatePassword,
                            RaceTrackingHandler raceTrackingHandler) throws InterruptedException,
                            UnknownHostException, IOException, ParseException, Exception {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public SwissTimingFactory getSwissTimingFactory() {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public com.sap.sailing.domain.swisstimingadapter.DomainFactory getSwissTimingDomainFactory() {
                        return com.sap.sailing.domain.swisstimingadapter.DomainFactory.INSTANCE;
                    }

                    @Override
                    public StartList readStartListForRace(String raceId, RegattaResults regattaResults) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public RegattaResults readRegattaEntryListFromXrrUrl(String xrrEntryListUrl) throws IOException,
                            JAXBException {
                        // TODO Auto-generated method stub
                        return null;
                    }
                };
            }
        };
    }

    public ScoreCorrectionProvider getScoreCorrectionProvider() {
        return scoreCorrectionProvider;
    }

    @Override
    protected TracTracAdapterFactory getTracTracAdapterFactory() {
        return tracTracAdapterFactory;
    }

    @Override
    protected SwissTimingAdapterFactory getSwissTimingAdapterFactory() {
        return swissTimingAdapterFactory;
    }

    @Override
    protected SwissTimingReplayService getSwissTimingReplayService() {
        return swissTimingReplayService;
    }

    @Override
    protected RaceLogTrackingAdapterFactory getRaceLogTrackingAdapterFactory() {
        return raceLogTrackingAdapterFactory;
    }
    
    @Override
    protected ReplicationService getReplicationService() {
        return replicationService;
    }

    @Override
    protected RacingEventService getService() {
        return service;
    }

    public RacingEventService getRacingEventService() {
        return getService();
    }
}
