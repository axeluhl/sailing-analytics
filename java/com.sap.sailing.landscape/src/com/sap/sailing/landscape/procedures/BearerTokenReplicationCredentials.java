package com.sap.sailing.landscape.procedures;

import java.util.Collections;

import com.sap.sailing.landscape.ReplicationCredentials;

public class BearerTokenReplicationCredentials implements ReplicationCredentials {
    /**
     * The user data variable used to specify which bearer token to use to authenticate at the master
     * in case this is to become a replica of some sort, e.g., replicating the {@code SecurityService}
     * and the {@code SharedSailingData} service.
     */
    private final static String REPLICATE_MASTER_BEARER_TOKEN = "REPLICATE_MASTER_BEARER_TOKEN";
    
    private final String bearerToken;
    
    public BearerTokenReplicationCredentials(String bearerToken) {
        this.bearerToken = bearerToken;
    }

    @Override
    public Iterable<String> getUserData() {
        return Collections.singleton(REPLICATE_MASTER_BEARER_TOKEN+"="+bearerToken);
    }

}
