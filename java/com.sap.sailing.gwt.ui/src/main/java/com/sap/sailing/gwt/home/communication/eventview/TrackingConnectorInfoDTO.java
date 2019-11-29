package com.sap.sailing.gwt.home.communication.eventview;

import com.google.gwt.user.client.rpc.IsSerializable;

public class TrackingConnectorInfoDTO implements IsSerializable {
    private String trackedBy;
    private String webUrl;
    
    public TrackingConnectorInfoDTO() {}
    
    public TrackingConnectorInfoDTO(String trackedBy, String webUrl) {
        this.trackedBy = trackedBy;
        this.webUrl = webUrl;
    }
    
    public String getTrackedBy() {
        return trackedBy;
    }
    public void setTrackedBy(String trackedBy) {
        this.trackedBy = trackedBy;
    }
    public String getWebUrl() {
        return webUrl;
    }
    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }
}
