package com.sap.sailing.gwt.ui.test;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.racelog.tracking.test.mock.MockSmartphoneImeiServiceFinderFactory;
import com.sap.sailing.domain.racelogtracking.RaceLogTrackingAdapterFactory;
import com.sap.sailing.domain.racelogtracking.impl.RaceLogTrackingAdapterFactoryImpl;
import com.sap.sailing.domain.swisstimingadapter.StartList;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingAdapter;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingAdapterFactory;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingFactory;
import com.sap.sailing.domain.swisstimingreplayadapter.SwissTimingReplayService;
import com.sap.sailing.domain.swisstimingreplayadapter.SwissTimingReplayServiceFactory;
import com.sap.sailing.domain.tracking.RaceHandle;
import com.sap.sailing.domain.tracking.TrackerManager;
import com.sap.sailing.domain.tractracadapter.TracTracAdapterFactory;
import com.sap.sailing.gwt.ui.server.SailingServiceImpl;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;
import com.sap.sailing.server.impl.RacingEventServiceImpl;
import com.sap.sailing.server.replication.Replicable;
import com.sap.sailing.server.replication.ReplicationService;
import com.sap.sailing.server.replication.impl.ReplicationInstancesManager;
import com.sap.sailing.server.replication.impl.ReplicationServiceImpl;
import com.sap.sailing.xrr.resultimport.schema.RegattaResults;

public class SailingServiceImplMock extends SailingServiceImpl {
    private static final long serialVersionUID = 8564037671550730455L;
    private RacingEventService service;
    
    public SailingServiceImplMock() {
        super();
        service = new RacingEventServiceImpl(true, new MockSmartphoneImeiServiceFinderFactory());
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
    protected ServiceTracker<ReplicationService<RacingEventService>, ReplicationService<RacingEventService>> createAndOpenReplicationServiceTracker(
            BundleContext context) {
        @SuppressWarnings("unchecked")
        ServiceTracker<ReplicationService<RacingEventService>, ReplicationService<RacingEventService>> result = mock(ServiceTracker.class);
        try {
            final ReplicationServiceImpl<RacingEventService, RacingEventServiceOperation<?>> replicationService =
                    new ReplicationServiceImpl<RacingEventService, RacingEventServiceOperation<?>>("test exchange", "localhost", 0,
                            new ReplicationInstancesManager<RacingEventService>()) {
                @Override
                protected ServiceTracker<Replicable<RacingEventService, RacingEventServiceOperation<?>>, Replicable<RacingEventService, RacingEventServiceOperation<?>>> getRacingEventServiceTracker() {
                    @SuppressWarnings("unchecked")
                    ServiceTracker<Replicable<RacingEventService, RacingEventServiceOperation<?>>, Replicable<RacingEventService, RacingEventServiceOperation<?>>> result =
                        (ServiceTracker<Replicable<RacingEventService, RacingEventServiceOperation<?>>, Replicable<RacingEventService, RacingEventServiceOperation<?>>>) mock(ServiceTracker.class);
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
        when(result.getService()).thenReturn(TracTracAdapterFactory.INSTANCE);
        return result;
    }

    @Override
    protected ServiceTracker<SwissTimingAdapterFactory, SwissTimingAdapterFactory> createAndOpenSwissTimingAdapterTracker(
            BundleContext context) {
        @SuppressWarnings("unchecked")
        ServiceTracker<SwissTimingAdapterFactory, SwissTimingAdapterFactory> result = mock(ServiceTracker.class);
        when(result.getService()).thenReturn(new SwissTimingAdapterFactory() {
            @Override
            public SwissTimingAdapter getOrCreateSwissTimingAdapter(DomainFactory baseDomainFactory) {
                return new SwissTimingAdapter() {
                    @Override
                    public List<com.sap.sailing.domain.swisstimingadapter.RaceRecord> getSwissTimingRaceRecords(
                            String hostname, int port) throws InterruptedException,
                            UnknownHostException, IOException, ParseException {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public RaceHandle addSwissTimingRace(TrackerManager trackerManager,
                            RegattaIdentifier regattaToAddTo, String raceID, String raceName, String raceDescription, BoatClass boatClass, String hostname,
                            int port, StartList startList, RaceLogStore logStore, long timeoutInMilliseconds)
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
    protected ServiceTracker<RaceLogTrackingAdapterFactory, RaceLogTrackingAdapterFactory> createAndOpenRaceLogTrackingAdapterTracker(
            BundleContext context) {
        @SuppressWarnings("unchecked")
        ServiceTracker<RaceLogTrackingAdapterFactory, RaceLogTrackingAdapterFactory> result = mock(ServiceTracker.class);
        RaceLogTrackingAdapterFactory factory = RaceLogTrackingAdapterFactoryImpl.INSTANCE;
        when(result.getService()).thenReturn(factory);
        return result;
    }

    @Override
    protected RacingEventService getService() {
        if (service == null) {
            service = new RacingEventServiceImpl();
        }
        return service;
    }
    
    public RacingEventService getRacingEventService() {
        return getService();
    }
}
