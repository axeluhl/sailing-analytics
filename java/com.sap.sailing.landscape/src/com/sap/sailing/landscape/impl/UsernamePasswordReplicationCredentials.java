package com.sap.sailing.landscape.impl;

import java.util.HashMap;
import java.util.Map;

import com.sap.sse.landscape.DefaultProcessConfigurationVariables;
import com.sap.sse.landscape.Log;
import com.sap.sse.landscape.Metrics;
import com.sap.sse.landscape.ProcessConfigurationVariable;
import com.sap.sse.landscape.ReplicationCredentials;
import com.sap.sse.security.util.RemoteServerUtil;

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
        result.put(DefaultProcessConfigurationVariables.REPLICATE_MASTER_USERNAME, username);
        result.put(DefaultProcessConfigurationVariables.REPLICATE_MASTER_PASSWORD, password);
        return result;
    }

    @Override
    public <LogT extends Log, MetricsT extends Metrics> String getBearerToken(String hostname, Integer port) {
        return RemoteServerUtil.resolveBearerTokenForRemoteServer(hostname, port==null?443:port, username, password);
    }
}
