package com.sap.sailing.server.gateway.test.jaxrs;

import java.util.Optional;

import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sailing.server.gateway.jaxrs.api.LeaderboardsResource;
import com.sap.sse.security.SecurityService;

/**
 * Required so mockito has visibility to getSecurityService and getScoreCorrectionProvider
 */
public class DummyLeaderboardsResource extends LeaderboardsResource {
    @Override
    protected SecurityService getSecurityService() {
        return super.getSecurityService();
    }

    @Override
    protected Optional<ScoreCorrectionProvider> getScoreCorrectionProvider(String scoreCorrectionProviderName) {
        return super.getScoreCorrectionProvider(scoreCorrectionProviderName);
    }
}
