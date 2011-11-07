package com.sap.sailing.domain.swisstimingadapter.impl;

import com.sap.sailing.domain.swisstimingadapter.SwissTimingConfiguration;

public class SwissTimingConfigurationImpl implements SwissTimingConfiguration {
    private final String name;
    private final String hostname;
    private final int port;
    
    public SwissTimingConfigurationImpl(String name, String hostname, int port) {
        super();
        this.name = name;
        this.hostname = hostname;
        this.port = port;
    }

    @Override
    public String getName() {
        return name;
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
