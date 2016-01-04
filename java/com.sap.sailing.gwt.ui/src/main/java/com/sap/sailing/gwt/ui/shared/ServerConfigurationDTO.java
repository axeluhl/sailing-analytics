package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ServerConfigurationDTO implements IsSerializable {
    private boolean isStandaloneServer;

    // for GWT
    ServerConfigurationDTO() {
    }

    public ServerConfigurationDTO(boolean isStandaloneServer) {
        this.isStandaloneServer = isStandaloneServer;
    }

    public boolean isStandaloneServer() {
        return isStandaloneServer;
    }
}
