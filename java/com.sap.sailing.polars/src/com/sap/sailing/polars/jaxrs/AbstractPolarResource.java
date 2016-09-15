package com.sap.sailing.polars.jaxrs;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.polars.impl.PolarDataServiceImpl;


public abstract class AbstractPolarResource {
    @Context ServletContext servletContext;
    
    protected PolarDataServiceImpl getPolarDataServiceImpl() {
        @SuppressWarnings("unchecked")
        ServiceTracker<PolarDataServiceImpl, PolarDataServiceImpl> tracker = (ServiceTracker<PolarDataServiceImpl, PolarDataServiceImpl>) servletContext
                .getAttribute(RestServletContainer.POLAR_DATA_SERVICE_TRACKER_NAME);
        return tracker.getService();
    }
}
