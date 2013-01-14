package com.sap.sailing.domain.tractracadapter;

import java.net.URI;
import java.net.URL;

import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.tracking.RaceTracker;

public interface TracTracRaceTracker extends RaceTracker {
    /**
     * returns the paramURL, liveURI and storedURI for the TracTrac connection maintained by this tracker
     */
    Triple<URL, URI, URI> getID();
    
}
