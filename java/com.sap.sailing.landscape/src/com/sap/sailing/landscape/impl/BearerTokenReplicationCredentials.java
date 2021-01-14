package com.sap.sailing.landscape.impl;

import java.util.HashMap;
import java.util.Map;

import com.sap.sse.landscape.DefaultProcessConfigurationVariables;
import com.sap.sse.landscape.ProcessConfigurationVariable;
import com.sap.sse.landscape.ReplicationCredentials;

public class BearerTokenReplicationCredentials implements ReplicationCredentials {
    private final String bearerToken;
    
    public BearerTokenReplicationCredentials(String bearerToken) {
        this.bearerToken = bearerToken;
    }

    @Override
    public Map<ProcessConfigurationVariable, String> getUserData() {
        final Map<ProcessConfigurationVariable, String> result = new HashMap<>();
        result.put(DefaultProcessConfigurationVariables.REPLICATE_MASTER_BEARER_TOKEN, bearerToken);
        return result;
    }

}
