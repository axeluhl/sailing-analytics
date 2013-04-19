package com.sap.sailing.gwt.ui.test;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.gwt.ui.server.SailingServiceImpl;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.impl.RacingEventServiceImpl;

public class MyTestSailingServiceImpl extends SailingServiceImpl implements MyTestSailingService {
    private static final long serialVersionUID = 3647254510616623048L;
    
    private RacingEventService service;

    public MyTestSailingServiceImpl(){
        super();
        service = new RacingEventServiceImpl();
    }
    
    public RacingEventService getService(){
        return service;
    }

    @Override
    protected ServiceTracker<RacingEventService, RacingEventService> createAndOpenRacingEventServiceTracker(
            BundleContext context) {
        // TODO Auto-generated method stub
        return null;
    }
    
    
}
