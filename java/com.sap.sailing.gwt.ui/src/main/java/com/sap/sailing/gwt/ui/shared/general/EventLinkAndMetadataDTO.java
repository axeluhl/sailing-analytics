package com.sap.sailing.gwt.ui.shared.general;

import com.sap.sailing.gwt.ui.shared.start.StageEventType;

public class EventLinkAndMetadataDTO extends EventMetadataDTO {
    
    private String baseURL;
    private boolean isOnRemoteServer;
    
    public StageEventType getStageType() {
        return getState() == EventState.RUNNING ? StageEventType.RUNNING : StageEventType.POPULAR;
    }

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
