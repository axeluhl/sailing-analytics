package com.sap.sailing.domain.yellowbrickadapter.impl;

import java.util.UUID;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.domain.leaderboard.LeaderboardGroupResolver;
import com.sap.sailing.domain.racelog.RaceLogAndTrackedRaceResolver;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.regattalog.RegattaLogStore;
import com.sap.sailing.domain.tracking.AbstractRaceTrackerImpl;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.RaceHandle;
import com.sap.sailing.domain.tracking.RaceTrackingHandler;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.yellowbrickadapter.YellowBrickRaceTrackingConnectivityParams;
import com.sap.sailing.domain.yellowbrickadapter.YellowBrickTrackingAdapter;

public class YellowBrickRaceTrackerImpl extends AbstractRaceTrackerImpl<YellowBrickRaceTrackingConnectivityParams> {
    private final String DEFAULT_REGATTA_NAME_PREFIX = "YellowBrick ";
    private final Regatta regatta;
    
    public YellowBrickRaceTrackerImpl(YellowBrickRaceTrackingConnectivityParams connectivityParams, Regatta regatta,
            TrackedRegattaRegistry trackedRegattaRegistry, WindStore windStore,
            RaceLogAndTrackedRaceResolver raceLogResolver, LeaderboardGroupResolver leaderboardGroupResolver,
            long timeoutInMilliseconds, RaceTrackingHandler raceTrackingHandler, RaceLogStore raceLogStore,
            RegattaLogStore regattaLogStore, DomainFactory baseDomainFactory,
            YellowBrickTrackingAdapter yellowBrickTrackingAdapter) {
        super(connectivityParams);
        this.regatta = getOrCreateEffectiveRegatta(DEFAULT_REGATTA_NAME_PREFIX+connectivityParams.getRaceUrl(), trackedRegattaRegistry, regatta);
    }

    /**
     * If {@code regatta} is set to a valid {@link Regatta}, it is returned unchanged. Otherwise, a default
     * regatta is looked up in the {@link TrackedRegattaRegistry} passed, and if not found, it is created
     * as a default regatta with a Time-on-Time/Time-on-Distance ranking metric.
     */
    private Regatta getOrCreateEffectiveRegatta(String name, TrackedRegattaRegistry trackedRegattaRegistry, Regatta regatta) {
        final Regatta result;
        if (regatta != null) {
            result = regatta;
        } else {
            result = trackedRegattaRegistry.getOrCreateDefaultRegatta(name, BoatClassMasterdata.IRC.name(), UUID.randomUUID());
        }
        return result;
    }

    @Override
    public Regatta getRegatta() {
        return regatta;
    }

    @Override
    public RaceDefinition getRace() {
        // TODO Implement YellowBrickRaceTrackerImpl.getRace(...)
        return null;
    }

    @Override
    public RaceHandle getRaceHandle() {
        // TODO Implement YellowBrickRaceTrackerImpl.getRaceHandle(...)
        return null;
    }

    @Override
    public DynamicTrackedRegatta getTrackedRegatta() {
        // TODO Implement YellowBrickRaceTrackerImpl.getTrackedRegatta(...)
        return null;
    }

    @Override
    public WindStore getWindStore() {
        // TODO Implement YellowBrickRaceTrackerImpl.getWindStore(...)
        return null;
    }

    @Override
    public Object getID() {
        // TODO Implement YellowBrickRaceTrackerImpl.getID(...)
        return null;
    }

}
