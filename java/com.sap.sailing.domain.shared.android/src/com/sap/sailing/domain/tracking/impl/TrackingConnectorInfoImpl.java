package com.sap.sailing.domain.tracking.impl;

import java.net.URL;

import com.sap.sailing.domain.tracking.TrackingConnectorInfo;

public class TrackingConnectorInfoImpl implements TrackingConnectorInfo {
    private static final long serialVersionUID = -1681930521305924294L;
    
    private final String trackedBy;
    private final URL webUrl;

    public TrackingConnectorInfoImpl(String trackedBy, URL webUrl) {
        super();
        this.trackedBy = trackedBy;
        this.webUrl = webUrl;
    }

    public String getTrackedBy() {
        return trackedBy;
    }

    public URL getWebUrl() {
        return webUrl;
    }
}
