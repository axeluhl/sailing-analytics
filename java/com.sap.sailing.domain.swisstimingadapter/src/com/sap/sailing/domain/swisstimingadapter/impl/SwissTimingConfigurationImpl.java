package com.sap.sailing.domain.swisstimingadapter.impl;

import com.sap.sailing.domain.swisstimingadapter.SwissTimingConfiguration;

public class SwissTimingConfigurationImpl implements SwissTimingConfiguration {
    private final String name;
    private final String jsonURL;
    private final String hostname;
    private final int port;
    
    public SwissTimingConfigurationImpl(String name, String jsonURL, String hostname, int port) {
        super();
        this.name = name;
        this.jsonURL = jsonURL;
        this.hostname = hostname;
        this.port = port;
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
    public int getPort() {
        return port;
    }

}
