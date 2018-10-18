package com.sap.sailing.domain.swisstimingadapter.impl;

import com.sap.sailing.domain.swisstimingadapter.SwissTimingConfiguration;

public class SwissTimingConfigurationImpl implements SwissTimingConfiguration {
    private static final long serialVersionUID = -4594050191153845375L;
    
    private final String name;
    private final String jsonURL;
    private final String hostname;
    private final Integer port;
    private final String updateURL;
    private final String updateUsername;
    private final String updatePassword;
    
    public SwissTimingConfigurationImpl(String name, String jsonURL, String hostname, Integer port, String updateURL, String updateUsername, String updatePassword) {
        super();
        this.name = name;
        this.jsonURL = jsonURL;
        this.hostname = hostname;
        this.port = port;
        this.updateURL = updateURL;
        this.updateUsername = updateUsername;
        this.updatePassword = updatePassword;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getJsonURL() {
        return jsonURL;
    }

    @Override
    public String getHostname() {
        return hostname;
    }

    @Override
    public Integer getPort() {
        return port;
    }

    public String getUpdateURL() {
        return updateURL;
    }

    public String getUpdateUsername() {
        return updateUsername;
    }

    public String getUpdatePassword() {
        return updatePassword;
    }
}
