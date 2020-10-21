package com.sap.sailing.landscape.impl;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.landscape.ReplicationCredentials;
import com.sap.sse.landscape.UserData;

public class BearerTokenReplicationCredentials implements ReplicationCredentials {
    private final String bearerToken;
    
    public BearerTokenReplicationCredentials(String bearerToken) {
        this.bearerToken = bearerToken;
    }

    @Override
    public Map<UserData, String> getUserData() {
        final Map<UserData, String> result = new HashMap<>();
        result.put(UserData.REPLICATE_MASTER_BEARER_TOKEN, bearerToken);
        return result;
    }

}
