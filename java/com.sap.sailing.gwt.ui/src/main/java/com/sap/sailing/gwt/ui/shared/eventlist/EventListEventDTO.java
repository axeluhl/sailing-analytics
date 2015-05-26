package com.sap.sailing.gwt.ui.shared.eventlist;

import com.sap.sailing.gwt.ui.shared.general.EventMetadataDTO;

public class EventListEventDTO extends EventMetadataDTO {
    private String baseURL;
    private boolean isOnRemoteServer;

    public String getBaseURL() {
        return baseURL;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public boolean isOnRemoteServer() {
        return isOnRemoteServer;
    }

    public void setOnRemoteServer(boolean isOnRemoteServer) {
        this.isOnRemoteServer = isOnRemoteServer;
    }
}
