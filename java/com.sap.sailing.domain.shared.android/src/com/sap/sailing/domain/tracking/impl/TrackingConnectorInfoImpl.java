package com.sap.sailing.domain.tracking.impl;

import java.net.URL;

import com.sap.sailing.domain.tracking.TrackingConnectorInfo;

public class TrackingConnectorInfoImpl implements TrackingConnectorInfo {
    String trackedBy;
    URL webUrl;

    public TrackingConnectorInfoImpl() {
    }

    public TrackingConnectorInfoImpl(String trackedBy, URL webUrl) {
        super();
        this.trackedBy = trackedBy;
        this.webUrl = webUrl;
    }

    public String getTrackedBy() {
        return trackedBy;
    }

    public void setTrackedBy(String trackedBy) {
        this.trackedBy = trackedBy;
    }

    public URL getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(URL webUrl) {
        this.webUrl = webUrl;
    }

}
