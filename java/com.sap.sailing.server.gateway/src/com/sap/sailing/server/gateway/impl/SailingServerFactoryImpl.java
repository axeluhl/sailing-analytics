package com.sap.sailing.server.gateway.impl;

import java.net.URL;
import java.util.logging.Logger;

import com.sap.sailing.server.gateway.interfaces.SailingServer;
import com.sap.sailing.server.gateway.interfaces.SailingServerFactory;
import com.sap.sse.replication.FullyInitializedReplicableTracker;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.util.RemoteServerUtil;

public class SailingServerFactoryImpl implements SailingServerFactory {
    private static final Logger logger = Logger.getLogger(SailingServerFactoryImpl.class.getName());
    private final FullyInitializedReplicableTracker<SecurityService> securityServiceTracker;
    
    public SailingServerFactoryImpl(FullyInitializedReplicableTracker<SecurityService> securityServiceTracker) {
        super();
        this.securityServiceTracker = securityServiceTracker;
    }

    @Override
    public SailingServer getSailingServer(URL baseUrl) {
        String bearerToken;
        try {
            final SecurityService securityService = securityServiceTracker.getInitializedService(/* timeout in millis */ 1000);
            final User user = securityService.getCurrentUser();
            if (user != null) {
                bearerToken = securityService.getAccessToken(user.getName());
            } else {
                bearerToken = null;
            }
        } catch (InterruptedException e) {
            logger.warning("Interrupted while trying to obtain security service; continuing without it.");
            bearerToken = null;
        }
        return new SailingServerImpl(baseUrl, bearerToken);
    }

    @Override
    public SailingServer getSailingServer(URL baseUrl, String bearerToken) {
        return new SailingServerImpl(baseUrl, bearerToken);
    }

    @Override
    public SailingServer getSailingServer(URL baseUrl, String username, String password) {
        return getSailingServer(baseUrl, RemoteServerUtil.resolveBearerTokenForRemoteServer(baseUrl.toString(), username, password));
    }

}
