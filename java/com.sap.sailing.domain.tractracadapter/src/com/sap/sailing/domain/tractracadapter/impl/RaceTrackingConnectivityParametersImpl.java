package com.sap.sailing.domain.tractracadapter.impl;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sailing.domain.tracking.RaceTrackingConnectivityParameters;
import com.sap.sailing.domain.tracking.TrackedEventRegistry;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tractracadapter.DomainFactory;

public class RaceTrackingConnectivityParametersImpl implements RaceTrackingConnectivityParameters {
    private final URL paramURL;
    private final URI liveURI;
    private final URI storedURI;
    private final TimePoint startOfTracking;
    private final TimePoint endOfTracking;
    private final WindStore windStore;
    private final DomainFactory domainFactory;

    public RaceTrackingConnectivityParametersImpl(URL paramURL, URI liveURI, URI storedURI,
            TimePoint startOfTracking, TimePoint endOfTracking, WindStore windStore, DomainFactory domainFactory) {
        super();
        this.paramURL = paramURL;
        this.liveURI = liveURI;
        this.storedURI = storedURI;
        this.startOfTracking = startOfTracking;
        this.endOfTracking = endOfTracking;
        this.windStore = windStore;
        this.domainFactory = domainFactory;
    }

    @Override
    public RaceTracker createRaceTracker(TrackedEventRegistry trackedEventRegistry) throws MalformedURLException,
            FileNotFoundException, URISyntaxException {
        RaceTracker tracker = domainFactory.createRaceTracker(paramURL, liveURI, storedURI, startOfTracking,
                endOfTracking, windStore, trackedEventRegistry);
        return tracker;
    }

    @Override
    public Object getTrackerID() {
        // TODO Auto-generated method stub
        return null;
    }

}
