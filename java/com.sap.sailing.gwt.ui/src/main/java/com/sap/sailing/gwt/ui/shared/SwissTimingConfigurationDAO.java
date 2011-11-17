package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SwissTimingConfigurationDAO implements IsSerializable {
    public String name;
    public String hostname;
    public int port;
    
    public SwissTimingConfigurationDAO() {}

    public SwissTimingConfigurationDAO(String name, String hostname, int port) {
        super();
        this.name = name;
        this.hostname = hostname;
        this.port = port;
    }
    
}
