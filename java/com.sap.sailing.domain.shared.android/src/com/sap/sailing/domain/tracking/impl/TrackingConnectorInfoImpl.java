package com.sap.sailing.domain.tracking.impl;

import java.net.URL;
import java.util.UUID;

import com.sap.sailing.domain.common.tracking.TrackingConnectorType;
import com.sap.sailing.domain.tracking.TrackingConnectorInfo;

public class TrackingConnectorInfoImpl implements TrackingConnectorInfo {
    private static final long serialVersionUID = 7970268841592389145L;
    private final TrackingConnectorType trackingConnectorType;
    private final URL webUrl;
    private final UUID uuid;

    public TrackingConnectorInfoImpl(TrackingConnectorType trackingConnectorType, URL webUrl, UUID itemUUID) {
        super();
        this.trackingConnectorType = trackingConnectorType;
        this.webUrl = webUrl;
        this.uuid = itemUUID;
    }

    public TrackingConnectorType getTrackingConnectorType() {
        return trackingConnectorType;
    }

    public URL getWebUrl() {
        return webUrl;
    }
    
    public UUID getUuid() {
    	return uuid;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((trackingConnectorType == null) ? 0 : trackingConnectorType.hashCode());
        result = prime * result + ((webUrl == null) ? 0 : webUrl.hashCode());
        result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
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
        if (trackingConnectorType == null) {
            if (other.trackingConnectorType != null)
                return false;
        } else if (!trackingConnectorType.equals(other.trackingConnectorType))
            return false;
        if (webUrl == null) {
            if (other.webUrl != null)
                return false;
        } else if (!webUrl.equals(other.webUrl))
            return false;
        if (uuid == null) {
            if (other.uuid != null)
                return false;
        } else if (!uuid.equals(other.uuid))
            return false;
        return true;
    }
}
