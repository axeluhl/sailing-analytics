package com.sap.sailing.landscape.gateway.jaxrs;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

import org.apache.shiro.SecurityUtils;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.landscape.LandscapeService;
import com.sap.sailing.shared.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sse.landscape.common.shared.SecuredLandscapeTypes;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;

public abstract class AbstractLandscapeResource extends AbstractSailingServerResource {
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
    
    protected void checkLandscapeManageAwsPermission() {
        SecurityUtils.getSubject().checkPermission(SecuredLandscapeTypes.LANDSCAPE.getStringPermissionForTypeRelativeIdentifier(SecuredLandscapeTypes.LandscapeActions.MANAGE,
                new TypeRelativeObjectIdentifier("AWS")));
    }
}
