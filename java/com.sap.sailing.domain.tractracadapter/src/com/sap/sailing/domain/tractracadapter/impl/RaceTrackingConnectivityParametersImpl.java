package com.sap.sailing.domain.tractracadapter.impl;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.racecommittee.RaceCommitteeStore;
import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sailing.domain.tracking.RaceTrackingConnectivityParameters;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tractracadapter.DomainFactory;

public class RaceTrackingConnectivityParametersImpl implements RaceTrackingConnectivityParameters {
    private final URL paramURL;
    private final URI liveURI;
    private final URI storedURI;
    private final TimePoint startOfTracking;
    private final TimePoint endOfTracking;
    private final WindStore windStore;
    private final RaceCommitteeStore raceCommitteeStore;
    private final DomainFactory domainFactory;
    private final long delayToLiveInMillis;
    private final boolean simulateWithStartTimeNow;

    public RaceTrackingConnectivityParametersImpl(URL paramURL, URI liveURI, URI storedURI, TimePoint startOfTracking,
            TimePoint endOfTracking, long delayToLiveInMillis, boolean simulateWithStartTimeNow, WindStore windStore,
            DomainFactory domainFactory, RaceCommitteeStore raceCommitteeStore) {
        super();
        this.paramURL = paramURL;
        this.liveURI = liveURI;
        this.storedURI = storedURI;
        this.startOfTracking = startOfTracking;
        this.endOfTracking = endOfTracking;
        this.delayToLiveInMillis = delayToLiveInMillis;
        this.windStore = windStore;
        this.raceCommitteeStore = raceCommitteeStore;
        this.domainFactory = domainFactory;
        this.simulateWithStartTimeNow = simulateWithStartTimeNow;
    }

    @Override
    public RaceTracker createRaceTracker(TrackedRegattaRegistry trackedRegattaRegistry) throws MalformedURLException,
            FileNotFoundException, URISyntaxException {
        RaceTracker tracker = domainFactory.createRaceTracker(paramURL, liveURI, storedURI, startOfTracking,
                endOfTracking, delayToLiveInMillis, simulateWithStartTimeNow, windStore, trackedRegattaRegistry, raceCommitteeStore);
        return tracker;
    }

    @Override
    public RaceTracker createRaceTracker(Regatta regatta, TrackedRegattaRegistry trackedRegattaRegistry)
            throws Exception {
        RaceTracker tracker = domainFactory.createRaceTracker(regatta, paramURL, liveURI, storedURI, startOfTracking,
                endOfTracking, delayToLiveInMillis, simulateWithStartTimeNow, windStore, trackedRegattaRegistry, raceCommitteeStore);
        return tracker;
    }

    @Override
    public Util.Triple<URL, URI, URI> getTrackerID() {
        return TracTracRaceTrackerImpl.createID(paramURL, liveURI, storedURI);
    }

    @Override
    public long getDelayToLiveInMillis() {
        return delayToLiveInMillis;
    }

}
