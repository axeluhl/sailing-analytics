package com.sap.sailing.gwt.home.communication.eventview;

import com.google.gwt.user.client.rpc.IsSerializable;

public class TrackingConnectorInfoDTO implements IsSerializable {
    private String trackedBy;
    private String webUrl;
    
    TrackingConnectorInfoDTO() {} // GWT serialization
    
    public TrackingConnectorInfoDTO(String trackedBy, String webUrl) {
        this.trackedBy = trackedBy;
        this.webUrl = webUrl;
    }
    
    public String getTrackedBy() {
        return trackedBy;
    }
    
    public String getWebUrl() {
        return webUrl;
    }
}
