package com.sap.sailing.landscape.impl;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.landscape.ReplicationCredentials;
import com.sap.sse.landscape.ProcessConfigurationVariable;

public class UsernamePasswordReplicationCredentials implements ReplicationCredentials {
    private final String username;
    private final String password;
    
    public UsernamePasswordReplicationCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public Map<ProcessConfigurationVariable, String> getUserData() {
        final Map<ProcessConfigurationVariable, String> result = new HashMap<>();
        result.put(ProcessConfigurationVariable.REPLICATE_MASTER_USERNAME, username);
        result.put(ProcessConfigurationVariable.REPLICATE_MASTER_PASSWORD, password);
        return result;
    }
}
