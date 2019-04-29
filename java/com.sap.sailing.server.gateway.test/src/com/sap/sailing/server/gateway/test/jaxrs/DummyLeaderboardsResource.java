package com.sap.sailing.server.gateway.test.jaxrs;

import com.sap.sailing.server.gateway.jaxrs.api.LeaderboardsResource;
import com.sap.sse.security.SecurityService;

/**
 * Required so mockito has visibility to getSecurityService
 */
public class DummyLeaderboardsResource extends LeaderboardsResource {
    @Override
    protected SecurityService getSecurityService() {
        return super.getSecurityService();
    }
}
