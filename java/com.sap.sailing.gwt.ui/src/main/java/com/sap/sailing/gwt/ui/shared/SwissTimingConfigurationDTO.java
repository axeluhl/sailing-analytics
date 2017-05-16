package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SwissTimingConfigurationDTO implements IsSerializable {
    private String name;
    private String jsonURL;
    private  String hostname;
    private  int port;
    
    public SwissTimingConfigurationDTO() {}

    public SwissTimingConfigurationDTO(String name, String jsonURL, String hostname, int port) {
        super();
        this.name = name;
        this.jsonURL = jsonURL;
        this.hostname = hostname;
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public String getJsonURL() {
        return jsonURL;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }
    
}
