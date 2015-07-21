package com.sap.sailing.gwt.ui.shared.dispatch.start;

import com.sap.sailing.gwt.ui.shared.dispatch.DTO;
import com.sap.sailing.gwt.ui.shared.general.EventReferenceDTO;

public class EventQuickfinderDTO extends EventReferenceDTO implements DTO, Comparable<EventQuickfinderDTO> {
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

    @Override
    public int compareTo(EventQuickfinderDTO o) {
        return getDisplayName().compareTo(o.getDisplayName());
    }
}
