package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.SailingServerConfiguration;

public class SailingServerConfigurationImpl implements SailingServerConfiguration {
    private static final long serialVersionUID = -639945748229124558L;

    /** indicates if the server is running in standalone mode or not  */
    private boolean isStandaloneServer;

    /** indicates if debranding is active */
    private Boolean debrandingActive;

    public SailingServerConfigurationImpl(boolean isStandaloneServer) {
        this.isStandaloneServer = isStandaloneServer;
        this.debrandingActive = false; // default
    }

    public SailingServerConfigurationImpl(boolean isStandaloneServer, Boolean debrandingActive) {
        this.isStandaloneServer = isStandaloneServer;
        this.debrandingActive = debrandingActive;
    }

    @Override
    public boolean isStandaloneServer() {
        return isStandaloneServer;
    }

    @Override
    public void setStandaloneServer(boolean isStandaloneServer) {
        this.isStandaloneServer = isStandaloneServer;
    }

    public Boolean getDebrandingActive() {
        return debrandingActive;
    }

    public void setDebrandingActive(Boolean debrandingActive) {
        this.debrandingActive = debrandingActive;
    }
}
