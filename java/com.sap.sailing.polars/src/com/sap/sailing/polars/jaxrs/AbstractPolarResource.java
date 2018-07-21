package com.sap.sailing.polars.jaxrs;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.polars.impl.PolarDataServiceImpl;

public abstract class AbstractPolarResource {
    @Context
    ServletContext servletContext;

    // Working with concrete implementation not to create new dependencies between domain and datamining
    // (PolarDataMiner is unavailable in com.sap.sailing.domain)
    public PolarDataServiceImpl getPolarDataServiceImpl() {
        @SuppressWarnings("unchecked")
        ServiceTracker<PolarDataServiceImpl, PolarDataServiceImpl> tracker = (ServiceTracker<PolarDataServiceImpl, PolarDataServiceImpl>) servletContext
                .getAttribute(RestServletContainer.POLAR_DATA_SERVICE_TRACKER_NAME);
        return tracker.getService();
    }
}
