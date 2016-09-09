package com.sap.sailing.polars.jaxrs;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.polars.PolarDataService;


public abstract class AbstractPolarResource {
    @Context ServletContext servletContext;
    
    protected PolarDataService getPolarDataService() {
        @SuppressWarnings("unchecked")
        ServiceTracker<PolarDataService, PolarDataService> tracker = (ServiceTracker<PolarDataService, PolarDataService>) servletContext
                .getAttribute(RestServletContainer.POLAR_DATA_SERVICE_TRACKER_NAME);
        return tracker.getService();
    }
}
