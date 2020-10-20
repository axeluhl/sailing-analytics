package com.sap.sailing.landscape.procedures;

import java.util.Arrays;

import com.sap.sailing.landscape.ReplicationCredentials;

public class UserPasswordReplicationCredentials implements ReplicationCredentials {
    private final static String REPLICATE_MASTER_USERNAME = "REPLICATE_MASTER_USERNAME";
    private final static String REPLICATE_MASTER_PASSWORD = "REPLICATE_MASTER_PASSWORD";
    
    private final String username;
    private final String password;
    
    public UserPasswordReplicationCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public Iterable<String> getUserData() {
        return Arrays.asList(REPLICATE_MASTER_USERNAME+"="+username, REPLICATE_MASTER_PASSWORD+"="+password);
    }
}
