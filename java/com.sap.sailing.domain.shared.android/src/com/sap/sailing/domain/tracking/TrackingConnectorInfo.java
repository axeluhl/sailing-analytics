package com.sap.sailing.domain.tracking;

import java.io.Serializable;
import java.net.URL;

public interface TrackingConnectorInfo extends Serializable {
    String getTrackedBy();
    URL getWebUrl();
}
