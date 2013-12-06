package com.sap.sailing.gwt.ui.test;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingAdapterFactory;
import com.sap.sailing.domain.swisstimingreplayadapter.SwissTimingReplayServiceFactory;
import com.sap.sailing.domain.tractracadapter.TracTracAdapterFactory;
import com.sap.sailing.gwt.ui.server.SailingServiceImpl;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.impl.RacingEventServiceImpl;
import com.sap.sailing.server.replication.ReplicationService;

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
        return null;
    }

    @Override
    protected ServiceTracker<TracTracAdapterFactory, TracTracAdapterFactory> createAndOpenTracTracAdapterTracker(
            BundleContext context) {
        return null;
    }

    @Override
    protected ServiceTracker<SwissTimingAdapterFactory, SwissTimingAdapterFactory> createAndOpenSwissTimingAdapterTracker(
            BundleContext context) {
        return null;
    }

    @Override
    protected ServiceTracker<SwissTimingReplayServiceFactory, SwissTimingReplayServiceFactory> createAndOpenSwissTimingReplayServiceTracker(
            BundleContext context) {
        return null;
    }

    @Override
    protected ServiceTracker<ScoreCorrectionProvider, ScoreCorrectionProvider> createAndOpenScoreCorrectionProviderServiceTracker(
            BundleContext bundleContext) {
        return null;
    }

    @Override
    protected ServiceTracker<ReplicationService, ReplicationService> createAndOpenReplicationServiceTracker(
            BundleContext context) {
        return null;
    }

    @Override
    protected RacingEventService getService() {
        if (service == null) {
            service = new RacingEventServiceImpl();
        }
        return service;
    }
}
