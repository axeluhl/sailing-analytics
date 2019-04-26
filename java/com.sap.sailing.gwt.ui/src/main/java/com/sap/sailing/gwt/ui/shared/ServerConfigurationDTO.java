package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.security.shared.dto.StrippedUserGroupDTO;

public class ServerConfigurationDTO implements IsSerializable {
    private boolean isStandaloneServer;
    private Boolean isPublic;
    private Boolean isSelfService;
    private StrippedUserGroupDTO serverDefaultTenant;

    // for GWT
    ServerConfigurationDTO() {
    }

    /**
     * serverDefaultTenant is only for transfer to client, it is not used in the server configuration update
     */
    public ServerConfigurationDTO(boolean isStandaloneServer, Boolean isPublic, Boolean isSelfService,
            StrippedUserGroupDTO serverDefaultTenant) {
        this.isStandaloneServer = isStandaloneServer;
        this.isPublic = isPublic;
        this.isSelfService = isSelfService;
        this.serverDefaultTenant = serverDefaultTenant;
    }

    public StrippedUserGroupDTO getServerDefaultTenant() {
        return serverDefaultTenant;
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
