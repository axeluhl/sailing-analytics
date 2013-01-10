package com.sap.sailing.gwt.ui.test;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.gwt.ui.server.SailingServiceImpl;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.impl.RacingEventServiceImpl;

public class SailingServiceImplMock extends SailingServiceImpl {
    private static final long serialVersionUID = 8564037671550730455L;
    private final RacingEventService service;
    
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
    protected RacingEventService getService() {
        return service;
    }
}
