package com.sap.sailing.gwt.ui.test;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.swisstimingadapter.RaceSpecificMessageLoader;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingAdapter;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingAdapterFactory;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingFactory;
import com.sap.sailing.domain.swisstimingreplayadapter.SwissTimingReplayService;
import com.sap.sailing.domain.swisstimingreplayadapter.SwissTimingReplayServiceFactory;
import com.sap.sailing.domain.tracking.RacesHandle;
import com.sap.sailing.domain.tracking.TrackerManager;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tractracadapter.RaceRecord;
import com.sap.sailing.domain.tractracadapter.TracTracAdapter;
import com.sap.sailing.domain.tractracadapter.TracTracAdapterFactory;
import com.sap.sailing.domain.tractracadapter.TracTracConfiguration;
import com.sap.sailing.gwt.ui.server.SailingServiceImpl;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.impl.RacingEventServiceImpl;
import com.sap.sailing.server.replication.ReplicationService;
import com.sap.sailing.server.replication.impl.ReplicationInstancesManager;
import com.sap.sailing.server.replication.impl.ReplicationServiceImpl;

public class SailingServiceImplMock extends SailingServiceImpl {
    private static final long serialVersionUID = 8564037671550730455L;
    private RacingEventService service;
    
    public SailingServiceImplMock() {
        super();
        service = new RacingEventServiceImpl();
    }

    @Override
    protected ServiceTracker<RacingEventService, RacingEventService> createAndOpenRacingEventServiceTracker(
            BundleContext context) {
        @SuppressWarnings("unchecked")
        ServiceTracker<RacingEventService, RacingEventService> result = mock(ServiceTracker.class);
        when(result.getService()).thenReturn(new RacingEventServiceImpl());
        return result;
    }
    
    @Override
    protected ServiceTracker<ReplicationService, ReplicationService> createAndOpenReplicationServiceTracker(
            BundleContext context) {
        @SuppressWarnings("unchecked")
        ServiceTracker<ReplicationService, ReplicationService> result = mock(ServiceTracker.class);
        try {
            final ReplicationServiceImpl replicationService = new ReplicationServiceImpl("test exchange", "localhost", new ReplicationInstancesManager()) {
                @Override
                protected ServiceTracker<RacingEventService, RacingEventService> getRacingEventServiceTracker() {
                    @SuppressWarnings("unchecked")
                    ServiceTracker<RacingEventService, RacingEventService> result = (ServiceTracker<RacingEventService, RacingEventService>) mock(ServiceTracker.class);
                    doReturn("Humba Humba").when(result).toString();
                    when(result.getService()).thenReturn(new RacingEventServiceImpl());
                    return result;
                }
            };
            when(result.getService()).thenReturn(replicationService);
        } catch (IOException e) {
            throw new RuntimeException();
        }
        return result;
    }
    
    @Override
    protected ServiceTracker<ScoreCorrectionProvider, ScoreCorrectionProvider> createAndOpenScoreCorrectionProviderServiceTracker(
            BundleContext bundleContext) {
        return null;
    }

    @Override
    protected ServiceTracker<TracTracAdapterFactory, TracTracAdapterFactory> createAndOpenTracTracAdapterTracker(
            BundleContext context) {
        @SuppressWarnings("unchecked")
        ServiceTracker<TracTracAdapterFactory, TracTracAdapterFactory> result = mock(ServiceTracker.class);
        when(result.getService()).thenReturn(new TracTracAdapterFactory() {
            @Override
            public TracTracAdapter getOrCreateTracTracAdapter(DomainFactory baseDomainFactory) {
                return new TracTracAdapter() {
                    
                    @Override
                    public Pair<String, List<RaceRecord>> getTracTracRaceRecords(URL jsonURL, boolean loadClientParams)
                            throws IOException, ParseException, org.json.simple.parser.ParseException, URISyntaxException {
                        // TODO Auto-generated method stub
                        return null;
                    }
                    
                    @Override
                    public com.sap.sailing.domain.tractracadapter.DomainFactory getTracTracDomainFactory() {
                        // TODO Auto-generated method stub
                        return null;
                    }
                    
                    @Override
                    public RaceRecord getSingleTracTracRaceRecord(URL jsonURL, String raceId, boolean loadClientParams)
                            throws Exception {
                        // TODO Auto-generated method stub
                        return null;
                    }
                    
                    @Override
                    public TracTracConfiguration createTracTracConfiguration(String name, String jsonURL, String liveDataURI,
                            String storedDataURI, String courseDesignUpdateURI, String tracTracUsername, String tracTracPassword) {
                        // TODO Auto-generated method stub
                        return null;
                    }
                    
                    @Override
                    public RacesHandle addTracTracRace(TrackerManager trackerManager, RegattaIdentifier regattaToAddTo, URL paramURL,
                            URI liveURI, URI storedURI, URI courseDesignUpdateURI, TimePoint trackingStartTime,
                            TimePoint trackingEndTime, RaceLogStore raceLogStore, long timeoutForReceivingRaceDefinitionInMilliseconds,
                            boolean simulateWithStartTimeNow, String tracTracUsername, String tracTracPassword)
                            throws MalformedURLException, FileNotFoundException, URISyntaxException, Exception {
                        // TODO Auto-generated method stub
                        return null;
                    }
                    
                    @Override
                    public RacesHandle addTracTracRace(TrackerManager trackerManager, URL paramURL, URI liveURI, URI storedURI,
                            URI courseDesignUpdateURI, RaceLogStore raceLogStore, long timeoutInMilliseconds, String tracTracUsername,
                            String tracTracPassword) throws MalformedURLException, FileNotFoundException, URISyntaxException, Exception {
                        // TODO Auto-generated method stub
                        return null;
                    }
                    
                    @Override
                    public Regatta addRegatta(TrackerManager trackerManager, URL jsonURL, URI liveURI, URI storedURI,
                            URI courseDesignUpdateURI, WindStore windStore, long timeoutInMilliseconds, String tracTracUsername,
                            String tracTracPassword, RaceLogStore raceLogStore) throws MalformedURLException, FileNotFoundException,
                            URISyntaxException, IOException, ParseException, org.json.simple.parser.ParseException, Exception {
                        // TODO Auto-generated method stub
                        return null;
                    }
                };
            }
        });
        return result;
    }

    @Override
    protected ServiceTracker<SwissTimingAdapterFactory, SwissTimingAdapterFactory> createAndOpenSwissTimingAdapterTracker(
            BundleContext context) {
        @SuppressWarnings("unchecked")
        ServiceTracker<SwissTimingAdapterFactory, SwissTimingAdapterFactory> result = mock(ServiceTracker.class);
        when(result.getService()).thenReturn(new SwissTimingAdapterFactory() {
            @Override
            public SwissTimingAdapter getOrCreateSwissTimingAdapter(DomainFactory baseDomainFactory,
                    RaceSpecificMessageLoader persistence) {
                return new SwissTimingAdapter() {
                    @Override
                    public List<com.sap.sailing.domain.swisstimingadapter.RaceRecord> getSwissTimingRaceRecords(
                            String hostname, int port, boolean canSendRequests) throws InterruptedException,
                            UnknownHostException, IOException, ParseException {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public RacesHandle addSwissTimingRace(TrackerManager trackerManager,
                            RegattaIdentifier regattaToAddTo, String raceID, String hostname, int port,
                            boolean canSendRequests, RaceLogStore logStore, long timeoutInMilliseconds)
                            throws InterruptedException, UnknownHostException, IOException, ParseException,
                            Exception {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public SwissTimingFactory getSwissTimingFactory() {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public void storeSwissTimingDummyRace(String racMessage, String stlMesssage, String ccgMessage)
                            throws IllegalArgumentException {
                        // TODO Auto-generated method stub
                        
                    }

                    @Override
                    public com.sap.sailing.domain.swisstimingadapter.DomainFactory getSwissTimingDomainFactory() {
                        return com.sap.sailing.domain.swisstimingadapter.DomainFactory.INSTANCE;
                    }
                };
            }
        });
        return result;
    }

    @Override
    protected ServiceTracker<SwissTimingReplayServiceFactory, SwissTimingReplayServiceFactory> createAndOpenSwissTimingReplayServiceTracker(
            BundleContext context) {
        @SuppressWarnings("unchecked")
        ServiceTracker<SwissTimingReplayServiceFactory, SwissTimingReplayServiceFactory> result = mock(ServiceTracker.class);
        final SwissTimingReplayServiceFactory swissTimingReplayService = new SwissTimingReplayServiceFactory() {
            @Override
            public SwissTimingReplayService createSwissTimingReplayService(
                    com.sap.sailing.domain.swisstimingadapter.DomainFactory domainFactory) {
                // TODO Auto-generated method stub
                return null;
            }
        };
        when(result.getService()).thenReturn(swissTimingReplayService);
        return result;
    }

    @Override
    protected RacingEventService getService() {
        if (service == null) {
            service = new RacingEventServiceImpl();
        }
        return service;
    }
}
