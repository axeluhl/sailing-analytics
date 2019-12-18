package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.core.shared.GwtIncompatible;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.tracking.TrackingConnectorInfo;

public class TrackingConnectorInfoDTO implements IsSerializable {
    private String trackedBy;
    private String webUrl;

    TrackingConnectorInfoDTO() {} // GWT serialization

    @GwtIncompatible
    public TrackingConnectorInfoDTO(TrackingConnectorInfo trackingConnectorInfo) {
        this.trackedBy = trackingConnectorInfo.getTrackedBy();
        this.webUrl = trackingConnectorInfo.getWebUrl() == null ? null : trackingConnectorInfo.getWebUrl().toString();
    }

    public String getTrackedBy() {
        return trackedBy;
    }

    public String getWebUrl() {
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
        TrackingConnectorInfoDTO other = (TrackingConnectorInfoDTO) obj;
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
