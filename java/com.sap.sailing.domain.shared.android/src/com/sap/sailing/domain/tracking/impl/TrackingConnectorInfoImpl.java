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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((trackedBy == null) ? 0 : trackedBy.hashCode());
        result = prime * result + ((webUrl == null) ? 0 : webUrl.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TrackingConnectorInfoImpl other = (TrackingConnectorInfoImpl) obj;
        if (trackedBy == null) {
            if (other.trackedBy != null)
                return false;
        } else if (!trackedBy.equals(other.trackedBy))
            return false;
        if (webUrl == null) {
            if (other.webUrl != null)
                return false;
        } else if (!webUrl.equals(other.webUrl))
            return false;
        return true;
    }
}
