package com.sap.sailing.domain.tracking;

import java.net.URL;

public interface TrackingConnectorInfo {
    public String getTrackedBy();
    public void setTrackedBy(String trackedBy);
    public URL getWebUrl();
    public void setWebUrl(URL webUrl);
}
