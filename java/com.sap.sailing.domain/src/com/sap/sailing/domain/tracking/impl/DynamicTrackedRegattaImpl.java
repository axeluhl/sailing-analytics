package com.sap.sailing.domain.tracking.impl;

import java.util.Optional;

import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Sideline;
import com.sap.sailing.domain.tracking.DynamicRaceDefinitionSet;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sse.util.ThreadLocalTransporter;

public class DynamicTrackedRegattaImpl extends TrackedRegattaImpl implements DynamicTrackedRegatta {
    private static final long serialVersionUID = -90155868534737120L;

    public DynamicTrackedRegattaImpl(Regatta regatta) {
        super(regatta);
    }

    @Override
    public DynamicTrackedRace getTrackedRace(RaceDefinition race) {
        return (DynamicTrackedRace) super.getTrackedRace(race);
    }

    @SuppressWarnings("unchecked") // the tracked races of a dynamic tracked regatta are always DynamicTrackedRace objects; see also getTrackedRace(RaceDefinition)
    @Override
    public Iterable<DynamicTrackedRace> getTrackedRaces() {
        return (Iterable<DynamicTrackedRace>) super.getTrackedRaces();
    }

    @Override
    public DynamicTrackedRace getExistingTrackedRace(RaceDefinition race) {
        return (DynamicTrackedRace) super.getExistingTrackedRace(race);
    }

    @Override
    public DynamicTrackedRace createTrackedRace(RaceDefinition raceDefinition, Iterable<Sideline> sidelines, WindStore windStore,
            long delayToLiveInMillis, long millisecondsOverWhichToAverageWind, long millisecondsOverWhichToAverageSpeed,
            DynamicRaceDefinitionSet raceDefinitionSetToUpdate, boolean useMarkPassingCalculator, RaceLogResolver raceLogResolver,
            Optional<ThreadLocalTransporter> threadLocalTransporter) {
        return (DynamicTrackedRace) super.createTrackedRace(raceDefinition, sidelines, windStore, delayToLiveInMillis,
                millisecondsOverWhichToAverageWind,
                millisecondsOverWhichToAverageSpeed, raceDefinitionSetToUpdate, useMarkPassingCalculator, raceLogResolver, threadLocalTransporter);
    }
}
