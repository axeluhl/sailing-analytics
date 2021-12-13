package com.sap.sailing.landscape.gateway.jaxrs;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.landscape.LandscapeService;

public abstract class AbstractLandscapeResource {
    @Context
    ServletContext servletContext;

    // Working with concrete implementation not to create new dependencies between domain and datamining
    // (PolarDataMiner is unavailable in com.sap.sailing.domain)
    public LandscapeService getLandscapeService() {
        @SuppressWarnings("unchecked")
        ServiceTracker<LandscapeService, LandscapeService> tracker = (ServiceTracker<LandscapeService, LandscapeService>) servletContext
                .getAttribute(RestServletContainer.LANDSCAPE_SERVICE_TRACKER_NAME);
        return tracker.getService();
    }
}
