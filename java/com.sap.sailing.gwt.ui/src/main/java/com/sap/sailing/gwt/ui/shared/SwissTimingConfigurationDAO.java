package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SwissTimingConfigurationDAO implements IsSerializable {
    public String name;
    public String hostname;
    public int port;
    public boolean canSendRequests;
    
    public SwissTimingConfigurationDAO() {}

    public SwissTimingConfigurationDAO(String name, String hostname, int port, boolean canSendRequests) {
        super();
        this.name = name;
        this.hostname = hostname;
        this.port = port;
        this.canSendRequests = canSendRequests;
    }
    
}
