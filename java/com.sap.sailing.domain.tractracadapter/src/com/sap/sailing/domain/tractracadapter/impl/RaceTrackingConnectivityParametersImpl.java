package com.sap.sailing.domain.tractracadapter.impl;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.regattalog.RegattaLogStore;
import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tracking.impl.AbstractRaceTrackingConnectivityParameters;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.tractrac.model.lib.api.event.CreateModelException;
import com.tractrac.model.lib.api.event.IRace;
import com.tractrac.subscription.lib.api.SubscriberInitializationException;

public class RaceTrackingConnectivityParametersImpl extends AbstractRaceTrackingConnectivityParameters {
    public static final String TYPE = "TRAC_TRAC";
    
    private final URL paramURL;
    private final URI liveURI;
    private final URI storedURI;
    private final URI courseDesignUpdateURI;
    private final TimePoint startOfTracking;
    private final TimePoint endOfTracking;
    private final RaceLogStore raceLogStore;
    private final RegattaLogStore regattaLogStore;
    private final DomainFactory domainFactory;
    private final long delayToLiveInMillis;
    private final Duration offsetToStartTimeOfSimulatedRace;
    private final String tracTracUsername;
    private final String tracTracPassword;
    private final String raceStatus;
    private final String raceVisibility;
    private final boolean useInternalMarkPassingAlgorithm;
    private final boolean preferReplayIfAvailable;

    /**
     * @param preferReplayIfAvailable
     *            when a non-{@code null} {@code storedURI} and/or {@code liveURI} are provided and the {@link IRace}
     *            specifies something different and claims to be in replay mode ({@link IRace#getConnectionType} is
     *            {@code File}) then if this parameter is {@code true} the race will be loaded from the replay file
     *            instead of the {@code storedURI}/{@code liveURI} specified. This is particularly useful for restoring
     *            races if since the last connection the race was migrated to a replay file format.
     */
    public RaceTrackingConnectivityParametersImpl(URL paramURL, URI liveURI, URI storedURI, URI courseDesignUpdateURI,
            TimePoint startOfTracking, TimePoint endOfTracking, long delayToLiveInMillis,
            Duration offsetToStartTimeOfSimulatedRace,  boolean useInternalMarkPassingAlgorithm, RaceLogStore raceLogStore, RegattaLogStore regattaLogStore,
            DomainFactory domainFactory, String tracTracUsername, String tracTracPassword, String raceStatus,
            String raceVisibility, boolean trackWind, boolean correctWindDirectionByMagneticDeclination, boolean preferReplayIfAvailable) {
        super(trackWind, correctWindDirectionByMagneticDeclination);
        this.paramURL = paramURL;
        this.liveURI = liveURI;
        this.storedURI = storedURI;
        this.courseDesignUpdateURI = courseDesignUpdateURI;
        this.startOfTracking = startOfTracking;
        this.endOfTracking = endOfTracking;
        this.delayToLiveInMillis = delayToLiveInMillis;
        this.domainFactory = domainFactory;
        this.offsetToStartTimeOfSimulatedRace = offsetToStartTimeOfSimulatedRace;
        this.raceLogStore = raceLogStore;
        this.regattaLogStore = regattaLogStore;
        this.tracTracUsername = tracTracUsername;
        this.tracTracPassword = tracTracPassword;
        this.raceStatus = raceStatus;
        this.raceVisibility = raceVisibility;
        this.useInternalMarkPassingAlgorithm = useInternalMarkPassingAlgorithm;
        this.preferReplayIfAvailable = preferReplayIfAvailable;
    }

    @Override
    public String getTypeIdentifier() {
        return TYPE;
    }

    @Override
    public RaceTracker createRaceTracker(TrackedRegattaRegistry trackedRegattaRegistry, WindStore windStore,
            RaceLogResolver raceLogResolver) throws MalformedURLException, FileNotFoundException, URISyntaxException,
            CreateModelException, SubscriberInitializationException {
        RaceTracker tracker = domainFactory.createRaceTracker(paramURL, liveURI, storedURI, courseDesignUpdateURI,
                startOfTracking, endOfTracking, delayToLiveInMillis, offsetToStartTimeOfSimulatedRace, useInternalMarkPassingAlgorithm, raceLogStore,
                regattaLogStore, windStore, tracTracUsername, tracTracPassword, raceStatus,
                raceVisibility, trackedRegattaRegistry, raceLogResolver, this, preferReplayIfAvailable);
        return tracker;
    }

    @Override
    public RaceTracker createRaceTracker(Regatta regatta, TrackedRegattaRegistry trackedRegattaRegistry,
            WindStore windStore, RaceLogResolver raceLogResolver) throws Exception {
        RaceTracker tracker = domainFactory.createRaceTracker(regatta, paramURL, liveURI, storedURI,
                courseDesignUpdateURI, startOfTracking, endOfTracking, delayToLiveInMillis, offsetToStartTimeOfSimulatedRace, useInternalMarkPassingAlgorithm,
                raceLogStore, regattaLogStore, windStore, tracTracUsername, tracTracPassword, raceStatus,
                raceVisibility, trackedRegattaRegistry, raceLogResolver, this, preferReplayIfAvailable);
        return tracker;
    }

    @Override
    public Object getTrackerID() {
        return TracTracRaceTrackerImpl.createID(paramURL, liveURI, storedURI);
    }

    @Override
    public long getDelayToLiveInMillis() {
        return delayToLiveInMillis;
    }

    public URL getParamURL() {
        return paramURL;
    }

    public URI getLiveURI() {
        return liveURI;
    }

    public URI getStoredURI() {
        return storedURI;
    }

    public URI getCourseDesignUpdateURI() {
        return courseDesignUpdateURI;
    }

    public TimePoint getStartOfTracking() {
        return startOfTracking;
    }

    public TimePoint getEndOfTracking() {
        return endOfTracking;
    }

    public Duration getOffsetToStartTimeOfSimulatedRace() {
        return offsetToStartTimeOfSimulatedRace;
    }

    public String getTracTracUsername() {
        return tracTracUsername;
    }

    public String getTracTracPassword() {
        return tracTracPassword;
    }

    public String getRaceStatus() {
        return raceStatus;
    }

    public String getRaceVisibility() {
        return raceVisibility;
    }

    public boolean isUseInternalMarkPassingAlgorithm() {
        return useInternalMarkPassingAlgorithm;
    }

}
