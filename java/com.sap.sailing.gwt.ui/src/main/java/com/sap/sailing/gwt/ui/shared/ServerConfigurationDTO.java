package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ServerConfigurationDTO implements IsSerializable {
    private boolean isStandaloneServer;
    private boolean isPublic;
    private boolean isSelfService;

    // for GWT
    ServerConfigurationDTO() {
    }

    public ServerConfigurationDTO(boolean isStandaloneServer, boolean isPublic, boolean isSelfService) {
        this.isStandaloneServer = isStandaloneServer;
        this.isPublic = isPublic;
        this.isSelfService = isSelfService;
    }

    public boolean isStandaloneServer() {
        return isStandaloneServer;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public boolean isSelfService() {
        return isSelfService;
    }
}
