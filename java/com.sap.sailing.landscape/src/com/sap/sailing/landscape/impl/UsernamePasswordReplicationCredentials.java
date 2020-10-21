package com.sap.sailing.landscape.impl;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.landscape.ReplicationCredentials;
import com.sap.sse.landscape.UserData;

public class UsernamePasswordReplicationCredentials implements ReplicationCredentials {
    private final String username;
    private final String password;
    
    public UsernamePasswordReplicationCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public Map<UserData, String> getUserData() {
        final Map<UserData, String> result = new HashMap<>();
        result.put(UserData.REPLICATE_MASTER_USERNAME, username);
        result.put(UserData.REPLICATE_MASTER_PASSWORD, password);
        return result;
    }
}
