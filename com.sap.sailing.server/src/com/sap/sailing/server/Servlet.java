package com.sap.sailing.server;

import javax.servlet.http.HttpServlet;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

public abstract class Servlet extends HttpServlet {
    private static final long serialVersionUID = -6514453597593669376L;

    protected static final String PARAM_ACTION = "action";
    
    private ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;
    
    private RacingEventService service;
    
    protected Servlet() {
        BundleContext context = Activator.getDefault();
        racingEventServiceTracker = new ServiceTracker<RacingEventService, RacingEventService>(context, RacingEventService.class.getName(), null);
        racingEventServiceTracker.open();
        // grab the service
        service = (RacingEventService) racingEventServiceTracker.getService();
    }

    protected RacingEventService getService() {
        return service;
    }

    protected void setService(RacingEventService service) {
        this.service = service;
    }
}
