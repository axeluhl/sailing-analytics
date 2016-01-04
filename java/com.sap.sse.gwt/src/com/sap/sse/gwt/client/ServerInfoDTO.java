package com.sap.sse.gwt.client;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ServerInfoDTO implements IsSerializable {
    private String serverName;
    private String buildVersion;
    
    // for GWT
    ServerInfoDTO() {
    }

    public ServerInfoDTO(String serverName, String buildVersion) {
        this.serverName = serverName;
        this.buildVersion = buildVersion;
    }

    public String getBuildVersion() {
        return buildVersion;
    }

    public String getServerName() {
        return serverName;
    }
}
