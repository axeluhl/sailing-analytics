package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ServerConfigurationDTO implements IsSerializable {
    private boolean isStandaloneServer;
    private Boolean isPublic;
    private Boolean isSelfService;

    // for GWT
    ServerConfigurationDTO() {
    }

    public ServerConfigurationDTO(boolean isStandaloneServer, Boolean isPublic, Boolean isSelfService) {
        this.isStandaloneServer = isStandaloneServer;
        this.isPublic = isPublic;
        this.isSelfService = isSelfService;
    }

    public boolean isStandaloneServer() {
        return isStandaloneServer;
    }

    public Boolean isPublic() {
        return isPublic;
    }

    public Boolean isSelfService() {
        return isSelfService;
    }
}
