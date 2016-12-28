package com.sap.sailing.domain.swisstimingreplayadapter.impl;

import java.util.Collections;
import java.util.Set;

import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.regattalog.RegattaLogStore;
import com.sap.sailing.domain.swisstimingadapter.DomainFactory;
import com.sap.sailing.domain.swisstimingreplayadapter.SwissTimingReplayService;
import com.sap.sailing.domain.tracking.AbstractRaceTrackerImpl;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.RaceHandle;
import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sailing.domain.tracking.RaceTrackingConnectivityParameters;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sse.common.Util;

public class SwissTimingReplayConnectivityParameters implements RaceTrackingConnectivityParameters {
    public static final String TYPE = "SWISS_TIMING_REPLAY";
    
    private final boolean useInternalMarkPassingAlgorithm;
    private final DomainFactory domainFactory;
    private final String boatClassName;
    private final RaceLogStore raceLogStore;
    private final RegattaLogStore regattaLogStore;
    private final String raceID;
    private final String link;
    private final SwissTimingReplayService replayService;
    
    private class SwissTimingReplayRaceTracker extends AbstractRaceTrackerImpl {
        private final WindStore windStore;
        private SwissTimingReplayToDomainAdapter listener;

        private SwissTimingReplayRaceTracker(WindStore windStore, SwissTimingReplayToDomainAdapter listener) {
            this.windStore = windStore;
            this.listener = listener;
        }

        @Override
        public Regatta getRegatta() {
            return listener.getRegatta();
        }

        @Override
        public Set<RaceDefinition> getRaces() {
            RaceDefinition race;
            try {
                race = listener.getRaceDefinition(raceID, 1 /* very short timeout */);
                final Set<RaceDefinition> result;
                if (race == null) {
                    result = Collections.emptySet();
                } else {
                    result = Collections.singleton(race);
                }
                return result;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public DynamicTrackedRegatta getTrackedRegatta() {
            return listener.getTrackedRegatta();
        }

        @Override
        public RaceHandle getRacesHandle() {
            return new RaceHandle() {
                @Override
                public Regatta getRegatta() {
                    return listener.getRegatta();
                }

                @Override
                public RaceDefinition getRace() {
                    try {
                        return listener.getRaceDefinition(raceID);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public RaceDefinition getRace(long timeoutInMilliseconds) {
                    try {
                        return listener.getRaceDefinition(raceID, timeoutInMilliseconds);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public DynamicTrackedRegatta getTrackedRegatta() {
                    return SwissTimingReplayRaceTracker.this.getTrackedRegatta();
                }

                @Override
                public RaceTracker getRaceTracker() {
                    return SwissTimingReplayRaceTracker.this;
                }
            };
        }

        @Override
        public WindStore getWindStore() {
            return windStore;
        }

        @Override
        public Object getID() {
            return getTrackerID();
        }
    }

    public SwissTimingReplayConnectivityParameters(RegattaIdentifier regattaToAddTo, String link, String raceID,
            String regattaName, String boatClassName, boolean useInternalMarkPassingAlgorithm,
            DomainFactory domainFactory, SwissTimingReplayService replayService, RaceLogStore raceLogStore, RegattaLogStore regattaLogStore) {
        this.link = link;
        this.raceID = raceID;
        this.boatClassName = boatClassName;
        this.useInternalMarkPassingAlgorithm = useInternalMarkPassingAlgorithm;
        this.domainFactory = domainFactory;
        this.replayService = replayService;
        this.raceLogStore = raceLogStore;
        this.regattaLogStore = regattaLogStore;
    }

    @Override
    public RaceTracker createRaceTracker(TrackedRegattaRegistry trackedRegattaRegistry, final WindStore windStore,
            RaceLogResolver raceLogResolver) throws Exception {
        SwissTimingReplayToDomainAdapter listener = new SwissTimingReplayToDomainAdapter(/* regatta */ null,
                raceID, domainFactory.getBaseDomainFactory().getOrCreateBoatClass(boatClassName),
                domainFactory, trackedRegattaRegistry, useInternalMarkPassingAlgorithm, raceLogResolver, raceLogStore, regattaLogStore);
        replayService.loadRaceData(link, listener);
        return new SwissTimingReplayRaceTracker(windStore, listener);
    }

    @Override
    public RaceTracker createRaceTracker(Regatta regatta, TrackedRegattaRegistry trackedRegattaRegistry,
            WindStore windStore, RaceLogResolver raceLogResolver) throws Exception {
        SwissTimingReplayToDomainAdapter listener = new SwissTimingReplayToDomainAdapter(regatta, raceID,
                domainFactory.getBaseDomainFactory().getOrCreateBoatClass(boatClassName), domainFactory,
                trackedRegattaRegistry, useInternalMarkPassingAlgorithm, raceLogResolver, raceLogStore,
                regattaLogStore);
        replayService.loadRaceData(link, listener);
        return new SwissTimingReplayRaceTracker(windStore, listener);
    }

    @Override
    public Object getTrackerID() {
        return new Util.Pair<>(link, raceID);
    }

    @Override
    public long getDelayToLiveInMillis() {
        return 0;
    }

    @Override
    public String getTypeIdentifier() {
        return TYPE;
    }

}
