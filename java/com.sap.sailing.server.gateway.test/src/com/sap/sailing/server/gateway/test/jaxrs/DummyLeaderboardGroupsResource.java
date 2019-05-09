package com.sap.sailing.server.gateway.test.jaxrs;

import com.sap.sailing.server.gateway.jaxrs.api.LeaderboardGroupsResource;
import com.sap.sse.security.SecurityService;

/**
 * Required so mockito has visibility to getSecurityService
 */
public class DummyLeaderboardGroupsResource extends LeaderboardGroupsResource {
    @Override
    protected SecurityService getSecurityService() {
        return super.getSecurityService();
    }
}
