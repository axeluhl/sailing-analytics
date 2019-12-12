package com.sap.sailing.domain.tracking;

import java.io.Serializable;
import java.net.URL;

/**
 * Identifies the tracking connector that was used to create a TrackedRace.
 * Further the Connector can provide a webUrl, that leads to an event web page.
 */
public interface TrackingConnectorInfo extends Serializable {
    
    /**
     * gets a String representation of the Tracking-Service used to Track the Race
     */
    String getTrackedBy();
    
    /**
     * gets a {@link URL} representation of the web-URL associated with the Event; 
     * may be {@code null} if the API of the respective Tracking-Service does not provide a URL;
     */
    URL getWebUrl();
}
