package com.sap.sse.security.jaxrs;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

import org.osgi.util.tracker.ServiceTracker;

import com.sap.sse.rest.StreamingOutputUtil;
import com.sap.sse.security.SecurityService;

public abstract class AbstractSecurityResource extends StreamingOutputUtil {
    @Context
    ServletContext servletContext;

    public SecurityService getService() {
        @SuppressWarnings("unchecked")
        ServiceTracker<SecurityService, SecurityService> tracker = (ServiceTracker<SecurityService, SecurityService>) servletContext
                .getAttribute(RestServletContainer.SECURITY_SERVICE_TRACKER_NAME);
        return tracker.getService();
    }
}
