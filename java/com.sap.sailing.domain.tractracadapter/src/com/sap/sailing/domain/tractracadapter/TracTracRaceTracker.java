package com.sap.sailing.domain.tractracadapter;

import java.net.URI;
import java.net.URL;

import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sse.common.UtilNew;

public interface TracTracRaceTracker extends RaceTracker {
    /**
     * returns the paramURL, liveURI and storedURI for the TracTrac connection maintained by this tracker
     */
    UtilNew.Triple<URL, URI, URI> getID();
    
}
