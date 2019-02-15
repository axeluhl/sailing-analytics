package com.sap.sailing.windestimation.jaxrs;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.windestimation.integration.WindEstimationFactoryServiceImpl;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public abstract class AbstractWindEstimationDataResource {
    @Context
    ServletContext servletContext;

    public WindEstimationFactoryServiceImpl getWindEstimationFactoryServiceImpl() {
        @SuppressWarnings("unchecked")
        ServiceTracker<WindEstimationFactoryServiceImpl, WindEstimationFactoryServiceImpl> tracker = (ServiceTracker<WindEstimationFactoryServiceImpl, WindEstimationFactoryServiceImpl>) servletContext
                .getAttribute(RestServletContainer.WIND_ESTIMATION_FACTORY_SERVICE_TRACKER_NAME);
        return tracker.getService();
    }
}
